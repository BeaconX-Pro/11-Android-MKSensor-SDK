package com.moko.bxp.s.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityQuickSwitchBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class QuickSwitchActivity extends BaseActivity {
    private ActivityQuickSwitchBinding mBind;
    private boolean enablePasswordVerify;
    private boolean enableLedIndicator;
    private boolean enableConnect;
    private boolean enableTagIdAutoFill;
    private boolean resetBeaconByButton;
    private boolean turnOffByButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityQuickSwitchBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>(8);
            orderTasks.add(OrderTaskAssembler.getConnectStatus());
            orderTasks.add(OrderTaskAssembler.getTriggerLedStatus());
            orderTasks.add(OrderTaskAssembler.getVerifyPasswordEnable());
            orderTasks.add(OrderTaskAssembler.getTagIdAutoFillStatus());
            orderTasks.add(OrderTaskAssembler.getResetByButtonEnable());
            orderTasks.add(OrderTaskAssembler.getButtonTurnOffEnable());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
        setListener();
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (value.length > 4) {
                            int header = value[0] & 0xFF;// 0xEB
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = value[2] & 0xFF;
                            if (header != 0xEB) return;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) return;
                            int length = value[3] & 0xFF;
                            if (flag == 0x01 && length == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_CONNECT_ENABLE:
                                    case KEY_TRIGGER_LED_ENABLE:
                                    case KEY_TAG_ID_AUTO_FILL_ENABLE:
                                    case KEY_BUTTON_RESET_ENABLE:
                                    case KEY_BUTTON_TURN_OFF_ENABLE:
                                        if (result != 0xAA) {
                                            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Success");
                                        }
                                        break;
                                }
                            } else if (flag == 0x00 && length == 1) {
                                // read
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_TRIGGER_LED_ENABLE:
                                        this.enableLedIndicator = result == 1;
                                        setStatus(enableLedIndicator, mBind.ivTriggerLed, mBind.tvTriggerLed);
                                        break;

                                    case KEY_CONNECT_ENABLE:
                                        this.enableConnect = result == 1;
                                        setStatus(enableConnect, mBind.ivEnableConnect, mBind.tvConnectEnableStatus);
                                        break;

                                    case KEY_TAG_ID_AUTO_FILL_ENABLE:
                                        this.enableTagIdAutoFill = result == 1;
                                        setStatus(enableTagIdAutoFill, mBind.ivEnableTagId, mBind.tvTagIdEnableStatus);
                                        break;

                                    case KEY_BUTTON_RESET_ENABLE:
                                        this.resetBeaconByButton = result == 1;
                                        setStatus(resetBeaconByButton, mBind.ivEnableReset, mBind.tvResetEnableStatus);
                                        break;

                                    case KEY_BUTTON_TURN_OFF_ENABLE:
                                        this.turnOffByButton = result == 1;
                                        setStatus(turnOffByButton, mBind.ivEnableTurnOff, mBind.tvTurnOffEnableStatus);
                                        break;
                                }
                            }
                        }
                        break;
                    case CHAR_PASSWORD:
                        if (value.length > 4) {
                            int header = value[0] & 0xFF;// 0xEB
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = value[2] & 0xFF;
                            if (header != 0xEB) return;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) return;
                            int length = value[3] & 0xFF;
                            if (flag == 0x01 && length == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                if (configKeyEnum == ParamsKeyEnum.KEY_VERIFY_PASSWORD_ENABLE) {
                                    if (result != 0xAA) {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Success");
                                    }
                                }
                            } else if (flag == 0x00 && length == 1) {
                                // read
                                int result = value[4] & 0xFF;
                                this.enablePasswordVerify = result == 1;
                                setStatus(enablePasswordVerify, mBind.ivEnablePwd, mBind.tvPwdEnableStatus);
                            }
                        }
                        break;
                }
            }
        });
    }

    private void setListener() {
        mBind.ivEnableConnect.setOnClickListener(v -> {
            if (enableConnect) {
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setTitle("Warning！");
                dialog.setMessage("Are you sure to set the Beacon non-connectable？");
                dialog.setConfirm("OK");
                dialog.setOnAlertConfirmListener(() -> setConnectEnable(true));
                dialog.show(getSupportFragmentManager());
            } else {
                setConnectEnable(false);
            }
        });
        mBind.ivTriggerLed.setOnClickListener(v -> {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setTriggerIndicatorStatus(enableLedIndicator ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getTriggerLedStatus());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        mBind.ivEnablePwd.setOnClickListener(v -> {
            if (enablePasswordVerify) {
                final AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setTitle("Warning！");
                dialog.setMessage("If Password verification is disabled, it will not need password to connect the Beacon.");
                dialog.setConfirm("OK");
                dialog.setOnAlertConfirmListener(() -> setVerifyPasswordEnable(false));
                dialog.show(getSupportFragmentManager());
            } else {
                setVerifyPasswordEnable(true);
            }
        });
        mBind.ivEnableTagId.setOnClickListener(v -> {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setTagIdAutoFill(enableTagIdAutoFill ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getTagIdAutoFillStatus());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        });
        mBind.ivEnableReset.setOnClickListener(v -> {
            if (resetBeaconByButton) {
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setTitle("Warning！");
                dialog.setMessage("If Button reset is disabled, you cannot reset the Beacon by button operation.");
                dialog.setConfirm("OK");
                dialog.setOnAlertConfirmListener(() -> setResetBeaconByButton(true));
                dialog.show(getSupportFragmentManager());
            } else {
                setResetBeaconByButton(false);
            }
        });
        mBind.ivEnableTurnOff.setOnClickListener(v -> {
            if (turnOffByButton) {
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setTitle("Warning！");
                dialog.setMessage("If this function is disabled, you cannot power off the Beacon by button.");
                dialog.setConfirm("OK");
                dialog.setOnAlertConfirmListener(() -> setTurnOffByButton(true));
                dialog.show(getSupportFragmentManager());
            } else {
                setTurnOffByButton(false);
            }
        });
    }

    private void setTurnOffByButton(boolean enable) {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.setButtonTurnOffEnable(enable ? 0 : 1));
        orderTasks.add(OrderTaskAssembler.getButtonTurnOffEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    private void setResetBeaconByButton(boolean enable) {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.setResetByButton(enable ? 0 : 1));
        orderTasks.add(OrderTaskAssembler.getResetByButtonEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    private void setConnectEnable(boolean enable) {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.setConnectStatus(enable ? 0 : 1));
        orderTasks.add(OrderTaskAssembler.getConnectStatus());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    private void setStatus(boolean enable, ImageView imageView, TextView textView) {
        imageView.setImageResource(enable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        textView.setText(enable ? "Enable" : "Disable");
        textView.setEnabled(enable);
    }

    public void setVerifyPasswordEnable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setVerifyPasswordEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getVerifyPasswordEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                    dismissSyncProgressDialog();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    public void onBack(View view) {
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        Intent intent = new Intent();
        intent.putExtra("pwdEnable", enablePasswordVerify);
        intent.putExtra(AppConstants.EXTRA_KEY1, turnOffByButton);
        intent.putExtra(AppConstants.EXTRA_KEY2, resetBeaconByButton);
        setResult(RESULT_OK, intent);
        finish();
    }
}
