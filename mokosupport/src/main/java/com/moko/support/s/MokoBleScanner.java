package com.moko.support.s;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.s.callback.MokoScanDeviceCallback;
import com.moko.support.s.entity.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public final class MokoBleScanner {
    private MokoLeScanHandler mMokoLeScanHandler;
    private MokoScanDeviceCallback mMokoScanDeviceCallback;

    public void startScanDevice(MokoScanDeviceCallback callback) {
        mMokoScanDeviceCallback = callback;
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> scanFilterList = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
//        builder.setServiceData(new ParcelUuid(OrderServices.SERVICE_ADV_TRIGGER.getUuid()), null);
        scanFilterList.add(builder.build());
        mMokoLeScanHandler = new MokoLeScanHandler(callback);
        scanner.startScan(scanFilterList, settings, mMokoLeScanHandler);
        callback.onStartScan();
    }

    public void stopScanDevice() {
        if (mMokoLeScanHandler != null && mMokoScanDeviceCallback != null) {
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mMokoLeScanHandler);
            mMokoScanDeviceCallback.onStopScan();
            mMokoLeScanHandler = null;
            mMokoScanDeviceCallback = null;
        }
    }

    public static class MokoLeScanHandler extends ScanCallback {
        private final MokoScanDeviceCallback callback;

        public MokoLeScanHandler(MokoScanDeviceCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (null == result.getScanRecord()) return;
            byte[] scanRecord = result.getScanRecord().getBytes();
            String name = result.getScanRecord().getDeviceName();
            int rssi = result.getRssi();
            if (null == scanRecord || scanRecord.length == 0 || rssi == 127) {
                return;
            }
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.name = name;
            deviceInfo.rssi = rssi;
            deviceInfo.mac = device.getAddress();
            deviceInfo.scanRecord = MokoUtils.bytesToHexString(scanRecord);
            deviceInfo.scanResult = result;
            callback.onScanDevice(deviceInfo);
        }
    }
}
