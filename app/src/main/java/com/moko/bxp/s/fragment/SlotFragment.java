package com.moko.bxp.s.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.bxp.s.databinding.FragmentSlotBinding;

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



}
