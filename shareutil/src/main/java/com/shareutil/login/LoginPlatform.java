package com.shareutil.login;

import android.support.annotation.IntDef;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class LoginPlatform {

    @Documented
    @IntDef({QQ, WX, WEIBO, INS, GOOGLE, FACEBOOK, TWITTER})
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    public @interface Platform {
    }

    public static final int QQ = 0X0071;

    public static final int WX = 0X0073;

    public static final int WEIBO = 0X0075;

    public static final int INS = 0X0077;

    public static final int GOOGLE = 0X0079;

    public static final int FACEBOOK = 0X0081;

    public static final int TWITTER = 0X0083;
}
