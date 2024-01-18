package com.moko.bxp.s.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.bxp.s.c.databinding.FragmentAoaSettingBinding;

public class SettingFragment extends Fragment {
    private FragmentAoaSettingBinding mBind;
    private boolean showPwd;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentAoaSettingBinding.inflate(inflater, container, false);
        setPwdShown(showPwd);
        return mBind.getRoot();
    }

    public void setAcc(int accEnable) {
        if (accEnable == 0) {
            //无三轴
            mBind.tvAcc.setVisibility(View.GONE);
            mBind.lineAcc.setVisibility(View.GONE);
            mBind.tvPowerSave.setVisibility(View.GONE);
            mBind.linePowerSave.setVisibility(View.GONE);
        }
    }

    public void setPwdShown(boolean showPwd) {
        this.showPwd = showPwd;
        if (null == mBind) return;
        mBind.tvModifyPwd.setVisibility(showPwd ? View.VISIBLE : View.GONE);
        mBind.lineModifyPwd.setVisibility(showPwd ? View.VISIBLE : View.GONE);
    }
}
