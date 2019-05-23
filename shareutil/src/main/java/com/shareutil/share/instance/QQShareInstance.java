package com.shareutil.share.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.shareutil.ShareLogger;
import com.shareutil.ShareUtil;
import com.shareutil.share.ImageDecoder;
import com.shareutil.share.ShareImageObject;
import com.shareutil.share.ShareListener;
import com.shareutil.share.SharePlatform;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.Tencent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class QQShareInstance implements ShareInstance {

    private Tencent mTencent;
    private Disposable mShareFunc;
    private Disposable mShareImage;

    public QQShareInstance(Context context, String app_id) {
        mTencent = Tencent.createInstance(app_id, context.getApplicationContext());
    }

    @Override
    public void shareText(int platform, String text, Activity activity, ShareListener listener) {
        if (platform == SharePlatform.QZONE) {
            shareToQZoneForText(text, activity, listener);
        } else {
            listener.shareFailure(new Exception(ShareLogger.INFO.QQ_NOT_SUPPORT_SHARE_TXT));
            recycle();
            activity.finish();
        }
    }

    @Override
    public void shareMedia(final int platform, final String title, final String targetUrl, final String summary,
                           String miniId, String miniPath, final ShareImageObject shareImageObject,
                           final Activity activity, final ShareListener listener) {
        shareFunc(platform, title, targetUrl, summary, shareImageObject, false, activity, listener);
    }

    @Override
    public void shareMedia(final int platform, final String title, final String targetUrl, final String summary,
                           String miniId, String miniPath, final ShareImageObject shareImageObject, boolean shareImmediate,
                           final Activity activity, final ShareListener listener) {
        // 直接分享，外部处理好分享图片
        shareFunc(platform, title, targetUrl, summary, shareImageObject, shareImmediate, activity, listener);
    }

    @SuppressLint("CheckResult")
    private void shareFunc(final int platform, final String title, final String targetUrl, final String summary,
                           final ShareImageObject shareImageObject, final boolean immediate, final Activity activity, final ShareListener listener) {
        mShareFunc = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<String> emitter) {
                try {
                    if (immediate) {
                        emitter.onNext(shareImageObject.getPathOrUrl());
                    } else {
                        emitter.onNext(ImageDecoder.decode(activity, shareImageObject));
                    }
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) {
                        if (platform == SharePlatform.QZONE) {
                            shareToQZoneForMedia(title, targetUrl, summary, s, activity, listener);
                        } else {
                            shareToQQForMedia(title, summary, targetUrl, s, activity, listener);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        listener.shareFailure(new Exception(throwable));
                        recycle();
                        activity.finish();
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void shareImage(final int platform, final ShareImageObject shareImageObject,
                           final Activity activity, final ShareListener listener) {
        mShareImage = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<String> emitter) throws Exception {
                try {
                    emitter.onNext(ImageDecoder.decode(activity, shareImageObject));
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String localPath) throws Exception {
                        if (platform == SharePlatform.QZONE) {
                            shareToQzoneForImage(localPath, activity, listener);
                        } else {
                            shareToQQForImage(localPath, activity, listener);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        listener.shareFailure(new Exception(throwable));
                        recycle();
                        activity.finish();
                    }
                });
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, ShareUtil.mShareListener);
    }

    @Override
    public boolean isInstall(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }

        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        for (PackageInfo info : packageInfos) {
            if (TextUtils.equals(info.packageName.toLowerCase(), "com.tencent.mobileqq")) {
                return true;
            }
        }
        return false;
    }

    private void shareToQQForMedia(String title, String summary, String targetUrl, String thumbUrl,
                                   Activity activity, ShareListener listener) {
        if (mTencent == null) {
            listener.shareFailure(new Exception("分享失败"));
            recycle();
            activity.finish();
            return;
        }

        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, thumbUrl);
        mTencent.shareToQQ(activity, params, listener);
        recycle();
    }

    private void shareToQQForImage(String localUrl, Activity activity, ShareListener listener) {
        if (mTencent == null) {
            listener.shareFailure(new Exception("分享失败"));
            recycle();
            activity.finish();
            return;
        }

        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, localUrl);
        mTencent.shareToQQ(activity, params, listener);
        recycle();
    }

    private void shareToQZoneForText(String text, Activity activity, ShareListener listener) {
        if (mTencent == null) {
            listener.shareFailure(new Exception("分享失败"));
            recycle();
            activity.finish();
            return;
        }

        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, text);
        mTencent.publishToQzone(activity, params, listener);
        recycle();
    }

    private void shareToQZoneForMedia(String title, String targetUrl, String summary,
                                      String imageUrl, Activity activity, ShareListener listener) {
        if (mTencent == null) {
            listener.shareFailure(new Exception("分享失败"));
            recycle();
            activity.finish();
            return;
        }

        final Bundle params = new Bundle();
        final ArrayList<String> image = new ArrayList<>();
        image.add(imageUrl);
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, image);
        mTencent.shareToQzone(activity, params, listener);
        recycle();
    }

    private void shareToQzoneForImage(String imagePath, Activity activity, ShareListener listener) {
        if (mTencent == null) {
            listener.shareFailure(new Exception("分享失败"));
            recycle();
            activity.finish();
            return;
        }

        final Bundle params = new Bundle();
        final ArrayList<String> image = new ArrayList<>();
        image.add(imagePath);
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, image);
        mTencent.publishToQzone(activity, params, listener);

        recycle();
    }

    @Override
    public void recycle() {
        if (mShareFunc != null && !mShareFunc.isDisposed()) {
            mShareFunc.dispose();
            mShareFunc = null;
        }
        if (mShareImage != null && !mShareImage.isDisposed()) {
            mShareImage.dispose();
            mShareImage = null;
        }
        if (mTencent != null) {
            mTencent.releaseResource();
            mTencent = null;
        }
    }
}
