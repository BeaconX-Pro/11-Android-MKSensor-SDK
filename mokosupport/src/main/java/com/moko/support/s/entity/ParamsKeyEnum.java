package com.moko.support.s.entity;

import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    KEY_DEVICE_MAC(0x20),
    KEY_AXIS_PARAMS(0x21),
    KEY_BUTTON_RESET_ENABLE(0x23),
    KEY_BUTTON_TURN_OFF_ENABLE(0x25),
    KEY_RESET(0x28),
    KEY_ALL_SLOT_ADV_TYPE(0x30),
    KEY_SLOT_TRIGGER_TYPE(0x31),
    KEY_SLOT_PARAMS_BEFORE(0x32),
    KEY_SLOT_PARAMS_AFTER(0x33),
    KEY_NORMAL_SLOT_ADV_PARAMS(0x34),
    KEY_ADV_CHANNEL(0x35),
    KEY_AOA_CTE_ENABLE(0x36),
    KEY_CONNECT_ENABLE(0x37),
    KEY_ADV_MODE(0x3A),
    KEY_TAG_ID_AUTO_FILL_ENABLE(0x3C),
    KEY_SYNC_CURRENT_TIME(0x3F),
    KEY_TH_STORE(0x40),
    KEY_TH_SAMPLE_RATE(0x41),
    KEY_CLEAR_HISTORY_TH(0x42),
    KEY_SENSOR_TYPE(0x4A),
    KEY_PASSWORD(0x51),
    KEY_MODIFY_PASSWORD(0x52),
    KEY_VERIFY_PASSWORD_ENABLE(0x53),
    KEY_LED_REMOTE_REMINDER(0x61),
    KEY_BUZZER_REMOTE_REMINDER(0x62),
    KEY_TRIGGER_LED_ENABLE(0x65),
    KEY_MAGNETIC_TRIGGER_COUNT(0x68),
    KEY_MOTION_TRIGGER_COUNT(0x69),
    KEY_BATTERY_VOLTAGE(0x6A),
    KEY_BATTERY_PERCENT(0x6B),
    KEY_BATTERY_MODE(0x6C),
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
