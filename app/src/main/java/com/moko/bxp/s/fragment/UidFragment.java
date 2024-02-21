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
import com.moko.bxp.s.databinding.FragmentUidBinding;
import com.moko.bxp.s.entity.SlotData;
import com.moko.bxp.s.entity.TriggerStep1Bean;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.SlotFrameTypeEnum;
import com.moko.support.s.entity.TxPowerEnum;
import com.moko.support.s.entity.TxPowerEnumC112;

import java.util.ArrayList;

public class UidFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "UidFragment";
    private FragmentUidBinding mBind;
    private boolean isLowPowerMode;
    private SlotData slotData;

    public UidFragment() {
    }

    public static UidFragment newInstance() {
        return new UidFragment();
    }

    public void setSlotData(SlotData slotData) {
        this.slotData = slotData;
        if (slotData.isC112) {
            mBind.sbTxPower.setMax(5);
            mBind.tvTxPowerTips.setText("(-20, -16, -12, -8, -4, 0)");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentUidBinding.inflate(inflater, container, false);
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        mBind.etNamespace.setTransformationMethod(new A2bigA());
        mBind.etInstanceId.setTransformationMethod(new A2bigA());
        setDefault();
        mBind.ivLowPowerMode.setOnClickListener(v -> {
            isLowPowerMode = !isLowPowerMode;
            changeView();
        });
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
            mBind.sbRssi.setProgress(100);
            mBind.sbTxPower.setProgress(5);
        } else {
            mBind.etAdvInterval.setText(String.valueOf(slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(slotData.standbyDuration));
            isLowPowerMode = slotData.standbyDuration != 0;
            changeView();
            if (slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                mBind.sbRssi.setProgress(100);
                mRssi = 0;
                mBind.tvRssi.setText(String.format("%ddBm", mRssi));
            } else {
                int advTxPowerProgress = slotData.rssi_0m + 100;
                mBind.sbRssi.setProgress(advTxPowerProgress);
                mRssi = slotData.rssi_0m;
                mBind.tvRssi.setText(String.format("%ddBm", mRssi));
            }

            int txPowerProgress;
            if (slotData.isC112) {
                txPowerProgress = TxPowerEnumC112.fromTxPower(slotData.txPower).ordinal();
            } else {
                txPowerProgress = TxPowerEnum.fromTxPower(slotData.txPower).ordinal();
            }
            mBind.sbTxPower.setProgress(txPowerProgress);
            mTxPower = slotData.txPower;
            mBind.tvTxPower.setText(String.format("%ddBm", mTxPower));
        }
        if (slotData.frameTypeEnum == SlotFrameTypeEnum.UID) {
            mBind.etNamespace.setText(slotData.namespace);
            mBind.etInstanceId.setText(slotData.instanceId);
            mBind.etNamespace.setSelection(mBind.etNamespace.getText().toString().length());
            mBind.etInstanceId.setSelection(mBind.etInstanceId.getText().toString().length());
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private int mAdvInterval;
    private int mAdvDuration;
    private int mStandbyDuration;
    private int mRssi;
    private int mTxPower;
    private String mNamespaceIdHex;
    private String mInstanceIdHex;
    private final TriggerStep1Bean triggerStep1Bean = new TriggerStep1Bean();

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateData(seekBar.getId(), progress);
    }

    @SuppressLint("DefaultLocale")
    public void updateData(int viewId, int progress) {
        if (viewId == R.id.sb_rssi) {
            int rssi = progress - 100;
            mBind.tvRssi.setText(String.format("%ddBm", rssi));
            mRssi = rssi;
        } else if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
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

    public TriggerStep1Bean getTriggerStep1Bean(){
        return triggerStep1Bean;
    }

    @Override
    public boolean isValid() {
        String namespace = mBind.etNamespace.getText().toString();
        String instanceId = mBind.etInstanceId.getText().toString();
        String advInterval = mBind.etAdvInterval.getText().toString();
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(namespace) || TextUtils.isEmpty(instanceId)) {
            ToastUtils.showToast(requireContext(), "Data format incorrect!");
            return false;
        }
        if (namespace.length() != 20 || instanceId.length() != 12) {
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
        triggerStep1Bean.advDuration = mAdvDuration;
        triggerStep1Bean.standByDuration = mStandbyDuration;
        triggerStep1Bean.rssi = mRssi;
        triggerStep1Bean.txPower = mTxPower;
        triggerStep1Bean.advInterval = mAdvInterval = advIntervalInt;
        triggerStep1Bean.namespaceId = mNamespaceIdHex = namespace;
        triggerStep1Bean.instanceId = mInstanceIdHex = instanceId;
        triggerStep1Bean.isLowPowerMode = isLowPowerMode;
        return true;
    }

    @Override
    public void sendData() {
        ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.setSlotAdvParamsBefore(slotData.slotEnum.ordinal(),
                mAdvInterval, mAdvDuration, mStandbyDuration, mRssi, mTxPower));
        orderTasks.add(OrderTaskAssembler.setSlotParamsUID(slotData.slotEnum.ordinal(),
                mNamespaceIdHex, mInstanceIdHex));
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
            int rssiProgress = slotData.rssi_0m + 100;
            mBind.sbRssi.setProgress(rssiProgress);

            int txPowerProgress;
            if (slotData.isC112) {
                txPowerProgress = TxPowerEnumC112.fromTxPower(slotData.txPower).ordinal();
            } else {
                txPowerProgress = TxPowerEnum.fromTxPower(slotData.txPower).ordinal();
            }
            mBind.sbTxPower.setProgress(txPowerProgress);
            mBind.etNamespace.setText(slotData.namespace);
            mBind.etInstanceId.setText(slotData.instanceId);
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbRssi.setProgress(100);
            mBind.sbTxPower.setProgress(5);
            mBind.etNamespace.setText("");
            mBind.etInstanceId.setText("");
            isLowPowerMode = false;
            changeView();
        }
    }
}
