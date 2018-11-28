package com.shareutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.shareutil.pay.IPayParamsBean;
import com.shareutil.pay.PayListener;
import com.shareutil.pay.PayPlatform;
import com.shareutil.pay.instance.AliPayInstance;
import com.shareutil.pay.instance.PayInstance;
import com.shareutil.pay.instance.WXPayInstance;

import java.lang.ref.WeakReference;

/**
 * Describe : 支付
 * Created by Leo on 2018/5/7.
 */
public class PayUtil {

    private static PayInstance mPayInstance;
    private static PayListener mPayListener;
    private static int mPlatform;
    private static IPayParamsBean mPayParamsBean;

    static final int TYPE = 800;

    public static void pay(Context context, @PayPlatform.Platform int platform, IPayParamsBean paramsBean, PayListener listener) {
        mPlatform = platform;
        mPayParamsBean = paramsBean;
        mPayListener = new PayListenerProxy(listener);
        if (platform == PayPlatform.ALIPAY) {
            mPayInstance = new AliPayInstance();
            mPayInstance.doPay((Activity) context, mPayParamsBean, mPayListener);
        } else {
            _ShareActivity.newInstance(context, TYPE);
        }
    }

    static void action(Activity activity) {
        switch (mPlatform) {
            case PayPlatform.ALIPAY:
                mPayInstance = new AliPayInstance();
                break;
            case PayPlatform.WXPAY:
                mPayInstance = new WXPayInstance(activity);
                break;
            default:
                mPayListener.payFailed(new Exception(ShareLogger.INFO.UNKNOW_PLATFORM));
                activity.finish();
        }
        mPayInstance.doPay(activity, mPayParamsBean, mPayListener);
    }

    static void handleResult(Intent data) {
        if (mPayInstance != null) {
            mPayInstance.handleResult(data);
        }
    }

    public static void recycle() {
        if (mPayInstance != null) {
            mPayInstance.recycle();
        }
        mPayInstance = null;
        mPayListener = null;
        mPlatform = 0;
    }

    private static class PayListenerProxy extends PayListener {

        private WeakReference<PayListener> mListener;

        PayListenerProxy(PayListener listener) {
            mListener = new WeakReference<>(listener);
        }

        @Override
        public void paySuccess() {
            ShareLogger.i(ShareLogger.INFO.PAY_SUCCESS);
            if (mListener.get() != null) {
                mListener.get().paySuccess();
            }
            recycle();
        }

        @Override
        public void payFailed(Exception e) {
            ShareLogger.i(ShareLogger.INFO.PAY_FAIL);
            if (mListener.get() != null) {
                mListener.get().payFailed(e);
            }
            recycle();
        }

        @Override
        public void payCancel() {
            ShareLogger.i(ShareLogger.INFO.PAY_CANCEL);
            if (mListener.get() != null) {
                mListener.get().payCancel();
            }
            recycle();
        }
    }
}
