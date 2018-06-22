package com.shareutil.pay.instance;

import android.app.Activity;
import android.content.Intent;

import com.shareutil.pay.IPayParamsBean;
import com.shareutil.pay.PayListener;

/**
 * Describe : 支付instance
 * Created by Leo on 2018/5/7 on 15:20.
 */
public interface PayInstance<T extends IPayParamsBean> {
    void doPay(Activity activity, T payParams, PayListener payListener);

    void handleResult(Intent data);

    void recycle();
}
