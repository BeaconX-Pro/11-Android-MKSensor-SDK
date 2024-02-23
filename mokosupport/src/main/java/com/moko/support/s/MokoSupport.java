package com.moko.support.s;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleLib;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.support.s.entity.ExportData;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.handler.MokoCharacteristicHandler;
import com.moko.support.s.task.ParamsTask;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MokoSupport extends MokoBleLib {
    private HashMap<OrderCHAR, BluetoothGattCharacteristic> mCharacteristicMap;

    private static volatile MokoSupport INSTANCE;

    private Context mContext;

    private MokoBleConfig mBleConfig;

    private MokoSupport() {
        //no instance
    }

    public static MokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        super.init(context);
    }


    @Override
    public MokoBleManager getMokoBleManager() {
        mBleConfig = new MokoBleConfig(mContext, this);
        return mBleConfig;
    }

    ///////////////////////////////////////////////////////////////////////////
    // connect
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeviceConnected(BluetoothGatt gatt) {
        mCharacteristicMap = new MokoCharacteristicHandler().getCharacteristics(gatt);
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCONNECTED);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public BluetoothGattCharacteristic getCharacteristic(Enum orderCHAR) {
        return mCharacteristicMap.get(orderCHAR);
    }

    ///////////////////////////////////////////////////////////////////////////
    // order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isCHARNull() {
        if (mCharacteristicMap == null || mCharacteristicMap.isEmpty()) {
            disConnectBle();
            return true;
        }
        return false;
    }

    @Override
    public void orderFinish() {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_FINISH);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderTimeout(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_TIMEOUT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderResult(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_RESULT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public boolean orderResponseValid(BluetoothGattCharacteristic characteristic, OrderTask orderTask) {
        final UUID responseUUID = characteristic.getUuid();
        final OrderCHAR orderCHAR = (OrderCHAR) orderTask.orderCHAR;
        return responseUUID.equals(orderCHAR.getUuid());
    }

    @Override
    public boolean orderNotify(BluetoothGattCharacteristic characteristic, byte[] value) {
        final UUID responseUUID = characteristic.getUuid();
        OrderCHAR orderCHAR = null;
        if (responseUUID.equals(OrderCHAR.CHAR_DISCONNECT.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_DISCONNECT;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_ACC.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_ACC;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_HALL.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_HALL;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_TH_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_TH_NOTIFY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_TH_HISTORY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_TH_HISTORY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_PARAMS.getUuid())) {
            if (null != value && value.length >= 4 && (value[2] & 0xff) == 0x44) {
                return true;
            }
            if (null != value && value.length >= 4 && (value[2] & 0xff) == 0x6E) {
                OrderTaskResponse response = new OrderTaskResponse();
                response.orderCHAR = orderCHAR;
                response.responseValue = value;
                OrderTaskResponseEvent event = new OrderTaskResponseEvent();
                event.setAction(MokoConstants.ACTION_CURRENT_DATA);
                event.setResponse(response);
                EventBus.getDefault().post(event);
                return true;
            }
        }
        if (orderCHAR == null) return false;
        XLog.i(orderCHAR.name());
        OrderTaskResponse response = new OrderTaskResponse();
        response.orderCHAR = orderCHAR;
        response.responseValue = value;
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_CURRENT_DATA);
        event.setResponse(response);
        EventBus.getDefault().post(event);
        return true;
    }

    public void enableHallStatusNotify() {
        if (mBleConfig != null)
            mBleConfig.enableHallStatusNotify();
    }

    public void disableHallStatusNotify() {
        if (mBleConfig != null)
            mBleConfig.disableHallStatusNotify();
    }

    public void enableAccNotify() {
        if (mBleConfig != null)
            mBleConfig.enableAccNotify();
    }

    public void disableAccNotify() {
        if (mBleConfig != null)
            mBleConfig.disableAccNotify();
    }

    public void enableTHNotify() {
        if (null != mBleConfig) {
            mBleConfig.enableTHNotify();
        }
    }

    public void disableTHNotify() {
        if (null != mBleConfig) mBleConfig.disableTHNotify();
    }

    public void enableHistoryThNotify() {
        if (null != mBleConfig) {
            mBleConfig.enableHistoryTHNotify();
        }
    }

    public void disableHistoryThNotify() {
        if (null != mBleConfig) {
            mBleConfig.disableHistoryTHNotify();
        }
    }

    public ArrayList<ExportData> exportSingleEvents;
    public StringBuilder storeSingleEventString;
    public ArrayList<ExportData> exportDoubleEvents;
    public StringBuilder storeDoubleEventString;
    public ArrayList<ExportData> exportLongEvents;
    public StringBuilder storeLongEventString;
}
