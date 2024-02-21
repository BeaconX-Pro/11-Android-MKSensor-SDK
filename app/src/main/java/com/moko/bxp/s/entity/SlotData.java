package com.moko.bxp.s.entity;


import com.moko.support.s.entity.SlotEnum;
import com.moko.support.s.entity.SlotFrameTypeEnum;
import com.moko.support.s.entity.UrlSchemeEnum;

import java.io.Serializable;

public class SlotData implements Serializable {
    public SlotEnum slotEnum;
    public SlotFrameTypeEnum frameTypeEnum;
    // iBeacon
    public String iBeaconUUID;
    public String major;
    public String minor;
    public int rssi_1m;
    // URL
    public UrlSchemeEnum urlSchemeEnum;
    public String urlContentHex;
    // UID
    public String namespace;
    public String instanceId;
    // TLM
    // No data
    // Tag
    public String deviceName;
    public String tagId;

    // BaseParam
    public int rssi_0m;
    public int txPower;
    public int advInterval;
    public int advDuration;
    public int standbyDuration;
    public boolean isC112;

    // Trigger
    public int triggerType;
    public int triggerAdvStatus;
    public int triggerAdvDuration;
    public int staticDuration;
}
