package com.moko.support.s.task;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.entity.UrlExpansionEnum;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;


public class ParamsTask extends OrderTask {
    public byte[] data;

    public ParamsTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void getData(ParamsKeyEnum key) {
        createGetParamsData(key.getParamsKey());
    }

    public void setData(ParamsKeyEnum key) {
        createSetParamsData(key.getParamsKey());
    }

    private void createGetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) 0x00, (byte) paramsKey, (byte) 0x00};
    }

    private void createSetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) 0x01, (byte) paramsKey, (byte) 0x00};
    }

    public void getHallTriggerEvent(int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_HALL_TRIGGER_EVENT.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void getAxisTriggerEvent(int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_AXIS_TRIGGER_EVENT.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void getHumTriggerEvent(int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_HUM_TRIGGER_EVENT.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void getTempTriggerEvent(int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_TEMP_TRIGGER_EVENT.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void getSlotAdvParams(@IntRange(from = 0, to = 5) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void setSlotParamsNoData(@IntRange(from = 0, to = 5) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) 0xFF // No Data
        };
    }

    public void setSlotParamsUID(@IntRange(from = 0, to = 5) int slot,
                                 String namespaceId, String instanceId) {
        byte[] namespaceIdBytes = MokoUtils.hex2bytes(namespaceId);
        byte[] instanceIdBytes = MokoUtils.hex2bytes(instanceId);
        data = new byte[22];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey();
        data[3] = (byte) 0x12;
        data[4] = (byte) slot;
        data[5] = (byte) 0x00; // UID
        System.arraycopy(namespaceIdBytes, 0, data, 6, namespaceIdBytes.length);
        System.arraycopy(instanceIdBytes, 0, data, 16, instanceIdBytes.length);
        response.responseValue = data;
    }

    public void setSlotParamsURL(@IntRange(from = 0, to = 5) int slot,
                                 int urlScheme, @NonNull String urlContent) {
        String urlContentHex;
        if (urlContent.indexOf(".") >= 0) {
            String urlExpansion = urlContent.substring(urlContent.lastIndexOf("."));
            UrlExpansionEnum urlExpansionEnum = UrlExpansionEnum.fromUrlExpanDesc(urlExpansion);
            if (urlExpansionEnum == null) {
                urlContentHex = MokoUtils.string2Hex(urlContent);
            } else {
                String content = urlContent.substring(0, urlContent.lastIndexOf("."));
                urlContentHex = MokoUtils.string2Hex(content) + MokoUtils.int2HexString(urlExpansionEnum.getUrlExpanType());
            }
        } else {
            urlContentHex = MokoUtils.string2Hex(urlContent);
        }
        byte[] urlContentBytes = MokoUtils.hex2bytes(urlContentHex);
        int urlLength = urlContentBytes.length;
        data = new byte[7 + urlLength];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey();
        data[3] = (byte) (3 + urlLength);
        data[4] = (byte) slot;
        data[5] = (byte) 0x10;// URL
        data[6] = (byte) urlScheme;
        System.arraycopy(urlContentBytes, 0, data, 7, urlContentBytes.length);
        response.responseValue = data;
    }

    public void setSlotParamsTagInfo(@IntRange(from = 0, to = 5) int slot,
                                     String deviceName, String tagId) {
        byte[] deviceNameBytes = deviceName.getBytes();
        byte[] tagIdBytes = MokoUtils.hex2bytes(tagId);
        int deviceNameLength = deviceNameBytes.length;
        int tagIdLength = tagIdBytes.length;
        data = new byte[8 + deviceNameLength + tagIdLength];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey();
        data[3] = (byte) (4 + deviceNameLength + tagIdLength);
        data[4] = (byte) slot;
        data[5] = (byte) 0x80; // TAG
        data[6] = (byte) deviceNameLength;
        System.arraycopy(deviceNameBytes, 0, data, 7, deviceNameLength);
        data[7 + deviceNameLength] = (byte) tagIdLength;
        for (int i = 0; i < tagIdLength; i++) {
            data[8 + deviceNameLength + i] = tagIdBytes[i];
        }
        response.responseValue = data;
    }

    public void setSlotParamsIBeacon(@IntRange(from = 0, to = 5) int slot,
                                     int major, int minor, String uuid) {
        byte[] majorBytes = MokoUtils.toByteArray(major, 2);
        byte[] minorBytes = MokoUtils.toByteArray(minor, 2);
        byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
        data = new byte[26];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey();
        data[3] = (byte) 0x16;
        data[4] = (byte) slot;
        data[5] = (byte) 0x50; // iBeacon
        System.arraycopy(majorBytes, 0, data, 6, majorBytes.length);
        System.arraycopy(minorBytes, 0, data, 8, minorBytes.length);
        System.arraycopy(uuidBytes, 0, data, 10, uuidBytes.length);
        response.responseValue = data;
    }

    public void setSlotParamsTLM(@IntRange(from = 0, to = 5) int slot) {
        data = new byte[6];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey();
        data[3] = (byte) 0x02;
        data[4] = (byte) slot;
        data[5] = (byte) 0x20; // TLM
        response.responseValue = data;
    }

    public void setSlotParamsTH(@IntRange(from = 0, to = 5) int slot) {
        data = new byte[6];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey();
        data[3] = (byte) 0x02;
        data[4] = (byte) slot;
        data[5] = (byte) 0x70; // TLM
        response.responseValue = data;
    }

    public void setHallTriggerEvent(int slot, int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_HALL_TRIGGER_EVENT.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) enable
        };
    }

    public void setAxisTriggerEvent(int slot, int enable, int staticPeriod) {
        byte[] bytes = MokoUtils.toByteArray(staticPeriod, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AXIS_TRIGGER_EVENT.getParamsKey(),
                (byte) 0x04,
                (byte) slot,
                (byte) enable,
                bytes[0],
                bytes[1]
        };
    }

    public void setHumTriggerEvent(int slot, int enable, int threshold) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_HUM_TRIGGER_EVENT.getParamsKey(),
                (byte) 0x04,
                (byte) slot,
                (byte) enable,
                (byte) threshold,
                1
        };
    }

    public void setTempTriggerEvent(int slot, int enable, int threshold) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TEMP_TRIGGER_EVENT.getParamsKey(),
                (byte) 0x04,
                (byte) slot,
                (byte) enable,
                (byte) threshold,
                1
        };
    }

    public void getTriggerBeforeSlotParams(@IntRange(from = 0, to = 5) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS_BEFORE.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void getTriggerAfterSlotParams(@IntRange(from = 0, to = 5) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS_AFTER.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void setSlotAdvParamsAfter(int slot, int interval, int duration, int rssi, int txPower) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        byte[] durationBytes = MokoUtils.toByteArray(duration, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS_AFTER.getParamsKey(),
                (byte) 0x07,
                (byte) slot,
                intervalBytes[0],
                intervalBytes[1],
                durationBytes[0],
                durationBytes[1],
                (byte) rssi,
                (byte) txPower
        };
    }

    public void setSlotAdvParamsBefore(@IntRange(from = 0, to = 5) int slot,
                                       @IntRange(from = 100, to = 10000) int interval,
                                       @IntRange(from = 1, to = 65535) int duration,
                                       @IntRange(from = 0, to = 65535) int standbyDuration,
                                       @IntRange(from = -100, to = 0) int rssi, int txPower) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        byte[] durationBytes = MokoUtils.toByteArray(duration, 2);
        byte[] standbyDurationBytes = MokoUtils.toByteArray(standbyDuration, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS_BEFORE.getParamsKey(),
                (byte) 0x09,
                (byte) slot,
                intervalBytes[0],
                intervalBytes[1],
                durationBytes[0],
                durationBytes[1],
                standbyDurationBytes[0],
                standbyDurationBytes[1],
                (byte) rssi,
                (byte) txPower
        };
    }

    public void setTHStore(@IntRange(from = 0, to = 1) int enable, @IntRange(from = 1, to = 65535) int interval) {
        byte[] bytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TH_STORE.getParamsKey(),
                (byte) 0x03,
                (byte) enable,
                bytes[0],
                bytes[1]
        };
    }

    public void setTHSampleInterval(@IntRange(from = 1, to = 65535) int interval) {
        byte[] bytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TH_SAMPLE_RATE.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1]
        };
    }

    public void setCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        calendar.setTimeZone(timeZone);
        long utcTime = calendar.getTimeInMillis() / 1000;
        byte[] bytes = ByteBuffer.allocate(8).putLong(utcTime).array();
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SYNC_CURRENT_TIME.getParamsKey(),
                0x04,
                bytes[4],
                bytes[5],
                bytes[6],
                bytes[7]
        };
    }

    public void setHallStoreEnable(int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_HALL_STORE_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setStatus(@IntRange(from = 0, to = 1) int status, ParamsKeyEnum keyEnum) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) keyEnum.getParamsKey(),
                (byte) 0x01,
                (byte) status
        };
    }

    //设置远程控制led
    public void setRemoteReminder(@IntRange(from = 100, to = 10000) int interval,
                                  @IntRange(from = 1, to = 600) int time) {
        byte[] bytesInterval = MokoUtils.toByteArray(interval, 2);
        byte[] bytesTime = MokoUtils.toByteArray(time, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REMOTE_REMINDER.getParamsKey(),
                (byte) 0x05,
                (byte) 0x03,
                bytesInterval[0],
                bytesInterval[1],
                bytesTime[0],
                bytesTime[1]
        };
    }

    public void getSlotTriggerType(int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void setSlotTriggerType(int slot, int triggerType) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_TYPE.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) triggerType
        };
    }

    public void setAdvMode(int advMode) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) advMode
        };
    }

    public void setHallPowerEnable(int enable){
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_HALL_POWER_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setAxisParams(@IntRange(from = 0, to = 4) int rate,
                              @IntRange(from = 0, to = 3) int scale,
                              @IntRange(from = 1, to = 255) int sensitivity) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AXIS_PARAMS.getParamsKey(),
                (byte) 0x03,
                (byte) rate,
                (byte) scale,
                (byte) sensitivity
        };
        response.responseValue = data;
    }

    public void setTriggerLedStatus(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TRIGGER_LED_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    private String dataBytesStr = "";

//    @Override
//    public boolean parseValue(byte[] value) {
//        final int header = value[0] & 0xFF;
//        final int flag = value[1] & 0xFF;
//        if (header != 0xEC || flag != 0) return true;
//        int cmd = value[2] & 0xff;
//        int packetCount = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
//        int packetIndex = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
//        int length = value[7] & 0xff;
//        if (packetIndex < packetCount - 1) {
//            //有多包数据
//            byte[] remainBytes = Arrays.copyOfRange(value, 8, 8 + length);
//            dataBytesStr += MokoUtils.bytesToHexString(remainBytes);
//        } else {
//            if (length == 0) {
//                data = new byte[5];
//                data[0] = (byte) 0xEC;
//                data[1] = (byte) 0x00;
//                data[2] = (byte) cmd;
//                data[3] = 0;
//                data[4] = 0;
//                response.responseValue = data;
//                orderStatus = ORDER_STATUS_SUCCESS;
//                MokoSupport.getInstance().pollTask();
//                MokoSupport.getInstance().executeTask();
//                MokoSupport.getInstance().orderResult(response);
//                return false;
//            }
//            byte[] remainBytes = Arrays.copyOfRange(value, 8, 8 + length);
//            dataBytesStr += MokoUtils.bytesToHexString(remainBytes);
//            byte[] dataBytes = MokoUtils.hex2bytes(dataBytesStr);
//            int dataLength = dataBytes.length;
//            byte[] dataLengthBytes = MokoUtils.toByteArray(dataLength, 2);
//            data = new byte[dataLength + 5];
//            data[0] = (byte) 0xEC;
//            data[1] = (byte) 0x00;
//            data[2] = (byte) cmd;
//            data[3] = dataLengthBytes[0];
//            data[4] = dataLengthBytes[1];
//            System.arraycopy(dataBytes, 0, data, 5, dataLength);
//            response.responseValue = data;
//            orderStatus = ORDER_STATUS_SUCCESS;
//            MokoSupport.getInstance().pollTask();
//            MokoSupport.getInstance().executeTask();
//            MokoSupport.getInstance().orderResult(response);
//            dataBytesStr = "";
//        }
//        return false;
//    }
}
