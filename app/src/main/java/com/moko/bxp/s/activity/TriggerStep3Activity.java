package com.moko.bxp.s.activity;

import static com.moko.bxp.s.utils.SlotAdvType.HALL_TRIGGER;
import static com.moko.bxp.s.utils.SlotAdvType.HUM_TRIGGER;
import static com.moko.bxp.s.utils.SlotAdvType.I_BEACON;
import static com.moko.bxp.s.utils.SlotAdvType.MOTION_TRIGGER;
import static com.moko.bxp.s.utils.SlotAdvType.SENSOR_INFO;
import static com.moko.bxp.s.utils.SlotAdvType.TEMP_TRIGGER;
import static com.moko.bxp.s.utils.SlotAdvType.TH;
import static com.moko.bxp.s.utils.SlotAdvType.TLM;
import static com.moko.bxp.s.utils.SlotAdvType.UID;
import static com.moko.bxp.s.utils.SlotAdvType.URL;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityTriggerStep3Binding;
import com.moko.bxp.s.dialog.AlertMessageDialog;
import com.moko.bxp.s.dialog.BottomDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.entity.TriggerStep1Bean;
import com.moko.bxp.s.entity.TriggerStep2Bean;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: jun.liu
 * @date: 2024/1/30 17:29
 * @des: 配置触发后的一些参数
 */
public class TriggerStep3Activity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private ActivityTriggerStep3Binding mBind;
    private boolean mReceiverTag;
    private int slot;
    private TriggerStep1Bean step1Bean;
    private TriggerStep2Bean step2Bean;
    private TriggerStep1Bean step3Bean;
    private boolean isC112;
    private final String[] frameTypeArray = {"UID", "URL", "TLM", "iBeacon", "Sensor info", "T&H"};
    private final String[] urlSchemeArray = {"http://www.", "https://www.", "http://", "https://"};
    private int urlSchemeSelect;
    private int frameTypeSelected;
    private final int[] txPowerC112 = {-20, -16, -12, -8, -4, 0};
    private final int[] txPower = {-20, -16, -12, -8, -4, 0, 3, 4, 6};
    private byte[] advBytes;
    private int currentIndex;
    private int currentFrameType;
    private EditText et1;
    private EditText et2;
    private EditText et3;
    private boolean isParamsError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityTriggerStep3Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        slot = getIntent().getIntExtra("slot", 0);
        step1Bean = getIntent().getParcelableExtra("step1");
        step2Bean = getIntent().getParcelableExtra("step2");
        isC112 = getIntent().getBooleanExtra("c112", false);
        mBind.tvTitle.setText("SLOT" + (slot + 1));

        setListener();
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.getSlotAdvParams(slot + 3));
            orderTasks.add(OrderTaskAssembler.getTriggerAfterSlotParams(slot + 3));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
        if (isC112) {
            mBind.tvTxPowerSelect.setText("(-20, -16, -12, -8, -4, 0)");
            mBind.sbTxPower.setMax(5);
        }
    }

    private void setListener() {
        mBind.tvFrameType.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(frameTypeArray)), frameTypeSelected);
            dialog.setListener(value -> {
                frameTypeSelected = value;
                mBind.tvFrameType.setText(frameTypeArray[value]);
                setSlotParams(value, value == currentIndex ? advBytes : null);
            });
            dialog.show(getSupportFragmentManager());
        });
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        mBind.btnDone.setOnClickListener(v -> {
            step3Bean = new TriggerStep1Bean();
            step3Bean.frameType = currentFrameType;
            if (currentFrameType == 0x00) {
                //uid
                if (TextUtils.isEmpty(et1.getText()) || TextUtils.isEmpty(et2.getText())) {
                    ToastUtils.showToast(this, "Data format incorrect!");
                    return;
                }
                step3Bean.namespaceId = et1.getText().toString();
                step3Bean.instanceId = et2.getText().toString();
                if (step3Bean.namespaceId.length() != 20 || step3Bean.instanceId.length() != 12) {
                    ToastUtils.showToast(this, "Data format incorrect!");
                    return;
                }
            } else if (currentFrameType == 0x10) {
                //url
                if (TextUtils.isEmpty(et1.getText())) {
                    ToastUtils.showToast(this, "Data format incorrect!");
                    return;
                }
                step3Bean.urlScheme = urlSchemeSelect;
                step3Bean.url = et1.getText().toString();
            } else if (currentFrameType == 0x50) {
                //iBeacon
                if (TextUtils.isEmpty(et1.getText()) || TextUtils.isEmpty(et2.getText()) || TextUtils.isEmpty(et3.getText())) {
                    ToastUtils.showToast(this, "Data format incorrect!");
                    return;
                }
                step3Bean.major = Integer.parseInt(et1.getText().toString());
                step3Bean.minor = Integer.parseInt(et2.getText().toString());
                if (step3Bean.major > 65535 || step3Bean.minor > 65535 || step3Bean.uuid.length() != 32) {
                    ToastUtils.showToast(this, "Data format incorrect!");
                    return;
                }
                step3Bean.uuid = et3.getText().toString();
            } else if (currentFrameType == 0x80) {
                //sensor info
                if (TextUtils.isEmpty(et1.getText()) || TextUtils.isEmpty(et2.getText())) {
                    ToastUtils.showToast(this, "Data format incorrect!");
                    return;
                }
                step3Bean.deviceName = et1.getText().toString();
                step3Bean.tagId = et2.getText().toString();
            }
            //下发参数配置协议
            if (TextUtils.isEmpty(mBind.etAdvInterval.getText()) || TextUtils.isEmpty(mBind.etAdvDuration.getText())) {
                ToastUtils.showToast(this, "Data format incorrect!");
                return;
            }
            int interval = Integer.parseInt(mBind.etAdvInterval.getText().toString());
            int duration = Integer.parseInt(mBind.etAdvDuration.getText().toString());
            if (interval < 1 || interval > 100 || duration > 65535) {
                ToastUtils.showToast(this, "Data format incorrect!");
                return;
            }
            step3Bean.advInterval = interval * 100;
            step3Bean.advDuration = duration;
            step3Bean.rssi = mBind.sbRssi.getProgress() - 100;
            if (isC112) {
                step3Bean.txPower = txPowerC112[mBind.sbTxPower.getProgress()];
            } else {
                step3Bean.txPower = txPower[mBind.sbTxPower.getProgress()];
            }
            sendParams();
        });
    }

    private void sendParams() {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        //先下发第一步的参数
        //根据类型下发参数
        isParamsError = false;
        if (!step1Bean.advBeforeTrigger) {
            orderTasks.add(OrderTaskAssembler.setSlotParamsNoData(slot));
        } else {
            if (step1Bean.frameType == UID) {
                orderTasks.add(OrderTaskAssembler.setSlotParamsUID(slot, step1Bean.namespaceId, step1Bean.instanceId));
            } else if (step1Bean.frameType == URL) {
                orderTasks.add(OrderTaskAssembler.setSlotParamsURL(slot, step1Bean.urlScheme, step1Bean.url));
            } else if (step1Bean.frameType == TLM) {
                orderTasks.add(OrderTaskAssembler.setSlotParamsTLM(slot));
            } else if (step1Bean.frameType == I_BEACON) {
                orderTasks.add(OrderTaskAssembler.setSlotParamsIBeacon(slot, step1Bean.major, step1Bean.minor, step1Bean.uuid));
            } else if (step1Bean.frameType == SENSOR_INFO) {
                orderTasks.add(OrderTaskAssembler.setSlotParamsTagInfo(slot, step1Bean.deviceName, step1Bean.tagId));
            } else if (step1Bean.frameType == TH) {
                orderTasks.add(OrderTaskAssembler.setSlotParamsTH(slot));
            }
//            if (step1Bean.isLowPowerMode) {
            orderTasks.add(OrderTaskAssembler.setSlotAdvParamsBefore(slot, step1Bean.advInterval, step1Bean.advDuration,
                    step1Bean.standByDuration, step1Bean.rssi, step1Bean.txPower));
//            } else {
//                orderTasks.add(OrderTaskAssembler.setSlotAdvParamsBefore(slot, step1Bean.advInterval, 1, 0, 0, step1Bean.txPower));
//            }
        }
        //下发第二步触发类型的参数
        orderTasks.add(OrderTaskAssembler.setSlotTriggerType(slot, step2Bean.triggerType));
        if (step2Bean.triggerType == TEMP_TRIGGER) {
            //温度触发
            orderTasks.add(OrderTaskAssembler.setTempTriggerEvent(slot, step2Bean.triggerEventSelect, step2Bean.tempThreshold));
        } else if (step2Bean.triggerType == HUM_TRIGGER) {
            //湿度触发
            orderTasks.add(OrderTaskAssembler.setHumTriggerEvent(slot, step2Bean.triggerEventSelect, step2Bean.humThreshold));
        } else if (step2Bean.triggerType == MOTION_TRIGGER) {
            //移动触发
            orderTasks.add(OrderTaskAssembler.setAxisTriggerEvent(slot, step2Bean.triggerEventSelect, step2Bean.axisStaticPeriod));
        } else if (step2Bean.triggerType == HALL_TRIGGER) {
            //霍尔触发
            orderTasks.add(OrderTaskAssembler.setHallTriggerEvent(slot, step2Bean.triggerEventSelect));
        }
        //3、配置触发后的参数
        if (step3Bean.frameType == UID) {
            orderTasks.add(OrderTaskAssembler.setSlotParamsUID(slot + 3, step3Bean.namespaceId, step3Bean.instanceId));
        } else if (step3Bean.frameType == URL) {
            orderTasks.add(OrderTaskAssembler.setSlotParamsURL(slot + 3, step3Bean.urlScheme, step3Bean.url));
        } else if (step3Bean.frameType == TLM) {
            orderTasks.add(OrderTaskAssembler.setSlotParamsTLM(slot + 3));
        } else if (step3Bean.frameType == I_BEACON) {
            orderTasks.add(OrderTaskAssembler.setSlotParamsIBeacon(slot + 3, step3Bean.major, step3Bean.minor, step3Bean.uuid));
        } else if (step3Bean.frameType == SENSOR_INFO) {
            orderTasks.add(OrderTaskAssembler.setSlotParamsTagInfo(slot + 3, step3Bean.deviceName, step3Bean.tagId));
        } else if (step3Bean.frameType == TH) {
            orderTasks.add(OrderTaskAssembler.setSlotParamsTH(slot + 3));
        }
        orderTasks.add(OrderTaskAssembler.setSlotAdvParamsAfter(slot + 3, step3Bean.advInterval, step3Bean.advDuration,
                step3Bean.rssi, step3Bean.txPower));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 500)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 500)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length > 4) {
                        int header = value[0] & 0xFF;// 0xEB
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xEB) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_SLOT_ADV_PARAMS:
                                    if (length > 1) {
                                        //4 5 6通道的广播参数
                                        advBytes = value;
                                        int slotType = value[5] & 0xff;
                                        frameTypeSelected = getSlotIndex(slotType);
                                        currentIndex = frameTypeSelected;
                                        mBind.tvFrameType.setText(frameTypeArray[frameTypeSelected]);
                                        //非no data
                                        setSlotParams(frameTypeSelected, value);
                                    }
                                    break;

                                case KEY_SLOT_PARAMS_AFTER:
                                    if (length == 7) {
                                        int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                        int duration = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                        mBind.etAdvInterval.setText(String.valueOf(interval / 100));
                                        mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().length());
                                        mBind.etAdvDuration.setText(String.valueOf(duration));
                                        mBind.etAdvDuration.setSelection(mBind.etAdvDuration.getText().length());
                                        int rssi = value[9];
                                        mBind.sbRssi.setProgress(rssi + 100);
                                        mBind.tvRssi.setText(rssi + "dBm");
                                        int txPower = value[10];
                                        int index;
                                        if (isC112) {
                                            index = txPowerC112[txPower];
                                        } else {
                                            index = this.txPower[txPower];
                                        }
                                        mBind.sbTxPower.setProgress(index);
                                        mBind.tvTxPower.setText(txPower + "dBm");
                                    }
                                    break;
                            }
                        } else if (flag == 1) {
                            switch (configKeyEnum) {
                                case KEY_SLOT_ADV_PARAMS:
                                case KEY_SLOT_PARAMS_BEFORE:
                                case KEY_SLOT_TRIGGER_TYPE:
                                case KEY_TEMP_TRIGGER_EVENT:
                                case KEY_HUM_TRIGGER_EVENT:
                                case KEY_AXIS_TRIGGER_EVENT:
                                case KEY_HALL_TRIGGER_EVENT:
                                    if ((value[4] & 0xff) != 0xAA) isParamsError = true;
                                    break;
                                case KEY_SLOT_PARAMS_AFTER:
                                    if ((value[4] & 0xff) != 0xAA) isParamsError = true;
                                    if (isParamsError) {
                                        ToastUtils.showToast(this, "set up fail");
                                    } else {
                                        showTips();
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void showTips() {
        String tips = "";
        if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 1 && !step1Bean.advBeforeTrigger && step3Bean.advDuration > 0) {
            tips = "*The Beacon will start advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after door open, and stop advertising immediately when door is closed.";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 1 && !step1Bean.advBeforeTrigger && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after device moves, and stop advertising immediately when door is closed.";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 0 && !step1Bean.advBeforeTrigger && step3Bean.advDuration > 0) {
            tips = "*The Beacon will start advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms  after door is closed, and stop advertising immediately when door is opened.";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 0 && !step1Bean.advBeforeTrigger && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms  after door is closed, and stop advertising immediately when door is opened.";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step3Bean.advDuration > 0 && step1Bean.standByDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after door open, and advertising for " +
                    step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when door is closed";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step3Bean.advDuration > 0 && step1Bean.standByDuration == 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after door open, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when door is closed";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step3Bean.advDuration == 0 && step1Bean.standByDuration > 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after door open, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when door is closed";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step3Bean.advDuration == 0 && step1Bean.standByDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after door open, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when door is closed";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after door is closed, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when door is opened.";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration > 0) {
            tips = " *The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after door is closed, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when door is opened.";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration == 0) {
            tips = " *The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after door is closed, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when door is opened.";
        } else if (step2Bean.triggerType == 4 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after door is closed, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when door is opened.";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 0 && !step1Bean.advBeforeTrigger && step3Bean.standByDuration > 0) {
            tips = "*The Beacon will start advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after device moves, and stop advertising immediately after device keep stationary for " + step2Bean.axisStaticPeriod + "s";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 0 && !step1Bean.advBeforeTrigger && step3Bean.standByDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after device moves, and stop advertising immediately after device keep stationary for " + step2Bean.axisStaticPeriod + "s";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 1 && !step1Bean.advBeforeTrigger && step3Bean.standByDuration > 0) {
            tips = "*The Beacon will start advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after device keep stationary for " + step2Bean.axisStaticPeriod + "s, and stop advertising immediately when device moves.";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 1 && !step1Bean.advBeforeTrigger && step3Bean.standByDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after device keep stationary for " + step2Bean.axisStaticPeriod + "s, and stop advertising immediately when device moves.";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after device moves, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device keep stationary for " + step2Bean.axisStaticPeriod + "s";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after device moves, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device keep stationary for " + step2Bean.axisStaticPeriod + "s";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after device moves, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device keep stationary for " + step2Bean.axisStaticPeriod + "s";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after device moves, and keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device keep stationary for " + step2Bean.axisStaticPeriod + "s";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device keep stationary for " + step2Bean.axisStaticPeriod + "s, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms after device moves";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device keep stationary for " + step2Bean.axisStaticPeriod + "s, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms after device moves";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device keep stationary for " + step2Bean.axisStaticPeriod + "s, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms after device moves";
        } else if (step2Bean.triggerType == 3 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device keep stationary for " + step2Bean.axisStaticPeriod + "s, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms after device moves";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 0 && !step1Bean.advBeforeTrigger && step3Bean.advDuration > 0) {
            tips = "*The Beacon will start advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after device temperature is more than or equal to " + step2Bean.tempThreshold + "℃, and stop advertising immediately after device temperature is less than " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 0 && !step1Bean.advBeforeTrigger && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after device temperature is more than or equal to " + step2Bean.tempThreshold + "℃, and stop advertising immediately after device temperature is less than " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 1 && !step1Bean.advBeforeTrigger && step3Bean.advDuration > 0) {
            tips = "*The Beacon will start advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after device temperature is less than " + step2Bean.tempThreshold + "℃, and stop advertising immediately when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃.";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 1 && !step1Bean.advBeforeTrigger && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after device temperature is less than " + step2Bean.tempThreshold + "℃, and stop advertising immediately when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃.";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device temperature is less than " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device temperature is less than " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device temperature is less than " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device temperature is less than " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device temperature is less than " + step2Bean.tempThreshold + "℃, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device temperature is less than " + step2Bean.tempThreshold + "℃, and keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device temperature is less than " + step2Bean.tempThreshold + "℃, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 1 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device temperature is less than " + step2Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device temperature is more than or equal to " + step2Bean.tempThreshold + "℃";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 0 && !step1Bean.advBeforeTrigger && step3Bean.advDuration > 0) {
            tips = "*The Beacon will start advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after device humidity is more than or equal to " + step2Bean.humThreshold + "%, and stop advertising immediately after device humidity is less than " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 0 && !step1Bean.advBeforeTrigger && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising after at the interval of " + step3Bean.advInterval * 100 + "ms device humidity is more than or equal to " + step2Bean.humThreshold + "%, and stop advertising immediately after device humidity is less than " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 1 && !step1Bean.advBeforeTrigger && step3Bean.advDuration > 0) {
            tips = "*The Beacon will start advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms after device humidity is less than " + step2Bean.humThreshold + "%, and stop advertising immediately when device humidity is more than or equal to " + step2Bean.humThreshold + "%.";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 1 && !step1Bean.advBeforeTrigger && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms after device humidity is less than " + step2Bean.humThreshold + "%, and stop advertising immediately when device humidity is more than or equal to " + step2Bean.humThreshold + "%.";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device humidity is more than or equal to " + step2Bean.humThreshold + "%, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device humidity is less than " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device humidity is more than or equal to " + step2Bean.humThreshold + "%, and keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device humidity is less than " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device humidity is more than or equal to " + step2Bean.humThreshold + "%, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device humidity is less than " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 0 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device humidity is more than or equal to " + step2Bean.humThreshold + "%, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device humidity is less than " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device humidity is less than " + step2Bean.humThreshold + "%, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device humidity is more than or equal to " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration > 0) {
            tips = "*The Beacon will advertising for " + step3Bean.advDuration + "s at the interval of " + step3Bean.advInterval * 100 + "ms when device humidity is less than " + step2Bean.humThreshold + "%, and keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device humidity is more than or equal to " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration > 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device humidity is less than " + step2Bean.humThreshold + "%, and advertising for " + step1Bean.advDuration + "s every " + step1Bean.standByDuration + "s at the interval of " + step1Bean.advInterval * 100 + "ms when device humidity is more than or equal to " + step2Bean.humThreshold + "%";
        } else if (step2Bean.triggerType == 2 && step2Bean.triggerEventSelect == 1 && step1Bean.advBeforeTrigger && step1Bean.standByDuration == 0 && step3Bean.advDuration == 0) {
            tips = "*The Beacon will keep advertising at the interval of " + step3Bean.advInterval * 100 + "ms when device humidity is less than " + step2Bean.humThreshold + "%, and  keep advertising at the interval of " + step1Bean.advInterval * 100 + "ms when device humidity more than or equal to " + step2Bean.humThreshold + "%";
        }
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(tips);
        dialog.setCancelGone();
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            //返回通道页面
            Intent intent = new Intent(this, DeviceInfoActivity.class);
            startActivity(intent);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }

    private int getSlotIndex(int slotType) {
        //"UID", "URL", "TLM", "iBeacon", "Sensor info", "T&H
        currentFrameType = slotType;
        switch (slotType) {
            case 0x00:
                return 0;
            case 0x10:
                return 1;
            case 0x20:
                return 2;
            case 0x50:
                return 3;
            case 0x80:
                return 4;
            case 0x70:
                return 5;
        }
        return 0;
    }

    private void setSlotParams(int index, byte[] value) {
        //"UID", "URL", "TLM", "iBeacon", "Sensor info", "T&H
        mBind.layoutDeviceName.removeAllViews();
        if (index == 0) {
            //uid
            mBind.tvAdvContent.setVisibility(View.VISIBLE);
            currentFrameType = 0x00;
            View view = LayoutInflater.from(this).inflate(R.layout.layout_uid, mBind.layoutDeviceName);
            EditText etNameSpace = view.findViewById(R.id.et_namespace);
            EditText etInstanceId = view.findViewById(R.id.et_instance_id);
            et1 = etNameSpace;
            et2 = etInstanceId;
            if (null != value && value.length > 21) {
                etNameSpace.setText(MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 6, 16)));
                etNameSpace.setSelection(etNameSpace.getText().length());
                etInstanceId.setText(MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 16, 22)));
                etInstanceId.setSelection(etInstanceId.getText().length());
            }
            mBind.layoutDeviceName.addView(view);
        } else if (index == 1) {
            //url
            currentFrameType = 0x10;
            mBind.tvAdvContent.setVisibility(View.VISIBLE);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_url, mBind.layoutDeviceName);
            EditText etUrl = view.findViewById(R.id.et_url);
            TextView tvUrlScheme = view.findViewById(R.id.tv_url_scheme);
            tvUrlScheme.setText(urlSchemeArray[urlSchemeSelect]);
            et1 = etUrl;
            if (null != value && value.length > 7) {
                urlSchemeSelect = value[6] & 0xff;
                tvUrlScheme.setText(urlSchemeArray[urlSchemeSelect]);
                String url = new String(Arrays.copyOfRange(value, 7, value.length));
                etUrl.setText(url);
                etUrl.setSelection(etUrl.getText().length());
            }
            mBind.layoutDeviceName.addView(view);
            tvUrlScheme.setOnClickListener(v -> {
                BottomDialog dialog = new BottomDialog();
                dialog.setDatas(new ArrayList<>(Arrays.asList(urlSchemeArray)), urlSchemeSelect);
                dialog.setListener(value1 -> {
                    urlSchemeSelect = value1;
                    tvUrlScheme.setText(urlSchemeArray[urlSchemeSelect]);
                });
                dialog.show(getSupportFragmentManager());
            });
        } else if (index == 2) {
            //tlm
            currentFrameType = 0x20;
            mBind.tvAdvContent.setVisibility(View.GONE);
        } else if (index == 3) {
            //iBeacon
            currentFrameType = 0x50;
            mBind.tvAdvContent.setVisibility(View.VISIBLE);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_ibeacon, mBind.layoutDeviceName);
            EditText etMajor = view.findViewById(R.id.et_major);
            EditText etMinor = view.findViewById(R.id.et_minor);
            EditText etUUid = view.findViewById(R.id.et_uuid);
            et1 = etMajor;
            et2 = etMinor;
            et3 = etUUid;
            if (null != value && value.length > 25) {
                etMajor.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8))));
                etMajor.setSelection(etMajor.getText().length());
                etMinor.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 8, 10))));
                etMinor.setSelection(etMinor.getText().length());
                etUUid.setText(MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 10, value.length)));
                etUUid.setSelection(etUUid.getText().length());
            }
            mBind.layoutDeviceName.addView(view);
        } else if (index == 4) {
            currentFrameType = 0x80;
            mBind.tvAdvContent.setVisibility(View.VISIBLE);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_sensor_info, mBind.layoutDeviceName);
            EditText etDeviceName = view.findViewById(R.id.et_device_name);
            EditText etTagId = view.findViewById(R.id.et_tag_id);
            et1 = etDeviceName;
            et2 = etTagId;
            if (null != value && value.length > 9) {
                int nameLength = value[6] & 0xff;
                etDeviceName.setText(new String(Arrays.copyOfRange(value, 7, 7 + nameLength)));
                etDeviceName.setSelection(etDeviceName.getText().length());
                int idLength = value[7 + nameLength];
                etTagId.setText(MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 8 + nameLength, 8 + nameLength + idLength)));
            }
            mBind.layoutDeviceName.addView(view);
        } else if (index == 5) {
            currentFrameType = 0x70;
            mBind.tvAdvContent.setVisibility(View.GONE);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        dismissSyncProgressDialog();
                        finish();
                    }
                }
            }
        }
    };

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sb_rssi) {
            //rssi
            int rssi = progress - 100;
            mBind.tvRssi.setText(rssi + "dBm");
        } else if (seekBar.getId() == R.id.sb_tx_power) {
            //txPower
            if (isC112) {
                mBind.tvTxPower.setText(txPowerC112[progress] + "dBm");
            } else {
                mBind.tvTxPower.setText(txPower[progress] + "dBm");
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
