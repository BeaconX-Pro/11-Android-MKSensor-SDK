package com.moko.bxp.s.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityDfuBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;
import com.moko.bxp.s.utils.FileUtils;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.task.OTADataTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author: jun.liu
 * @date: 2025/2/20 15:28
 * @des:
 */
public class DfuActivity extends BaseActivity{
    private ActivityDfuBinding mBind;
    //ota
    private int mIndex = 0;
    private boolean mLastPackage = false;
    private int mPackageCount = 0;
    private final int MTU = 233;
    private boolean isUpgrading;
    private boolean isUpgradeCompleted;
    private boolean isOTAMode;
    private byte[] mFirmwareFileBytes;
    private String mDeviceMac;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDfuBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mBind.tvBack.setOnClickListener(v -> back());
        mDeviceMac = getIntent().getStringExtra("mac");
        mBind.tvDfu.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            BluetoothGattCharacteristic characteristic = MokoSupport.getInstance().getCharacteristic(OrderCHAR.CHAR_OTA_CONTROL);
            if (characteristic == null) {
                ToastUtils.showToast(this, "Error:Characteristic of OTA is null!");
                return;
            }
            chooseLauncher.launch("*/*");
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
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
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
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
                                    mBind.tvDfu.postDelayed(this::writeDataToDevice, 500);
                                }
                            }
                            if (value[0] == 0x03) {
                                // 完成升级
                                XLog.w("onDfuCompleted...");
                                isUpgradeCompleted = true;
                                mBind.tvDfu.postDelayed(() -> MokoSupport.getInstance().disConnectBle(), 1000);
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

    private void back() {
        MokoSupport.getInstance().disConnectBle();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void reconnectOTADevice() {
        mBind.tvDfu.postDelayed(() -> {
            if (mDFUDialog != null && mDFUDialog.isShowing())
                mDFUDialog.setMessage("DeviceConnecting...");
            MokoSupport.getInstance().connDevice(mDeviceMac);
        }, 4000);
    }

    // 1.
    private void otaBegin() {
        //Writing 0x00 to control characteristic to DFU mode  target device begins OTA process
        mBind.tvDfu.postDelayed(() -> {
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
        mBind.tvBack.postDelayed(() -> {
            XLog.i("OTA END");
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.endDFU());
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(this);
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
}
