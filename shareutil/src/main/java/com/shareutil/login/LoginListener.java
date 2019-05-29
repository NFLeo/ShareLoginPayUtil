package com.shareutil.login;

import com.shareutil.login.result.BaseToken;

public abstract class LoginListener {

    public abstract void loginSuccess(LoginResultData result);

    public void beforeFetchUserInfo(BaseToken token) {
    }

    public abstract void loginFailure(Exception e, int errorCode);

    public abstract void loginCancel();
}
