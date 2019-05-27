package com.shareutil;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.shareutil.login.LoginPlatform;

public class _ShareActivity extends Activity {

    private int mType;
    private boolean isNew;

    private static final String TYPE = "share_activity_type";

    public static void newInstance(Context context, int type) {
        if (context == null) {
            return;
        }

        Intent intent = new Intent(context, _ShareActivity.class);
        if (context instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(TYPE, type);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShareLogger.i(ShareLogger.INFO.ACTIVITY_CREATE);
        isNew = true;

        // init data
        mType = getIntent().getIntExtra(TYPE, 0);
        if (mType == ShareUtil.TYPE) {
            // 分享
            ShareUtil.action(this);
        } else if (mType == LoginUtil.TYPE) {
            if (LoginUtil.getPlatform() == LoginPlatform.INS) {
                setCusContentView();
            }
            // 登录
            LoginUtil.action(this);
        } else if (mType == PayUtil.TYPE) {
            PayUtil.action(this);
        } else {
            // handle 微信回调
            LoginUtil.handleResult(-1, -1, getIntent());
            ShareUtil.handleResult(-1, -1, getIntent());
            finish();
        }
    }

    private void setCusContentView() {
        FrameLayout container = new FrameLayout(_ShareActivity.this);
        container.setId(R.id.id_share_container);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(container);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShareLogger.i(ShareLogger.INFO.ACTIVITY_RESUME);
        if (isNew) {
            isNew = false;
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LoginUtil.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ShareLogger.i(ShareLogger.INFO.ACTIVITY_NEW_INTENT);
        handleCallBack(0, 0, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ShareLogger.i(ShareLogger.INFO.ACTIVITY_RESULT);
        handleCallBack(requestCode, resultCode, data);
    }

    // 处理回调
    private void handleCallBack(int requestCode, int resultCode, Intent data) {
        if (mType == LoginUtil.TYPE) {
            LoginUtil.handleResult(requestCode, resultCode, data);
        } else if (mType == ShareUtil.TYPE) {
            ShareUtil.handleResult(requestCode, resultCode, data);
        } else if (mType == PayUtil.TYPE) {
            PayUtil.handleResult(data);
        }
        finish();
    }
}
