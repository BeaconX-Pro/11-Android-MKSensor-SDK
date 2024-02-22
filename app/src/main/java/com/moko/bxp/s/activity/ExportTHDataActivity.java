package com.moko.bxp.s.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.elvishew.xlog.XLog;
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
import java.util.Calendar;
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
    private boolean isSync;
    private StringBuilder thStoreString = new StringBuilder();
    private final List<THStoreData> thStoreData = new LinkedList<>();
    private final List<THStoreData> filterThStoreData = new LinkedList<>();
    private THDataListAdapter mAdapter;
    private ActivityExportThDataBinding mBind;
    private int deviceType;
    private final SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS, Locale.getDefault());
    private Date startDate;
    private Date endDate;
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

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
                if (null != startDate && null != endDate && !TextUtils.isEmpty(mBind.tvStartDate.getText()) && !TextUtils.isEmpty(mBind.tvEndDate.getText())) {
                    List<Float> tempList = new LinkedList<>();
                    List<Float> humList = new LinkedList<>();
                    for (THStoreData bean : filterThStoreData) {
                        if (bean.timeStamp >= startDate.getTime() && bean.timeStamp <= endDate.getTime()) {
                            tempList.add(0, Float.valueOf(bean.temp));
                            if (deviceType != 3) humList.add(0, Float.valueOf(bean.humidity));
                        }
                    }
                    mBind.tempChartView.setxValue(tempList);
                    if (deviceType != 3) mBind.humiChartView.setxValue(humList);
                    int length = tempList.size();
                    mBind.thChartTotal.setText(getString(R.string.th_chart_total, length));
                    mBind.thChartDisplay.setText(getString(R.string.th_chart_display, Math.min(length, 1000)));
                } else {
                    List<Float> tempList = new LinkedList<>();
                    List<Float> humList = new LinkedList<>();
                    for (THStoreData bean : thStoreData) {
                        tempList.add(0, Float.valueOf(bean.temp));
                        if (deviceType != 3) humList.add(0, Float.valueOf(bean.humidity));
                    }
                    mBind.tempChartView.setxValue(tempList);
                    if (deviceType != 3) mBind.humiChartView.setxValue(humList);
                    int length = tempList.size();
                    mBind.thChartTotal.setText(getString(R.string.th_chart_total, length));
                    mBind.thChartDisplay.setText(getString(R.string.th_chart_display, Math.min(length, 1000)));
                }
            } else {
                // 隐藏折线图
                mBind.llThData.setVisibility(View.VISIBLE);
                mBind.llThChartView.setVisibility(View.GONE);
            }
        });
        mAdapter = new THDataListAdapter(deviceType);
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
        mBind.tvStartDate.setOnClickListener(v -> onSelectTimeClick(1));
        mBind.tvEndDate.setOnClickListener(v -> onSelectTimeClick(2));
        mBind.tvCancel.setOnClickListener(v -> {
            mBind.tvStartDate.setText("");
            mBind.tvEndDate.setText("");
            startDate = null;
            endDate = null;
            mAdapter.replaceData(thStoreData);
            mBind.tvSumRecord.setText("Sum records:" + thStoreData.size());
            mBind.tvFilterRecord.setText("Filtered records:" + thStoreData.size());
            if (thStoreData.size() > 0) {
                Drawable top = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download_enable, null);
                mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            } else {
                Drawable top = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download, null);
                mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            }
        });
        mBind.tvStart.setOnClickListener(v -> onStartClick());
    }

    private void onStartClick() {
        if (null == startDate || null == endDate) return;
        filterThStoreData.clear();
        for (THStoreData bean : thStoreData) {
            long timeStamp = bean.timeStamp;
            if (timeStamp >= startDate.getTime() && timeStamp <= endDate.getTime()) {
                filterThStoreData.add(bean);
            }
        }
        mAdapter.replaceData(filterThStoreData);
        mBind.tvSumRecord.setText("Sum records:" + thStoreData.size());
        mBind.tvFilterRecord.setText("Filtered records:" + filterThStoreData.size());
        if (filterThStoreData.size() > 0) {
            Drawable top = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download_enable, null);
            mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
        } else {
            Drawable top = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download, null);
            mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
        }
    }

    private void onSelectTimeClick(int flag) {
        if (flag == 2) {
            mBind.tvEndDate.setText("");
            this.endDate = null;
        }
        Calendar startDate = Calendar.getInstance();
        startDate.set(2024, 0, 1);
        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DATE), Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND));
        TimePickerView pickerView = new TimePickerBuilder(this, (date, view) -> {
            if (flag == 1) {
                this.startDate = date;
                mBind.tvStartDate.setText(sdfDate.format(date));
            } else if (flag == 2) {
                if (date.getTime() < this.startDate.getTime()) {
                    ToastUtils.showToast(this, "The end time must be greater than the start time");
                    return;
                }
                this.endDate = date;
                mBind.tvEndDate.setText(sdfDate.format(date));
            }
        }).setType(new boolean[]{true, true, true, true, true, true})
                .setCancelText("Cancel")
                .setSubmitText("Confirm")
                .setContentTextSize(16)
                .setTitleSize(18)
                .setOutSideCancelable(false)
                .isCyclic(false)
                .setSubmitColor(ContextCompat.getColor(this, R.color.blue_2f84d0))
                .setCancelColor(ContextCompat.getColor(this, R.color.blue_2f84d0))
                .setRangDate(startDate, currentDate)
                .setDate(flag == 1 ? startDate : currentDate)
                .setLabel("", "", "", "", "", "")
                .isCenterLabel(true)
                .isDialog(true)
                .build();
        Dialog mDialog = pickerView.getDialog();
        if (mDialog != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);
            params.leftMargin = 0;
            params.rightMargin = 0;
            pickerView.getDialogContainerLayout().setLayoutParams(params);
            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f);
            }
        }
        pickerView.show();
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
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
                        int flag = value[1] & 0xff;
                        int key = value[2] & 0xff;
                        if (header == 0xEC && key == 0x80 && flag == 2) {
                            //历史数据通知
                            int totalPackage = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
                            int packageIndex = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                            if (totalPackage == 0 || totalPackage - 1 == packageIndex) {
                                //最后一帧数据了
                                isSync = false;
                                mBind.cbDataShow.setEnabled(true);
                                mBind.ivSync.clearAnimation();
                                mBind.tvSync.setText("Sync");
                                dismissSyncProgressDialog();
                                MokoSupport.getInstance().disableHistoryThNotify();
                            }
                            int length = value[7] & 0xff;
                            if (length > 0) {
                                byte[] data = Arrays.copyOfRange(value, 8, length + 8);
                                mBind.llThData.setVisibility(View.VISIBLE);
                                for (int i = 0; i < data.length; i += 8) {
                                    THStoreData storeData = new THStoreData();
                                    //i=0  0-8 8-16
                                    byte[] bytes = Arrays.copyOfRange(data, i, i + 8);
                                    int year = MokoUtils.toInt(Arrays.copyOfRange(bytes, 0, 4));
                                    storeData.time = sdf.format(new Date(year * 1000L));
                                    storeData.timeStamp = year * 1000L;
                                    float temp = MokoUtils.byte2short(Arrays.copyOfRange(bytes, 4, 6)) * 0.1f;
                                    storeData.temp = MokoUtils.getDecimalFormat("0.0").format(temp);
                                    float hum = MokoUtils.byte2short(Arrays.copyOfRange(bytes, 6, 8)) * 0.1f;
                                    storeData.humidity = MokoUtils.getDecimalFormat("0.0").format(hum);
                                    thStoreData.add(0, storeData);
                                }
                                if (null != startDate && null != endDate && !TextUtils.isEmpty(mBind.tvStartDate.getText()) && !TextUtils.isEmpty(mBind.tvEndDate.getText())) {
                                    onStartClick();
                                } else {
                                    mAdapter.replaceData(thStoreData);
                                    mBind.tvSumRecord.setText("Sum records:" + thStoreData.size());
                                    mBind.tvFilterRecord.setText("Filtered records:" + thStoreData.size());
                                    if (thStoreData.size() > 0) {
                                        Drawable top = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download_enable, null);
                                        mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                                    }
                                }
                                if (thStoreData.size() > 0) {
                                    mBind.tvStart.setEnabled(true);
                                    mBind.tvStart.setBackgroundResource(R.drawable.shape_radius_blue_btn_bg);
                                }
                            } else {
                                mBind.tvSumRecord.setText("Sum records:0");
                                mBind.tvFilterRecord.setText("Filtered records:0");
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
                                thStoreData.clear();
                                mAdapter.replaceData(thStoreData);
                                mBind.llThData.setVisibility(View.GONE);
                                Drawable top = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download, null);
                                mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                                mBind.tvStart.setEnabled(false);
                                mBind.tvStart.setBackgroundResource(R.drawable.shape_radius_grey);
                                mBind.tvStartDate.setText("");
                                mBind.tvEndDate.setText("");
                                startDate = null;
                                endDate = null;
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
                boolean a = file.createNewFile();
                XLog.e(a);
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
                boolean a = file.createNewFile();
                XLog.i(a);
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
            showSyncingProgressDialog();
            MokoSupport.getInstance().enableHistoryThNotify();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            mBind.ivSync.startAnimation(animation);
            mBind.tvSync.setText("Stop");
            mBind.cbDataShow.setChecked(false);
            thStoreData.clear();
            filterThStoreData.clear();
            mAdapter.replaceData(thStoreData);
            Drawable top = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download, null);
            mBind.tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            mBind.tvStart.setEnabled(false);
            mBind.tvStart.setBackgroundResource(R.drawable.shape_radius_grey);
        }
    }

    public void onExport(View view) {
        if (isWindowLocked()) return;
        if (null != startDate && null != endDate && !TextUtils.isEmpty(mBind.tvStartDate.getText()) && !TextUtils.isEmpty(mBind.tvEndDate.getText())) {
            for (THStoreData storeData : filterThStoreData) {
                if (deviceType == 3) {
                    thStoreString.append(String.format("%s T%s", storeData.time, storeData.temp)).append("\n");
                } else {
                    thStoreString.append(String.format("%s T%s H%s", storeData.time, storeData.temp, storeData.humidity)).append("\n");
                }
            }
        } else {
            for (THStoreData storeData : thStoreData) {
                if (deviceType == 3) {
                    thStoreString.append(String.format("%s T%s", storeData.time, storeData.temp)).append("\n");
                } else {
                    thStoreString.append(String.format("%s T%s H%s", storeData.time, storeData.temp, storeData.humidity)).append("\n");
                }
            }
        }
        if (TextUtils.isEmpty(thStoreString)) return;
        showSyncingProgressDialog();
        writeTHFile("");
        mBind.tvExport.postDelayed(() -> {
            dismissSyncProgressDialog();
            String log = thStoreString.toString();
            writeTHFile(log);
            File file = getTHFile();
            // 发送邮件
            String address = "Development@mokotechnology.com";
            String title = "T&H Log";
            Utils.sendEmail(this, address, title, title, "Choose Email Client", file);
        }, 500);
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
