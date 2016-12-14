package org.neige.wakeyouinmusic.android.players;

import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.WakeYouInMusicApplication;
import org.neige.wakeyouinmusic.android.models.SpotifyRingtone;
import org.neige.wakeyouinmusic.android.spotify.ApiSpotify;
import org.neige.wakeyouinmusic.android.spotify.models.PlaylistTrack;
import org.neige.wakeyouinmusic.android.spotify.models.User;
import org.neige.wakeyouinmusic.backend.spotify.model.SpotifyToken;

import retrofit.RetrofitError;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SpotifyPlayer extends PlaylistPlayer implements Player.InitializationObserver, PlayerNotificationCallback, ConnectionStateCallback {

    private static final String TAG = "SpotifyPlayer";
    private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

    private final SpotifyRingtone ringtone;
    private final CompositeSubscription subscription = new CompositeSubscription();

    private Player mPlayer;
    private Track currentTrack = new Track();
    private User currentUser = null;
    private boolean askPlay = false;
    private boolean isLoggedIn = false;

    protected SpotifyPlayer(WakeYouInMusicApplication application, SpotifyRingtone ringtone) throws PlayerException {
        super(application);
        this.ringtone = ringtone;

        if (!ApiSpotify.getInstance().isConnected()) {
            throw new PlayerException(context.getString(R.string.player_error_disconnected));
        }

        try {
            Config playerConfig = new Config(application, ApiSpotify.getInstance().getAccessToken(), WakeYouInMusicApplication.SPOTIFY_CLIENT_ID);
            mPlayer = Spotify.getPlayer(playerConfig, this, this);
        } catch (Exception e) {
            Log.e(TAG, "Spotify PLayer initialization : " + e.getCause(), e);
            Crashlytics.logException(e);
            throw new PlayerException(context.getString(R.string.player_error_spotify_internal));
        }
    }

    @Override
    public void play() {
        super.play();
        if (isLoggedIn) {
            if (currentUser == null) {
                loadUser();
            } else {
                initPlayer();
            }
        } else {
            askPlay = true;
        }
    }

    @Override
    public void pause() {
        mPlayer.pause();
    }

    @Override
    public void resume() {
        mPlayer.resume();
    }

    @Override
    public void next() {
        mPlayer.skipToNext();
    }

    @Override
    public void previous() {
        mPlayer.skipToPrevious();
    }

    @Override
    public void stop() {
        if (!mPlayer.isShutdown()) {
            mPlayer.pause();
            mPlayer.shutdown();
        }
        Spotify.destroyPlayer(this);
        subscription.clear();
        super.stop();
    }

    @Override
    public int getStreamType() {
        return STREAM_TYPE;
    }

    @Override
    public Track getCurrentTrackInformation() {
        return currentTrack;
    }

    private void initPlayer() {
        mPlayer.play("spotify:playlist:" + ringtone.getSpotifyId());
        mPlayer.setShuffle(ringtone.isShuffle());
        mPlayer.setRepeat(true);
    }

    private void loadUser() {
        subscription.add(ApiSpotify.getInstance().getSpotifyService().getCurrentUser().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).subscribe(new Subscriber<User>() {
            @Override
            public void onCompleted() {
                initPlayer();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Unable to retrieve user information", e);
                if (playerListener != null) {
                    playerListener.onError(manageError(e));
                }
            }

            @Override
            public void onNext(User user) {
                SpotifyPlayer.this.currentUser = user;
            }
        }));
    }

    @Override
    public void onInitialized(Player player) {
        Log.d(TAG, "Spotify player Initialized");
        try {
            mPlayer.setConnectivityStatus(getNetworkConnectivity());
        } catch (PlayerException e) {
            playerListener.onError(e);
        }
        mPlayer.addConnectionStateCallback(this);
        mPlayer.addPlayerNotificationCallback(this);
    }

    @Override
    public void onError(Throwable throwable) {
        Log.e(TAG, "Spotify PLayer initialization", throwable);
        if (playerListener != null) {
            playerListener.onError(new PlayerException(context.getString(R.string.player_error_no_internet)));
        }
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        if (eventType == EventType.TRACK_CHANGED) {
            loadTrackInformation(playerState.trackUri);
        } else if (eventType == EventType.LOST_PERMISSION) {
            if (playerListener != null) {
                playerListener.onError(new PlayerException(context.getString(R.string.player_error_lost_permission)));
            }
        }
    }

    private void loadTrackInformation(final String trackUri) {
        subscription.add(ApiSpotify.getInstance().getSpotifyService().getTrack(trackUri).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).subscribe(new Subscriber<org.neige.wakeyouinmusic.android.spotify.models.Track>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Unable to load track information", e);
            }

            @Override
            public void onNext(org.neige.wakeyouinmusic.android.spotify.models.Track track) {
                currentTrack = new Track();
                currentTrack.setName(track.name);
                currentTrack.setArtist(track.artists.get(0).name);
                currentTrack.setCoverUrl(track.album.images.get(0).url);

                if (playerListener != null){
                    playerListener.onTrackChange();
                }
            }
        }));
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        if (playerListener != null) {
            playerListener.onError(new PlayerException(s));
        }
    }

    private Connectivity getNetworkConnectivity() throws PlayerException {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            throw new PlayerException(context.getString(R.string.player_error_no_internet));
        }
    }

    private PlayerException manageError(Throwable e) {
        if (e instanceof RetrofitError) {
            if (((RetrofitError) e).getKind() == RetrofitError.Kind.HTTP) {
                switch (((RetrofitError) e).getResponse().getStatus()) {
                    case 404:
                        return new PlayerException(context.getString(R.string.player_error_playlist_unavailable));
                    case 401:
                        return new PlayerException(context.getString(R.string.player_error_unauthorized));
                }
            } else if (((RetrofitError) e).getKind() == RetrofitError.Kind.NETWORK) {
                return new PlayerException(context.getString(R.string.player_error_no_internet));
            }
        }
        return new PlayerException(context.getString(R.string.player_error_unknown));
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "onLoggedIn");
        isLoggedIn = true;
        if (askPlay) {
            play();
        }
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "onLoggedOut");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        Log.d(TAG, "onLoginFailed", throwable);
        if (currentUser == null) {
            subscription
                    .add(ApiSpotify.getInstance().getSpotifyService().getCurrentUser().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<User>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Unable to retrieve user", e);
                            if (playerListener != null) {
                                playerListener.onError(manageError(e));
                            }
                        }

                        @Override
                        public void onNext(User user) {
                            currentUser = user;
                            if (!currentUser.product.equals(User.PRODUCT_PREMIUM)) {
                                Log.i(TAG, "User don't have premium account");
                                if (playerListener != null) {
                                    playerListener.onError(new PlayerException(context.getString(R.string.player_error_no_prenium)));
                                }
                            } else {
                                //if token is expired its should be refresh by this request
                                mPlayer.login(ApiSpotify.getInstance().getAccessToken());
                            }
                        }
                    }));
        } else {
            refreshToken();
        }
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "onTemporaryError");
        if (playerListener != null) {
            playerListener.onError(new PlayerException(context.getString(R.string.player_error_no_internet)));
        }
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d(TAG, "onConnectionMessage");
    }

    private void refreshToken() {
        subscription.add(ApiSpotify.getInstance().refreshToken().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<SpotifyToken>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Unable to refresh token", e);
                if (playerListener != null) {
                    playerListener.onError(manageError(e));
                }
            }

            @Override
            public void onNext(SpotifyToken spotifyToken) {
                mPlayer.login(spotifyToken.getAccessToken());
            }
        }));
    }
}
