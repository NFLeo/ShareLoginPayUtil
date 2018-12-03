package com.shareutil.share.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.shareutil.R;
import com.shareutil.share.ImageDecoder;
import com.shareutil.share.ShareImageObject;
import com.shareutil.share.ShareListener;

import java.io.File;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.LongConsumer;
import io.reactivex.schedulers.Schedulers;

public class DefaultShareInstance implements ShareInstance {

    private static final int SHARE_TYPE_TEXT = 0x11111;
    private static final int SHARE_TYPE_IMAGE = 0x11112;

    @Override
    public void shareText(int platform, String text, Activity activity, ShareListener listener) {
        handleShare(activity, SHARE_TYPE_TEXT, "", "", text, null);

    }

    @Override
    public void shareMedia(int platform, final String title, final String targetUrl, final String summary, String miniId, String miniPath, ShareImageObject shareImageObject, final Activity activity, final ShareListener listener) {
        createImageShare(title, targetUrl, summary, shareImageObject, activity, listener);
    }

    @Override
    public void shareMedia(int platform, String title, String targetUrl, String summary, String miniId, String miniPath, ShareImageObject shareImageObject, boolean shareImmediate, Activity activity, ShareListener listener) {
        createImageShare(title, targetUrl, summary, shareImageObject, activity, listener);
    }

    private void handleShare(Activity activity, int type, String title, String targetUrl, String summary, Uri imageUri) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", summary, targetUrl));

        if (type == SHARE_TYPE_IMAGE && imageUri != null) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            sendIntent.setType("image/*");
        } else {
            sendIntent.setType("text/plain");
        }

        activity.startActivity(Intent.createChooser(sendIntent, activity.getResources().getString(R.string.vista_share_title)));
    }

    @SuppressLint("CheckResult")
    @Override
    public void shareImage(int platform, final ShareImageObject shareImageObject,
                           final Activity activity, final ShareListener listener) {
        createImageShare("", "", "", shareImageObject, activity, listener);
    }

    @SuppressLint("CheckResult")
    private void createImageShare(final String title, final String targetUrl, final String summary, final ShareImageObject shareImageObject,
                                  final Activity activity, final ShareListener listener) {
        Flowable.create(new FlowableOnSubscribe<Uri>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Uri> emitter) {
                try {
                    Uri uri = FileProvider.getUriForFile(activity, getAppPackageName(activity) + ".file.provider",
                            new File(ImageDecoder.decode(activity, shareImageObject)));
                    emitter.onNext(uri);
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest(new LongConsumer() {
                    @Override
                    public void accept(long t) {
                        listener.shareRequest();
                    }
                })
                .subscribe(new Consumer<Uri>() {
                    @Override
                    public void accept(@NonNull Uri uri) {
                        handleShare(activity, SHARE_TYPE_IMAGE, title, targetUrl, summary, uri);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) {
                        listener.shareFailure(new Exception(throwable));
                        activity.finish();
                    }
                });
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        // Default share, do nothing
    }

    @Override
    public boolean isInstall(Context context) {
        return true;
    }

    @Override
    public void recycle() {
    }

    public String getAppPackageName(Context context) {
        String packagename = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
            packagename = info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packagename;
    }
}
