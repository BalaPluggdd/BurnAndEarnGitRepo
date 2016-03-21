package com.pluggdd.burnandearn.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.iid.InstanceIDListenerService;
import com.pluggdd.burnandearn.service.MyInstanceIDListenerService;
import com.pluggdd.burnandearn.service.RegistrationIntentService;

import io.fabric.sdk.android.Fabric;

/**
 * Created by User on 10-Dec-15.
 */
public class BurnAndEarnApplication extends Application {

    private static BurnAndEarnApplication sApplicationInstatance;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(getBaseContext());
        Fabric.with(this, new Crashlytics());
        // Set up volley request queue
        sApplicationInstatance = this;
        // To start GCM service
        if(!isMyServiceRunning(RegistrationIntentService.class)){
            startService(new Intent(getBaseContext(),RegistrationIntentService.class));
            startService(new Intent(getBaseContext(),MyInstanceIDListenerService.class));
        }
    }

    public synchronized static BurnAndEarnApplication getInstance() {
        return sApplicationInstatance;
    }

    public static Context getAppcontext(){
        return  sApplicationInstatance.getApplicationContext();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
