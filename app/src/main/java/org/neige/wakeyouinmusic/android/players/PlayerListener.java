package org.neige.wakeyouinmusic.android.players;

public interface PlayerListener {

	public void onTrackChange();

	public void onError(PlayerException e);

}
