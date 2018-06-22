package com.shareutil.login.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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
    private Context context;
    private LoginListener mLoginListener;

    public WeiboLoginInstance(Activity activity, LoginListener listener, String appId, String redirectUrl, String scope, boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        this.context = activity;
        AuthInfo authInfo = new AuthInfo(activity, appId, redirectUrl, scope);
        WbSdk.install(activity, authInfo);
        mSsoHandler = new SsoHandler(activity);
        mLoginListener = listener;
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
                listener.loginFailure(new Exception("授权失败"));
                return;
            }

            WeiboToken weiboToken = WeiboToken.parse(token);
            AccessTokenKeeper.writeAccessToken(context, token);
            if (fetchUserInfo) {
                mLoginListener.beforeFetchUserInfo(weiboToken);
                fetchUserInfo(weiboToken);
            } else {
                mLoginListener.loginSuccess(new LoginResult(LoginPlatform.WEIBO, weiboToken));
            }
        }

        @Override
        public void cancel() {
            if (mLoginListener != null) {
                mLoginListener.loginCancel();
            }
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            if (mLoginListener != null) {
                mLoginListener.loginFailure(new Exception(errorMessage.getErrorMessage()));
            }
        }
    }

    @Override
    public void doLogin(Activity activity, final LoginListener listener, final boolean fetchUserInfo) {
        mSsoHandler.authorize(new SelfWbAuthListener(listener, fetchUserInfo));
    }

    @SuppressLint("CheckResult")
    @Override
    public void fetchUserInfo(final BaseToken token) {
        Flowable.create(new FlowableOnSubscribe<WeiboUser>() {
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
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        mLoginListener.loginFailure(new Exception(throwable));
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
        return mSsoHandler.isWbAppInstalled();
    }

    @Override
    public void recycle() {
        mSsoHandler = null;
        mLoginListener = null;
    }
}
