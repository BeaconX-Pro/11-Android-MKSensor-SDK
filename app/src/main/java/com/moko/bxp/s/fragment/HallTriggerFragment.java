package com.moko.bxp.s.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.databinding.FragmentHallTriggerBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 15:49
 * @des:
 */
public class HallTriggerFragment extends BaseFragment<FragmentHallTriggerBinding> {
    @Override
    protected void onCreateView() {
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.tvTips.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    @Override
    protected FragmentHallTriggerBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHallTriggerBinding.inflate(inflater, container, false);
    }

    public void setValue(int lockedAdvDuration) {
        mBind.cbLockAdv.setChecked(lockedAdvDuration == 1);
    }
}
