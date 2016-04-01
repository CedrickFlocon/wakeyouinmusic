package org.neige.wakeyouinmusic.backend.model;

import java.io.Serializable;

public class SpotifyToken implements Serializable {

	private String AccessToken;
	private String tokenType;
	private int expiresIn;
	private String refreshToken;

	public String getAccessToken() {
		return AccessToken;
	}

	public void setAccessToken(String accessToken) {
		AccessToken = accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

}
