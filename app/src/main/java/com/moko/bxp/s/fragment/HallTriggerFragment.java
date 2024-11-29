package com.moko.bxp.s.fragment;

import android.text.TextUtils;
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
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.group.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    @Override
    protected FragmentHallTriggerBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHallTriggerBinding.inflate(inflater, container, false);
    }

    public void setValue(int lockedAdvDuration) {
        mBind.cbLockAdv.setChecked(lockedAdvDuration > 0);
        if (lockedAdvDuration > 0) {
            mBind.etLockDuration.setText(String.valueOf(lockedAdvDuration));
        }
    }

    public boolean isValid() {
        if (mBind.cbLockAdv.isChecked()) {
            if (TextUtils.isEmpty(mBind.etLockDuration.getText())) return false;
            int duration = Integer.parseInt(mBind.etLockDuration.getText().toString());
            return duration >= 1 && duration <= 65535;
        }
        return true;
    }

    public int getLockedDuration() {
        if (mBind.cbLockAdv.isChecked()) {
            return Integer.parseInt(mBind.etLockDuration.getText().toString());
        }
        return 0;
    }
}
