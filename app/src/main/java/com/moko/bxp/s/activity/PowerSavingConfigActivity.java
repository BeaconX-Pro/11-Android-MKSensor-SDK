package com.moko.bxp.s.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityPowerSavingConfigBinding;
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
import java.util.Arrays;

public class PowerSavingConfigActivity extends BaseActivity {
    private ActivityPowerSavingConfigBinding mBind;
    public boolean isEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityPowerSavingConfigBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mBind.etStaticTriggerTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String triggerTime = editable.toString();
                mBind.tvStaticTriggerTimeTips.setText(getString(R.string.static_trigger_time_tips, triggerTime));
            }
        });
        mBind.tvStaticTriggerTimeTips.setText(getString(R.string.static_trigger_time_tips, ""));
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getPowerSavingStaticTriggerTime());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
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
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_POWER_SAVING_STATIC_TRIGGER_TIME) {
                                if (result == 0xAA) {
                                    ToastUtils.showToast(this, "Success");
                                } else {
                                    ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                }
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_POWER_SAVING_STATIC_TRIGGER_TIME) {
                                if (length == 2) {
                                    int time = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                    isEnable = time > 0;
                                    mBind.ivPowerSavingMode.setImageResource(isEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                    mBind.clStaticTriggerTime.setVisibility(isEnable ? View.VISIBLE : View.GONE);
                                    if (isEnable) {
                                        mBind.etStaticTriggerTime.setText(String.valueOf(time));
                                        mBind.etStaticTriggerTime.setSelection(mBind.etStaticTriggerTime.getText().length());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            String timeStr = isEnable ? mBind.etStaticTriggerTime.getText().toString() : "0";
            int time = Integer.parseInt(timeStr);
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setPowerSavingStaticTriggerTime(time));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        } else {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
        }
    }

    public void onBack(View view) {
        finish();
    }

    private boolean isValid() {
        if (isEnable) {
            if (TextUtils.isEmpty(mBind.etStaticTriggerTime.getText())) return false;
            String timeStr = mBind.etStaticTriggerTime.getText().toString();
            int time = Integer.parseInt(timeStr);
            return time >= 1 && time <= 65535;
        }
        return true;
    }

    public void onPowerSavingMode(View view) {
        if (isWindowLocked()) return;
        isEnable = !isEnable;
        mBind.ivPowerSavingMode.setImageResource(isEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.clStaticTriggerTime.setVisibility(isEnable ? View.VISIBLE : View.GONE);
    }
}
