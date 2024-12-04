package com.moko.bxp.s.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.databinding.FragmentHumidityTriggerBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 16:00
 * @des:
 */
public class HumidityTriggerFragment extends BaseFragment<FragmentHumidityTriggerBinding> implements SeekBar.OnSeekBarChangeListener {
    @Override
    protected void onCreateView() {
        mBind.sbHum.setOnSeekBarChangeListener(this);
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.tvTips.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    @Override
    protected FragmentHumidityTriggerBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHumidityTriggerBinding.inflate(inflater, container, false);
    }

    public void setValue(int threshold, int lockedAdvDuration) {
        mBind.sbHum.setProgress(threshold);
        mBind.cbLockAdv.setChecked(lockedAdvDuration == 1);
    }

    public int getHumThreshold() {
        return mBind.sbHum.getProgress();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mBind.tvHumValue.setText(progress + "%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
