package com.moko.bxp.s.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivitySlotDataBinding;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
import com.moko.bxp.s.entity.SlotData;
import com.moko.bxp.s.fragment.IBeaconFragment;
import com.moko.bxp.s.fragment.SensorInfoFragment;
import com.moko.bxp.s.fragment.TlmFragment;
import com.moko.bxp.s.fragment.UidFragment;
import com.moko.bxp.s.fragment.UrlFragment;
import com.moko.bxp.s.utils.ToastUtils;
import com.moko.support.s.MokoSupport;
import com.moko.support.s.OrderTaskAssembler;
import com.moko.support.s.entity.OrderCHAR;
import com.moko.support.s.entity.ParamsKeyEnum;
import com.moko.support.s.entity.SlotFrameTypeEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SlotDataActivity extends BaseActivity implements NumberPickerView.OnValueChangeListener {
    private FragmentManager fragmentManager;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    private SensorInfoFragment sensorInfoFragment;
    public SlotData slotData;
    private ISlotDataAction slotDataActionImpl;
    private String[] slotTypeArray;
    public SlotFrameTypeEnum currentFrameTypeEnum;
    public boolean isConfigError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySlotDataBinding mBind = ActivitySlotDataBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        if (getIntent() != null && getIntent().getExtras() != null) {
            slotData = (SlotData) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_SLOT_DATA);
            if (null == slotData) return;
            currentFrameTypeEnum = slotData.frameTypeEnum;
            XLog.i(slotData.toString());
        }
        fragmentManager = getSupportFragmentManager();
        createFragments();
        slotTypeArray = getResources().getStringArray(R.array.slot_type);
        mBind.npvSlotType.setDisplayedValues(slotTypeArray);
        final int length = slotTypeArray.length;
        mBind.npvSlotType.setMinValue(0);
        mBind.npvSlotType.setMaxValue(5);
        mBind.npvSlotType.setOnValueChangedListener(this);
        for (int i = 0; i < length; i++) {
            if (slotData.frameTypeEnum.getShowName().equals(slotTypeArray[i])) {
                mBind.npvSlotType.setValue(i);
                showFragment(i);
                break;
            }
        }
        mBind.tvSlotTitle.setText(slotData.slotEnum.getTitle());
        EventBus.getDefault().register(this);
        mBind.rlTriggerSwitch.setOnClickListener(v -> {
            Intent intent = new Intent(this, TriggerStep1Activity.class);
            intent.putExtra("slot", slotData.slotEnum.getSlot());
            intent.putExtra("isC112", slotData.isC112);
            intent.putExtra("triggerType", 0);
            startActivity(intent);
        });
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        uidFragment = UidFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
        uidFragment.setSlotData(slotData);
        urlFragment = UrlFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
        urlFragment.setSlotData(slotData);
        tlmFragment = TlmFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
        tlmFragment.setSlotData(slotData);
        iBeaconFragment = IBeaconFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
        iBeaconFragment.setSlotData(slotData);
        sensorInfoFragment = SensorInfoFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, sensorInfoFragment);
        sensorInfoFragment.setSlotData(slotData);
        fragmentTransaction.commit();
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                SlotDataActivity.this.finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                ToastUtils.showToast(SlotDataActivity.this, isConfigError ? "Error" : "Successfully configure");
                isConfigError = false;
                dismissSyncProgressDialog();
                SlotDataActivity.this.setResult(RESULT_OK);
                SlotDataActivity.this.finish();
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
                        if (flag == 0x00) return;
                        if (length > 0) {
                            int result = value[4] & 0xFF;
                            if (result == 0x00) {
                                isConfigError = true;
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

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
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        XLog.i(newVal + "");
        XLog.i(picker.getContentByCurrValue());
        showFragment(newVal);
        if (slotDataActionImpl != null) {
            slotDataActionImpl.resetParams(currentFrameTypeEnum);
        }
    }

    private void showFragment(int index) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromShowName(slotTypeArray[index]);
        if (null == slotFrameTypeEnum) return;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (slotFrameTypeEnum) {
            case TLM:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(sensorInfoFragment).show(tlmFragment).commit();
                slotDataActionImpl = tlmFragment;
                break;
            case UID:
                fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(sensorInfoFragment).show(uidFragment).commit();
                slotDataActionImpl = uidFragment;
                break;
            case URL:
                fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(sensorInfoFragment).show(urlFragment).commit();
                slotDataActionImpl = urlFragment;
                break;
            case IBEACON:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(sensorInfoFragment).show(iBeaconFragment).commit();
                slotDataActionImpl = iBeaconFragment;
                break;
            case SENSOR_INFO:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(sensorInfoFragment).commit();
                slotDataActionImpl = sensorInfoFragment;
                break;
            case NO_DATA:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(sensorInfoFragment).commit();
                slotDataActionImpl = null;
                break;
        }
        slotData.frameTypeEnum = slotFrameTypeEnum;
    }

    public void onBack(View view) {
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        isConfigError = false;
        if (slotDataActionImpl == null) {
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setSlotParamsNoData(slotData.slotEnum.ordinal()));
            return;
        }
        if (!slotDataActionImpl.isValid()) return;
        showSyncingProgressDialog();
        slotDataActionImpl.sendData();
    }

    public void onSelectUrlScheme(View view) {
        if (isWindowLocked()) return;
        if (urlFragment != null) urlFragment.selectUrlScheme();
    }
}
