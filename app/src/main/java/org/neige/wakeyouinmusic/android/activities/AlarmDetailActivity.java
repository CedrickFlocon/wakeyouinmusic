package org.neige.wakeyouinmusic.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.neige.wakeyouinmusic.android.AlarmHelper;
import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.dialogs.RingtoneDialogFragment;
import org.neige.wakeyouinmusic.android.fragments.AlarmDetailFragment;
import org.neige.wakeyouinmusic.android.models.Alarm;

public class AlarmDetailActivity extends ActionBarActivity {

	public static String PARAMETER_ALARM = "alarm";
	public static String FRAGMENT_ALARM_DETAIL_TAG = "AlarmDetailFragment";

	private Alarm alarm;

	public static Intent newIntent(Context context, Alarm alarm) {
		Intent i = new Intent(context, AlarmDetailActivity.class);
		if (alarm != null) {
			i.putExtra(PARAMETER_ALARM, alarm);
		}
		return i;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_detail);

		if (savedInstanceState == null) {
			alarm = (Alarm) getIntent().getExtras().getSerializable(PARAMETER_ALARM);
			getFragmentManager().beginTransaction().add(R.id.fragment, AlarmDetailFragment.newInstance(alarm), FRAGMENT_ALARM_DETAIL_TAG).commit();
		} else {
			alarm = (Alarm) savedInstanceState.getSerializable(PARAMETER_ALARM);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_delete, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(PARAMETER_ALARM, getAlarmDetailFragment().getAlarm());
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		save();
		super.onBackPressed();
		overridePendingTransition(R.anim.activity_transition_static, R.anim.activity_transition_to_bottom);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				save();
				finish();
				overridePendingTransition(R.anim.activity_transition_static, R.anim.activity_transition_to_bottom);
				break;

			case R.id.deleteMenu:
				AlarmHelper.getInstance().deleteAlarm(alarm);
				finish();
				overridePendingTransition(R.anim.activity_transition_static, R.anim.activity_transition_fade_out);
				break;

			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void save(){
		Intent intent = this.getIntent();
		intent.putExtra(PARAMETER_ALARM, getAlarmDetailFragment().getAlarm());
		this.setResult(RESULT_OK, intent);
	}

	private AlarmDetailFragment getAlarmDetailFragment(){
		return (AlarmDetailFragment) getFragmentManager().findFragmentByTag(FRAGMENT_ALARM_DETAIL_TAG);
	}
}
