package com.moko.bxp.s.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityDeviceInfoBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.fragment.DeviceFragment;
import com.moko.bxp.s.fragment.SettingFragment;
import com.moko.bxp.s.fragment.SlotFragment;
import com.moko.bxp.s.service.DfuService;
import com.moko.bxp.s.utils.FileUtils;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;
    private ActivityDeviceInfoBinding mBind;
    private FragmentManager fragmentManager;
    private SlotFragment slotFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mDeviceMac;
    private boolean mIsClose;
    private boolean mReceiverTag = false;
    private int mDisconnectType;
    public boolean isAdvParamsSuc;
    private boolean isModifyPassword;
    private byte[] slotBytes;
    public boolean isSupportAcc;
    public boolean isSupportTH;
    private boolean isHallPowerEnable;
    private byte[] deviceTypeBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceInfoBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        fragmentManager = getSupportFragmentManager();
        initFragment();
        mBind.rgOptions.setOnCheckedChangeListener(this);
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
        boolean enablePwd = getIntent().getBooleanExtra("pwdEnable", false);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getAllSlotAdvType());
        orderTasks.add(OrderTaskAssembler.getSlotTriggerType(0));
        orderTasks.add(OrderTaskAssembler.getSlotTriggerType(1));
        orderTasks.add(OrderTaskAssembler.getSlotTriggerType(2));
        orderTasks.add(OrderTaskAssembler.getSensorType());
        orderTasks.add(OrderTaskAssembler.getHallPowerEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        //连接时候不需要密码
        settingFragment.setModifyPasswordShown(enablePwd);
        settingFragment.setResetVisibility(enablePwd);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                if (mIsClose) return;
                if (mDisconnectType > 0) return;
                if (MokoSupport.getInstance().isBluetoothOpen()) {
                    if (isUpgrading) {
                        mBind.tvTitle.postDelayed(this::dismissDFUProgressDialog, 2000);
                    } else {
                        AlertMessageDialog dialog = new AlertMessageDialog();
                        dialog.setTitle("Dismiss");
                        dialog.setMessage("The device disconnected!");
                        dialog.setConfirm("Exit");
                        dialog.setCancelGone();
                        dialog.setOnAlertConfirmListener(() -> {
                            EventBus.getDefault().post("refresh");
                            finish();
                        });
                        dialog.show(getSupportFragmentManager());
                    }
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_DISCONNECT) {
                    if (value.length == 5) {
                        mDisconnectType = value[4] & 0xff;
                        if (mDisconnectType == 2 && isModifyPassword) {
                            isModifyPassword = false;
                            dismissSyncProgressDialog();
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setMessage("Modify password success!\nPlease reconnect the Device.");
                            dialog.setCancelGone();
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                EventBus.getDefault().post("refresh");
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                        } else if (mDisconnectType == 3) {
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setMessage("Reset success!\nBeacon is disconnected.");
                            dialog.setCancelGone();
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                EventBus.getDefault().post("refresh");
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                        } else if (mDisconnectType == 4) {
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setTitle("Dismiss");
                            dialog.setMessage("The device disconnected!");
                            dialog.setConfirm("Exit");
                            dialog.setCancelGone();
                            dialog.setOnAlertConfirmListener(() -> {
                                EventBus.getDefault().post("refresh");
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                        }
                    }
                }
            }
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PASSWORD:
                        if (value.length == 5) {
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
                                if (configKeyEnum == ParamsKeyEnum.KEY_MODIFY_PASSWORD) {
                                    if (result != 0xAA) {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    }
                                }
                            }
                        }
                        break;
                    case CHAR_PARAMS:
                        if (null != value && value.length >= 4) {
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
                                    case KEY_NORMAL_ADV_PARAMS:
                                        isAdvParamsSuc = result == 0xAA;
                                        break;
                                    case KEY_BUTTON_TRIGGER_PARAMS:
                                        if (isAdvParamsSuc && result == 0xAA) {
                                            ToastUtils.showToast(this, "Success");
                                        } else {
                                            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                        }
                                        break;

                                    case KEY_ADV_MODE:
                                        if (result == 0xAA) {
                                            ToastUtils.showToast(this, "Success");
                                        } else {
                                            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_ALL_SLOT_ADV_TYPE:
                                        if (length == 6) {
                                            slotBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        }
                                        break;
                                    case KEY_SLOT_TRIGGER_TYPE:
                                        if (length == 2) {
                                            int slot = value[4] & 0xff;
                                            int triggerType = value[5] & 0xff;
                                            if (slot == 0)
                                                slotFragment.setSlot1(slotBytes, triggerType);
                                            else if (slot == 1)
                                                slotFragment.setSlot2(slotBytes, triggerType);
                                            else if (slot == 2)
                                                slotFragment.setSlot3(slotBytes, triggerType);
                                        }
                                        break;

                                    case KEY_DEVICE_MAC:
                                        if (length == 6) {
                                            String mac = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 10));
                                            StringBuilder stringBuffer = new StringBuilder(mac);
                                            stringBuffer.insert(2, ":");
                                            stringBuffer.insert(5, ":");
                                            stringBuffer.insert(8, ":");
                                            stringBuffer.insert(11, ":");
                                            stringBuffer.insert(14, ":");
                                            mDeviceMac = stringBuffer.toString().toUpperCase();
                                            deviceFragment.setMacAddress(mDeviceMac);
                                        }
                                        break;

                                    case KEY_SENSOR_TYPE:
                                        if (length == 5) {
                                            isSupportAcc = (value[4] & 0xff) != 0;
                                            isSupportTH = (value[5] & 0xff) != 0;
                                            deviceTypeBytes = Arrays.copyOfRange(value, 4, 6);
                                        }
                                        break;

                                    case KEY_HALL_POWER_ENABLE:
                                        if (length == 1) {
                                            isHallPowerEnable = (value[4] & 0xff) == 1;
                                            if (!isSupportAcc && !isSupportTH && isHallPowerEnable) {
                                                settingFragment.setSensorGone();
                                            }
                                            settingFragment.setDeviceTypeValue(deviceTypeBytes, isHallPowerEnable);
                                        }
                                        break;

                                    case KEY_BATTERY_VOLTAGE:
                                        if (length == 2) {
                                            int battery = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                            deviceFragment.setBattery(battery);
                                        }
                                        break;

                                    case KEY_ADV_MODE:
                                        if (length == 1) {
                                            settingFragment.setAdvMode(value[4] & 0xff);
                                        }
                                        break;
                                }
                            }
                        }
                        break;

                    case CHAR_MODEL_NUMBER:
                        deviceFragment.setProductMode(new String(value).trim());
                        break;

                    case CHAR_SOFTWARE_REVISION:
                        deviceFragment.setSoftwareVersion(new String(value).trim());
                        break;

                    case CHAR_FIRMWARE_REVISION:
                        deviceFragment.setFirmwareVersion(new String(value).trim());
                        break;

                    case CHAR_HARDWARE_REVISION:
                        deviceFragment.setHardwareVersion(new String(value).trim());
                        break;

                    case CHAR_SERIAL_NUMBER:
                        deviceFragment.setProductDate(new String(value).trim());
                        break;

                    case CHAR_MANUFACTURER_NAME:
                        deviceFragment.setManufacturer(new String(value).trim());
                        break;
                }
            }
        });
    }

    public void setModifyPassword(boolean isModifyPassword) {
        this.isModifyPassword = isModifyPassword;
    }

    private void getDeviceInfo() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getProductDate());
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
                        AlertMessageDialog dialog = new AlertMessageDialog();
                        dialog.setTitle("Dismiss");
                        dialog.setCancelGone();
                        dialog.setMessage("The current system of bluetooth is not available!");
                        dialog.setConfirm(R.string.ok);
                        dialog.setOnAlertConfirmListener(() -> finish());
                        dialog.show(getSupportFragmentManager());
                    }
                }
            }
        }
    };

    public void onQuickClick(View view) {
        quickLauncher.launch(new Intent(this, QuickSwitchActivity.class));
    }

    @Subscribe
    public void onHallDisable(String hallDisable) {
        if ("hallDisable".equals(hallDisable)) {
            //禁用了霍尔关机功能
            isHallPowerEnable = true;
            settingFragment.setDeviceTypeValue(deviceTypeBytes, true);
        }
    }

    private final ActivityResultLauncher<Intent> quickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (null != result && null != result.getData()) {
            Intent intent = result.getData();
            boolean pwdEnable = intent.getBooleanExtra("pwdEnable", false);
            isHallPowerEnable = intent.getBooleanExtra("hallEnable", false);
            settingFragment.setDeviceTypeValue(deviceTypeBytes, isHallPowerEnable);
            if (!isSupportAcc && !isSupportTH && isHallPowerEnable) {
                settingFragment.setSensorGone();
            }
            settingFragment.setResetVisibility(pwdEnable);
        }
    });

    private final ActivityResultLauncher<String> chooseLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        String firmwareFilePath = FileUtils.getPath(this, result);
        if (TextUtils.isEmpty(firmwareFilePath)) return;
        final File firmwareFile = new File(firmwareFilePath);
        if (firmwareFile.exists()) {
            final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                    .setKeepBond(false)
                    .setDisableNotification(true);
            starter.setZip(null, firmwareFilePath);
            starter.start(this, DfuService.class);
            showDFUProgressDialog("Waiting...");
        } else {
            Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
        }
    });

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
        MokoSupport.getInstance().disConnectBle();
        mIsClose = false;
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void initFragment() {
        slotFragment = SlotFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        deviceFragment = DeviceFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, slotFragment)
                .add(R.id.frame_container, settingFragment)
                .add(R.id.frame_container, deviceFragment)
                .show(slotFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();
    }

    private void showSlotFragment() {
        if (slotFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(settingFragment)
                    .hide(deviceFragment)
                    .show(slotFragment)
                    .commit();
        }
        mBind.tvTitle.setText("SLOT");
    }

    private void showSettingFragment() {
        if (settingFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(slotFragment)
                    .hide(deviceFragment)
                    .show(settingFragment)
                    .commit();
        }
        mBind.tvTitle.setText("SETTING");
    }

    private void showDeviceFragment() {
        if (deviceFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(slotFragment)
                    .hide(settingFragment)
                    .show(deviceFragment)
                    .commit();
        }
        mBind.tvTitle.setText("DEVICE");
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.radioBtn_alarm) {
            showSlotFragment();
            getSlot();
        } else if (checkedId == R.id.radioBtn_setting) {
            showSettingFragment();
            getSettings();
        } else if (checkedId == R.id.radioBtn_device) {
            showDeviceFragment();
            getDeviceInfo();
        }
    }

    private void getSettings() {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAdvMode());
    }

    private void getSlot() {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getAllSlotAdvType());
        orderTasks.add(OrderTaskAssembler.getSlotTriggerType(0));
        orderTasks.add(OrderTaskAssembler.getSlotTriggerType(1));
        orderTasks.add(OrderTaskAssembler.getSlotTriggerType(2));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    public void onBack(View view) {
        back();
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        AlertMessageDialog dialog = new AlertMessageDialog();
        if (isUpgradeCompleted) {
            dialog.setMessage("DFU Successfully!\nPlease reconnect the device.");
        } else {
            dialog.setMessage("Opps!DFU Failed.\nPlease try again!");
        }
        dialog.setCancelGone();
        dialog.setConfirm(R.string.ok);
        dialog.setOnAlertConfirmListener(() -> {
            isUpgrading = false;
            MokoSupport.getInstance().disConnectBle();
            EventBus.getDefault().post("refresh");
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private int mDeviceConnectCount;
    private boolean isUpgrading;
    private boolean isUpgradeCompleted;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            XLog.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                ToastUtils.showToast(DeviceInfoActivity.this, "Error:DFU Failed");
                MokoSupport.getInstance().disConnectBle();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            XLog.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(@NonNull String deviceAddress) {
            isUpgrading = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }

        @Override
        public void onEnablingDfuMode(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(@NonNull String deviceAddress) {
            XLog.w("onDfuCompleted...");
            isUpgradeCompleted = true;
        }

        @Override
        public void onDfuAborted(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(@NonNull String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            String progress = String.format("Progress:%d%%", percent);
            XLog.i(progress);
            mDFUDialog.setMessage(progress);
        }

        @Override
        public void onError(@NonNull String deviceAddress, int error, int errorType, String message) {
            XLog.i("DFU Error:" + message + error);
            dismissDFUProgressDialog();
        }
    };

    public void onDFU(View view) {
        if (isWindowLocked()) return;
        chooseLauncher.launch("*/*");
    }
}
