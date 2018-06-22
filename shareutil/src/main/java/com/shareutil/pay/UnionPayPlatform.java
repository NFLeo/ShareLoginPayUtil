package com.shareutil.pay;

import android.support.annotation.StringDef;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

 /**
  * Describe : 银联支付类型枚举
  * Created by Leo on 2018/5/8.
  */
public class UnionPayPlatform {

    @Documented
    @StringDef({RELEASE, DEV})
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    public @interface Platform { }

    // 正式
    public static final String RELEASE = "00";
    // 测试
    public static final String DEV = "01";
}
