package com.moko.bxp.s.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moko.bxp.s.databinding.FragmentMotionTriggerBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 12:26
 * @des:
 */
public class MotionTriggerFragment extends Fragment {
    private FragmentMotionTriggerBinding mBind;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBind = FragmentMotionTriggerBinding.inflate(inflater, container, false);
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.group.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        return mBind.getRoot();
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
