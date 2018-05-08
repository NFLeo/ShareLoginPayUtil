package me.shaohui.shareutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import me.shaohui.shareutil.pay.IPayParamsBean;
import me.shaohui.shareutil.pay.PayListener;
import me.shaohui.shareutil.pay.PayPlatform;
import me.shaohui.shareutil.pay.instance.AliPayInstance;
import me.shaohui.shareutil.pay.instance.PayInstance;
import me.shaohui.shareutil.pay.instance.UnionPayInstance;
import me.shaohui.shareutil.pay.instance.WXPayInstance;

import static me.shaohui.shareutil.ShareLogger.INFO;

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
            context.startActivity(_ShareActivity.newInstance(context, TYPE));
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
            case PayPlatform.UNIONPAY:
                mPayInstance = new UnionPayInstance();
                break;
            default:
                mPayListener.payFailed(new Exception(INFO.UNKNOW_PLATFORM));
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

        private PayListener mListener;

        PayListenerProxy(PayListener listener) {
            mListener = listener;
        }

        @Override
        public void paySuccess() {
            ShareLogger.i(INFO.PAY_SUCCESS);
            mListener.paySuccess();
            recycle();
        }

        @Override
        public void payFailed(Exception e) {
            ShareLogger.i(INFO.PAY_FAIL);
            mListener.payFailed(e);
            recycle();
        }

        @Override
        public void payCancel() {
            ShareLogger.i(INFO.PAY_CANCEL);
            mListener.payCancel();
            recycle();
        }
    }
}
