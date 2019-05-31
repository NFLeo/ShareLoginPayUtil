package com.shareutil.login.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shareutil.BuildConfig;
import com.shareutil.LoginUtil;
import com.shareutil.ShareLogger;
import com.shareutil.ShareManager;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResultData;
import com.shareutil.login.result.BaseToken;
import com.shareutil.login.result.TwitterToken;
import com.shareutil.login.result.TwitterUser;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class TwitterLoginInstance extends LoginInstance {

    private TwitterAuthClient mTwitterAuthClient;

    public TwitterLoginInstance(Activity activity, final LoginListener listener,
                                final boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);

        TwitterConfig config = new TwitterConfig.Builder(activity)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(ShareManager.CONFIG.getTwitterConsumerKey(),
                        ShareManager.CONFIG.getTwitterConsumerSecret()))
                .debug(BuildConfig.DEBUG).build();

        Twitter.initialize(config);
        mTwitterAuthClient = new TwitterAuthClient();
    }

    @Override
    public void doLogin(Activity activity, final LoginListener listener, boolean fetchUserInfo) {
        if (mTwitterAuthClient == null) {
            ShareLogger.i(ShareLogger.INFO.LOGIN_ERROR);
            listener.loginFailure(new Exception(ShareLogger.INFO.LOGIN_ERROR), -1);
            LoginUtil.recycle();
            return;
        }
        ShareLogger.i("twitter doLogin");
        mTwitterAuthClient.authorize(activity, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterToken twitterToken = new TwitterToken(result.data);
                ShareLogger.i(ShareLogger.INFO.LOGIN_AUTH_SUCCESS);
                if (mFetchUserInfo) {
                    mLoginListener.beforeFetchUserInfo(twitterToken);
                    fetchUserInfo(twitterToken);
                } else {
                    mLoginListener.loginSuccess(new LoginResultData(LoginPlatform.TWITTER, twitterToken));
                    LoginUtil.recycle();
                }
            }

            @Override
            public void failure(TwitterException exception) {
                mLoginListener.loginFailure(exception, 303);
                LoginUtil.recycle();
            }
        });
    }

    @SuppressLint("CheckResult")
    @Override
    public void fetchUserInfo(final BaseToken token) {
        mSubscribe = Flowable.create(new FlowableOnSubscribe<TwitterUser>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<TwitterUser> userEmitter) {

                TwitterApiClient apiClient = TwitterCore.getInstance().getApiClient();
                Call<User> userCall = apiClient.getAccountService().verifyCredentials(true, false, false);

                try {
                    Response<User> execute = userCall.execute();
                    userEmitter.onNext(new TwitterUser(execute.body()));
                    userEmitter.onComplete();
                } catch (Exception e) {
                    ShareLogger.e(ShareLogger.INFO.FETCH_USER_INOF_ERROR);
                    userEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<TwitterUser>() {
                    @Override
                    public void accept(@NonNull TwitterUser user) {
                        mLoginListener.loginSuccess(new LoginResultData(LoginPlatform.TWITTER, token, user));
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
        if (mTwitterAuthClient == null) return;
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean isInstall(Context context) {
        return false;
    }

    @Override
    public void recycle() {
        mTwitterAuthClient = null;
        super.recycle();
    }
}
