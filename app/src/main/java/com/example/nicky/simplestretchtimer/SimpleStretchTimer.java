package com.example.nicky.simplestretchtimer;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.google.android.gms.ads.MobileAds;
import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

/**
 * Created by Nicky on 8/18/17.
 */

public class SimpleStretchTimer extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initializeWithDefaults(this);
        }
        MobileAds.initialize(this, "ca-app-pub-9905467979125118~3385635611");

    }

}

