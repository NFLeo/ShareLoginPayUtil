package me.shaohui.shareutil.share;

import android.graphics.Bitmap;
import android.util.Pair;

/**
 * Created by shaohui on 2016/11/19.
 * Update by Leo on 2018/4/29
 * 添加微信、微博图片分享
 */
public class ShareImageObject {

    private Bitmap mBitmap;
    private String mPathOrUrl;
    private boolean shareImmediate;
    private byte[] bytes;
    private Pair<String, byte[]> pair;

    public ShareImageObject(Object object) {
        if (object instanceof Bitmap) {
            mBitmap = (Bitmap) object;
        } else if (object instanceof String) {
            mPathOrUrl = (String) object;
        } else if (object instanceof byte[]) {
            bytes = (byte[]) object;
        } else if (object instanceof Pair) {
            pair = (Pair<String, byte[]>) object;
        }
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

    public ShareImageObject(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public ShareImageObject(String pathOrUrl) {
        mPathOrUrl = pathOrUrl;
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
