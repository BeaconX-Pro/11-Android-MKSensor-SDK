package com.moko.bxp.s.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moko.bxp.s.databinding.FragmentDeviceSBinding;

public class DeviceFragment extends BaseFragment<FragmentDeviceSBinding> {
    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    @Override
    protected FragmentDeviceSBinding getViewBind(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentDeviceSBinding.inflate(inflater, container, false);
    }

    public void setBattery(int battery) {
        mBind.tvBattery.setText(battery + "mV");
    }

    public void setBatteryPercent(int percent) {
        mBind.tvBatteryPercent.setText(percent + "%");
    }

    public void setMacAddress(String macAddress) {
        if (null == mBind) return;
        mBind.tvMacAddress.setText(macAddress);
    }

    public void setProductMode(String productMode) {
        mBind.tvDeviceModel.setText(productMode);
    }

    public void setSoftwareVersion(String softwareVersion) {
        mBind.tvSoftwareVersion.setText(softwareVersion);
    }

    public void setFirmwareVersion(String firmwareVersion) {
        mBind.tvFirmwareVersion.setText(firmwareVersion);
    }

    public void setHardwareVersion(String hardwareVersion) {
        mBind.tvHardwareVersion.setText(hardwareVersion);
    }

    public void setProductDate(String productDate) {
        mBind.tvProductDate.setText(productDate);
    }

    public void setManufacturer(String manufacturer) {
        mBind.tvManufacturer.setText(manufacturer);
    }
}
