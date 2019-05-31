package com.shareutil;

public class ShareConfig {

    private String wxId;

    private String wxSecret;

    private String qqId;

    private String weiboId;

    private String weiboRedirectUrl = "https://api.weibo.com/oauth2/default.html";

    private String weiboScope = "email";

    private String insClientId;

    private String insScope;

    private String insRedirectURIs;

    private String googleClientId;

    private String googleClientSecret;

    private String fbClientId;

    private String fbClientScheme;

    private String twitterConsumerKey;

    private String twitterConsumerSecret;

    private boolean debug;

    public static ShareConfig instance() {
        return new ShareConfig();
    }

    public ShareConfig wxId(String id) {
        wxId = id;
        return this;
    }

    public ShareConfig wxSecret(String id) {
        wxSecret = id;
        return this;
    }

    public ShareConfig qqId(String id) {
        qqId = id;
        return this;
    }

    public ShareConfig weiboId(String id) {
        weiboId = id;
        return this;
    }

    public ShareConfig weiboRedirectUrl(String url) {
        weiboRedirectUrl = url;
        return this;
    }

    public ShareConfig weiboScope(String scope) {
        weiboScope = scope;
        return this;
    }

    public ShareConfig insRedirectURIs(String redirectURIs) {
        this.insRedirectURIs = redirectURIs;
        return this;
    }

    public ShareConfig insClientId(String insClientId) {
        this.insClientId = insClientId;
        return this;
    }

    public ShareConfig insScope(String insScope) {
        this.insScope = insScope;
        return this;
    }

    public ShareConfig googleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
        return this;
    }

    public ShareConfig googleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
        return this;
    }

    public ShareConfig fbClientId(String fbClientId) {
        this.fbClientId = fbClientId;
        return this;
    }

    public ShareConfig fbClientScheme(String fbClientScheme) {
        this.fbClientScheme = fbClientScheme;
        return this;
    }

    public ShareConfig twitterConsumerKey(String twitterConsumerKey) {
        this.twitterConsumerKey = twitterConsumerKey;
        return this;
    }

    public ShareConfig twitterConsumerSecret(String twitterConsumerSecret) {
        this.twitterConsumerSecret = twitterConsumerSecret;
        return this;
    }

    public ShareConfig debug(boolean isDebug) {
        debug = isDebug;
        return this;
    }

    public String getWxId() {
        return wxId;
    }

    public String getWxSecret() {
        return wxSecret;
    }

    public String getQqId() {
        return qqId;
    }

    public String getWeiboId() {
        return weiboId;
    }

    public String getWeiboRedirectUrl() {
        return weiboRedirectUrl;
    }

    public String getWeiboScope() {
        return weiboScope;
    }

    public String getInsClientId() {
        return insClientId;
    }

    public String getInsScope() {
        return insScope;
    }

    public String getInsRedirectURIs() {
        return insRedirectURIs;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public String getFbClientId() {
        return fbClientId;
    }

    public String getFbClientScheme() {
        return fbClientScheme;
    }

    public String getTwitterConsumerKey() {
        return twitterConsumerKey;
    }

    public String getTwitterConsumerSecret() {
        return twitterConsumerSecret;
    }

    public boolean isDebug() {
        return debug;
    }
}
