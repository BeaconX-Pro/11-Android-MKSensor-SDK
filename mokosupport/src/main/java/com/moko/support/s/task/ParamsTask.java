package com.moko.support.s.task;

import androidx.annotation.IntRange;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;

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

    public void setAdvMode(int advMode) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) advMode
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

    public void setPowerSavingStaticTriggerTime(@IntRange(from = 0, to = 65535) int time) {
        byte[] intervalBytes = MokoUtils.toByteArray(time, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_POWER_SAVING_STATIC_TRIGGER_TIME.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1]
        };
        response.responseValue = data;
    }

    ///////////////////////////////////////
    public void setNormalAdvParams(int advInterval, int txPower, int advDuration, int standByDuration, int advChannel) {
        byte[] advIntervalBytes = MokoUtils.toByteArray(advInterval, 2);
        byte[] advDurationBytes = MokoUtils.toByteArray(advDuration, 2);
        byte[] standByBytes = MokoUtils.toByteArray(standByDuration, 2);
        data = new byte[4 + 8];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_NORMAL_ADV_PARAMS.getParamsKey();
        data[3] = 8;
        data[4] = advIntervalBytes[0];
        data[5] = advIntervalBytes[1];
        data[6] = (byte) txPower;
        data[7] = advDurationBytes[0];
        data[8] = advDurationBytes[1];
        data[9] = standByBytes[0];
        data[10] = standByBytes[1];
        data[11] = (byte) advChannel;
        response.responseValue = data;
    }

    public void setButtonTriggerParams(int advInterval, int txPower, int advDuration, int triggerType) {
        byte[] advIntervalBytes = MokoUtils.toByteArray(advInterval, 2);
        byte[] advDurationBytes = MokoUtils.toByteArray(advDuration, 2);
        data = new byte[4 + 6];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_BUTTON_TRIGGER_PARAMS.getParamsKey();
        data[3] = 6;
        data[4] = advIntervalBytes[0];
        data[5] = advIntervalBytes[1];
        data[6] = (byte) txPower;
        data[7] = advDurationBytes[0];
        data[8] = advDurationBytes[1];
        data[9] = (byte) triggerType;
        response.responseValue = data;
    }

    public void setTriggerLedStatus(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TRIGGER_LED_STATUS.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    private String dataBytesStr = "";

    @Override
    public boolean parseValue(byte[] value) {
        final int header = value[0] & 0xFF;
        final int flag = value[1] & 0xFF;
        if (header != 0xEC || flag != 0) return true;
        int cmd = value[2] & 0xff;
        int packetCount = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
        int packetIndex = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
        int length = value[7] & 0xff;
        if (packetIndex < packetCount - 1) {
            //有多包数据
            byte[] remainBytes = Arrays.copyOfRange(value, 8, 8 + length);
            dataBytesStr += MokoUtils.bytesToHexString(remainBytes);
        } else {
            if (length == 0) {
                data = new byte[5];
                data[0] = (byte) 0xEC;
                data[1] = (byte) 0x00;
                data[2] = (byte) cmd;
                data[3] = 0;
                data[4] = 0;
                response.responseValue = data;
                orderStatus = ORDER_STATUS_SUCCESS;
                MokoSupport.getInstance().pollTask();
                MokoSupport.getInstance().executeTask();
                MokoSupport.getInstance().orderResult(response);
                return false;
            }
            byte[] remainBytes = Arrays.copyOfRange(value, 8, 8 + length);
            dataBytesStr += MokoUtils.bytesToHexString(remainBytes);
            byte[] dataBytes = MokoUtils.hex2bytes(dataBytesStr);
            int dataLength = dataBytes.length;
            byte[] dataLengthBytes = MokoUtils.toByteArray(dataLength, 2);
            data = new byte[dataLength + 5];
            data[0] = (byte) 0xEC;
            data[1] = (byte) 0x00;
            data[2] = (byte) cmd;
            data[3] = dataLengthBytes[0];
            data[4] = dataLengthBytes[1];
            System.arraycopy(dataBytes, 0, data, 5, dataLength);
            response.responseValue = data;
            orderStatus = ORDER_STATUS_SUCCESS;
            MokoSupport.getInstance().pollTask();
            MokoSupport.getInstance().executeTask();
            MokoSupport.getInstance().orderResult(response);
            dataBytesStr = "";
        }
        return false;
    }
}
