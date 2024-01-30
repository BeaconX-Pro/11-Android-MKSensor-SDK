package com.moko.bxp.s.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.BaseApplication;
import com.moko.bxp.s.R;
import com.moko.bxp.s.adapter.THDataListAdapter;
import com.moko.bxp.s.databinding.ActivityExportThDataBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.entity.THStoreData;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.bxp.s.utils.Utils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author: jun.liu
 * @date: 2024/1/29 18:13
 * @des:
 */
public class ExportTHDataActivity extends BaseActivity {
    private static final String TRACKED_FILE = "T&HDatas.txt";
    private static String PATH_LOGCAT;
    private boolean mReceiverTag = false;
    private boolean mIsShown;
    private boolean isSync;
    private List<Float> mHumiList = new LinkedList<>();
    private List<Float> mTempList = new LinkedList<>();
    private StringBuilder thStoreString;
    private List<THStoreData> thStoreData;
    private THDataListAdapter mAdapter;
    private ActivityExportThDataBinding mBind;
    private int deviceType;
    private final SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS, Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityExportThDataBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        PATH_LOGCAT = BaseApplication.PATH_LOGCAT + File.separator + TRACKED_FILE;
        deviceType = getIntent().getIntExtra("type", 0);
        if (deviceType == 3) {
            mBind.tvTitle.setText("Export Temperature data");
            mBind.tvHumidity.setVisibility(View.GONE);
            mBind.humiChartView.setVisibility(View.GONE);
        }
        mBind.cbDataShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 绘制折线图并展示
                mBind.llThData.setVisibility(View.GONE);
                mBind.llThChartView.setVisibility(View.VISIBLE);
                mBind.tempChartView.setxValue(mTempList);
                if (deviceType != 3) mBind.humiChartView.setxValue(mHumiList);
                int length = mTempList.size();
                mBind.thChartTotal.setText(getString(R.string.th_chart_total, length));
                mBind.thChartDisplay.setText(getString(R.string.th_chart_display, Math.min(length, 1000)));
            } else {
                // 隐藏折线图
                mBind.llThData.setVisibility(View.VISIBLE);
                mBind.llThChartView.setVisibility(View.GONE);
            }
        });
        mAdapter = new THDataListAdapter(deviceType);
        thStoreData = new LinkedList<>();
        thStoreString = new StringBuilder();
        mAdapter.replaceData(thStoreData);
        mBind.rvThData.setAdapter(mAdapter);

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        }
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
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                dismissSyncProgressDialog();
                ToastUtils.showToast(this, "time out");
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_TH_HISTORY) {
                    if (null != value && value.length >= 8) {
                        int header = value[0] & 0xff;
                        int key = value[2] & 0xff;
                        if (header == 0xEC && key == 0x80) {
                            //历史数据通知
                            int totalPackage = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
                            int currentPackage = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                            int length = value[7] & 0xff;
                            byte[] data = Arrays.copyOfRange(value, 8, length + 8);
                            mBind.llThData.setVisibility(View.VISIBLE);
                            for (int i = 0; i < data.length; i += 8) {
                                THStoreData storeData = new THStoreData();
                                //i=0  0-8 8-16
                                byte[] bytes = Arrays.copyOfRange(data, i, i + 8);
                                int year = MokoUtils.toInt(Arrays.copyOfRange(bytes, 0, 4));
                                storeData.time = sdf.format(new Date(year * 1000L));
                                float temp = MokoUtils.byte2short(Arrays.copyOfRange(bytes, 4, 6)) * 0.1f;
                                storeData.temp = MokoUtils.getDecimalFormat("0.0").format(temp);
                                float hum = MokoUtils.byte2short(Arrays.copyOfRange(bytes, 6, 8)) * 0.1f;
                                storeData.humidity = MokoUtils.getDecimalFormat("0.0").format(hum);
                                thStoreData.add(0, storeData);
                                mTempList.add(0, temp);
                                if (deviceType != 3) mHumiList.add(0, hum);
                                if (deviceType == 3) {
                                    thStoreString.append(String.format("%s T%s", storeData.time, storeData.temp));
                                } else {
                                    thStoreString.append(String.format("%s T%s H%s", storeData.time, storeData.temp, storeData.humidity));
                                }
                                thStoreString.append("\n");
                            }
                            mAdapter.replaceData(thStoreData);
                            if (currentPackage >= totalPackage - 1) {
                                MokoSupport.getInstance().disableHistoryThNotify();
                                isSync = false;
                                mBind.ivSync.clearAnimation();
                                mBind.tvSync.setText("Sync");
                                mBind.cbDataShow.setEnabled(true);
                                if (!mIsShown && thStoreData.size() > 0) {
                                    mIsShown = true;
                                    Drawable top = getResources().getDrawable(R.drawable.ic_download_enable);
                                    mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                                }
                            }
                        }
                    }
                }
            } else if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int key = value[2] & 0xff;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                        if (configKeyEnum == ParamsKeyEnum.KEY_CLEAR_HISTORY_TH) {
                            if ((value[4] & 0xff) == 0xAA) {
                                thStoreString = new StringBuilder();
                                writeTHFile("");
                                mIsShown = false;
                                thStoreData.clear();
                                mAdapter.replaceData(thStoreData);
                                mBind.llThData.setVisibility(View.GONE);
                                Drawable top = getResources().getDrawable(R.drawable.ic_download);
                                mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                                ToastUtils.showToast(this, "Erase success!");
                            } else {
                                ToastUtils.showToast(this, "Failed");
                            }
                        }
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
        mTempList.clear();
        mTempList = null;
        mHumiList.clear();
        mHumiList = null;
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
        MokoSupport.getInstance().disableHistoryThNotify();
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public static void writeTHFile(String thLog) {
        File file = new File(PATH_LOGCAT);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(thLog);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getTHFile() {
        File file = new File(PATH_LOGCAT);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void onBack(View view) {
        back();
    }

    public void onSync(View view) {
        if (isWindowLocked()) return;
        if (!isSync) {
            isSync = true;
            MokoSupport.getInstance().enableHistoryThNotify();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            mBind.ivSync.startAnimation(animation);
            mBind.tvSync.setText("Stop");
            mBind.cbDataShow.setChecked(false);
            mBind.cbDataShow.setEnabled(false);
            thStoreData.clear();
            mTempList.clear();
            mHumiList.clear();
            mAdapter.replaceData(thStoreData);
            Drawable top = getResources().getDrawable(R.drawable.ic_download);
            mIsShown = false;
            mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
        }
    }

    public void onExport(View view) {
        if (isWindowLocked()) return;
        if (mIsShown) {
            showSyncingProgressDialog();
            writeTHFile("");
            mBind.tvExport.postDelayed(() -> {
                dismissSyncProgressDialog();
                String log = thStoreString.toString();
                if (!TextUtils.isEmpty(log)) {
                    writeTHFile(log);
                    File file = getTHFile();
                    // 发送邮件
                    String address = "Development@mokotechnology.com";
                    String title = "T&H Log";
                    Utils.sendEmail(ExportTHDataActivity.this, address, title, title, "Choose Email Client", file);
                }
            }, 500);
        }
    }

    public void onEmpty(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning!");
        dialog.setMessage("Are you sure to erase all the saved T&H data?");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.clearHistoryTHData());
        });
        dialog.show(getSupportFragmentManager());
    }
}
