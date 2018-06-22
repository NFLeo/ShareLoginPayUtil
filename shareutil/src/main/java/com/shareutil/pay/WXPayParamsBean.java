/*
******************************* Copyright (c)*********************************\
**
**                 (c) Copyright 2017, King, china
**                          All Rights Reserved
**                                
**                              By(King)
**                         
**------------------------------------------------------------------------------
*/
package com.shareutil.pay;

 /**
  * Describe : 微信支付参数
  * Created by Leo on 2018/5/7.
  */
public class WXPayParamsBean implements IPayParamsBean {
    /**
     * sign : ECE311C3DF76E009E6F37F05C350625F
     * timestamp : 1474886901
     * partnerid : 1391669502
     * package : Sign=WXPay
     * appid : wx46a24ab145becbde
     * nonceStr : 0531a4a42fa846fe8a7563847cd24c2a
     * prepayId : wx20160926184820acbd9357100240402425
     */
    private String sign;
    private String timestamp;
    private String partnerid;
    private String packageValue;
    private String appid;
    private String nonceStr;
    private String prepayId;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public String getPackageValue() {
        return packageValue;
    }

    public void setPackageValue(String packageValue) {
        this.packageValue = packageValue;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }
}
