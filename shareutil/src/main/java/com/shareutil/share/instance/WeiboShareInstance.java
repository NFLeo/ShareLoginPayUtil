package com.shareutil.share.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Pair;

import com.shareutil.ShareUtil;
import com.shareutil.share.ImageDecoder;
import com.shareutil.share.ShareImageObject;
import com.shareutil.share.ShareListener;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Describe :
 * Created by Leo on 2018/6/22.
 */
public class WeiboShareInstance implements ShareInstance {

    private WbShareHandler shareHandler;
    private Disposable mShareImage;

    private static final int TARGET_SIZE = 1024;
    private static final int TARGET_LENGTH = 2097152;

    public WeiboShareInstance(Context context, String appId, String redirectUrl, String scope) {
        AuthInfo authInfo = new AuthInfo(context, appId, redirectUrl, scope);
        WbSdk.install(context, authInfo);
        shareHandler = new WbShareHandler((Activity) context);
        shareHandler.registerApp();
    }

    @Override
    public void shareText(int platform, String text, Activity activity, ShareListener listener) {
        WeiboMultiMessage message = new WeiboMultiMessage();
        message.textObject = getTextObj("", "", text);
        shareHandler.shareMessage(message, false);
    }

    @Override
    public void shareMedia(int platform, String title, String targetUrl, String summary, String miniId, String miniPath, ShareImageObject shareImageObject, Activity activity, ShareListener listener) {
        shareTextOrImage(shareImageObject, title, targetUrl, summary, activity, listener);
    }

    @Override
    public void shareMedia(int platform, String title, String targetUrl, String summary, String miniId, String miniPath, ShareImageObject shareImageObject, boolean shareImmediate, Activity activity, ShareListener listener) {
        if (shareImmediate) {
            if (shareImageObject.getPair() != null) {
                shareTextOrImage(shareImageObject.getPair(), title, targetUrl, summary, activity, listener);
            }
        } else {
            shareTextOrImage(shareImageObject, title, targetUrl, summary, activity, listener);
        }
    }

    @Override
    public void shareImage(int platform, ShareImageObject shareImageObject, Activity activity,
                           ShareListener listener) {
        shareTextOrImage(shareImageObject, "", "", "", activity, listener);
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent intent) {
        if (shareHandler == null) {
            return;
        }

        shareHandler.doResultIntent(intent, new WbShareCallback() {
            @Override
            public void onWbShareSuccess() {
                ShareUtil.mShareListener.shareSuccess();
                ShareUtil.recycle();
            }

            @Override
            public void onWbShareCancel() {
                ShareUtil.mShareListener.shareCancel();
                ShareUtil.recycle();
            }

            @Override
            public void onWbShareFail() {
                ShareUtil.mShareListener.shareFailure(new Exception("分享失败"));
                ShareUtil.recycle();
            }
        });
    }

    @Override
    public boolean isInstall(Context context) {
        return WbSdk.isWbInstall(context);
    }

    @SuppressLint("CheckResult")
    private void shareTextOrImage(final ShareImageObject shareImageObject, final String title, final String targetUrl, final String summary,
                                  final Activity activity, final ShareListener listener) {

        mShareImage = Flowable.create(new FlowableOnSubscribe<Pair<String, byte[]>>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Pair<String, byte[]>> emitter) {
                try {
                    String path = ImageDecoder.decode(activity, shareImageObject);
                    emitter.onNext(Pair.create(path, ImageDecoder.compress2Byte(path, TARGET_SIZE, TARGET_LENGTH)));
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Pair<String, byte[]>>() {
                    @Override
                    public void accept(@NonNull Pair<String, byte[]> stringPair) {
                        handleShare(stringPair, title, targetUrl, summary);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) {
                        listener.shareFailure(new Exception(throwable.getMessage()));
                        recycle();
                        activity.finish();
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void shareTextOrImage(final Pair<String, byte[]> shareImageObject, final String title, final String targetUrl, final String summary,
                                  final Activity activity, final ShareListener listener) {

        mShareImage = Flowable.create(new FlowableOnSubscribe<Pair<String, byte[]>>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Pair<String, byte[]>> emitter) {
                try {
                    emitter.onNext(shareImageObject);
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Pair<String, byte[]>>() {
                    @Override
                    public void accept(@NonNull Pair<String, byte[]> stringPair) {
                        handleShare(stringPair, title, targetUrl, summary);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) {
                        listener.shareFailure(new Exception(throwable));
                        recycle();
                        activity.finish();
                    }
                });
    }

    private void handleShare(Pair<String, byte[]> stringPair, String title, String targetUrl, String summary) {

        WeiboMultiMessage message = new WeiboMultiMessage();
        if (!TextUtils.isEmpty(summary)) {
            message.textObject = getTextObj(title, targetUrl, summary);
        }

        if (stringPair != null) {
            message.imageObject = getImageObj(stringPair);
        }

        shareHandler.shareMessage(message, false);
        recycle();
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj(String title, String targetUrl, String summary) {
        TextObject textObject = new TextObject();
        textObject.text = summary;
        textObject.title = title;
        textObject.actionUrl = targetUrl;
        return textObject;
    }

    /**
     * 创建图片消息对象。
     * String、Bitmap、Resource type image change to pair to share
     *
     * @return 图片消息对象。
     */
    private ImageObject getImageObj(Pair<String, byte[]> object) {
        ImageObject imageObject = new ImageObject();
        imageObject.imagePath = object.first;
        imageObject.imageData = object.second;
        return imageObject;
    }

    @Override
    public void recycle() {

        if (mShareImage != null && !mShareImage.isDisposed()) {
            mShareImage.dispose();
            mShareImage = null;
        }
        shareHandler = null;
    }
}
