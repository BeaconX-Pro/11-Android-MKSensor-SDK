package com.moko.bxp.s.entity;

import java.io.Serializable;

public class AdvInfo implements Serializable {
    public String name;
    public int rssi;
    public String mac;
    public String scanRecord;
    public int battery;
    public long intervalTime;
    public long scanTime;
    public int txPower;
    public boolean connectable;
    public String productMhz;
    public String productTxPower;
    public int productAdvInterval;
    public String deviceInfoMhz;
    public String deviceInfoTxPower;
    public int deviceInfoAdvInterval;
    public int temperature;
    public int alarmCount;
    public String alarmStatus;
    public int advType;
    public int batterPercent;
}
