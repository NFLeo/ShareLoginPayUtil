package com.shareutil.login.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.shareutil.LoginUtil;
import com.shareutil.ShareLogger;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResult;
import com.shareutil.login.result.BaseToken;
import com.shareutil.login.result.WeiboToken;
import com.shareutil.login.result.WeiboUser;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Describe :
 * Created by Leo on 2018/6/22.
 */
public class WeiboLoginInstance extends LoginInstance {

    private static final String USER_INFO = "https://api.weibo.com/2/users/show.json";

    private SsoHandler mSsoHandler;

    public WeiboLoginInstance(Activity activity, LoginListener listener, String appId, String redirectUrl, String scope, boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        AuthInfo authInfo = new AuthInfo(activity, appId, redirectUrl, scope);
        WbSdk.install(activity, authInfo);
        mSsoHandler = new SsoHandler(activity);
    }

    private class SelfWbAuthListener implements WbAuthListener {

        private LoginListener listener;
        private boolean fetchUserInfo;

        SelfWbAuthListener(LoginListener listener, boolean fetchUserInfo) {
            this.listener = listener;
            this.fetchUserInfo = fetchUserInfo;
        }

        @Override
        public void onSuccess(final Oauth2AccessToken token) {

            if (token == null) {
                listener.loginFailure(new Exception( ShareLogger.INFO.WEIBO_AUTH_ERROR), ShareLogger.INFO.ERR_GET_TOKEN_CODE);
                LoginUtil.recycle();
                return;
            }

            WeiboToken weiboToken = new WeiboToken(token);
            AccessTokenKeeper.writeAccessToken(mActivity, token);
            if (fetchUserInfo) {
                mLoginListener.beforeFetchUserInfo(weiboToken);
                fetchUserInfo(weiboToken);
            } else {
                mLoginListener.loginSuccess(new LoginResult(LoginPlatform.WEIBO, weiboToken));
                LoginUtil.recycle();
            }
        }

        @Override
        public void cancel() {
            if (mLoginListener != null) {
                mLoginListener.loginCancel();
                LoginUtil.recycle();
            }
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            if (mLoginListener != null) {
                mLoginListener.loginFailure(new Exception(errorMessage.getErrorMessage()), ShareLogger.INFO.ERR_WEIBO_AUTH_CODE);
                LoginUtil.recycle();
            }
        }
    }

    @Override
    public void doLogin(Activity activity, final LoginListener listener, final boolean fetchUserInfo) {
        if (mSsoHandler == null) {
            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WEIBO_LOGIN_ERROR), ShareLogger.INFO.ERR_WEIBO_AUTH_CODE);
            LoginUtil.recycle();
            return;
        }

        mSsoHandler.authorize(new SelfWbAuthListener(listener, fetchUserInfo));
    }

    @SuppressLint("CheckResult")
    @Override
    public void fetchUserInfo(final BaseToken token) {
        mSubscribe = Flowable.create(new FlowableOnSubscribe<WeiboUser>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<WeiboUser> weiboUserEmitter) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(buildUserInfoUrl(token, USER_INFO)).build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WeiboUser user = WeiboUser.parse(jsonObject);
                    weiboUserEmitter.onNext(user);
                } catch (IOException | JSONException e) {
                    ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR);
                    weiboUserEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeiboUser>() {
                    @Override
                    public void accept(@NonNull WeiboUser weiboUser) throws Exception {
                        mLoginListener.loginSuccess(
                                new LoginResult(LoginPlatform.WEIBO, token, weiboUser));
                        LoginUtil.recycle();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        mLoginListener.loginFailure(new Exception(throwable), ShareLogger.INFO.ERR_FETCH_CODE);
                        LoginUtil.recycle();
                    }
                });
    }

    private String buildUserInfoUrl(BaseToken token, String baseUrl) {
        return baseUrl + "?access_token=" + token.getAccessToken() + "&uid=" + token.getOpenid();
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean isInstall(Context context) {
        return WbSdk.isWbInstall(context);
    }

    @Override
    public void recycle() {
        super.recycle();
        mSsoHandler = null;
    }
}
