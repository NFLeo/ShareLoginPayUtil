package com.shareutil.login.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.shareutil.LoginUtil;
import com.shareutil.ShareLogger;
import com.shareutil.ShareManager;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResultData;
import com.shareutil.login.result.BaseToken;
import com.shareutil.login.result.GoogleToken;
import com.shareutil.login.result.GoogleUser;

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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Describe : google登录
 * Created by Leo on 2019/5/27.
 */
public class GoogleLoginInstance extends LoginInstance {

    public static final int RC_SIGN_IN = 10009;
    private static final String TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";
    private static final String USER_INFO = "https://www.googleapis.com/oauth2/v2/userinfo";

    private GoogleSignInClient mGoogleSignInClient;
    private String mClientId;
    private String mClientSecret;
    private Disposable mTaskSubscribe;
    private GoogleToken googleToken;

    public GoogleLoginInstance(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        mClientId = ShareManager.CONFIG.getGoogleClientId();
        mClientSecret = ShareManager.CONFIG.getGoogleClientSecret();
        mClient = new OkHttpClient.Builder().build();

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(mClientId)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER), new Scope(Scopes.OPEN_ID), new Scope(Scopes.PLUS_ME))
                .requestServerAuthCode(mClientId)
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(activity, signInOptions);
    }

    @Override
    public void doLogin(Activity activity, final LoginListener listener, final boolean fetchUserInfo) {
        if (mGoogleSignInClient == null) {
            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.LOGIN_ERROR), ShareLogger.INFO.ERR_AUTH_CODE);
            LoginUtil.recycle();
            return;
        }

        ShareLogger.i("gg doLogin");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(final Task<GoogleSignInAccount> task) {
        mTaskSubscribe = Flowable.create(new FlowableOnSubscribe<GoogleToken>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<GoogleToken> googleTokenEmitter) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    googleToken = new GoogleToken(account);
                    googleToken.setServerAuthCode(googleToken.getSignInAccount().getServerAuthCode());

                    Request request = new Request.Builder().url(TOKEN_URL).post(buildAuthTokenUrl(googleToken)).build();
                    try {
                        Response response = mClient.newCall(request).execute();
                        ShareLogger.i(ShareLogger.INFO.LOGIN_AUTH_SUCCESS);
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        ShareLogger.i("auth:" + jsonObject.toString());

                        googleToken.parse(jsonObject);
                        googleTokenEmitter.onNext(googleToken);
                        googleTokenEmitter.onComplete();
                    } catch (IOException | JSONException e) {
                        ShareLogger.e(ShareLogger.INFO.ERR_AUTH_ERROR);
                        googleTokenEmitter.onError(e);
                    }
                } catch (ApiException e) {
                    ShareLogger.e(ShareLogger.INFO.ERR_AUTH_ERROR);
                    googleTokenEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<GoogleToken>() {
                    @Override
                    public void accept(@NonNull GoogleToken googleToken) throws Exception {
                        if (mFetchUserInfo) {
                            mLoginListener.beforeFetchUserInfo(googleToken);
                            fetchUserInfo(googleToken);
                        } else {
                            mLoginListener.loginSuccess(new LoginResultData(LoginPlatform.GOOGLE, googleToken));
                            LoginUtil.recycle();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        mLoginListener.loginFailure(new Exception(throwable), ShareLogger.INFO.ERR_AUTH_ERROR_CODE);
                        LoginUtil.recycle();
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void fetchUserInfo(final BaseToken token) {
        mSubscribe = Flowable.create(new FlowableOnSubscribe<GoogleUser>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<GoogleUser> userEmitter) {

                Request request = new Request.Builder().url(buildUserInfoUrl((GoogleToken) token)).build();
                try {
                    Response response = mClient.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    GoogleUser user = new GoogleUser(jsonObject);
                    userEmitter.onNext(user);
                    userEmitter.onComplete();
                } catch (IOException | JSONException e) {
                    ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR);
                    userEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<GoogleUser>() {
                    @Override
                    public void accept(@NonNull GoogleUser user) throws Exception {
                        mLoginListener.loginSuccess(
                                new LoginResultData(LoginPlatform.GOOGLE, token, user));
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

    private RequestBody buildAuthTokenUrl(GoogleToken token) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("client_id", mClientId);
        builder.add("client_secret", mClientSecret);
        builder.add("grant_type", "authorization_code");
        builder.add("code", token.getServerAuthCode());
        return builder.build();
    }

    private String buildUserInfoUrl(GoogleToken token) {
        return USER_INFO + "?access_token=" + token.getAccessToken();
    }

    @Override
    public boolean isInstall(Context context) {
        return false;
    }

    @Override
    public void recycle() {

        if (mTaskSubscribe != null && !mTaskSubscribe.isDisposed()) {
            mTaskSubscribe.dispose();
        }

        mGoogleSignInClient = null;
        super.recycle();
    }
}
