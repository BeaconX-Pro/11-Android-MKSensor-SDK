package com.moko.bxp.s.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.bxp.s.c.databinding.ACFragmentDeviceBinding;

public class DeviceFragment extends Fragment {
    private ACFragmentDeviceBinding mBind;

    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = ACFragmentDeviceBinding.inflate(inflater, container, false);
        return mBind.getRoot();
    }

    public void setBattery(int battery) {
        mBind.tvBattery.setText(battery + "mV");
    }

    public void setMacAddress(String macAddress){
        mBind.tvMacAddress.setText(macAddress);
    }

    public void setProductMode(String productMode){
        mBind.tvDeviceModel.setText(productMode);
    }

    public void setSoftwareVersion(String softwareVersion){
        mBind.tvSoftwareVersion.setText(softwareVersion);
    }

    public void setFirmwareVersion(String firmwareVersion){
        mBind.tvFirmwareVersion.setText(firmwareVersion);
    }

    public void setHardwareVersion(String hardwareVersion){
        mBind.tvHardwareVersion.setText(hardwareVersion);
    }

    public void setProductDate(String productDate){
        mBind.tvProductDate.setText(productDate);
    }

    public void setManufacturer(String manufacturer){
        mBind.tvManufacturer.setText(manufacturer);
    }
}
