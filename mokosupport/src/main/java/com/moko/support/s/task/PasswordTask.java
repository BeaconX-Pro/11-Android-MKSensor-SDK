package com.moko.support.s.task;

import androidx.annotation.IntRange;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;

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
        System.arraycopy(passwordBytes, 0, data, 4, length);
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
        System.arraycopy(passwordBytes, 0, data, 4, length);
        response.responseValue = data;
    }

    public void setVerifyPasswordEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_VERIFY_PASSWORD_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }
}
