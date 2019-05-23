package com.shareutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.shareutil.share.ShareImageObject;
import com.shareutil.share.ShareListener;
import com.shareutil.share.SharePlatform;
import com.shareutil.share.instance.DefaultShareInstance;
import com.shareutil.share.instance.QQShareInstance;
import com.shareutil.share.instance.ShareInstance;
import com.shareutil.share.instance.WeiboShareInstance;
import com.shareutil.share.instance.WxShareInstance;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.List;
import java.util.Locale;

public class ShareUtil {
    /**
     * 测试case
     * <p>
     * 1. 本地图片 vs 网络图片
     * 2. 图片大小限制
     * 3. 文字长度限制
     */

    public static final int TYPE = 798;

    public static ShareListener mShareListener;

    private static ShareInstance mShareInstance;

    private final static int TYPE_IMAGE = 1;
    private final static int TYPE_TEXT = 2;
    private final static int TYPE_MEDIA = 3;

    private static int mType;
    private static boolean mShareImmediate;
    private static int mPlatform;
    private static String mText;
    private static ShareImageObject mShareImageObject;
    private static String mTitle;
    private static String mSummary;
    private static String mTargetUrl;
    private static String mMiniId;                  // 小程序id
    private static String mMiniPath;                // 小程序path

    static void action(Activity activity) {
        mShareInstance = getShareInstance(mPlatform, activity);

        // 防止之后调用 NullPointException
        if (mShareListener == null) {
            activity.finish();
            return;
        }

        if (!mShareInstance.isInstall(activity)) {
            mShareListener.shareFailure(new Exception(ShareLogger.INFO.NOT_INSTALL));
            recycle();
            activity.finish();
            return;
        }

        switch (mType) {
            case TYPE_TEXT:
                mShareInstance.shareText(mPlatform, mText, activity, mShareListener);
                break;
            case TYPE_IMAGE:
                mShareInstance.shareImage(mPlatform, mShareImageObject, activity, mShareListener);
                break;
            case TYPE_MEDIA:
                mShareInstance.shareMedia(mPlatform, mTitle, mTargetUrl, mSummary, mMiniId, mMiniPath,
                        mShareImageObject, mShareImmediate, activity, mShareListener);
                break;
        }
    }

    public static void shareText(Context context, @SharePlatform.Platform int platform, String text,
                                 ShareListener listener) {
        initShareData(context, TYPE_TEXT, platform, listener, text, null, false, null, null, null, null, null);
    }

    public static void shareImage(Context context, @SharePlatform.Platform final int platform,
                                  final Object urlOrPath, ShareListener listener) {
        initShareData(context, TYPE_IMAGE, platform, listener, null, urlOrPath, false, null, null, null, null, null);
    }

    public static void shareMedia(Context context, @SharePlatform.Platform int platform, String title, String summary,
                                  String targetUrl, Object thumb, boolean shareImmediate, ShareListener listener) {
        initShareData(context, TYPE_MEDIA, platform, listener, null, thumb, shareImmediate, summary, targetUrl, title, null, null);
    }

    public static void shareMedia(Context context, @SharePlatform.Platform int platform, String title,
                                  String summary, String targetUrl, Object thumb, ShareListener listener) {
        initShareData(context, TYPE_MEDIA, platform, listener, null, thumb, false, summary, targetUrl, title, null, null);
    }

    public static void shareMedia(Context context, @SharePlatform.Platform int platform, String title, String summary,
                                  String targetUrl, Object thumbUrlOrPath, String miniId, String miniPath, ShareListener listener) {
        initShareData(context, TYPE_MEDIA, platform, listener, null, thumbUrlOrPath, false, summary, targetUrl, title, miniId, miniPath);
    }

    public static void shareMedia(Context context, @SharePlatform.Platform int platform, String title, String summary,
                                  String targetUrl, Object thumbUrlOrPath, boolean shareImmediate, String miniId, String miniPath, ShareListener listener) {
        initShareData(context, TYPE_MEDIA, platform, listener, null, thumbUrlOrPath, shareImmediate, summary, targetUrl, title, miniId, miniPath);
    }

    /**
     * deal with share data
     *
     * @param context     ctx
     * @param platform    share type
     * @param title       share title
     * @param summary     share content
     * @param targetUrl   targetUrl
     * @param imageObject logo   (image url path or bitmap)
     * @param miniId      miniprogram id
     * @param miniPath    miniprogram path
     * @param listener    result listener
     */
    private static void initShareData(Context context, int type, int platform, ShareListener listener,
                                      String text, Object imageObject, boolean immediate,
                                      String summary, String targetUrl, String title, String miniId, String miniPath) {
        switch (type) {
            case TYPE_TEXT:
                mText = text;
                break;
            case TYPE_IMAGE:
                mShareImageObject = new ShareImageObject(imageObject);
                mShareImageObject.setShareImmediate(immediate);
                break;
            case TYPE_MEDIA:
                mShareImageObject = new ShareImageObject(imageObject);
                mShareImageObject.setShareImmediate(immediate);
                mShareImmediate = immediate;
                mSummary = summary;
                mTargetUrl = targetUrl;
                mTitle = title;
                mMiniId = miniId;
                mMiniPath = miniPath;
                break;
        }

        mType = type;
        mPlatform = platform;
        mShareListener = listener;
        _ShareActivity.newInstance(context, TYPE);
    }

    public static void handleResult(int requestCode, int resultCode, Intent data) {
        // 微博分享会同时回调onActivityResult和onNewIntent， 而且前者返回的intent为null
        if (mShareInstance != null && data != null) {
            mShareInstance.handleResult(requestCode, resultCode, data);
        } else if (mShareInstance != null) {
            if (mPlatform == SharePlatform.QQ || mPlatform == SharePlatform.QZONE) {
                mShareInstance.handleResult(requestCode, resultCode, null);
            } else if (mPlatform != SharePlatform.WEIBO) {
                ShareLogger.e(ShareLogger.INFO.HANDLE_DATA_NULL);
            }
        } else {
            ShareLogger.e(ShareLogger.INFO.UNKNOWN_ERROR);
        }
    }

    private static ShareInstance getShareInstance(@SharePlatform.Platform int platform, Context context) {
        switch (platform) {
            case SharePlatform.WX:
            case SharePlatform.WX_TIMELINE:
                return new WxShareInstance(context, ShareManager.CONFIG.getWxId());
            case SharePlatform.QQ:
            case SharePlatform.QZONE:
                return new QQShareInstance(context, ShareManager.CONFIG.getQqId());
            case SharePlatform.WEIBO:
                return new WeiboShareInstance(context, ShareManager.CONFIG.getWeiboId(),
                        ShareManager.CONFIG.getWeiboRedirectUrl(), ShareManager.CONFIG.getWeiboScope());
            case SharePlatform.DEFAULT:
            default:
                return new DefaultShareInstance();
        }
    }

    public static void recycle() {
        mTitle = null;
        mSummary = null;
        mShareListener = null;
        mMiniPath = null;
        mMiniId = null;

        // bitmap recycle
        if (mShareImageObject != null
                && mShareImageObject.getBitmap() != null
                && !mShareImageObject.getBitmap().isRecycled()) {
            mShareImageObject.getBitmap().recycle();
        }
        mShareImageObject = null;

        if (mShareInstance != null) {
            mShareInstance.recycle();
        }
        mShareInstance = null;
        mShareListener = null;
    }

    @Deprecated
    public static boolean isQQInstalled(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }

        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        for (PackageInfo info : packageInfos) {
            if (TextUtils.equals(info.packageName.toLowerCase(Locale.getDefault()),
                    "com.tencent.mobileqq")) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static boolean isWeiBoInstalled(@NonNull Context context) {
        return mShareInstance.isInstall(context);
    }

    @Deprecated
    public static boolean isWeiXinInstalled(Context context) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, ShareManager.CONFIG.getWxId(), true);
        return api.isWXAppInstalled();
    }
}
