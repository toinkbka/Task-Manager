package detect.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hust.common.ProcessUtils;
import com.hust.model.MainListItem;

import java.util.ArrayList;
import java.util.List;

import detect.contextuser.ContextAware;
import detect.contextuser.RecentTaskHelper;
import detect.contextuser.Task;
import detect.netmanager.WifiReceiver;
import main.hut.ShellUtils;

/**
 * Created by Lai Dong on 3/29/2016.
 */
public class ServiceDetect extends Service {
    WifiManager mWifiManager;
    WifiReceiver mWifiReceiver;
    String currentRunningActivityName = "";
    String namePackage = "";
    boolean kill = true;
    public int singalStenths = 0;

    PackageManager manager;
    static List<String> listAppClosed = new ArrayList<>();
    static List<String> listAppFreezed = new ArrayList<>();
    static List<String> showMostAppUsed = new ArrayList<>();
    static ActivityManager activityManager;
    List<ActivityManager.RunningAppProcessInfo> listAppRunning = new ArrayList<ActivityManager.RunningAppProcessInfo>();

    Handler hd = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            listAppClosed = (List<String>) msg.obj;
            super.handleMessage(msg);
        }

    };

    Handler hd1 = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            listAppFreezed = (List<String>) msg.obj;
            super.handleMessage(msg);
        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.e("Service", "Onstart service");
        // Run CheckRunningActivity on Application start (or on device boot).
    //    new CheckRunningActivity(getApplicationContext()).start();
        /*Intent i = new Intent(this, UpdateService.class);
        startService(i);*/
     //   ContextUser();
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) {
            mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            WifiReceiver mWifiReceiver = new WifiReceiver(mWifiManager);
            IntentFilter mIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            this.registerReceiver(mWifiReceiver, mIntentFilter);
            mWifiManager.startScan();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
      //  this.unregisterReceiver(mWifiReceiver);
            kill = false;
      //      new CheckRunningActivity(getApplicationContext()).interrupt();
        super.onDestroy();
    }


    class CheckRunningActivity extends Thread {
        ActivityManager activityManager = null;
        Context context = null;

        public CheckRunningActivity(Context con) {
            context = con;
            activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
        }

        public void run() {

            while (kill) {
                // Return a list of the tasks that are currently running,
                // with the most recent being first and older ones after in
                // order.
                // Taken 1 inside getRunningTasks method means want to take only
                // top activity from stack and forgot the olders.
                List<ActivityManager.RunningTaskInfo> taskInfo = activityManager
                        .getRunningTasks(1);

                String taskName = taskInfo.get(0).topActivity.getPackageName();

                try {
                    String name = (String) getPackageManager()
                            .getApplicationLabel(
                                    getPackageManager().getApplicationInfo(
                                            taskName,
                                            PackageManager.GET_META_DATA));
                    if (!currentRunningActivityName.equals(name.trim())
                            && !namePackage.equals(taskName.trim())) {
                        currentRunningActivityName = name;
                        namePackage = taskName;
                        // call Task from Database
                        Task task = RecentTaskHelper.getInstance(
                                getApplicationContext()).getTaskByNameApp(
                                currentRunningActivityName);
                        if (task == null) {
                            task = new Task(currentRunningActivityName, 1,
                                    namePackage);
							/*if (!task.packApp.equals(getApplicationContext().getPackageName()))*/
                            String appPackage = (String) getPackageManager()
                                    .getApplicationLabel(
                                            getPackageManager()
                                                    .getApplicationInfo(
                                                            getApplicationContext()
                                                                    .getPackageName(),
                                                            PackageManager.GET_META_DATA));
                            if (!task.nameApp
                                    .equals(appPackage)&&!task.packApp.equals("com.sec.android.app.launcher")) {
                                RecentTaskHelper.getInstance(
                                        getApplicationContext()).insert(task);
                            }

                        } else {
                            int count = task.count;
                            count += 1;
                            task.count = count;
                            RecentTaskHelper.getInstance(
                                    getApplicationContext()).update(task.id,
                                    task);
                        }

                    } else {
						/*
						 * Log.e("Service", "Application running exist " +
						 * currentRunningActivityName);
						 */
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    // context user
    public void ContextUser(){
        manager = getPackageManager();
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        listAppRunning = activityManager.getRunningAppProcesses();
        List<String> listAppRunnings = new ArrayList<>();
        for(ActivityManager.RunningAppProcessInfo i: listAppRunning){
            if(!listAppRunnings.contains(i.processName))
                listAppRunnings.add(i.processName);
        }
        //list app he thong
        List<String> appSystem = new ArrayList<>();
        List<PackageInfo> list = manager.getInstalledPackages(0);
        for(PackageInfo pi : list) {
            try {
                ApplicationInfo ai = manager.getApplicationInfo(pi.packageName, 0);
                if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    appSystem.add(pi.packageName);
                }
            } catch (PackageManager.NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        List<ApplicationInfo> packages = manager
                .getInstalledApplications(PackageManager.GET_META_DATA);

        List<Task> tasks = RecentTaskHelper
                .getInstance(getApplicationContext()).getAllTask();
        // list App most used
        List<String> AppInstall = new ArrayList<>(); // tat ca cac app cai dat
        List<String> appTasks_nofrequently = new ArrayList<>(); // apps su dung nhung ko thuong xuyen
        List<String> appFullTasks = new ArrayList<>(); // tat ca cac app su dung save tren database
        String input = "com.samsung.inputmethod";
        String setting ="com.android.settings";

        for (Task i : tasks) {
            appFullTasks.add(i.packApp);

            if (i.count > 4) {
                showMostAppUsed.add(i.packApp);
            }
            else
                appTasks_nofrequently.add(i.packApp);
        }

        // phan loai nguoi dung
        for (String i : showMostAppUsed) {
            //	Log.e("App most Used : ", i);
        }

        if(showMostAppUsed.contains("com.android.contacts")&&showMostAppUsed.contains("com.android.mms")){
            Log.e("Xep loai nguoi dung: ", "Nghe gọi, nhắn tin");
        }
        else if(showMostAppUsed.contains("Contacts"))
            Log.e("Xep loai nguoi dung: ", "Nghe gọi");
        else
            Log.e("Xep loai nguoi dung: ", "Còn lại");

        for (ApplicationInfo packageInfo : packages) {
            AppInstall.add(packageInfo.packageName);
        }

        for(String i:AppInstall){
            if(!appFullTasks.contains(i)&& !appSystem.contains(i)
                    &&!i.equals("com.example.showlistpackage")&&!i.equals(setting)){
                // close App

						/*if (Utils.runCmd("pm block " + i.toString()
								+ ";pm disable " + i.toString())) {
							Toast.makeText(getApplicationContext(),
									"Đã tắt " + i.toString(),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(), "Disable Error",
									Toast.LENGTH_SHORT).show();
						}*/
                if(!listAppClosed.contains(i))
                    listAppClosed.add(i);
            }
        }
        Message msg = new Message();
        msg.obj = listAppClosed;
        hd.sendMessage(msg);

        for(String i: appTasks_nofrequently){
            if(!appTasks_nofrequently.contains(input)&&!i.equals(setting)){ //&& !appSystem.contains(i)
                // freeze app
						/*if(Utils.runCmd("am force-stop " + i.toString()))
						{
							Toast.makeText(getApplicationContext(),
									"đã đóng băng " + i.toString(),
									Toast.LENGTH_SHORT).show();
							listAppFreezed.add(i);
						}
						else {
							Toast.makeText(getApplicationContext(), "Freeze Error",
									Toast.LENGTH_SHORT).show();
						}*/
                if(!listAppFreezed.contains(i))
                    listAppFreezed.add(i);
            }
        }

        Message msg1 = new Message();
        msg1.obj = listAppFreezed;
        hd1.sendMessage(msg1);

    }

}
