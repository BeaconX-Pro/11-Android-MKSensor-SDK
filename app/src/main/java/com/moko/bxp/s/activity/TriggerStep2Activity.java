package com.moko.bxp.s.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityTriggerStep2Binding;
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
 * @date: 2024/1/30 16:08
 * @des:
 */
public class TriggerStep2Activity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private ActivityTriggerStep2Binding mBind;
    private boolean mReceiverTag;
    private int slot;
    private TriggerStep1Bean step1Bean;
    // TODO: 2024/1/31
    //触发类型 1 2 3 4
    private int triggerType;
    private final String[] triggerTypeArray = {"Temperature detection", "Humidity detection", "Motion detection", "Door magnetic detection"};
    private final String[] hallTriggerEventArray = {"Door close", "Door open"};
    private final String[] axisTriggerEventArray = {"Device start moving", "Device remains stationary"};
    private final String[] tempTriggerEventArray = {"Temperature above threshold", "Temperature below threshold"};
    private final String[] humTriggerEventArray = {"Humidity above threshold", "Humidity below threshold"};
    private int currentTriggerEventSelect;
    public int hallTriggerSelect;
    public int axisTriggerSelect;
    public int tempTriggerSelect;
    public int humTriggerSelect;
    private boolean isC112;
    private int axisStaticPeriod;
    private int tempThreshold;
    private int humThreshold;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityTriggerStep2Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        slot = getIntent().getIntExtra("slot", 0);
        step1Bean = getIntent().getParcelableExtra("step1");
        isC112 = getIntent().getBooleanExtra("c112", false);
        mBind.tvTitle.setText("SLOT" + (slot + 1));
        setListener();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getSlotTriggerType(slot));
//        orderTasks.add(OrderTaskAssembler.getHallTriggerEvent(slot));
//        orderTasks.add(OrderTaskAssembler.getAxisTriggerEvent(slot));
//        orderTasks.add(OrderTaskAssembler.getTempTriggerEvent(slot));
//        orderTasks.add(OrderTaskAssembler.getHumTriggerEvent(slot));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    private void setStatus() {
        mBind.tvTriggerType.setText(triggerTypeArray[triggerType - 1]);
        if (triggerType == 1) {
            //温度
            mBind.layoutTemperature.setVisibility(View.VISIBLE);
            mBind.layoutHum.setVisibility(View.GONE);
            mBind.groupStaticPeriod.setVisibility(View.GONE);
            mBind.tvTriggerEvent.setText(tempTriggerEventArray[tempTriggerSelect]);
            mBind.sbTemp.setProgress(tempThreshold + 40);
            mBind.tvTempValue.setText(tempThreshold + "℃");
            currentTriggerEventSelect = tempTriggerSelect;
        } else if (triggerType == 2) {
            //湿度
            mBind.layoutTemperature.setVisibility(View.GONE);
            mBind.layoutHum.setVisibility(View.VISIBLE);
            mBind.groupStaticPeriod.setVisibility(View.GONE);
            mBind.tvTriggerEvent.setText(humTriggerEventArray[humTriggerSelect]);
            mBind.sbHum.setProgress(humThreshold);
            mBind.tvHumValue.setText(humThreshold + "%");
            currentTriggerEventSelect = humTriggerSelect;
        } else if (triggerType == 3) {
            //三轴
            mBind.layoutTemperature.setVisibility(View.GONE);
            mBind.layoutHum.setVisibility(View.GONE);
            mBind.groupStaticPeriod.setVisibility(View.VISIBLE);
            mBind.tvTriggerEvent.setText(axisTriggerEventArray[axisTriggerSelect]);
            mBind.etStaticPeriod.setText(axisStaticPeriod);
            mBind.etStaticPeriod.setSelection(mBind.etStaticPeriod.getText().length());
            currentTriggerEventSelect = axisTriggerSelect;
        } else if (triggerType == 4) {
            //霍尔
            mBind.layoutTemperature.setVisibility(View.GONE);
            mBind.layoutHum.setVisibility(View.GONE);
            mBind.groupStaticPeriod.setVisibility(View.GONE);
            mBind.tvTriggerEvent.setText(hallTriggerEventArray[hallTriggerSelect]);
            currentTriggerEventSelect = hallTriggerSelect;
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
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
                                case KEY_HALL_TRIGGER_EVENT:
                                    if (length == 2) {
                                        hallTriggerSelect = value[5] & 0xff;
                                        setStatus();
                                    }
                                    break;

                                case KEY_AXIS_TRIGGER_EVENT:
                                    if (length == 4) {
                                        axisTriggerSelect = value[5] & 0xff;
                                        axisStaticPeriod = MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8));
                                        setStatus();
                                    }
                                    break;

                                case KEY_TEMP_TRIGGER_EVENT:
                                    if (length == 5) {
                                        tempTriggerSelect = value[5] & 0xff;
                                        tempThreshold = MokoUtils.toIntSigned(Arrays.copyOfRange(value, 6, 8));
                                        setStatus();
                                    }
                                    break;

                                case KEY_HUM_TRIGGER_EVENT:
                                    if (length == 4) {
                                        humTriggerSelect = value[5] & 0xff;
                                        humThreshold = value[6] & 0xff;
                                        setStatus();
                                    }
                                    break;

                                case KEY_SLOT_TRIGGER_TYPE:
                                    if (length == 2) {
                                        triggerType = value[5] & 0xff;
                                        if (triggerType == 1) {
                                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getTempTriggerEvent(slot));
                                        } else if (triggerType == 2) {
                                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getHumTriggerEvent(slot));
                                        } else if (triggerType == 3) {
                                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAxisTriggerEvent(slot));
                                        } else if (triggerType == 4) {
                                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getHallTriggerEvent(slot));
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void setListener() {
        mBind.tvTriggerType.setText(triggerTypeArray[triggerType - 1]);
        mBind.tvTriggerEvent.setText(getTriggerEvent()[0]);
        mBind.tvTriggerType.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(triggerTypeArray)), triggerType - 1);
            dialog.setListener(value -> {
                triggerType = value + 1;
                setStatus();
            });
            dialog.show(getSupportFragmentManager());
        });
        mBind.tvTriggerEvent.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(getTriggerEvent())), getCurrentTriggerEventSelect());
            dialog.setListener(value -> {
                if (triggerType == 1) tempTriggerSelect = value;
                else if (triggerType == 2) humTriggerSelect = value;
                else if (triggerType == 3) axisTriggerSelect = value;
                else if (triggerType == 4) hallTriggerSelect = value;
                currentTriggerEventSelect = value;
                mBind.tvTriggerEvent.setText(getTriggerEvent()[value]);
            });
            dialog.show(getSupportFragmentManager());
        });
        mBind.btnNext.setOnClickListener(v -> onNext());
        mBind.sbTemp.setOnSeekBarChangeListener(this);
        mBind.sbHum.setOnSeekBarChangeListener(this);
    }

    private void onNext() {
        //下一步
        TriggerStep2Bean bean = new TriggerStep2Bean();
        bean.triggerType = triggerType;
        bean.triggerEventSelect = currentTriggerEventSelect;
        if (triggerType == 1) {
            //温度
            bean.tempThreshold = mBind.sbTemp.getProgress() - 40;
        } else if (triggerType == 2) {
            //湿度
            bean.humThreshold = mBind.sbHum.getProgress();
        } else if (triggerType == 3) {
            //三轴
            if (TextUtils.isEmpty(mBind.etStaticPeriod.getText())) {
                ToastUtils.showToast(this, "Data format incorrect!");
                return;
            }
            int staticPeriod = Integer.parseInt(mBind.etStaticPeriod.getText().toString());
            if (staticPeriod < 1 || staticPeriod > 65535) {
                ToastUtils.showToast(this, "Data format incorrect!");
                return;
            }
            bean.axisStaticPeriod = staticPeriod;
        }
        Intent intent = new Intent(this, TriggerStep3Activity.class);
        intent.putExtra("step2", bean);
        intent.putExtra("step1", step1Bean);
        intent.putExtra("slot", slot);
        intent.putExtra("c112", isC112);
        startActivity(intent);
        finish();
    }

    private String[] getTriggerEvent() {
        if (triggerType == 1) {
            return tempTriggerEventArray;
        } else if (triggerType == 2) {
            return humTriggerEventArray;
        } else if (triggerType == 3) {
            return axisTriggerEventArray;
        } else {
            return hallTriggerEventArray;
        }
    }

    private int getCurrentTriggerEventSelect() {
        if (triggerType == 1) {
            return tempTriggerSelect;
        } else if (triggerType == 2) {
            return humTriggerSelect;
        } else if (triggerType == 3) {
            return axisTriggerSelect;
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
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sbTemp) {
            //温度的
            mBind.tvTempValue.setText((progress - 40) + "℃");
        } else if (seekBar.getId() == R.id.sbHum) {
            //湿度
            mBind.tvHumValue.setText(progress + "%");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
