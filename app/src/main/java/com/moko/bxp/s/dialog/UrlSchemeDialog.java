package com.moko.bxp.s.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.moko.bxp.s.databinding.DialogUrlSchemeBinding;
import com.moko.support.s.entity.UrlSchemeEnum;

public class UrlSchemeDialog extends MokoBaseDialog<DialogUrlSchemeBinding> {
    public void renderConvertView(String urlScheme) {
        UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlDesc(urlScheme);
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

    private UrlSchemeClickListener urlSchemeClickListener;

    public void setUrlSchemeClickListener(UrlSchemeClickListener urlSchemeClickListener) {
        this.urlSchemeClickListener = urlSchemeClickListener;
    }

    @Override
    protected DialogUrlSchemeBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogUrlSchemeBinding.inflate(inflater,container,false);
    }

    public interface UrlSchemeClickListener {
        void onEnsureClicked(String urlType);
    }
}
