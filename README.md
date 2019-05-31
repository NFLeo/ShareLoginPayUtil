
# 改自https://github.com/shaohui10086/ShareUtil ，在其基础上添加些许功能及优化，改动如下：
```
    1. 使用rxjava2
    2. 添加小程序分享支持
    3. 部分代码优化
    4. 添加微信、支付宝支付，避免主目录添加wx文件夹方式
    5. 同步更新微博官方分享支付（2018-6-22）
    6. 支持网络依赖
    7. 支持Instagram、Google、FaceBook、Twitter海外登录方式，调用方式一致
```

## TODO
- 支持多图分享
- 支持视频分享

## ChangeLog

#### 2019-05-30
- 添加并测试成功Twitter登录

#### 2019-05-29
- 添加并测试成功Instagram登录
- 添加并测试成功FaceBook登录
- 添加并测试成功Google登录

#### 2019-05-23
- 更新支付宝支付（修改为aar依赖方式）
- 去除登录、分享、支付所产生的内存泄漏

#### 2018-12-3
- 统一provider authorities方便多provider并存
- authorities 必须为 包名.file.provider

#### 2018-11-28
- 修复分享、登录、支付引发的内存泄漏
- 登录结果回调新增登录失败code
- 修复7.0无法调用系统分享问题

#### 2018-08-27
- 抽离出gradle基础配置，方便调用时更改

#### 2018-07-27
- 修复微信支付成功无法获取回调

#### 2018-07-12
- 修复QQ分享无法获取成功回调

#### 2018-07-11
- 兼容安卓8.0

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

5. 混淆代码见 example module下 混淆文件

## Usage

### 使用配置

#### 使用方式
1. 项目主module中 build.gradle 添加如下代码
```
    defaultConfig {
        ...
        // 必须配置qqid 否则无法获取分享成功回调
        // 注：以下三个参数均为占位，里面的值随意
        manifestPlaceholders = [
                qq_id: "1106618327"
                FACEBOOK_APP_ID: "1106618327",
                FACEBOOK_SCHEME: "1106618327"
        ]
        ...
    }

    // 新版本发布不了，只能通过aar方式了
    // api 'com.nfleo:ShareLoginPayUtil:1.1.1'
```
2. 项目build.gradle 中添加如下代码
```
    buildscript {
        repositories {
            jcenter()
            maven { url "https://dl.bintray.com/thelasterstar/maven/" }
        }
    }

    allprojects {
        repositories {
            maven {url "https://maven.google.com"}
            google()

            // 微博 SDK 所需的配置
            maven { url "https://dl.bintray.com/thelasterstar/maven/" }

            // 支付宝 SDK AAR 包所需的配置
            flatDir {
                dirs '../shareutil/libs'
            }
            jcenter()
        }
    }

    ......

    // 所需版本可自行配置
    ext {
        compileSdkVersion = 27

        minSdkVersion = 21
        targetSdkVersion = 27
        supportVersion = "27.1.1"
    }
```

3. 在使用之前设置在各个平台申请的Id，以及分享的回调（推荐放在Application的onCreate方法中）
    
            // init
            ShareConfig config = ShareConfig.instance()
                    .qqId(QQ_ID)
                    .wxId(WX_ID)
                    .weiboId(WEIBO_ID)
                    // 下面两个，如果不需要登录功能，可不填写
                    .weiboRedirectUrl(REDIRECT_URL)
                    .wxSecret(WX_ID);
            ShareManager.init(config);

            ** 系统分享图片必须添加以下两步
            1. 必须在AndroidManifest.xml添加 provider
            <!--兼容系统分享 7.0不支持uri跳转 必须使用content://替代-->
            <provider
                android:name="android.support.v4.content.FileProvider"
                android:authorities="${applicationId}.file.provider"  // 必须写成这个值
                android:exported="false"
                android:grantUriPermissions="true">
                <meta-data
                    android:name="android.support.FILE_PROVIDER_PATHS"
                    android:resource="@xml/file_paths" />
            </provider>

            2.res文件夹下创建xml文件夹，并在xml下创建file_paths.xml 文件名随便取
            注：分享图片保存地址为 /storage/emulated/0/Android/data/com.leo.project/files/share_image.jpg
            因此保存到 外部存储区域根目录下（使用 external-files-path）
            <paths xmlns:android="http://schemas.android.com/apk/res/android">
                <external-files-path name="name" path="" />
            </paths>

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

        微信版本：com.tencent.mm.opensdk:wechat-sdk-android-without-mta:5.1.4
        QQ版本：libs/open_sdk_r6008_lite.jar
        微博版本: com.sina.weibo.sdk:core:4.1.0:openDefaultRelease@aar
        支付宝版本： 'libs/alipaySdk-20180403.jar'
3. 分享的bitmap，会在分享之后被回收掉，所以分享之后最好不要再对该bitmap做任何操作。
4. ShareListener的回调结果仅供参考，不可当做分享是否返回的依据，它并不是那么完全可靠，因为某些操作，例如微博分享取消，但是用户选择了保存草稿，这时候客户端并不会收到回调，所以也就不会调用ShareListener的onCancel

## Thanks
- https://github.com/shaohui10086/ShareUtil
- https://github.com/tianzhijiexian/ShareLoginLib