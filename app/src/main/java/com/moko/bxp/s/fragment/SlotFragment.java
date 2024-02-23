package com.moko.bxp.s.fragment;

import static com.moko.support.s.entity.SlotFrameTypeEnum.IBEACON;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.activity.DeviceInfoActivity;
import com.moko.bxp.s.activity.SlotDataActivity;
import com.moko.bxp.s.activity.TriggerStep1Activity;
import com.moko.bxp.s.databinding.FragmentSlotBinding;
import com.moko.bxp.s.entity.SlotData;
import com.moko.bxp.s.utils.SlotAdvType;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.SlotEnum;
import com.moko.support.s.entity.SlotFrameTypeEnum;
import com.moko.support.s.entity.UrlSchemeEnum;

import java.util.ArrayList;
import java.util.Arrays;

public class SlotFragment extends Fragment {
    private FragmentSlotBinding mBind;
    private SlotData slotData;
    private int slot1TriggerType;
    private int slot2TriggerType;
    private int slot3TriggerType;
    private byte[] frameTypeBytes;
    private DeviceInfoActivity activity;
    private boolean isC112;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        return new SlotFragment();
    }

    public void setC112(boolean isC112) {
        this.isC112 = isC112;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentSlotBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        setListener();
        return mBind.getRoot();
    }

    private void setListener() {
        mBind.rlSlot1.setOnClickListener(v -> {
            if (slot1TriggerType == 0) {
                //没有配置触发功能
                slotData = new SlotData();
                slotData.frameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameTypeBytes[0] & 0xff);
                if (null == slotData.frameTypeEnum) return;
                createData(slotData.frameTypeEnum, SlotEnum.SLOT1);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(0, slot1TriggerType);
            }
        });
        mBind.rlSlot2.setOnClickListener(v -> {
            if (slot2TriggerType == 0) {
                //没有配置触发功能
                slotData = new SlotData();
                slotData.frameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameTypeBytes[1] & 0xff);
                if (null == slotData.frameTypeEnum) return;
                createData(slotData.frameTypeEnum, SlotEnum.SLOT2);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(1, slot2TriggerType);
            }
        });
        mBind.rlSlot3.setOnClickListener(v -> {
            if (slot3TriggerType == 0) {
                //没有配置触发功能
                slotData = new SlotData();
                slotData.frameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameTypeBytes[2] & 0xff);
                if (null == slotData.frameTypeEnum) return;
                createData(slotData.frameTypeEnum, SlotEnum.SLOT3);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(2, slot3TriggerType);
            }
        });
    }

    private void toTriggerActivity(int slot, int triggerType) {
        Intent intent = new Intent(requireActivity(), TriggerStep1Activity.class);
        intent.putExtra("slot", slot);
        intent.putExtra("isC112", isC112);
        intent.putExtra("triggerType", triggerType);
        startActivity(intent);
    }

    private void createData(SlotFrameTypeEnum frameType, SlotEnum slot) {
        slotData.slotEnum = slot;
        switch (frameType) {
            case NO_DATA:
                Intent intent = new Intent(getActivity(), SlotDataActivity.class);
                intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
                startActivity(intent);
                break;
            case IBEACON:
            case TLM:
            case URL:
            case UID:
            case SENSOR_INFO:
                getSlotData(slot);
                break;
        }
    }

    private void getSlotData(SlotEnum slotEnum) {
        activity.showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getSlotAdvParams(slotEnum.ordinal()));
        orderTasks.add(OrderTaskAssembler.getTriggerBeforeSlotParams(slotEnum.ordinal()));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setSlot1(byte[] bytes, int triggerType) {
        slot1TriggerType = triggerType;
        frameTypeBytes = bytes;
        if (triggerType != 0) {
            //有触发
            mBind.tvSlot1.setText(SlotAdvType.getSlotAdvType(bytes[0] & 0xff) + "/" + SlotAdvType.getSlotAdvType(bytes[3] & 0xff));
        } else {
            mBind.tvSlot1.setText(SlotAdvType.getSlotAdvType(bytes[0] & 0xff));
        }
    }

    public void setSlot2(byte[] bytes, int triggerType) {
        slot2TriggerType = triggerType;
        if (triggerType != 0) {
            //有触发
            mBind.tvSlot2.setText(SlotAdvType.getSlotAdvType(bytes[1] & 0xff) + "/" + SlotAdvType.getSlotAdvType(bytes[4] & 0xff));
        } else {
            mBind.tvSlot2.setText(SlotAdvType.getSlotAdvType(bytes[1] & 0xff));
        }
    }

    public void setSlot3(byte[] bytes, int triggerType) {
        slot3TriggerType = triggerType;
        if (triggerType != 0) {
            //有触发
            mBind.tvSlot3.setText(SlotAdvType.getSlotAdvType(bytes[2] & 0xff) + "/" + SlotAdvType.getSlotAdvType(bytes[5] & 0xff));
        } else {
            mBind.tvSlot3.setText(SlotAdvType.getSlotAdvType(bytes[2] & 0xff));
        }
    }

    // 不同类型的数据长度不同
    public void setSlotParams(byte[] rawDataBytes) {
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

    public void setSlotAdvParams(byte[] rawDataBytes, boolean isC112) {
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

        Intent intent = new Intent(getActivity(), SlotDataActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
        startActivity(intent);
    }
}
