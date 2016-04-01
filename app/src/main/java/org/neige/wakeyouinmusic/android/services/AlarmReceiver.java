package org.neige.wakeyouinmusic.android.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.neige.wakeyouinmusic.android.AlarmHelper;
import org.neige.wakeyouinmusic.android.models.Alarm;

public class AlarmReceiver extends WakefulBroadcastReceiver {

	private static final String TAG = "AlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Alarm alarm = (Alarm) ((Bundle) intent.getExtras().getParcelable(AlarmHelper.EXTRA_BUNDLE)).getSerializable(AlarmHelper.EXTRA_ALARM);
		Log.i(TAG, "Alarm start. id : " + alarm.getId());

		Intent i = new Intent(context, AlarmService.class);
		i.setAction(AlarmService.START_ACTION);
		i.putExtra("alarm",alarm);
		startWakefulService(context, i);
	}
}