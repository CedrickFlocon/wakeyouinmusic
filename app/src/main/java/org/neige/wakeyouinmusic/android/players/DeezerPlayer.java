package org.neige.wakeyouinmusic.android.players;

import android.media.AudioManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.deezer.sdk.model.PaginatedList;
import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.JsonUtils;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.RequestListener;
import com.deezer.sdk.player.PlayerWrapper;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import org.json.JSONException;
import org.json.JSONObject;
import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.WakeYouInMusicApplication;
import org.neige.wakeyouinmusic.android.models.DeezerRingtone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeezerPlayer extends PlaylistPlayer implements PlayerWrapperListener, OnPlayerErrorListener {

	private static final String TAG = "DeezerPlayer";
	private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

	private com.deezer.sdk.player.PlaylistPlayer player;
	private DeezerRingtone deezerRingtone;
	private WakeYouInMusicApplication application;
	private DeezerConnect deezerConnect;
	private List<Integer> randomTrack = new ArrayList<>();
	private int currentTrackIndex = 0;

	public DeezerPlayer(WakeYouInMusicApplication application, DeezerRingtone ringtone) throws PlayerException {
		super(application);
		this.application = application;

		deezerConnect = new DeezerConnect(application.getApplicationContext(), WakeYouInMusicApplication.DEEZER_APPLICATION_ID);
		SessionStore sessionStore = new SessionStore();
		sessionStore.restore(deezerConnect, application.getApplicationContext());
		deezerRingtone = ringtone;
		try {
			player = new com.deezer.sdk.player.PlaylistPlayer(application, deezerConnect, new WifiAndMobileNetworkStateChecker());
		} catch (TooManyPlayersExceptions | DeezerError e) {
			Log.e(TAG, "Deezer PLayer initialization : " + e.getCause(), e);
			Crashlytics.logException(e);
			throw new PlayerException(context.getString(R.string.player_error_deezer_internal));
		}
		player.addPlayerListener(this);
		player.addOnPlayerErrorListener(this);

		player.setRepeatMode(PlayerWrapper.RepeatMode.ALL);
	}

	@Override
	public void play() {
		super.play();
		//Test if the playlist is empty
		deezerConnect.requestAsync(DeezerRequestFactory.requestPlaylistTracks(deezerRingtone.getDeezerId()), new RequestListener() {
			@Override
			public void onComplete(String s, Object o) {
				try {
					PaginatedList tracks = (PaginatedList) JsonUtils.deserializeObject(new JSONObject(s));
					if (tracks.size() == 0) {
						if (playerListener != null) {
							playerListener.onError(new PlayerException(context.getString(R.string.player_error_empty_playlist)));
						}
					} else {
						for (int i = 0; i < tracks.getTotalSize(); i++) {
							randomTrack.add(i);
						}
						if (deezerRingtone.isShuffle()) {
							Collections.shuffle(randomTrack);
						}
						if (player.getPlayerState() != PlayerState.RELEASED) {
							player.playPlaylist(deezerRingtone.getDeezerId(), randomTrack.get(currentTrackIndex));
						}
					}
				} catch (JSONException e) {
					onException(e, o);
				}
			}

			@Override
			public void onException(Exception e, Object o) {
				onRequestException(e, o);
			}
		});
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void resume() {
		player.play();
	}

	@Override
	public void next() {
		if (currentTrackIndex == randomTrack.size() - 1){
			currentTrackIndex = -1;
		}

		if (randomTrack.size() < currentTrackIndex + 1){
			return;
		}

		player.skipToTrack(randomTrack.get(++currentTrackIndex));
	}

	@Override
	public void previous() {
		if (currentTrackIndex == 0){
			currentTrackIndex = randomTrack.size();
		}

		if (currentTrackIndex - 1 < 0){
			return;
		}

		player.skipToTrack(randomTrack.get(--currentTrackIndex));
	}

	@Override
	public void stop() {
		if (player.getPlayerState() != PlayerState.RELEASED) {
			player.stop();
		}
		player.release();
		super.stop();
	}

	@Override
	public int getStreamType() {
		return STREAM_TYPE;
	}

	@Override
	public org.neige.wakeyouinmusic.android.players.Track getCurrentTrackInformation() {
		org.neige.wakeyouinmusic.android.players.Track track = new org.neige.wakeyouinmusic.android.players.Track();
		track.setName(getCurrentTrackTitle());
		track.setArtist(getCurrentTrackArtiste());
		track.setCoverUrl(getCurrentTrackCover());
		return track;
	}

	private Track getCurrentTrack() {
		try {
			return player.getTracks().size() == 0 ? null : player.getCurrentTrack();
		}catch (Exception e){
			Log.e(TAG, "Deezer SDK crash", e);
			return null;
		}
	}

	private String getCurrentTrackTitle() {
		return getCurrentTrack() == null ? "" : player.getCurrentTrack().getTitle();
	}

	private String getCurrentTrackCover() {
		return getCurrentTrack() == null ? "" : player.getCurrentTrack().getAlbum().getCoverUrl();
	}

	private String getCurrentTrackArtiste() {
		return getCurrentTrack() == null ? "" : player.getCurrentTrack().getArtist().getName();
	}

	@Override
	public void onAllTracksEnded() {
		Log.d(TAG, "onAllTracksEnded");
	}

	@Override
	public void onPlayTrack(PlayableEntity playableEntity) {
		if (playerListener != null) {
			playerListener.onTrackChange();
		}
	}

	@Override public void onTrackEnded(PlayableEntity playableEntity) {
		if (currentTrackIndex == randomTrack.size() - 1){
			currentTrackIndex = -1;
		}
		int nextTrack = randomTrack.get(++currentTrackIndex);
		//hack
		nextTrack = nextTrack == 0 ? randomTrack.size() - 1 : nextTrack - 1;
		player.skipToTrack(nextTrack);
	}

	@Override
	public void onRequestException(Exception e, Object o) {
		Log.e(TAG, "onRequestException", e);
		if (playerListener != null) {
			String message;
			if (application.isNetworkAvailable()) {
				message = context.getString(R.string.error_unknown);
			} else {
				message = context.getString(R.string.error_no_connection);
			}
			playerListener.onError(new PlayerException(message, e));
		}
	}

	@Override
	public void onPlayerError(Exception e, long l) {
		Log.e(TAG, "onPlayerError", e);
		if (playerListener != null) {
			playerListener.onError(new PlayerException(e));
		}
	}
}
