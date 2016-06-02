package com.hust.common;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Debug;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.hust.model.ClassListItem;
import com.hust.model.CpuListItem;
import com.hust.model.MainListItem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by laidong on 20/09/2015.
 */
public class ProcessUtils {

    private ActivityManager activityManager;
    private PackageManager packageManager;
    private Context mContext;
    public static ProcessUtils instance;

    public ProcessUtils(Context mContext){
        activityManager= (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        packageManager=mContext.getPackageManager();
        this.mContext=mContext;
    }

    public static ProcessUtils getInstance(Context mContext){
        if(instance==null){
            instance=new ProcessUtils(mContext);
        }
        return instance;
    }
    public int usageMemory(int pid) {
        int pids[] = new int[1];
        pids[0] = pid;
        Debug.MemoryInfo[] memoryInfos = activityManager.getProcessMemoryInfo(pids);

        return memoryInfos[0].getTotalPss();
    }

    public List<ClassListItem> serviceClassNames(
            List<ActivityManager.RunningServiceInfo> runningService, ActivityManager.RunningAppProcessInfo app) {
        List<ClassListItem> classNames = new ArrayList<ClassListItem>();
        if (runningService != null) {
            for (ActivityManager.RunningServiceInfo srv : runningService) {
                if (app.processName.equals(srv.process)) {
                    classNames
                            .add(new ClassListItem(srv.service.getClassName()));
                }
            }
        }
        return classNames;
    }

    public String mainClassName(List<ResolveInfo> appList,
                                 ActivityManager.RunningAppProcessInfo app) {
        if (appList != null) {
            for (ResolveInfo resInfo : appList) {
                if (app.processName.equals(resInfo.activityInfo.processName)) {
                    return resInfo.activityInfo.name;
                }
            }
        }
        return "";
    }



    public String returnLabel(String tag) {
        try {
            return (String) packageManager.getApplicationLabel(packageManager
                    .getApplicationInfo(tag, 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void getAllList(List<MainListItem> items) {
        Intent it = new Intent(Intent.ACTION_MAIN);
        it.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ActivityManager.RunningAppProcessInfo> runningApp = activityManager
                .getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> runningService = activityManager
                .getRunningServices(100);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(it, 0);

        if (runningApp != null) {
            for (ActivityManager.RunningAppProcessInfo app : runningApp) {
                try {
                    ApplicationInfo appInfo = packageManager
                            .getApplicationInfo(app.processName, 0);
                    items.add(new MainListItem(packageManager
                            .getApplicationIcon(appInfo),
                            ProcessUtils.getInstance(mContext).returnLabel(app.processName), app.processName,
                            app.pid, ProcessUtils.getInstance(mContext).usageMemory(app.pid), 0, true,
                            app.importance, ProcessUtils.getInstance(mContext).serviceClassNames(runningService,
                            app), ProcessUtils.getInstance(mContext).mainClassName(appList, app)));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public int makeUsageCpu(int pid) {
        try {
            String buf = "/proc/" + String.valueOf(pid) + "/stat";
            FileReader in = new FileReader(buf);
            BufferedReader br = new BufferedReader(in);
            String line;
            int result = 0;

            while ((line = br.readLine()) != null) {
                String[] strAry = line.split(" ");
                result = Integer.valueOf(strAry[12])
                        + Integer.valueOf(strAry[13]);
            }
            br.close();
            in.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /*public void SetAlarmStartService(){
        Intent alarmIntent = new Intent(mContext, ServiceKillApp.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(mContext);
        String timeString=mSharedPreferences.getString("lstSetting", "10000");
        long time=Long.parseLong(timeString);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), time, pendingIntent);
        Toast.makeText(mContext, "Alarm Set time "+time, Toast.LENGTH_SHORT).show();
    }

    public void SetAlarmStartService(String timeString){
        Intent alarmIntent = new Intent(mContext, ServiceKillApp.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        long time=Long.parseLong(timeString);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), time, pendingIntent);
        Toast.makeText(mContext, "Alarm Set time "+time, Toast.LENGTH_SHORT).show();
    }
*/
}
