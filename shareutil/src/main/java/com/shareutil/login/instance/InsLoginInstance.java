package com.shareutil.login.instance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.shareutil.LoginUtil;
import com.shareutil.R;
import com.shareutil.ShareLogger;
import com.shareutil.ShareManager;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResultData;
import com.shareutil.login.result.BaseToken;
import com.shareutil.login.result.InsToken;
import com.shareutil.login.result.InsUser;

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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Describe :
 * Created by Leo on 2019/5/23 on 11:02.
 */
public class InsLoginInstance extends LoginInstance {

    public static final String sAuthorizationUrl = "https://api.instagram.com/oauth/authorize";
    public static final String sTokenUrl = "https://api.instagram.com/oauth/access_token";

    private String mClientId;
    private String mClientSecret;
    private String mRedirectURIs;
    private WebView mWebView;
    private FrameLayout webParentView;

    public InsLoginInstance(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        mClientId = ShareManager.CONFIG.getInsClientId();
        mClientSecret = ShareManager.CONFIG.getInsScope();
        mRedirectURIs = ShareManager.CONFIG.getInsRedirectURIs();
        mLoginListener = listener;
        mClient = new OkHttpClient.Builder().build();
    }

    @Override
    public void doLogin(final Activity activity, final LoginListener listener, final boolean fetchUserInfo) {
        mWebView = new WebView(activity);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAllowFileAccess(true);//资源加载超时操作
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setTextSize(WebSettings.TextSize.NORMAL);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NORMAL);
        webSettings.setAppCacheMaxSize(5 * 1048576);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setKeepScreenOn(true);
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                ShareLogger.i("ins doLogin");
                ShareLogger.i(url);
                if (url.startsWith(mRedirectURIs)) {
                    InsToken token = new InsToken(url);
                    ShareLogger.i(ShareLogger.INFO.LOGIN_AUTH_SUCCESS);
                    if (fetchUserInfo) {
                        listener.beforeFetchUserInfo(token);
                        fetchUserInfo(token);
                    } else {
                        listener.loginSuccess(new LoginResultData(LoginPlatform.INS, token));
                        LoginUtil.recycle();
                    }

                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                final SslErrorHandler mHandler;
                mHandler = sslErrorHandler;
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("ssl证书验证失败");
                builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.proceed();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.cancel();
                    }
                }).setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                            mHandler.cancel();
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        mWebView.loadUrl(buildAuthUrl());

        View decorView = activity.getWindow().getDecorView();
        webParentView = decorView.findViewById(R.id.id_share_container);
        if (webParentView.getChildCount() > 0) {
            webParentView.removeAllViews();
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        webParentView.addView(mWebView, layoutParams);
    }

    @Override
    public void fetchUserInfo(final BaseToken token) {
        mSubscribe = Flowable.create(new FlowableOnSubscribe<InsUser>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<InsUser> insUserEmitter) {

                Request post = new Request.Builder().url(sTokenUrl).post(buildUserInfoUrl(token)).build();
                try {
                    Response response = mClient.newCall(post).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    ShareLogger.i("auth:" + jsonObject.toString());
                    InsUser user = new InsUser(jsonObject);
                    insUserEmitter.onNext(user);
                    insUserEmitter.onComplete();
                } catch (IOException | JSONException e) {
                    insUserEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<InsUser>() {
                    @Override
                    public void accept(@NonNull InsUser insUser) {
                        mLoginListener.loginSuccess(new LoginResultData(LoginPlatform.INS, token, insUser));
                        LoginUtil.recycle();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) {
                        mLoginListener.loginFailure(new Exception(throwable), ShareLogger.INFO.ERR_FETCH_CODE);
                        LoginUtil.recycle();
                    }
                });
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public boolean isInstall(Context context) {
        return false;
    }

    @Override
    public void recycle() {

        //清空所有Cookie
        CookieSyncManager.createInstance(mActivity);  //Create a singleton CookieSyncManager within a context
        CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
        cookieManager.removeAllCookie();// Removes all cookies.
        CookieSyncManager.getInstance().sync(); // forces sync manager to sync now

        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.clearCache(true);

        webParentView.removeAllViews();
        mWebView = null;
        mClientSecret = null;
        mClientId = null;
        super.recycle();
    }

    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            mLoginListener.loginCancel();
            mActivity.finish();
            recycle();
        }
    }

    private String buildAuthUrl() {
        return sAuthorizationUrl + "?client_id=" + mClientId
                + "&response_type=code"
                + "&redirect_uri=" + mRedirectURIs;
    }

    private RequestBody buildUserInfoUrl(BaseToken token) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("client_id", mClientId);
        builder.add("client_secret", mClientSecret);
        builder.add("grant_type", "authorization_code");
        builder.add("redirect_uri", mRedirectURIs);
        builder.add("code", ((InsToken) token).getCode());
        return builder.build();
    }
}
