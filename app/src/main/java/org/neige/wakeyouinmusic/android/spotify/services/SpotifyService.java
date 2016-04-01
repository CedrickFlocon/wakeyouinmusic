package org.neige.wakeyouinmusic.android.spotify.services;

import org.neige.wakeyouinmusic.android.spotify.models.Pager;
import org.neige.wakeyouinmusic.android.spotify.models.Playlist;
import org.neige.wakeyouinmusic.android.spotify.models.PlaylistBase;
import org.neige.wakeyouinmusic.android.spotify.models.PlaylistTrack;
import org.neige.wakeyouinmusic.android.spotify.models.Track;
import org.neige.wakeyouinmusic.android.spotify.models.User;

import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import rx.Observable;

public interface SpotifyService {

	@GET("/me")
	Observable<User> getCurrentUser();

	@GET("/users/{id}/playlists")
	Observable<Pager<Playlist>> getPlaylists(@Path("id") String userId, @QueryMap Map<String, Object> options);

	@GET("/tracks/{id}")
	Observable<Track> getTrack(@Path("id") String trackId);

	@GET("/users/{user_id}/playlists/{playlist_id}/tracks")
	Observable<Pager<PlaylistTrack>> getPlaylistTracks(@Path("user_id") String userId, @Path("playlist_id") String playlistId,@Query("market") String market,  @QueryMap Map<String, Object> options);

}
