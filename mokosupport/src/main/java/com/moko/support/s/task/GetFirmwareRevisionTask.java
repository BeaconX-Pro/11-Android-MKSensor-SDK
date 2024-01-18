package com.moko.support.s.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.s.entity.OrderCHAR;

public class GetFirmwareRevisionTask extends OrderTask {

    public byte[] data;

    public GetFirmwareRevisionTask() {
        super(OrderCHAR.CHAR_FIRMWARE_REVISION, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
