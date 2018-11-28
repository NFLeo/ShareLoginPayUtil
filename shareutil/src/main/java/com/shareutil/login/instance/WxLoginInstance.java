package com.shareutil.login.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.shareutil.ShareLogger;
import com.shareutil.ShareManager;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResult;
import com.shareutil.login.result.BaseToken;
import com.shareutil.login.result.WxToken;
import com.shareutil.login.result.WxUser;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WxLoginInstance extends LoginInstance {

    private static final String SCOPE_USER_INFO = "snsapi_userinfo";
    private static final String SCOPE_BASE = "snsapi_base";
    private Disposable mTokenSubscribe;

    private static final String BASE_URL = "https://api.weixin.qq.com/sns/";

    private IWXAPI mIWXAPI;

    private LoginListener mLoginListener;

    private OkHttpClient mClient;

    private boolean fetchUserInfo;

    public WxLoginInstance(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        mLoginListener = listener;
        mIWXAPI = WXAPIFactory.createWXAPI(activity, ShareManager.CONFIG.getWxId());

        mClient = new OkHttpClient();
        this.fetchUserInfo = fetchUserInfo;
    }

    @Override
    public void doLogin(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = SCOPE_USER_INFO;
        req.state = String.valueOf(System.currentTimeMillis());
        mIWXAPI.sendReq(req);
    }

    @SuppressLint("CheckResult")
    private void getToken(final String code) {
        mTokenSubscribe = Flowable.create(new FlowableOnSubscribe<WxToken>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<WxToken> wxTokenEmitter) {
                Request request = new Request.Builder().url(buildTokenUrl(code)).build();
                try {
                    Response response = mClient.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WxToken token = WxToken.parse(jsonObject);
                    wxTokenEmitter.onNext(token);
                } catch (IOException | JSONException e) {
                    wxTokenEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WxToken>() {
                    @Override
                    public void accept(@NonNull WxToken wxToken) {
                        if (fetchUserInfo) {
                            mLoginListener.beforeFetchUserInfo(wxToken);
                            fetchUserInfo(wxToken);
                        } else {
                            mLoginListener.loginSuccess(new LoginResult(LoginPlatform.WX, wxToken));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) {
                        mLoginListener.loginFailure(new Exception(throwable.getMessage()), ShareLogger.INFO.ERR_GET_TOKEN_CODE);
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void fetchUserInfo(final BaseToken token) {
        mSubscribe = Flowable.create(new FlowableOnSubscribe<WxUser>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<WxUser> wxUserEmitter) {
                Request request = new Request.Builder().url(buildUserInfoUrl(token)).build();
                try {
                    Response response = mClient.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WxUser user = WxUser.parse(jsonObject);
                    wxUserEmitter.onNext(user);
                } catch (IOException | JSONException e) {
                    wxUserEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WxUser>() {
                    @Override
                    public void accept(@NonNull WxUser wxUser) {
                        mLoginListener.loginSuccess(
                                new LoginResult(LoginPlatform.WX, token, wxUser));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) {
                        mLoginListener.loginFailure(new Exception(throwable), ShareLogger.INFO.ERR_FETCH_CODE);
                    }
                });
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        mIWXAPI.handleIntent(data, new IWXAPIEventHandler() {
            @Override
            public void onReq(BaseReq baseReq) {
            }

            @Override
            public void onResp(BaseResp baseResp) {
                if (baseResp instanceof SendAuth.Resp && baseResp.getType() == 1) {
                    SendAuth.Resp resp = (SendAuth.Resp) baseResp;
                    switch (resp.errCode) {
                        case BaseResp.ErrCode.ERR_OK:
                            getToken(resp.code);
                            break;
                        case BaseResp.ErrCode.ERR_USER_CANCEL:
                            mLoginListener.loginCancel();
                            break;
                        case BaseResp.ErrCode.ERR_SENT_FAILED:
                            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WX_ERR_SENT_FAILED), ShareLogger.INFO.WX_ERR_SENT_FAILED_CODE);
                            break;
                        case BaseResp.ErrCode.ERR_UNSUPPORT:
                            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WX_ERR_UNSUPPORT), ShareLogger.INFO.WX_ERR_UNSUPPORT_CODE);
                            break;
                        case BaseResp.ErrCode.ERR_AUTH_DENIED:
                            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WX_ERR_AUTH_DENIED), ShareLogger.INFO.WX_ERR_AUTH_DENIED_CODE);
                            break;
                        default:
                            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WX_ERR_AUTH_ERROR), ShareLogger.INFO.WX_ERR_AUTH_ERROR_CODE);
                    }
                }
            }
        });
    }

    @Override
    public boolean isInstall(Context context) {
        return mIWXAPI.isWXAppInstalled();
    }

    @Override
    public void recycle() {
        if (mSubscribe != null && !mSubscribe.isDisposed()) {
            mSubscribe.dispose();
        }
        if (mTokenSubscribe != null && !mTokenSubscribe.isDisposed()) {
            mTokenSubscribe.dispose();
        }
        if (mIWXAPI != null) {
            mIWXAPI.detach();
            mIWXAPI = null;
        }
    }

    private String buildTokenUrl(String code) {
        return BASE_URL
                + "oauth2/access_token?appid=" + ShareManager.CONFIG.getWxId()
                + "&secret=" + ShareManager.CONFIG.getWxSecret()
                + "&code=" + code
                + "&grant_type=authorization_code";
    }

    private String buildUserInfoUrl(BaseToken token) {
        return BASE_URL
                + "userinfo?access_token=" + token.getAccessToken()
                + "&openid=" + token.getOpenid();
    }
}