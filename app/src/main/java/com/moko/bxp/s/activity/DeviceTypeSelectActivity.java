package com.moko.bxp.s.activity;

import android.content.Intent;
import android.view.View;

import com.moko.bxp.s.BuildConfig;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivityDeviceTypeSelectSBinding;
import com.moko.bxp.s.dialog.AlertMessageDialog;

/**
 * @author: jun.liu
 * @date: 2024/2/19 17:13
 * @des:
 */
public class DeviceTypeSelectActivity extends BaseActivity<ActivityDeviceTypeSelectSBinding> {
    @Override
    protected void onCreate() {
        mBind.layoutCommon.setOnClickListener(v -> onTypeClick(1));
        mBind.layoutTH.setOnClickListener(v -> onTypeClick(2));
        mBind.layoutHum.setOnClickListener(v -> onTypeClick(3));
        mBind.tvBack.setVisibility(BuildConfig.IS_LIBRARY? View.VISIBLE:View.GONE);
        if (BuildConfig.IS_LIBRARY){
            mBind.tvBack.setOnClickListener(v -> back());
        }
    }

    @Override
    protected ActivityDeviceTypeSelectSBinding getViewBinding() {
        return ActivityDeviceTypeSelectSBinding.inflate(getLayoutInflater());
    }

    @Override
    protected boolean registerEvent() {
        return false;
    }

    private void onTypeClick(int flag) {
        Intent intent = new Intent(this, BXPSMainActivity.class);
        intent.putExtra("flag", flag);
        startActivity(intent);
    }

    private void back(){
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!BuildConfig.IS_LIBRARY) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setMessage(R.string.main_exit_tips);
            dialog.setOnAlertConfirmListener(this::finish);
            dialog.show(getSupportFragmentManager());
        } else {
            back();
        }
    }
}
