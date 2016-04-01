package org.neige.wakeyouinmusic.android.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.neige.wakeyouinmusic.android.AlarmHelper;
import org.neige.wakeyouinmusic.android.database.DataBaseHelper;
import org.neige.wakeyouinmusic.android.models.Alarm;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

	private static int DAY_IN_MILLIS = (24 * 60 * 60 * 1000);

	@Override
	public void onReceive(Context context, Intent intent) {
		DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
		List<Alarm> alarms = dataBaseHelper.selectAlarms();
		dataBaseHelper.close();

		for (Alarm alarm : alarms) {
			if (alarm.isEnable()) {
				if (alarm.getDayOfWeeks().size() > 0 || AlarmHelper.getInstance().getNextCalendar(alarm).getTimeInMillis() - alarm.getLastModified().getTimeInMillis() < DAY_IN_MILLIS) {
					AlarmHelper.getInstance().registerAlarm(alarm);
				}else {
					alarm.setEnable(false);
					AlarmHelper.getInstance().setAlarm(alarm);
				}
			}
		}
	}
}