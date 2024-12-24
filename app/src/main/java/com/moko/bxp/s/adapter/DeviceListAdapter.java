package com.moko.bxp.s.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.bxp.s.R;
import com.moko.bxp.s.entity.AdvIBeacon;
import com.moko.bxp.s.entity.AdvInfo;
import com.moko.bxp.s.entity.AdvSensorInfo;
import com.moko.bxp.s.entity.AdvTLM;
import com.moko.bxp.s.entity.AdvUID;
import com.moko.bxp.s.entity.AdvURL;
import com.moko.bxp.s.utils.AdvInfoParser;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseQuickAdapter<AdvInfo, BaseViewHolder> {
    public DeviceListAdapter() {
        super(R.layout.list_item_device_s);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void convert(BaseViewHolder helper, AdvInfo item) {
        helper.setText(R.id.tv_name, TextUtils.isEmpty(item.name) ? "N/A" : item.name);
        helper.setText(R.id.tv_mac, "MAC:" + item.mac);
        helper.setText(R.id.tv_rssi, String.format("%ddBm", item.rssi));
        helper.setText(R.id.tv_interval_time, item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime));
        helper.setText(R.id.tv_battery, item.battery < 0 ? "N/A" : item.battery <= 100 ? item.battery + "%" : item.battery + "mV");
        helper.addOnClickListener(R.id.tv_connect);
        helper.setGone(R.id.tv_connect, item.connectState > 0);
        helper.setVisible(R.id.tv_tag_id, false);
        LinearLayout parent = helper.getView(R.id.ll_adv_info);
        parent.removeAllViews();
        ArrayList<AdvInfo.ValidData> validDataList = new ArrayList<>(item.validDataHashMap.values());
        for (AdvInfo.ValidData validData : validDataList) {
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_UID) {
                parent.addView(createUIDView(AdvInfoParser.getUID(validData.data), parent));
            }
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_URL) {
                parent.addView(createURLView(AdvInfoParser.getURL(validData.data), parent));
            }
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_TLM) {
                parent.addView(createTLMView(AdvInfoParser.getTLM(validData.data), parent));
            }
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_IBEACON || validData.type == AdvInfo.VALID_DATA_TYPE_IBEACON_APPLE) {
                AdvIBeacon beaconXiBeacon = AdvInfoParser.getIBeacon(item.rssi, validData.data, validData.type);
                beaconXiBeacon.txPower = validData.txPower == Integer.MIN_VALUE ? "N/A" : String.valueOf(validData.txPower);
                parent.addView(createIBeaconView(beaconXiBeacon, parent));
            }
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_SENSOR_INFO) {
                parent.addView(createSensorView(AdvInfoParser.getSensorInfo(validData.data), parent));
                helper.setVisible(R.id.tv_tag_id, true);
                helper.setText(R.id.tv_tag_id, String.format("Tag ID:0x%s", validData.data.substring(36)));
            }else {
                helper.setVisible(R.id.tv_tag_id, false);
            }
        }
    }

    private View createUIDView(AdvUID uid, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_uid_s, parent, false);
        TextView tvRSSI0M = view.findViewById(R.id.tv_rssi_0m);
        TextView tvNameSpace = view.findViewById(R.id.tv_namespace);
        TextView tvInstanceId = view.findViewById(R.id.tv_instance_id);
        tvRSSI0M.setText(String.format("%sdBm", uid.rssi));
        tvNameSpace.setText("0x" + uid.namespaceId.toUpperCase());
        tvInstanceId.setText("0x" + uid.instanceId.toUpperCase());
        return view;
    }

    private View createURLView(final AdvURL url, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_url_s, parent, false);
        TextView tvRSSI0M = view.findViewById(R.id.tv_rssi_0m);
        TextView tvUrl = view.findViewById(R.id.tv_url);
        tvRSSI0M.setText(String.format("%sdBm", url.rssi));
        tvUrl.setText(url.url);
        tvUrl.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvUrl.getPaint().setAntiAlias(true);//抗锯齿
        tvUrl.setOnClickListener(v -> {
            Uri uri = Uri.parse(url.url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(intent);
        });
        return view;
    }

    private View createTLMView(AdvTLM tlm, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_tlm_s, parent, false);
        TextView tv_vbatt = view.findViewById(R.id.tv_vbatt);
        TextView tv_temp = view.findViewById(R.id.tv_temp);
        TextView tv_adv_cnt = view.findViewById(R.id.tv_adv_cnt);
        TextView tv_sec_cnt = view.findViewById(R.id.tv_sec_cnt);
        tv_vbatt.setText(String.format("%smV", tlm.vbatt));
        tv_temp.setText(tlm.temp);
        tv_adv_cnt.setText(tlm.adv_cnt);
        tv_sec_cnt.setText(tlm.sec_cnt);
        return view;
    }

    private View createIBeaconView(AdvIBeacon iBeacon, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_ibeacon_s, parent, false);
        TextView tv_tx_power = view.findViewById(R.id.tv_tx_power);
        TextView tv_rssi_1m = view.findViewById(R.id.tv_rssi_1m);
        TextView tv_uuid = view.findViewById(R.id.tv_uuid);
        TextView tv_major = view.findViewById(R.id.tv_major);
        TextView tv_minor = view.findViewById(R.id.tv_minor);
        TextView tv_proximity_state = view.findViewById(R.id.tv_proximity_state);

        tv_rssi_1m.setText(String.format("%sdBm", iBeacon.rssi));
        tv_tx_power.setText("N/A".equals(iBeacon.txPower) ? iBeacon.txPower : (iBeacon.txPower + "dBm"));
        tv_proximity_state.setText(iBeacon.distanceDesc);
        tv_uuid.setText(iBeacon.uuid);
        tv_major.setText(iBeacon.major);
        tv_minor.setText(iBeacon.minor);
        return view;
    }

    private View createSensorView(AdvSensorInfo tag, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_sensor_info_s, parent, false);
        TextView tvMagneticStatus = view.findViewById(R.id.tv_magnetic_status);
        TextView tvMagneticTriggerCount = view.findViewById(R.id.tv_magnetic_trigger_count);
        LinearLayout llAccInfo = view.findViewById(R.id.ll_acc_info);
        TextView tvMotionStatus = view.findViewById(R.id.tv_motion_status);
        TextView tvMotionTriggerCount = view.findViewById(R.id.tv_motion_trigger_count);
        TextView tvAcc = view.findViewById(R.id.tv_acc);
        TextView tvTemp = view.findViewById(R.id.tvTemp);
        TextView tvHum = view.findViewById(R.id.tvHum);
        RelativeLayout layoutTemp = view.findViewById(R.id.layoutTemp);
        RelativeLayout layoutHum = view.findViewById(R.id.layoutHum);
        tvMagneticStatus.setText(tag.hallStatus);
        tvMagneticTriggerCount.setText(tag.hallTriggerCount);
        if (tag.isAccEnable) {
            llAccInfo.setVisibility(View.VISIBLE);
            tvMotionStatus.setText(tag.motionStatus);
            tvMotionTriggerCount.setText(tag.motionTriggerCount);
            tvAcc.setText(String.format("%s;%s;%s", tag.accX, tag.accY, tag.accZ));
        } else {
            llAccInfo.setVisibility(View.GONE);
        }
        if (tag.tempEnable) {
            layoutTemp.setVisibility(View.VISIBLE);
            tvTemp.setText(tag.temp);
        } else {
            layoutTemp.setVisibility(View.GONE);
        }
        if (tag.humEnable) {
            layoutHum.setVisibility(View.VISIBLE);
            tvHum.setText(tag.hum);
        } else {
            layoutHum.setVisibility(View.GONE);
        }
        return view;
    }
}
