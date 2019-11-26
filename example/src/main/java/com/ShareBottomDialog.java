package com;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Toast;

import com.shareutil.ShareUtil;
import com.shareutil.share.ShareListener;
import com.shareutil.share.SharePlatform;

import me.shaohui.bottomdialog.BaseBottomDialog;

public class ShareBottomDialog extends BaseBottomDialog implements View.OnClickListener {

    private Context mContext;

    @Override
    public int getLayoutRes() {
        return R.layout.layout_bottom_share;
    }

    @Override
    public void bindView(final View v) {
        v.findViewById(R.id.share_qq).setOnClickListener(this);
        v.findViewById(R.id.share_qzone).setOnClickListener(this);
        v.findViewById(R.id.share_weibo).setOnClickListener(this);
        v.findViewById(R.id.share_wx).setOnClickListener(this);
        v.findViewById(R.id.share_wx_mini).setOnClickListener(this);
        v.findViewById(R.id.share_wx_timeline).setOnClickListener(this);
        v.findViewById(R.id.share_system).setOnClickListener(this);

        mContext = v.getContext();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_qq:
                ShareUtil.shareImage(getContext(), SharePlatform.QQ,
                        "http://android-screenimgs.25pp.com/fs08/2018/05/11/4/110_f77a9c519c81005292e24f6eb324ea3b_234x360.jpg",
                        shareListener);
                break;
            case R.id.share_qzone:
                ShareUtil.shareMedia(getContext(), SharePlatform.QZONE, "Title", "summary",
                        "https://www.baidu.com", "http://android-screenimgs.25pp.com/fs08/2018/05/11/4/110_f77a9c519c81005292e24f6eb324ea3b_234x360.jpg",
                        shareListener);
                break;
            case R.id.share_weibo:
                ShareUtil.shareText(getContext(), SharePlatform.WEIBO, "测试微博分享文字", shareListener);
                break;
            case R.id.share_wx_timeline:
                ShareUtil.shareImage(getContext(), SharePlatform.WX_TIMELINE,
                        "http://android-screenimgs.25pp.com/fs08/2018/05/11/4/110_f77a9c519c81005292e24f6eb324ea3b_234x360.jpg", shareListener);
                break;
            case R.id.share_wx:
                ShareUtil.shareMedia(getContext(), SharePlatform.WX, "标题", "内容", "http://www.baidu.com",
                        "http://img.funplanet.cn/user/photo/53/358de6e7da5212cb725872bda47113af.jpg", shareListener);
                break;
            case R.id.share_wx_mini:
                ShareUtil.shareMedia(getContext(), SharePlatform.WX, "标题", "内容", "http://www.baidu.com",
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                        "gh_41bb43658d5e", "share/card/card", shareListener);
                break;
            case R.id.share_system:
                ShareUtil.shareMedia(getContext(), SharePlatform.DEFAULT, "标题", "内容",
                        "http://www.baidu.com", R.mipmap.ic_launcher, shareListener);
                break;
            default:
                break;
        }
    }

    private ShareListener shareListener = new ShareListener() {
        @Override
        public void shareStart() {
            Toast.makeText(mContext, "开始分享 ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void shareSuccess() {
            Toast.makeText(mContext, "分享成功 ", Toast.LENGTH_SHORT).show();
            dismiss();
        }

        @Override
        public void shareFailure(Exception e) {
            Toast.makeText(mContext, "分享失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
            dismiss();
        }

        @Override
        public void shareCancel() {
            Toast.makeText(mContext, "取消分享", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    };

    @Override
    public void dismiss() {
        ShareUtil.recycle();
        super.dismiss();
    }
}
