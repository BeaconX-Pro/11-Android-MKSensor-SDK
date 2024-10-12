package com.moko.bxp.s.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moko.bxp.s.databinding.FragmentTemperatureTriggerBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 11:47
 * @des:
 */
public class TemperatureTriggerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private FragmentTemperatureTriggerBinding mBind;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBind = FragmentTemperatureTriggerBinding.inflate(inflater, container, false);
        mBind.sbTemp.setOnSeekBarChangeListener(this);
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.group.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        return mBind.getRoot();
    }

    public void setValues(int threshold, int lockedAdvDuration) {
        mBind.tvTempValue.setText(threshold + "℃");
        mBind.sbTemp.setProgress(threshold + 64);
        mBind.cbLockAdv.setChecked(lockedAdvDuration > 0);
        if (lockedAdvDuration > 0) {
            mBind.etLockDuration.setText(String.valueOf(lockedAdvDuration));
            mBind.etLockDuration.setSelection(mBind.etLockDuration.getText().length());
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

    public int getTempThreshold() {
        return mBind.sbTemp.getProgress() - 64;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mBind.tvTempValue.setText((progress - 64) + "℃");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
