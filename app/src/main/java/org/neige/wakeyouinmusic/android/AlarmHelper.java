package org.neige.wakeyouinmusic.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.neige.wakeyouinmusic.android.database.DataBaseHelper;
import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.models.DayOfWeek;
import org.neige.wakeyouinmusic.android.models.DefaultRingtone;
import org.neige.wakeyouinmusic.android.services.AlarmReceiver;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class AlarmHelper {

	public static final String EXTRA_ALARM = "alarm";
	public static final String EXTRA_BUNDLE = "bundle";
	private static final String PERIOD_SEPARATOR = ", ";
	private static final String TAG = "AlarmHelper";
	private String DEFAULT_RINGTONE_URI = Settings.System.CONTENT_URI + "/" + Settings.System.ALARM_ALERT;

	private static AlarmHelper instance;
	private final Context context;
	private android.app.AlarmManager alarmManager;

	private AlarmHelper(Context context) {
		this.context = context;
		alarmManager = (android.app.AlarmManager) context.getSystemService(context.ALARM_SERVICE);
	}

	public static AlarmHelper getInstance() {
		if (instance == null) {
			throw new UnsupportedOperationException("You need to call init first");
		}
		return instance;
	}

	public static void init(Context context) {
		instance = new AlarmHelper(context);
	}

	private void insertAlarm(Alarm alarm) {
		DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
		dataBaseHelper.insertAlarm(alarm);
		dataBaseHelper.close();
	}

	private void updateAlarm(Alarm alarm) {
		DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
		dataBaseHelper.updateAlarm(alarm);
		dataBaseHelper.close();

	}

	public void setAlarm(Alarm alarm) {
		alarm.setLastModified(Calendar.getInstance());

		if (alarm.getId() > 0) {
			updateAlarm(alarm);
		} else {
			insertAlarm(alarm);
		}

		if (alarm.isEnable()) {
			makeToast(alarm);
			registerAlarm(alarm);
		} else {
			cancelAlarm(alarm);
		}
	}

	public void deleteAlarm(Alarm alarm) {
		cancelAlarm(alarm);

		//Delete in database
		DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
		dataBaseHelper.deleteAlarm(alarm);
		dataBaseHelper.close();
	}

	public void registerAlarm(Alarm alarm) {
        registerSystemAlarm(alarm, getNextCalendar(alarm));

	}

	public void snoozeAlarm(Alarm alarm, int snoozeSecondDuration) {
		Calendar snoozeCalendar = Calendar.getInstance();
        snoozeCalendar.add(Calendar.SECOND, snoozeSecondDuration);
        registerSystemAlarm(alarm, snoozeCalendar);
	}

    private void registerSystemAlarm(Alarm alarm, Calendar nextAlarm){
        Bundle bundle = new Bundle();
        Intent intent = new Intent(context, AlarmReceiver.class);
        bundle.putSerializable(EXTRA_ALARM, alarm);
        intent.putExtra(EXTRA_BUNDLE, bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(nextAlarm.getTimeInMillis(), pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), pendingIntent);
        }else {
            alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), pendingIntent);
        }
        Log.i(TAG, "Alarm register. alarm.id : " + alarm.getId() + " at : " + nextAlarm.getTime().toString());
    }

	public void cancelAlarm(Alarm alarm) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) alarm.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.cancel(pendingIntent);
		Log.i(TAG, "Alarm cancel id : " + alarm.getId());
	}

	public Calendar getNextCalendar(Alarm alarm) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		//Hours
		if (calendar.get(Calendar.HOUR_OF_DAY) > alarm.getTime().get(Calendar.HOUR_OF_DAY) ||
				(calendar.get(Calendar.HOUR_OF_DAY) == alarm.getTime().get(Calendar.HOUR_OF_DAY) &&
						(calendar.get(Calendar.MINUTE) >= alarm.getTime().get(Calendar.MINUTE)))) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}

		calendar.set(Calendar.HOUR_OF_DAY, alarm.getTime().get(Calendar.HOUR_OF_DAY));
		calendar.set(Calendar.MINUTE, alarm.getTime().get(Calendar.MINUTE));
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		//Day if repeat
		if (alarm.getDayOfWeeks().size() > 0) {
			int dayToAdd = 0;
			for (int dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1; ; dayIndex = (dayIndex + 1) % 7) {
				if (alarm.getDayOfWeeks().contains(DayOfWeek.values()[dayIndex])) {
					calendar.add(Calendar.DAY_OF_YEAR, dayToAdd);
					break;
				}
				dayToAdd++;
			}
		}

		return calendar;
	}

	public String getPeriodString(Alarm alarm) {
		if (alarm.getDayOfWeeks() == null || alarm.getDayOfWeeks().size() == 0) {
			if (getNextCalendar(alarm).get(Calendar.DAY_OF_WEEK) != Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
				return context.getString(R.string.period_tomorrow);
			} else {
				return context.getString(R.string.period_today);
			}
		} else if (alarm.getDayOfWeeks().size() == 7) {
			return context.getString(R.string.period_every_day);
		} else if (alarm.getDayOfWeeks().size() == 2 && alarm.getDayOfWeeks().contains(DayOfWeek.SUNDAY) &&
				alarm.getDayOfWeeks().contains(DayOfWeek.SATURDAY)) {
			return context.getString(R.string.period_weekend);
		} else if (alarm.getDayOfWeeks().size() == 5 && !alarm.getDayOfWeeks().contains(DayOfWeek.SUNDAY) &&
				!alarm.getDayOfWeeks().contains(DayOfWeek.SATURDAY)) {
			return context.getString(R.string.period_week_day);
		} else {
			DateFormatSymbols symbols = new DateFormatSymbols();
			String[] dayNames = symbols.getShortWeekdays();
			String period = "";
			for (DayOfWeek dayOfWeek : alarm.getDayOfWeeks()) {
				switch (dayOfWeek) {
					case SUNDAY:
						period += dayNames[Calendar.SUNDAY];
						break;
					case MONDAY:
						period += dayNames[Calendar.MONDAY];
						break;
					case TUESDAY:
						period += dayNames[Calendar.TUESDAY];
						break;
					case WEDNESDAY:
						period += dayNames[Calendar.WEDNESDAY];
						break;
					case THURSDAY:
						period += dayNames[Calendar.THURSDAY];
						break;
					case FRIDAY:
						period += dayNames[Calendar.FRIDAY];
						break;
					case SATURDAY:
						period += dayNames[Calendar.SATURDAY];
						break;
				}
				period += PERIOD_SEPARATOR;
			}
			return period.substring(0, period.length() - PERIOD_SEPARATOR.length());
		}
	}

    public DefaultRingtone getErrorRingtone() {
        DefaultRingtone ringtone = new DefaultRingtone();
        ringtone.setTitle(context.getString(R.string.alarm_alert_error_ringtone));
        ringtone.setUri(getDefaultRingtoneUri());
		ringtone.setIsErrorRingtone(true);

        return ringtone;
    }

    public String getDefaultRingtoneUri() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
        String ringtoneUri = sharedPreferences.getString(WakeYouInMusicApplication.PREFERENCE_DEFAULT_RINGTONE_KEY, null);

		if (ringtoneUri != null && !DEFAULT_RINGTONE_URI.equals(ringtoneUri)){
			return ringtoneUri;
		}else if (defaultRingtoneUri != null){
			return defaultRingtoneUri.toString();
		}else {
			RingtoneManager ringtoneManager = new RingtoneManager(context);
			ringtoneManager.setType(RingtoneManager.TYPE_ALARM);
			Cursor alarmsCursor = ringtoneManager.getCursor();
			if (alarmsCursor.getCount() > 0){
				alarmsCursor.moveToFirst();
				ringtoneUri = ringtoneManager.getRingtoneUri(alarmsCursor.getPosition()).toString();
			}else {
				throw new RuntimeException("Unable to find at least one ringtone");
			}
			return ringtoneUri;
		}
    }

	public void makeToast(Alarm alarm) {
		String fromNow;
		long differenceInMillis = getNextCalendar(alarm).getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		int day, hour, minute;
		day = Math.round(differenceInMillis / (24 * 60 * 60 * 1000));
		hour =  Math.round(differenceInMillis / (60 * 60 * 1000));
		minute = Math.round(differenceInMillis / (60 * 1000));

		if (day > 0) {
			hour %= day*24;
			if (hour == 0) {
				fromNow = context.getString(R.string.toast_from_now_small, context.getResources().getQuantityString(R.plurals.day, day,day));
			}else {
				fromNow = context.getString(R.string.toast_from_now_long, context.getResources().getQuantityString(R.plurals.day, day,day), context.getResources().getQuantityString(R.plurals.hour, hour, hour));
			}
		} else {
			if (hour == 0) {
				fromNow = context.getString(R.string.toast_from_now_small, context.getResources().getQuantityString(R.plurals.minute, minute, minute));
			}else {
				minute %= hour*60;
				if (minute == 0){
					fromNow = context.getString(R.string.toast_from_now_small, context.getResources().getQuantityString(R.plurals.hour, hour, hour));
				}else {
					minute %= hour*60;
					fromNow = context.getString(R.string.toast_from_now_long, context.getResources().getQuantityString(R.plurals.hour, hour, hour), context.getResources().getQuantityString(R.plurals.minute, minute, minute));
				}
			}
		}

		Toast.makeText(context, fromNow, Toast.LENGTH_SHORT).show();
	}
}
