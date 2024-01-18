package com.moko.bxp.s.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.moko.bxp.s.R;
import com.moko.bxp.s.databinding.DialogLoadingMessageDBinding;
import com.moko.bxp.s.view.ProgressDrawable;

public class LoadingMessageDialog extends MokoBaseDialog<DialogLoadingMessageDBinding> {
    //    private static final int DIALOG_DISMISS_DELAY_TIME = 15000;
    public static final String TAG = LoadingMessageDialog.class.getSimpleName();
    private String message;
    private int messageId = -1;

    @Override
    protected DialogLoadingMessageDBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogLoadingMessageDBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        ProgressDrawable progressDrawable = new ProgressDrawable();
        progressDrawable.setColor(ContextCompat.getColor(getContext(), R.color.text_black_4d4d4d));
        mBind.ivLoading.setImageDrawable(progressDrawable);
        progressDrawable.start();
        if (messageId > 0) {
            message = getString(messageId);
        }
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.setting_syncing);
        }
        mBind.tvLoadingMessage.setText(message);
//        mBind.tvLoadingMessage.postDelayed(() -> {
//            if (isVisible()) {
//                dismissAllowingStateLoss();
//                if (callback != null) {
//                    callback.onOvertimeDismiss();
//                }
//            }
//        }, DIALOG_DISMISS_DELAY_TIME);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (callback != null) {
            callback.onDismiss();
        }
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

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(@StringRes int messageId) {
        this.messageId = messageId;
    }

    private DialogDissmissCallback callback;

    public void setDialogDismissCallback(final DialogDissmissCallback callback) {
        this.callback = callback;
    }

    public interface DialogDissmissCallback {
        void onOvertimeDismiss();

        void onDismiss();
    }
}
