package com.shareutil.login.result;

import com.facebook.AccessToken;

import java.util.Date;

public class FacebookToken extends BaseToken {
    private String userId;
    private String applicationId;
    private Date lastRefresh;
    private Date dataAccessExpirationTime;
    private AccessToken mAccessToken;

    public FacebookToken(AccessToken token) {
        setAccessTokenBean(token);
        setAccessToken(token.getToken());
        setOpenid(token.getUserId());
        setUserId(token.getUserId());
        setApplicationId(token.getApplicationId());
        setLastRefresh(token.getLastRefresh());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }


    public Date getLastRefresh() {
        return lastRefresh;
    }

    public void setLastRefresh(Date lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    public Date getDataAccessExpirationTime() {
        return dataAccessExpirationTime;
    }

    public void setDataAccessExpirationTime(Date dataAccessExpirationTime) {
        this.dataAccessExpirationTime = dataAccessExpirationTime;
    }

    public AccessToken getAccessTokenBean() {
        return mAccessToken;
    }

    public void setAccessTokenBean(AccessToken accessToken) {
        mAccessToken = accessToken;
    }

    @Override
    public String toString() {
        return "FaceBookToken{" +
                "userId='" + userId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", lastRefresh=" + lastRefresh +
                ", dataAccessExpirationTime=" + dataAccessExpirationTime +
                '}';
    }
}
