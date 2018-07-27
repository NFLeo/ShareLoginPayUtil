package com.shareutil.pay.instance;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

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
    private WXPayParamsBean payParams;
    private PayListener payCallback;

    public WXPayInstance(Activity activity) {
        mIWXAPI = WXAPIFactory.createWXAPI(activity, ShareManager.CONFIG.getWxId());
        mIWXAPI.handleIntent(activity.getIntent(), this);
    }

    @Override
    public void doPay(Activity activity, WXPayParamsBean payParams, PayListener payListener) {
        this.payParams = payParams;
        payCallback = payListener;
        if (!check()) {
            if (payCallback != null) {
                payCallback.payFailed(new Exception("please install client first"));
                activity.finish();
            }
            return;
        }
        if (this.payParams == null || TextUtils.isEmpty(this.payParams.getAppid()) || TextUtils.isEmpty(this.payParams.getPartnerid())
                || TextUtils.isEmpty(this.payParams.getPrepayId()) || TextUtils.isEmpty(this.payParams.getPackageValue()) ||
                TextUtils.isEmpty(this.payParams.getNonceStr()) || TextUtils.isEmpty(this.payParams.getTimestamp()) ||
                TextUtils.isEmpty(this.payParams.getSign())) {
            if (payCallback != null) {
                payCallback.payFailed(new Exception("pay params can`t be null"));
                activity.finish();
            }
            return;
        }

        PayReq req = new PayReq();
        req.appId = this.payParams.getAppid();
        req.partnerId = this.payParams.getPartnerid();
        req.prepayId = this.payParams.getPrepayId();
        req.packageValue = this.payParams.getPackageValue();
        req.nonceStr = this.payParams.getNonceStr();
        req.timeStamp = this.payParams.getTimestamp();
        req.sign = this.payParams.getSign();
        mIWXAPI.sendReq(req);
    }

    @Override
    public void handleResult(Intent data) {
        mIWXAPI.handleIntent(data, this);
    }

    @Override
    public void recycle() {
        if (mIWXAPI != null) {
            mIWXAPI.detach();
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
                    break;
                case -1:
                    String errorStr = TextUtils.isEmpty(baseResp.errStr) ? "pay failed" : baseResp.errStr;
                    payCallback.payFailed(new Exception(errorStr));
                    break;
                case -2:
                    payCallback.payCancel();
                    break;
            }
        }
    }
}
