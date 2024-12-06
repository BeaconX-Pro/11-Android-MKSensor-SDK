package com.moko.bxp.s.activity;

import static com.moko.support.s.entity.SlotAdvType.HALL_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.HALL_TRIGGER_AWAY;
import static com.moko.support.s.entity.SlotAdvType.HALL_TRIGGER_NEAR;
import static com.moko.support.s.entity.SlotAdvType.HUM_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.HUM_TRIGGER_ABOVE;
import static com.moko.support.s.entity.SlotAdvType.HUM_TRIGGER_BELOW;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER_MOTION;
import static com.moko.support.s.entity.SlotAdvType.MOTION_TRIGGER_STATIONARY;
import static com.moko.support.s.entity.SlotAdvType.NO_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.TEMP_TRIGGER;
import static com.moko.support.s.entity.SlotAdvType.TEMP_TRIGGER_ABOVE;
import static com.moko.support.s.entity.SlotAdvType.TEMP_TRIGGER_BELOW;

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
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityTriggerStep1Binding;
import com.moko.bxp.s.dialog.BottomDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.entity.TriggerEvent;
import com.moko.bxp.s.fragment.HallTriggerFragment;
import com.moko.bxp.s.fragment.HumidityTriggerFragment;
import com.moko.bxp.s.fragment.MotionTriggerFragment;
import com.moko.bxp.s.fragment.TemperatureTriggerFragment;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.entity.TriggerStep1Bean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: jun.liu
 * @date: 2024/1/30 16:08
 * @des:
 */
public class TriggerStep1Activity extends BaseActivity {
    private ActivityTriggerStep1Binding mBind;
    private boolean mReceiverTag;
    private int slot;
    private int triggerType;
    //1 2 3 4
//    private final String[] triggerTypeArray = {"Temperature detection", "Humidity detection", "Motion detection", "Magnetic detection"};
    private final String[] tempTriggerEventArray = {"Temperature above threshold", "Temperature below threshold"};
    private final String[] humTriggerEventArray = {"Humidity above threshold", "Humidity below threshold"};
    private final String[] motionTriggerEventArray = {"Device start moving", "Device keep static"};
    private final String[] hallTriggerEventArray = {"Door close", "Door open"};
    public int hallTriggerSelect;
    public int triggerCondition;
    public int motionTriggerSelect;
    public int tempTriggerSelect;
    public int humTriggerSelect;
    private TriggerEvent triggerEvent;
    private boolean isTriggerOpen = true;
    private FragmentManager fragmentManager;
    private TemperatureTriggerFragment temperatureTriggerFragment;
    private HumidityTriggerFragment humidityTriggerFragment;
    private MotionTriggerFragment motionTriggerFragment;
    private HallTriggerFragment hallTriggerFragment;
    private int rawTriggerType;
    private int accStatus;
    private int thStatus;
    private final List<String> triggerList = new ArrayList<>();
    private final String MOTION_DETECTION = "Motion detection";
    private final String TEMPERATURE_DETECTION = "Temperature detection";
    private final String HUMIDITY_DETECTION = "Humidity detection";
    private final String MAGNETIC_DETECTION = "Magnetic detection";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityTriggerStep1Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        slot = getIntent().getIntExtra(AppConstants.SLOT, 0);
        triggerEvent = getIntent().getParcelableExtra(AppConstants.EXTRA_KEY1);
        accStatus = getIntent().getIntExtra(AppConstants.EXTRA_KEY2, 0);
        thStatus = getIntent().getIntExtra(AppConstants.EXTRA_KEY3, 0);
        boolean isButtonReset = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY4, false);
        boolean isButtonPowerEnable = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY5, false);
        mBind.tvTitle.setText("SLOT" + (slot + 1));
        //这里可能是无触发
        rawTriggerType = triggerType = null == triggerEvent ? getNoTriggerType() : (triggerEvent.triggerType == NO_TRIGGER ? getNoTriggerType() : triggerEvent.triggerType);
        fragmentManager = getSupportFragmentManager();
        if (accStatus > 0) {
            triggerList.add(MOTION_DETECTION);
        }
        if (thStatus == 1 || thStatus == 2 || thStatus == 4) {
            triggerList.add(TEMPERATURE_DETECTION);
            triggerList.add(HUMIDITY_DETECTION);
        } else if (thStatus == 3) {
            triggerList.add(TEMPERATURE_DETECTION);
        }
        if (!isButtonReset && !isButtonPowerEnable) {
            triggerList.add(MAGNETIC_DETECTION);
        }
        initFragment();
        showFragment();
        initListener();
        mBind.tvBack.setOnClickListener(v -> finish());
    }

    private int getNoTriggerType() {
        if (accStatus > 0) return MOTION_TRIGGER;
        if (thStatus > 0) return TEMP_TRIGGER;
        return HALL_TRIGGER;
    }

    private int getIndexByTriggerType() {
        switch (triggerType) {
            case TEMP_TRIGGER:
                for (int i = 0; i < triggerList.size(); i++) {
                    if (TEMPERATURE_DETECTION.equals(triggerList.get(i))) return i;
                }
                break;

            case HUM_TRIGGER:
                for (int i = 0; i < triggerList.size(); i++) {
                    if (HUMIDITY_DETECTION.equals(triggerList.get(i))) return i;
                }
                break;

            case MOTION_TRIGGER:
                for (int i = 0; i < triggerList.size(); i++) {
                    if (MOTION_DETECTION.equals(triggerList.get(i))) return i;
                }
                break;

            case HALL_TRIGGER:
                for (int i = 0; i < triggerList.size(); i++) {
                    if (MAGNETIC_DETECTION.equals(triggerList.get(i))) return i;
                }
                break;
        }
        return 0;
    }

    private void initFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        temperatureTriggerFragment = new TemperatureTriggerFragment();
        fragmentTransaction.add(R.id.layoutContainer, temperatureTriggerFragment);
        humidityTriggerFragment = new HumidityTriggerFragment();
        fragmentTransaction.add(R.id.layoutContainer, humidityTriggerFragment);
        motionTriggerFragment = new MotionTriggerFragment();
        fragmentTransaction.add(R.id.layoutContainer, motionTriggerFragment);
        hallTriggerFragment = new HallTriggerFragment();
        fragmentTransaction.add(R.id.layoutContainer, hallTriggerFragment);
        fragmentTransaction.commit();
    }

    private void showFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (triggerType) {
            case TEMP_TRIGGER:
                //温度触发
                fragmentTransaction.hide(humidityTriggerFragment).hide(motionTriggerFragment).hide(hallTriggerFragment).show(temperatureTriggerFragment).commit();
                break;

            case HUM_TRIGGER:
                //湿度触发
                fragmentTransaction.hide(temperatureTriggerFragment).hide(motionTriggerFragment).hide(hallTriggerFragment).show(humidityTriggerFragment).commit();
                break;

            case MOTION_TRIGGER:
                fragmentTransaction.hide(temperatureTriggerFragment).hide(humidityTriggerFragment).hide(hallTriggerFragment).show(motionTriggerFragment).commit();
                break;

            case HALL_TRIGGER:
                fragmentTransaction.hide(temperatureTriggerFragment).hide(humidityTriggerFragment).hide(motionTriggerFragment).show(hallTriggerFragment).commit();
                break;
        }
    }

    private void setStatus() {
        mBind.tvTriggerType.setText(triggerList.get(getIndexByTriggerType()));
        if (triggerType == TEMP_TRIGGER) {
            //温度
            if (null != triggerEvent && triggerType == rawTriggerType) {
                tempTriggerSelect = triggerEvent.triggerCondition == TEMP_TRIGGER_ABOVE ? 0 : 1;
                triggerCondition = triggerEvent.triggerCondition;
                temperatureTriggerFragment.setValues(triggerEvent.triggerThreshold, triggerEvent.lockAdvDuration);
            } else {
                tempTriggerSelect = 0;
                triggerCondition = TEMP_TRIGGER_ABOVE;
                temperatureTriggerFragment.setValues(-64, 0);
            }
            mBind.tvTriggerEvent.setText(tempTriggerEventArray[tempTriggerSelect]);
        } else if (triggerType == HUM_TRIGGER) {
            //湿度
            if (null != triggerEvent && triggerType == rawTriggerType) {
                humTriggerSelect = triggerEvent.triggerCondition == HUM_TRIGGER_ABOVE ? 0 : 1;
                triggerCondition = triggerEvent.triggerCondition;
                humidityTriggerFragment.setValue(triggerEvent.triggerThreshold, triggerEvent.lockAdvDuration);
            } else {
                humTriggerSelect = 0;
                triggerCondition = HUM_TRIGGER_ABOVE;
                humidityTriggerFragment.setValue(0, 0);
            }
            mBind.tvTriggerEvent.setText(humTriggerEventArray[humTriggerSelect]);
        } else if (triggerType == MOTION_TRIGGER) {
            //移动触发
            if (null != triggerEvent && triggerType == rawTriggerType) {
                motionTriggerSelect = triggerEvent.triggerCondition == MOTION_TRIGGER_MOTION ? 0 : 1;
                triggerCondition = triggerEvent.triggerCondition;
                motionTriggerFragment.setValue(triggerEvent.staticPeriod);
            } else {
                motionTriggerSelect = 0;
                triggerCondition = MOTION_TRIGGER_MOTION;
                motionTriggerFragment.setValue(0);
            }
            mBind.tvTriggerEvent.setText(motionTriggerEventArray[motionTriggerSelect]);
            motionTriggerFragment.setIsStartMove(motionTriggerSelect == 0);
        } else if (triggerType == HALL_TRIGGER) {
            //霍尔触发
            if (null != triggerEvent && triggerType == rawTriggerType) {
                hallTriggerSelect = triggerEvent.triggerCondition == HALL_TRIGGER_AWAY ? 0 : 1;
                triggerCondition = triggerEvent.triggerCondition;
                hallTriggerFragment.setValue(triggerEvent.lockAdvDuration);
            } else {
                hallTriggerSelect = 0;
                triggerCondition = HALL_TRIGGER_NEAR;
                hallTriggerFragment.setValue(0);
            }
            mBind.tvTriggerEvent.setText(hallTriggerEventArray[hallTriggerSelect]);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    private int getTriggerTypeBySelect(@NonNull String str) {
        if (MOTION_DETECTION.equals(str)) return MOTION_TRIGGER;
        if (TEMPERATURE_DETECTION.equals(str)) return TEMP_TRIGGER;
        if (HUMIDITY_DETECTION.equals(str)) return HUM_TRIGGER;
        if (MAGNETIC_DETECTION.equals(str)) return HALL_TRIGGER;
        return 0;
    }

    private void initListener() {
        setStatus();
        mBind.tvTriggerType.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas((ArrayList<String>) triggerList, getIndexByTriggerType());
            dialog.setListener(value -> {
                triggerType = getTriggerTypeBySelect(triggerList.get(value));
                showFragment();
                setStatus();
            });
            dialog.show(getSupportFragmentManager());
        });
        mBind.tvTriggerEvent.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(getTriggerEvent())), getCurrentTriggerEventSelect());
            dialog.setListener(this::changeTriggerEvent);
            dialog.show(getSupportFragmentManager());
        });
        mBind.btnNext.setOnClickListener(v -> onNext());
        mBind.ivTrigger.setOnClickListener(v -> {
            isTriggerOpen = !isTriggerOpen;
            mBind.ivTrigger.setImageResource(isTriggerOpen ? R.drawable.ic_checked : R.drawable.ic_unchecked);
            mBind.btnNext.setText(isTriggerOpen ? "Next" : "Done");
            mBind.layoutTrigger.setVisibility(isTriggerOpen ? View.VISIBLE : View.GONE);
        });
    }

    private void changeTriggerEvent(int value) {
        if (triggerType == TEMP_TRIGGER) {
            tempTriggerSelect = value;
            triggerCondition = value == 0 ? TEMP_TRIGGER_ABOVE : TEMP_TRIGGER_BELOW;
        } else if (triggerType == HUM_TRIGGER) {
            humTriggerSelect = value;
            triggerCondition = value == 0 ? HUM_TRIGGER_ABOVE : HUM_TRIGGER_BELOW;
        } else if (triggerType == MOTION_TRIGGER) {
            motionTriggerSelect = value;
            triggerCondition = value == 0 ? MOTION_TRIGGER_MOTION : MOTION_TRIGGER_STATIONARY;
            motionTriggerFragment.setIsStartMove(value == 0);
        } else if (triggerType == HALL_TRIGGER) {
            hallTriggerSelect = value;
            triggerCondition = value == 0 ? HALL_TRIGGER_AWAY : HALL_TRIGGER_NEAR;
        }
        mBind.tvTriggerEvent.setText(getTriggerEvent()[value]);
    }

    private void onNext() {
        if (!isTriggerOpen) return;
        //下一步
        TriggerStep1Bean bean = new TriggerStep1Bean();
        bean.triggerType = triggerType;
        bean.triggerCondition = triggerCondition;
        bean.slot = slot;
        if (triggerType == TEMP_TRIGGER) {
            //温度
            bean.tempThreshold = temperatureTriggerFragment.getTempThreshold();
            bean.lockedAdv = temperatureTriggerFragment.lockedAdv();
        } else if (triggerType == HUM_TRIGGER) {
            //湿度
            bean.humThreshold = humidityTriggerFragment.getHumThreshold();
            bean.lockedAdv = humidityTriggerFragment.lockedAdv();
        } else if (triggerType == MOTION_TRIGGER) {
            //三轴
            if (motionTriggerFragment.isValid()) {
                bean.axisStaticPeriod = motionTriggerFragment.getPeriod();
            } else {
                ToastUtils.showToast(this, "Data format incorrect!");
                return;
            }
        } else if (triggerType == HALL_TRIGGER) {
            //霍尔
            bean.lockedAdv = hallTriggerFragment.lockedAdv();
        }
        Intent intent = new Intent(this, TriggerStep2Activity.class);
        intent.putExtra("step1", bean);
        intent.putExtra(AppConstants.SLOT, slot);
        startActivity(intent);
        EventBus.getDefault().unregister(this);
        finish();
    }

    private String[] getTriggerEvent() {
        if (triggerType == TEMP_TRIGGER) {
            return tempTriggerEventArray;
        } else if (triggerType == HUM_TRIGGER) {
            return humTriggerEventArray;
        } else if (triggerType == MOTION_TRIGGER) {
            return motionTriggerEventArray;
        } else {
            return hallTriggerEventArray;
        }
    }

    private int getCurrentTriggerEventSelect() {
        if (triggerType == TEMP_TRIGGER) {
            return tempTriggerSelect;
        } else if (triggerType == HUM_TRIGGER) {
            return humTriggerSelect;
        } else if (triggerType == MOTION_TRIGGER) {
            return motionTriggerSelect;
        } else {
            return hallTriggerSelect;
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
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}
