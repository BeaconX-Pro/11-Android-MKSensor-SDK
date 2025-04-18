package com.moko.bxp.s.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.moko.lib.bxpui.dialog.LoadingMessageDialog;

import org.greenrobot.eventbus.EventBus;

public abstract class BaseActivity<VB extends ViewBinding> extends FragmentActivity {
    // 记录上次页面控件点击时间,屏蔽无效点击事件
    protected VB mBind;
    protected long mLastOnClickTime = 0;
    private LoadingMessageDialog mLoadingMessageDialog;
    private boolean mReceiverTag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = getViewBinding();
        setContentView(mBind.getRoot());
        if (registerEvent()){
            EventBus.getDefault().register(this);
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
        }
        onCreate();
    }

    protected abstract VB getViewBinding();

    protected void onCreate() {
    }

    protected boolean registerEvent(){
        return true;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        onSystemBleTurnOff();
                    }
                }
            }
        }
    };

    protected void onSystemBleTurnOff(){
        dismissSyncProgressDialog();
        finish();
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

    public void showSyncingProgressDialog() {
        if (null != mLoadingMessageDialog && mLoadingMessageDialog.isAdded() && !mLoadingMessageDialog.isDetached()) {
            mLoadingMessageDialog.dismissAllowingStateLoss();
        }
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        if (!mLoadingMessageDialog.isAdded())
            mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null && mLoadingMessageDialog.isAdded() && !mLoadingMessageDialog.isDetached())
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    public boolean isWindowLocked() {
        long current = SystemClock.elapsedRealtime();
        if (current - mLastOnClickTime > 500) {
            mLastOnClickTime = current;
            return false;
        } else {
            return true;
        }
    }

    public boolean isWriteStoragePermissionOpen() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationPermissionOpen() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
