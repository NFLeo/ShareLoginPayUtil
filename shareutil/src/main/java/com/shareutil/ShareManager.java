package com.shareutil;

public class ShareManager {

    public static ShareConfig CONFIG = new ShareConfig();

    public static void init(ShareConfig config) {
        CONFIG = config;
    }
}
