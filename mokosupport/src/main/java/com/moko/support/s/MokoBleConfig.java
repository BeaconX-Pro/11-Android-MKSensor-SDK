package com.moko.support.s;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
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

final class MokoBleConfig extends MokoBleManager {

    private MokoResponseCallback mMokoResponseCallback;
    private BluetoothGattCharacteristic paramsCharacteristic;
    private BluetoothGattCharacteristic disconnectCharacteristic;
    private BluetoothGattCharacteristic accCharacteristic;
    private BluetoothGattCharacteristic passwordCharacteristic;

    public MokoBleConfig(@NonNull Context context, MokoResponseCallback callback) {
        super(context);
        mMokoResponseCallback = callback;
    }

    @Override
    public boolean init(BluetoothGatt gatt) {
        final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
        if (service != null) {
            paramsCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid());
            disconnectCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid());
            accCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_ACC.getUuid());
            passwordCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_PASSWORD.getUuid());
            enableParamsNotify();
            enableDisconnectNotify();
            enablePasswordNotify();
            return true;
        }
        return false;
    }

    @Override
    public void write(BluetoothGattCharacteristic characteristic, byte[] value) {
    }

    @Override
    public void read(BluetoothGattCharacteristic characteristic, byte[] value) {
        mMokoResponseCallback.onCharacteristicRead(characteristic, value);
    }

    @Override
    public void discovered(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID lastCharacteristicUUID = characteristic.getUuid();
        if (passwordCharacteristic.getUuid().equals(lastCharacteristicUUID))
            mMokoResponseCallback.onServicesDiscovered(gatt);
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
}