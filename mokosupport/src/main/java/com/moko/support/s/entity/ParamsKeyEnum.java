package com.moko.support.s.entity;

import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    KEY_DEVICE_MAC(0x20),
    KEY_AXIS_PARAMS(0x21),
    KEY_RESET(0x28),
    KEY_TRIGGER_LED_STATUS(0x30),
    KEY_BATTERY_VOLTAGE(0x31),
    KEY_NORMAL_ADV_PARAMS(0x35),
    KEY_BUTTON_TRIGGER_PARAMS(0x36),
    KEY_POWER_SAVING_STATIC_TRIGGER_TIME(0x37),
    KEY_SENSOR_TYPE(0x39),
    KEY_PASSWORD(0x51),
    KEY_MODIFY_PASSWORD(0x52),
    KEY_VERIFY_PASSWORD_ENABLE(0x53),

    KEY_ALL_SLOT_ADV_TYPE(0x6C),
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
