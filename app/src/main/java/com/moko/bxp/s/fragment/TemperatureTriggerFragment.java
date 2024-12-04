package com.moko.bxp.s.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.databinding.FragmentTemperatureTriggerBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 11:47
 * @des:
 */
public class TemperatureTriggerFragment extends BaseFragment<FragmentTemperatureTriggerBinding> implements SeekBar.OnSeekBarChangeListener {
    @Override
    protected void onCreateView() {
        mBind.sbTemp.setOnSeekBarChangeListener(this);
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.tvTips.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    @Override
    protected FragmentTemperatureTriggerBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentTemperatureTriggerBinding.inflate(inflater, container, false);
    }

    public void setValues(int threshold, int lockedAdvDuration) {
        mBind.tvTempValue.setText(threshold + "℃");
        mBind.sbTemp.setProgress(threshold + 64);
        mBind.cbLockAdv.setChecked(lockedAdvDuration == 1);
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
