package com.moko.bxp.s.dialog;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.DialogLoadingSBinding;
import com.moko.bxp.s.view.ProgressDrawable;

public class LoadingDialog extends MokoBaseDialog<DialogLoadingSBinding> {
    public static final String TAG = LoadingDialog.class.getSimpleName();

    @Override
    protected DialogLoadingSBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogLoadingSBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        ProgressDrawable progressDrawable = new ProgressDrawable();
        progressDrawable.setColor(ContextCompat.getColor(getContext(), R.color.text_black_4d4d4d));
        mBind.ivLoading.setImageDrawable(progressDrawable);
        progressDrawable.start();
    }

    @Override
    public int getDialogStyle() {
        return R.style.CenterDialog;
    }

    @Override
    public int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public boolean getCancelOutside() {
        return false;
    }

    @Override
    public boolean getCancellable() {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ProgressDrawable) mBind.ivLoading.getDrawable()).stop();
    }
}
