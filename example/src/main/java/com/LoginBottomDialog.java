package com;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.shareutil.LoginUtil;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResult;

import java.lang.ref.WeakReference;

import me.shaohui.bottomdialog.BaseBottomDialog;

public class LoginBottomDialog extends BaseBottomDialog implements View.OnClickListener {

    private LoginListener mLoginListener;
    private Context mCotext;

    @Override
    public int getLayoutRes() {
        return R.layout.layout_bottom_login;
    }

    @Override
    public void bindView(final View v) {
        v.findViewById(R.id.share_qq).setOnClickListener(this);
        v.findViewById(R.id.share_weibo).setOnClickListener(this);
        v.findViewById(R.id.share_wx).setOnClickListener(this);
        mCotext = v.getContext();
        mLoginListener = new MyLoginListener(LoginBottomDialog.this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_qq:
                LoginUtil.login(getContext(), LoginPlatform.QQ, mLoginListener);
                break;
            case R.id.share_weibo:
                LoginUtil.login(getContext(), LoginPlatform.WEIBO, mLoginListener);
                break;
            case R.id.share_wx:
                LoginUtil.login(getContext(), LoginPlatform.WX, mLoginListener);
                break;
        }

        dismiss();
    }

    private static class MyLoginListener extends LoginListener {

        private WeakReference<LoginBottomDialog> context;

        MyLoginListener(LoginBottomDialog context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void loginSuccess(LoginResult result) {
            if (context.get() != null) {
                Toast.makeText(context.get().mCotext, "登陆成功 " + result.getUserInfo().getNickname(), Toast.LENGTH_SHORT).show();
            }
            LoginUtil.recycle();
            context.get().mLoginListener = null;
        }

        @Override
        public void loginFailure(Exception e, int errorCode) {
            if (context.get() != null) {
                Toast.makeText(context.get().mCotext, "登录失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            LoginUtil.recycle();
            context.get().mLoginListener = null;
        }

        @Override
        public void loginCancel() {
            if (context.get() != null) {
                Toast.makeText(context.get().mCotext, "登录取消", Toast.LENGTH_SHORT).show();
            }
            LoginUtil.recycle();
            context.get().mLoginListener = null;
        }
    }
}
