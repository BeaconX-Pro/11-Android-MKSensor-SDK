package com.moko.support.s.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;

import androidx.annotation.IntRange;


public class PasswordTask extends OrderTask {
    public byte[] data;

    public PasswordTask() {
        super(OrderCHAR.CHAR_PASSWORD, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsKeyEnum key) {
        createGetParamsData(key.getParamsKey());
    }

    private void createGetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) 0x00, (byte) paramsKey, (byte) 0x00};
    }

    public void setPassword(String password) {
        byte[] passwordBytes = password.getBytes();
        int length = passwordBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_PASSWORD.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = passwordBytes[i];
        }
        response.responseValue = data;
    }

    public void setNewPassword(String password) {
        byte[] passwordBytes = password.getBytes();
        int length = passwordBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_MODIFY_PASSWORD.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = passwordBytes[i];
        }
        response.responseValue = data;
    }

    public void setVerifyPasswordEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_VERIFY_PASSWORD_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }
}
