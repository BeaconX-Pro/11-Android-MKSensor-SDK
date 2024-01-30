package com.moko.bxp.s.adapter;

import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.bxp.s.R;
import com.moko.bxp.s.entity.THStoreData;

public class THDataListAdapter extends BaseQuickAdapter<THStoreData, BaseViewHolder> {
    private final int deviceType;

    public THDataListAdapter(int deviceType) {
        super(R.layout.item_export_th_data);
        this.deviceType = deviceType;
    }

    @Override
    protected void convert(BaseViewHolder helper, THStoreData item) {
        TextView tvHum = helper.getView(R.id.tv_humidity);
        if (deviceType == 3) {
            tvHum.setVisibility(View.GONE);
        } else {
            tvHum.setVisibility(View.VISIBLE);
            helper.setText(R.id.tv_humidity, item.humidity);
        }
        helper.setText(R.id.tv_time, item.time);
        helper.setText(R.id.tv_temp, item.temp);
    }
}
