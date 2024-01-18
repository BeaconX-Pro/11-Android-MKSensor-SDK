package com.moko.support.s.task;

import androidx.annotation.IntRange;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;


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
}
