package org.neige.wakeyouinmusic.android.players;

public class PlayerException extends Exception {

	public PlayerException(Throwable throwable) {
		super(throwable.getMessage(), throwable);
	}

	public PlayerException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public PlayerException(String detailMessage) {
		super(detailMessage);
	}
}
