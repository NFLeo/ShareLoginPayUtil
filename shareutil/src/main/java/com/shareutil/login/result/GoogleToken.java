package com.shareutil.login.result;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONObject;

public class GoogleToken extends BaseToken {

    private GoogleSignInAccount signInAccount;

    private int expiresIn;

    private String tokenType;

    private String idToken;

    private String scope;

    private String serverAuthCode;

    public GoogleToken(GoogleSignInAccount signInAccount) {
        if (signInAccount == null) {
            return;
        }

        setSignInAccount(signInAccount);
        setAccessToken(signInAccount.getIdToken());
    }

    public void parse(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        if (jsonObject.has("access_token")) {
            setAccessToken(jsonObject.optString("access_token"));
        }

        if (jsonObject.has("expires_in")) {
            setExpiresIn(jsonObject.optInt("expires_in"));
        }

        if (jsonObject.has("token_type")) {
            setTokenType(jsonObject.optString("token_type"));
        }

        if (jsonObject.has("id_token")) {
            setIdToken(jsonObject.optString("id_token"));
            setOpenid(getIdToken());
        }

        if (jsonObject.has("scope")) {
            setScope(jsonObject.optString("scope"));
        }
    }

    public GoogleSignInAccount getSignInAccount() {
        return signInAccount;
    }

    public void setSignInAccount(GoogleSignInAccount signInAccount) {
        this.signInAccount = signInAccount;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getServerAuthCode() {
        return serverAuthCode;
    }

    public void setServerAuthCode(String serverAuthCode) {
        this.serverAuthCode = serverAuthCode;
    }

    @Override
    public String toString() {
        return "GoogleToken{" +
                "signInAccount=" + signInAccount +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", idToken='" + idToken + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
