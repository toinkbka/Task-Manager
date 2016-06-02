package com.hust.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hust.model.ClassListItem;
import com.hust.model.CpuListItem;
import com.hust.model.MainListItem;
import com.hust.adapter.TaskAdapter;
import com.hust.database.AppSave;
import com.hust.database.DatabaseManager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
//import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.widget.TextView;

public class ProcessThread extends Thread {
    private ActivityManager activityManager;
    private PackageManager packageManager;
    private ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    private TextView textView;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private Handler handler = new Handler();
    private List<MainListItem> items, newItems;
    private TaskAdapter adapter;
    private List<CpuListItem> cpuData;
    private boolean loopStatus;
    private int activityNo, sortNo;
    Context mContext;

    public ProcessThread(Context context, ActivityManager activityManager,
                         TextView textView, List<MainListItem> items,
                         TaskAdapter adapter,
                         int activityNo) {
        this.activityManager = activityManager;
        mContext=context;
        packageManager = context.getPackageManager();
        this.textView = textView;
        this.items = items;
        this.adapter = adapter;
        this.loopStatus = true;
        this.activityNo = makeNo(activityNo);
        this.sortNo = makeNo(sortNo);
    }

    public void run() {
        cpuData = new ArrayList<CpuListItem>();

        while (loopStatus) {
            try {
            //    makeCpuData(items);
                newItems = new ArrayList<MainListItem>();
                ProcessUtils.getInstance(mContext).getAllList(newItems);
                activityManager.getMemoryInfo(memoryInfo);

                changeCpu(newItems);

                handler.post(new Runnable() {
                    public void run() {
                        copyItems(items, newItems);
                        textView.setText("RAM free: "
                                + numberFormat
                                .format(memoryInfo.availMem / 1024)
                                + " kB");
                        adapter.notifyDataSetChanged();
                        FileOutputStream fileout;
                       String textRam = numberFormat.format(memoryInfo.availMem/1024)+ "/n";
                        try {
                            fileout = new FileOutputStream(
                                    new File("/sdcard/ram.txt"), true);
                            fileout.write(textRam.toString().getBytes());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        this.loopStatus = false;
    }

    private void makeCpuData(List<MainListItem> items) {
        for (final MainListItem item : items) {
            Thread thread = new Thread() {
                public void run() {
                    int min = 1000, oldData, newData, result;
                    boolean type = true;
                    oldData = ProcessUtils.getInstance(mContext).makeUsageCpu(item.pid);
                    try {
                        Thread.sleep(min);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    newData = ProcessUtils.getInstance(mContext).makeUsageCpu(item.pid);
                    result = (newData - oldData) * 1000 / min;
                    if (result < 0)
                        result = 0;
                    for(Iterator<CpuListItem> iterator = cpuData.iterator(); iterator.hasNext();){
                        CpuListItem value = iterator.next();
                        if(value.pid == item.pid){
                            if (result == 0)
                                cpuData.remove(value);
                            else
                                value.cpu = result;
                            type = false;
                            break;
                        }
                    }
                   /* for (CpuListItem item2 : cpuData) {
                        if (item2.pid == item.pid) {
                            if (result == 0)
                                cpuData.remove(item2);
                            else
                                item2.cpu = result;
                            type = false;
                            break;
                        }
                    }*/
                    if (type && result != 0)
                        cpuData.add(new CpuListItem(item.pid, result));
                }
            };
            thread.start();
        }
    }

    private void changeCpu(List<MainListItem> items) {
        for (CpuListItem item2 : cpuData) {
            for (MainListItem item1 : items) {
                if (item1.pid == item2.pid) {
                    item1.cpu = item2.cpu;
                    break;
                }
            }
        }
    }

    protected void copyItems(List<MainListItem> items1,
                             List<MainListItem> items2) {
        List<AppSave> listApp = DatabaseManager.getInstance().getAllListApp();
        List<String> list = new ArrayList<>();
        for (AppSave app : listApp) {
            list.add(app.processName.trim());
        }

        items1.clear();
        if (activityNo == 0) {
            for (MainListItem item : items2) {
                if (!list.contains(item.processName)) {
                    items1.add(item);
                }

            }
        }

        if (activityNo == 1) {
            for (MainListItem item : items2) {
                if (list.contains(item.processName)) {
                    items1.add(item);
                    list.remove(item.processName);
                }

            }

            if(list.size()>0){
                for(String st:list){
                    try {
                        ApplicationInfo appInfo = packageManager
                                .getApplicationInfo(st, 0);
                        items.add(new MainListItem(packageManager
                                .getApplicationIcon(appInfo),
                                ProcessUtils.getInstance(mContext).returnLabel(st), st,
                                -1, 0, 0, true,
                               0, null, null));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private int makeNo(int no) {
        if (no > -1 && no < 3)
            return no;
        else
            return 0;
    }
}