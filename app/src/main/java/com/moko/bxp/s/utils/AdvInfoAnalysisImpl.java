package com.moko.bxp.s.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;
import android.text.TextUtils;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.entity.AdvInfo;
import com.moko.support.s.entity.DeviceInfo;
import com.moko.support.s.service.DeviceInfoAnalysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class AdvInfoAnalysisImpl implements DeviceInfoAnalysis<AdvInfo> {
    private final HashMap<String, AdvInfo> beaconXInfoHashMap;
    //1 所有的 2 sensorInfo广播帧切有温湿度贴片 3 sensorInfo广播帧切只有温度贴片
    private final int flag;

    public AdvInfoAnalysisImpl(int flag) {
        this.flag = flag;
        this.beaconXInfoHashMap = new HashMap<>();
    }

    @Override
    public AdvInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        int battery = -1;
        boolean isEddystone = false;
        boolean isSensorInfo = false;
        boolean isProductTest = false;
        boolean isBeacon = false;
        byte[] values = null;
        String tagId = null;
        int type = -1;
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        if (null == record) return null;
        Map<ParcelUuid, byte[]> map = record.getServiceData();
        byte[] manufacturerBytes = record.getManufacturerSpecificData(0x004C);
        if (null != manufacturerBytes && manufacturerBytes.length == 23) {
            if (flag == 2 || flag == 3) return null;
            isBeacon = true;
            type = AdvInfo.VALID_DATA_TYPE_IBEACON_APPLE;
            values = manufacturerBytes;
        }
        if (map != null && !map.isEmpty()) {
            for (ParcelUuid parcelUuid : map.keySet()) {
                if (parcelUuid.toString().startsWith("0000feaa")) {
                    if (flag == 2 || flag == 3) return null;
                    isEddystone = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case AdvInfo.VALID_DATA_FRAME_TYPE_UID:
                                if (bytes.length != 20) return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_UID;
                                break;
                            case AdvInfo.VALID_DATA_FRAME_TYPE_URL:
                                if (bytes.length > 20) return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_URL;
                                break;
                            case AdvInfo.VALID_DATA_FRAME_TYPE_TLM:
                                if (bytes.length != 14) return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_TLM;
                                break;
                        }
                    }
                    values = bytes;
                    break;
                } else if (parcelUuid.toString().startsWith("0000feab")) {
                    if (flag == 2 || flag == 3) return null;
                    isSensorInfo = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        if ((bytes[0] & 0xff) == AdvInfo.VALID_DATA_FRAME_TYPE_IBEACON) {
                            if (bytes.length != 23) return null;
                            type = AdvInfo.VALID_DATA_FRAME_TYPE_IBEACON;
                        } else if ((bytes[0] & 0xff) == AdvInfo.VALID_DATA_FRAME_TYPE_TH_INFO) {
                            if (bytes.length != 16) return null;
                            type = AdvInfo.VALID_DATA_FRAME_TYPE_TH_INFO;
                        }
                    }
                    values = bytes;
                    break;
                } else if (parcelUuid.toString().startsWith("0000ea01")) {
                    isSensorInfo = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        if ((bytes[0] & 0xff) == AdvInfo.VALID_DATA_FRAME_TYPE_SENSOR_INFO) {
                            if (bytes.length < 19) return null;
                            //传感器状态
                            int sensorStatus = bytes[1] & 0xff;
                            int tempStatus = sensorStatus >> 3 & 0x01;
                            int humStatus = sensorStatus >> 4 & 0x01;
                            if (flag == 2) {
                                if (tempStatus != 1 || humStatus != 1) return null;
                            } else if (flag == 3) {
                                //只有温度贴片
                                if (tempStatus != 1 || humStatus != 0) return null;
                            }
                            type = AdvInfo.VALID_DATA_FRAME_TYPE_SENSOR_INFO;
                            battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 16, 18));
                            tagId = MokoUtils.bytesToHexString(Arrays.copyOfRange(bytes,18,bytes.length));
                        }
                    }
                    values = bytes;
                    break;
                } else if (parcelUuid.toString().startsWith("0000eb01")) {
                    if (flag == 2 || flag == 3) return null;
                    isProductTest = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        if ((bytes[0] & 0xff) == AdvInfo.VALID_DATA_FRAME_TYPE_PRODUCTION_TEST) {
                            if (bytes.length != 13) return null;
                            battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 1, 3));
                            type = AdvInfo.VALID_DATA_FRAME_TYPE_PRODUCTION_TEST;
                            values = bytes;
                        }
                    }
                    break;
                }else return null;
            }
        }
        if ((!isEddystone && !isSensorInfo && !isProductTest && !isBeacon) || values == null || type == -1) {
            return null;
        }
        AdvInfo advInfo;
        if (beaconXInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = beaconXInfoHashMap.get(deviceInfo.mac);
            if (null == advInfo) return null;
            if (!TextUtils.isEmpty(deviceInfo.name)) advInfo.name = deviceInfo.name;
            advInfo.rssi = deviceInfo.rssi;
            if (battery >= 0) advInfo.battery = battery;
            if (result.isConnectable()) advInfo.connectState = 1;
            advInfo.scanRecord = deviceInfo.scanRecord;
            advInfo.tagId = tagId;
            long currentTime = SystemClock.elapsedRealtime();
            advInfo.intervalTime = currentTime - advInfo.scanTime;
            advInfo.scanTime = currentTime;
        } else {
            advInfo = new AdvInfo();
            advInfo.name = deviceInfo.name;
            advInfo.mac = deviceInfo.mac;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.battery = battery;
            advInfo.tagId = tagId;
            if (result.isConnectable()) {
                advInfo.connectState = 1;
            } else {
                advInfo.connectState = 0;
            }
            advInfo.scanRecord = deviceInfo.scanRecord;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfo.validDataHashMap = new HashMap<>();
            beaconXInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        String data = MokoUtils.bytesToHexString(values);
        if (advInfo.validDataHashMap.containsKey(data)) {
            return advInfo;
        } else {
            AdvInfo.ValidData validData = new AdvInfo.ValidData();
            validData.data = data;
            validData.type = type;
            validData.values = values;
            validData.txPower = record.getTxPowerLevel();
            if (type == AdvInfo.VALID_DATA_FRAME_TYPE_TLM) {
                advInfo.validDataHashMap.put(String.valueOf(type), validData);
                return advInfo;
            }
            if (type == AdvInfo.VALID_DATA_FRAME_TYPE_SENSOR_INFO) {
                advInfo.validDataHashMap.put(String.valueOf(type), validData);
                return advInfo;
            }
            advInfo.validDataHashMap.put(String.valueOf(type), validData);
        }
        return advInfo;
    }
}
