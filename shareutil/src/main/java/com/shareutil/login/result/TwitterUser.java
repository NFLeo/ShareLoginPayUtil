package com.shareutil.login.result;

import com.twitter.sdk.android.core.models.User;

public class TwitterUser extends BaseUser {

    public TwitterUser(User user) {
        if (user == null) {
            return;
        }

        setHeadImageUrl(user.profileImageUrlHttps);
        setHeadImageUrlLarge(user.profileBackgroundImageUrlHttps);
        setNickname(user.name);
        setOpenId(user.idStr);
    }
}
