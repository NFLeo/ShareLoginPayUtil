package com.shareutil.pay.instance;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.shareutil.PayUtil;
import com.shareutil.ShareManager;
import com.shareutil.pay.PayListener;
import com.shareutil.pay.WXPayParamsBean;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Describe : 微信支付
 * Created by Leo on 2018/5/7 on 16:04.
 */
public class WXPayInstance implements PayInstance<WXPayParamsBean>, IWXAPIEventHandler {

    private IWXAPI mIWXAPI;
    private PayListener payCallback;

    public WXPayInstance(Activity activity) {
        mIWXAPI = WXAPIFactory.createWXAPI(activity, ShareManager.CONFIG.getWxId());
        mIWXAPI.handleIntent(activity.getIntent(), this);
    }

    @Override
    public void doPay(Activity activity, WXPayParamsBean payParams, PayListener payListener) {
        payCallback = payListener;
        if (!check()) {
            if (payCallback != null) {
                payCallback.payFailed(new Exception("please install client first"));
                activity.finish();
            }
            return;
        }
        if (payParams == null || TextUtils.isEmpty(payParams.getAppid()) || TextUtils.isEmpty(payParams.getPartnerid())
                || TextUtils.isEmpty(payParams.getPrepayId()) || TextUtils.isEmpty(payParams.getPackageValue()) ||
                TextUtils.isEmpty(payParams.getNonceStr()) || TextUtils.isEmpty(payParams.getTimestamp()) ||
                TextUtils.isEmpty(payParams.getSign())) {
            if (payCallback != null) {
                payCallback.payFailed(new Exception("pay params can`t be null"));
                activity.finish();
            }
            return;
        }

        PayReq req = new PayReq();
        req.appId = payParams.getAppid();
        req.partnerId = payParams.getPartnerid();
        req.prepayId = payParams.getPrepayId();
        req.packageValue = payParams.getPackageValue();
        req.nonceStr = payParams.getNonceStr();
        req.timeStamp = payParams.getTimestamp();
        req.sign = payParams.getSign();
        mIWXAPI.sendReq(req);
    }

    @Override
    public void handleResult(Intent data) {
        if (mIWXAPI == null) {
            if (payCallback != null) {
                payCallback.payFailed(new Exception("pay error"));
            }
            return;
        }

        mIWXAPI.handleIntent(data, this);
    }

    @Override
    public void recycle() {
        if (mIWXAPI != null) {
            mIWXAPI.detach();
            mIWXAPI = null;
        }
    }

    //检测是否支持微信支付
    private boolean check() {
        return mIWXAPI.isWXAppInstalled() && mIWXAPI.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (payCallback == null) {
            return;
        }

        if (baseResp != null && baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            switch (baseResp.errCode) {
                case 0:
                    payCallback.paySuccess();
                    PayUtil.recycle();
                    break;
                case -1:
                    String errorStr = TextUtils.isEmpty(baseResp.errStr) ? "pay failed" : baseResp.errStr;
                    payCallback.payFailed(new Exception(errorStr));
                    PayUtil.recycle();
                    break;
                case -2:
                    payCallback.payCancel();
                    PayUtil.recycle();
                    break;
            }
        }
    }
}
