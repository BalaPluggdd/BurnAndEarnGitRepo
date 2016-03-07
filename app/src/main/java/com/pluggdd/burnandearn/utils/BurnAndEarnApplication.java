package com.pluggdd.burnandearn.utils;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by User on 10-Dec-15.
 */
public class BurnAndEarnApplication extends Application {

    private static BurnAndEarnApplication sApplicationInstatance;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        // Set up volley request queue
        sApplicationInstatance = this;

    }

    public synchronized static BurnAndEarnApplication getInstance() {
        return sApplicationInstatance;
    }

    public static Context getAppcontext(){
        return  sApplicationInstatance.getApplicationContext();
    }


}
