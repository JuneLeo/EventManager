package com.event.leo.application;

import android.app.Application;

import com.event.leo.event.notify.ActivityEventManager;

/**
 * Created by 宋鹏飞 on 2018/4/13.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityEventManager.init(this);
    }
}
