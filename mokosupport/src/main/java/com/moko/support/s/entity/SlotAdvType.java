package com.moko.support.s.entity;

/**
 * @author: jun.liu
 * @date: 2024/1/18 18:29
 * @des:
 */
public class SlotAdvType {
    public static final int SLOT1 = 0;
    public static final int SLOT2 = 1;
    public static final int SLOT3 = 2;

    public static final int UID = 0x00;
    public static final int URL = 0x10;
    public static final int TLM = 0x20;
    public static final int I_BEACON = 0x50;
    public static final int TH = 0x70;
    public static final int SENSOR_INFO = 0x80;
    public static final int NO_DATA = 0xFF;
    public static final int NO_TRIGGER = 0x00;
    public static final int TEMP_TRIGGER = 0x01;
    public static final int HUM_TRIGGER = 0x02;
    public static final int MOTION_TRIGGER = 0x03;
    public static final int HALL_TRIGGER = 0x04;
    public static final int TEMP_TRIGGER_ABOVE = 0x10;
    public static final int TEMP_TRIGGER_BELOW = 0x11;
    public static final int HUM_TRIGGER_ABOVE = 0x20;
    public static final int HUM_TRIGGER_BELOW = 0x21;
    public static final int MOTION_TRIGGER_MOTION = 0x30;
    public static final int MOTION_TRIGGER_STATIONARY = 0x31;
    public static final int HALL_TRIGGER_AWAY = 0x40;
    public static final int HALL_TRIGGER_NEAR = 0x41;

    public static final String[] SLOT_TYPE_ARRAY = {"UID", "URL", "TLM", "iBeacon", "Sensor info", "No Data"};
    public static final int[] SLOT_TYPE = {UID, URL, TLM, I_BEACON, SENSOR_INFO, NO_DATA};

    public static String getSlotAdvType(int type) {
        switch (type) {
            case 0x00:
                return "UID";
            case 0x10:
                return "URL";
            case 0x20:
                return "TLM";
            case 0x50:
                return "iBeacon";
            case 0x70:
                return "T&H_INFO";
            case 0x80:
                return "Sensor info";
            case 0xFF:
                return "No data";
        }
        return "No data";
    }

    public static int getSlotTypeIndex(int type) {
        //"UID", "URL", "TLM", "iBeacon", "Sensor info", "No Data"
        switch (type) {
            case UID:
                return 0;
            case URL:
                return 1;
            case TLM:
                return 2;
            case I_BEACON:
                return 3;
            case SENSOR_INFO:
                return 4;
            case NO_DATA:
                return 5;
        }
        return 5;
    }
}
