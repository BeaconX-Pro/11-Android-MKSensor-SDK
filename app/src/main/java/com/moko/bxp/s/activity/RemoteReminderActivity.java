package com.moko.bxp.s.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.databinding.ActivityRemoteReminderBinding;
import com.moko.bxp.s.dialog.BottomDialog;
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

/**
 * @author: jun.liu
 * @date: 2024/10/28 15:25
 * @des: 远程提醒
 */
public class RemoteReminderActivity extends BaseActivity {
    private ActivityRemoteReminderBinding mBind;
    private final String[] array = {"4000", "4500"};
    private int mSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityRemoteReminderBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mBind.btnRemind.setOnClickListener(v -> onLedRemindClick());
        mBind.btnBuzzerRemind.setOnClickListener(v -> onBuzzerRemindClick());
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getBuzzerFrequency());
        mBind.btnFrequency.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(array)), mSelect);
            dialog.setListener(value -> {
                showSyncingProgressDialog();
                mSelect = value;
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setBuzzerFrequency(value == 0 ? 4000 : 4500));
            });
            dialog.show(getSupportFragmentManager());
        });
    }

    //远程提醒
    private void onLedRemindClick() {
        if (TextUtils.isEmpty(mBind.etInterval.getText()) || TextUtils.isEmpty(mBind.etTime.getText())) {
            ToastUtils.showToast(this, "Opps！Please check the input characters and try again.");
            return;
        }
        int interval = Integer.parseInt(mBind.etInterval.getText().toString().trim());
        int time = Integer.parseInt(mBind.etTime.getText().toString().trim());
        if (interval < 1 || interval > 100) {
            ToastUtils.showToast(this, "Blinking interval should in 1~100");
            return;
        }
        if (time < 1 || time > 600) {
            //单位100ms
            ToastUtils.showToast(this, "Blinking time should in 1~600");
            return;
        }
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setLedRemoteReminder(interval * 100, time * 10));
    }

    private void onBuzzerRemindClick() {
        if (TextUtils.isEmpty(mBind.etBuzzerInterval.getText()) || TextUtils.isEmpty(mBind.etBuzzerTime.getText())) {
            ToastUtils.showToast(this, "Opps！Please check the input characters and try again.");
            return;
        }
        int interval = Integer.parseInt(mBind.etBuzzerInterval.getText().toString().trim());
        int time = Integer.parseInt(mBind.etBuzzerTime.getText().toString().trim());
        if (interval < 1 || interval > 100) {
            ToastUtils.showToast(this, "Blinking interval should in 1~100");
            return;
        }
        if (time < 1 || time > 600) {
            ToastUtils.showToast(this, "Blinking time should in 1~600");
            return;
        }
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setBuzzerRemoteReminder(interval * 100, time * 10));
    }

    public void onBack(View view) {
        finish();
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
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length > 4) {
                        int header = value[0] & 0xFF;// 0xEB
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xEB || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01 && length == 0x01) {
                            switch (configKeyEnum) {
                                case KEY_LED_REMOTE_REMINDER:
                                case KEY_BUZZER_REMOTE_REMINDER:
                                    // write
                                    int result = value[4] & 0xFF;
                                    if (result == 0xAA) {
                                        ToastUtils.showToast(this, "success");
                                    } else {
                                        ToastUtils.showToast(this, "fail");
                                    }
                                    break;
                                case KEY_BUZZER_FREQUENCY:
                                    ToastUtils.showToast(this, (value[4] & 0xff) == 0xAA ? "success" : "fail");
                                    if ((value[4] & 0xff) == 0xAA) {
                                        mBind.btnFrequency.setText(array[mSelect]);
                                    }
                                    break;
                            }
                        } else if (flag == 0) {
                            if (length > 0) {
                                int frequency = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                if (frequency == 4000) mSelect = 0;
                                else mSelect = 1;
                                mBind.btnFrequency.setText(array[mSelect]);
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
}
