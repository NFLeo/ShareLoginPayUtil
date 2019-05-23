package com.shareutil.login;

import android.support.annotation.IntDef;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class LoginPlatform {

    @Documented
    @IntDef({QQ, WX, WEIBO})
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    public @interface Platform {
    }

    public static final int QQ = 1;

    public static final int WX = 3;

    public static final int WEIBO = 5;
}
