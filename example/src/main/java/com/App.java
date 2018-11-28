package com;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Describe :
 * Created by Leo on 2018/11/28 on 9:34.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
