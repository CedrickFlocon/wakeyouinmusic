package org.neige.wakeyouinmusic.android.models;

public class SpotifyRingtone extends PlaylistRingtone {

	private String spotifyId;

	public String getSpotifyId() {
		return spotifyId;
	}

	public void setSpotifyId(String spotifyId) {
		this.spotifyId = spotifyId;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SpotifyRingtone)) {
			return false;
		}
		return getSpotifyId().equals(((SpotifyRingtone) o).getSpotifyId());
	}
}
