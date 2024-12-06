package com.moko.support.s.entity;

import java.util.UUID;

public enum OrderServices {
    SERVICE_CUSTOM(UUID.fromString("0000AA00-0000-1000-8000-00805F9B34FB")),
    SERVICE_OTA(UUID.fromString("1D14D6EE-FD63-4FA1-BFA4-8F47B42119F0")),
    ;
    private final UUID uuid;

    OrderServices(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
