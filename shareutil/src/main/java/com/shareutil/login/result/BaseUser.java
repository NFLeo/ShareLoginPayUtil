package com.shareutil.login.result;

public class BaseUser {

    private String openId;

    private String nickname;

    /**
     * sex
     * 0. 未知
     * 1. 男
     * 2. 女
     */
    private int sex;

    private String headImageUrl;

    private String headImageUrlLarge;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getHeadImageUrl() {
        return headImageUrl;
    }

    public void setHeadImageUrl(String headImageUrl) {
        this.headImageUrl = headImageUrl;
    }

    public String getHeadImageUrlLarge() {
        return headImageUrlLarge;
    }

    public void setHeadImageUrlLarge(String headImageUrlLarge) {
        this.headImageUrlLarge = headImageUrlLarge;
    }
}
