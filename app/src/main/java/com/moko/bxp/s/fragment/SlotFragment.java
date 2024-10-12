package com.moko.bxp.s.fragment;

import static com.moko.support.s.entity.SlotAdvType.NO_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.SLOT1;
import static com.moko.support.s.entity.SlotAdvType.SLOT2;
import static com.moko.support.s.entity.SlotAdvType.SLOT3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.activity.SlotDataActivity;
import com.moko.bxp.s.activity.TriggerStep1Activity;
import com.moko.bxp.s.databinding.FragmentSlotBinding;
import com.moko.bxp.s.entity.TriggerEvent;
import com.moko.support.s.entity.SlotAdvType;

public class SlotFragment extends Fragment {
    private FragmentSlotBinding mBind;
    private TriggerEvent slot1TriggerEvent;
    private TriggerEvent slot2TriggerEvent;
    private TriggerEvent slot3TriggerEvent;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        return new SlotFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentSlotBinding.inflate(inflater, container, false);
        setListener();
        return mBind.getRoot();
    }

    private void setListener() {
        mBind.rlSlot1.setOnClickListener(v -> {
            if (slot1TriggerEvent.triggerType == NO_TRIGGER) {
                //没有配置触发功能
                toSlotDataActivity(SLOT1);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(SLOT1, slot1TriggerEvent);
            }
        });
        mBind.rlSlot2.setOnClickListener(v -> {
            if (slot2TriggerEvent.triggerType == NO_TRIGGER) {
                //没有配置触发功能
                toSlotDataActivity(SLOT2);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(SLOT2, slot2TriggerEvent);
            }
        });
        mBind.rlSlot3.setOnClickListener(v -> {
            if (slot3TriggerEvent.triggerType == NO_TRIGGER) {
                //没有配置触发功能
                toSlotDataActivity(SLOT3);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(SLOT3, slot3TriggerEvent);
            }
        });
    }

    private void toTriggerActivity(int slot, TriggerEvent triggerEvent) {
        Intent intent = new Intent(requireActivity(), TriggerStep1Activity.class);
        intent.putExtra("slot", slot);
        intent.putExtra("trigger", triggerEvent);
        startActivity(intent);
    }

    private void toSlotDataActivity(int slot) {
        Intent intent = new Intent(getActivity(), SlotDataActivity.class);
        intent.putExtra(AppConstants.SLOT, slot);
        startActivity(intent);
    }

    public void setSlotType(byte[] bytes) {
        mBind.tvSlot1.setText(SlotAdvType.getSlotAdvType(bytes[0] & 0xff));
        mBind.tvSlot2.setText(SlotAdvType.getSlotAdvType(bytes[1] & 0xff));
        mBind.tvSlot3.setText(SlotAdvType.getSlotAdvType(bytes[2] & 0xff));
    }

    public void setSlotTriggerType(int slot, TriggerEvent event) {
        if (slot == SLOT1) slot1TriggerEvent = event;
        else if (slot == SLOT2) slot2TriggerEvent = event;
        else if (slot == SLOT3) slot3TriggerEvent = event;
    }
}
