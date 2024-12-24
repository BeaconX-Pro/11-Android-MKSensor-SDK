package com.moko.bxp.s.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.databinding.FragmentHallTriggerSBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 15:49
 * @des:
 */
public class HallTriggerFragment extends BaseFragment<FragmentHallTriggerSBinding> {
    private int lockedAdvDuration = -1;

    @Override
    protected void onCreateView() {
        if (lockedAdvDuration != -1) mBind.cbLockAdv.setChecked(lockedAdvDuration == 1);
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.tvTips.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    @Override
    protected FragmentHallTriggerSBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHallTriggerSBinding.inflate(inflater, container, false);
    }

    public void setValue(int lockedAdvDuration) {
        this.lockedAdvDuration = lockedAdvDuration;
        if (null != mBind) mBind.cbLockAdv.setChecked(lockedAdvDuration == 1);
    }

    public boolean lockedAdv(){
        return mBind.cbLockAdv.isChecked();
    }
}
