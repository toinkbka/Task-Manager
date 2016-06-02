package detect.screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import detect.service.UpdateService;

/**
 * Created by Lai Dong on 3/28/2016.
 */
public class ScreenReceiver extends BroadcastReceiver {
    private static boolean screenOff;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
        }
        Intent i = new Intent(context, UpdateService.class);
        i.putExtra("screen_state", screenOff);
        context.startService(i);

    }
}
