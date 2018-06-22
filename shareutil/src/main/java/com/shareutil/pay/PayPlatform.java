package com.shareutil.pay;

import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class PayPlatform {

    @Documented
    @IntDef({ALIPAY, WXPAY})
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    public @interface Platform { }

    public static final int ALIPAY = 1;

    public static final int WXPAY = 2;
}
