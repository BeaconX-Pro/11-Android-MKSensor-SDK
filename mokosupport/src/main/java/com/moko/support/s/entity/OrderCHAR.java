package com.moko.support.s.entity;

import java.io.Serializable;
import java.util.UUID;

public enum OrderCHAR implements Serializable {
    // AA00
    CHAR_PARAMS(UUID.fromString("0000AA01-0000-1000-8000-00805F9B34FB")),
    CHAR_DISCONNECT(UUID.fromString("0000AA02-0000-1000-8000-00805F9B34FB")),
    CHAR_PASSWORD(UUID.fromString("0000AA04-0000-1000-8000-00805F9B34FB")),
    CHAR_ACC(UUID.fromString("0000AA03-0000-1000-8000-00805F9B34FB")),
    CHAR_HALL(UUID.fromString("0000AA05-0000-1000-8000-00805F9B34FB")),
    CHAR_TH_NOTIFY(UUID.fromString("0000AA06-0000-1000-8000-00805F9B34FB")),
    CHAR_TH_HISTORY(UUID.fromString("0000AA09-0000-1000-8000-00805F9B34FB")),

    // OTA
    CHAR_OTA_CONTROL(UUID.fromString("F7BF3564-FB6D-4E53-88A4-5E37E0326063")),
    CHAR_OTA_DATA(UUID.fromString("984227F3-34FC-4045-A5D0-2C581F81A153")),
    ;

    private final UUID uuid;

    OrderCHAR(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
