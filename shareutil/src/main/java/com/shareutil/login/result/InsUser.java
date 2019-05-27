package com.shareutil.login.result;

import org.json.JSONObject;

/**
 * Describe : ins user data
 * Created by Leo on 2019/5/23.
 */
public class InsUser extends BaseUser {

    private String accessToken;

    public InsUser(JSONObject jsonObject) {

        if (jsonObject == null) {
            return;
        }

        if (!jsonObject.has("user")) {
            return;
        }

        JSONObject userObject = jsonObject.optJSONObject("user");

        if (userObject.has("id")) {
            setOpenId(userObject.optString("id"));
        }

        if (userObject.has("username")) {
            setNickname(userObject.optString("username"));
        }

        if (userObject.has("access_token")) {
            setAccessToken(jsonObject.optString("access_token"));
        }

        if (userObject.has("profile_picture")) {
            setHeadImageUrl(userObject.optString("profile_picture"));
            setHeadImageUrlLarge(userObject.optString("profile_picture"));
        }

        if (userObject.has("gender")) {
            String gender = userObject.optString("gender");
            setSex(gender.endsWith("male") ? 1 : (gender.equals("female") ? 2 : 0));
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
