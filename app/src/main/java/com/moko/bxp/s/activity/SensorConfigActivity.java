package com.moko.bxp.s.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.databinding.ActivitySensorConfigBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SensorConfigActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySensorConfigBinding mBind = ActivitySensorConfigBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        int accStatus = getIntent().getIntExtra(AppConstants.EXTRA_KEY1, 0);
        int thStatus = getIntent().getIntExtra(AppConstants.EXTRA_KEY2, 0);
        boolean isHallPowerEnable = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY3, false);
        boolean isButtonPowerEnable = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY4, false);
        mBind.tvAccConfig.setVisibility(accStatus == 0 ? View.GONE : View.VISIBLE);
        if (thStatus == 0) {
            mBind.tvTH.setVisibility(View.GONE);
        } else {
            mBind.tvTH.setVisibility(View.VISIBLE);
            if (thStatus == 3) {
                //只有温度
                mBind.tvTH.setText("Temperature");
            }
        }
        if (!isHallPowerEnable && !isButtonPowerEnable) {
            mBind.tvHall.setVisibility(View.VISIBLE);
        }
        mBind.tvTH.setOnClickListener(v -> {
            Intent intent = new Intent(this, THDataActivity.class);
            //是否只有温度传感器
            intent.putExtra(AppConstants.EXTRA_KEY1, thStatus == 3);
            startActivity(intent);
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                SensorConfigActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onBack(View view) {
        back();
    }

    private void back() {
        EventBus.getDefault().unregister(this);
        finish();
    }

    public void onAccConfig(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, AccDataActivity.class);
        startActivity(intent);
    }

    public void onHallSensorConfig(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, HallSensorConfigActivity.class);
        startActivity(intent);
    }
}
