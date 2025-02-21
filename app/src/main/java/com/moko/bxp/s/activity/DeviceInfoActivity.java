package com.moko.bxp.s.activity;

import static com.moko.support.s.entity.SlotAdvType.NO_DATA;
import static com.moko.support.s.entity.SlotAdvType.NO_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.SLOT1;
import static com.moko.support.s.entity.SlotAdvType.SLOT2;
import static com.moko.support.s.entity.SlotAdvType.SLOT3;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
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
import androidx.fragment.app.FragmentManager;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityDeviceInfoSBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.entity.TriggerEvent;
import com.moko.bxp.s.fragment.DeviceFragment;
import com.moko.bxp.s.fragment.SettingFragment;
import com.moko.bxp.s.fragment.SlotFragment;
import com.moko.bxp.s.utils.FileUtils;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.task.OTADataTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private ActivityDeviceInfoSBinding mBind;
    private FragmentManager fragmentManager;
    private SlotFragment slotFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mDeviceMac;
    private boolean mIsClose;
    private boolean mReceiverTag = false;
    private int mDisconnectType;
    private boolean isModifyPassword;
    private boolean isButtonPowerEnable;
    private boolean isButtonResetEnable;
    private boolean enablePwd;
    private int accStatus = -1;
    private int thStatus;
    private boolean hasGetInfo;
    //ota
    private int mIndex = 0;
    private boolean mLastPackage = false;
    private int mPackageCount = 0;
    private final int MTU = 233;
    private boolean isUpgrading;
    private boolean isUpgradeCompleted;
    private boolean isOTAMode;
    private byte[] mFirmwareFileBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceInfoSBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        fragmentManager = getSupportFragmentManager();
        enablePwd = getIntent().getBooleanExtra("pwdEnable", false);
        mDeviceMac = getIntent().getStringExtra("mac");
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
        getSlotData();
    }

    private void getSlotData() {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getSensorType());
        orderTasks.add(OrderTaskAssembler.getButtonTurnOffEnable());
        orderTasks.add(OrderTaskAssembler.getResetByButtonEnable());
        orderTasks.add(OrderTaskAssembler.getAllSlotAdvType());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    public void getSlotType() {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAllSlotAdvType());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getSlotData();
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
                if (isUpgrading) {
                    if (!isOTAMode) {
                        reconnectOTADevice();
                    } else {
                        dismissDFUProgressDialog();
                    }
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
            } else if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                if (isUpgrading) {
                    if (mDFUDialog != null && mDFUDialog.isShowing())
                        mDFUDialog.setMessage("EnablingDfuMode...");
                    otaBegin();
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
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action) || MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
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
                            if (flag == 1 && length == 1) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_ADV_MODE:
                                    case KEY_BATTERY_MODE:
                                    case KEY_ADV_CHANNEL:
                                    case KEY_BATTERY_PERCENT:
                                        if (result == 0xAA) {
                                            ToastUtils.showToast(this, "Success");
                                        } else {
                                            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                        }
                                        break;
                                }
                            } else if (flag == 0) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_ALL_SLOT_ADV_TYPE:
                                        if (length == 3) {
                                            byte[] slotBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            slotFragment.setSlotType(slotBytes);
                                            TriggerEvent triggerEvent = new TriggerEvent();
                                            triggerEvent.triggerType = NO_TRIGGER;
                                            List<OrderTask> orderTasks = new ArrayList<>(3);
                                            if ((slotBytes[0] & 0xff) == NO_DATA) {
                                                slotFragment.setSlotTriggerType(SLOT1, triggerEvent);
                                            } else {
                                                orderTasks.add(OrderTaskAssembler.getSlotTriggerType(SLOT1));
                                            }
                                            if ((slotBytes[1] & 0xff) == NO_DATA) {
                                                slotFragment.setSlotTriggerType(SLOT2, triggerEvent);
                                            } else {
                                                orderTasks.add(OrderTaskAssembler.getSlotTriggerType(SLOT2));
                                            }
                                            if ((slotBytes[2] & 0xff) == NO_DATA) {
                                                slotFragment.setSlotTriggerType(SLOT3, triggerEvent);
                                            } else {
                                                orderTasks.add(OrderTaskAssembler.getSlotTriggerType(SLOT3));
                                            }
                                            if (!orderTasks.isEmpty()) {
                                                //获取通道触发类型
                                                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
                                            } else {
                                                dismissSyncProgressDialog();
                                            }
                                        }
                                        break;

                                    case KEY_SLOT_TRIGGER_TYPE:
                                        if (length == 8) {
                                            int slot = value[4] & 0xff;
                                            TriggerEvent bean = new TriggerEvent();
                                            bean.triggerType = value[5] & 0xff;
                                            bean.triggerCondition = value[6] & 0xff;
                                            bean.triggerThreshold = MokoUtils.toIntSigned(Arrays.copyOfRange(value, 7, 9));
                                            bean.lockAdvDuration = value[9] & 0xff;
                                            bean.staticPeriod = MokoUtils.toInt(Arrays.copyOfRange(value, 10, 12));
                                            slotFragment.setSlotTriggerType(slot, bean);
                                            dismissSyncProgressDialog();
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
                                            if (null != deviceFragment) {
                                                deviceFragment.setMacAddress(mDeviceMac);
                                            }
                                        }
                                        break;

                                    case KEY_SENSOR_TYPE:
                                        if (length == 5) {
                                            accStatus = value[4];
                                            thStatus = value[5];
                                        }
                                        break;

                                    case KEY_BUTTON_TURN_OFF_ENABLE:
                                        if (length == 1) {
                                            isButtonPowerEnable = (value[4] & 0xff) == 1;
                                        }
                                        break;

                                    case KEY_BUTTON_RESET_ENABLE:
                                        if (length == 1) {
                                            isButtonResetEnable = (value[4] & 0xff) == 1;
                                            if (accStatus == 0 && thStatus == 0 && (isButtonPowerEnable || isButtonResetEnable)) {
                                                settingFragment.setSensorGone();
                                            }
                                            settingFragment.setDeviceTypeValue(accStatus, thStatus, isButtonPowerEnable, isButtonResetEnable);
                                            slotFragment.setDeviceTypeValue(accStatus, thStatus, isButtonPowerEnable, isButtonResetEnable);
                                        }
                                        break;

                                    case KEY_BATTERY_VOLTAGE:
                                        if (length == 2) {
                                            int battery = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                            deviceFragment.setBattery(battery);
                                        }
                                        break;

                                    case KEY_BATTERY_PERCENT:
                                        if (length == 1) {
                                            deviceFragment.setBatteryPercent(value[4] & 0xff);
                                        }
                                        break;

                                    case KEY_ADV_MODE:
                                        if (length == 1) {
                                            settingFragment.setAdvMode(value[4] & 0xff);
                                        }
                                        break;

                                    case KEY_BATTERY_MODE:
                                        if (length == 1) {
                                            settingFragment.setBatteryMode(value[4] & 0xff);
                                        }
                                        break;

                                    case KEY_ADV_CHANNEL:
                                        if (length == 1) {
                                            settingFragment.setAdvChannel(value[4] & 0xff);
                                        }
                                        break;

                                    case KEY_PRODUCT_MODEL:
                                        if (length > 0) {
                                            deviceFragment.setProductMode(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        }
                                        break;

                                    case KEY_SOFTWARE_VERSION:
                                        if (length > 0) {
                                            deviceFragment.setSoftwareVersion(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        }
                                        break;

                                    case KEY_FIRMWARE_VERSION:
                                        if (length > 0) {
                                            deviceFragment.setFirmwareVersion(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        }
                                        break;

                                    case KEY_HARDWARE_VERSION:
                                        if (length > 0) {
                                            deviceFragment.setHardwareVersion(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        }
                                        break;

                                    case KEY_MANUFACTURE_DATE:
                                        if (length > 0) {
                                            String year = String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6)));
                                            String month = (value[6] & 0xff) < 10 ? "0" + (value[6] & 0xff) : String.valueOf(value[6] & 0xff);
                                            String day = (value[7] & 0xff) < 10 ? "0" + (value[7] & 0xff) : String.valueOf(value[7] & 0xff);
                                            deviceFragment.setProductDate(year + "/" + month + "/" + day);
                                        }
                                        break;

                                    case KEY_MANUFACTURE:
                                        if (length > 0) {
                                            deviceFragment.setManufacturer(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        }
                                        break;
                                }
                            }
                        }
                        break;

                    case CHAR_OTA_CONTROL:
                        if (value.length == 1) {
                            if (value[0] == 0x00) {
                                // 判断是否包含Data特征
                                BluetoothGattCharacteristic characteristic = MokoSupport.getInstance().getCharacteristic(OrderCHAR.CHAR_OTA_DATA);
                                if (null != characteristic) {
                                    isOTAMode = true;
                                    // 直接发送升级包
                                    if (mDFUDialog != null && mDFUDialog.isShowing())
                                        mDFUDialog.setMessage("DfuProcessStarting...");
                                    mIndex = 0;
                                    mLastPackage = false;
                                    mPackageCount = 0;
                                    mBind.tvTitle.postDelayed(this::writeDataToDevice, 500);
                                }
                            }
                            if (value[0] == 0x03) {
                                // 完成升级
                                XLog.w("onDfuCompleted...");
                                isUpgradeCompleted = true;
                                mBind.tvTitle.postDelayed(() -> MokoSupport.getInstance().disConnectBle(), 1000);
                            }
                        } else {
                            XLog.i(MokoUtils.bytesToHexString(value));
                            int code = MokoUtils.toInt(value);
                            if (code != 0) {
                                ToastUtils.showToast(this, "Error:DFU Failed!");
                            }
                        }
                        break;
                    case CHAR_OTA_DATA:
                        if (mLastPackage) {
                            if (mDFUDialog != null && mDFUDialog.isShowing())
                                mDFUDialog.setMessage("Progress:100%");
                            XLog.i("OTA UPLOAD SEND DONE");
                            otaEnd();
                            return;
                        }
                        writeDataToDevice();
                        break;
                }
            }
        });
    }

    public void setModifyPassword(boolean isModifyPassword) {
        this.isModifyPassword = isModifyPassword;
    }

    private void getDeviceInfo() {
        hasGetInfo = true;
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>(10);
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getBatteryPercent());
        orderTasks.add(OrderTaskAssembler.getDeviceMac());
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

    private final ActivityResultLauncher<Intent> quickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (null != result && null != result.getData()) {
            Intent intent = result.getData();
            boolean pwdEnable = intent.getBooleanExtra("pwdEnable", false);
            isButtonPowerEnable = intent.getBooleanExtra(AppConstants.EXTRA_KEY1, false);
            isButtonResetEnable = intent.getBooleanExtra(AppConstants.EXTRA_KEY2, false);
            settingFragment.setDeviceTypeValue(accStatus, thStatus, isButtonPowerEnable, isButtonResetEnable);
            if (accStatus == 0 && thStatus == 0 && (isButtonPowerEnable || isButtonResetEnable)) {
                settingFragment.setSensorGone();
            }
            slotFragment.setDeviceTypeValue(accStatus, thStatus, isButtonPowerEnable, isButtonResetEnable);
            settingFragment.setResetVisibility(pwdEnable);
            settingFragment.setModifyPasswordShown(enablePwd && pwdEnable);
        }
    });

    private final ActivityResultLauncher<String> chooseLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (null == result) return;
        String firmwareFilePath = FileUtils.getPath(this, result);
        if (TextUtils.isEmpty(firmwareFilePath)) return;
        final File firmwareFile = new File(firmwareFilePath);
        if (firmwareFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(firmwareFile);
                mFirmwareFileBytes = new byte[in.available()];
                int read = in.read(mFirmwareFileBytes, 0, in.available());
                XLog.i("选择文件：" + read);
                in.close();
            } catch (IOException e) {
                XLog.e(e);
                ToastUtils.showToast(this, "file error");
                return;
            }
            isUpgrading = true;
            showDFUProgressDialog("Waiting...");
            otaBegin();
        } else {
            Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
        }
    });

    private void reconnectOTADevice() {
        mBind.tvTitle.postDelayed(() -> {
            if (mDFUDialog != null && mDFUDialog.isShowing())
                mDFUDialog.setMessage("DeviceConnecting...");
            MokoSupport.getInstance().connDevice(mDeviceMac);
        }, 4000);
    }

    // 1.
    private void otaBegin() {
        //Writing 0x00 to control characteristic to DFU mode  target device begins OTA process
        mBind.tvTitle.postDelayed(() -> {
            XLog.i("OTA BEGIN");
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.startDFU());
        }, 500);
    }

    // 2.
    @SuppressLint("DefaultLocale")
    private void writeDataToDevice() {
        byte[] payload = new byte[MTU];
        if (mIndex + MTU >= mFirmwareFileBytes.length) {
            int restSize = mFirmwareFileBytes.length - mIndex;
            System.arraycopy(mFirmwareFileBytes, mIndex, payload, 0, restSize); //copy rest bytes
            mLastPackage = true;
        } else {
            payload = Arrays.copyOfRange(mFirmwareFileBytes, mIndex, mIndex + MTU);
        }
        OTADataTask task = new OTADataTask();
        task.setData(payload);
        MokoSupport.getInstance().sendOrder(task);
        final int progress = (int) (100.0f * mIndex / mFirmwareFileBytes.length);
        if (mDFUDialog != null && mDFUDialog.isShowing())
            mDFUDialog.setMessage(String.format("Progress:%d%%", progress));
        mPackageCount = mPackageCount + 1;
        mIndex = mIndex + MTU;
    }

    // 3.
    private void otaEnd() {
        mBind.tvTitle.postDelayed(() -> {
            XLog.i("OTA END");
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.endDFU());
        }, 500);
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
        settingFragment.setModifyPasswordShown(enablePwd);
        settingFragment.setResetVisibility(enablePwd);
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
            if (!hasGetInfo) {
                getDeviceInfo();
            } else {
                showSyncingProgressDialog();
                ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
                orderTasks.add(OrderTaskAssembler.getBattery());
                orderTasks.add(OrderTaskAssembler.getBatteryPercent());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
            }
        }
    }

    private void getSettings() {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(3);
        orderTasks.add(OrderTaskAssembler.getBatteryMode());
        orderTasks.add(OrderTaskAssembler.getAdvChannel());
        orderTasks.add(OrderTaskAssembler.getAdvMode());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    private void getSlot() {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAllSlotAdvType());
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

    public void onDFU(View view) {
        if (isWindowLocked()) return;
        BluetoothGattCharacteristic characteristic = MokoSupport.getInstance().getCharacteristic(OrderCHAR.CHAR_OTA_CONTROL);
        if (characteristic == null) {
            ToastUtils.showToast(DeviceInfoActivity.this, "Error:Characteristic of OTA is null!");
            return;
        }
        chooseLauncher.launch("*/*");
    }
}
