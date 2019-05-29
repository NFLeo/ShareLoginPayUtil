package com.shareutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.instance.FBLoginInstance;
import com.shareutil.login.instance.GoogleLoginInstance;
import com.shareutil.login.instance.InsLoginInstance;
import com.shareutil.login.instance.LoginInstance;
import com.shareutil.login.instance.QQLoginInstance;
import com.shareutil.login.instance.WeiboLoginInstance;
import com.shareutil.login.instance.WxLoginInstance;

public class LoginUtil {

    private static LoginInstance mLoginInstance;

    private static LoginListener mLoginListener;

    private static int mPlatform;

    private static boolean isFetchUserInfo;

    static final int TYPE = 799;

    public static void login(Context context, @LoginPlatform.Platform int platform, LoginListener listener) {
        login(context, platform, listener, true);
    }

    public static void login(Context context, @LoginPlatform.Platform int platform,
                             LoginListener listener, boolean fetchUserInfo) {
        mPlatform = platform;
        mLoginListener = listener;
        isFetchUserInfo = fetchUserInfo;
        _ShareActivity.newInstance(context, TYPE);
    }

    static void action(Activity activity) {
        switch (mPlatform) {
            case LoginPlatform.QQ:
                mLoginInstance = new QQLoginInstance(activity, mLoginListener, isFetchUserInfo);
                break;
            case LoginPlatform.WEIBO:
                mLoginInstance = new WeiboLoginInstance(activity, mLoginListener, ShareManager.CONFIG.getWeiboId(),
                        ShareManager.CONFIG.getWeiboRedirectUrl(), ShareManager.CONFIG.getWeiboScope(), isFetchUserInfo);
                break;
            case LoginPlatform.WX:
                mLoginInstance = new WxLoginInstance(activity, mLoginListener, isFetchUserInfo);
                if (!mLoginInstance.isInstall(activity)) {
                    mLoginListener.loginFailure(new Exception(ShareLogger.INFO.NOT_INSTALL), ShareLogger.INFO.NOT_INSTALL_CODE);
                    recycle();
                    activity.finish();
                    return;
                }
                break;
            case LoginPlatform.INS:
                mLoginInstance = new InsLoginInstance(activity, mLoginListener, isFetchUserInfo);
                break;
            case LoginPlatform.FACEBOOK:
                mLoginInstance = new FBLoginInstance(activity, mLoginListener, isFetchUserInfo);
                break;
            case LoginPlatform.GOOGLE:
                mLoginInstance = new GoogleLoginInstance(activity, mLoginListener, isFetchUserInfo);
                break;
            default:
                mLoginListener.loginFailure(new Exception(ShareLogger.INFO.UNKNOW_PLATFORM), ShareLogger.INFO.UNKNOW_PLATFORM_CODE);
                activity.finish();
                return;
        }
        mLoginInstance.doLogin(activity, mLoginListener, isFetchUserInfo);
    }

    static void handleResult(int requestCode, int resultCode, Intent data) {
        if (mLoginInstance != null) {
            mLoginInstance.handleResult(requestCode, resultCode, data);
        }
    }

    public static int getPlatform() {
        return mPlatform;
    }

    public static void onBackPressed() {
        if (mLoginInstance instanceof InsLoginInstance) {
            ((InsLoginInstance) mLoginInstance).onBackPressed();
        }
    }

    public static void recycle() {
        if (mLoginInstance != null) {
            mLoginInstance.recycle();
        }
        mLoginInstance = null;
        mLoginListener = null;
        mPlatform = 0;
        isFetchUserInfo = false;
    }
}
