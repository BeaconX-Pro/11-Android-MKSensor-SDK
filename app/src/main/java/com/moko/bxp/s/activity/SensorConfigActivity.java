package com.moko.bxp.s.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.bxp.s.databinding.ActivitySensorConfigBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SensorConfigActivity extends BaseActivity {
    private ActivitySensorConfigBinding mBind;
    private byte[] deviceTypeBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySensorConfigBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        deviceTypeBytes = getIntent().getByteArrayExtra("deviceTypeBytes");
        boolean isHallPowerEnable = getIntent().getBooleanExtra("hallEnable", false);
        if (null != deviceTypeBytes && deviceTypeBytes.length == 2) {
            if ((deviceTypeBytes[0] & 0xff) == 0) {
                mBind.tvAccConfig.setVisibility(View.GONE);
                mBind.lineAcc.setVisibility(View.GONE);
            }
            if ((deviceTypeBytes[1] & 0xff) == 0) {
                mBind.tvTH.setVisibility(View.GONE);
                mBind.lineTH.setVisibility(View.GONE);
            } else {
                mBind.tvTH.setVisibility(View.VISIBLE);
                mBind.lineTH.setVisibility(View.VISIBLE);
                if ((deviceTypeBytes[1] & 0xff) == 3) {
                    //只有温度
                    mBind.tvTH.setText("Temperature");
                }
            }
        }
        if (isHallPowerEnable) {
            mBind.tvHall.setVisibility(View.GONE);
            mBind.lineHall.setVisibility(View.GONE);
        }
        mBind.tvTH.setOnClickListener(v -> {
            Intent intent = new Intent(this, THDataActivity.class);
            intent.putExtra("type",deviceTypeBytes[1] & 0xff);
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
//        Intent intent = new Intent();
//        intent.putExtra("status", status);
//        setResult(RESULT_OK, intent);
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

    @Subscribe
    public void onHallChange(String hallDisable){
        if ("hallDisable".equals(hallDisable)){
            mBind.tvHall.setVisibility(View.GONE);
            mBind.lineHall.setVisibility(View.GONE);
        }
    }
}
