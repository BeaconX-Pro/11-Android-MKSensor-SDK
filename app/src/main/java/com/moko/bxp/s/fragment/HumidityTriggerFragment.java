package com.moko.bxp.s.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.databinding.FragmentHumidityTriggerSBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 16:00
 * @des:
 */
public class HumidityTriggerFragment extends BaseFragment<FragmentHumidityTriggerSBinding> implements SeekBar.OnSeekBarChangeListener {
    private int threshold;
    private int lockedAdvDuration;

    @Override
    protected void onCreateView() {
        mBind.sbHum.setProgress(threshold);
        mBind.cbLockAdv.setChecked(lockedAdvDuration == 1);
        mBind.sbHum.setOnSeekBarChangeListener(this);
        mBind.cbLockAdv.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.tvTips.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    @Override
    protected FragmentHumidityTriggerSBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHumidityTriggerSBinding.inflate(inflater, container, false);
    }

    public void setValue(int threshold, int lockedAdvDuration) {
        this.threshold = threshold;
        this.lockedAdvDuration = lockedAdvDuration;
        if (null == mBind) return;
        mBind.sbHum.setProgress(threshold);
        mBind.cbLockAdv.setChecked(lockedAdvDuration == 1);
    }

    public boolean lockedAdv(){
        return mBind.cbLockAdv.isChecked();
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
