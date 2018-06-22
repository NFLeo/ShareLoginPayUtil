
# 改自https://github.com/shaohui10086/ShareUtil ，在其基础上添加些许功能及优化，改动如下：
```
    1. 使用rxjava2
    2. 添加小程序分享支持
    3. 部分代码优化
    4. 添加微信、支付宝支付，避免主目录添加wx文件夹方式
    5. 同步更新微博官方分享支付（2018-6-22）
```

## ChangeLog
#### 2018-06-22
- 去除银联支付
- 重写微博分享登录
- 支持资源图片分享
- 修改shareActivity为透明

#### 2018-05-07
- 添加微信支付、支付宝支付

#### 2018-04-29
- 修复小程序无法分享问题
- qq_id置于shareutil包中，引用时直接修改该值
- 暴露直接分享接口，分享过程需对图片进行二次处理（添加水印或其他处理），分享时直接传入处理后的图片，并调用直接分享方法


#### 2018-02-07
- 改用rxjava2库，支持小程序分享功能

# ShareUtil
`ShareUtil`是一个综合性的分享及登录工具库，
支持微信分享，微博分享，QQ分享，QQ空间分享以及Android系统默认分享,
分纯文字、纯图片、图文混合（支持小程序分享）分享方式
支持微信登录，微博登录以及QQ登录并获取用户信息。

## Feature

1. 多种分享方式： 纯文字、纯图片、图文混合
    
2. 支持分享图片本地链接，网络链接或者Bitmap， 不需要考虑各个平台的不一致性。

3. 支持微信、QQ、微博登录并获取登录用户信息

4. 支持小程序分享

## Usage

### 使用配置

1. 直接依赖shareutil项目

2. 主项目AnidroidManifest添加以下代码：
```
        <activity
                theme:"@style/transition"
                android:name="com.shareutil._ShareActivity"/>

        <activity-alias
                android:name=".wxapi.WXEntryActivity"
                android:exported="true"
                android:targetActivity="com.shareutil._ShareActivity" />
```
3. build.gradle 配置
在defaultConfig节点下增加你的qq id信息

项目会提示manifest缺少qq_id ，可忽略

        defaultConfig {
        	...
        	
            manifestPlaceholders = [
                    //  替换成你的qq_id
                    qq_id: "123456789"
            ]
            
        }
4. 在使用之前设置在各个平台申请的Id，以及分享的回调（推荐放在Application的onCreate方法中）
    
            // init
            ShareConfig config = ShareConfig.instance()
                    .qqId(QQ_ID)
                    .wxId(WX_ID)
                    .weiboId(WEIBO_ID)
                    // 下面两个，如果不需要登录功能，可不填写
                    .weiboRedirectUrl(REDIRECT_URL)
                    .wxSecret(WX_ID);
            ShareManager.init(config);

### 分享使用

        ShareUtil.shareImage(this, SharePlatform.QQ, "http://image.com", shareListener);
        ShareUtil.shareText(this, SharePlatform.WX, "分享文字", shareListener);
        ShareUtil.shareMedia(this, SharePlatform.QZONE, "title", "summary", "targetUrl", "thumb", shareListener);

        // miniId为小程序id    miniPath为小程序Path
        // 低版本微信不支持小程序分享， 此时默认网页分享方式
        ShareUtil.shareMedia(this, SharePlatform.WX, "title", "summary", "targetUrl", "thumb", "miniId", "miniPath", shareListener);

### 登录使用

            // LoginPlatform.WEIBO  微博登录   
            // LoginPlatform.WX     微信登录
            // LoginPlatform.QQ     QQ登录 
            final LoginListener listener = new LoginListener() {
                    @Override
                    public void loginSuccess(LoginResult result) {
                        //登录成功， 如果你选择了获取用户信息，可以通过
                    }
                
                    @Override
                    public void loginFailure(Exception e) {
                        Log.i("TAG", "登录失败");
                    }
        
                    @Override
                    public void loginCancel() {
                        Log.i("TAG", "登录取消");
                    }
                };
            LoginUtil.login(MainActivity.this, LoginPlatform.WEIBO, mLoginListener, isFetchUserInfo);

### 支付使用

        // PayPlatform.ALIPAY   支付宝支付
        // PayPlatform.WXPAY    微信支付
        PayListener mPayListener = new PayListener() {
            @Override
            public void paySuccess() {
                Toast.makeText(v.getContext(), "支付成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void payFailed(Exception e) {
                Toast.makeText(v.getContext(), "支付失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void payCancel() {
                Toast.makeText(v.getContext(), "支付取消", Toast.LENGTH_SHORT).show();
            }
        };

        // 支付宝支付
        AliPayParamsBean payParamsBean = new AliPayParamsBean();
        payParamsBean.setOrderInfo("xxxx");
        PayUtil.pay(getContext(), PayPlatform.ALIPAY, payParamsBean, mPayListener);

        // 微信支付
        WXPayParamsBean wxPayParamsBean = new WXPayParamsBean();
        wxPayParamsBean.setAppid("xxxx");
        wxPayParamsBean.setNonceStr("xxxx");
        wxPayParamsBean.setPartnerid("xxxx");
        wxPayParamsBean.setPackageValue("xxxx");
        wxPayParamsBean.setPrepayId("xxxx");
        wxPayParamsBean.setSign("xxxx");
        wxPayParamsBean.setTimestamp("xxxx");
        PayUtil.pay(getContext(), PayPlatform.WXPAY, wxPayParamsBean, mPayListener);

## 使用说明

1. QQ不支持纯文字分享，会直接分享失败
2. 使用Jar文件的版本如下：

        微信版本：com.tencent.mm.opensdk:wechat-sdk-android-without-mta:1.4.0
        QQ版本：libs/open_sdk_r5923_lite.jar
        微博版本: libs/weiboSDKCore_3.1.4.jar
3. 分享的bitmap，会在分享之后被回收掉，所以分享之后最好不要再对该bitmap做任何操作。
4. example 中的代码可以参考，但是不可运行，因为需要保证包名以及签名文件和你申请各个平台id所填写信息保持一致
5. ShareListener的回调结果仅供参考，不可当做分享是否返回的依据，它并不是那么完全可靠，因为某些操作，例如微博分享取消，但是用户选择了保存草稿，这时候客户端并不会收到回调，所以也就不会调用ShareListener的onCancel

## Thanks
- https://github.com/shaohui10086/ShareUtil
- https://github.com/tianzhijiexian/ShareLoginLib