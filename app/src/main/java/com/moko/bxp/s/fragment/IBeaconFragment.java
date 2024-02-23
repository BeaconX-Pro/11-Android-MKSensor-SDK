package com.moko.bxp.s.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
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
import com.moko.bxp.s.databinding.FragmentIbeaconBinding;
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

public class IBeaconFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "IBeaconFragment";
    private FragmentIbeaconBinding mBind;
    private boolean isLowPowerMode;
    private SlotData slotData;

    public IBeaconFragment() {
    }

    public static IBeaconFragment newInstance() {
        return new IBeaconFragment();
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
        mBind = FragmentIbeaconBinding.inflate(inflater, container, false);
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        //限制只输入大写，自动小写转大写
        mBind.etUuid.setTransformationMethod(new A2bigA());
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
            mBind.sbRssi.setProgress(41);
            mBind.sbTxPower.setProgress(5);
        } else {
            isLowPowerMode = slotData.standbyDuration != 0;
            mBind.etAdvInterval.setText(String.valueOf(slotData.advInterval/100));
            mBind.etAdvDuration.setText(String.valueOf(slotData.advDuration));
            if (isLowPowerMode) {
                mBind.etStandbyDuration.setText(String.valueOf(slotData.standbyDuration));
            }
            changeView();

            if (slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
                int rssiProgress = slotData.rssi_1m + 100;
                mBind.sbRssi.setProgress(rssiProgress);
                mRssi = slotData.rssi_1m;
                mBind.tvRssi.setText(String.format("%ddBm", mRssi));
            } else if (slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                mBind.sbRssi.setProgress(41);
                mRssi = -59;
                mBind.tvRssi.setText(String.format("%ddBm", mRssi));
            } else {
                int advTxPowerProgress = slotData.rssi_0m + 100;
                mBind.sbRssi.setProgress(advTxPowerProgress);
                mRssi = slotData.rssi_0m;
                mBind.tvRssi.setText(String.format("%ddBm", mRssi));
            }

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
        if (slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
            mBind.etMajor.setText(String.valueOf(Integer.parseInt(slotData.major, 16)));
            mBind.etMinor.setText(String.valueOf(Integer.parseInt(slotData.minor, 16)));
            mBind.etUuid.setText(slotData.iBeaconUUID.toUpperCase());
            mBind.etMajor.setSelection(mBind.etMajor.getText().toString().length());
            mBind.etMinor.setSelection(mBind.etMinor.getText().toString().length());
            mBind.etUuid.setSelection(mBind.etUuid.getText().toString().length());
        }
    }

    private int mAdvInterval;
    private int mAdvDuration;
    private int mStandbyDuration;
    private int mRssi;
    private int mTxPower;
    private int mMajor;
    private int mMinor;
    private String mUUIDHex;
    private final TriggerStep1Bean triggerStep1Bean = new TriggerStep1Bean();

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateData(seekBar.getId(), progress);
    }

    public TriggerStep1Bean getTriggerStep1Bean(){
        return triggerStep1Bean;
    }

    @SuppressLint("DefaultLocale")
    public void updateData(int viewId, int progress) {
        if (viewId == R.id.sb_rssi) {
            int rssi = progress - 100;
            mBind.tvRssi.setText(String.format("%ddBm", rssi));
            mRssi = rssi;
        } else if (viewId == R.id.sb_tx_power) {
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
        String majorStr = mBind.etMajor.getText().toString();
        String minorStr = mBind.etMinor.getText().toString();
        String uuidStr = mBind.etUuid.getText().toString();
        String advInterval = mBind.etAdvInterval.getText().toString();
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(majorStr) || TextUtils.isEmpty(minorStr) || TextUtils.isEmpty(uuidStr)) {
            ToastUtils.showToast(requireContext(), "Data format incorrect!");
            return false;
        }
        if (Integer.parseInt(majorStr) > 65535 || Integer.parseInt(minorStr) > 65535 || uuidStr.length() != 32) {
            ToastUtils.showToast(requireContext(), "Data format incorrect!");
            return false;
        }
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
        triggerStep1Bean.major = mMajor = Integer.parseInt(majorStr);
        triggerStep1Bean.minor = mMinor = Integer.parseInt(minorStr);
        triggerStep1Bean.uuid = mUUIDHex = uuidStr;
        triggerStep1Bean.advDuration = mAdvDuration;
        triggerStep1Bean.standByDuration = mStandbyDuration;
        triggerStep1Bean.rssi = mRssi;
        triggerStep1Bean.txPower = mTxPower;
        triggerStep1Bean.isLowPowerMode = isLowPowerMode;
        return true;
    }

    @Override
    public void sendData() {
        // 切换通道，保证通道是在当前设置通道里
        ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.setSlotAdvParamsBefore(slotData.slotEnum.ordinal(),
                mAdvInterval, mAdvDuration, mStandbyDuration, mRssi, mTxPower));
        orderTasks.add(OrderTaskAssembler.setSlotParamsIBeacon(slotData.slotEnum.ordinal(),
                mMajor, mMinor, mUUIDHex));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
    public void resetParams(SlotFrameTypeEnum currentFrameTypeEnum) {
        if (slotData.frameTypeEnum == currentFrameTypeEnum) {
            mBind.etAdvInterval.setText(String.valueOf(slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(slotData.standbyDuration));
            isLowPowerMode = slotData.standbyDuration != 0;
            changeView();

            int rssiProgress = slotData.rssi_1m + 100;
            mBind.sbRssi.setProgress(rssiProgress);

            int txPowerProgress;
            if (slotData.isC112) {
                txPowerProgress = Objects.requireNonNull(TxPowerEnumC112.fromTxPower(slotData.txPower)).ordinal();
            } else {
                txPowerProgress = Objects.requireNonNull(TxPowerEnum.fromTxPower(slotData.txPower)).ordinal();
            }
            mBind.sbTxPower.setProgress(txPowerProgress);

            mBind.etMajor.setText(Integer.parseInt(slotData.major, 16) + "");
            mBind.etMinor.setText(Integer.parseInt(slotData.minor, 16) + "");
            mBind.etUuid.setText(slotData.iBeaconUUID.toUpperCase());
            mBind.etMajor.setSelection(mBind.etMajor.getText().toString().length());
            mBind.etMinor.setSelection(mBind.etMinor.getText().toString().length());
            mBind.etUuid.setSelection(mBind.etUuid.getText().toString().length());
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("");
            mBind.sbRssi.setProgress(100);
            mBind.sbTxPower.setProgress(5);

            mBind.etMajor.setText("");
            mBind.etMinor.setText("");
            mBind.etUuid.setText("");
            isLowPowerMode = false;
            changeView();
        }
    }
}
