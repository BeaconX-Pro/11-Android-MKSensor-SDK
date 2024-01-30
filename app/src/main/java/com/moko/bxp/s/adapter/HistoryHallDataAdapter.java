package com.moko.bxp.s.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.bxp.s.R;
import com.moko.bxp.s.entity.HallHistoryBean;

/**
 * @author: jun.liu
 * @date: 2024/1/26 18:24
 * @des:
 */
public class HistoryHallDataAdapter extends BaseQuickAdapter<HallHistoryBean, BaseViewHolder> {
    public HistoryHallDataAdapter() {
        super(R.layout.item_export_hall_data);
    }

    @Override
    protected void convert(BaseViewHolder helper, HallHistoryBean item) {
        helper.setText(R.id.tv_time, item.time);
        helper.setText(R.id.tvStatus, item.status);
    }
}
