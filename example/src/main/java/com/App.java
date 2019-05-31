package com;

import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;

/**
 * Describe :
 * Created by Leo on 2018/11/28 on 9:34.
 */
public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Crashlytics());
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
