package com.shareutil.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.shareutil.login.LoginListener;
import com.shareutil.login.result.BaseToken;

import io.reactivex.disposables.Disposable;

public abstract class LoginInstance {

    LoginListener mLoginListener;
    Activity mActivity;
    Disposable mSubscribe;
    boolean mFetchUserInfo;

    LoginInstance(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        mActivity = activity;
        mLoginListener = listener;
        mFetchUserInfo = fetchUserInfo;
    }

    public abstract void doLogin(Activity activity, LoginListener listener, boolean fetchUserInfo);

    public abstract void fetchUserInfo(BaseToken token);

    public abstract void handleResult(int requestCode, int resultCode, Intent data);

    public abstract boolean isInstall(Context context);

    public void recycle() {
        if (mSubscribe != null && !mSubscribe.isDisposed()) {
            mSubscribe.dispose();
            mSubscribe = null;
        }

        mLoginListener = null;
        mActivity = null;
    }
}
