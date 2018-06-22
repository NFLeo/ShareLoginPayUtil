package com;

import android.view.View;
import android.widget.Toast;
import me.shaohui.bottomdialog.BaseBottomDialog;
import com.shareutil.LoginUtil;
import com.shareutil.login.LoginListener;
import com.shareutil.login.LoginPlatform;
import com.shareutil.login.LoginResult;

public class LoginBottomDialog extends BaseBottomDialog implements View.OnClickListener {

    private LoginListener mLoginListener;

    @Override
    public int getLayoutRes() {
        return R.layout.layout_bottom_login;
    }

    @Override
    public void bindView(final View v) {
        v.findViewById(R.id.share_qq).setOnClickListener(this);
        v.findViewById(R.id.share_weibo).setOnClickListener(this);
        v.findViewById(R.id.share_wx).setOnClickListener(this);

        mLoginListener = new LoginListener() {
            @Override
            public void loginSuccess(LoginResult result) {
                Toast.makeText(v.getContext(), "登陆成功 " + result.getUserInfo().getNickname(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void loginFailure(Exception e) {
                Toast.makeText(v.getContext(), "登录失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void loginCancel() {
                Toast.makeText(v.getContext(), "登录取消", Toast.LENGTH_SHORT).show();
            }
        };
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
}
