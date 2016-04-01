package org.neige.wakeyouinmusic.android.spotify;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.neige.wakeyouinmusic.android.BuildConfig;
import org.neige.wakeyouinmusic.android.spotify.models.User;
import org.neige.wakeyouinmusic.android.spotify.services.SpotifyService;
import org.neige.wakeyouinmusic.backend.spotify.Spotify;
import org.neige.wakeyouinmusic.backend.spotify.model.SpotifyToken;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;

public class ApiSpotify {

	public static final int PLAYLIST_LIMIT = 25;
	public static final int TRACK_LIMIT = 25;
	private static final String TAG = "ApiSpotify";
	private static final String BASE_URL_BACKEND = "https://wakeyouinmusic.appspot.com/_ah/api/";
	private static final String BASE_URL_SPOTIFY = "https://api.spotify.com/v1/";
	private static final String PREF_SPOTIFY_USER_TOKEN = "spotify_user_token";
	private static final String PREF_SPOTIFY_USER_REFRESH_TOKEN = "spotify_user_refresh_token";
	private static final String PREF_SPOTIFY_USER_ID = "spotify_user_id";
	private static final int HTTP_CONNECT_TIMEOUT = 6000;           // milliseconds
	private static final int HTTP_READ_TIMEOUT = 10000;             // milliseconds
	private static final long HTTP_CACHE_SIZE = 10 * 1024 * 1024;   // byte
	private static final String HEADER_TOKEN_KEY = "Authorization";
	private static final String HEADER_TOKEN_VALUE_SUFFIX = " Bearer ";

	private static ApiSpotify instance;

	private String accessToken = null;
	private String refreshToken = null;
	private String userId;
	private Client httpClient;
	private GsonConverter gsonConverter;
	private SharedPreferences sharedPreferences;
	private Spotify spotifyBackEndApi;

	//Service
	private SpotifyService spotifyService;

	/**
	 * Returns the instance of this singleton.
	 */
	public static ApiSpotify getInstance() {
		if (instance == null) {
			throw new IllegalStateException("You must call init before try to getInstance()");
		}
		return instance;
	}

	/**
	 * Init the singleton with an application context
	 * @param application
	 */
	public static void init(Application application) {
		instance = new ApiSpotify();

		instance.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
		instance.setUserToken(instance.sharedPreferences.getString(PREF_SPOTIFY_USER_TOKEN, null), instance.sharedPreferences.getString(PREF_SPOTIFY_USER_REFRESH_TOKEN, null));
		instance.setUserId(instance.sharedPreferences.getString(PREF_SPOTIFY_USER_ID, null));

		instance.httpClient = instance.getHttpClient(application);
		instance.gsonConverter = instance.getGsonConverter();

		instance.spotifyBackEndApi = new Spotify.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl(BASE_URL_BACKEND)
				.setGoogleClientRequestInitializer(abstractGoogleClientRequest -> abstractGoogleClientRequest.setDisableGZipContent(true)).build();

		instance.buildService();
	}

	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * Get user id for user path rest request
	 * @return userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Set user id for user path rest request
	 * @param userId
	 */
	private void setUserId(String userId) {
		this.userId = userId;
		sharedPreferences.edit().putString(PREF_SPOTIFY_USER_ID, userId).apply();
	}

	/**
	 * Creates the RestAdapter by setting custom HttpClient.
	 */
	private RestAdapter buildRestAdapter() {
		return new RestAdapter.Builder().setEndpoint(BASE_URL_SPOTIFY).setRequestInterceptor(getRequestInterceptor()).setConverter(gsonConverter).setErrorHandler(getErrorHandler())
				.setClient(httpClient).setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC).build();
	}

	private ErrorHandler getErrorHandler() {
		return cause -> cause;
	}

	/**
	 * Custom Http Client to define connection timeouts.
	 * @param context
	 * @return
	 */
	private Client getHttpClient(Context context) {
		OkHttpClient httpClient = new OkHttpClient();
		httpClient.interceptors().add(chain -> {
			Request request = chain.request();

			// try the request
			Response response = chain.proceed(request);

			// if not authorized but seem to have valid accessToken
			if (response.code() == 401 && isConnected()) {

				try {
					SpotifyToken spotifyToken = spotifyBackEndApi.refresh(refreshToken).execute();
					setUserToken(spotifyToken.getAccessToken(), spotifyToken.getRefreshToken());
				} catch (Exception e) {
					Log.e(TAG, "Unable to refrsh accessToken", e);
					setUserToken(null, null);
					return response;
				}

				// create a new request and modify it accordingly using the new accessToken
				Request newRequest = request.newBuilder().removeHeader(HEADER_TOKEN_KEY).addHeader(HEADER_TOKEN_KEY, HEADER_TOKEN_VALUE_SUFFIX + accessToken).build();

				// retry the request
				return chain.proceed(newRequest);
			}

			// otherwise just pass the original response on
			return response;
		});
		httpClient.setConnectTimeout(HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
		httpClient.setReadTimeout(HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS);
		try {
			Cache cache = new Cache(context.getCacheDir(), HTTP_CACHE_SIZE);
			httpClient.setCache(cache);
		} catch (IOException e) {
			Log.i(this.getClass().getName(), "Unable to instantiate cache system", e);
		}
		return new OkClient(httpClient);
	}

	/**
	 * Generic Request Interceptor
	 * @return
	 */
	private RequestInterceptor getRequestInterceptor() {
		return request -> {
			request.addHeader("Content-Type", "application/json");
			if (accessToken != null) {
				request.addHeader(HEADER_TOKEN_KEY, HEADER_TOKEN_VALUE_SUFFIX + accessToken);
			}
		};
	}

	/**
	 * Create a Gson Converter with all custom Serializer and Deserializer
	 * @return
	 */
	private GsonConverter getGsonConverter() {
		Gson gson = new GsonBuilder().create();
		return new GsonConverter(gson);
	}

	/**
	 * Init all rest service
	 */
	private void buildService() {
		RestAdapter restAdapter = buildRestAdapter();

		spotifyService = restAdapter.create(SpotifyService.class);
	}

	/**
	 * Request to spotify a accessToken and a refresh accessToken from authentication code
	 * @param code from the spotify authentication code flow
	 * @return Observable Spotify credential
	 */
	public Observable<User> requestToken(String code) {
		return Observable.create(subscriber -> {
			try {
				SpotifyToken spotifyToken = spotifyBackEndApi.token(code).execute();
				setUserToken(spotifyToken.getAccessToken(), spotifyToken.getRefreshToken());

				getSpotifyService().getCurrentUser().doOnNext(user -> {
					if (!user.product.equals(User.PRODUCT_PREMIUM)) {
						Log.i(TAG, "User don't have premium account");
						setUserToken(null, null);
					} else {
						setUserId(user.id);
					}

					subscriber.onNext(user);
					subscriber.onCompleted();

				}).subscribe();
			} catch (Exception e) {
				Log.e(TAG, "Unable to retrieve accessToken", e);
				subscriber.onError(e);
			}
		});
	}

	/**
	 * Try to refresh user accessToken
	 * @return Observable of spotify accessToken
	 */
	public Observable<SpotifyToken> refreshToken() {
		return Observable.create(subscriber -> {
			try {
				SpotifyToken spotifyToken = spotifyBackEndApi.refresh(refreshToken).execute();
				setUserToken(spotifyToken.getAccessToken(), spotifyToken.getRefreshToken());
				subscriber.onNext(spotifyToken);
				subscriber.onCompleted();
			} catch (Exception e) {
				Log.e(TAG, "Unable to refresh accessToken");
				setUserToken(null, null);
				subscriber.onError(e);
			}
		});
	}

	/**
	 * log out the user
	 */
	public void logout() {
		setUserToken(null, null);
		setUserId(null);
	}

	/**
	 * Add user authentication information and reinitialize the local rest adapters to allow it to add the driver login accessToken to any subsuquent REST request
	 * Use null value to reset the tokens after logout
	 * @param userToken user accessToken recouped from login
	 */
	private void setUserToken(String userToken, String refreshToken) {
		this.accessToken = userToken;

		if (userToken == null || refreshToken != null) {
			this.refreshToken = refreshToken;
			sharedPreferences.edit().putString(PREF_SPOTIFY_USER_REFRESH_TOKEN, refreshToken).apply();
		}

		sharedPreferences.edit().putString(PREF_SPOTIFY_USER_TOKEN, userToken).apply();
	}

	/**
	 * Test if the user credential is not null
	 * @return
	 */
	public boolean isConnected() {
		return accessToken != null && refreshToken != null && userId != null;
	}

	/**
	 * Get the rest service for user interaction
	 * @return The point of interest service to query
	 */
	public SpotifyService getSpotifyService() {
		return spotifyService;
	}

}
