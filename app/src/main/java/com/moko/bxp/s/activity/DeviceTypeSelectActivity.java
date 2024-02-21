package com.moko.bxp.s.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityDeviceTypeSelectBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;

/**
 * @author: jun.liu
 * @date: 2024/2/19 17:13
 * @des:
 */
public class DeviceTypeSelectActivity extends BaseActivity {
    private ActivityDeviceTypeSelectBinding mBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceTypeSelectBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mBind.layoutCommon.setOnClickListener(v -> onTypeClick(1));
        mBind.layoutTH.setOnClickListener(v -> onTypeClick(2));
        mBind.layoutHum.setOnClickListener(v -> onTypeClick(3));
    }

    private void onTypeClick(int flag) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("flag", flag);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(R.string.main_exit_tips);
        dialog.setOnAlertConfirmListener(this::finish);
        dialog.show(getSupportFragmentManager());
    }
}
