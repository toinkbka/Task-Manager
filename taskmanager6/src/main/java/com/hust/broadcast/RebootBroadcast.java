/*
package com.hust.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.hust.common.ProcessUtils;
import com.hust.service.ServiceKillApp;

public class RebootBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//Toast.makeText(context, "Boot complete", Toast.LENGTH_LONG).show();
		ProcessUtils.getInstance(context).SetAlarmStartService();
	}

}
*/
