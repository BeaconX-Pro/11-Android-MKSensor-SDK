package com.moko.support.s.entity;


import java.io.Serializable;

public enum SlotEnum implements Serializable {
    SLOT1(0, "SLOT1"),
    SLOT2(1, "SLOT2"),
    SLOT3(2, "SLOT3"),
    SLOT4(3, "SLOT4"),
    SLOT5(4, "SLOT5"),
    SLOT6(5, "SLOT6"),
    ;
    private String title;
    private int slot;

    SlotEnum(int slot, String title) {
        this.slot = slot;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getSlot() {
        return slot;
    }


}
