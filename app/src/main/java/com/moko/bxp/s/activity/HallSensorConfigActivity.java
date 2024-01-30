package com.moko.bxp.s.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityHallConfigBinding;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 霍尔传感器
 */
public class HallSensorConfigActivity extends BaseActivity {
    private boolean mReceiverTag = false;
    private boolean isHallStoreEnable;
    private ActivityHallConfigBinding mBind;
    private final SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS, Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityHallConfigBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getMagneticTriggerCount());
            orderTasks.add(OrderTaskAssembler.getHallStoreEnable());
            orderTasks.add(OrderTaskAssembler.getCurrentTime());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
        setListener();
        MokoSupport.getInstance().enableHallStatusNotify();
    }

    private void setListener() {
        mBind.ivDoorStatusStore.setOnClickListener(v -> {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setHallStoreEnable(isHallStoreEnable ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getHallStoreEnable());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        });
        mBind.tvUpdate.setOnClickListener(v -> {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setCurrentTime());
            orderTasks.add(OrderTaskAssembler.getCurrentTime());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
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
                if (Objects.requireNonNull(orderCHAR) == OrderCHAR.CHAR_PARAMS) {
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
                                case KEY_HALL_POWER_ENABLE:
                                    if (result == 0xAA) {
                                        ToastUtils.showToast(this, "Success");
                                        //打开了霍尔关机功能
                                        EventBus.getDefault().post("hallDisable");
                                        back();
                                    } else {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    }
                                    break;

                                case KEY_SYNC_CURRENT_TIME:
                                case KEY_HALL_STORE_ENABLE:
                                case KEY_MAGNETIC_TRIGGER_COUNT:
                                    if (result == 0xAA) {
                                        ToastUtils.showToast(this, "Success");
                                    } else {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    }
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_MAGNETIC_TRIGGER_COUNT:
                                    if (length == 2) {
                                        int count = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                        mBind.tvTriggerCount.setText(String.valueOf(count));
                                    }
                                    break;

                                case KEY_HALL_STORE_ENABLE:
                                    if (length == 1) {
                                        isHallStoreEnable = (value[4] & 0xFF) == 1;
                                        mBind.ivDoorStatusStore.setImageResource(isHallStoreEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                    }
                                    break;

                                case KEY_SYNC_CURRENT_TIME:
                                    if (length == 4) {
                                        int time = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 4 + length));
                                        mBind.tvUpdateDate.setText(sdf.format(time * 1000L));
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                byte[] value = response.responseValue;
                if (null != value && value.length == 5) {
                    int header = value[0] & 0xFF;// 0xEB
                    int flag = value[1] & 0xFF;// read or write
                    int cmd = value[2] & 0xFF;
                    if (header != 0xEB) return;
                    int length = value[3] & 0xFF;
                    if (length == 1 && flag == 2 && cmd == 0x90) {
                        int status = value[4] & 0xFF;
                        mBind.tvMagnetStatus.setText(status == 0 ? "Closed" : "Open");
                    }
                }
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        dismissSyncProgressDialog();
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        MokoSupport.getInstance().disableHallStatusNotify();
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

    private void back() {
        // 关闭通知
        MokoSupport.getInstance().disableHallStatusNotify();
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onBack(View view) {
        back();
    }

    public void onClear(View view) {
        if (isWindowLocked()) return;
        // 保存
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.clearMagneticTriggerCount());
        orderTasks.add(OrderTaskAssembler.getMagneticTriggerCount());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onHallSensorEnable(View view) {
        if (isWindowLocked()) return;
        //能到这里霍尔关机功能是禁用的
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning！");
        dialog.setMessage("*If you enable it, you will not be able to use the Hall trigger and count functions");
        dialog.setConfirm("OK");
        dialog.setCancel("Cancel");
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setTurnOffByButton(1));
        });
        dialog.show(getSupportFragmentManager());
    }
}
