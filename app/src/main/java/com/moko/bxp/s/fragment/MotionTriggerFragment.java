package com.moko.bxp.s.fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.databinding.FragmentMotionTriggerBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 12:26
 * @des:
 */
public class MotionTriggerFragment extends BaseFragment<FragmentMotionTriggerBinding> {
    @Override
    protected void onCreateView() {
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.group.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    @Override
    protected FragmentMotionTriggerBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMotionTriggerBinding.inflate(inflater, container, false);
    }

    public void setValue(int staticPeriod, int lockedAdvDuration) {
        if (staticPeriod > 0) {
            mBind.etStaticPeriod.setText(String.valueOf(staticPeriod));
        }
        mBind.cbLockAdv.setChecked(lockedAdvDuration > 0);
        if (lockedAdvDuration > 0) {
            mBind.etLockDuration.setText(String.valueOf(lockedAdvDuration));
        }
    }

    public int getPeriod() {
        return Integer.parseInt(mBind.etStaticPeriod.getText().toString());
    }

    public int getLockedDuration() {
        if (mBind.cbLockAdv.isChecked()) {
            return Integer.parseInt(mBind.etLockDuration.getText().toString());
        }
        return 0;
    }

    public boolean isValid() {
        if (TextUtils.isEmpty(mBind.etStaticPeriod.getText())) return false;
        int period = Integer.parseInt(mBind.etStaticPeriod.getText().toString());
        if (period < 1 || period > 65535) return false;
        if (mBind.cbLockAdv.isChecked()) {
            if (TextUtils.isEmpty(mBind.etLockDuration.getText())) return false;
            int duration = Integer.parseInt(mBind.etLockDuration.getText().toString());
            return duration >= 1 && duration <= 65535;
        }
        return true;
    }
}
