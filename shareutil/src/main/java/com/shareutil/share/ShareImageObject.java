package com.shareutil.share;

import android.graphics.Bitmap;
import android.util.Pair;

/**
 * Describe : 微博支持资源图片分享
 * Created by Leo on 2018/6/22.
 */
public class ShareImageObject {

    public static final int IMAGE_TYPE_BITMAP = 0X186221;
    public static final int IMAGE_TYPE_PATH = 0X186222;
    public static final int IMAGE_TYPE_RES = 0X186223;
    public static final int IMAGE_TYPE_BYTE = 0X186224;
    public static final int IMAGE_TYPE_PAIR = 0X186225;

    private Object mObject;
    private Bitmap mBitmap;
    private String mPathOrUrl;
    private boolean shareImmediate;
    private int mImageRes;
    private byte[] bytes;
    private Pair<String, byte[]> pair;

    public ShareImageObject(Object object) {
        mObject = object;
        if (object instanceof Bitmap) {
            mBitmap = (Bitmap) object;
        } else if (object instanceof String) {
            mPathOrUrl = (String) object;
        } else if (object instanceof byte[]) {
            bytes = (byte[]) object;
        } else if (object instanceof Pair) {
            pair = (Pair<String, byte[]>) object;
        } else if (object instanceof Integer) {
            mImageRes = (int) object;
        }
    }

    public int returnImageType() {
        if (mObject instanceof Bitmap) {
            return IMAGE_TYPE_BITMAP;
        } else if (mObject instanceof String) {
            return IMAGE_TYPE_PATH;
        } else if (mObject instanceof byte[]) {
            return IMAGE_TYPE_BYTE;
        } else if (mObject instanceof Pair) {
            return IMAGE_TYPE_PAIR;
        } else if (mObject instanceof Integer) {
            return IMAGE_TYPE_RES;
        }

        return -1;
    }

    public int getImageRes() {
        return mImageRes;
    }

    public void setImageRes(int mImageRes) {
        this.mImageRes = mImageRes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Pair<String, byte[]> getPair() {
        return pair;
    }

    public void setPair(Pair<String, byte[]> pair) {
        this.pair = pair;
    }

    public boolean isShareImmediate() {
        return shareImmediate;
    }

    public void setShareImmediate(boolean shareImmediate) {
        this.shareImmediate = shareImmediate;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public String getPathOrUrl() {
        return mPathOrUrl;
    }

    public void setPathOrUrl(String pathOrUrl) {
        mPathOrUrl = pathOrUrl;
    }
}
