package com.shareutil;

public class ShareManager {

    private static boolean isInit = false;

    public static ShareConfig CONFIG = new ShareConfig();

    public static void init(ShareConfig config) {
        isInit = true;
        CONFIG = config;
    }
}
