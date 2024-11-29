package com.moko.support.s;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.callback.MokoResponseCallback;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.OrderServices;

import java.util.UUID;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;

final class MokoBleConfig extends MokoBleManager {

    private MokoResponseCallback mMokoResponseCallback;
    private BluetoothGattCharacteristic paramsCharacteristic;
    private BluetoothGattCharacteristic disconnectCharacteristic;
    private BluetoothGattCharacteristic accCharacteristic;
    private BluetoothGattCharacteristic passwordCharacteristic;
    private BluetoothGattCharacteristic hallCharacteristic;
    private BluetoothGattCharacteristic thCharacteristic;
    private BluetoothGattCharacteristic historyTHCharacteristic;
    private BluetoothGatt gatt;

    public MokoBleConfig(@NonNull Context context, MokoResponseCallback callback) {
        super(context);
        mMokoResponseCallback = callback;
    }

    @Override
    public boolean init(BluetoothGatt gatt) {
        final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
        if (service != null) {
            this.gatt = gatt;
            paramsCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid());
            disconnectCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid());
            accCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_ACC.getUuid());
            hallCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_HALL.getUuid());
            thCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_TH_NOTIFY.getUuid());
            historyTHCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_TH_HISTORY.getUuid());
            passwordCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_PASSWORD.getUuid());
            enablePasswordNotify();
            enableParamsNotify();
            enableDisconnectNotify();
            requestMtu(247).done(bluetoothDevice -> {
                mMokoResponseCallback.onServicesDiscovered(gatt);
            }).enqueue();
            return true;
        }
        return false;
    }

    @Override
    public void write(BluetoothGattCharacteristic characteristic, byte[] value) {
        XLog.e("write******************");
    }

    @Override
    public void read(BluetoothGattCharacteristic characteristic, byte[] value) {
        mMokoResponseCallback.onCharacteristicRead(characteristic, value);
    }

    @Override
    public void discovered(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//        UUID lastCharacteristicUUID = characteristic.getUuid();
//        if (passwordCharacteristic.getUuid().equals(lastCharacteristicUUID))
//            mMokoResponseCallback.onServicesDiscovered(gatt);
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
        mMokoResponseCallback.onDeviceDisconnected(device, reason);
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        mMokoResponseCallback.onDeviceDisconnected(device, reason);
    }


    public void enableParamsNotify() {
        setIndicationCallback(paramsCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(paramsCharacteristic, value);
        });
        enableNotifications(paramsCharacteristic).enqueue();
    }

    public void disableParamsNotify() {
        disableNotifications(paramsCharacteristic).enqueue();
    }

    public void enableDisconnectNotify() {
        setIndicationCallback(disconnectCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(disconnectCharacteristic, value);
        });
        enableNotifications(disconnectCharacteristic).enqueue();
    }

    public void disableDisconnectNotify() {
        disableNotifications(disconnectCharacteristic).enqueue();
    }

    public void enablePasswordNotify() {
        setIndicationCallback(passwordCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(passwordCharacteristic, value);
        });
        enableNotifications(passwordCharacteristic).enqueue();
    }

    public void disablePasswordNotify() {
        disableNotifications(passwordCharacteristic).enqueue();
    }

    public void enableAccNotify() {
        setIndicationCallback(accCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(accCharacteristic, value);
        });
        enableNotifications(accCharacteristic).enqueue();
    }

    public void disableAccNotify() {
        disableNotifications(accCharacteristic).enqueue();
    }

    public void enableHallStatusNotify() {
        setIndicationCallback(hallCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(hallCharacteristic, value);
        });
        enableNotifications(hallCharacteristic).enqueue();
    }

    public void disableHallStatusNotify() {
        disableNotifications(hallCharacteristic).enqueue();
    }

    public void enableTHNotify() {
        setIndicationCallback(thCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(thCharacteristic, value);
        });
        enableNotifications(thCharacteristic).enqueue();
    }

    public void disableTHNotify() {
        disableNotifications(thCharacteristic).enqueue();
    }

    public void enableHistoryTHNotify() {
        setIndicationCallback(historyTHCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(historyTHCharacteristic, value);
        });
        enableIndications(historyTHCharacteristic).fail(new FailCallback() {
            @Override
            public void onRequestFailed(@NonNull BluetoothDevice bluetoothDevice, int i) {
                XLog.e("fail**************" + i);
            }
        }).done(new SuccessCallback() {
            @Override
            public void onRequestCompleted(@NonNull BluetoothDevice bluetoothDevice) {
                XLog.e("done***************");
            }
        }).enqueue();


//        enableNotifications(historyTHCharacteristic).fail(new FailCallback() {
//            @Override
//            public void onRequestFailed(@NonNull BluetoothDevice bluetoothDevice, int i) {
//                XLog.e("fail*******************"+i);
//            }
//        }).done(new SuccessCallback() {
//            @Override
//            public void onRequestCompleted(@NonNull BluetoothDevice bluetoothDevice) {
//                XLog.e("done***********************");
//            }
//        }).enqueue();
    }

    public void disableHistoryTHNotify() {
        disableIndications(historyTHCharacteristic).enqueue();
    }
}