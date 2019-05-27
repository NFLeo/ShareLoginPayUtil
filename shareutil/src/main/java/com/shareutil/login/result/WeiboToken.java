package com.shareutil.login.result;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class WeiboToken extends BaseToken {

    private String refreshToken;

    private String phoneNum;

    public WeiboToken(Oauth2AccessToken token) {
        if (token == null) {
            return;
        }
        setOpenid(token.getUid());
        setAccessToken(token.getToken());
        setRefreshToken(token.getRefreshToken());
        setPhoneNum(token.getPhoneNum());
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
