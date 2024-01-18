package com.moko.bxp.s.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.fragment.app.FragmentActivity;

import com.elvishew.xlog.XLog;
import com.moko.bxp.s.c.R;
import com.moko.bxp.s.c.databinding.FragmentAdvertisementBinding;
import com.moko.bxp.s.dialog.BottomDialog;

import java.util.ArrayList;
import java.util.Arrays;

public class AdvertisementFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private FragmentAdvertisementBinding mBind;
    private int mTxPower;
    private int mTriggerTxPower;
    private final String[] advChannelVal = {"2401", "2402", "2426", "2480", "2481"};
    private int mSelectedAdvChannel;
    private final String[] advIntervalVal = {"10", "20", "50", "100", "200", "250", "500", "1000", "2000", "5000", "10000", "20000", "50000"};
    private int mSelectedAdvInterval = -1;
    private int mSelectedTriggerAdvInterval = -1;
    private final String[] triggerTypeVal = {"Single click button", "Press button twice", "Press button three times"};
    private int mSelectTriggerType;
    private boolean isTrigger;
    private FragmentActivity activity;
    private int triggerAdvDuration;
    private final int[] txPowerArray = {-40, -30, -20, -16, -12, -8, -4, 0, 3, 4};

    public AdvertisementFragment() {
    }

    public static AdvertisementFragment newInstance() {
        return new AdvertisementFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentAdvertisementBinding.inflate(inflater, container, false);
        activity = (FragmentActivity) getActivity();
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        mBind.layoutTrigger.sbTriggerTxPower.setOnSeekBarChangeListener(this);
        mBind.ivTrigger.setOnClickListener(v -> {
            isTrigger = !isTrigger;
            mBind.ivTrigger.setImageResource(isTrigger ? R.drawable.ic_checked : R.drawable.ic_unchecked);
            mBind.layoutTrigger.getRoot().setVisibility(isTrigger ? View.VISIBLE : View.GONE);
            if (isTrigger && mSelectTriggerType == 0) {
                //本来是关闭的状态 现在打开了默认点击触发 2023.07.14和产品王鑫确认过这里
                mSelectTriggerType = 1;
            }
            mBind.layoutTrigger.tvTriggerType.setText(triggerTypeVal[mSelectTriggerType - 1]);
        });
        mBind.tvAdvChannel.setOnClickListener(v -> onAdvChannelClick());
        mBind.tvAdvInterval.setOnClickListener(v -> onAdvIntervalClick());
        mBind.layoutTrigger.tvTriggerType.setOnClickListener(v -> onTriggerTypeClick());
        mBind.layoutTrigger.tvTriggerAdvInterval.setOnClickListener(v -> onTriggerAdvIntervalClick());
        return mBind.getRoot();
    }

    public boolean isTrigger() {
        return isTrigger;
    }

    public int getSelectTriggerType() {
        return mSelectTriggerType;
    }

    public int getSelectedTriggerAdvInterval() {
        return Integer.parseInt(advIntervalVal[mSelectedTriggerAdvInterval]);
    }

    public int getSelectedAdvInterval() {
        return Integer.parseInt(advIntervalVal[mSelectedAdvInterval]);
    }

    public int getTxPower() {
        return mTxPower;
    }

    public int getTriggerTxPower() {
        return mTriggerTxPower;
    }

    public int getSelectedAdvChannel() {
        return mSelectedAdvChannel;
    }

    public int getAdvDuration() {
        return Integer.parseInt(mBind.etAdvDuration.getText().toString());
    }

    public int getStandbyTime() {
        return Integer.parseInt(mBind.etStandByDuration.getText().toString());
    }

    public int getTriggerAdvDuration() {
        return triggerAdvDuration;
    }

    public boolean isValid() {
        if (mSelectedAdvInterval == -1) return false;
        if (mSelectedTriggerAdvInterval == -1) return false;
        if (TextUtils.isEmpty(mBind.etAdvDuration.getText())) return false;
        int duration = Integer.parseInt(mBind.etAdvDuration.getText().toString());
        if (duration < 1 || duration > 65535) return false;
        if (TextUtils.isEmpty(mBind.etStandByDuration.getText())) return false;
        int standby = Integer.parseInt(mBind.etStandByDuration.getText().toString());
        if (standby > 65535) return false;
        if (isTrigger) {
            if (TextUtils.isEmpty(mBind.layoutTrigger.etTriggerAdvDuration.getText())) return false;
            int triggerDuration = Integer.parseInt(mBind.layoutTrigger.etTriggerAdvDuration.getText().toString());
            this.triggerAdvDuration = triggerDuration;
            return triggerDuration >= 1 && triggerDuration <= 65535;
        }
        return true;
    }

    /**
     * 触发广播间隔
     */
    private void onTriggerAdvIntervalClick() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(advIntervalVal)), mSelectedTriggerAdvInterval);
        dialog.setListener(value -> {
            mSelectedTriggerAdvInterval = value;
            mBind.layoutTrigger.tvTriggerAdvInterval.setText(advIntervalVal[value]);
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    /**
     * 触发类型
     */
    private void onTriggerTypeClick() {
        BottomDialog dialog = new BottomDialog();
        if (isTrigger && mSelectTriggerType == 0) {
            //本来是关闭的状态 现在打开了默认点击触发 2023.07.14和产品王鑫确认过这里
            mSelectTriggerType = 1;
        }
        dialog.setDatas(new ArrayList<>(Arrays.asList(triggerTypeVal)), mSelectTriggerType - 1);
        dialog.setListener(value -> {
            mSelectTriggerType = value + 1;
            mBind.layoutTrigger.tvTriggerType.setText(triggerTypeVal[value]);
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    /**
     * 广播间隔
     */
    private void onAdvIntervalClick() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(advIntervalVal)), mSelectedAdvInterval);
        dialog.setListener(value -> {
            mSelectedAdvInterval = value;
            mBind.tvAdvInterval.setText(advIntervalVal[value]);
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    /**
     * 切换信道
     */
    private void onAdvChannelClick() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(advChannelVal)), mSelectedAdvChannel);
        dialog.setListener(value -> {
            mSelectedAdvChannel = value;
            mBind.tvAdvChannel.setText(advChannelVal[value]);
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    public void setAdvChannel(int channel) {
        mSelectedAdvChannel = channel;
        mBind.tvAdvChannel.setText(advChannelVal[channel]);
    }

    public void setAdvInterval(int interval) {
        for (int i = 0; i < advIntervalVal.length; i++) {
            if (interval == Integer.parseInt(advIntervalVal[i])) {
                mSelectedAdvInterval = i;
                break;
            }
        }
        if (mSelectedAdvInterval != -1)
            mBind.tvAdvInterval.setText(advIntervalVal[mSelectedAdvInterval]);
    }

    public void setAdvDuration(int duration) {
        mBind.etAdvDuration.setText(String.valueOf(duration));
        mBind.etAdvDuration.setSelection(mBind.etAdvDuration.getText().length());
    }

    public void setStandByDuration(int duration) {
        mBind.etStandByDuration.setText(String.valueOf(duration));
        mBind.etStandByDuration.setSelection(mBind.etStandByDuration.getText().length());
    }

    public void updateAdvTxPower(int progress) {
        XLog.i("333333**********pro" + progress);
        mBind.sbTxPower.setProgress(getProgress(progress));
        mBind.tvTxPower.setText(String.format("%ddBm", progress));
        mTxPower = progress;
    }

    private int getProgress(int progress) {
        int index = 0;
        for (int i = 0; i < txPowerArray.length; i++) {
            if (progress == txPowerArray[i]) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void updateTriggerAdvTxPower(int progress) {
        mBind.layoutTrigger.sbTriggerTxPower.setProgress(getProgress(progress));
        mBind.layoutTrigger.tvTriggerTxPower.setText(String.format("%ddBm", progress));
        mTriggerTxPower = progress;
    }

    public void setTriggerData(int advInterval, int txPower, int advDuration, int triggerType) {
        mSelectTriggerType = triggerType;
        isTrigger = triggerType != 0;
        if (isTrigger) mBind.layoutTrigger.tvTriggerType.setText(triggerTypeVal[triggerType - 1]);
        if (!isTrigger) {
            mBind.ivTrigger.setImageResource(R.drawable.ic_unchecked);
            mBind.layoutTrigger.getRoot().setVisibility(View.GONE);
        } else {
            mBind.ivTrigger.setImageResource(R.drawable.ic_checked);
            mBind.layoutTrigger.getRoot().setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < advIntervalVal.length; i++) {
            if (advInterval == Integer.parseInt(advIntervalVal[i])) {
                mSelectedTriggerAdvInterval = i;
                break;
            }
        }
        if (mSelectedTriggerAdvInterval != -1) {
            mBind.layoutTrigger.tvTriggerAdvInterval.setText(advIntervalVal[mSelectedTriggerAdvInterval]);
        }
        updateTriggerAdvTxPower(txPower);
        mBind.layoutTrigger.etTriggerAdvDuration.setText(String.valueOf(advDuration));
        triggerAdvDuration = advDuration;
        mBind.layoutTrigger.etTriggerAdvDuration.setSelection(mBind.layoutTrigger.etTriggerAdvDuration.getText().length());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sb_tx_power) {
            mBind.tvTxPower.setText(String.format("%ddBm", txPowerArray[progress]));
            mTxPower = txPowerArray[progress];
        } else if (seekBar.getId() == R.id.sb_trigger_tx_power) {
            mBind.layoutTrigger.tvTriggerTxPower.setText(String.format("%ddBm", txPowerArray[progress]));
            mTriggerTxPower = txPowerArray[progress];
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
