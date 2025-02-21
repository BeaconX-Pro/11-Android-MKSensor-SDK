package com.moko.support.s.task;

import static com.moko.support.s.entity.SlotAdvType.HUM_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.I_BEACON;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.SENSOR_INFO;
import static com.moko.support.s.entity.SlotAdvType.TEMP_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.UID;
import static com.moko.support.s.entity.SlotAdvType.URL;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.entity.SlotData;
import com.moko.support.s.entity.TriggerStep1Bean;

import java.nio.ByteBuffer;
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
        response.responseValue = data = new byte[]{(byte) 0xEA, (byte) 0x00, (byte) key.getParamsKey(), (byte) 0x00};
    }

    public void setData(ParamsKeyEnum key) {
        response.responseValue = data = new byte[]{(byte) 0xEA, (byte) 0x01, (byte) key.getParamsKey(), (byte) 0x00};
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

    public void getTriggerAfterSlotParams(@IntRange(from = 0, to = 2) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS_AFTER.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void setSlotAdvParamsAfter(@NonNull SlotData afterBean) {
        int length = 0;
        byte[] bytes = getAdvBytes(afterBean);
        if (null != bytes) length = bytes.length;
        byte[] intervalBytes = MokoUtils.toByteArray(afterBean.advInterval, 2);
        byte[] durationBytes = MokoUtils.toByteArray(afterBean.advDuration, 2);
        data = new byte[12 + length];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_PARAMS_AFTER.getParamsKey();
        data[3] = (byte) (length + 8);
        data[4] = (byte) afterBean.slot;
        data[5] = intervalBytes[0];
        data[6] = intervalBytes[1];
        data[7] = durationBytes[0];
        data[8] = durationBytes[1];
        data[9] = (byte) afterBean.rssi;
        data[10] = (byte) afterBean.txPower;
        data[11] = (byte) afterBean.currentFrameType;
        if (length > 0) {
            System.arraycopy(bytes, 0, data, 12, bytes.length);
        }
        response.responseValue = data;
    }

    public void setSlotAdvParamsBefore(@NonNull SlotData beforeBean) {
        int length = 0;
        byte[] bytes = getAdvBytes(beforeBean);
        if (null != bytes) length = bytes.length;
        byte[] intervalBytes = MokoUtils.toByteArray(beforeBean.advInterval, 2);
        byte[] durationBytes = MokoUtils.toByteArray(beforeBean.advDuration, 2);
        byte[] standbyDurationBytes = MokoUtils.toByteArray(beforeBean.standbyDuration, 2);
        data = new byte[14 + length];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_PARAMS_BEFORE.getParamsKey();
        data[3] = (byte) (length + 10);
        data[4] = (byte) beforeBean.slot;
        data[5] = intervalBytes[0];
        data[6] = intervalBytes[1];
        data[7] = durationBytes[0];
        data[8] = durationBytes[1];
        data[9] = standbyDurationBytes[0];
        data[10] = standbyDurationBytes[1];
        data[11] = (byte) beforeBean.rssi;
        data[12] = (byte) beforeBean.txPower;
        data[13] = (byte) beforeBean.currentFrameType;
        if (length > 0) {
            System.arraycopy(bytes, 0, data, 14, bytes.length);
        }
        response.responseValue = data;
    }

    public void setAdvChannel(int channel) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_CHANNEL.getParamsKey(),
                (byte) 0x01,
                (byte) channel
        };
    }

    public void setBatteryMode(int mode) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BATTERY_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) mode
        };
    }

    public void resetBatteryPercent() {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BATTERY_PERCENT.getParamsKey(),
                (byte) 0x01,
                (byte) 0x01
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
    public void setLedRemoteReminder(@IntRange(from = 100, to = 10000) int interval,
                                     @IntRange(from = 10, to = 6000) int time) {
        byte[] bytesInterval = MokoUtils.toByteArray(interval, 2);
        byte[] bytesTime = MokoUtils.toByteArray(time, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LED_REMOTE_REMINDER.getParamsKey(),
                (byte) 0x05,
                (byte) 0x03,
                bytesInterval[0],
                bytesInterval[1],
                bytesTime[0],
                bytesTime[1]
        };
    }

    public void setBuzzerRemoteReminder(@IntRange(from = 100, to = 10000) int interval,
                                        @IntRange(from = 10, to = 6000) int time) {
        byte[] bytesInterval = MokoUtils.toByteArray(interval, 2);
        byte[] bytesTime = MokoUtils.toByteArray(time, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BUZZER_REMOTE_REMINDER.getParamsKey(),
                (byte) 0x05,
                (byte) 0x04,
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

    public void setSlotTriggerType(@NonNull TriggerStep1Bean triggerBean) {
        int triggerThreshold;
        if (triggerBean.triggerType == TEMP_TRIGGER) {
            triggerThreshold = triggerBean.tempThreshold;
        } else if (triggerBean.triggerType == HUM_TRIGGER) {
            triggerThreshold = triggerBean.humThreshold;
        } else {
            triggerThreshold = 0;
        }
        byte[] triggerThresholdBytes = MokoUtils.toByteArray(triggerThreshold, 2);
        int staticDuration = triggerBean.triggerType == MOTION_TRIGGER ? triggerBean.axisStaticPeriod : 0;
        byte[] staticDurationBytes = MokoUtils.toByteArray(staticDuration, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_TYPE.getParamsKey(),
                (byte) 0x08,
                (byte) triggerBean.slot,
                (byte) triggerBean.triggerType,
                (byte) triggerBean.triggerCondition,
                triggerThresholdBytes[0],
                triggerThresholdBytes[1],
                (byte) (triggerBean.lockedAdv ? 1 : 0),
                staticDurationBytes[0],
                staticDurationBytes[1]
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

    public void setButtonTurnOffEnable(int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BUTTON_TURN_OFF_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void getNormalSlotAdvParams(int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_NORMAL_SLOT_ADV_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void setNormalSlotAdvParams(@NonNull SlotData slotData) {
        int length = 0;
        byte[] bytes = getAdvBytes(slotData);
        if (null != bytes) length = bytes.length;
        byte[] intervalBytes = MokoUtils.toByteArray(slotData.advInterval, 2);
        byte[] durationBytes = MokoUtils.toByteArray(slotData.advDuration, 2);
        byte[] standbyBytes = MokoUtils.toByteArray(slotData.standbyDuration, 2);
        data = new byte[14 + length];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_NORMAL_SLOT_ADV_PARAMS.getParamsKey();
        data[3] = (byte) (length + 10);
        data[4] = (byte) slotData.slot;
        data[5] = intervalBytes[0];
        data[6] = intervalBytes[1];
        data[7] = durationBytes[0];
        data[8] = durationBytes[1];
        data[9] = standbyBytes[0];
        data[10] = standbyBytes[1];
        data[11] = (byte) slotData.rssi;
        data[12] = (byte) slotData.txPower;
        data[13] = (byte) slotData.currentFrameType;
        if (length > 0) {
            System.arraycopy(bytes, 0, data, 14, length);
        }
        response.responseValue = data;
    }

    private byte[] getAdvBytes(@NonNull SlotData slotData) {
        switch (slotData.currentFrameType) {
            case UID:
                byte[] nameBytes = MokoUtils.hex2bytes(slotData.namespace);
                byte[] idBytes = MokoUtils.hex2bytes(slotData.instanceId);
                byte[] uidBytes = new byte[16];
                System.arraycopy(nameBytes, 0, uidBytes, 0, nameBytes.length);
                System.arraycopy(idBytes, 0, uidBytes, 10, idBytes.length);
                return uidBytes;
            case URL:
                byte[] contentBytes = slotData.urlContent.getBytes();
                byte[] urlBytes = new byte[contentBytes.length + 1];
                urlBytes[0] = (byte) slotData.urlScheme;
                System.arraycopy(contentBytes, 0, urlBytes, 1, contentBytes.length);
                return urlBytes;
            case I_BEACON:
                byte[] beaconBytes = new byte[20];
                byte[] uuidBytes = MokoUtils.hex2bytes(slotData.uuid);
                byte[] majorBytes = MokoUtils.toByteArray(slotData.major, 2);
                byte[] minorBytes = MokoUtils.toByteArray(slotData.minor, 2);
                System.arraycopy(uuidBytes, 0, beaconBytes, 0, uuidBytes.length);
                System.arraycopy(majorBytes, 0, beaconBytes, 16, majorBytes.length);
                System.arraycopy(minorBytes, 0, beaconBytes, 18, minorBytes.length);
                return beaconBytes;
            case SENSOR_INFO:
                byte[] deviceNameBytes = slotData.deviceName.getBytes();
                byte[] tagIdBytes = MokoUtils.hex2bytes(slotData.tagId);
                byte[] sensorInfoBytes = new byte[deviceNameBytes.length + tagIdBytes.length + 2];
                sensorInfoBytes[0] = (byte) deviceNameBytes.length;
                System.arraycopy(deviceNameBytes, 0, sensorInfoBytes, 1, deviceNameBytes.length);
                sensorInfoBytes[deviceNameBytes.length + 1] = (byte) tagIdBytes.length;
                System.arraycopy(tagIdBytes, 0, sensorInfoBytes, deviceNameBytes.length + 2, tagIdBytes.length);
                return sensorInfoBytes;
        }
        return null;
    }

    public void setAxisParams(@IntRange(from = 0, to = 4) int rate,
                              @IntRange(from = 0, to = 3) int scale,
                              @IntRange(from = 1, to = 255) int sensitivity) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AXIS_PARAMS.getParamsKey(),
                (byte) 0x03,
                (byte) rate,
                (byte) scale,
                (byte) sensitivity
        };
    }

    public void setBuzzerFrequency(int frequency) {
        byte[] bytes = MokoUtils.toByteArray(frequency, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BUZZER_FREQUENCY.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1]
        };
    }
}
