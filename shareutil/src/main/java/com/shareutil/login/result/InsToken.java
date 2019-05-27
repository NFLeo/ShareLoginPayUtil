package com.shareutil.login.result;

public class InsToken extends BaseToken {

    private String code;

    public InsToken(String url) {

        String[] split = url.split("[?]");
        if (split != null && split.length == 2) {
            String params = split[1];
            String[] paramList = params.split("[&]");
            for (String param : paramList) {
                String[] keyValue = param.split("[=]");
                if (keyValue[0].equals("code")) {
                    code = keyValue[1];
                }
            }
        }
    }

    public String getCode() {
        return code;
    }
}
