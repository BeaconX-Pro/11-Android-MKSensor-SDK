package com.moko.bxp.s.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.moko.bxp.s.BuildConfig;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityDeviceTypeSelectSBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;

/**
 * @author: jun.liu
 * @date: 2024/2/19 17:13
 * @des:
 */
public class DeviceTypeSelectActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDeviceTypeSelectSBinding mBind = ActivityDeviceTypeSelectSBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mBind.layoutCommon.setOnClickListener(v -> onTypeClick(1));
        mBind.layoutTH.setOnClickListener(v -> onTypeClick(2));
        mBind.layoutHum.setOnClickListener(v -> onTypeClick(3));
    }

    private void onTypeClick(int flag) {
        Intent intent = new Intent(this, BXPSMainActivity.class);
        intent.putExtra("flag", flag);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (!BuildConfig.IS_LIBRARY) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setMessage(R.string.main_exit_tips);
            dialog.setOnAlertConfirmListener(this::finish);
            dialog.show(getSupportFragmentManager());
        } else {
            finish();
        }
    }
}
