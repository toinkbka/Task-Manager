package detect.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import java.util.Timer;
import java.util.TimerTask;

import detect.contextuser.ContextAware;
import detect.netmanager.Connectivity;
import detect.screen.ScreenReceiver;
import firewall.Api;

/**
 * Created by Lai Dong on 3/28/2016.
 */
public class UpdateService extends Service{
    private Handler mHandler = new Handler();
    TelephonyManager telephonyManager;
    MyPhoneStateListener psListener;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
      //  Toast.makeText(getApplicationContext(),"Service onCreate ", Toast.LENGTH_SHORT).show();
        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      //  Toast.makeText(getApplicationContext(), "onstart service",Toast.LENGTH_SHORT).show();
        if(intent!=null) {
            boolean screenOn = intent.getBooleanExtra("screen_state", false);// true neu no ko get dc thi mac dinh la true
            ConnectivityManager manager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
            if (!screenOn) { //screen on
                Log.e("Detect Screen: ", "On");
               /* ContentResolver.setMasterSyncAutomatically(true);       // Sync Enable
                mHandler.postDelayed(mRunnable_screenOn, 2 * 60 * 1000);*/
                //   if (is3g) {
                psListener = new MyPhoneStateListener();
                telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                //    }

            } else {
                // screen off
                // alarm Sync
                Log.e("Detect Screen: ", "Off");
                mHandler.postDelayed(mRunnable_screenOff, 2*60*1000);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private final Runnable mRunnable_screenOn = new Runnable() {
        @Override
        public void run() {
            ContentResolver.setMasterSyncAutomatically(false);
            mHandler.postDelayed(mRunnable_screenOn, 30*1000);
            Log.e("Detect Sync: ", "Sync false");
            mHandler.removeCallbacks(mRunnable_screenOn);
        }
    };

    private final Runnable mRunnable_screenOff = new Runnable() {
        @Override
        public void run() {
         //   ContentResolver.setMasterSyncAutomatically(false);  // Disable Sync
            Api.any_wifi=true;
            Api.applyIptablesRules(getApplicationContext(), Api.list_3G, Api.list_Wifi, false, Api.MODE_ON);
            mHandler.postDelayed(mRunnable_screenOff, 2 * 60 * 1000);
        //    Log.e("Detect Sync: ", "Sync false");
            Log.e("FireWall: ", "is enabled");
            mHandler.removeCallbacks(mRunnable_screenOff);
        }
    };

    public class MyPhoneStateListener extends PhoneStateListener {
        public int signalStrengthValue;
        int signal;
        String updateMsg;

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
           /* if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99)
                    signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    signalStrengthValue = signalStrength.getGsmSignalStrength();
            } else {
                signalStrengthValue = signalStrength.getCdmaDbm();
            }
*/
            if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                updateMsg = "cdma dBM=" + signalStrength.getCdmaDbm() +" asu";
                signal = signalStrength.getCdmaDbm();
            } else if  (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                updateMsg = "gsm signal=" + signalStrength.getGsmSignalStrength() + " asu";
                signal = signalStrength.getGsmSignalStrength();
            }

            if(signal<10){
                Api.any_wifi=true;
                Api.applyIptablesRules(getApplicationContext(), Api.list_3G, Api.list_Wifi, false, Api.MODE_ON);
                Log.e("FireWall: ", "is enabled");
            }else{
                if(Api.MODE_CUR){
                    Api.cleanIptablesRules(getApplicationContext());
                    Log.e("FireWall", "is disable");
                }
            }
        }
    }
}
