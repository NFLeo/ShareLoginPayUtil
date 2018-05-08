package com;

import android.view.View;
import android.widget.Toast;

import me.shaohui.bottomdialog.BaseBottomDialog;
import me.shaohui.shareutil.PayUtil;
import me.shaohui.shareutil.pay.AliPayParamsBean;
import me.shaohui.shareutil.pay.PayListener;
import me.shaohui.shareutil.pay.PayPlatform;
import me.shaohui.shareutil.pay.UnionPayParamsBean;
import me.shaohui.shareutil.pay.UnionPayPlatform;
import me.shaohui.shareutil.pay.WXPayParamsBean;

public class PayBottomDialog extends BaseBottomDialog implements View.OnClickListener {

    private PayListener mPayListener;

    @Override
    public int getLayoutRes() {
        return R.layout.layout_bottom_pay;
    }

    @Override
    public void bindView(final View v) {
        v.findViewById(R.id.share_alipay).setOnClickListener(this);
        v.findViewById(R.id.share_union).setOnClickListener(this);
        v.findViewById(R.id.share_wx).setOnClickListener(this);

        mPayListener = new PayListener() {
            @Override
            public void paySuccess() {
                Toast.makeText(v.getContext(), "支付成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void payFailed(Exception e) {
                Toast.makeText(v.getContext(), "支付失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void payCancel() {
                Toast.makeText(v.getContext(), "支付取消", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_alipay:
                AliPayParamsBean payParamsBean = new AliPayParamsBean();
                payParamsBean.setOrderInfo("xxxx");
                PayUtil.pay(getContext(), PayPlatform.ALIPAY, payParamsBean, mPayListener);
                break;
            case R.id.share_wx:
                WXPayParamsBean wxPayParamsBean = new WXPayParamsBean();
                wxPayParamsBean.setAppid("xxxx");
                wxPayParamsBean.setNonceStr("xxxx");
                wxPayParamsBean.setPartnerid("xxxx");
                wxPayParamsBean.setPackageValue("xxxx");
                wxPayParamsBean.setPrepayId("xxxx");
                wxPayParamsBean.setSign("xxxx");
                wxPayParamsBean.setTimestamp("xxxx");

                PayUtil.pay(getContext(), PayPlatform.WXPAY, wxPayParamsBean, mPayListener);
                break;
            case R.id.share_union:
                UnionPayParamsBean unionPayParamsBean = new UnionPayParamsBean();
                unionPayParamsBean.setTn("xxxxxx");
                unionPayParamsBean.setMode(UnionPayPlatform.RELEASE);
                PayUtil.pay(getContext(), PayPlatform.UNIONPAY, unionPayParamsBean, mPayListener);
                break;
        }
        dismiss();
    }
}
