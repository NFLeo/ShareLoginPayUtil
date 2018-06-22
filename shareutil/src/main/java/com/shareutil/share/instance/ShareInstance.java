package com.shareutil.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.shareutil.share.ShareImageObject;
import com.shareutil.share.ShareListener;

public interface ShareInstance {

    void shareText(int platform, String text, Activity activity, ShareListener listener);

    void shareMedia(int platform, String title, String targetUrl, String summary, final String miniId, final String miniPath,
                    ShareImageObject shareImageObject, Activity activity, ShareListener listener);

    /**
     * 分享图文
     * @param platform           分享类型
     * @param title              分享标题
     * @param targetUrl          分享后跳转链接
     * @param summary            分享说明
     * @param miniId             小程序id
     * @param miniPath           小程序path
     * @param shareImageObject   分享图标对象
     * @param shareImmediate     是否直接分享（不对图片二次处理）
     * @param activity           activity
     * @param listener           分享结果
     */
    void shareMedia(int platform, String title, String targetUrl, String summary, final String miniId, final String miniPath,
                    ShareImageObject shareImageObject, boolean shareImmediate, Activity activity, ShareListener listener);

    void shareImage(int platform, ShareImageObject shareImageObject, Activity activity,
                    ShareListener listener);

    void handleResult(Intent data);

    boolean isInstall(Context context);

    void recycle();
}
