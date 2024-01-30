package com.moko.support.s;

import androidx.annotation.IntRange;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.task.GetFirmwareRevisionTask;
import com.moko.support.s.task.GetHardwareRevisionTask;
import com.moko.support.s.task.GetManufacturerNameTask;
import com.moko.support.s.task.GetModelNumberTask;
import com.moko.support.s.task.GetSerialNumberTask;
import com.moko.support.s.task.GetSoftwareRevisionTask;
import com.moko.support.s.task.ParamsTask;
import com.moko.support.s.task.PasswordTask;

public class OrderTaskAssembler {

    /**
     * 获取制造商
     */
    public static OrderTask getManufacturer() {
        return new GetManufacturerNameTask();
    }

    /**
     * 获取设备型号
     */
    public static OrderTask getDeviceModel() {
        return new GetModelNumberTask();
    }

    /**
     * 获取生产日期
     */
    public static OrderTask getProductDate() {
        return new GetSerialNumberTask();
    }

    /**
     * 获取硬件版本
     */
    public static OrderTask getHardwareVersion() {
        return new GetHardwareRevisionTask();
    }

    /**
     * 获取固件版本
     */
    public static OrderTask getFirmwareVersion() {
        return new GetFirmwareRevisionTask();
    }

    public static OrderTask getHistoryHallData(){
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_HALL_HISTORY_DATA);
        return task;
    }

    public static OrderTask getTHStore(){
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TH_STORE);
        return task;
    }

    public static OrderTask setTHStore(@IntRange(from = 0,to = 1) int enable,@IntRange(from = 1,to = 65535) int interval){
        ParamsTask task = new ParamsTask();
        task.setTHStore(enable,interval);
        return task;
    }

    public static OrderTask getTHSampleInterval(){
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TH_SAMPLE_RATE);
        return task;
    }

    public static OrderTask setTHSampleInterval(@IntRange(from = 1,to = 65535) int interval){
        ParamsTask task = new ParamsTask();
        task.setTHSampleInterval(interval);
        return task;
    }

    public static OrderTask getCurrentTime(){
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

    /**
     * 获取软件版本
     */
    public static OrderTask getSoftwareVersion() {
        return new GetSoftwareRevisionTask();
    }

    public static OrderTask getHallStoreEnable(){
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_HALL_STORE_ENABLE);
        return task;
    }

    public static OrderTask getMagneticTriggerCount(){
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_MAGNETIC_TRIGGER_COUNT);
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

    public static OrderTask getPowerSavingStaticTriggerTime() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_POWER_SAVING_STATIC_TRIGGER_TIME);
        return task;
    }

    public static OrderTask setPowerSavingStaticTriggerTime(@IntRange(from = 0, to = 65535) int time) {
        ParamsTask task = new ParamsTask();
        task.setPowerSavingStaticTriggerTime(time);
        return task;
    }

    public static OrderTask getSensorType() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SENSOR_TYPE);
        return task;
    }

    public static OrderTask getHallPowerEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_HALL_POWER_ENABLE);
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

    public static OrderTask getNormalAdvParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_NORMAL_ADV_PARAMS);
        return task;
    }

    public static OrderTask getButtonTriggerParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BUTTON_TRIGGER_PARAMS);
        return task;
    }

    public static OrderTask getTriggerLedStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TRIGGER_LED_STATUS);
        return task;
    }

    public static OrderTask getConnectStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_CONNECT_ENABLE);
        return task;
    }

    public static OrderTask getTagIdAutoFillStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TAG_ID_AUTO_FILL_ENABLE);
        return task;
    }

    public static OrderTask getResetByButtonStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_RESET_BY_BUTTON_ENABLE);
        return task;
    }

    public static OrderTask getTurnOffByButtonStatus() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_HALL_POWER_ENABLE);
        return task;
    }

    public static OrderTask getBattery() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BATTERY_VOLTAGE);
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

    public static OrderTask setNormalAdvParams(int advInterval, int txPower, int advDuration, int standByDuration, int advChannel) {
        ParamsTask task = new ParamsTask();
        task.setNormalAdvParams(advInterval, txPower, advDuration, standByDuration, advChannel);
        return task;
    }

    public static OrderTask setButtonTriggerParams(int advInterval, int txPower, int advDuration, int triggerType) {
        ParamsTask task = new ParamsTask();
        task.setButtonTriggerParams(advInterval, txPower, advDuration, triggerType);
        return task;
    }

    public static OrderTask setTriggerLedStatus(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setTriggerLedStatus(enable);
        return task;
    }


    //新增
    public static OrderTask getAllSlotAdvType(){
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_ALL_SLOT_ADV_TYPE);
        return task;
    }

    public static OrderTask getSlotTriggerType(int slot){
        ParamsTask task = new ParamsTask();
        task.getSlotTriggerType(slot);
        return task;
    }

    public static OrderTask setRemoteReminder(@IntRange(from = 100, to = 10000) int interval,
                                              @IntRange(from = 1, to = 600) int time) {
        ParamsTask task = new ParamsTask();
        task.setRemoteReminder(interval, time);
        return task;
    }

    public static OrderTask setConnectStatus(int status){
        ParamsTask task = new ParamsTask();
        task.setStatus(status,ParamsKeyEnum.KEY_CONNECT_ENABLE);
        return task;
    }

    public static OrderTask setTriggerIndicatorStatus(int status){
        ParamsTask task = new ParamsTask();
        task.setStatus(status,ParamsKeyEnum.KEY_TRIGGER_LED_ENABLE);
        return task;
    }
    
    public static OrderTask setTagIdAutoFill(int status){
        ParamsTask task = new ParamsTask();
        task.setStatus(status, ParamsKeyEnum.KEY_TAG_ID_AUTO_FILL_ENABLE);
        return task;
    }

    public static OrderTask setResetByButton(int status){
        ParamsTask task = new ParamsTask();
        task.setStatus(status, ParamsKeyEnum.KEY_RESET_BY_BUTTON_ENABLE);
        return task;
    }

    public static OrderTask setTurnOffByButton(int status){
        ParamsTask task = new ParamsTask();
        task.setStatus(status, ParamsKeyEnum.KEY_HALL_POWER_ENABLE);
        return task;
    }

    public static OrderTask clearMagneticTriggerCount(){
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MAGNETIC_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask setHallStoreEnable(int enable){
        ParamsTask task = new ParamsTask();
        task.setHallStoreEnable(enable);
        return task;
    }

    public static OrderTask setCurrentTime(){
        ParamsTask task = new ParamsTask();
        task.setCurrentTime();
        return task;
    }

    public static OrderTask clearHallHistory(){
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_CLEAR_HISTORY_HALL);
        return task;
    }

    public static OrderTask clearHistoryTHData(){
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_CLEAR_HISTORY_TH);
        return task;
    }

}
