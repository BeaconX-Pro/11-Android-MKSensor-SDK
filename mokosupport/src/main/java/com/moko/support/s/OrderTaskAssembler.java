package com.moko.support.s;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.entity.SlotData;
import com.moko.support.s.entity.TriggerStep1Bean;
import com.moko.support.s.task.ParamsTask;
import com.moko.support.s.task.PasswordTask;

public class OrderTaskAssembler {

    /**
     * 获取制造商
     */
    public static OrderTask getManufacturer() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_MANUFACTURE);
        return task;
    }

    /**
     * 获取设备型号
     */
    public static OrderTask getDeviceModel() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_PRODUCT_MODEL);
        return task;
    }

    /**
     * 获取生产日期
     */
    public static OrderTask getProductDate() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_MANUFACTURE_DATE);
        return task;
    }

    /**
     * 获取硬件版本
     */
    public static OrderTask getHardwareVersion() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_HARDWARE_VERSION);
        return task;
    }

    /**
     * 获取固件版本
     */
    public static OrderTask getFirmwareVersion() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_FIRMWARE_VERSION);
        return task;
    }

    /**
     * 获取软件版本
     */
    public static OrderTask getSoftwareVersion() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SOFTWARE_VERSION);
        return task;
    }

    public static OrderTask getTHStore() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TH_STORE);
        return task;
    }

    public static OrderTask setTHStore(@IntRange(from = 0, to = 1) int enable, @IntRange(from = 1, to = 65535) int interval) {
        ParamsTask task = new ParamsTask();
        task.setTHStore(enable, interval);
        return task;
    }

    public static OrderTask getTHSampleInterval() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TH_SAMPLE_RATE);
        return task;
    }

    public static OrderTask setTHSampleInterval(@IntRange(from = 1, to = 65535) int interval) {
        ParamsTask task = new ParamsTask();
        task.setTHSampleInterval(interval);
        return task;
    }

    public static OrderTask getCurrentTime() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SYNC_CURRENT_TIME);
        return task;
    }

    public static OrderTask getMotionTriggerCount() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_MOTION_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask clearMotionTriggerCount() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask getBatteryMode() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BATTERY_MODE);
        return task;
    }

    public static OrderTask setBatteryMode(int mode) {
        ParamsTask task = new ParamsTask();
        task.setBatteryMode(mode);
        return task;
    }

    public static OrderTask getBatteryPercent() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BATTERY_PERCENT);
        return task;
    }

    public static OrderTask resetBatteryPercent() {
        ParamsTask task = new ParamsTask();
        task.resetBatteryPercent();
        return task;
    }

    public static OrderTask getMagneticTriggerCount() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_MAGNETIC_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask getAdvChannel() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_ADV_CHANNEL);
        return task;
    }

    public static OrderTask setAdvChannel(int channel) {
        ParamsTask task = new ParamsTask();
        task.setAdvChannel(channel);
        return task;
    }

    public static OrderTask getDeviceMac() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DEVICE_MAC);
        return task;
    }

    public static OrderTask getAxisParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_AXIS_PARAMS);
        return task;
    }

    public static OrderTask getNormalSlotAdvParams(int slot) {
        ParamsTask task = new ParamsTask();
        task.getNormalSlotAdvParams(slot);
        return task;
    }

    public static OrderTask setNormalSlotAdvParams(@NonNull SlotData slotData) {
        ParamsTask task = new ParamsTask();
        task.setNormalSlotAdvParams(slotData);
        return task;
    }

    public static OrderTask setAxisParams(@IntRange(from = 0, to = 4) int rate,
                                          @IntRange(from = 0, to = 3) int scale,
                                          @IntRange(from = 1, to = 255) int sensitivity) {
        ParamsTask task = new ParamsTask();
        task.setAxisParams(rate, scale, sensitivity);
        return task;
    }

    public static OrderTask setNewPassword(String password) {
        PasswordTask task = new PasswordTask();
        task.setNewPassword(password);
        return task;
    }

    public static OrderTask resetDevice() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_RESET);
        return task;
    }

    public static OrderTask getSensorType() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SENSOR_TYPE);
        return task;
    }

    public static OrderTask getButtonTurnOffEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BUTTON_TURN_OFF_ENABLE);
        return task;
    }

    public static OrderTask setButtonTurnOffEnable(int enable) {
        ParamsTask task = new ParamsTask();
        task.setButtonTurnOffEnable(enable);
        return task;
    }

    public static OrderTask getAdvMode() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_ADV_MODE);
        return task;
    }

    public static OrderTask setAdvMode(int advMode) {
        ParamsTask task = new ParamsTask();
        task.setAdvMode(advMode);
        return task;
    }

    public static OrderTask getVerifyPasswordEnable() {
        PasswordTask task = new PasswordTask();
        task.setData(ParamsKeyEnum.KEY_VERIFY_PASSWORD_ENABLE);
        return task;
    }

    public static OrderTask getTriggerLedStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TRIGGER_LED_ENABLE);
        return task;
    }

    public static OrderTask getBuzzerFrequency() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BUZZER_FREQUENCY);
        return task;
    }

    public static OrderTask getConnectStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_CONNECT_ENABLE);
        return task;
    }

    public static OrderTask getAoaCteStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_AOA_CTE_ENABLE);
        return task;
    }

    public static OrderTask getTagIdAutoFillStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TAG_ID_AUTO_FILL_ENABLE);
        return task;
    }

    public static OrderTask getResetByButtonEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BUTTON_RESET_ENABLE);
        return task;
    }

    public static OrderTask getBattery() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BATTERY_VOLTAGE);
        return task;
    }

    public static OrderTask getTHHistoryCount(){
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TH_HISTORY_COUNT);
        return task;
    }

    public static OrderTask setVerifyPasswordEnable(@IntRange(from = 0, to = 1) int enable) {
        PasswordTask task = new PasswordTask();
        task.setVerifyPasswordEnable(enable);
        return task;
    }

    public static OrderTask setPassword(String password) {
        PasswordTask task = new PasswordTask();
        task.setPassword(password);
        return task;
    }

    public static OrderTask getAllSlotAdvType() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_ALL_SLOT_ADV_TYPE);
        return task;
    }

    public static OrderTask getSlotTriggerType(int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotTriggerType(slot);
        return task;
    }

    public static OrderTask setLedRemoteReminder(@IntRange(from = 100, to = 10000) int interval,
                                                 @IntRange(from = 10, to = 6000) int time) {
        ParamsTask task = new ParamsTask();
        task.setLedRemoteReminder(interval, time);
        return task;
    }

    public static OrderTask setBuzzerRemoteReminder(@IntRange(from = 100, to = 10000) int interval,
                                                    @IntRange(from = 10, to = 6000) int time) {
        ParamsTask task = new ParamsTask();
        task.setBuzzerRemoteReminder(interval, time);
        return task;
    }

    public static OrderTask setConnectStatus(int status) {
        ParamsTask task = new ParamsTask();
        task.setStatus(status, ParamsKeyEnum.KEY_CONNECT_ENABLE);
        return task;
    }

    public static OrderTask setAoaCteStatus(int status) {
        ParamsTask task = new ParamsTask();
        task.setStatus(status, ParamsKeyEnum.KEY_AOA_CTE_ENABLE);
        return task;
    }

    public static OrderTask setTriggerIndicatorStatus(int status) {
        ParamsTask task = new ParamsTask();
        task.setStatus(status, ParamsKeyEnum.KEY_TRIGGER_LED_ENABLE);
        return task;
    }

    public static OrderTask setBuzzerFrequency(int frequency) {
        ParamsTask task = new ParamsTask();
        task.setBuzzerFrequency(frequency);
        return task;
    }

    public static OrderTask setTagIdAutoFill(int status) {
        ParamsTask task = new ParamsTask();
        task.setStatus(status, ParamsKeyEnum.KEY_TAG_ID_AUTO_FILL_ENABLE);
        return task;
    }

    public static OrderTask setResetByButton(int status) {
        ParamsTask task = new ParamsTask();
        task.setStatus(status, ParamsKeyEnum.KEY_BUTTON_RESET_ENABLE);
        return task;
    }

    public static OrderTask clearMagneticTriggerCount() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MAGNETIC_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask setCurrentTime() {
        ParamsTask task = new ParamsTask();
        task.setCurrentTime();
        return task;
    }

    public static OrderTask clearHistoryTHData() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_CLEAR_HISTORY_TH);
        return task;
    }

    public static OrderTask setSlotAdvParamsBefore(@NonNull SlotData beforeBean) {
        ParamsTask task = new ParamsTask();
        task.setSlotAdvParamsBefore(beforeBean);
        return task;
    }

    public static OrderTask setSlotAdvParamsAfter(@NonNull SlotData afterBean) {
        ParamsTask task = new ParamsTask();
        task.setSlotAdvParamsAfter(afterBean);
        return task;
    }

    public static OrderTask getTriggerBeforeSlotParams(@IntRange(from = 0, to = 2) int slot) {
        ParamsTask task = new ParamsTask();
        task.getTriggerBeforeSlotParams(slot);
        return task;
    }

    public static OrderTask getTriggerAfterSlotParams(@IntRange(from = 0, to = 2) int slot) {
        ParamsTask task = new ParamsTask();
        task.getTriggerAfterSlotParams(slot);
        return task;
    }

    public static OrderTask setSlotTriggerType(@NonNull TriggerStep1Bean triggerBean) {
        ParamsTask task = new ParamsTask();
        task.setSlotTriggerType(triggerBean);
        return task;
    }
}
