package com.moko.bxp.s.activity;

import static com.moko.support.s.entity.SlotFrameTypeEnum.IBEACON;
import static com.moko.support.s.entity.SlotFrameTypeEnum.SENSOR_INFO;
import static com.moko.support.s.entity.SlotFrameTypeEnum.TLM;
import static com.moko.support.s.entity.SlotFrameTypeEnum.UID;
import static com.moko.support.s.entity.SlotFrameTypeEnum.URL;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityTriggerStep1Binding;
import com.moko.bxp.s.dialog.BottomDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.entity.SlotData;
import com.moko.bxp.s.entity.TriggerStep1Bean;
import com.moko.bxp.s.fragment.IBeaconFragment;
import com.moko.bxp.s.fragment.SensorInfoFragment;
import com.moko.bxp.s.fragment.TlmFragment;
import com.moko.bxp.s.fragment.UidFragment;
import com.moko.bxp.s.fragment.UrlFragment;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.entity.SlotFrameTypeEnum;
import com.moko.support.s.entity.UrlSchemeEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author: jun.liu
 * @date: 2024/1/30 12:05
 * @des:
 */
public class TriggerStep1Activity extends BaseActivity {
    private ActivityTriggerStep1Binding mBind;
    private boolean mReceiverTag;
    private boolean isTrigger;
    private boolean advBeforeTrigger = true;
    private int slot;
    private final String[] frameTypeArray = {"UID", "URL", "TLM", "iBeacon", "Sensor info", "T&H"};
    private int frameTypeSelected;
    private boolean isC112;
    private int currentIndex;
    private TriggerStep1Bean bean;

    private FragmentManager fragmentManager;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    private SensorInfoFragment sensorInfoFragment;
    public SlotData slotData = new SlotData();
    private ISlotDataAction slotDataActionImpl;
    private SlotFrameTypeEnum currentFrameTypeEnum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityTriggerStep1Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        slot = getIntent().getIntExtra("slot", 0);
        isC112 = getIntent().getBooleanExtra("isC112", false);
        int triggerType = getIntent().getIntExtra("triggerType", 0);
        fragmentManager = getSupportFragmentManager();
        isTrigger = triggerType != 0;
        setListener();
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.getSlotAdvParams(slot));
            orderTasks.add(OrderTaskAssembler.getTriggerBeforeSlotParams(slot));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
        mBind.tvTitle.setText("SLOT" + (slot + 1));
        mBind.tvBack.setOnClickListener(v -> finish());
        mBind.ivTrigger.setImageResource(isTrigger ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.cbLayoutAdvTrigger.setVisibility(isTrigger ? View.VISIBLE : View.GONE);
        mBind.frameSlotContainer.setVisibility(isTrigger ? View.VISIBLE : View.GONE);
        mBind.btnNext.setText(isTrigger ? "Next" : "Save");
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        uidFragment = UidFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
        uidFragment.setSlotData(slotData);
        urlFragment = UrlFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
        urlFragment.setSlotData(slotData);
        tlmFragment = TlmFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
        tlmFragment.setSlotData(slotData);
        iBeaconFragment = IBeaconFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
        iBeaconFragment.setSlotData(slotData);
        sensorInfoFragment = SensorInfoFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, sensorInfoFragment);
        sensorInfoFragment.setSlotData(slotData);
        fragmentTransaction.commit();
    }

    private void showFragment(int index) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromShowName(frameTypeArray[index]);
        if (null == slotFrameTypeEnum) return;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (slotFrameTypeEnum) {
            case TLM:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(sensorInfoFragment).show(tlmFragment).commit();
                slotDataActionImpl = tlmFragment;
                break;
            case UID:
                fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(sensorInfoFragment).show(uidFragment).commit();
                slotDataActionImpl = uidFragment;
                break;
            case URL:
                fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(sensorInfoFragment).show(urlFragment).commit();
                slotDataActionImpl = urlFragment;
                break;
            case IBEACON:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(sensorInfoFragment).show(iBeaconFragment).commit();
                slotDataActionImpl = iBeaconFragment;
                break;
            case SENSOR_INFO:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(sensorInfoFragment).commit();
                slotDataActionImpl = sensorInfoFragment;
                break;
            case NO_DATA:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(sensorInfoFragment).commit();
                slotDataActionImpl = null;
                break;
        }
        slotData.frameTypeEnum = slotFrameTypeEnum;
    }

    private void setListener() {
        mBind.ivAdv.setOnClickListener(v -> {
            advBeforeTrigger = !advBeforeTrigger;
            mBind.ivAdv.setImageResource(advBeforeTrigger ? R.drawable.ic_checked : R.drawable.ic_unchecked);
            mBind.layoutAdvTrigger.setVisibility(advBeforeTrigger ? View.VISIBLE : View.GONE);
            mBind.frameSlotContainer.setVisibility(advBeforeTrigger ? View.VISIBLE : View.GONE);
        });
        mBind.ivTrigger.setOnClickListener(v -> {
            isTrigger = !isTrigger;
            mBind.ivTrigger.setImageResource(isTrigger ? R.drawable.ic_checked : R.drawable.ic_unchecked);
            if (isTrigger) {
                mBind.cbLayoutAdvTrigger.setVisibility(View.VISIBLE);
                mBind.frameSlotContainer.setVisibility(View.VISIBLE);
                if (!advBeforeTrigger) {
                    mBind.layoutAdvTrigger.setVisibility(View.GONE);
                    mBind.frameSlotContainer.setVisibility(View.GONE);
                }
            } else {
                mBind.cbLayoutAdvTrigger.setVisibility(View.GONE);
                mBind.frameSlotContainer.setVisibility(View.GONE);
            }


            mBind.btnNext.setText(isTrigger ? "Next" : "Save");
        });
        mBind.tvFrameType.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(frameTypeArray)), frameTypeSelected);
            dialog.setListener(value -> {
                frameTypeSelected = value;
                mBind.tvFrameType.setText(frameTypeArray[value]);
                showFragment(value);
                if (null != slotDataActionImpl)
                    slotDataActionImpl.resetParams(currentFrameTypeEnum);
            });
            dialog.show(getSupportFragmentManager());
        });
        mBind.btnNext.setOnClickListener(v -> {
            if (!isTrigger) {
                //当前配置成无触发
                showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setSlotTriggerType(slot, 0));
                return;
            }
            bean = new TriggerStep1Bean();
            bean.frameType = Integer.parseInt(slotData.frameTypeEnum.getFrameType(), 16);
            bean.advBeforeTrigger = advBeforeTrigger;
            TriggerStep1Bean triggerStep1Bean = null;
            if (slotData.frameTypeEnum == UID) {
                //uid
                if (uidFragment.isValid()) {
                    triggerStep1Bean = uidFragment.getTriggerStep1Bean();
                    bean.namespaceId = triggerStep1Bean.namespaceId;
                    bean.instanceId = triggerStep1Bean.instanceId;
                }
            } else if (slotData.frameTypeEnum == URL) {
                //url
                if (urlFragment.isValid()) {
                    triggerStep1Bean = urlFragment.getTriggerStep1Bean();
                    bean.urlScheme = triggerStep1Bean.urlScheme;
                    bean.url = triggerStep1Bean.url;
                }
            } else if (slotData.frameTypeEnum == IBEACON) {
                //iBeacon
                if (iBeaconFragment.isValid()) {
                    triggerStep1Bean = iBeaconFragment.getTriggerStep1Bean();
                    bean.major = triggerStep1Bean.major;
                    bean.minor = triggerStep1Bean.minor;
                    bean.uuid = triggerStep1Bean.uuid;
                }
            } else if (slotData.frameTypeEnum == SENSOR_INFO) {
                //sensor info
                if (sensorInfoFragment.isValid()) {
                    triggerStep1Bean = sensorInfoFragment.getTriggerStep1Bean();
                    bean.deviceName = triggerStep1Bean.deviceName;
                    bean.tagId = triggerStep1Bean.tagId;
                }
            } else if (slotData.frameTypeEnum == TLM) {
                triggerStep1Bean = tlmFragment.getTriggerStep1Bean();
            }
            if (null == triggerStep1Bean) return;
            //通道参数
            bean.advInterval = triggerStep1Bean.advInterval * 100;
            bean.isLowPowerMode = triggerStep1Bean.isLowPowerMode;
            bean.advDuration = triggerStep1Bean.advDuration;
            bean.standByDuration = triggerStep1Bean.standByDuration;
            bean.rssi = triggerStep1Bean.rssi;
            bean.txPower = triggerStep1Bean.txPower;
            Intent intent = new Intent(this, TriggerStep2Activity.class);
            intent.putExtra("step1", bean);
            intent.putExtra("slot", slot);
            intent.putExtra("c112", isC112);
            startActivity(intent);
            finish();
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
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length > 4) {
                        int header = value[0] & 0xFF;// 0xEB
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xEB) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_SLOT_ADV_PARAMS:
                                    if (length > 1) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        setSlotParams(rawDataBytes);
                                        int slotType = value[5] & 0xff;
                                        if (slotType == 0xff) {
                                            //noData
                                            mBind.layoutAdvTrigger.setVisibility(View.GONE);
                                            isTrigger = false;
                                            mBind.ivAdv.setImageResource(R.drawable.ic_unchecked);
                                        } else {
                                            frameTypeSelected = getSlotIndex(slotType);
                                            currentIndex = frameTypeSelected;
                                            currentFrameTypeEnum = slotData.frameTypeEnum = SlotFrameTypeEnum.fromShowName(frameTypeArray[currentIndex]);
                                            mBind.tvFrameType.setText(frameTypeArray[frameTypeSelected]);
                                        }
                                    }
                                    break;

                                case KEY_SLOT_PARAMS_BEFORE:
                                    if (length == 9) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        setSlotAdvParams(rawDataBytes);
                                        createFragments();
                                        showFragment(currentIndex);
                                    }
                                    break;
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

    private void setSlotParams(byte[] rawDataBytes) {
        int frameType = rawDataBytes[1] & 0xFF;
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum != null) {
            switch (slotFrameTypeEnum) {
                case URL:
                    int urlType = (int) rawDataBytes[2] & 0xff;
                    slotData.urlSchemeEnum = UrlSchemeEnum.fromUrlType(urlType);
                    slotData.urlContentHex = MokoUtils.bytesToHexString(rawDataBytes).substring(6);
                    break;
                case UID:
                    slotData.namespace = MokoUtils.bytesToHexString(rawDataBytes).substring(4, 24);
                    slotData.instanceId = MokoUtils.bytesToHexString(rawDataBytes).substring(24);
                    break;
                case SENSOR_INFO:
                    int deviceNameLength = rawDataBytes[2] & 0xFF;
                    byte[] deviceName = Arrays.copyOfRange(rawDataBytes, 3, 3 + deviceNameLength);
                    slotData.deviceName = new String(deviceName);
                    byte[] tagId = Arrays.copyOfRange(rawDataBytes, 4 + deviceNameLength, rawDataBytes.length);
                    slotData.tagId = MokoUtils.bytesToHexString(tagId);
                    break;
                case IBEACON:
                    byte[] major = Arrays.copyOfRange(rawDataBytes, 2, 4);
                    byte[] minor = Arrays.copyOfRange(rawDataBytes, 4, 6);
                    byte[] uuid = Arrays.copyOfRange(rawDataBytes, 6, 22);
                    slotData.major = MokoUtils.bytesToHexString(major);
                    slotData.minor = MokoUtils.bytesToHexString(minor);
                    slotData.iBeaconUUID = MokoUtils.bytesToHexString(uuid);
                    break;
            }
        }
    }

    private void setSlotAdvParams(byte[] rawDataBytes) {
        slotData.advInterval = MokoUtils.toInt(Arrays.copyOfRange(rawDataBytes, 1, 3));
        slotData.advDuration = MokoUtils.toInt(Arrays.copyOfRange(rawDataBytes, 3, 5));
        slotData.standbyDuration = MokoUtils.toInt(Arrays.copyOfRange(rawDataBytes, 5, 7));
        if (slotData.frameTypeEnum == IBEACON) {
            slotData.rssi_1m = rawDataBytes[7];
        } else {
            slotData.rssi_0m = rawDataBytes[7];
        }
        slotData.txPower = rawDataBytes[8];
        slotData.isC112 = isC112;
    }

    private int getSlotIndex(int slotType) {
        //"UID", "URL", "TLM", "iBeacon", "Sensor info", "T&H
        switch (slotType) {
            case 0x00:
                return 0;
            case 0x10:
                return 1;
            case 0x20:
                return 2;
            case 0x50:
                return 3;
            case 0x80:
                return 4;
            case 0x70:
                return 5;
        }
        return 0;
    }
}
