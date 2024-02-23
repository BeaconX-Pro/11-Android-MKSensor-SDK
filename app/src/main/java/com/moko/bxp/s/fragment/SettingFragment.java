package com.moko.bxp.s.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.ble.lib.task.OrderTask;
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

public class SettingFragment extends Fragment {
    private FragmentSettingBinding mBind;
    private final String[] advModeArray = {"Legacy", "Long Range"};
    private int mSelected;
    private byte[] deviceTypeBytes;
    private boolean isHallPowerEnable;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentSettingBinding.inflate(inflater, container, false);
        setListener();
        return mBind.getRoot();
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
            if (null == deviceTypeBytes) {
                List<OrderTask> orderTasks = new ArrayList<>(2);
                orderTasks.add(OrderTaskAssembler.getSensorType());
                orderTasks.add(OrderTaskAssembler.getHallPowerEnable());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
                return;
            }
            Intent intent = new Intent(getActivity(), SensorConfigActivity.class);
            intent.putExtra("deviceTypeBytes", deviceTypeBytes);
            intent.putExtra("hallEnable", isHallPowerEnable);
            startActivity(intent);
        });
    }

    private void onAdvModeClick() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(advModeArray)), mSelected);
        dialog.setListener(value -> {
            if (null != getActivity()) {
                ((DeviceInfoActivity) getActivity()).showSyncingProgressDialog();
                List<OrderTask> orderTasks = new ArrayList<>(2);
                orderTasks.add(OrderTaskAssembler.setAdvMode(value));
                orderTasks.add(OrderTaskAssembler.getAdvMode());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
            }
        });
        dialog.show(getChildFragmentManager());
    }

    public void setResetVisibility(boolean enablePasswordVerify) {
        mBind.tvResetBeacon.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
        mBind.lineResetBeacon.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setModifyPasswordShown(boolean enablePasswordVerify) {
        mBind.tvModifyPwd.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
        mBind.lineModifyPwd.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setAdvMode(int advMode) {
        mSelected = advMode;
        mBind.tvAdvMode.setText(advModeArray[mSelected]);
    }

    public void setSensorGone() {
        mBind.tvSensor.setVisibility(View.GONE);
        mBind.lineSensor.setVisibility(View.GONE);
    }

    public void setDeviceTypeValue(byte[] deviceTypeBytes, boolean isHallPowerEnable) {
        this.deviceTypeBytes = deviceTypeBytes;
        this.isHallPowerEnable = isHallPowerEnable;
    }
}
