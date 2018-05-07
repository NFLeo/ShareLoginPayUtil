package me.shaohui.shareutil.pay;

/**
 * Describe : 支付宝支付参数
 * Created by Leo on 2018/5/7 on 15:28.
 */
public class AliPayParamsBean implements IPayParamsBean {
    private String orderInfo;

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
}
