package detect.netmanager;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.hust.activity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lai Dong on 3/28/2016.
 */
public class DetectNet extends Fragment {
    WifiManager mWifiManager;
    static TextView tvScanwifi;
    static ListView lv;
    static TextView tvWificonnected, tvIP;
    static ImageView imgWifi;
    WifiReceiver mWifiReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_detectnet, container, false);

        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        tvScanwifi = (TextView) view.findViewById(R.id.tv_scan);
        tvWificonnected = (TextView) view.findViewById(R.id.tv_namewifi);
        tvIP = (TextView) view.findViewById(R.id.tv_signal);
        lv = (ListView) view.findViewById(R.id.listView);
        imgWifi = (ImageView) view.findViewById(R.id.imgWifi);

        mWifiReceiver = new WifiReceiver(mWifiManager);
        IntentFilter mIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        getContext().registerReceiver(mWifiReceiver, mIntentFilter);
        mWifiManager.startScan();

        return view;
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mWifiReceiver);
        super.onDestroy();
    }

  /*  @Override
    public void onStop() {
        getContext().unregisterReceiver(mWifiReceiver);
        super.onStop();
    }*/
}
