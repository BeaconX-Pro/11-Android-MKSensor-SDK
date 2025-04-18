package com.moko.bxp.s.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.elvishew.xlog.XLog;
import com.moko.bxp.s.databinding.DialogUrlSchemeSBinding;
import com.moko.lib.bxpui.dialog.MokoBaseDialog;
import com.moko.support.s.entity.UrlSchemeEnum;

public class UrlSchemeDialog extends MokoBaseDialog<DialogUrlSchemeSBinding> {
    private String urlScheme;
    private UrlSchemeClickListener urlSchemeClickListener;
    public UrlSchemeDialog(){}
    public UrlSchemeDialog(String urlScheme){
        this.urlScheme = urlScheme;
        XLog.i("*************");
    }

    private void renderConvertView() {
        UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlDesc(urlScheme);
        if (null == urlSchemeEnum) return;
        switch (urlSchemeEnum.getUrlType()) {
            case 0:
                mBind.rbHttpWww.setChecked(true);
                break;
            case 1:
                mBind.rbHttpsWww.setChecked(true);
                break;
            case 2:
                mBind.rbHttp.setChecked(true);
                break;
            case 3:
                mBind.rbHttps.setChecked(true);
                break;
        }
    }

    @Override
    protected void onCreateView() {
        XLog.i("*************/////////////");
        renderConvertView();
        mBind.tvCancel.setOnClickListener(v -> dismiss());
        mBind.tvEnsure.setOnClickListener(v -> {
            dismiss();
            String tag;
            if (mBind.rbHttpWww.isChecked()) tag = "0";
            else if (mBind.rbHttpsWww.isChecked()) tag = "1";
            else if (mBind.rbHttp.isChecked()) tag = "2";
            else tag = "3";
            urlSchemeClickListener.onEnsureClicked(tag);
        });
    }

    public void setUrlSchemeClickListener(UrlSchemeClickListener urlSchemeClickListener) {
        this.urlSchemeClickListener = urlSchemeClickListener;
    }

    @Override
    protected DialogUrlSchemeSBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogUrlSchemeSBinding.inflate(inflater,container,false);
    }

    public interface UrlSchemeClickListener {
        void onEnsureClicked(String urlType);
    }
}
