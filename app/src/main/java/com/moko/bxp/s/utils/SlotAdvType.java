package com.moko.bxp.s.utils;

/**
 * @author: jun.liu
 * @date: 2024/1/18 18:29
 * @des:
 */
public class SlotAdvType {
    public static String getSlotAdvType(int type){
        switch (type){
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
}
