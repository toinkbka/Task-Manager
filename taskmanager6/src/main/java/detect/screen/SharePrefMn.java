package detect.screen;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Lai Dong on 3/30/2016.
 */
public class SharePrefMn {
    public static String SHARE_NAME = "setting_data";
    public static String KEY_SET_ALARM = "isSetAlarm";

    static SharePrefMn instance;

    SharedPreferences share;

    Context mContext;

    public SharePrefMn(Context mContext) {
        this.mContext = mContext;
        share = mContext
                .getSharedPreferences(SHARE_NAME, mContext.MODE_MULTI_PROCESS);
    }

    public static SharePrefMn getInstance(Context mContext) {
        if (instance == null) {
            instance = new SharePrefMn(mContext);
        }

        return instance;
    }
    // dk SharedPreferences de setAlarm
    public void setAlarm(boolean isSetAlarm) {
        SharedPreferences.Editor edit = share.edit();
        edit.putBoolean(KEY_SET_ALARM, isSetAlarm);
        edit.commit();

    }

    public boolean isSetAlarm() {
        return share.getBoolean(KEY_SET_ALARM, false);
    }

}
