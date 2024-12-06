package com.moko.support.s.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.OrderServices;

import java.util.HashMap;

public class MokoCharacteristicHandler {
    private HashMap<OrderCHAR, BluetoothGattCharacteristic> mCharacteristicMap;

    public MokoCharacteristicHandler() {
        //no instance
        mCharacteristicMap = new HashMap<>();
    }

    public HashMap<OrderCHAR, BluetoothGattCharacteristic> getCharacteristics(final BluetoothGatt gatt) {
        if (mCharacteristicMap != null && !mCharacteristicMap.isEmpty()) {
            mCharacteristicMap.clear();
        }
        if (gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_PARAMS, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_DISCONNECT, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_PASSWORD.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_PASSWORD.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_PASSWORD, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_ACC.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_ACC.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_ACC, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_TH_HISTORY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_TH_HISTORY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_TH_HISTORY, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_TH_NOTIFY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_TH_NOTIFY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_TH_NOTIFY, characteristic);
            }
        }
        if (gatt.getService(OrderServices.SERVICE_OTA.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_OTA.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_OTA_CONTROL.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_OTA_CONTROL.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_OTA_CONTROL, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_OTA_DATA.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_OTA_DATA.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_OTA_DATA, characteristic);
            }
        }
        return mCharacteristicMap;
    }
}
