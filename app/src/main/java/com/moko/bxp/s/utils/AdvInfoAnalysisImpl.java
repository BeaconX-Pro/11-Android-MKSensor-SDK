package com.moko.bxp.s.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.entity.AdvInfo;
import com.moko.support.s.entity.DeviceInfo;
import com.moko.support.s.entity.OrderServices;
import com.moko.support.s.service.DeviceInfoAnalysis;

import java.util.Arrays;
import java.util.HashMap;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class AdvInfoAnalysisImpl implements DeviceInfoAnalysis<AdvInfo> {
    private final HashMap<String, AdvInfo> beaconXInfoHashMap;

    public AdvInfoAnalysisImpl() {
        this.beaconXInfoHashMap = new HashMap<>();
    }

    private String getAdvChannel(int paramsInfo) {
        int bit0 = (paramsInfo & 0x01) == 1 ? 1 : 0;
        int bit1 = (paramsInfo >> 1 & 0x01) == 1 ? 1 : 0;
        int bit2 = (paramsInfo >> 2 & 0x01) == 1 ? 1 : 0;
        StringBuilder builder = new StringBuilder();
        builder.append(bit2).append(bit1).append(bit0);
        String result = builder.toString();
        if ("000".equals(result)) return "2401MHZ";
        if ("001".equals(result)) return "2402MHZ";
        if ("010".equals(result)) return "2426MHZ";
        if ("011".equals(result)) return "2480MHZ";
        if ("100".equals(result)) return "2481MHZ";
        return null;
    }

    private String getDbm(int paramsInfo) {
        int bit4 = (paramsInfo >> 4 & 0x01) == 1 ? 1 : 0;
        int bit5 = (paramsInfo >> 5 & 0x01) == 1 ? 1 : 0;
        int bit6 = (paramsInfo >> 6 & 0x01) == 1 ? 1 : 0;
        int bit7 = (paramsInfo >> 7 & 0x01) == 1 ? 1 : 0;
        StringBuilder builder = new StringBuilder();
        builder.append(bit7).append(bit6).append(bit5).append(bit4);
        String result = builder.toString();
        if ("0000".equals(result)) return "0dBm";
        if ("0001".equals(result)) return "+3dBm";
        if ("0010".equals(result)) return "+4dBm";
        if ("0011".equals(result)) return "-40dBm";
        if ("0100".equals(result)) return "-20dBm";
        if ("0101".equals(result)) return "-16dBm";
        if ("0110".equals(result)) return "-12dBm";
        if ("0111".equals(result)) return "-8dBm";
        if ("1000".equals(result)) return "-4dBm";
        if ("1001".equals(result)) return "-30dBm";
        return null;
    }

    private int getAdInterval(int interval) {
        int bit0 = (interval & 0x01) == 1 ? 1 : 0;
        int bit1 = (interval >> 1 & 0x01) == 1 ? 1 : 0;
        int bit2 = (interval >> 2 & 0x01) == 1 ? 1 : 0;
        int bit3 = (interval >> 3 & 0x01) == 1 ? 1 : 0;
        int bit4 = (interval >> 4 & 0x01) == 1 ? 1 : 0;
        int bit5 = (interval >> 5 & 0x01) == 1 ? 1 : 0;
        int bit6 = (interval >> 6 & 0x01) == 1 ? 1 : 0;
        StringBuilder builder = new StringBuilder();
        builder.append(bit0).append(bit1).append(bit2).append(bit3).append(bit4).append(bit5).append(bit6);
        String result = builder.toString();
        if ("0101011".equals(result)) return 10;
        if ("1010011".equals(result)) return 20;
        if ("0010101".equals(result)) return 50;
        if ("0101001".equals(result)) return 100;
        if ("1010001".equals(result)) return 200;
        if ("0010001".equals(result)) return 250;
        if ("0100001".equals(result)) return 500;
        if ("1000001".equals(result)) return 1000;
        if ("0100000".equals(result)) return 2000;
        if ("1010000".equals(result)) return 5000;
        if ("0101000".equals(result)) return 10000;
        if ("0010100".equals(result)) return 20000;
        if ("1010010".equals(result)) return 50000;
        if ("0101010".equals(result)) return 100000;
        return -1;
    }

    @Override
    public AdvInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        if (null == record) return null;
        int battery = -1;
        int batterPercent = -1;
        String productMhz = null;
        String productTxPower = null;
        int productAdvInterval = -1;
        String deviceInfoMhz = null;
        String deviceInfoTxPower = null;
        int deviceInfoAdvInterval = -1;
        int temperature = -1;
        int alarmCount = -1;
        String alarmStatus = null;
        int advType = -1;
        int key = -1;
        //产测信息帧
        byte[] serviceData = record.getServiceData(new ParcelUuid(OrderServices.SERVICE_ADV_PRODUCT_TEST.getUuid()));
        if (null != serviceData && serviceData.length == 13) {
            battery = MokoUtils.toInt(Arrays.copyOfRange(serviceData, 1, 3));
            productMhz = getAdvChannel(serviceData[9] & 0xff);
            productTxPower = getDbm(serviceData[9] & 0xff);
            productAdvInterval = getAdInterval(serviceData[10] & 0xff);
            advType = 1;
            key = 0x01;
        }
        //设备信息帧
        byte[] bytes = record.getManufacturerSpecificData(0x000D);
        if (null != bytes && bytes.length == 27) {
            if (bytes[0] != 0x04) return null;
            advType = 2;
            key = bytes[1] & 0xff;
            if (key == 0x10) {
                //参数信息定位包
                deviceInfoMhz = getAdvChannel(bytes[2] & 0xff);
                deviceInfoTxPower = getDbm(bytes[2] & 0xff);
                deviceInfoAdvInterval = getAdInterval(bytes[4] & 0xff);
                //报警状态
                int i = bytes[3] & 0xff;
                alarmStatus = (i >> 3 & 0x01) == 1 ? "Triggerd" : "Standy";
                //电量百分比
                int batterInfo = bytes[3] & 0xff;
                int bit4 = (batterInfo >> 4 & 0x01) == 1 ? 1 : 0;
                int bit5 = (batterInfo >> 5 & 0x01) == 1 ? 1 : 0;
                int bit6 = (batterInfo >> 6 & 0x01) == 1 ? 1 : 0;
                int bit7 = (batterInfo >> 7 & 0x01) == 1 ? 1 : 0;
                batterPercent = Integer.parseInt("" + bit7 + bit6 + bit5 + bit4, 2);
            } else if (key == 0x1C) {
                //此处固件已做计算，app端直接取就好
                temperature = bytes[2];
                String countHigh = MokoUtils.byte2HexString(bytes[3]);
                String countLow = MokoUtils.byte2HexString(bytes[4]);
                alarmCount = Integer.parseInt(countHigh + countLow, 16);
            }
        }
        if (advType == -1 || (key != 0x10 && key != 0x1C && key != 0x18 && key != 0x01))
            return null;
        AdvInfo advInfo;
        if (beaconXInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = beaconXInfoHashMap.get(deviceInfo.mac);
            if (advInfo == null) return null;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.battery = battery;
            advInfo.connectable = result.isConnectable();
            if (key == 0x1C) {
                advInfo.temperature = temperature;
                advInfo.alarmCount = alarmCount;
            }
            if (key == 0x10) {
                advInfo.deviceInfoAdvInterval = deviceInfoAdvInterval;
                advInfo.deviceInfoMhz = deviceInfoMhz;
                advInfo.deviceInfoTxPower = deviceInfoTxPower;
                advInfo.alarmStatus = alarmStatus;
                advInfo.batterPercent = batterPercent;
            }
            advInfo.productAdvInterval = productAdvInterval;
            advInfo.productMhz = productMhz;
            advInfo.productTxPower = productTxPower;
            advInfo.txPower = record.getTxPowerLevel();
            advInfo.scanRecord = deviceInfo.scanRecord;
            long currentTime = SystemClock.elapsedRealtime();
            advInfo.intervalTime = currentTime - advInfo.scanTime;
            advInfo.scanTime = currentTime;
            advInfo.advType = advType;
        } else {
            advInfo = new AdvInfo();
            advInfo.name = deviceInfo.name;
            advInfo.mac = deviceInfo.mac;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.battery = battery;
            advInfo.connectable = result.isConnectable();
            advInfo.txPower = record.getTxPowerLevel();
            advInfo.productAdvInterval = productAdvInterval;
            advInfo.productMhz = productMhz;
            advInfo.productTxPower = productTxPower;
            advInfo.deviceInfoAdvInterval = deviceInfoAdvInterval;
            advInfo.deviceInfoMhz = deviceInfoMhz;
            advInfo.deviceInfoTxPower = deviceInfoTxPower;
            advInfo.temperature = temperature;
            advInfo.alarmCount = alarmCount;
            advInfo.alarmStatus = alarmStatus;
            advInfo.scanRecord = deviceInfo.scanRecord;
            advInfo.advType = advType;
            advInfo.batterPercent = batterPercent;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            beaconXInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        return advInfo;
    }
}
