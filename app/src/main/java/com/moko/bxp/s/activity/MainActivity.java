package com.moko.bxp.s.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.BuildConfig;
import com.moko.bxp.s.R;
import com.moko.bxp.s.adapter.DeviceListAdapter;
import com.moko.bxp.s.databinding.ActivityMainBinding;
import com.moko.bxp.s.dialog.LoadingDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.dialog.PasswordDialog;
import com.moko.bxp.s.dialog.ScanFilterDialog;
import com.moko.bxp.s.entity.AdvInfo;
import com.moko.bxp.s.utils.AdvInfoAnalysisImpl;
import com.moko.bxp.s.utils.SPUtiles;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoBleScanner;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.callback.MokoScanDeviceCallback;
import com.moko.support.s.entity.DeviceInfo;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends BaseActivity implements MokoScanDeviceCallback, BaseQuickAdapter.OnItemChildClickListener {
    private ActivityMainBinding mBind;
    private boolean mReceiverTag = false;
    private final ConcurrentHashMap<String, AdvInfo> advInfoHashMap = new ConcurrentHashMap<>();
    private final ArrayList<AdvInfo> advInfoList = new ArrayList<>();
    private DeviceListAdapter adapter;
    private MokoBleScanner mokoBleScanner;
    private Handler mHandler;
    private boolean isPasswordError;
    private AdvInfoAnalysisImpl advInfoAnalysisImpl;
    public static String PATH_LOGCAT;
    private Animation animation = null;
    public String filterMac;
    public String filterName;
    public String filterTagId;
    public int filterRssi = -100;
    private int disconnectType;
    private String mPassword;
    private String mSavedPassword;
    private boolean enablePwd;
    private int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        // 初始化Xlog
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 优先保存到SD卡中
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PATH_LOGCAT = getExternalFilesDir(null).getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "mokoBeaconXPro" : "BXP_S");
            } else {
                PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "mokoBeaconXPro" : "BXP_S");
            }
        } else {
            // 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getFilesDir().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "mokoBeaconXPro" : "BXP_S");
        }
        MokoSupport.getInstance().init(getApplicationContext());
        flag = getIntent().getIntExtra("flag", 1);
        adapter = new DeviceListAdapter();
        adapter.setOnItemChildClickListener(this);
        adapter.openLoadAnimation();
        mBind.rvDevices.setAdapter(adapter);

        mHandler = new Handler(Looper.getMainLooper());
        mokoBleScanner = new MokoBleScanner(this);
        EventBus.getDefault().register(this);
        mSavedPassword = SPUtiles.getStringValue(this, AppConstants.SP_KEY_SAVED_PASSWORD, "");
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            if (animation == null) startScan();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if (animation != null) {
                                mHandler.removeMessages(0);
                                mokoBleScanner.stopScanDevice();
                                onStopScan();
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            if (animation == null) startScan();
                            break;

                    }
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            mPassword = "";
            // 设备断开，通知页面更新
            dismissLoadingProgressDialog();
            dismissLoadingMessageDialog();
            if (animation == null) {
                if (isPasswordError) {
                    isPasswordError = false;
                } else {
                    if (disconnectType == 1) {
                        disconnectType = 0;
                    } else {
                        ToastUtils.showToast(this, "Connection failed");
                    }
                }
                if (null == animation) startScan();
            }
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            // 设备连接成功，通知页面更新
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getVerifyPasswordEnable());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            MokoSupport.getInstance().disConnectBle();
        }
        if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
            dismissLoadingProgressDialog();
        }
        if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
            OrderTaskResponse response = event.getResponse();
            OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
            byte[] value = response.responseValue;
            if (orderCHAR == OrderCHAR.CHAR_PASSWORD) {
                if (value.length == 5) {
                    int header = value[0] & 0xFF;// 0xEB
                    int flag = value[1] & 0xFF;// read or write
                    int cmd = value[2] & 0xFF;
                    ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                    if (header != 0xEB || null == configKeyEnum) return;
                    int length = value[3] & 0xFF;
                    if (configKeyEnum == ParamsKeyEnum.KEY_PASSWORD) {
                        if (flag == 1 && length == 1) {
                            int result = value[4] & 0xFF;
                            dismissLoadingMessageDialog();
                            if (result == 0xAA) {
                                mSavedPassword = mPassword;
                                SPUtiles.setStringValue(this, AppConstants.SP_KEY_SAVED_PASSWORD, mSavedPassword);
                                XLog.i("Success");
                                Intent intent = new Intent(this, DeviceInfoActivity.class);
                                intent.putExtra("pwdEnable", enablePwd);
                                startActivity(intent);
                            } else {
                                isPasswordError = true;
                                ToastUtils.showToast(this, "Password incorrect！");
                                MokoSupport.getInstance().disConnectBle();
                            }
                        }
                    } else if (configKeyEnum == ParamsKeyEnum.KEY_VERIFY_PASSWORD_ENABLE) {
                        if (flag == 0 && length == 1) {
                            int enable = value[4] & 0xff;
                            if (enable == 1) {
                                // 开启验证
                                enablePwd = true;
                                //开启密码验证的监听
                                showPasswordDialog();
                            } else {
                                enablePwd = false;
                                Intent intent = new Intent(this, DeviceInfoActivity.class);
                                intent.putExtra("pwdEnable", enablePwd);
                                startActivity(intent);
                            }
                        }
                    }
                }
            }
        } else if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
            //监听密码连接超时
            OrderTaskResponse response = event.getResponse();
            OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
            byte[] value = response.responseValue;
            if (orderCHAR == OrderCHAR.CHAR_DISCONNECT) {
                if (null != value && value.length == 5) {
                    disconnectType = value[4] & 0xff;
                    if (disconnectType == 1) {
                        //密码验证超时
                        if (null != dialog && dialog.isAdded() && dialog.isVisible())
                            dialog.dismiss();
                        ToastUtils.showToast(this, "Password entry timed out！");
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefresh(String flag) {
        if ("refresh".equals(flag)) {
            mPassword = "";
            if (animation == null) startScan();
        }
    }

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

    @Override
    public void onStartScan() {
        advInfoHashMap.clear();
        new Thread(() -> {
            while (animation != null) {
                runOnUiThread(() -> {
                    adapter.replaceData(advInfoList);
                    mBind.tvDeviceNum.setText(String.format("DEVICE(%d)", advInfoList.size()));
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    XLog.e(e);
                }
                updateDevices();
            }
        }).start();
    }

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        AdvInfo advInfo = advInfoAnalysisImpl.parseDeviceInfo(deviceInfo);
        if (advInfo == null) return;
        advInfoHashMap.put(advInfo.mac, advInfo);
    }

    @Override
    public void onStopScan() {
        mBind.ivRefresh.clearAnimation();
        animation = null;
    }

    private void updateDevices() {
        advInfoList.clear();
        if (!TextUtils.isEmpty(filterName) || !TextUtils.isEmpty(filterMac) || filterRssi != -100 || !TextUtils.isEmpty(filterTagId)) {
            ArrayList<AdvInfo> advInfoListFilter = new ArrayList<>(advInfoHashMap.values());
            Iterator<AdvInfo> iterator = advInfoListFilter.iterator();
            while (iterator.hasNext()) {
                AdvInfo advInfo = iterator.next();
                if (advInfo.rssi > filterRssi) {
                    if (TextUtils.isEmpty(filterName) && TextUtils.isEmpty(filterMac) && TextUtils.isEmpty(filterTagId)) {
                        continue;
                    } else {
                        if (!TextUtils.isEmpty(filterName) && TextUtils.isEmpty(advInfo.name)) {
                            iterator.remove();
                        } else if (!TextUtils.isEmpty(filterName) && advInfo.name.toLowerCase().contains(filterName.toLowerCase())) {
                            continue;
                        } else if (!TextUtils.isEmpty(filterMac) && TextUtils.isEmpty(advInfo.mac)) {
                            iterator.remove();
                        } else if (!TextUtils.isEmpty(filterMac) && advInfo.mac.toLowerCase().replaceAll(":", "").contains(filterMac.toLowerCase())) {
                            continue;
                        } else if (!TextUtils.isEmpty(filterTagId) && TextUtils.isEmpty(advInfo.tagId)) {
                            iterator.remove();
                        } else if (!TextUtils.isEmpty(filterTagId) && advInfo.tagId.contains(filterTagId)) {
                            continue;
                        }
//                        else if (!TextUtils.isEmpty(filterTagId) && null != validData && !TextUtils.isEmpty(validData.data.substring(36))&& !validData.data.substring(36).contains(filterTagId)){
//                            iterator.remove();
//                        }
                        else {
                            iterator.remove();
                        }
                    }
                } else {
                    iterator.remove();
                }
            }
            advInfoList.addAll(advInfoListFilter);
        } else {
            advInfoList.addAll(advInfoHashMap.values());
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(advInfoList, (lhs, rhs) -> {
            if (lhs.rssi > rhs.rssi) {
                return -1;
            } else if (lhs.rssi < rhs.rssi) {
                return 1;
            }
            return 0;
        });
    }

    private void startScan() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        mBind.ivRefresh.startAnimation(animation);
        advInfoAnalysisImpl = new AdvInfoAnalysisImpl(flag);
        mokoBleScanner.startScanDevice(this);
        mHandler.postDelayed(() -> mokoBleScanner.stopScanDevice(), 1000 * 60);
    }

    private LoadingDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager());
    }

    private void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismissAllowingStateLoss();
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    private void showLoadingMessageDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Verifying..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    private void dismissLoadingMessageDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        //防止重复点击
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        final AdvInfo advInfo = (AdvInfo) adapter.getItem(position);
        if (advInfo != null && !isFinishing()) {
            if (animation != null) {
                mHandler.removeMessages(0);
                mokoBleScanner.stopScanDevice();
            }
            showLoadingProgressDialog();
            MokoSupport.getInstance().connDevice(advInfo.mac);
        }
    }

    private PasswordDialog dialog;

    private void showPasswordDialog() {
        // show password
        dialog = new PasswordDialog();
        dialog.setPassword(mSavedPassword);
        dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
            @Override
            public void onEnsureClicked(String password) {
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    MokoSupport.getInstance().enableBluetooth();
                    return;
                }
                XLog.i(password);
                mPassword = password;
                showLoadingMessageDialog();
                mBind.ivRefresh.postDelayed(() -> MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setPassword(password)), 200);
            }

            @Override
            public void onDismiss() {
                MokoSupport.getInstance().disConnectBle();
                startScan();
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onBack(View view) {
        if (isWindowLocked()) return;
        back();
    }

    public void onAbout(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void onFilter(View view) {
        if (isWindowLocked()) return;
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        ScanFilterDialog scanFilterDialog = new ScanFilterDialog(this);
        scanFilterDialog.setFilterMac(filterMac);
        scanFilterDialog.setFilterRssi(filterRssi);
        scanFilterDialog.setFilterName(filterName);
        scanFilterDialog.setFilterTagId(filterTagId);
        scanFilterDialog.setOnScanFilterListener((filterName, filterMac, filterRssi, filterTagId) -> {
            MainActivity.this.filterMac = filterMac;
            String showFilterMac;
            if (filterMac.length() == 12) {
                StringBuilder stringBuffer = new StringBuilder(filterMac);
                stringBuffer.insert(2, ":");
                stringBuffer.insert(5, ":");
                stringBuffer.insert(8, ":");
                stringBuffer.insert(11, ":");
                stringBuffer.insert(14, ":");
                showFilterMac = stringBuffer.toString();
            } else {
                showFilterMac = filterMac;
            }
            MainActivity.this.filterRssi = filterRssi;
            MainActivity.this.filterName = filterName;
            MainActivity.this.filterTagId = filterTagId;
            if (!TextUtils.isEmpty(showFilterMac) || filterRssi != -100 || !TextUtils.isEmpty(filterName) || !TextUtils.isEmpty(filterTagId)) {
                mBind.rlFilter.setVisibility(View.VISIBLE);
                mBind.rlEditFilter.setVisibility(View.GONE);
                StringBuilder stringBuilder = new StringBuilder();
                if (!TextUtils.isEmpty(filterName)) {
                    stringBuilder.append(filterName).append(";");
                }
                if (!TextUtils.isEmpty(showFilterMac)) {
                    stringBuilder.append(showFilterMac).append(";");
                }
                if (!TextUtils.isEmpty(filterTagId)) {
                    stringBuilder.append(filterTagId).append(";");
                }
                if (filterRssi != -100) {
                    stringBuilder.append(String.format("%sdBm", filterRssi + "")).append(";");
                }
                mBind.tvFilter.setText(stringBuilder.toString());
            } else {
                mBind.rlFilter.setVisibility(View.GONE);
                mBind.rlEditFilter.setVisibility(View.VISIBLE);
            }
            if (isWindowLocked()) return;
            if (animation == null) startScan();
        });
        scanFilterDialog.setOnDismissListener(dialog -> {
            if (isWindowLocked()) return;
            if (animation == null) startScan();
        });
        scanFilterDialog.show();
    }

    private void back() {
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
//        if (BuildConfig.IS_LIBRARY) {
        finish();
//        } else {
//            AlertMessageDialog dialog = new AlertMessageDialog();
//            dialog.setMessage(R.string.main_exit_tips);
//            dialog.setOnAlertConfirmListener(this::finish);
//            dialog.show(getSupportFragmentManager());
//        }
    }

    public void onRefresh(View view) {
        if (isWindowLocked())
            return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        if (animation == null) {
            startScan();
        } else {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
    }

    public void onFilterDelete(View view) {
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        mBind.rlFilter.setVisibility(View.GONE);
        mBind.rlEditFilter.setVisibility(View.VISIBLE);
        filterMac = "";
        filterName = "";
        filterTagId = "";
        filterRssi = -100;
        if (isWindowLocked()) return;
        if (animation == null) startScan();
    }
}
