package com.moko.bxp.s.fragment;

import static com.moko.support.s.entity.SlotAdvType.NO_DATA;

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

import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.FragmentTlmBinding;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.SlotData;
import com.moko.support.s.entity.TxPowerEnum;

import java.util.Objects;

public class TlmFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "TlmFragment";
    private FragmentTlmBinding mBind;
    private boolean isLowPowerMode;
    private SlotData slotData;
    private int mTxPower;
    private boolean isTriggerAfter;

    public TlmFragment() {
    }

    public static TlmFragment newInstance() {
        return new TlmFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentTlmBinding.inflate(inflater, container, false);
        if (isTriggerAfter) {
            mBind.layoutLowPower.setVisibility(View.GONE);
            mBind.layoutStandDuration.setVisibility(View.GONE);
            mBind.etAdvDuration.setHint("0~65535");
        }
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        mBind.ivLowPowerMode.setOnClickListener(v -> {
            isLowPowerMode = !isLowPowerMode;
            changeView();
        });
        return mBind.getRoot();
    }

    public void setTriggerAfter(boolean isTriggerAfter) {
        this.isTriggerAfter = isTriggerAfter;
        if (isTriggerAfter && null != mBind) {
            mBind.layoutLowPower.setVisibility(View.GONE);
            mBind.layoutStandDuration.setVisibility(View.GONE);
            mBind.etAdvDuration.setHint("0~65535");
        }
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateData(seekBar.getId(), progress);
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
        if (TextUtils.isEmpty(mBind.etAdvInterval.getText())) {
            ToastUtils.showToast(requireContext(), "The Adv interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(mBind.etAdvInterval.getText().toString());
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(requireContext(), "The Adv interval range is 1~100");
            return false;
        }
        int mAdvDuration;
        int mStandbyDuration = 0;
        if (isLowPowerMode || isTriggerAfter) {
            if (TextUtils.isEmpty(mBind.etAdvDuration.getText())) {
                ToastUtils.showToast(requireContext(), "The Adv duration can not be empty.");
                return false;
            }
            int advDurationInt = Integer.parseInt(mBind.etAdvDuration.getText().toString());
            if (isTriggerAfter) {
                if (advDurationInt > 65535) {
                    ToastUtils.showToast(requireContext(), "The Adv duration range is 0~65535");
                    return false;
                }
            } else {
                if (advDurationInt < 1 || advDurationInt > 65535) {
                    ToastUtils.showToast(requireContext(), "The Adv duration range is 1~65535");
                    return false;
                }
            }
            mAdvDuration = advDurationInt;
            if (!isTriggerAfter) {
                if (TextUtils.isEmpty(mBind.etStandbyDuration.getText())) {
                    ToastUtils.showToast(requireContext(), "The Standby duration can not be empty.");
                    return false;
                }
                int standbyDurationInt = Integer.parseInt(mBind.etStandbyDuration.getText().toString());
                if (standbyDurationInt > 65535 || standbyDurationInt < 1) {
                    ToastUtils.showToast(requireContext(), "The Standby duration range is 1~65535");
                    return false;
                }
                mStandbyDuration = standbyDurationInt;
            }
        } else {
            mAdvDuration = 10;
        }
        slotData.advInterval = advIntervalInt;
        slotData.advDuration = mAdvDuration;
        slotData.standbyDuration = mStandbyDuration;
        slotData.txPower = mTxPower;
        return true;
    }

    @Override
    public void sendData() {
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setNormalSlotAdvParams(slotData));
    }

    @Override
    public void setParams(@NonNull SlotData slotData) {
        this.slotData = slotData;
        if (slotData.currentFrameType == NO_DATA) return;
        mBind.etAdvInterval.setText(String.valueOf(slotData.advInterval / 100));
        mBind.etAdvDuration.setText(String.valueOf(slotData.advDuration));
        if (!isTriggerAfter) {
            if (slotData.standbyDuration > 0) {
                mBind.etStandbyDuration.setText(String.valueOf(slotData.standbyDuration));
            }
            isLowPowerMode = slotData.standbyDuration != 0;
            changeView();
        }
        int txPowerProgress = Objects.requireNonNull(TxPowerEnum.fromTxPower(slotData.txPower)).ordinal();
        mBind.sbTxPower.setProgress(txPowerProgress);
    }

    @Override
    public SlotData getSlotData() {
        return slotData;
    }
}
