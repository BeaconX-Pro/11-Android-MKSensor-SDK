package com.moko.bxp.s.activity;

import static com.moko.support.s.entity.SlotAdvType.HALL_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.HALL_TRIGGER_AWAY;
import static com.moko.support.s.entity.SlotAdvType.HALL_TRIGGER_NEAR;
import static com.moko.support.s.entity.SlotAdvType.HUM_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.HUM_TRIGGER_ABOVE;
import static com.moko.support.s.entity.SlotAdvType.HUM_TRIGGER_BELOW;
import static com.moko.support.s.entity.SlotAdvType.I_BEACON;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER_MOTION;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER_STATIONARY;
import static com.moko.support.s.entity.SlotAdvType.NO_DATA;
import static com.moko.support.s.entity.SlotAdvType.SENSOR_INFO;
import static com.moko.support.s.entity.SlotAdvType.TEMP_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.TEMP_TRIGGER_ABOVE;
import static com.moko.support.s.entity.SlotAdvType.TEMP_TRIGGER_BELOW;
import static com.moko.support.s.entity.SlotAdvType.TLM;
import static com.moko.support.s.entity.SlotAdvType.UID;
import static com.moko.support.s.entity.SlotAdvType.URL;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityTriggerStep3Binding;
import com.moko.bxp.s.dialog.AlertMessageDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.fragment.IBeaconFragment;
import com.moko.bxp.s.fragment.SensorInfoFragment;
import com.moko.bxp.s.fragment.TlmFragment;
import com.moko.bxp.s.fragment.UidFragment;
import com.moko.bxp.s.fragment.UrlFragment;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.entity.SlotAdvType;
import com.moko.support.s.entity.SlotData;
import com.moko.support.s.entity.TriggerStep1Bean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: jun.liu
 * @date: 2024/10/10 17:29
 * @des: 配置触发前的一些参数
 */
public class TriggerStep3Activity extends BaseActivity {
    private ActivityTriggerStep3Binding mBind;
    private boolean mReceiverTag;
    private int slot;
    private TriggerStep1Bean step1Bean;
    private SlotData step2Bean;
    private SlotData step3Bean;
    private boolean isParamsError;
    private FragmentManager fragmentManager;
    private ISlotDataAction slotDataActionImpl;
    private boolean isAdvBeforeTrigger;

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
        slot = getIntent().getIntExtra(AppConstants.SLOT, 0);
        step1Bean = getIntent().getParcelableExtra("step1");
        step2Bean = getIntent().getParcelableExtra("step2");
        assert null != step1Bean && null != step2Bean;
        mBind.tvTitle.setText("SLOT" + (slot + 1));
        fragmentManager = getSupportFragmentManager();
        initFragment();
        setListener();
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getTriggerBeforeSlotParams(slot));
        }
    }

    private void initFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //这里的通道类型不支持修改,必须和第二步的触发后选择的通道类型保持一致
        switch (step2Bean.currentFrameType) {
            case UID:
                UidFragment uidFragment = UidFragment.newInstance();
                fragmentTransaction.add(R.id.frame_slot_container, uidFragment).show(uidFragment).commit();
                uidFragment.setTriggerAfter(false, step1Bean);
                slotDataActionImpl = uidFragment;
                break;
            case URL:
                UrlFragment urlFragment = UrlFragment.newInstance();
                fragmentTransaction.add(R.id.frame_slot_container, urlFragment).show(urlFragment).commit();
                slotDataActionImpl = urlFragment;
                urlFragment.setTriggerAfter(false, step1Bean);
                break;
            case TLM:
                TlmFragment tlmFragment = TlmFragment.newInstance();
                fragmentTransaction.add(R.id.frame_slot_container, tlmFragment).show(tlmFragment).commit();
                slotDataActionImpl = tlmFragment;
                tlmFragment.setTriggerAfter(false, step1Bean);
                break;
            case I_BEACON:
                IBeaconFragment iBeaconFragment = IBeaconFragment.newInstance();
                fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment).show(iBeaconFragment).commit();
                slotDataActionImpl = iBeaconFragment;
                iBeaconFragment.setTriggerAfter(false, step1Bean);
                break;
            case SENSOR_INFO:
                SensorInfoFragment sensorInfoFragment = SensorInfoFragment.newInstance();
                fragmentTransaction.add(R.id.frame_slot_container, sensorInfoFragment).show(sensorInfoFragment).commit();
                slotDataActionImpl = sensorInfoFragment;
                sensorInfoFragment.setTriggerAfter(false, step1Bean);
                break;
        }
    }

    private void setListener() {
        mBind.ivAdv.setOnClickListener(v -> {
            isAdvBeforeTrigger = !isAdvBeforeTrigger;
            mBind.ivAdv.setImageResource(isAdvBeforeTrigger ? R.drawable.ic_checked : R.drawable.ic_unchecked);
            mBind.layoutAdvTrigger.setVisibility(isAdvBeforeTrigger ? View.VISIBLE : View.GONE);
            if (isAdvBeforeTrigger) {
                //打开了触发前广播 这时候通道类型强制为触发后的通道保持一致
                slotDataActionImpl.setParams(slotData);
            }
        });
        mBind.btnDone.setOnClickListener(v -> {
            //先校验触发前的参数
            if (isAdvBeforeTrigger) {
                //打开了触发前参数
                if (slotDataActionImpl.isValid()) {
                    sendParams();
                }
            } else {
                sendParams();
            }
        });
        mBind.tvBack.setOnClickListener(v -> finish());
        mBind.btnBack.setOnClickListener(v -> finish());
    }

    private void sendParams() {
        showSyncingProgressDialog();
        isParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>(3);
        //1、下发第一步的参数 设置通道的触发类型
        orderTasks.add(OrderTaskAssembler.setSlotTriggerType(step1Bean));
        //2、下发第二步参数，触发后的参数
        orderTasks.add(OrderTaskAssembler.setSlotAdvParamsAfter(step2Bean));
        //3、下发第三步的参数，触发前的参数
        step3Bean = slotDataActionImpl.getSlotData();
        step3Bean.slot = this.slot;
        step3Bean.currentFrameType = !isAdvBeforeTrigger ? NO_DATA : step2Bean.currentFrameType;
        if (step1Bean.triggerType == MOTION_TRIGGER && step1Bean.triggerCondition == MOTION_TRIGGER_STATIONARY) {
            step3Bean.advDuration = step1Bean.axisStaticPeriod;
        }
        orderTasks.add(OrderTaskAssembler.setSlotAdvParamsBefore(step3Bean));
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_SLOT_PARAMS_BEFORE) {
                                if (length >= 10) {
                                    byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                    setSlotAdvParams(rawDataBytes);
                                }
                            }
                        } else if (flag == 1) {
                            switch (configKeyEnum) {
                                case KEY_SLOT_TRIGGER_TYPE:
                                case KEY_SLOT_PARAMS_AFTER:
                                    if ((value[4] & 0xff) != 0xAA) isParamsError = true;
                                    break;
                                case KEY_SLOT_PARAMS_BEFORE:
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
        switch (step1Bean.triggerType) {
            case TEMP_TRIGGER:
                if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and stop advertising immediately after device temperature is less than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = " *The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device temperature is more than or equal to " + step1Bean.tempThreshold + "℃.\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before stopping)";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms after device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and stop advertising immediately after device temperature is less than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms after device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and stop advertising after device temperature is less than " + step1Bean.tempThreshold + "℃\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the 5s post-trigger broadcast before stopping）";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device temperature is less than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device temperature is less than " + step1Bean.tempThreshold + "℃\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = " *The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and keep advertising at the interval of " + step3Bean.advInterval + "ms when device temperature is less than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step3Bean.advInterval + "ms when device temperature is less than " + step1Bean.tempThreshold + "℃\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device temperature is less than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device temperature is less than " + step1Bean.tempThreshold + "℃\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step3Bean.advInterval + "ms when device temperature is less than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device temperature is more than or equal to " + step1Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step3Bean.advInterval + "ms when device temperature is less than " + step1Bean.tempThreshold + "℃\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and stop advertising immediately after device temperature is more than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device temperature is less than or equal to " + step1Bean.tempThreshold + "℃.\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before stopping)";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising  at the interval of " + step2Bean.advInterval + "ms after device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and stop advertising immediately after device temperature is more than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising  at the interval of " + step2Bean.advInterval + "ms after device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and stop advertising after device temperature is more than " + step1Bean.tempThreshold + "℃\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the 5s post-trigger broadcast before stopping）";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device temperature is more than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device temperature is more than " + step1Bean.tempThreshold + "℃\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and keep advertising at the interval of " + step3Bean.advInterval + "ms when device temperature is more than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = " *The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step3Bean.advInterval + "ms when device temperature is more than " + step1Bean.tempThreshold + "℃\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device temperature is more than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device temperature is more than " + step1Bean.tempThreshold + "℃\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step3Bean.advInterval + "ms when device temperature is more than " + step1Bean.tempThreshold + "℃";
                } else if (step1Bean.triggerCondition == TEMP_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device temperature is less than or equal to " + step1Bean.tempThreshold + "℃, and  keep advertising at the interval of " + step3Bean.advInterval + "ms when device temperature is more than " + step1Bean.tempThreshold + "℃\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                }
                break;

            case HUM_TRIGGER:
                if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and stop advertising immediately after device Humidity is less than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device Humidity is more than or equal to " + step1Bean.humThreshold + "%.\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before stopping)";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms after device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and stop advertising immediately after device Humidity is less than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms after device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and stop advertising after device Humidity is less than " + step1Bean.humThreshold + "%\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the 5s post-trigger broadcast before stopping）";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device Humidity is less than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device Humidity is less than " + step1Bean.humThreshold + "%\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and keep advertising at the interval of " + step3Bean.advInterval + "ms when device Humidity is less than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and keep advertising at the interval of " + step3Bean.advInterval + "ms when device Humidity is less than " + step1Bean.humThreshold + "%\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device Humidity is less than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device Humidity is less than " + step1Bean.humThreshold + "%\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms when device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when device Humidity is less than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_ABOVE && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms when device Humidity is more than or equal to " + step1Bean.humThreshold + "%, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when device Humidity is less than " + step1Bean.humThreshold + "%\n" +
                            "（If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and stop advertising immediately after device Humidity is more than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device Humidity is less than or equal to " + step1Bean.humThreshold + "%.\n" +
                            "    (If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before stopping)";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising  at the interval of " + step2Bean.advInterval + "ms after device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and stop advertising immediately after device Humidity is more than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising  at the interval of " + step2Bean.advInterval + "ms after device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and stop advertising after device Humidity is more than " + step1Bean.humThreshold + "%\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the 5s post-trigger broadcast before stopping）";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = " *The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device Humidity is more than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device Humidity is more than " + step1Bean.humThreshold + "%\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when device Humidity is more than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when device Humidity is more than " + step1Bean.humThreshold + "%\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = " *The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms when device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device Humidity is more than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms when device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device Humidity is more than " + step1Bean.humThreshold + "%\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms when device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when device Humidity is more than " + step1Bean.humThreshold + "%";
                } else if (step1Bean.triggerCondition == HUM_TRIGGER_BELOW && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms when device Humidity is less than or equal to " + step1Bean.humThreshold + "%, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when device Humidity is more than " + step1Bean.humThreshold + "%\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                }
                break;

            case MOTION_TRIGGER:
                if (step1Bean.triggerCondition == MOTION_TRIGGER_MOTION && !isAdvBeforeTrigger && step2Bean.advDuration > 0) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device moves, and stop advertising immediately after device keep stationary for " + step1Bean.axisStaticPeriod + "s";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_MOTION && !isAdvBeforeTrigger && step2Bean.advDuration == 0) {
                    tips = "*The Beacon will start advertising for " + step1Bean.axisStaticPeriod + "s at the interval of " + step2Bean.advInterval + "ms after device moves, and stop advertising immediately after device keep stationary for " + step1Bean.axisStaticPeriod + "s";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_MOTION && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device moves, and advertising for " + step3Bean.standbyDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device keep stationary for " + step1Bean.axisStaticPeriod + "s";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_MOTION && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0) {
                    tips = "*The Beacon will advertising for " + step1Bean.axisStaticPeriod + "s at the interval of " + step2Bean.advInterval + "ms after device moves, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when device keep stationary for " + step1Bean.axisStaticPeriod + "s";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_MOTION && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device moves, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when device keep stationary for " + step1Bean.axisStaticPeriod + "s";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_MOTION && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0) {
                    tips = "*The Beacon will advertising for " + step1Bean.axisStaticPeriod + "s at the interval of " + step2Bean.advInterval + "ms after device moves, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when device keep stationary for " + step1Bean.axisStaticPeriod + "s";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_STATIONARY && !isAdvBeforeTrigger && step2Bean.advDuration > 0) {
                    tips = " *The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device keep stationary for " + step1Bean.axisStaticPeriod + "s, and stop advertising immediately when device moves.";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_STATIONARY && !isAdvBeforeTrigger && step2Bean.advDuration == 0) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms after device keep stationary for " + step1Bean.axisStaticPeriod + "s, and stop advertising immediately when device moves.";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_STATIONARY && isAdvBeforeTrigger && step2Bean.advDuration > 0) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after device keep stationary for " + step1Bean.axisStaticPeriod + "s, and advertising for " + step1Bean.axisStaticPeriod + "s at the interval of " + step3Bean.advInterval + "ms when device moves.";
                } else if (step1Bean.triggerCondition == MOTION_TRIGGER_STATIONARY && isAdvBeforeTrigger && step2Bean.advDuration == 0) {
                    tips = "*The Beacon will keep advertising at the interval of " + step2Bean.advInterval + "ms after device keep stationary for " + step1Bean.axisStaticPeriod + "s, and advertising for " + step1Bean.axisStaticPeriod + "s at the interval of " + step3Bean.advInterval + "ms when device moves.";
                }
                break;

            case HALL_TRIGGER:
                if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door open, and stop advertising immediately when door close";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms when door open.\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before stopping)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising  at the interval of " + step2Bean.advInterval + "ms after door open, and stop advertising immediately when door close";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising  at the interval of " + step2Bean.advInterval + "ms after door open, and stop advertising when door close\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the 5s post-trigger broadcast before stopping)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door open, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when door close";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door open, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when door close\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door open, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when door close";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door open, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when door close\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms after door open, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when door close";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms when door open, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when door close\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state）";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms after door open, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when door close";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_NEAR && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms after door open, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when door close\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door close, and stop advertising immediately when door open";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && !isAdvBeforeTrigger && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will start advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door close.\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before stopping)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising  at the interval of " + step2Bean.advInterval + "ms after door close, and stop advertising immediately when door open";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && !isAdvBeforeTrigger && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising  at the interval of " + step2Bean.advInterval + "ms after door close, and stop advertising when door open\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the 5s post-trigger broadcast before stopping)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door close, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when door open";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door close, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when door open\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door close, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when door open";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration > 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will advertising for " + step2Bean.advDuration + "s at the interval of " + step2Bean.advInterval + "ms after door close, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when door open\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered, the beacon will be locked to complete the Total adv duration broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms after door close, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when door open";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && isAdvBeforeTrigger && step3Bean.standbyDuration > 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms after door close, and advertising for " + step3Bean.advDuration + "s every " + step3Bean.standbyDuration + "s at the interval of " + step3Bean.advInterval + "ms when door open\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state)";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && !step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms after door close, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when door open";
                } else if (step1Bean.triggerCondition == HALL_TRIGGER_AWAY && isAdvBeforeTrigger && step3Bean.standbyDuration == 0 && step2Bean.advDuration == 0 && step1Bean.lockedAdv) {
                    tips = "*The Beacon will keep advertising at the interval of  " + step2Bean.advInterval + "ms after door close, and  keep advertising at the interval of  " + step3Bean.advInterval + "ms when door open\n" +
                            "(If the beacon quickly returns to a state where the trigger condition is no longer met shortly after the event is triggered,the beacon will be locked to complete the 5s post-trigger broadcast before switching to the pre-trigger broadcast state)";
                }
                break;
        }
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(tips);
        dialog.setCancelGone();
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            //返回通道页面
            EventBus.getDefault().unregister(this);
            Intent intent = new Intent(this, DeviceInfoActivity.class);
            startActivity(intent);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }

    private void setSlotAdvParams(@NonNull byte[] value) {
        int frameType = value[9] & 0xff;
        isAdvBeforeTrigger = frameType != NO_DATA;
        mBind.layoutAdvTrigger.setVisibility(isAdvBeforeTrigger ? View.VISIBLE : View.GONE);
        mBind.ivAdv.setImageResource(isAdvBeforeTrigger ? R.drawable.ic_checked : R.drawable.ic_unchecked);
//        boolean clearRawData = frameType != step2Bean.currentFrameType;
//        if (clearRawData) frameType = step2Bean.currentFrameType;
        mBind.tvFrameType.setText(SlotAdvType.getSlotAdvType(step2Bean.currentFrameType));
        SlotData slotData = new SlotData();
        slotData.advInterval = MokoUtils.toInt(Arrays.copyOfRange(value, 1, 3));
        slotData.advDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
        slotData.standbyDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
        slotData.rssi = value[7];
        slotData.txPower = value[8];
        slotData.currentFrameType = frameType;
        slotData.slot = this.slot;
        if (frameType == UID) {
            slotData.namespace = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 10, 20));
            slotData.instanceId = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 20, 26));
        } else if (frameType == URL) {
            slotData.urlScheme = value[10] & 0xff;
            slotData.urlContent = new String(Arrays.copyOfRange(value, 11, value.length));
        } else if (frameType == I_BEACON) {
            slotData.uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 10, 26));
            slotData.major = MokoUtils.toInt(Arrays.copyOfRange(value, 26, 28));
            slotData.minor = MokoUtils.toInt(Arrays.copyOfRange(value, 28, 30));
        } else if (frameType == SENSOR_INFO) {
            int nameLength = value[10] & 0xff;
            slotData.deviceName = new String(Arrays.copyOfRange(value, 11, 11 + nameLength));
            slotData.tagId = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 12 + nameLength, value.length));
        }
        this.slotData = slotData;
        if (null != slotDataActionImpl) {
            slotDataActionImpl.setParams(slotData);
        }
    }

    private SlotData slotData;

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
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}
