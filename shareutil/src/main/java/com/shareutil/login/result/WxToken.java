package com.shareutil.login.result;

import org.json.JSONException;
import org.json.JSONObject;

public class WxToken extends BaseToken {

    private String refresh_token;

    public WxToken(JSONObject jsonObject) throws JSONException {

        if (jsonObject == null) {
            return;
        }

        if (jsonObject.has("openid")) {
            setOpenid(jsonObject.getString("openid"));
        }
        if (jsonObject.has("access_token")) {
            setAccessToken(jsonObject.getString("access_token"));
        }
        if (jsonObject.has("refresh_token")) {
            setRefreshToken(jsonObject.getString("refresh_token"));
        }
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
