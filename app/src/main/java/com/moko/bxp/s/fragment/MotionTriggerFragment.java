package com.moko.bxp.s.fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.databinding.FragmentMotionTriggerBinding;

/**
 * @author: jun.liu
 * @date: 2024/9/27 12:26
 * @des:
 */
public class MotionTriggerFragment extends BaseFragment<FragmentMotionTriggerBinding> {

    @Override
    protected FragmentMotionTriggerBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMotionTriggerBinding.inflate(inflater, container, false);
    }

    public void setValue(int staticPeriod) {
        if (staticPeriod > 0) {
            mBind.etStaticPeriod.setText(String.valueOf(staticPeriod));
        }
    }

    public void setIsStartMove(boolean isStartMove) {
        if (isStartMove) {
            mBind.tvStaticTip.setText("*Static verify period: the parameter that determines when a stationary event occurs on the device.\n\n* If Trigger event is \"Device start moving\", The duration for determining stillness dictates the broadcast duration after a motion-triggered event. The broadcast duration following a single motion trigger will always be shorter than the stillness determination duration.");
        } else {
            mBind.tvStaticTip.setText("*Static verify period: the parameter that determines when a stationary event occurs on the device");
        }
    }

    public int getPeriod() {
        return Integer.parseInt(mBind.etStaticPeriod.getText().toString());
    }

    public boolean isValid() {
        if (TextUtils.isEmpty(mBind.etStaticPeriod.getText())) return false;
        int period = Integer.parseInt(mBind.etStaticPeriod.getText().toString());
        return period >= 1 && period <= 65535;
    }
}
