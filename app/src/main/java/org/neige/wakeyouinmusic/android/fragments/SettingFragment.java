package org.neige.wakeyouinmusic.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.deezer.sdk.model.User;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.JsonUtils;
import com.deezer.sdk.network.request.event.RequestListener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.neige.wakeyouinmusic.android.ActionPreference;
import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.WakeYouInMusicApplication;
import org.neige.wakeyouinmusic.android.activities.AboutActivity;
import org.neige.wakeyouinmusic.android.spotify.ApiSpotify;

import retrofit.RetrofitError;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SettingFragment extends PreferenceFragment {

	public static final int ACTIVITY_RESULT_SPOTIFY_LOGIN = 1000;
	private static final String TAG = "PreferenceFragment";
	private static final String MARKET_WEB_BASE_URI = "http://play.google.com/store/apps/details?id=";
	private final CompositeSubscription subscriptions = new CompositeSubscription();
	private DeezerConnect deezerConnect;
	private SessionStore sessionStore = new SessionStore();

	private ActionPreference preferenceDeezerConnect;
	private ActionPreference preferenceSpotifyConnect;
	private Preference volumePreference;
	private ListPreference crescendoDurationPreference, snoozeDurationPreference;
	private RingtonePreference ringtonePreference;

	public static SettingFragment newInstance() {
		SettingFragment fragment = new SettingFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		deezerConnect = new DeezerConnect(activity, WakeYouInMusicApplication.DEEZER_APPLICATION_ID);
		sessionStore.restore(deezerConnect, activity);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();

		updateDeezerActionPreference();
		updateSpotifyActionPreference();
	}

	@Override
	public void onPause() {
		super.onPause();
		subscriptions.clear();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.global_preference);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		preferenceDeezerConnect = (ActionPreference) findPreference(WakeYouInMusicApplication.PREFERENCE_DEEZER_LOGIN_KEY);
		preferenceSpotifyConnect = (ActionPreference) findPreference(WakeYouInMusicApplication.PREFERENCE_SPOTIFY_LOGIN_KEY);
		volumePreference = findPreference(WakeYouInMusicApplication.PREFERENCE_VOLUME_KEY);
		crescendoDurationPreference = (ListPreference) findPreference(WakeYouInMusicApplication.PREFERENCE_CRESCENDO_DURATION_KEY);
		snoozeDurationPreference = (ListPreference) findPreference(WakeYouInMusicApplication.PREFERENCE_SNOOZE_DURATION_KEY);
		ringtonePreference = (RingtonePreference) findPreference(WakeYouInMusicApplication.PREFERENCE_DEFAULT_RINGTONE_KEY);

		Uri actualDefaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_ALARM);
		updateRingtonePreferenceSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(WakeYouInMusicApplication.PREFERENCE_DEFAULT_RINGTONE_KEY, actualDefaultRingtoneUri == null ? null : actualDefaultRingtoneUri.toString()));
		ringtonePreference.setOnPreferenceChangeListener((preference, newValue) -> {
			updateRingtonePreferenceSummary((String) newValue);
			return true;
		});

		updateVolumeSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(WakeYouInMusicApplication.PREFERENCE_VOLUME_KEY, WakeYouInMusicApplication.PREFERENCE_VOLUME_DEFAULT_VALUE));
		volumePreference.setOnPreferenceChangeListener((preference, newValue) -> {
			updateVolumeSummary((Integer) newValue);
			return true;
		});

		updateListPreferenceSummary(crescendoDurationPreference, crescendoDurationPreference.getValue(), R.array.crescendoDuration, R.array.crescendoDurationValue);
		crescendoDurationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
			updateListPreferenceSummary(crescendoDurationPreference, (String) newValue, R.array.crescendoDuration, R.array.crescendoDurationValue);
			return true;
		});

		updateListPreferenceSummary(snoozeDurationPreference, snoozeDurationPreference.getValue(), R.array.snoozeDuration, R.array.snoozeDurationValue);
		snoozeDurationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
			updateListPreferenceSummary(snoozeDurationPreference, (String) newValue, R.array.snoozeDuration, R.array.snoozeDurationValue);
			return true;
		});

		preferenceDeezerConnect.setWidgetClickListener(v -> {
			switch (v.getId()) {
				case R.id.signInButton:
					deezerConnection();
					break;
				case R.id.signOutButton:
					sessionStore.clear(getActivity());
					deezerConnect.logout(getActivity());
					updateDeezerActionPreference();
					break;
				case R.id.retryButton:
					if (deezerConnect.isSessionValid()) {
						updateDeezerActionPreference();
					} else {
						deezerConnection();
					}
					break;
				default:
					Log.d(TAG, "Preference action not implemented. View id : " + v.getId());
			}
		});

		preferenceSpotifyConnect.setWidgetClickListener(v -> {
			switch (v.getId()) {
				case R.id.signInButton:
					spotifyConnection();
					break;
				case R.id.signOutButton:
					ApiSpotify.getInstance().logout();
					AuthenticationClient.logout(getActivity());
					updateSpotifyActionPreference();
					break;
				case R.id.retryButton:
					if (ApiSpotify.getInstance().isConnected()) {
						updateSpotifyActionPreference();
					} else {
						spotifyConnection();
					}
					break;
				default:
					Log.d(TAG, "Preference action not implemented. View id : " + v.getId());
			}
		});

		volumePreference.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View v = ((LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.preference_volume, null);
            ((SeekBar) v.findViewById(R.id.seekBarVolume)).setProgress(PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getInt(WakeYouInMusicApplication.PREFERENCE_VOLUME_KEY, WakeYouInMusicApplication.PREFERENCE_VOLUME_DEFAULT_VALUE));
            builder.setView(v);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (volumePreference.getOnPreferenceChangeListener() != null){
                        if (volumePreference.getOnPreferenceChangeListener().onPreferenceChange(volumePreference, ((SeekBar) v.findViewById(R.id.seekBarVolume)).getProgress())){
                            save();
                        }
                    }else {
                        save();
                    }
                }

                private void save(){
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(WakeYouInMusicApplication.PREFERENCE_VOLUME_KEY, ((SeekBar) v.findViewById(R.id.seekBarVolume)).getProgress()).commit();
                }
            });
            builder.setNegativeButton(android.R.string.cancel,null);
            builder.setTitle(R.string.title_volume);
            builder.create().show();
            return true;
        });

		findPreference(WakeYouInMusicApplication.PREFERENCE_SHARE_APPLICATION_KEY).setOnPreferenceClickListener(preference -> {
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message) + MARKET_WEB_BASE_URI + getActivity().getPackageName());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
			shareIntent.setType("text/plain");
			startActivity(Intent.createChooser(shareIntent, getString(R.string.title_share_application)));
			return true;
		});

		findPreference(WakeYouInMusicApplication.PREFERENCE_ABOUT_KEY).setOnPreferenceClickListener(preference -> {
			startActivity(new Intent(getActivity(), AboutActivity.class));
			getActivity().overridePendingTransition(R.anim.activity_transition_from_right, R.anim.activity_transition_to_left);
			return true;
		});
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == ACTIVITY_RESULT_SPOTIFY_LOGIN) {
			AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

			switch (response.getType()) {
				case CODE:
					Log.i(TAG, "Spotify connection successful");
					subscriptions.add(ApiSpotify.getInstance().requestToken(response.getCode()).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
							.subscribe(new Subscriber<org.neige.wakeyouinmusic.android.spotify.models.User>() {
								@Override
								public void onCompleted() {
								}

								@Override
								public void onError(Throwable e) {
									Log.e(TAG, "Unable to retrieve user info", e);
									updateActionPreference(preferenceSpotifyConnect, e, R.layout.view_retry);
								}

								@Override
								public void onNext(org.neige.wakeyouinmusic.android.spotify.models.User user) {
									if (user.product.equals(org.neige.wakeyouinmusic.android.spotify.models.User.PRODUCT_PREMIUM)) {
										updateActionPreference(preferenceSpotifyConnect, user.display_name == null ? user.id :user.display_name, R.layout.view_sign_out);
									} else {
										AuthenticationClient.logout(getActivity());
										updateActionPreference(preferenceSpotifyConnect, getString(R.string.error_spotify_no_premium), R.layout.view_sign_in);
									}
								}
							}));
					break;

				case ERROR:
					updateActionPreference(preferenceSpotifyConnect, new Exception(), R.layout.view_retry);
					break;

				default:
					Log.i(TAG, "Spotify connection canceled");
					updateSpotifyActionPreference();
					break;
			}
		}
	}

	private void updateVolumeSummary(int newValue){
		volumePreference.setSummary(newValue + "%");
	}

	private void updateListPreferenceSummary(ListPreference listPreference, String newValue, int resourceArrayId, int resourceArrayValueId){
		String[] stringArray = getResources().getStringArray(resourceArrayValueId);
		for (int i = 0; i < stringArray.length; i++) {
			String resource = stringArray[i];
			if (newValue.equals(resource)){
				listPreference.setSummary(getResources().getStringArray(resourceArrayId)[i]);
				return;
			}
		}
		listPreference.setSummary("");
	}

	private void updateRingtonePreferenceSummary(String uri){
		if (uri != null){
			Ringtone  r = RingtoneManager.getRingtone(getActivity(), Uri.parse(uri));
			ringtonePreference.setSummary(r == null ? getString(R.string.ringtone_unnamed) : r.getTitle(getActivity()));
		}
	}

	private void updateDeezerActionPreference() {
		if (deezerConnect.isSessionValid()) {
			preferenceDeezerConnect.setSummary(R.string.loading);
			deezerConnect.requestAsync(DeezerRequestFactory.requestCurrentUser(), new RequestListener() {
				@Override
				public void onComplete(String s, Object o) {
					try {
						updateActionPreference(preferenceDeezerConnect, ((User) JsonUtils.deserializeObject(new JSONObject(s))).getName(), R.layout.view_sign_out);
						preferenceDeezerConnect.setWidgetLayoutResource(R.layout.view_sign_out);
					} catch (JSONException e) {
						onException(e, o);
					}
				}

				@Override
				public void onException(Exception e, Object o) {
					Log.e(TAG, "Deezer get user information error", e);
					updateActionPreference(preferenceDeezerConnect, e, R.layout.view_retry);
				}
			});
		} else {
			updateActionPreference(preferenceDeezerConnect, R.string.not_connected, R.layout.view_sign_in);
		}
	}

	private void updateSpotifyActionPreference() {
		if (ApiSpotify.getInstance().isConnected()) {
			preferenceSpotifyConnect.setSummary(R.string.loading);
			subscriptions.add(ApiSpotify.getInstance().getSpotifyService().getCurrentUser().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Subscriber<org.neige.wakeyouinmusic.android.spotify.models.User>() {
						@Override
						public void onCompleted() {

						}

						@Override
						public void onError(Throwable e) {
							Log.e(TAG, "Spotify get user information error", e);
							if (e instanceof RetrofitError && ((RetrofitError) e).getResponse() != null &&
									((RetrofitError) e).getResponse().getStatus() == 401) {
								updateActionPreference(preferenceSpotifyConnect, R.string.not_connected, R.layout.view_sign_in);
							} else {
								updateActionPreference(preferenceSpotifyConnect, e, R.layout.view_retry);
							}
						}

						@Override
						public void onNext(org.neige.wakeyouinmusic.android.spotify.models.User user) {
							updateActionPreference(preferenceSpotifyConnect, user.display_name == null ? user.id : user.display_name, R.layout.view_sign_out);
						}
					}));
		} else {
			updateActionPreference(preferenceSpotifyConnect, R.string.not_connected, R.layout.view_sign_in);
		}
	}

	private void updateActionPreference(ActionPreference actionPreference, Throwable e, int layout) {
		int message;
		if (((WakeYouInMusicApplication) getActivity().getApplication()).isNetworkAvailable()) {
			message = R.string.error_unknown;
		} else {
			message = R.string.error_no_connection;
		}
		updateActionPreference(actionPreference, message, layout);
	}

	private void updateActionPreference(ActionPreference actionPreference, int message, int layout) {
		updateActionPreference(actionPreference, getString(message), layout);
	}

	private void updateActionPreference(ActionPreference actionPreference, String message, int layout) {
		actionPreference.setSummary(message);
		actionPreference.setWidgetLayoutResource(layout);
	}

	private void deezerConnection() {
		DialogListener listener = new DialogListener() {
			public void onComplete(Bundle values) {
				sessionStore.save(deezerConnect, getActivity());
				updateDeezerActionPreference();
				Log.i(TAG, "Deezer connection successful");
			}

			public void onCancel() {
				Log.i(TAG, "Deezer connection canceled");
				updateDeezerActionPreference();
			}

			public void onException(Exception e) {
				Log.e(TAG, "Deezer connection exception", e);
				updateActionPreference(preferenceDeezerConnect, e, R.layout.view_retry);
			}
		};
		deezerConnect.authorize(getActivity(), WakeYouInMusicApplication.DEEZER_PERMISSIONS, listener);
	}

	private void spotifyConnection() {
		preferenceSpotifyConnect.setSummary(R.string.loading);
		AuthenticationRequest.Builder builder =
				new AuthenticationRequest.Builder(WakeYouInMusicApplication.SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.CODE, WakeYouInMusicApplication.SPOTIFY_CALLBACK);

		builder.setScopes(WakeYouInMusicApplication.SPOTIFY_PERMISSIONS);
		AuthenticationRequest request = builder.build();

		startActivityForResult(AuthenticationClient.createLoginActivityIntent(getActivity(), request), ACTIVITY_RESULT_SPOTIFY_LOGIN);
	}

}