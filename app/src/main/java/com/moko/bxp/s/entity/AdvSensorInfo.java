package com.moko.bxp.s.entity;

import java.io.Serializable;


public class AdvSensorInfo implements Serializable {
    public String hallStatus;
    public String motionStatus;
    public boolean isAccEnable;
    public String hallTriggerCount;
    public String motionTriggerCount;
    public String accX;
    public String accY;
    public String accZ;
    public String temp;
    public String hum;
    public boolean tempEnable;
    public boolean humEnable;
}
