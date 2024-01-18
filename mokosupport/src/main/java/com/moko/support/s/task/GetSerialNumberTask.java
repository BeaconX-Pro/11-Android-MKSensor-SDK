package com.moko.support.s.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.s.entity.OrderCHAR;


public class GetSerialNumberTask extends OrderTask {

    public byte[] data;

    public GetSerialNumberTask() {
        super(OrderCHAR.CHAR_SERIAL_NUMBER, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
