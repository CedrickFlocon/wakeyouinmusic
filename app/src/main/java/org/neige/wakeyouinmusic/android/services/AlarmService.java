package org.neige.wakeyouinmusic.android.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.neige.wakeyouinmusic.android.AlarmHelper;
import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.WakeYouInMusicApplication;
import org.neige.wakeyouinmusic.android.activities.AlarmAlertActivity;
import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.models.DeezerRingtone;
import org.neige.wakeyouinmusic.android.models.DefaultRingtone;
import org.neige.wakeyouinmusic.android.models.SpotifyRingtone;
import org.neige.wakeyouinmusic.android.players.PlayerException;
import org.neige.wakeyouinmusic.android.players.PlayerListener;
import org.neige.wakeyouinmusic.android.players.PlaylistPlayer;
import org.neige.wakeyouinmusic.android.players.Track;

public class AlarmService extends Service implements PlayerListener {

	private static final long[] VIBRATOR_PATTERN = {0, 200, 500};

	public static final int NOTIFICATION_ID = 1;
	public static final String START_ACTION = "start";
	public static final String NEXT_ACTION = "next";
	public static final String PREVIOUS_ACTION = "previous";
	public static final String DISMISS_ACTION = "dismiss";
	public static final String SNOOZE_ACTION = "snooze";
	public static final String DISPLAY_UI_ACTION = "displayUi";
	private static final String TAG = "AlarmService";

	private PlaylistPlayer errorPlayer, playlistPlayer;
	private Alarm alarm;
	private AlarmBinder alarmBinder = new AlarmBinder();
	private UpdateInterfaceListener updateInterfaceListener;
	private AlarmNotification alarmNotification;
	private PlayerException playerException = null;
	private Intent intent;

    @Override
	public void onCreate() {
		super.onCreate();
		try {
			errorPlayer = PlaylistPlayer.getPlaylistPlayer((WakeYouInMusicApplication) getApplication(), AlarmHelper.getInstance().getErrorRingtone());
		} catch (PlayerException e) {
			Log.e(TAG, "Should never happen", e);
			throw new RuntimeException(e);
		}

		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if(mgr != null) {
			mgr.listen(new PhoneStateListener(){
				private boolean paused = false;
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
					if (getCurrentPlayer() == null) return;

					switch(state){
						case TelephonyManager.CALL_STATE_RINGING:
							getCurrentPlayer().pause();
							paused = true;
							break;
						case TelephonyManager.CALL_STATE_IDLE:
							if (paused){
								getCurrentPlayer().resume();
							}
							paused = false;
							break;
						case TelephonyManager.CALL_STATE_OFFHOOK:
							paused = true;
							getCurrentPlayer().pause();
							break;
					}
					super.onCallStateChanged(state, incomingNumber);
				}
			}, PhoneStateListener.LISTEN_CALL_STATE);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start command : " + intent.getAction());
        this.intent = intent;

		if (!intent.getAction().equals(START_ACTION) && alarm == null){ // Prevent multi click on notification
            stopService();
            return START_REDELIVER_INTENT;
        }

		switch (intent.getAction()) {
			case START_ACTION:
				registerNextAlarm((Alarm) intent.getExtras().getSerializable(AlarmHelper.EXTRA_ALARM));
				if (playlistPlayer != null){
					Log.i(TAG, "Not allowed to start two alarm. Dismiss second one");
				}else {
					alarm = (Alarm) intent.getExtras().getSerializable(AlarmHelper.EXTRA_ALARM);
					alarmNotification = new AlarmNotification(getApplication(), alarm);
					play();
				}
				break;
			case NEXT_ACTION:
				next();
				break;
			case PREVIOUS_ACTION:
				previous();
				break;
			case DISMISS_ACTION:
				dismiss();
				break;
			case SNOOZE_ACTION:
				snooze();
				break;
			case DISPLAY_UI_ACTION:
				displayUi();
				break;
		}

		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return alarmBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	private void play() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
			if (!((PowerManager) getSystemService(POWER_SERVICE)).isInteractive()) {
				displayUi();
			}
		} else {
			if (!((PowerManager) getSystemService(POWER_SERVICE)).isScreenOn()) {
				displayUi();
			}
		}

		try {
			playlistPlayer = PlaylistPlayer.getPlaylistPlayer((WakeYouInMusicApplication) getApplication(), alarm.getRingtone());
		} catch (PlayerException e) {
			onError(e);
			return;
		}
		startForeground();
		playlistPlayer.setPlayerListener(this);
		playlistPlayer.play();

		if (alarm.isVibrate()) {
			((Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE)).vibrate(VIBRATOR_PATTERN, 0);
		}
	}

	public void previous() {
		playlistPlayer.previous();
	}

	public void next() {
		playlistPlayer.next();
	}

	private void registerNextAlarm(Alarm currentAlarm){
		if (currentAlarm.getDayOfWeeks().size() > 0) {
			AlarmHelper.getInstance().registerAlarm(currentAlarm);
		} else {
			currentAlarm.setEnable(false);
			AlarmHelper.getInstance().setAlarm(currentAlarm);
		}
	}

	public void dismiss(){
		Toast.makeText(getApplicationContext(),getResources().getString(R.string.toast_dismiss) , Toast.LENGTH_LONG).show();
		stop();
	}

	public void snooze() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		int snoozeDurationInSecond = Integer.parseInt(sharedPrefs.getString(WakeYouInMusicApplication.PREFERENCE_SNOOZE_DURATION_KEY, WakeYouInMusicApplication.PREFERENCE_SNOOZE_DURATION_DEFAULT_VALUE));

		Toast.makeText(getApplicationContext(),getResources().getQuantityString(R.plurals.toast_snooze, snoozeDurationInSecond / 60, snoozeDurationInSecond/60), Toast.LENGTH_LONG).show();
		AlarmHelper.getInstance().snoozeAlarm(alarm, snoozeDurationInSecond);

		stop();
	}

	private void stop() {
		if (playlistPlayer != null){
			playlistPlayer.setPlayerListener(null);
			playlistPlayer.stop();
			playlistPlayer = null;
		}

		errorPlayer.stop();
		errorPlayer = null;

		if (updateInterfaceListener != null) {
			updateInterfaceListener.onServiceStop();
		}
		if (alarm.isVibrate()) {
			((Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE)).cancel();
		}
		stopService();
	}

	private void stopService() {
        alarm = null;
		stopForeground(true);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
		stopSelf();
		AlarmReceiver.completeWakefulIntent(intent);
	}

	private void displayUi() {
		Intent intent = new Intent(getApplicationContext(), AlarmAlertActivity.class);
		intent.putExtra(AlarmHelper.getInstance().EXTRA_ALARM, alarm);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void startForeground() {
		if (alarm.getRingtone() instanceof DeezerRingtone ||
			alarm.getRingtone() instanceof SpotifyRingtone) {
			alarmNotification.updateDefaultView(alarm, playlistPlayer.getCurrentTrackInformation());
		} else if (alarm.getRingtone() instanceof DefaultRingtone) {
			alarmNotification.updateDefaultView(alarm, playlistPlayer.getCurrentTrackInformation());
		} else {
			throw new UnsupportedOperationException("Unknown ringtone type");
		}

		startForeground(NOTIFICATION_ID, alarmNotification.getNotification());
	}

	private void updateNotification() {
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, alarmNotification.getNotification());
	}

	public void setUpdateInterfaceListener(UpdateInterfaceListener updateInterfaceListener) {
		this.updateInterfaceListener = updateInterfaceListener;
		if (updateInterfaceListener == null) {
			return;
		}
		if (playerException == null) {
			updateInterfaceListener.onTrackChange(playlistPlayer.getCurrentTrackInformation());
			updateInterfaceListener.changeSteamType(playlistPlayer.getStreamType());
		} else {
			updateInterfaceListener.onError(playerException);
			updateInterfaceListener.changeSteamType(errorPlayer.getStreamType());
		}
	}

	private PlaylistPlayer getCurrentPlayer(){
		if (playerException != null){
			return errorPlayer;
		}
		return playlistPlayer;
	}

	@Override
	public void onTrackChange() {
		if (updateInterfaceListener != null) {
			updateInterfaceListener.onTrackChange(playlistPlayer.getCurrentTrackInformation());
		}
		alarmNotification.updatePlaylistView(playlistPlayer.getCurrentTrackInformation(), NOTIFICATION_ID);
		updateNotification();
	}

	@Override
	public void onError(PlayerException e) {
		Log.e(TAG, "Player Error", e);
		playerException = e;

		//Play error player
		if (playlistPlayer != null){
			playlistPlayer.setPlayerListener(null);
			playlistPlayer.stop();
		}
		errorPlayer.play();

		//Update notification
		alarmNotification.updateErrorView(e.getMessage(), errorPlayer.getCurrentTrackInformation());
		updateNotification();

		//Update ui
		if (updateInterfaceListener != null) {
			new Handler(getApplication().getMainLooper()).post(() ->
					updateInterfaceListener.onError(e));
					updateInterfaceListener.changeSteamType(errorPlayer.getStreamType());
		}
	}

	public interface UpdateInterfaceListener {
		public void onTrackChange(Track track);

		public void onError(PlayerException e);

		public void onServiceStop();

		public void changeSteamType(int streamType);
	}

	public class AlarmBinder extends Binder {

		public AlarmService getService() {
			return AlarmService.this;
		}

	}

}