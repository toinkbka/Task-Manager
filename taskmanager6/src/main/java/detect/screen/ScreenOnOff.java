package detect.screen;

import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by Lai Dong on 3/18/2016.
 */
public class ScreenOnOff {
    Context mContext;
    static ScreenOnOff instance;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    public ScreenOnOff(Context context){
        this.mContext = context;
    }

    public static ScreenOnOff getInstance(Context mContext){
        if(instance==null){
            instance = new ScreenOnOff(mContext);
        }
        return instance;
    }
    public void turnOnScreen(){
        Toast.makeText(mContext, "Screen On", Toast.LENGTH_SHORT).show();
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        mWakeLock.acquire();
    }

    public void turnOffScreen(){
        Toast.makeText(mContext, "Screen OFF", Toast.LENGTH_SHORT).show();
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "TAG");
        mWakeLock.acquire();
    }

    public void ScreenTimeout(String timeString){
        int time=Integer.parseInt(timeString);
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time);

    }
}
