package org.neige.wakeyouinmusic.android.players;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.preference.PreferenceManager;

import org.neige.wakeyouinmusic.android.WakeYouInMusicApplication;
import org.neige.wakeyouinmusic.android.models.DeezerRingtone;
import org.neige.wakeyouinmusic.android.models.DefaultRingtone;
import org.neige.wakeyouinmusic.android.models.Ringtone;
import org.neige.wakeyouinmusic.android.models.SpotifyRingtone;

public abstract class PlaylistPlayer {

	private int FADE_VOLUME_PITCH = 100;

	protected PlayerListener playerListener;
	protected int oldVolume , alarmVolume, currentVolume;
	protected int crescendoDuration;
	protected float volumePercentage;
	protected AudioManager audioManager;
	protected Context context;

	protected int progressiveVolume = 0;
	private Handler handler = new Handler();
	private boolean isStop = false;
	private final Runnable runnable = new Runnable() {
		public void run() {
			if (!isStop && (currentVolume == 0 || currentVolume == audioManager.getStreamVolume(getStreamType()))){
				audioManager.setStreamVolume(getStreamType(), alarmVolume * (++progressiveVolume) / 100, 0);
				currentVolume = audioManager.getStreamVolume(getStreamType());
				if (progressiveVolume < 100){
					handler.postDelayed(this, crescendoDuration * 1000 / FADE_VOLUME_PITCH);
				}
			}
		}
	};

	protected PlaylistPlayer(Context context) {
		this.context = context;
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		volumePercentage = (float)PreferenceManager.getDefaultSharedPreferences(context).getInt(WakeYouInMusicApplication.PREFERENCE_VOLUME_KEY, WakeYouInMusicApplication.PREFERENCE_VOLUME_DEFAULT_VALUE) / 100;
		crescendoDuration = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(WakeYouInMusicApplication.PREFERENCE_CRESCENDO_DURATION_KEY, WakeYouInMusicApplication.PREFERENCE_CRESCENDO_DURATION_DEFAULT_VALUE));
		oldVolume = audioManager.getStreamVolume(getStreamType());
		alarmVolume = Math.round(audioManager.getStreamMaxVolume(getStreamType()) * volumePercentage);
	}

	public static PlaylistPlayer getPlaylistPlayer(WakeYouInMusicApplication application, Ringtone ringtone) throws PlayerException {
		if (ringtone instanceof DeezerRingtone) {
			return new DeezerPlayer(application, (DeezerRingtone) ringtone);
		} else if (ringtone instanceof SpotifyRingtone) {
			return new SpotifyPlayer(application, (SpotifyRingtone) ringtone);
		}else if (ringtone instanceof DefaultRingtone) {
			return new DefaultPlayer(application, (DefaultRingtone) ringtone);
		} else {
			throw new UnsupportedOperationException("Unknown ringtone type");
		}
	}

	public void setPlayerListener(PlayerListener playerListener) {
		this.playerListener = playerListener;
	}

	public void play() {
		handler.post(runnable);
	}

	public abstract void pause();

	public abstract void resume();

	public abstract void next();

	public abstract void previous();

	public void stop() {
		isStop = true;
		audioManager.setStreamVolume(getStreamType(), oldVolume, 0);
	}

	public abstract int getStreamType();

	public abstract Track getCurrentTrackInformation();

}
