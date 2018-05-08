package me.shaohui.shareutil.pay.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.unionpay.UPPayAssistEx;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import me.shaohui.shareutil.pay.PayListener;
import me.shaohui.shareutil.pay.UnionPayParamsBean;

/**
 * Describe : 银联支付
 * Created by Leo on 2018/5/8 on 20:16.
 */
public class UnionPayInstance implements PayInstance<UnionPayParamsBean> {

    private UnionPayParamsBean payParams;
    private PayListener payCallback;
    private Activity activity;

    @Override
    public void doPay(Activity activity, UnionPayParamsBean payParams, PayListener payListener) {
        this.activity = activity;
        if (payListener == null) {
            activity.finish();
            return;
        }

        if (payParams == null) {
            payListener.payFailed(new Exception("pay params can`t be null"));
            return;
        }

        this.payParams = payParams;
        this.payCallback = payListener;
        UPPayAssistEx.startPay(activity, null, null, payParams.getTn(), payParams.getMode());
    }

    @Override
    public void handleResult(Intent data) {
        /*************************************************
         * 步骤3：处理银联手机支付控件返回的支付结果
         ************************************************/
        if (data == null) {
            activity.finish();
            return;
        }
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase("success")) {
            // 支付成功后，extra中如果存在result_data，取出校验
            // result_data结构见c）result_data参数说明
            if (data.hasExtra("result_data")) {
                String result = data.getExtras().getString("result_data");
                try {
                    JSONObject resultJson = new JSONObject(result);
                    String sign = resultJson.getString("sign");
                    String dataOrg = resultJson.getString("data");
                    // 验签证书同后台验签证书
                    // 此处的verify，商户需送去商户后台做验签
                    boolean ret = verify(dataOrg, sign, "mode");
                    if (ret) {
                        // 验证通过后，显示支付结果
                        if (payCallback != null) {
                            payCallback.paySuccess();
                        }
                    } else {
                        // 验证不通过后的处理
                        // 建议通过商户后台查询支付结果
                        if (payCallback != null) {
                            payCallback.payFailed(new Exception("verify failed"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // 未收到签名信息
                // 建议通过商户后台查询支付结果
                if (payCallback != null) {
                    payCallback.paySuccess();
                }
            }
        } else if (str.equalsIgnoreCase("fail")) {
            if (payCallback != null) {
                payCallback.payFailed(new Exception("pay failed"));
            }
        } else if (str.equalsIgnoreCase("cancel")) {
            if (payCallback != null) {
                payCallback.payCancel();
            }
        }
        releaseUinonPayContext();
        activity.finish();
    }

    private static boolean verify(String msg, String sign64, String mode) {
        // 此处的verify，商户需送去商户后台做验签
        return true;
    }

    private static void releaseUinonPayContext() {

        Field[] fields = UPPayAssistEx.class.getDeclaredFields();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                if (field.getType() == Context.class) {
                    try {
                        field.setAccessible(true);
                        field.set(null, null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void recycle() {
        releaseUinonPayContext();
    }
}
