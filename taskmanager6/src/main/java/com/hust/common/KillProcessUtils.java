package com.hust.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.hust.model.ClassListItem;
import com.hust.model.MainListItem;

import java.util.List;

/**
 * Created by laidong on 20/09/2015.
 */
public class KillProcessUtils {

    Context mContext;

    public static KillProcessUtils instance;
    ActivityManager activityManager;

    public KillProcessUtils(Context mContext) {
        this.mContext = mContext;
        activityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
    }

    KillProcessListener killProcessListener;

    public KillProcessUtils(Context mContext, KillProcessListener killProcessListener) {
        this.mContext = mContext;
        activityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        this.killProcessListener = killProcessListener;
    }

    public static KillProcessUtils getInstance(Context mContext) {
        if (instance == null) {
            instance = new KillProcessUtils(mContext);
        }
        return instance;
    }

    public static KillProcessUtils getInstance(Context mContext,KillProcessListener killProcessListener) {
        if (instance == null) {
            instance = new KillProcessUtils(mContext,killProcessListener);
        }
        return instance;
    }

    public void killAll(List<MainListItem> items) {
        boolean type = true;
        for (MainListItem item : items) {
            killTask(item);
        }
        if(killProcessListener!=null){
            killProcessListener.onKillTaskComplete(items.size());
        }

    }

    public interface KillProcessListener {
        void onKillTaskComplete(int count);
    }

    public void killTask(final MainListItem item) {
        int time = 0;
        if (!item.classNames.isEmpty()) {
            for (ClassListItem name : item.classNames) {
                Intent intent = new Intent();
                intent.setClassName(item.processName, name.className);
                try {
                    time = 1000;
                    mContext.stopService(intent);
                } catch (SecurityException e) {
//    				Toast.makeText(context, item.label + "disabled", Toast.LENGTH_SHORT).show();
                }
            }
        }
        final int sleepTime = time;
        Thread thread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                activityManager.killBackgroundProcesses(item.processName);
            }
        };
        thread.start();
        if(killProcessListener!=null){
            killProcessListener.onKillTaskComplete(1);
        }

    }
}
