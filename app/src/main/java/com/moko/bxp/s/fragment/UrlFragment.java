package com.moko.bxp.s.fragment;

import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER_MOTION;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER_STATIONARY;
import static com.moko.support.s.entity.SlotAdvType.NO_DATA;

import android.annotation.SuppressLint;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.FragmentUrlBinding;
import com.moko.bxp.s.dialog.UrlSchemeDialog;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.SlotData;
import com.moko.support.s.entity.TriggerStep1Bean;
import com.moko.support.s.entity.TxPowerEnum;
import com.moko.support.s.entity.UrlExpansionEnum;
import com.moko.support.s.entity.UrlSchemeEnum;

import java.util.Objects;

public class UrlFragment extends BaseFragment<FragmentUrlBinding> implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "UrlFragment";
    private final String FILTER_ASCII = "[!-~]*";
    private boolean isLowPowerMode;
    private SlotData slotData;
    private int mRssi;
    private int mTxPower;
    private int mUrlScheme;
    private boolean isTriggerAfter;
    private TriggerStep1Bean step1Bean;
    private int maxAdvDuration;

    public UrlFragment() {
    }

    public static UrlFragment newInstance() {
        return new UrlFragment();
    }

    @Override
    protected void onCreateView() {
        Log.i(TAG, "onCreateView: ");
        if (isTriggerAfter) {
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
            if (step1Bean.triggerType == MOTION_TRIGGER && step1Bean.triggerCondition == MOTION_TRIGGER_STATIONARY) {
                //不支持设置lowPowerMode功能
                isLowPowerMode = false;
                mBind.ivLowPowerMode.setEnabled(false);
                changeView();
            }
        }
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) return "";
            return null;
        };
        mBind.etUrl.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32), filter});
        mBind.ivLowPowerMode.setOnClickListener(v -> {
            isLowPowerMode = !isLowPowerMode;
            changeView();
        });
        mBind.ivDetail.setOnClickListener(v -> showLowPowerTips());
    }

    @Override
    protected FragmentUrlBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentUrlBinding.inflate(inflater, container, false);
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
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(mBind.etUrl.getText())) {
            ToastUtils.showToast(requireContext(), "Data format incorrect!");
            return false;
        }
        String urlContent = mBind.etUrl.getText().toString();
        if (TextUtils.isEmpty(mBind.etAdvInterval.getText())) {
            ToastUtils.showToast(requireContext(), "The Adv interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(mBind.etAdvInterval.getText().toString());
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(requireContext(), "The Adv interval range is 1~100");
            return false;
        }
        int mStandbyDuration = 0;
        int mAdvDuration;
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
        if (urlContent.contains(".")) {
            String urlExpansion = urlContent.substring(urlContent.lastIndexOf("."));
            UrlExpansionEnum urlExpansionEnum = UrlExpansionEnum.fromUrlExpanDesc(urlExpansion);
            if (urlExpansionEnum == null) {
                // url中有点，但不符合eddystone结尾格式，内容长度不能超过17个字符
                if (urlContent.length() < 2 || urlContent.length() > 17) {
                    ToastUtils.showToast(requireContext(), "Data format incorrect!");
                    return false;
                }
            } else {
                String content = urlContent.substring(0, urlContent.lastIndexOf("."));
                if (content.isEmpty() || content.length() > 16) {
                    ToastUtils.showToast(requireContext(), "Data format incorrect!");
                    return false;
                }
            }
        } else {
            // url中没有有点，内容长度不能超过17个字符
            if (urlContent.length() < 2 || urlContent.length() > 17) {
                ToastUtils.showToast(requireContext(), "Data format incorrect!");
                return false;
            }
        }
        slotData.urlContent = urlContent;
        slotData.advInterval = advIntervalInt;
        slotData.urlScheme = mUrlScheme;
        slotData.advDuration = mAdvDuration;
        slotData.standbyDuration = mStandbyDuration;
        slotData.rssi = mRssi;
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
        int rssiProgress = slotData.rssi + 100;
        mBind.sbRssi.setProgress(rssiProgress);
        int txPowerProgress = Objects.requireNonNull(TxPowerEnum.fromTxPower(slotData.txPower)).ordinal();
        mBind.sbTxPower.setProgress(txPowerProgress);

        mBind.tvUrlScheme.setText(Objects.requireNonNull(UrlSchemeEnum.fromUrlType(slotData.urlScheme)).getUrlDesc());
        mBind.etUrl.setText(slotData.urlContent);
    }

    @Override
    public SlotData getSlotData() {
        return slotData;
    }

    public void selectUrlScheme() {
        UrlSchemeDialog dialog = new UrlSchemeDialog(mBind.tvUrlScheme.getText().toString());
        dialog.setUrlSchemeClickListener(urlType -> {
            UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlType(Integer.parseInt(urlType));
            if (null == urlSchemeEnum) return;
            mBind.tvUrlScheme.setText(urlSchemeEnum.getUrlDesc());
            mUrlScheme = Integer.parseInt(urlType);
        });
        dialog.show(getChildFragmentManager());
    }
}
