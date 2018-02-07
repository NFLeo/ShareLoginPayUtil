package me.shaohui.shareutil.share;

import android.graphics.Bitmap;

/**
 * Created by shaohui on 2016/11/19.
 */

public class ShareImageObject {

    private Bitmap mBitmap;

    private String mPathOrUrl;

    public ShareImageObject(Object object) {
        if (object instanceof Bitmap) {
            mBitmap = (Bitmap) object;
        } else if (object instanceof String) {
            mPathOrUrl = (String) object;
        }
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
