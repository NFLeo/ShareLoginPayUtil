package com.shareutil.login.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.shareutil.BuildConfig;
import com.shareutil.LoginUtil;
import com.shareutil.ShareLogger;
import com.shareutil.ShareManager;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResultData;
import com.shareutil.login.result.BaseToken;
import com.shareutil.login.result.FacebookToken;
import com.shareutil.login.result.FacebookUser;

import org.json.JSONException;

import java.util.Arrays;

public class FBLoginInstance extends LoginInstance {

    private CallbackManager callbackManager;
    private GraphRequest request;

    public FBLoginInstance(Activity activity, final LoginListener listener,
                           final boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.setApplicationId(ShareManager.CONFIG.getFbClientId());
            FacebookSdk.sdkInitialize(activity.getApplicationContext());
            if (BuildConfig.DEBUG) {
                FacebookSdk.setIsDebugEnabled(true);
                FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
            }
        }

        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void doLogin(Activity activity, final LoginListener listener, boolean fetchUserInfo) {
        if (callbackManager == null) {
            ShareLogger.i(ShareLogger.INFO.LOGIN_ERROR);
            listener.loginFailure(new Exception(ShareLogger.INFO.LOGIN_ERROR), -1);
            LoginUtil.recycle();
            return;
        }
        ShareLogger.i("fb doLogin");
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                FacebookToken faceBookToken = new FacebookToken(accessToken);
                ShareLogger.i(ShareLogger.INFO.LOGIN_AUTH_SUCCESS);
                if (mFetchUserInfo) {
                    fetchUserInfo(faceBookToken);
                } else {
                    LoginUtil.recycle();
                }
            }

            @Override
            public void onCancel() {
                mLoginListener.loginCancel();
                LoginUtil.recycle();
            }

            @Override
            public void onError(FacebookException error) {
                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
                mLoginListener.loginFailure(new Exception(error), 303);
                LoginUtil.recycle();
            }
        });

        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile"));
    }

    @SuppressLint("CheckResult")
    @Override
    public void fetchUserInfo(final BaseToken token) {
        Bundle params = new Bundle();
        params.putString("fields", "picture,name,id,email,permissions");

        request = new GraphRequest(((FacebookToken) token).getAccessTokenBean(),
                "/me", params, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                try {
                    if (response != null && response.getJSONObject() != null) {
                        ShareLogger.i(response.getJSONObject().toString());
                        FacebookUser faceBookUser = new FacebookUser(response.getJSONObject());
                        mLoginListener.loginSuccess(new LoginResultData(LoginPlatform.FACEBOOK, token, faceBookUser));
                    } else {
                        mLoginListener.loginFailure(new JSONException("解析GraphResponse异常！！"), 303);
                    }
                    LoginUtil.recycle();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        request.executeAsync();
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean isInstall(Context context) {
        return false;
    }

    @Override
    public void recycle() {
        request = null;
        callbackManager = null;
        super.recycle();
    }
}
