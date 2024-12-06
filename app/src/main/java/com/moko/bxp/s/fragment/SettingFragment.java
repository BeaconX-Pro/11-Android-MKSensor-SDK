package com.moko.bxp.s.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.ble.lib.task.OrderTask;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.activity.DeviceInfoActivity;
import com.moko.bxp.s.activity.RemoteReminderActivity;
import com.moko.bxp.s.activity.SensorConfigActivity;
import com.moko.bxp.s.databinding.FragmentSettingBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;
import com.moko.bxp.s.dialog.BottomDialog;
import com.moko.bxp.s.dialog.ModifyPasswordDialog;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingFragment extends BaseFragment<FragmentSettingBinding> {
    private final String[] advModeArray = {"Legacy", "Long Range"};
    private int mSelected;
    private boolean isButtonPowerEnable;
    private boolean isButtonResetEnable;
    private int accStatus = -1;
    private int thStatus;
    private final String[] batteryModeArray = {"Percentage", "Voltage"};
    private int batteryModeSelect;
    //01 02 04 07
    private final String[] advChannelArray = {"CH37", "CH38", "CH39", "CH37&CH38&CH39"};
    private int advChannelSelect;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    protected void onCreateView() {
        setListener();
    }

    @Override
    protected FragmentSettingBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSettingBinding.inflate(inflater, container, false);
    }

    private int getIndexByChannel(int channel) {
        if (channel == 1) return 0;
        if (channel == 2) return 1;
        if (channel == 4) return 2;
        return 3;
    }

    private int getChannelByIndex(int index) {
        if (index == 0) return 1;
        if (index == 1) return 2;
        if (index == 2) return 4;
        return 7;
    }

    private void setListener() {
        mBind.tvAdvMode.setOnClickListener(v -> onAdvModeClick());
        mBind.tvResetBeacon.setOnClickListener(v -> {
            if (null != getActivity()) {
                AlertMessageDialog resetDeviceDialog = new AlertMessageDialog();
                resetDeviceDialog.setTitle("Warning！");
                resetDeviceDialog.setMessage("Are you sure to reset the Beacon？");
                resetDeviceDialog.setConfirm("OK");
                resetDeviceDialog.setOnAlertConfirmListener(() -> {
                    ((DeviceInfoActivity) getActivity()).showSyncingProgressDialog();
                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetDevice());
                });
                resetDeviceDialog.show(getChildFragmentManager());
            }
        });
        mBind.tvModifyPwd.setOnClickListener(v -> {
            final ModifyPasswordDialog modifyPasswordDialog = new ModifyPasswordDialog();
            modifyPasswordDialog.setOnModifyPasswordClicked(new ModifyPasswordDialog.ModifyPasswordClickListener() {
                @Override
                public void onEnsureClicked(String password) {
                    DeviceInfoActivity activity = (DeviceInfoActivity) getActivity();
                    if (null != activity) {
                        activity.setModifyPassword(true);
                        activity.showSyncingProgressDialog();
                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setNewPassword(password));
                    }
                }

                @Override
                public void onPasswordNotMatch() {
                    AlertMessageDialog dialog = new AlertMessageDialog();
                    dialog.setMessage("Password do not match!\nPlease try again.");
                    dialog.setConfirm("OK");
                    dialog.setCancelGone();
                    dialog.show(getChildFragmentManager());
                }
            });
            modifyPasswordDialog.show(getChildFragmentManager());
        });
        mBind.tvRemoteMinder.setOnClickListener(v -> startActivity(new Intent(getActivity(), RemoteReminderActivity.class)));
        mBind.tvSensor.setOnClickListener(v -> {
            if (accStatus == -1) {
                List<OrderTask> orderTasks = new ArrayList<>(3);
                orderTasks.add(OrderTaskAssembler.getSensorType());
                orderTasks.add(OrderTaskAssembler.getButtonTurnOffEnable());
                orderTasks.add(OrderTaskAssembler.getResetByButtonEnable());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
                return;
            }
            Intent intent = new Intent(requireActivity(), SensorConfigActivity.class);
            intent.putExtra(AppConstants.EXTRA_KEY1, accStatus);
            intent.putExtra(AppConstants.EXTRA_KEY2, thStatus);
            intent.putExtra(AppConstants.EXTRA_KEY3, isButtonPowerEnable);
            intent.putExtra(AppConstants.EXTRA_KEY4, isButtonResetEnable);
            startActivity(intent);
        });
        mBind.tvBatteryAdvMode.setOnClickListener(v -> onBatteryModeClick());
        mBind.tvAdvChannel.setOnClickListener(v -> onAdvChannelClick());
        mBind.tvResetBattery.setOnClickListener(v -> resetBattery());
    }

    private void resetBattery() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning!");
        dialog.setMessage("*Please ensure you have replaced the new battery for this beacon before reset the Battery");
        dialog.setCancel("Cancel");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            if (null != getActivity()) {
                ((DeviceInfoActivity) getActivity()).showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetBatteryPercent());
            }
        });
        dialog.show(getChildFragmentManager());
    }

    private void onAdvChannelClick() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(advChannelArray)), advChannelSelect);
        dialog.setListener(value -> {
            advChannelSelect = value;
            mBind.tvAdvChannel.setText(advChannelArray[getIndexByChannel(value)]);
            if (null != getActivity()) {
                ((DeviceInfoActivity) getActivity()).showSyncingProgressDialog();
                List<OrderTask> orderTasks = new ArrayList<>(2);
                orderTasks.add(OrderTaskAssembler.setAdvChannel(getChannelByIndex(value)));
                orderTasks.add(OrderTaskAssembler.getAdvChannel());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
            }
        });
        dialog.show(getChildFragmentManager());
    }

    private void onBatteryModeClick() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(batteryModeArray)), batteryModeSelect);
        dialog.setListener(value -> {
            batteryModeSelect = value;
            if (null != getActivity()) {
                ((DeviceInfoActivity) getActivity()).showSyncingProgressDialog();
                List<OrderTask> orderTasks = new ArrayList<>(2);
                orderTasks.add(OrderTaskAssembler.setBatteryMode(value + 1));
                orderTasks.add(OrderTaskAssembler.getBatteryMode());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
            }
        });
        dialog.show(getChildFragmentManager());
    }

    private void onAdvModeClick() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(advModeArray)), mSelected);
        dialog.setListener(value -> {
            if (null != getActivity()) {
                mSelected = value;
                ((DeviceInfoActivity) getActivity()).showSyncingProgressDialog();
                List<OrderTask> orderTasks = new ArrayList<>(2);
                orderTasks.add(OrderTaskAssembler.setAdvMode(value + 1));
                orderTasks.add(OrderTaskAssembler.getAdvMode());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
            }
        });
        dialog.show(getChildFragmentManager());
    }

    public void setResetVisibility(boolean enablePasswordVerify) {
        mBind.tvResetBeacon.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setModifyPasswordShown(boolean enablePasswordVerify) {
        mBind.tvModifyPwd.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setAdvMode(int advMode) {
        mSelected = advMode - 1;
        mBind.tvAdvMode.setText(advModeArray[mSelected]);
    }

    public void setBatteryMode(int batteryMode) {
        this.batteryModeSelect = batteryMode - 1;
        mBind.tvBatteryAdvMode.setText(batteryModeArray[batteryModeSelect]);
    }

    public void setAdvChannel(int channel) {
        this.advChannelSelect = getIndexByChannel(channel);
        mBind.tvAdvChannel.setText(advChannelArray[advChannelSelect]);
    }

    public void setSensorGone() {
        mBind.tvSensor.setVisibility(View.GONE);
    }

    public void setDeviceTypeValue(int accStatus, int thStatus, boolean isHallPowerEnable, boolean isButtonResetEnable) {
        this.accStatus = accStatus;
        this.thStatus = thStatus;
        this.isButtonPowerEnable = isHallPowerEnable;
        this.isButtonResetEnable = isButtonResetEnable;
    }
}
