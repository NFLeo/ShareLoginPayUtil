package com.shareutil.login.result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FacebookUser extends BaseUser {
    private ShadowBean mShadowBean;

    public FacebookUser(JSONObject jsonObject) throws JSONException {
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
            JSONObject picJson = jsonObject.getJSONObject("picture");
            if (picJson.has("url")) {
                setHeadImageUrl(picJson.getString("url"));
                setHeadImageUrlLarge(picJson.getString("url"));
            }
        }
    }

    public ShadowBean getShadowBean() {
        return mShadowBean;
    }

    public void setShadowBean(ShadowBean shadowBean) {
        mShadowBean = shadowBean;
    }

    public static class ShadowBean {

        /**
         * {
         * "picture": {
         * "data": {
         * "height": 50,
         * "is_silhouette": false,
         * "url": "https:\/\/platform-lookaside.fbsbx.com\/platform\/profilepic\/?asid=113457059789516&height=50&width=50&ext=1553045173&hash=AeQQylAIZzg5-91Q",
         * "width": 50
         * }* 	},
         * "name": "Jinqiang Xie",
         * "id": "113457059789516",
         * "permissions": {
         * "data": [{
         * "permission": "public_profile",
         * "status": "granted"
         * }        ]
         * }
         */

        private Picture picture;

        private String name;

        private String id;

        private Permissions permissions;


        public Picture getPicture() {
            return picture;
        }

        public void setPicture(Picture picture) {
            this.picture = picture;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Permissions getPermissions() {
            return permissions;
        }

        public void setPermissions(Permissions permissions) {
            this.permissions = permissions;
        }

        public static class Picture {

            private DataBean data;

            public DataBean getData() {
                return data;
            }

            public void setData(DataBean data) {
                this.data = data;
            }

            public static class DataBean {
                private int height;
                private int width;
                private String url;
                private boolean is_silhouette;

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public boolean isSilhouette() {
                    return is_silhouette;
                }

                public void setSilhouette(boolean silhouette) {
                    is_silhouette = silhouette;
                }
            }
        }

        public static class Permissions {
            private List<DataBean> data;


            public List<DataBean> getData() {
                return data;
            }

            public void setData(List<DataBean> data) {
                this.data = data;
            }

            public static class DataBean {
                private String permission;

                private String status;

                public String getPermission() {
                    return permission;
                }

                public void setPermission(String permission) {
                    this.permission = permission;
                }

                public String getStatus() {
                    return status;
                }

                public void setStatus(String status) {
                    this.status = status;
                }
            }
        }
    }
}
