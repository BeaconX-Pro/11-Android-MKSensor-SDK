package com.moko.bxp.s.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityThBinding;
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

public class THDataActivity extends BaseActivity {
    private boolean mReceiverTag = false;
    private boolean isTHStoreEnable;
    private final SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS, Locale.getDefault());
    private ActivityThBinding mBind;
    private boolean isParamsError;
    private int deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityThBinding.inflate(getLayoutInflater());
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
            MokoSupport.getInstance().enableTHNotify();
            showSyncingProgressDialog();
            mBind.tvTemp.postDelayed(() -> {
                ArrayList<OrderTask> orderTasks = new ArrayList<>();
                orderTasks.add(OrderTaskAssembler.getTHSampleInterval());
                orderTasks.add(OrderTaskAssembler.getTHStore());
                orderTasks.add(OrderTaskAssembler.getCurrentTime());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            }, 1000);
        }
        mBind.imgStore.setOnClickListener(v -> {
            isTHStoreEnable = !isTHStoreEnable;
            mBind.imgStore.setImageResource(isTHStoreEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        });
        deviceType = getIntent().getIntExtra("type", 0);
        if (deviceType == 3) {
            //只有温度
            mBind.group.setVisibility(View.GONE);
            mBind.tvTitle.setText("Temperature");
            mBind.tvDataStoreTitle.setText("Temperature Data Store");
            mBind.tvExportDataTitle.setText("Export Temperature data");
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
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xff;
                        int flag = value[1] & 0xff;
                        int key = value[2] & 0xff;
                        int length = value[3] & 0xff;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                        if (configKeyEnum == null || header != 0xEB) return;
                        if (flag == 0) {
                            switch (configKeyEnum) {
                                case KEY_TH_SAMPLE_RATE:
                                    if (length == 2) {
                                        byte[] period = Arrays.copyOfRange(value, 4, 6);
                                        String periodStr = String.valueOf(MokoUtils.toInt(period));
                                        mBind.etPeriod.setText(periodStr);
                                        mBind.etPeriod.setSelection(periodStr.length());
                                    }
                                    break;

                                case KEY_TH_STORE:
                                    if (length == 3) {
                                        isTHStoreEnable = (value[4] & 0xff) == 1;
                                        mBind.imgStore.setImageResource(isTHStoreEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                        mBind.etStorageInterval.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7))));
                                        mBind.etStorageInterval.setSelection(mBind.etStorageInterval.getText().length());
                                    }
                                    break;

                                case KEY_SYNC_CURRENT_TIME:
                                    if (length == 4) {
                                        int time = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 4 + length));
                                        mBind.tvUpdateDate.setText(sdf.format(time * 1000L));
                                    }
                                    break;
                            }
                        } else if (flag == 1) {
                            switch (configKeyEnum) {
                                case KEY_TH_SAMPLE_RATE:
                                    if ((value[4] & 0xff) != 0xAA) isParamsError = true;
                                    break;

                                case KEY_TH_STORE:
                                    if ((value[4] & 0xff) != 0xAA) isParamsError = true;
                                    ToastUtils.showToast(this, isParamsError ? "Opps！Save failed" : "Success");
                                    break;
                            }
                        }
                    }
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                byte[] value = response.responseValue;
                int header = value[0] & 0xff;
                int key = value[2] & 0xff;
                int length = value[3] & 0xff;
                if (header == 0xEB && key == 0x70 && length == 4) {
                    byte[] tempBytes = Arrays.copyOfRange(value, 4, 6);
                    float temp = MokoUtils.byte2short(tempBytes) * 0.1f;
                    mBind.tvTemp.setText(MokoUtils.getDecimalFormat("0.0").format(temp));
                    byte[] humidityBytes = Arrays.copyOfRange(value, 6, 8);
                    float humidity = MokoUtils.toInt(humidityBytes) * 0.1f;
                    mBind.tvHumidity.setText(MokoUtils.getDecimalFormat("0.0").format(humidity));
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
        MokoSupport.getInstance().disableTHNotify();
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onExportData(View view) {
        if (isWindowLocked()) return;
        // 跳转导出数据页面
        Intent intent = new Intent(this, ExportTHDataActivity.class);
        intent.putExtra("type",deviceType);
        startActivity(intent);
    }

    public void onUpdate(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.setCurrentTime());
        orderTasks.add(OrderTaskAssembler.getCurrentTime());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    public void onBack(View view) {
        back();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        // 保存
        if (TextUtils.isEmpty(mBind.etPeriod.getText())) {
            ToastUtils.showToast(this, "Sampling interval can not be empty");
            return;
        }
        String periodStr = mBind.etPeriod.getText().toString();
        int period = Integer.parseInt(periodStr);
        if (period < 1 || period > 65535) {
            ToastUtils.showToast(this, "Sampling interval range is 1~65535");
            return;
        }
        if (TextUtils.isEmpty(mBind.etStorageInterval.getText())) {
            ToastUtils.showToast(this, "Storage interval can not be empty");
            return;
        }
        String intervalStr = mBind.etPeriod.getText().toString();
        int interval = Integer.parseInt(intervalStr);
        if (interval < 1 || interval > 65535) {
            ToastUtils.showToast(this, "Storage interval range is 1~65535");
            return;
        }
        showSyncingProgressDialog();
        isParamsError = false;
        ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.setTHSampleInterval(period));
        orderTasks.add(OrderTaskAssembler.setTHStore(isTHStoreEnable ? 1 : 0, interval));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
