package com.shareutil.share.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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

    @Override
    public void shareText(int platform, String text, Activity activity, ShareListener listener) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, activity.getResources().getString(R.string.vista_share_title)));
    }

    @Override
    public void shareMedia(int platform, String title, String targetUrl, String summary, String miniId, String miniPath, ShareImageObject shareImageObject, Activity activity, ShareListener listener) {
        handleShare(activity, title, targetUrl, summary);
    }

    @Override
    public void shareMedia(int platform, String title, String targetUrl, String summary, String miniId, String miniPath, ShareImageObject shareImageObject, boolean shareImmediate, Activity activity, ShareListener listener) {
        handleShare(activity, title, targetUrl, summary);
    }

    private void handleShare(Activity activity, String title, String targetUrl, String summary) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, summary);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", title, targetUrl));
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, activity.getResources().getString(R.string.vista_share_title)));
    }

    @SuppressLint("CheckResult")
    @Override
    public void shareImage(int platform, final ShareImageObject shareImageObject,
                           final Activity activity, final ShareListener listener) {
        Flowable.create(new FlowableOnSubscribe<Uri>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Uri> emitter) throws Exception {
                try {
                    Uri uri = Uri.fromFile(new File(ImageDecoder.decode(activity, shareImageObject)));
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
                    public void accept(long t) throws Exception {
                        listener.shareRequest();
                    }
                })
                .subscribe(new Consumer<Uri>() {
                    @Override
                    public void accept(@NonNull Uri uri) throws Exception {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.setType("image/jpeg");
                        activity.startActivity(Intent.createChooser(shareIntent, activity.getResources().getText(R.string.vista_share_title)));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        listener.shareFailure(new Exception(throwable));
                    }
                });
    }

    public void shareMsg(String activityTitle, String msgTitle, String msgText,
                         String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(Intent.createChooser(intent, activityTitle));
    }

    @Override
    public void handleResult(Intent data) {
        // Default share, do nothing
    }

    @Override
    public boolean isInstall(Context context) {
        return true;
    }

    @Override
    public void recycle() {
    }
}
