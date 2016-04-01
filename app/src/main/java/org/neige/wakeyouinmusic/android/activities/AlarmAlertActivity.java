package org.neige.wakeyouinmusic.android.activities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.fragments.AlarmAlertFragment;
import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.players.PlayerException;
import org.neige.wakeyouinmusic.android.players.Track;
import org.neige.wakeyouinmusic.android.services.AlarmService;

public class AlarmAlertActivity extends ActionBarActivity
		implements AlarmAlertFragment.OnFragmentInteractionListener, AlarmService.UpdateInterfaceListener {

	private static String PARAMETER_ALARM = "alarm";

	private AlarmService alarmService = null;
	private Alarm alarm;
	private AlarmAlertFragment alarmAlertFragment;
	private boolean serviceBound = false;
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			alarmService = ((AlarmService.AlarmBinder) service).getService();
			alarmService.setUpdateInterfaceListener(AlarmAlertActivity.this);
			serviceBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			alarmService = null;
			serviceBound = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//If service is not running no need to display this activity
		if (!isServiceRunning(AlarmService.class)) {
			Intent intent = new Intent(this, AlarmListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return;
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.activity_alarm_alert);

		alarm = (Alarm) getIntent().getExtras().getSerializable(PARAMETER_ALARM);

		alarmAlertFragment = AlarmAlertFragment.newInstance(alarm);
		getFragmentManager().beginTransaction().replace(R.id.fragment, alarmAlertFragment).commit();
	}

	@Override
	protected void onStart() {
		super.onStart();

		bindService(new Intent(this, AlarmService.class), serviceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();

		if(serviceBound){
			alarmService.setUpdateInterfaceListener(null);
			unbindService(serviceConnection);
			serviceBound = false;
		}
	}

	@Override
	public void onNext() {
		if (alarmService != null)
			alarmService.next();
	}

	@Override
	public void onPrevious() {
		if (alarmService != null)
			alarmService.previous();
	}

	@Override
	public void onDismiss() {
		if (alarmService != null)
			alarmService.dismiss();
	}

	@Override
	public void onSnooze() {
		if (alarmService != null)
			alarmService.snooze();
	}

	@Override
	public void onTrackChange(Track track) {
		alarmAlertFragment.updateTrackInformation(track);
	}

	@Override
	public void onError(PlayerException e) {
		alarmAlertFragment.updateErrorInformation(e);
	}

	@Override
	public void onServiceStop() {
		finish();
	}

	@Override
	public void changeSteamType(int streamType) {
		setVolumeControlStream(streamType);
	}

	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}