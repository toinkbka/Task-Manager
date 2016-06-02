package com.hust.activity;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;


/**
 * Created by laidong on 20/09/2015.
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Configuration configuration=new Configuration.Builder(this).setDatabaseName("TaskManger.db").setDatabaseVersion(1).create();
        ActiveAndroid.initialize(configuration);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }
}
