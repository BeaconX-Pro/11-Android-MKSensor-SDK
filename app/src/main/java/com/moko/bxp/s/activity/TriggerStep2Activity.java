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
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityTriggerStep2Binding;
import com.moko.bxp.s.dialog.BottomDialog;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.entity.TriggerStep1Bean;
import com.moko.bxp.s.entity.TriggerStep2Bean;
import com.moko.bxp.s.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

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
    private boolean isC112;

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
        setStatus();
        setListener();
    }

    private void setStatus() {
        if (triggerType == 1) {
            //温度
            mBind.layoutTemperature.setVisibility(View.VISIBLE);
            mBind.layoutHum.setVisibility(View.GONE);
            mBind.groupStaticPeriod.setVisibility(View.GONE);
        } else if (triggerType == 2) {
            //湿度
            mBind.layoutTemperature.setVisibility(View.GONE);
            mBind.layoutHum.setVisibility(View.VISIBLE);
            mBind.groupStaticPeriod.setVisibility(View.GONE);
        } else if (triggerType == 3) {
            //三轴
            mBind.layoutTemperature.setVisibility(View.GONE);
            mBind.layoutHum.setVisibility(View.GONE);
            mBind.groupStaticPeriod.setVisibility(View.VISIBLE);
        } else if (triggerType == 4) {
            //霍尔
            mBind.layoutTemperature.setVisibility(View.GONE);
            mBind.layoutHum.setVisibility(View.GONE);
            mBind.groupStaticPeriod.setVisibility(View.GONE);
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

    private void setListener() {
        mBind.tvTriggerType.setText(triggerTypeArray[triggerType - 1]);
        mBind.tvTriggerEvent.setText(getTriggerEvent()[0]);
        mBind.tvTriggerType.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(triggerTypeArray)), triggerType - 1);
            dialog.setListener(value -> {
                triggerType = value + 1;
                mBind.tvTriggerType.setText(triggerTypeArray[value]);
                mBind.tvTriggerEvent.setText(getTriggerEvent()[0]);
                setStatus();
            });
            dialog.show(getSupportFragmentManager());
        });
        mBind.tvTriggerEvent.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(getTriggerEvent())), currentTriggerEventSelect);
            dialog.setListener(value -> {
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
            bean.tempThreshold = mBind.sbTemp.getProgress() - 20;
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
            mBind.tvTempValue.setText((progress - 20) + "℃");
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
