package org.neige.wakeyouinmusic.android.players;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.models.DefaultRingtone;

import java.io.IOException;

public class DefaultPlayer extends PlaylistPlayer {

	private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
	private static final String TAG = "DefaultPlayer";

	private DefaultRingtone ringtone;
	private MediaPlayer mediaPlayer =new MediaPlayer();

	public DefaultPlayer(Context context, DefaultRingtone ringtone) throws PlayerException {
		super(context);
		this.context = context;
		this.ringtone = ringtone;

		try {
			mediaPlayer.setDataSource(context, Uri.parse(ringtone.getUri()));
		} catch (IOException e) {
			Log.e(TAG, "Unable to parse Uri : " + ringtone.getUri(), e);
			if (ringtone.isErrorRingtone()){
				try {
					ringtone.setTitle(context.getString(R.string.alarm_alert_emergency_ringtone));
					mediaPlayer.setDataSource(context,Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.default_ringtone));
				}catch (IOException e1){
					throw new PlayerException(context.getString(R.string.player_error_ringtone_not_found),e);
				}
			}else {
				throw new PlayerException(context.getString(R.string.player_error_ringtone_not_found),e);
			}
		}

		mediaPlayer.setAudioStreamType(getStreamType());
		mediaPlayer.setLooping(true);

		try {
			mediaPlayer.prepare();
		} catch (IOException e) {
			throw new PlayerException(context.getString(R.string.player_error_unknown),e);
		}
	}

	@Override
	public void play() {
		super.play();
		mediaPlayer.start();
	}

	@Override
	public void pause() {
		mediaPlayer.pause();
	}

	@Override
	public void resume() {
		mediaPlayer.start();
	}

	@Override
	public void next() { }

	@Override
	public void previous() { }

	@Override
	public void stop() {
		mediaPlayer.stop();
		mediaPlayer.release();
		super.stop();
	}

	@Override
	public int getStreamType() {
		return STREAM_TYPE;
	}

	@Override
	public Track getCurrentTrackInformation() {
		Track track = new Track();
		track.setName(ringtone.getTitle());
		return track;
	}

}
