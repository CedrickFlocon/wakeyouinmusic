package org.neige.wakeyouinmusic.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.deezer.sdk.model.PaginatedList;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.JsonUtils;
import com.deezer.sdk.network.request.event.RequestListener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.WakeYouInMusicApplication;
import org.neige.wakeyouinmusic.android.adapters.RingtoneExpandableListAdapter;
import org.neige.wakeyouinmusic.android.models.DeezerRingtone;
import org.neige.wakeyouinmusic.android.models.DefaultRingtone;
import org.neige.wakeyouinmusic.android.models.Ringtone;
import org.neige.wakeyouinmusic.android.models.SpotifyRingtone;
import org.neige.wakeyouinmusic.android.spotify.ApiSpotify;
import org.neige.wakeyouinmusic.android.spotify.models.Pager;
import org.neige.wakeyouinmusic.android.spotify.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static rx.android.app.AppObservable.bindFragment;

public class RingtoneDialogFragment extends DialogFragment implements RingtoneExpandableListAdapter.ActionListener {

	public static final int ACTIVITY_RESULT_SPOTIFY_LOGIN = 1000;

    private final static int DEEZER_LIMIT = 25;
	private final static String TAG = "RingtoneDialogFragment";

    private final CompositeSubscription subscriptions = new CompositeSubscription();
	private CompositeSubscription subscription = new CompositeSubscription();

	//Deezer
	private DeezerConnect deezerConnect;
	private SessionStore sessionStore;

	//View
	private RingtoneExpandableListAdapter ringtoneAdapter;
	private ExpandableListView expandableListViewRingtone;

	//Callback
	private InteractionListener interactionListener;

	//Playlist
	private List<Ringtone> deezerRingtones = new ArrayList<>();
	private List<Ringtone> spotifyRingtones = new ArrayList<>();
	private Ringtone selectedRingtone = null;

	public static RingtoneDialogFragment newInstance(InteractionListener interactionListener) {
		RingtoneDialogFragment fragment = new RingtoneDialogFragment();
		Bundle bundle = new Bundle();
		fragment.setArguments(bundle);
		fragment.setInteractionListener(interactionListener);
		return fragment;
	}

    public void setInteractionListener(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		deezerConnect = new DeezerConnect(getActivity(), WakeYouInMusicApplication.DEEZER_APPLICATION_ID);
		sessionStore = new SessionStore();

		ringtoneAdapter = new RingtoneExpandableListAdapter(getActivity(), getRingtoneType());
		if (selectedRingtone != null){
			ringtoneAdapter.setSelectedRingtone(selectedRingtone);
		}
		ringtoneAdapter.setActionListener(this);
		loadRingtone();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = createView();
		builder.setTitle(R.string.ringtone_picker_title);
		builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            if (interactionListener != null){
                interactionListener.onAccept();
            }
        });
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setView(view);
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	private View createView() {
		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_ringtone, null);

		expandableListViewRingtone = (ExpandableListView) v.findViewById(R.id.expandableListViewRingtone);
		ringtoneAdapter.notifyDataSetChanged();
		expandableListViewRingtone.setAdapter(ringtoneAdapter);
		for (int i = 0; i < ringtoneAdapter.getGroupCount(); i++) {
			expandableListViewRingtone.expandGroup(i);
		}

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		subscription.clear();
	}

	private LinkedHashMap<Class<? extends Ringtone>, RingtoneExpandableListAdapter.Status> getRingtoneType() {
		LinkedHashMap<Class<? extends Ringtone>, RingtoneExpandableListAdapter.Status> ringtoneStatus = new LinkedHashMap<>();
		ringtoneStatus.put(DeezerRingtone.class, RingtoneExpandableListAdapter.Status.LOADING);
		ringtoneStatus.put(SpotifyRingtone.class, RingtoneExpandableListAdapter.Status.LOADING);
		ringtoneStatus.put(DefaultRingtone.class, RingtoneExpandableListAdapter.Status.LOADING);

		return ringtoneStatus;
	}

	private void loadRingtone() {
		deezerRingtones.clear();
		spotifyRingtones.clear();

		loadDefaultRingtone();
		loadDeezerRingtone();
		loadSpotifyRingtone();
	}

	private void loadDefaultRingtone() {
		List<Ringtone> androidDefaultRingtones = new ArrayList<>();
		RingtoneManager ringtoneManager = new RingtoneManager(getActivity());
		ringtoneManager.setType(RingtoneManager.TYPE_ALARM);
		Cursor alarmsCursor = ringtoneManager.getCursor();
		if (alarmsCursor.getCount() > 0){
			alarmsCursor.moveToFirst();
			do {
				DefaultRingtone defaultRingtone = new DefaultRingtone();
				defaultRingtone.setUri(ringtoneManager.getRingtoneUri(alarmsCursor.getPosition()).toString());
				defaultRingtone.setTitle(alarmsCursor.getString(alarmsCursor.getColumnIndex("title")));
				androidDefaultRingtones.add(defaultRingtone);
			} while (alarmsCursor.moveToNext());
			ringtoneAdapter.changeStatusToLoaded(DefaultRingtone.class, androidDefaultRingtones, false);
		}
	}

	private void loadDeezerRingtone() {
		if (sessionStore.restore(deezerConnect, getActivity())) {
			ringtoneAdapter.changeStatusToLoading(DeezerRingtone.class);

            DeezerRequest deezerRequest = DeezerRequestFactory.requestCurrentUserPlaylists();
            deezerRequest.getParams().putString("limit", String.valueOf(DEEZER_LIMIT));
			deezerRequest.getParams().putString("index", String.valueOf(deezerRingtones.size()));

			deezerConnect.requestAsync(deezerRequest, new RequestListener() {
                @Override
                public void onComplete(String s, Object o) {
                    Log.i(TAG, s);
                    boolean hasMore;

                    try {
                        PaginatedList<Playlist> playlists = (PaginatedList) JsonUtils.deserializeObject(new JSONObject(s));
                        if (playlists.getTotalSize() == 0) {
                            ringtoneAdapter.changeStatusToError(DeezerRingtone.class, getString(R.string.ringtone_picker_error_no_playlist));
                        } else {
                            for (Playlist playlist : playlists) {
                                DeezerRingtone deezerRingtone = new DeezerRingtone();
                                deezerRingtone.setTitle(playlist.getTitle());
                                deezerRingtone.setDeezerId(playlist.getId());
                                deezerRingtones.add(deezerRingtone);
                            }
                            hasMore = playlists.getTotalSize() != deezerRingtones.size();
                            ringtoneAdapter.changeStatusToLoaded(DeezerRingtone.class, deezerRingtones, hasMore);
                            if (hasMore){
                                loadDeezerRingtone();
                            }
                        }
                    } catch (JSONException e) {
                        onException(e, o);
                    }
                }

                @Override
                public void onException(Exception e, Object o) {
                    Log.e(TAG, e.getMessage(), e);
                    changeRingtoneTypeError(DeezerRingtone.class);
				}
			});
		} else {
			ringtoneAdapter.changeStatusToNotConnected(DeezerRingtone.class, getString(R.string.not_connected));
		}
	}

	private void loadSpotifyRingtone() {
		if (ApiSpotify.getInstance().isConnected()) {
			ringtoneAdapter.changeStatusToLoading(SpotifyRingtone.class);
			Map<String, Object> option = new HashMap<>();
			option.put("limit", String.valueOf(ApiSpotify.PLAYLIST_LIMIT));
			option.put("offset", spotifyRingtones.size());

			subscription.add(bindFragment(this, ApiSpotify.getInstance().getSpotifyService().getPlaylists(ApiSpotify.getInstance().getUserId(), option))
					.subscribe(new Subscriber<Pager<org.neige.wakeyouinmusic.android.spotify.models.Playlist>>() {
						private boolean spotifyHasMore = true;

						@Override
						public void onCompleted() {
							if (spotifyHasMore) {
								loadSpotifyRingtone();
							}
						}

						@Override
						public void onError(Throwable e) {
							changeRingtoneTypeError(SpotifyRingtone.class);
						}

						@Override
						public void onNext(Pager<org.neige.wakeyouinmusic.android.spotify.models.Playlist> playlistPager) {
							for (org.neige.wakeyouinmusic.android.spotify.models.Playlist playlistBase : playlistPager.items) {
								SpotifyRingtone spotifyRingtone = new SpotifyRingtone();
								spotifyRingtone.setSpotifyId(playlistBase.id);
								spotifyRingtone.setTitle(playlistBase.name);
								spotifyRingtones.add(spotifyRingtone);
							}
							spotifyHasMore = playlistPager.total != spotifyRingtones.size();
							if (playlistPager.total == 0){
								ringtoneAdapter.changeStatusToError(SpotifyRingtone.class, getString(R.string.ringtone_picker_error_no_playlist));
							}else {
								ringtoneAdapter.changeStatusToLoaded(SpotifyRingtone.class, spotifyRingtones, spotifyHasMore);
							}
						}
					}));
		} else {
			ringtoneAdapter.changeStatusToNotConnected(SpotifyRingtone.class, getString(R.string.not_connected));
		}
	}

	private void changeRingtoneTypeError(Class<? extends Ringtone> ringtone) {
		int message;
		if (((WakeYouInMusicApplication) getActivity().getApplication()).isNetworkAvailable()) {
			message = R.string.error_unknown;
		} else {
			message = R.string.error_no_connection;
		}
		changeRingtoneTypeError(ringtone, getString(message));
	}

	private void changeRingtoneTypeError(Class<? extends Ringtone> ringtone, String message) {
		ringtoneAdapter.changeStatusToError(ringtone, message);
	}

	public Ringtone getSelectedRingtone() {
		return ringtoneAdapter.getSelectedRingtone();
	}

	public void setSelectedRingtone(Ringtone selectedRingtone){
		this.selectedRingtone = selectedRingtone;
		if (ringtoneAdapter != null){
			ringtoneAdapter.setSelectedRingtone(selectedRingtone);
			ringtoneAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onRetry(Class<Ringtone> ringtoneClass) {
		if (ringtoneClass.equals(DeezerRingtone.class)) {
			if (sessionStore.restore(deezerConnect, getActivity())) {
				loadDeezerRingtone();
			} else {
				onConnect(ringtoneClass);
			}
		} else if (ringtoneClass.equals(SpotifyRingtone.class)) {
			if (ApiSpotify.getInstance().isConnected()) {
				loadSpotifyRingtone();
			} else {
				onConnect(ringtoneClass);
			}
		}
	}

	@Override
	public void onConnect(Class<Ringtone> ringtoneClass) {
		if (ringtoneClass.equals(DeezerRingtone.class)) {
			DialogListener listener = new DialogListener() {
				public void onComplete(Bundle values) {
					sessionStore.save(deezerConnect, getActivity());
					onRetry(ringtoneClass);
					Log.i(TAG, "Deezer connection successful");
				}

				public void onCancel() {
					Log.i(TAG, "Deezer connection canceled");
				}

				public void onException(Exception e) {
					Log.e(TAG, e.getMessage(), e);
					changeRingtoneTypeError(DeezerRingtone.class);
				}
			};
			deezerConnect.authorize(getActivity(), WakeYouInMusicApplication.DEEZER_PERMISSIONS, listener);
		} else if (ringtoneClass.equals(SpotifyRingtone.class)) {
			ringtoneAdapter.changeStatusToLoading(SpotifyRingtone.class);
			AuthenticationRequest.Builder builder =
					new AuthenticationRequest.Builder(WakeYouInMusicApplication.SPOTIFY_CLIENT_ID, AuthenticationResponse.Type.CODE, WakeYouInMusicApplication.SPOTIFY_CALLBACK);

			builder.setScopes(WakeYouInMusicApplication.SPOTIFY_PERMISSIONS);
			AuthenticationRequest request = builder.build();

			startActivityForResult(AuthenticationClient.createLoginActivityIntent(getActivity(), request), ACTIVITY_RESULT_SPOTIFY_LOGIN);
		}
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
									changeRingtoneTypeError(SpotifyRingtone.class);
								}

								@Override
								public void onNext(org.neige.wakeyouinmusic.android.spotify.models.User user) {
									if (user.product.equals(User.PRODUCT_PREMIUM)) {
										loadSpotifyRingtone();
									} else {
										AuthenticationClient.logout(getActivity());
										ringtoneAdapter.changeStatusToNotConnected(SpotifyRingtone.class, getString(R.string.error_spotify_no_premium));
									}

								}
							}));
					break;

				case ERROR:
					Log.e(TAG, "An error as occurred during login process : " + response.getError());
					changeRingtoneTypeError(SpotifyRingtone.class);
					break;

				default:
					ringtoneAdapter.changeStatusToNotConnected(SpotifyRingtone.class, getString(R.string.not_connected));
					Log.i(TAG, "Spotify connection canceled");
					break;
			}
		}
	}

	public interface InteractionListener{
		void onAccept();
	}

}