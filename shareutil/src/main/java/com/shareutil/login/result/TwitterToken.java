package com.shareutil.login.result;

import android.text.TextUtils;

import com.twitter.sdk.android.core.TwitterSession;

public class TwitterToken extends BaseToken {

    private long userId;
    private String userName;

    private String secret;

    public TwitterToken(TwitterSession session) {

        if (session == null) {
            return;
        }

        setUserId(session.getUserId());
        setUserName(session.getUserName());
        setOpenid(String.valueOf(session.getId()));

        if (session.getAuthToken() == null) {
            return;
        }

        setAccessToken(session.getAuthToken().token);
        setSecret(session.getAuthToken().secret);
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return TextUtils.isEmpty(userName) ? "" : userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSecret() {
        return TextUtils.isEmpty(secret) ? "" : secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
