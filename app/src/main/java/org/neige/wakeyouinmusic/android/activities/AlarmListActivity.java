package org.neige.wakeyouinmusic.android.activities;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.neige.wakeyouinmusic.android.AlarmHelper;
import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.database.DataBaseHelper;
import org.neige.wakeyouinmusic.android.fragments.AlarmListFragment;
import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.models.DayOfWeek;
import org.neige.wakeyouinmusic.android.models.DefaultRingtone;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;

public class AlarmListActivity extends ActionBarActivity implements AlarmListFragment.OnFragmentInteractionListener {

	private static final String TAG = "MainActivity";
	public static String FRAGMENT_ALARM_LIST_TAG = "AlarmListFragment";

	private static final int ACTIVITY_RESULT_ALARM_DETAIL = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_list);

		if (savedInstanceState == null){
			getFragmentManager().beginTransaction().replace(R.id.fragment, AlarmListFragment.newInstance(), TAG).commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
		List<Alarm> alarms = dataBaseHelper.selectAlarms();
		dataBaseHelper.close();
		((AlarmListFragment)getFragmentManager().findFragmentByTag(TAG)).setAlarmList(alarms);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settingMenu:
				startActivity(new Intent(this, SettingActivity.class));
				overridePendingTransition(R.anim.activity_transition_from_right, R.anim.activity_transition_to_left);
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode){
			case ACTIVITY_RESULT_ALARM_DETAIL:
				if (resultCode == RESULT_OK){
					AlarmHelper.getInstance().setAlarm((Alarm) data.getSerializableExtra(AlarmDetailActivity.PARAMETER_ALARM));
				}
				break;
		}
	}

	@Override
	public void onClickAlarm(Alarm alarm) {
		startActivityForResult(AlarmDetailActivity.newIntent(this, alarm), ACTIVITY_RESULT_ALARM_DETAIL);
		overridePendingTransition(R.anim.activity_transition_from_bottom, R.anim.activity_transition_static);
	}

	@Override
	public void onClickNewAlarm() {
		startActivityForResult(AlarmDetailActivity.newIntent(this, getDefaultAlarm()), ACTIVITY_RESULT_ALARM_DETAIL);
		overridePendingTransition(R.anim.activity_transition_from_bottom, R.anim.activity_transition_static);
	}

	private Alarm getDefaultAlarm() {
		//Default ringtone
		String ringtoneUri = AlarmHelper.getInstance().getDefaultRingtoneUri();
		android.media.Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(ringtoneUri));
		DefaultRingtone alarmRingtone = new DefaultRingtone();
		alarmRingtone.setTitle(ringtone == null ? getString(R.string.ringtone_unnamed) : ringtone.getTitle(this));
		alarmRingtone.setUri(ringtoneUri);

		//Default time
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		Alarm alarm = new Alarm();
		alarm.setRingtone(alarmRingtone);
		alarm.setLabel("");
		alarm.setTime(calendar);
		alarm.setEnable(true);
		alarm.setVibrate(false);
		alarm.setDayOfWeeks(EnumSet.noneOf(DayOfWeek.class));

		return alarm;
	}

}