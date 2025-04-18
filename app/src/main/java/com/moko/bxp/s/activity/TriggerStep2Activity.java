package com.moko.bxp.s.activity;

import static com.moko.support.s.entity.SlotAdvType.I_BEACON;
import static com.moko.support.s.entity.SlotAdvType.NO_DATA;
import static com.moko.support.s.entity.SlotAdvType.SENSOR_INFO;
import static com.moko.support.s.entity.SlotAdvType.TLM;
import static com.moko.support.s.entity.SlotAdvType.UID;
import static com.moko.support.s.entity.SlotAdvType.URL;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityTriggerStep2Binding;
import com.moko.lib.bxpui.dialog.BottomDialog;
import com.moko.bxp.s.fragment.IBeaconFragment;
import com.moko.bxp.s.fragment.SensorInfoFragment;
import com.moko.bxp.s.fragment.TlmFragment;
import com.moko.bxp.s.fragment.UidFragment;
import com.moko.bxp.s.fragment.UrlFragment;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.entity.SlotData;
import com.moko.support.s.entity.TriggerStep1Bean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author: jun.liu
 * @date: 2024/10/10 10:05
 * @des:
 */
public class TriggerStep2Activity extends BaseActivity<ActivityTriggerStep2Binding> {
    private int slot;
    private final String[] frameTypeArray = {"Sensor info", "TLM", "UID", "URL", "iBeacon"};
    private int rawFrameType;
    private int currentIndex;
    private TriggerStep1Bean step1Bean;
    private FragmentManager fragmentManager;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    private SensorInfoFragment sensorInfoFragment;
    private ISlotDataAction slotDataActionImpl;
    private int currentFrameType;
    private SlotData originSlotData;

    @Override
    protected void onCreate() {
        slot = getIntent().getIntExtra(AppConstants.SLOT, 0);
        step1Bean = getIntent().getParcelableExtra("step1");
        fragmentManager = getSupportFragmentManager();
        createFragments();
        setListener();
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            //获取触发后的参数
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getTriggerAfterSlotParams(slot));
        }
        mBind.tvTitle.setText("SLOT" + (slot + 1));
        mBind.tvBack.setOnClickListener(v -> finish());
    }

    @Override
    protected ActivityTriggerStep2Binding getViewBinding() {
        return ActivityTriggerStep2Binding.inflate(getLayoutInflater());
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        uidFragment = UidFragment.newInstance();
        uidFragment.setTriggerAfter(true, step1Bean);
        fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
        urlFragment = UrlFragment.newInstance();
        urlFragment.setTriggerAfter(true, step1Bean);
        fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
        tlmFragment = TlmFragment.newInstance();
        tlmFragment.setTriggerAfter(true, step1Bean);
        fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
        iBeaconFragment = IBeaconFragment.newInstance();
        iBeaconFragment.setTriggerAfter(true, step1Bean);
        fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
        sensorInfoFragment = SensorInfoFragment.newInstance();
        sensorInfoFragment.setTriggerAfter(true, step1Bean);
        fragmentTransaction.add(R.id.frame_slot_container, sensorInfoFragment);
        fragmentTransaction.commit();
    }

    private void showFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (currentFrameType) {
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
            case I_BEACON:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(sensorInfoFragment).show(iBeaconFragment).commit();
                slotDataActionImpl = iBeaconFragment;
                break;
            case SENSOR_INFO:
            case NO_DATA:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(sensorInfoFragment).commit();
                slotDataActionImpl = sensorInfoFragment;
                break;
        }
    }

    private void setListener() {
        mBind.tvFrameType.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(frameTypeArray)), currentIndex);
            dialog.setListener(value -> {
                currentIndex = value;
                currentFrameType = getFrameTypeByIndex(value);
                mBind.tvFrameType.setText(frameTypeArray[value]);
                showFragment();
                if (null != slotDataActionImpl) {
                    if (currentFrameType == rawFrameType) {
                        slotDataActionImpl.setParams(originSlotData);
                    } else {
                        SlotData slotData = new SlotData();
                        slotData.slot = this.slot;
                        slotData.step1TriggerType = step1Bean.triggerType;
                        slotData.currentFrameType = currentFrameType;
                        slotDataActionImpl.setParams(slotData);
                    }
                }
            });
            dialog.show(getSupportFragmentManager());
        });
        mBind.btnNext.setOnClickListener(v -> {
            if (slotDataActionImpl.isValid()) {
                SlotData slotData = slotDataActionImpl.getSlotData();
                slotData.slot = slot;
                Intent intent = new Intent(this, TriggerStep3Activity.class);
                intent.putExtra("step1", step1Bean);
                intent.putExtra("step2", slotData);
                intent.putExtra(AppConstants.SLOT, slot);
                startActivity(intent);
            }
        });
        mBind.tvBack.setOnClickListener(v -> finish());
        mBind.btnBack.setOnClickListener(v -> finish());
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_SLOT_PARAMS_AFTER) {
                                if (length >= 8) {
                                    byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                    setSlotAdvParams(rawDataBytes);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void setSlotAdvParams(@NonNull byte[] value) {
        rawFrameType = currentFrameType = value[7] & 0xff;
        int type = value[7] & 0xff;
        if (currentFrameType == NO_DATA) {
            rawFrameType = currentFrameType = SENSOR_INFO;
        }
        int index = currentIndex = getSlotIndex(rawFrameType);
        mBind.tvFrameType.setText(frameTypeArray[index]);
        showFragment();
        SlotData slotData = new SlotData();
        slotData.advInterval = MokoUtils.toInt(Arrays.copyOfRange(value, 1, 3));
        slotData.advDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
        slotData.rssi = value[5];
        slotData.txPower = value[6];
        slotData.currentFrameType = currentFrameType;
        slotData.step1TriggerType = step1Bean.triggerType;
        slotData.realType = type;
        slotData.slot = this.slot;
        if (type == UID) {
            slotData.namespace = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 8, 18));
            slotData.instanceId = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 18, 24));
        } else if (type == URL) {
            slotData.urlScheme = value[8] & 0xff;
            slotData.urlContent = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 9, value.length));
        } else if (type == I_BEACON) {
            slotData.uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 8, 24));
            slotData.major = MokoUtils.toInt(Arrays.copyOfRange(value, 24, 26));
            slotData.minor = MokoUtils.toInt(Arrays.copyOfRange(value, 26, 28));
        } else if (type == SENSOR_INFO) {
            int nameLength = value[8] & 0xff;
            slotData.deviceName = new String(Arrays.copyOfRange(value, 9, 9 + nameLength));
            slotData.tagId = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 10 + nameLength, value.length));
        }
        if (null != slotDataActionImpl) {
            originSlotData = slotData;
            slotDataActionImpl.setParams(slotData);
        }
    }

    private int getSlotIndex(int slotType) {
        //"Sensor info", "TLM", "UID", "URL", "iBeacon"
        switch (slotType) {
            case UID:
                return 2;
            case URL:
                return 3;
            case TLM:
                return 1;
            case I_BEACON:
                return 4;
            case SENSOR_INFO:
                return 0;
        }
        return 0;
    }

    private int getFrameTypeByIndex(int index) {
        switch (index) {
            case 0:
                return SENSOR_INFO;
            case 1:
                return TLM;
            case 2:
                return UID;
            case 3:
                return URL;
            case 4:
                return I_BEACON;
        }
        return SENSOR_INFO;
    }
}
