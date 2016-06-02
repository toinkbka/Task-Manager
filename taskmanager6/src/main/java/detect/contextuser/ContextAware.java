package detect.contextuser;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.hust.common.ProcessUtils;
import com.hust.model.MainListItem;

import java.util.ArrayList;
import java.util.List;

import main.hut.ShellUtils;

/**
 * Created by Lai Dong on 5/31/2016.
 */
public class ContextAware extends Thread {
    ActivityManager activityManager = null;
    Context context = null;
    PackageManager manager;
    ActivityManager.MemoryInfo memoryInfo;
    public ContextAware(Context context){
        this.context = context;
        activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        manager = context.getPackageManager();
    }

    @Override
    public void run() {
        while(true){
            List<MainListItem> items = new ArrayList<MainListItem>();
            ProcessUtils.getInstance(context).getAllList(items);
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
            for(MainListItem item : items){
                if(!item.processName.equals("com.hust.activity")&&!appSystem.contains(item.processName)) {
                    if ((item.importance == 300 || item.importance == 400)) {
                        if (ShellUtils.runCmd("am force-stop " + item.processName)) {
                            Log.e("App: ", item.label + "Hibernated ");
                        }
                    }
                }
            }
        }
    }
}
