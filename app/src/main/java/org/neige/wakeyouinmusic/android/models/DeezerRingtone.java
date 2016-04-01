package org.neige.wakeyouinmusic.android.models;

public class DeezerRingtone extends PlaylistRingtone {

	private long deezerId;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DeezerRingtone)) {
			return false;
		}
		return getDeezerId() == ((DeezerRingtone) o).getDeezerId();
	}

	public long getDeezerId() {
		return deezerId;
	}

	public void setDeezerId(long id) {
		this.deezerId = id;
	}
}
