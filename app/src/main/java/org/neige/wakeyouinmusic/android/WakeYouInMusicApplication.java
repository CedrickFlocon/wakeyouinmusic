package org.neige.wakeyouinmusic.android;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.deezer.sdk.model.Permissions;
import com.crashlytics.android.Crashlytics;

import org.neige.wakeyouinmusic.android.spotify.ApiSpotify;

import io.fabric.sdk.android.Fabric;

public class WakeYouInMusicApplication extends Application {

	public static final String[] DEEZER_PERMISSIONS = new String[]{Permissions.BASIC_ACCESS, Permissions.OFFLINE_ACCESS};
	public static final String DEEZER_APPLICATION_ID = "152251";

	public static final String[] SPOTIFY_PERMISSIONS = new String[]{"playlist-read-private","streaming", "user-read-private"};
	public static final String SPOTIFY_CALLBACK = "wakeyouinmusic://callback";
	public static final String SPOTIFY_CLIENT_ID = "49a6a8d30ee748f7a232328ecd277671";

	public static final String PREFERENCE_DEEZER_LOGIN_KEY = "deezerLoginButton";
	public static final String PREFERENCE_SPOTIFY_LOGIN_KEY = "spotifyLoginButton";
	public static final String PREFERENCE_SHARE_APPLICATION_KEY = "shareApplication";
	public static final String PREFERENCE_ABOUT_KEY = "about";

	public static final String PREFERENCE_DEFAULT_RINGTONE_KEY = "defaultRingtone";
	public static final String PREFERENCE_SNOOZE_DURATION_KEY = "snoozeDuration";
	public static final String PREFERENCE_VOLUME_KEY = "volume";
	public static final String PREFERENCE_CRESCENDO_DURATION_KEY = "crescendoDuration";

	public static final String PREFERENCE_SNOOZE_DURATION_DEFAULT_VALUE = "540";
	public static final int PREFERENCE_VOLUME_DEFAULT_VALUE = 50;
	public static final String PREFERENCE_CRESCENDO_DURATION_DEFAULT_VALUE = "5";

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Crashlytics crashlytics = new Crashlytics.Builder().disabled(BuildConfig.DEBUG).build();
		Fabric.with(this, crashlytics);

		ApiSpotify.init(this);
		AlarmHelper.init(this);
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
