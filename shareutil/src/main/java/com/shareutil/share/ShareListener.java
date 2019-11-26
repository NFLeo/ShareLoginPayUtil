package com.shareutil.share;

import com.shareutil.ShareLogger;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

public abstract class ShareListener implements IUiListener {
    @Override
    public final void onComplete(Object o) {
        shareSuccess();
    }

    @Override
    public final void onError(UiError uiError) {
        shareFailure(new Exception(uiError == null ? ShareLogger.INFO.DEFAULT_QQ_SHARE_ERROR : uiError.errorDetail));
    }

    @Override
    public final void onCancel() {
        shareCancel();
    }

    public abstract void shareStart();

    public abstract void shareSuccess();

    public abstract void shareFailure(Exception e);

    public abstract void shareCancel();
}
