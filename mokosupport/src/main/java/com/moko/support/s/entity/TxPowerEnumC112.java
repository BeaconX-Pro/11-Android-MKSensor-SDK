package com.moko.support.s.entity;

import java.io.Serializable;

/**
 * @author: jun.liu
 * @date: 2024/2/5 14:29
 * @des:
 */
public enum TxPowerEnumC112 implements Serializable {
    NEGATIVE_20(-20),
    NEGATIVE_16(-16),
    NEGATIVE_12(-12),
    NEGATIVE_8(-8),
    NEGATIVE_4(-4),
    NEGATIVE_0(0);

    private int txPower;

    TxPowerEnumC112(int txPower) {
        this.txPower = txPower;
    }

    public static TxPowerEnumC112 fromOrdinal(int ordinal) {
        for (TxPowerEnumC112 txPowerEnum : TxPowerEnumC112.values()) {
            if (txPowerEnum.ordinal() == ordinal) {
                return txPowerEnum;
            }
        }
        return null;
    }
    public static TxPowerEnumC112 fromTxPower(int txPower) {
        for (TxPowerEnumC112 txPowerEnum : TxPowerEnumC112.values()) {
            if (txPowerEnum.getTxPower() == txPower) {
                return txPowerEnum;
            }
        }
        return null;
    }

    public int getTxPower() {
        return txPower;
    }
}
