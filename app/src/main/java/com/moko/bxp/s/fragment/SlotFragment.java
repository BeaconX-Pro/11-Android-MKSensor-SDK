package com.moko.bxp.s.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.bxp.s.databinding.FragmentSlotBinding;
import com.moko.bxp.s.utils.SlotAdvType;

public class SlotFragment extends Fragment {
    private FragmentSlotBinding mBind;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        return new SlotFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentSlotBinding.inflate(inflater, container, false);
        return mBind.getRoot();
    }

    public void setSlot1(byte[] bytes, int afterTriggerType) {
        if (afterTriggerType != 0) {
            //有触发
            mBind.tvSlot1.setText(SlotAdvType.getSlotAdvType(bytes[0] & 0xff) + "/" + SlotAdvType.getSlotAdvType(bytes[3] & 0xff));
        }else {
            mBind.tvSlot1.setText(SlotAdvType.getSlotAdvType(bytes[0] & 0xff));
        }
    }

    public void setSlot2(byte[] bytes, int afterTriggerType) {
        if (afterTriggerType != 0) {
            //有触发
            mBind.tvSlot2.setText(SlotAdvType.getSlotAdvType(bytes[1] & 0xff) + "/" + SlotAdvType.getSlotAdvType(bytes[1] & 0xff));
        }else {
            mBind.tvSlot2.setText(SlotAdvType.getSlotAdvType(bytes[1] & 0xff));
        }
    }

    public void setSlot3(byte[] bytes, int afterTriggerType) {
        if (afterTriggerType != 0) {
            //有触发
            mBind.tvSlot3.setText(SlotAdvType.getSlotAdvType(bytes[2] & 0xff) + "/" + SlotAdvType.getSlotAdvType(bytes[5] & 0xff));
        }else {
            mBind.tvSlot3.setText(SlotAdvType.getSlotAdvType(bytes[2] & 0xff));
        }
    }


}
