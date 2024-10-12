package com.moko.support.s.entity;

import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    KEY_DEVICE_MAC(0x20),
    KEY_AXIS_PARAMS(0x21),
    KEY_SLOT_ADV_PARAMS(0x22),
    KEY_HALL_POWER_ENABLE(0x25),
    KEY_CONNECT_ENABLE(0x27),
    KEY_RESET(0x28),
    KEY_RESET_BY_BUTTON_ENABLE(0x29),

    KEY_DEVICE_TYPE(0x2F),
    KEY_ALL_SLOT_ADV_TYPE(0x30),
    KEY_SLOT_TRIGGER_TYPE(0x31),
    KEY_TEMP_TRIGGER_EVENT(0x31A),
    KEY_HUM_TRIGGER_EVENT(0x32A),
    KEY_SLOT_PARAMS_BEFORE(0x32),
    KEY_SLOT_PARAMS_AFTER(0x33),
    KEY_AXIS_TRIGGER_EVENT(0x33A),
    KEY_NORMAL_SLOT_ADV_PARAMS(0x34),

    KEY_ADV_CHANNEL(0x35),
    KEY_MAGNETIC_TRIGGER_COUNT(0x36),
    KEY_MOTION_TRIGGER_COUNT(0x37),

    KEY_TH_STORE(0x40),
    KEY_TH_SAMPLE_RATE(0x41),
    KEY_CLEAR_HISTORY_TH(0x42),
    KEY_SYNC_CURRENT_TIME(0x43),

    KEY_PASSWORD(0x51),
    KEY_MODIFY_PASSWORD(0x52),
    KEY_VERIFY_PASSWORD_ENABLE(0x53),

    KEY_ADV_MODE(0x3A),
    KEY_REMOTE_REMINDER(0x64),
    KEY_TAG_ID_AUTO_FILL_ENABLE(0x65),
    KEY_BATTERY_VOLTAGE(0x66),
    KEY_SENSOR_TYPE(0x67),
    KEY_TRIGGER_LED_ENABLE(0x6A),
    KEY_BATTERY_MODE(0x6C),
    KEY_HALL_STORE_ENABLE(0x6D),
    KEY_HALL_HISTORY_DATA(0x6E),
    KEY_CLEAR_HISTORY_HALL(0x6F),
    ;

    private final int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
