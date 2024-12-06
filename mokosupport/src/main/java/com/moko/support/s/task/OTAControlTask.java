package com.moko.support.s.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.s.entity.OrderCHAR;

public class OTAControlTask extends OrderTask {
    public byte[] data;

    public OTAControlTask() {
        super(OrderCHAR.CHAR_OTA_CONTROL, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }


    public void startDFU() {
        data = new byte[]{(byte) 0x00};
        response.responseValue = data;
    }

    public void endDFU() {
        data = new byte[]{(byte) 0x03};
        response.responseValue = data;
    }
}
