package com.shareutil.login.result;

import org.json.JSONException;
import org.json.JSONObject;

public class QQToken extends BaseToken {

    public QQToken(JSONObject jsonObject) throws JSONException {

        if (jsonObject == null) {
            return;
        }

        if (jsonObject.has("access_token")) {
            setAccessToken(jsonObject.getString("access_token"));
        }

        if (jsonObject.has("openid")) {
            setOpenid(jsonObject.getString("openid"));
        }
    }
}
