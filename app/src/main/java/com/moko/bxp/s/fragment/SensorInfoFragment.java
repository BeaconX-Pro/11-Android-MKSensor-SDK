package com.moko.bxp.s.fragment;

import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER_MOTION;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER_STATIONARY;
import static com.moko.support.s.entity.SlotAdvType.NO_DATA;

import android.annotation.SuppressLint;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.FragmentSensorInfoSBinding;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.SlotData;
import com.moko.support.s.entity.TriggerStep1Bean;
import com.moko.support.s.entity.TxPowerEnum;

import java.util.Objects;

public class SensorInfoFragment extends BaseFragment<FragmentSensorInfoSBinding> implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private final String FILTER_ASCII = "[ -~]*";
    private boolean isLowPowerMode;
    private SlotData slotData;
    private boolean isTriggerAfter;
    private TriggerStep1Bean step1Bean;
    private int maxAdvDuration;

    public SensorInfoFragment() {
    }

    public static SensorInfoFragment newInstance() {
        return new SensorInfoFragment();
    }

    @Override
    protected void onCreateView() {
        if (isTriggerAfter && null != step1Bean) {
            mBind.layoutLowPower.setVisibility(View.GONE);
            mBind.layoutStandDuration.setVisibility(View.GONE);
            mBind.advDuration.setText("Total adv duration");
            if (step1Bean.triggerType == MOTION_TRIGGER && step1Bean.triggerCondition == MOTION_TRIGGER_MOTION) {
                mBind.etAdvDuration.setHint("0~" + step1Bean.axisStaticPeriod);
                maxAdvDuration = step1Bean.axisStaticPeriod;
            } else {
                mBind.etAdvDuration.setHint("0~65535");
                maxAdvDuration = 65535;
            }
        } else {
            if (null != step1Bean && step1Bean.triggerType == MOTION_TRIGGER && step1Bean.triggerCondition == MOTION_TRIGGER_STATIONARY) {
                //不支持设置lowPowerMode功能
                isLowPowerMode = false;
                mBind.ivLowPowerMode.setEnabled(false);
                changeView();
            }
        }
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        //限制只输入大写，自动小写转大写
        mBind.etTagId.setTransformationMethod(new A2bigA());
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) return "";
            return null;
        };
        mBind.etDeviceName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20), filter});
        mBind.ivLowPowerMode.setOnClickListener(v -> {
            isLowPowerMode = !isLowPowerMode;
            changeView();
        });
        mBind.ivDetail.setOnClickListener(v -> showLowPowerTips());
    }

    @Override
    protected FragmentSensorInfoSBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSensorInfoSBinding.inflate(inflater, container, false);
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

    public void setTriggerAfter(boolean isTriggerAfter, TriggerStep1Bean step1Bean) {
        this.isTriggerAfter = isTriggerAfter;
        this.step1Bean = step1Bean;
        if (isTriggerAfter && null != mBind) {
            mBind.layoutLowPower.setVisibility(View.GONE);
            mBind.layoutStandDuration.setVisibility(View.GONE);
            mBind.advDuration.setText("Total adv duration");
            if (step1Bean.triggerType == MOTION_TRIGGER && step1Bean.triggerCondition == MOTION_TRIGGER_MOTION) {
                mBind.etAdvDuration.setHint("0~" + step1Bean.axisStaticPeriod);
                maxAdvDuration = step1Bean.axisStaticPeriod;
            } else {
                mBind.etAdvDuration.setHint("0~65535");
                maxAdvDuration = 65535;
            }
        } else if (!isTriggerAfter && null != mBind) {
            if (step1Bean.triggerType == MOTION_TRIGGER && step1Bean.triggerCondition == MOTION_TRIGGER_STATIONARY) {
                //不支持设置lowPowerMode功能
                isLowPowerMode = false;
                mBind.ivLowPowerMode.setEnabled(false);
                changeView();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateData(seekBar.getId(), progress);
    }

    @SuppressLint("DefaultLocale")
    public void updateData(int viewId, int progress) {
        if (viewId == R.id.sb_rssi) {
            int rssi = progress - 100;
            mBind.tvRssi.setText(String.format("%ddBm", rssi));
            slotData.rssi = rssi;
        } else if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
            if (null == txPowerEnum) return;
            int txPower = txPowerEnum.getTxPower();
            mBind.tvTxPower.setText(String.format("%ddBm", txPower));
            slotData.txPower = txPower;
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
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(mBind.etDeviceName.getText())) {
            ToastUtils.showToast(requireContext(), "Data format incorrect!");
            return false;
        }
        if (TextUtils.isEmpty(mBind.etTagId.getText()) || mBind.etTagId.getText().length() % 2 != 0) {
            ToastUtils.showToast(requireContext(), "Data format incorrect!");
            return false;
        }
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
            if (TextUtils.isEmpty(advDuration)) {
                ToastUtils.showToast(requireContext(), "The Adv duration can not be empty.");
                return false;
            }
            int advDurationInt = Integer.parseInt(advDuration);
            if (isTriggerAfter) {
                if (advDurationInt > maxAdvDuration) {
                    ToastUtils.showToast(requireContext(), "The Adv duration range is 0~" + maxAdvDuration);
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
            }
        } else {
            mAdvDuration = 10;
        }
        slotData.deviceName = mBind.etDeviceName.getText().toString();
        slotData.tagId = mBind.etTagId.getText().toString();
        slotData.advInterval = advIntervalInt * 100;
        slotData.advDuration = mAdvDuration;
        slotData.standbyDuration = mStandbyDuration;
        return true;
    }

    @Override
    public void sendData() {
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setNormalSlotAdvParams(slotData));
    }

    public static class A2bigA extends ReplacementTransformationMethod {
        @Override
        protected char[] getOriginal() {
            return new char[]{'a', 'b', 'c', 'd', 'e', 'f'};
        }

        @Override
        protected char[] getReplacement() {
            return new char[]{'A', 'B', 'C', 'D', 'E', 'F'};
        }
    }

    @Override
    public void setParams(@NonNull SlotData slotData) {
        this.slotData = slotData;
        if (slotData.step1TriggerType != MOTION_TRIGGER || slotData.realType != NO_DATA) {
            mBind.etAdvDuration.setText(String.valueOf(slotData.advDuration));
        }
        mBind.etAdvInterval.setText(String.valueOf(slotData.advInterval / 100));
        if (!isTriggerAfter) {
            if (slotData.standbyDuration > 0) {
                mBind.etStandbyDuration.setText(String.valueOf(slotData.standbyDuration));
            }
            isLowPowerMode = slotData.standbyDuration != 0;
            changeView();
        }
        int rssiProgress = slotData.rssi + 100;
        mBind.sbRssi.setProgress(rssiProgress);
        mBind.tvRssi.setText(slotData.rssi + "dBm");
        int txPowerProgress = Objects.requireNonNull(TxPowerEnum.fromTxPower(slotData.txPower)).ordinal();
        mBind.sbTxPower.setProgress(txPowerProgress);
        mBind.tvTxPower.setText(TxPowerEnum.fromTxPower(slotData.txPower).getTxPower() + "dBm");
        mBind.etDeviceName.setText(slotData.deviceName);
        mBind.etTagId.setText(slotData.tagId);
    }

    @Override
    public SlotData getSlotData() {
        return slotData;
    }
}
