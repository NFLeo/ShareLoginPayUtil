package com.shareutil.pay;

/**
 * Describe : 支付结果回调
 * Created by Leo on 2018/5/7 on 15:21.
 */
public abstract class PayListener {
    public abstract void paySuccess();

    public abstract void payFailed(Exception e);

    public abstract void payCancel();
}
