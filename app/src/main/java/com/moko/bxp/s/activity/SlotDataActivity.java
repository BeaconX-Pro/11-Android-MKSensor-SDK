package com.moko.bxp.s.activity;

import static com.moko.support.s.entity.SlotAdvType.I_BEACON;
import static com.moko.support.s.entity.SlotAdvType.NO_DATA;
import static com.moko.support.s.entity.SlotAdvType.SENSOR_INFO;
import static com.moko.support.s.entity.SlotAdvType.SLOT_TYPE_ARRAY;
import static com.moko.support.s.entity.SlotAdvType.TLM;
import static com.moko.support.s.entity.SlotAdvType.UID;
import static com.moko.support.s.entity.SlotAdvType.URL;

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
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.AppConstants;
import com.moko.bxp.s.ISlotDataAction;
import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.ActivitySlotDataSBinding;
import com.moko.bxp.s.dialog.LoadingMessageDialog;
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
import com.moko.support.s.entity.SlotAdvType;
import com.moko.support.s.entity.SlotData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SlotDataActivity extends BaseActivity implements NumberPickerView.OnValueChangeListener {
    private ActivitySlotDataSBinding mBind;
    private FragmentManager fragmentManager;
    private SensorInfoFragment sensorInfoFragment;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    private ISlotDataAction slotDataActionImpl;
    private int slot;
    private int currentFrameType;
    private int originFrameType;
    private SlotData originSlotData;
    private int accStatus;
    private int thStatus;
    private boolean isButtonPowerOff;
    private boolean isButtonReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySlotDataSBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        fragmentManager = getSupportFragmentManager();
        slot = getIntent().getIntExtra(AppConstants.SLOT, 0);
        accStatus = getIntent().getIntExtra(AppConstants.EXTRA_KEY2, 0);
        thStatus = getIntent().getIntExtra(AppConstants.EXTRA_KEY3, 0);
        isButtonReset = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY4, false);
        isButtonPowerOff = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY5, false);
        createFragments();
        mBind.npvSlotType.setDisplayedValues(SLOT_TYPE_ARRAY);
        mBind.npvSlotType.setMinValue(0);
        mBind.npvSlotType.setMaxValue(5);
        mBind.npvSlotType.setOnValueChangedListener(this);
        mBind.tvSlotTitle.setText("SLOT" + (slot + 1));
        EventBus.getDefault().register(this);
        mBind.rlTriggerSwitch.setOnClickListener(v -> {
            Intent intent = new Intent(this, TriggerStep1Activity.class);
            intent.putExtra(AppConstants.SLOT, slot);
            intent.putExtra(AppConstants.EXTRA_KEY2, accStatus);
            intent.putExtra(AppConstants.EXTRA_KEY3, thStatus);
            intent.putExtra(AppConstants.EXTRA_KEY4, isButtonReset);
            intent.putExtra(AppConstants.EXTRA_KEY5, isButtonPowerOff);
            startActivity(intent);
        });
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getNormalSlotAdvParams(slot));
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        uidFragment = UidFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
        urlFragment = UrlFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
        tlmFragment = TlmFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
        iBeaconFragment = IBeaconFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
        sensorInfoFragment = SensorInfoFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, sensorInfoFragment);
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_NORMAL_SLOT_ADV_PARAMS) {
                                //广播通道参数
                                dismissSyncProgressDialog();
                                if (length > 0) setSlotParams(value);
                            }
                        } else if (flag == 1) {
                            if (configKeyEnum == ParamsKeyEnum.KEY_NORMAL_SLOT_ADV_PARAMS) {
                                dismissSyncProgressDialog();
                                ToastUtils.showToast(this, (value[4] & 0xff) != 0xAA ? "Error" : "Successfully configure");
                                onBackPressed();
                            }
                        }
                    }
                }
            }
        });
    }

    private void setSlotParams(byte[] value) {
        originFrameType = value[13] & 0xff;
        int index = SlotAdvType.getSlotTypeIndex(value[13] & 0xff);
        mBind.npvSlotType.setValue(index);
        showFragment(index);
        if (currentFrameType == NO_DATA) return;
        SlotData slotData = new SlotData();
        slotData.advInterval = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
        slotData.advDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
        slotData.standbyDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 9, 11));
        slotData.rssi = value[11];
        slotData.txPower = value[12];
        slotData.currentFrameType = currentFrameType;
        slotData.slot = this.slot;
        if (currentFrameType == UID) {
            slotData.namespace = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 14, 24));
            slotData.instanceId = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 24, 30));
        } else if (currentFrameType == URL) {
            slotData.urlScheme = value[14] & 0xff;
            slotData.urlContent = new String(Arrays.copyOfRange(value, 15, value.length));
        } else if (currentFrameType == I_BEACON) {
            slotData.uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 14, 30));
            slotData.major = MokoUtils.toInt(Arrays.copyOfRange(value, 30, 32));
            slotData.minor = MokoUtils.toInt(Arrays.copyOfRange(value, 32, 34));
        } else if (currentFrameType == SENSOR_INFO) {
            int nameLength = value[14] & 0xff;
            slotData.deviceName = new String(Arrays.copyOfRange(value, 15, 15 + nameLength));
            slotData.tagId = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 16 + nameLength, value.length));
        }
        if (null != slotDataActionImpl) {
            originSlotData = slotData;
            slotDataActionImpl.setParams(slotData);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
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
            if (currentFrameType == originFrameType) {
                slotDataActionImpl.setParams(originSlotData);
            } else {
                SlotData slotData = new SlotData();
                slotData.slot = this.slot;
                slotData.currentFrameType = currentFrameType;
                slotDataActionImpl.setParams(slotData);
            }
        }
    }

    private void showFragment(int index) {
        currentFrameType = SlotAdvType.SLOT_TYPE[index];
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (currentFrameType) {
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
            case I_BEACON:
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
        mBind.rlTriggerSwitch.setVisibility(currentFrameType == NO_DATA ? View.GONE : View.VISIBLE);
    }

    public void onBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().unregister(this);
        setResult(RESULT_OK);
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (slotDataActionImpl == null) {
            SlotData slotData = new SlotData();
            slotData.slot = slot;
            slotData.currentFrameType = currentFrameType;
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setNormalSlotAdvParams(slotData));
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
