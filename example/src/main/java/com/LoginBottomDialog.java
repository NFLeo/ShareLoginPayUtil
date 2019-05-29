package com;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.shareutil.LoginUtil;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResultData;

import me.shaohui.bottomdialog.BaseBottomDialog;

public class LoginBottomDialog extends BaseBottomDialog implements View.OnClickListener {

    private Context mContext;

    @Override
    public int getLayoutRes() {
        return R.layout.layout_bottom_login;
    }

    @Override
    public void bindView(final View v) {
        v.findViewById(R.id.share_qq).setOnClickListener(this);
        v.findViewById(R.id.share_weibo).setOnClickListener(this);
        v.findViewById(R.id.share_wx).setOnClickListener(this);
        mContext = v.getContext();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_qq:
                LoginUtil.login(getContext(), LoginPlatform.INS, new LoginListener() {
                    @Override
                    public void loginSuccess(LoginResultData result) {
                        Toast.makeText(mContext, "登陆成功 " + result.getUserInfo().getNickname(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void loginFailure(Exception e, int errorCode) {
                        Toast.makeText(mContext, "登录失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void loginCancel() {
                        Toast.makeText(mContext, "取消登录 ", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.share_weibo:
                LoginUtil.login(getContext(), LoginPlatform.FACEBOOK, new LoginListener() {
                    @Override
                    public void loginSuccess(LoginResultData result) {
                        Toast.makeText(mContext, "登陆成功 " + result.getUserInfo().getNickname(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void loginFailure(Exception e, int errorCode) {
                        Toast.makeText(mContext, "登录失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void loginCancel() {
                        Toast.makeText(mContext, "取消登录 ", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.share_wx:
                LoginUtil.login(getContext(), LoginPlatform.GOOGLE, new LoginListener() {
                    @Override
                    public void loginSuccess(LoginResultData result) {
                        Toast.makeText(mContext, "登陆成功 " + result.getUserInfo().getNickname(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void loginFailure(Exception e, int errorCode) {
                        Toast.makeText(mContext, "登录失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void loginCancel() {
                        Toast.makeText(mContext, "取消登录 ", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }

        dismiss();
    }

    @Override
    public void dismiss() {
        LoginUtil.recycle();
        super.dismiss();
    }
}
