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
    ;

    private final UUID uuid;

    OrderCHAR(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
