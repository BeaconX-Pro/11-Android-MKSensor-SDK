package com.moko.bxp.s.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.ble.lib.task.OrderTask;
import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.FragmentTlmBinding;
import com.moko.bxp.s.entity.SlotData;
import com.moko.bxp.s.entity.TriggerStep1Bean;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.SlotFrameTypeEnum;
import com.moko.support.s.entity.TxPowerEnum;
import com.moko.support.s.entity.TxPowerEnumC112;

import java.util.ArrayList;
import java.util.Objects;

public class TlmFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "TlmFragment";
    private FragmentTlmBinding mBind;
    private boolean isLowPowerMode;
    private SlotData slotData;

    public TlmFragment() {
    }

    public static TlmFragment newInstance() {
        return new TlmFragment();
    }

    public void setSlotData(SlotData slotData) {
        this.slotData = slotData;
        if (slotData.isC112 && null != mBind) {
            mBind.sbTxPower.setMax(5);
            mBind.tvTxPowerTips.setText("(-20, -16, -12, -8, -4, 0)");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentTlmBinding.inflate(inflater, container, false);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        setDefault();
        mBind.ivLowPowerMode.setOnClickListener(v -> {
            isLowPowerMode = !isLowPowerMode;
            changeView();
        });
        if (slotData.isC112) {
            mBind.sbTxPower.setMax(5);
            mBind.tvTxPowerTips.setText("(-20, -16, -12, -8, -4, 0)");
        }
        return mBind.getRoot();
    }

    private void changeView() {
        mBind.ivLowPowerMode.setImageResource(isLowPowerMode ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        if (isLowPowerMode) {
            mBind.layoutAdvDuration.setVisibility(View.VISIBLE);
            mBind.layoutStandDuration.setVisibility(View.VISIBLE);
        } else {
            mBind.layoutAdvDuration.setVisibility(View.GONE);
            mBind.layoutStandDuration.setVisibility(View.GONE);
        }
    }

    @SuppressLint("DefaultLocale")
    private void setDefault() {
        if (slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbTxPower.setProgress(5);
        } else {
            isLowPowerMode = slotData.standbyDuration != 0;
            mBind.etAdvInterval.setText(String.valueOf(slotData.advInterval/100));
            mBind.etAdvDuration.setText(String.valueOf(slotData.advDuration));
            if (isLowPowerMode) {
                mBind.etStandbyDuration.setText(String.valueOf(slotData.standbyDuration));
            }
            changeView();
            int txPowerProgress;
            if (slotData.isC112) {
                txPowerProgress = Objects.requireNonNull(TxPowerEnumC112.fromTxPower(slotData.txPower)).ordinal();
            } else {
                txPowerProgress = Objects.requireNonNull(TxPowerEnum.fromTxPower(slotData.txPower)).ordinal();
            }
            mBind.sbTxPower.setProgress(txPowerProgress);
            mTxPower = slotData.txPower;
            mBind.tvTxPower.setText(String.format("%ddBm", mTxPower));
        }
    }

    private int mAdvInterval;
    private int mAdvDuration;
    private int mStandbyDuration;
    private int mTxPower;
    private final TriggerStep1Bean triggerStep1Bean = new TriggerStep1Bean();

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateData(seekBar.getId(), progress);
    }

    public TriggerStep1Bean getTriggerStep1Bean(){
        return triggerStep1Bean;
    }

    @SuppressLint("DefaultLocale")
    private void updateData(int viewId, int progress) {
        if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
            if (null == txPowerEnum) return;
            int txPower = txPowerEnum.getTxPower();
            mBind.tvTxPower.setText(String.format("%ddBm", txPower));
            mTxPower = txPower;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean isValid() {
        String advInterval = mBind.etAdvInterval.getText().toString();
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(advInterval)) {
            ToastUtils.showToast(requireContext(), "The Adv interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(advInterval);
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(requireContext(), "The Adv interval range is 1~100");
            return false;
        }
        if (isLowPowerMode) {
            if (TextUtils.isEmpty(advDuration)) {
                ToastUtils.showToast(requireContext(), "The Adv duration can not be empty.");
                return false;
            }
            int advDurationInt = Integer.parseInt(advDuration);
            if (advDurationInt < 1 || advDurationInt > 65535) {
                ToastUtils.showToast(requireContext(), "The Adv duration range is 1~65535");
                return false;
            }
            mAdvDuration = advDurationInt;
            if (TextUtils.isEmpty(standbyDuration)) {
                ToastUtils.showToast(requireContext(), "The Standby duration can not be empty.");
                return false;
            }
            int standbyDurationInt = Integer.parseInt(standbyDuration);
            if (standbyDurationInt > 65535 || standbyDurationInt < 1) {
                ToastUtils.showToast(requireContext(), "The Standby duration range is 1~65535");
                return false;
            }
            mStandbyDuration = standbyDurationInt;
        } else {
            mAdvDuration = 10;
            mStandbyDuration = 0;
        }
        triggerStep1Bean.advInterval = mAdvInterval = advIntervalInt;
        triggerStep1Bean.advDuration = mAdvDuration;
        triggerStep1Bean.standByDuration = mStandbyDuration;
        triggerStep1Bean.rssi = slotData.rssi_0m;
        triggerStep1Bean.txPower = mTxPower;
        triggerStep1Bean.isLowPowerMode = isLowPowerMode;
        return true;
    }

    @Override
    public void sendData() {
        ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.setSlotAdvParamsBefore(slotData.slotEnum.ordinal(),
                mAdvInterval, mAdvDuration, mStandbyDuration, slotData.rssi_0m, mTxPower));
        orderTasks.add(OrderTaskAssembler.setSlotParamsTLM(slotData.slotEnum.ordinal()));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    public void resetParams(SlotFrameTypeEnum currentFrameTypeEnum) {
        if (slotData.frameTypeEnum == currentFrameTypeEnum) {
            mBind.etAdvInterval.setText(String.valueOf(slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(slotData.standbyDuration));
            isLowPowerMode = slotData.standbyDuration != 0;
            changeView();
            int txPowerProgress;
            if (slotData.isC112) {
                txPowerProgress = Objects.requireNonNull(TxPowerEnumC112.fromTxPower(slotData.txPower)).ordinal();
            } else {
                txPowerProgress = Objects.requireNonNull(TxPowerEnum.fromTxPower(slotData.txPower)).ordinal();
            }
            mBind.sbTxPower.setProgress(txPowerProgress);
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("");
            mBind.sbTxPower.setProgress(5);
            isLowPowerMode = false;
            changeView();
        }
    }
}
