package com.moko.bxp.s.fragment;

import static com.moko.support.s.entity.SlotAdvType.NO_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.SLOT1;
import static com.moko.support.s.entity.SlotAdvType.SLOT2;
import static com.moko.support.s.entity.SlotAdvType.SLOT3;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.activity.DeviceInfoActivity;
import com.moko.bxp.s.activity.SlotDataActivity;
import com.moko.bxp.s.activity.TriggerStep1Activity;
import com.moko.bxp.s.databinding.FragmentSlotSBinding;
import com.moko.bxp.s.entity.TriggerEvent;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.entity.SlotAdvType;

public class SlotFragment extends BaseFragment<FragmentSlotSBinding> {
    private TriggerEvent slot1TriggerEvent;
    private TriggerEvent slot2TriggerEvent;
    private TriggerEvent slot3TriggerEvent;
    private boolean isButtonPowerEnable;
    private boolean isButtonReset;
    private int accStatus;
    private int thStatus;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        return new SlotFragment();
    }

    @Override
    protected void onCreateView() {
        setListener();
    }

    @Override
    protected FragmentSlotSBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSlotSBinding.inflate(inflater, container, false);
    }

    private void setListener() {
        mBind.rlSlot1.setOnClickListener(v -> {
            if (slot1TriggerEvent.triggerType == NO_TRIGGER) {
                //没有配置触发功能
                toSlotDataActivity(SLOT1);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(SLOT1, slot1TriggerEvent);
            }
        });
        mBind.rlSlot2.setOnClickListener(v -> {
            if (slot2TriggerEvent.triggerType == NO_TRIGGER) {
                //没有配置触发功能
                toSlotDataActivity(SLOT2);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(SLOT2, slot2TriggerEvent);
            }
        });
        mBind.rlSlot3.setOnClickListener(v -> {
            if (slot3TriggerEvent.triggerType == NO_TRIGGER) {
                //没有配置触发功能
                toSlotDataActivity(SLOT3);
            } else {
                //配置了触发功能 跳转配置触发功能引导页面
                toTriggerActivity(SLOT3, slot3TriggerEvent);
            }
        });
    }

    private void toTriggerActivity(int slot, TriggerEvent triggerEvent) {
        //先判断是否有传感器
        if (accStatus == 0 && thStatus == 0 && (isButtonReset || isButtonPowerEnable)) {
            ToastUtils.showToast(requireContext(), "当前没有传感器");
            return;
        }
        Intent intent = new Intent(requireActivity(), TriggerStep1Activity.class);
        intent.putExtra(AppConstants.SLOT, slot);
        intent.putExtra(AppConstants.EXTRA_KEY1, triggerEvent);
        intent.putExtra(AppConstants.EXTRA_KEY2, accStatus);
        intent.putExtra(AppConstants.EXTRA_KEY3, thStatus);
        intent.putExtra(AppConstants.EXTRA_KEY4, isButtonReset);
        intent.putExtra(AppConstants.EXTRA_KEY5, isButtonPowerEnable);
        launcher.launch(intent);
    }

    private void toSlotDataActivity(int slot) {
        Intent intent = new Intent(getActivity(), SlotDataActivity.class);
        intent.putExtra(AppConstants.SLOT, slot);
        intent.putExtra(AppConstants.EXTRA_KEY2, accStatus);
        intent.putExtra(AppConstants.EXTRA_KEY3, thStatus);
        intent.putExtra(AppConstants.EXTRA_KEY4, isButtonReset);
        intent.putExtra(AppConstants.EXTRA_KEY5, isButtonPowerEnable);
        launcher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (null == result) return;
        if (result.getResultCode() == Activity.RESULT_OK) {
            DeviceInfoActivity activity = (DeviceInfoActivity) requireActivity();
            if (!activity.isFinishing()) {
                activity.getSlotType();
            }
        }
    });

    public void setSlotType(byte[] bytes) {
        mBind.tvSlot1.setText(SlotAdvType.getSlotAdvType(bytes[0] & 0xff));
        mBind.tvSlot2.setText(SlotAdvType.getSlotAdvType(bytes[1] & 0xff));
        mBind.tvSlot3.setText(SlotAdvType.getSlotAdvType(bytes[2] & 0xff));
    }

    public void setSlotTriggerType(int slot, TriggerEvent event) {
        if (slot == SLOT1) slot1TriggerEvent = event;
        else if (slot == SLOT2) slot2TriggerEvent = event;
        else if (slot == SLOT3) slot3TriggerEvent = event;
    }

    public void setDeviceTypeValue(int accStatus, int thStatus, boolean isButtonPowerEnable, boolean isButtonReset) {
        this.accStatus = accStatus;
        this.thStatus = thStatus;
        this.isButtonPowerEnable = isButtonPowerEnable;
        this.isButtonReset = isButtonReset;
    }
}
