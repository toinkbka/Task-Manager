package detect.netmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hust.activity.R;

import java.util.ArrayList;
import java.util.List;

import firewall.Api;

/**
 * Created by Lai Dong on 3/26/2016.
 */
public class WifiReceiver extends BroadcastReceiver {
    // This method call when number of wifi connections changed
    WifiManager mWifiManager;
    static List<WifiName> listWifiname;
    private Handler mHandler = new Handler();
    ImageView img;
    TextView txt;

    public WifiReceiver(WifiManager wifi){
        this.mWifiManager = wifi;
    }
    public void onReceive(final Context c, Intent intent) {
        int state = mWifiManager.getWifiState();
        int maxLevel = 5;
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            // Get Scanned results in an array List
            List<ScanResult> wifiList = mWifiManager.getScanResults();
            final List<String> listSSID = new ArrayList<>();
            listWifiname = new ArrayList<>();
            // Iterate on the list
            for (ScanResult result : wifiList) {
                //The level of each wifiNetwork from 0-5
                 int level = WifiManager.calculateSignalLevel(
                        result.level,maxLevel);
                String SSID = result.SSID;
                String capabilities = result.capabilities;
                listSSID.add(SSID);
                WifiName wifiName = new WifiName(result.SSID, level);
                listWifiname.add(wifiName);
            }

            int rssi = mWifiManager.getConnectionInfo().getRssi();
           final int level_wifi = WifiManager.calculateSignalLevel(rssi, 5);
           // Log.e("Level of current cnect ", mWifiManager.getConnectionInfo().toString() + " Level is " + level_wifi + " out of 5");
//            DetectNet.tvWificonnected.setText(mWifiManager.getConnectionInfo().getSSID() + "");
//            DetectNet.tvIP.setText("Strength signal: " + mWifiManager.getConnectionInfo().getRssi() + "dbm");

           /* if(level_wifi==5)
                DetectNet.imgWifi.setImageResource(R.mipmap.excellent);
            else */
        /*    if(level_wifi==4){
                DetectNet.imgWifi.setImageResource(R.mipmap.wifi_ex);
            }
            else if(level_wifi==3){
                DetectNet.imgWifi.setImageResource(R.mipmap.wifi_good);
            }
            else if(level_wifi==2){
                DetectNet.imgWifi.setImageResource(R.mipmap.wifi_fair);
            }
            else if(level_wifi==1) {
                DetectNet.imgWifi.setImageResource(R.mipmap.weekness);
            }else
                    DetectNet.imgWifi.setImageResource(R.mipmap.nowifi);

            if(listSSID==null){
               DetectNet.tvScanwifi.setText("Scan wifi: WifiConnection not available");
            }else{
            String message = "Scanning complete. " + listSSID.size()
                        + " connections found!";
                DetectNet.tvScanwifi.setText("Scan wifi: " + message);
            }*/
              /*Excellent >-50 dBm
                Good -50 to -60 dBm
                Fair -60 to -70 dBm
                Weak < -70 dBm*/
            Thread th = new Thread(){
                @Override
                public void run() {
                    super.run();
                    // ko thay list mang xung quanh thi disable
                    if(listSSID==null){
                        mWifiManager.setWifiEnabled(false);

                    }/*else if(!Connectivity.isConnectedWifi(c)){
                        mHandler.postDelayed(mRunnable, 2*60*1000);
                    }*/
                    else if(Connectivity.isConnectedWifi(c)) { // co ket noi
                       /* if (Connectivity.CheckhasInternet() == false) { // co ket noi nhung ko co internet
                            mWifiManager.disconnect();
                            Log.e("no has Internet", "=> disable");
                        }
                        else*/
                            if(level_wifi<1) {  //cuong do yeu{
                                Api.any_wifi=true;
                                Api.applyIptablesRules(c,Api.list_3G,Api.list_Wifi,false,Api.MODE_ON);
                                Log.e("Wifi", " signal strength weakness");
                            } else {
                                if(Api.MODE_CUR){
                                    Api.cleanIptablesRules(c);
                                }
                            }
                    }
                    /*else{
                        mWifiManager.setWifiEnabled(true);
                        // Log.e("Wifi", "=> Enable");
                        if(!Connectivity.isConnectedWifi(c)) {
                            mHandler.postDelayed(mRunnable, 2 * 60 * 1000);
                        }
                    }*/

                }
            };
            th.start();

            Adapter adapter = new Adapter(listWifiname, c);
//            DetectNet.lv.setAdapter(adapter);

        }else{
            DetectNet.tvScanwifi.setText("Wifi is disable!");
        }
    }

    private Runnable mRunnable= new Runnable() {
        @Override
        public void run() {
            mWifiManager.setWifiEnabled(false);
            mHandler.postDelayed(mRunnable, 2*60*1000);
            Log.e("Wifi: ", "is Disable");
            mHandler.removeCallbacks(mRunnable);
        }
    };


private class Adapter extends BaseAdapter{
    List<WifiName> wifiTask;
    Context mContext;

    public Adapter(List<WifiName> wifiTask, Context mContext) {
        this.wifiTask = wifiTask;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return wifiTask.size();
    }

    @Override
    public Object getItem(int position) {
        return wifiTask.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final WifiName item = wifiTask.get(position);
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_listview, null);
           txt = (TextView) convertView.findViewById(R.id.tv_wifiName);
           img = (ImageView) convertView.findViewById(R.id.imgView);
            txt.setText(item.getWifiname());
            if(item.getLevel()==4){
                img.setImageResource(R.mipmap.wifi_ex);
            }else if(item.getLevel()==3){
                img.setImageResource(R.mipmap.wifi_good);
            }else if(item.getLevel()==2){
                img.setImageResource(R.mipmap.wifi_fair);
            }else
                img.setImageResource(R.mipmap.wifi_weak);
        }
        return convertView;
    }
}

}
