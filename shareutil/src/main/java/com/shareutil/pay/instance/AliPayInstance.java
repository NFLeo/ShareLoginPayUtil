package com.shareutil.pay.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.shareutil.pay.AliPayParamsBean;
import com.shareutil.pay.AliPayResultBean;
import com.shareutil.pay.PayListener;

/**
 * Describe : 支付宝支付
 * Created by Leo on 2018/5/7 on 15:26.
 */
public class AliPayInstance implements PayInstance<AliPayParamsBean> {

    private static final int PAY_RESULT = 0x9527;
    private static PayListener payListener;

    @Override
    public void doPay(final Activity activity, final AliPayParamsBean payParams, PayListener payListener) {
        AliPayInstance.payListener = payListener;

        if (payListener == null) {  return;  }

        if (payParams == null || TextUtils.isEmpty(payParams.getOrderInfo())) {
            payListener.payFailed(new Exception("pay params can`t be null"));
            return;
        }

        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask payTask = new PayTask(activity);
                String payResult = payTask.pay(payParams.getOrderInfo(), true);
                Message msg = new Message();
                msg.what = PAY_RESULT;
                msg.obj = payResult;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    @Override
    public void handleResult(Intent data) { }

    @Override
    public void recycle() {  }

    @SuppressLint("HandlerLeak")
    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PAY_RESULT) {
                AliPayResultBean resultBean = new AliPayResultBean((String) msg.obj);
                String resultStatus = resultBean.getResultStatus();
                String resultMsg = resultBean.getMemo();
                switch (resultStatus) {
                    // 判断resultStatus 为“9000”则代表支付成功
                    case "9000":
                        payListener.paySuccess();
                        break;
                    // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认
                    // 最终交易是否成功以服务端异步通知为准（小概率状态）
                    case "8000":
                        break;
                    // 支付取消
                    case "6001":
                        payListener.payCancel();
                        break;
                    // 支付失败
                    default:
                        payListener.payFailed(new Exception(resultMsg));
                        break;
                }
            }
        }
    };
}
