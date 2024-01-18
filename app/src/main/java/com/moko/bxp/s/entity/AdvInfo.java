package com.moko.bxp.s.entity;

import java.io.Serializable;
import java.util.HashMap;

public class AdvInfo implements Serializable {
    public static final int VALID_DATA_FRAME_TYPE_UID = 0x00;
    public static final int VALID_DATA_FRAME_TYPE_URL = 0x10;
    public static final int VALID_DATA_FRAME_TYPE_TLM = 0x20;
    public static final int VALID_DATA_FRAME_TYPE_IBEACON = 0x50;
    public static final int VALID_DATA_TYPE_IBEACON_APPLE = 0x02;
    public static final int VALID_DATA_FRAME_TYPE_TAG_INFO = 0x80;
    public static final int VALID_DATA_FRAME_TYPE_TH_INFO = 0x70;
    public static final int VALID_DATA_FRAME_TYPE_PRODUCTION_TEST = 0x90;


    public String name;
    public int rssi;
    public String mac;
    public String scanRecord;
    public int battery;
    public long intervalTime;
    public long scanTime;
    public int txPower;
    public int rangingData;
    public int connectState;
    public HashMap<String, ValidData> validDataHashMap;
    public static class ValidData {
        public int type;
        public int txPower;
        public byte[] values;
        public String data;
    }
}
