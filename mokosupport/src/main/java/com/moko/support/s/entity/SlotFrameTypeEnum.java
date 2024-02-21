package com.moko.support.s.entity;

import java.io.Serializable;

public enum SlotFrameTypeEnum implements Serializable {
    TLM("TLM", "20"),
    UID("UID", "00"),
    URL("URL", "10"),
    IBEACON("iBeacon", "50"),
    SENSOR_INFO("Sensor info", "80"),
    TH("T&H_INFO", "70"),
    NO_DATA("No data", "FF"),
    ;
    private String frameType;
    private String showName;

    SlotFrameTypeEnum(String showName, String frameType) {
        this.frameType = frameType;
        this.showName = showName;
    }


    public String getFrameType() {
        return frameType;
    }

    public String getShowName() {
        return showName;
    }

    public static SlotFrameTypeEnum fromFrameType(int frameType) {
        for (SlotFrameTypeEnum frameTypeEnum : SlotFrameTypeEnum.values()) {
            if (Integer.parseInt(frameTypeEnum.getFrameType(), 16) == frameType) {
                return frameTypeEnum;
            }
        }
        return null;
    }

    public static SlotFrameTypeEnum fromShowName(String showName) {
        for (SlotFrameTypeEnum frameTypeEnum : SlotFrameTypeEnum.values()) {
            if (showName.equals(frameTypeEnum.showName)) {
                return frameTypeEnum;
            }
        }
        return null;
    }
}
