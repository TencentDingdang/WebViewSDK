package com.tencent.ai.tvs.dmsdk.demo;

import android.app.Application;

import com.tencent.ai.tvs.env.EnvManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EnvManager.getInstance().init(this);
    }
}
