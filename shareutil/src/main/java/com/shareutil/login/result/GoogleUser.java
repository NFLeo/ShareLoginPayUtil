package com.shareutil.login.result;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleUser extends BaseUser {

    public GoogleUser(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            return;
        }

        if (jsonObject.has("id")) {
            setOpenId(jsonObject.getString("id"));
        }

        if (jsonObject.has("name")) {
            setNickname(jsonObject.getString("name"));
        }

        if (jsonObject.has("picture")) {
            setHeadImageUrl(jsonObject.getString("picture"));
            setHeadImageUrlLarge(jsonObject.getString("picture"));
        }

        if (jsonObject.has("gender")) {
            String gender = jsonObject.optString("gender");
            setSex(gender.endsWith("male") ? 1 : (gender.equals("female") ? 2 : 0));
        }
    }
}
