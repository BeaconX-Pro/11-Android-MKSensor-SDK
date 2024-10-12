package com.moko.bxp.s;

import androidx.annotation.NonNull;

import com.moko.support.s.entity.SlotData;

public interface ISlotDataAction {
    boolean isValid();

    void sendData();

    void setParams(@NonNull SlotData slotData);

    SlotData getSlotData();
}
