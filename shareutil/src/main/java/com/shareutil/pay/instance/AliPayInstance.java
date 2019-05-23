package com.shareutil.pay.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.shareutil.PayUtil;
import com.shareutil.ShareLogger;
import com.shareutil.pay.AliPayParamsBean;
import com.shareutil.pay.AliPayResultBean;
import com.shareutil.pay.PayListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Describe : 支付宝支付
 * Created by Leo on 2018/5/7 on 15:26.
 */
public class AliPayInstance implements PayInstance<AliPayParamsBean> {

    private Disposable subscribe;

    @SuppressLint("CheckResult")
    @Override
    public void doPay(final Activity activity, final AliPayParamsBean payParams, final PayListener payListener) {

        if (payListener == null) {  return;  }

        if (payParams == null || TextUtils.isEmpty(payParams.getOrderInfo())) {
            payListener.payFailed(new Exception("pay params can`t be null"));
            recycle();
            return;
        }

        subscribe = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                PayTask payTask = new PayTask(activity);
                String payResult = payTask.pay(payParams.getOrderInfo(), true);
                emitter.onNext(payResult);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) {
                if (TextUtils.isEmpty(o)) {
                    payListener.payFailed(new Exception(ShareLogger.INFO.PAY_FAILED));
                    PayUtil.recycle();
                    return;
                }

                AliPayResultBean resultBean = new AliPayResultBean(o);
                String resultStatus = resultBean.getResultStatus();
                String resultMsg = resultBean.getMemo();
                switch (resultStatus) {
                    // 判断resultStatus 为“9000”则代表支付成功
                    case "9000":
                        payListener.paySuccess();
                        PayUtil.recycle();
                        break;
                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认
                    // 最终交易是否成功以服务端异步通知为准（小概率状态）
                    case "8000":
                        break;
                    // 支付取消
                    case "6001":
                        payListener.payCancel();
                        PayUtil.recycle();
                        break;
                    // 支付失败
                    default:
                        payListener.payFailed(new Exception(resultMsg));
                        PayUtil.recycle();
                        break;
                }
            }
        });
    }

    @Override
    public void handleResult(Intent data) { }

    @Override
    public void recycle() {
        if (subscribe != null && !subscribe.isDisposed()) {
            subscribe.dispose();
            subscribe = null;
        }
//        if (mHandler != null) {
//            mHandler.removeCallbacksAndMessages(null);
//        }
    }

//    @SuppressLint("HandlerLeak")
//    private static Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what == PAY_RESULT) {
//                AliPayResultBean resultBean = new AliPayResultBean((String) msg.obj);
//                String resultStatus = resultBean.getResultStatus();
//                String resultMsg = resultBean.getMemo();
//                switch (resultStatus) {
//                    // 判断resultStatus 为“9000”则代表支付成功
//                    case "9000":
//                        payListener.paySuccess();
//                        PayUtil.recycle();
//                        break;
//                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认
//                    // 最终交易是否成功以服务端异步通知为准（小概率状态）
//                    case "8000":
//                        break;
//                    // 支付取消
//                    case "6001":
//                        payListener.payCancel();
//                        PayUtil.recycle();
//                        break;
//                    // 支付失败
//                    default:
//                        payListener.payFailed(new Exception(resultMsg));
//                        PayUtil.recycle();
//                        break;
//                }
//            }
//        }
//    };
//
//    private static class PayRunnable implements Runnable {
//        private WeakReference<Activity> activity;
//        private AliPayParamsBean payParams;
//
//        PayRunnable(Activity activity, AliPayParamsBean payParams) {
//            this.activity = new WeakReference<>(activity);
//            this.payParams = payParams;
//        }
//
//        @Override
//        public void run() {
//            if (activity.get() != null) {
//                PayTask payTask = new PayTask(activity.get());
//                String payResult = payTask.pay(payParams.getOrderInfo(), true);
//                Message msg = new Message();
//                msg.what = PAY_RESULT;
//                msg.obj = payResult;
//                mHandler.sendMessage(msg);
//            }
//        }
//    }
}
