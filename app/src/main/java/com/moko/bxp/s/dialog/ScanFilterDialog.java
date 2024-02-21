package com.moko.bxp.s.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.widget.SeekBar;

import com.moko.bxp.s.databinding.DialogScanFilterACBinding;

public class ScanFilterDialog extends BaseDialog<DialogScanFilterACBinding> {
    private int filterRssi;
    private String filterMac;
    private String filterName;
    private String filterTagId;

    @Override
    protected DialogScanFilterACBinding getViewBind() {
        return DialogScanFilterACBinding.inflate(getLayoutInflater());
    }

    public ScanFilterDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate() {
        mBind.tvRssi.setText(String.format("%sdBm", filterRssi + ""));
        mBind.sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssi = progress - 100;
                mBind.tvRssi.setText(String.format("%sdBm", rssi + ""));
                filterRssi = rssi;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBind.sbRssi.setProgress(filterRssi + 100);
        if (!TextUtils.isEmpty(filterName)) {
            mBind.etFilterName.setText(filterName);
            mBind.etFilterName.setSelection(filterName.length());
        }
        if (!TextUtils.isEmpty(filterTagId)) {
            mBind.etFilterTagId.setText(filterTagId);
            mBind.etFilterTagId.setSelection(mBind.etFilterTagId.getText().length());
        }
        setDismissEnable(true);
        if (!TextUtils.isEmpty(filterMac)) {
            mBind.etFilterMac.setText(filterMac);
            mBind.etFilterMac.setSelection(filterMac.length());
        }
        mBind.ivFilterMacDelete.setOnClickListener(v -> mBind.etFilterMac.setText(""));
        mBind.ivFilterNameDelete.setOnClickListener(v -> mBind.etFilterName.setText(""));
        mBind.ivFilterTagIdDelete.setOnClickListener(v -> mBind.etFilterTagId.setText(""));
        mBind.tvDone.setOnClickListener(v -> {
            listener.onDone(mBind.etFilterName.getText().toString(), mBind.etFilterMac.getText().toString(), filterRssi, mBind.etFilterTagId.getText().toString());
            dismiss();
        });
    }

    private OnScanFilterListener listener;

    public void setOnScanFilterListener(OnScanFilterListener listener) {
        this.listener = listener;
    }

    public void setFilterMac(String filterMac) {
        this.filterMac = filterMac;
    }

    public void setFilterRssi(int filterRssi) {
        this.filterRssi = filterRssi;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setFilterTagId(String filterTagId) {
        this.filterTagId = filterTagId;
    }

    public interface OnScanFilterListener {
        void onDone(String filterName, String filterMac, int filterRssi, String filterTagId);
    }
}
