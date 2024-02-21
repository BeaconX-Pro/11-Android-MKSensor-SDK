package com.moko.bxp.s;

import com.moko.support.s.entity.SlotFrameTypeEnum;

public interface ISlotDataAction {
    boolean isValid();

    void sendData();

    void resetParams(SlotFrameTypeEnum currentFrameTypeEnum);
}
