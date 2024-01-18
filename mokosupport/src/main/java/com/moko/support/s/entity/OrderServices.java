package com.moko.support.s.entity;

import java.util.UUID;

public enum OrderServices {
    SERVICE_DEVICE_INFO(UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")),
    SERVICE_CUSTOM(UUID.fromString("0000AA00-0000-1000-8000-00805F9B34FB")),
    SERVICE_ADV_TRIGGER(UUID.fromString("0000FEE0-0000-1000-8000-00805F9B34FB")),
    SERVICE_ADV_DEVICE(UUID.fromString("0000EA00-0000-1000-8000-00805F9B34FB")),
    SERVICE_ADV_PRODUCT_TEST(UUID.fromString("0000EC01-0000-1000-8000-00805F9B34FB")),

    ;
    private final UUID uuid;

    OrderServices(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
