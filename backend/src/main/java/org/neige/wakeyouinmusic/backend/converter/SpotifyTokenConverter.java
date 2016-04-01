package org.neige.wakeyouinmusic.backend.converter;

import com.google.appengine.repackaged.com.google.gson.JsonDeserializationContext;
import com.google.appengine.repackaged.com.google.gson.JsonDeserializer;
import com.google.appengine.repackaged.com.google.gson.JsonElement;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.appengine.repackaged.com.google.gson.JsonParseException;

import org.neige.wakeyouinmusic.backend.model.SpotifyToken;

import java.lang.reflect.Type;

public class SpotifyTokenConverter implements JsonDeserializer<SpotifyToken> {
	@Override
	public SpotifyToken deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		SpotifyToken spotifyToken = new SpotifyToken();

		JsonObject jsonObject = jsonElement.getAsJsonObject();

		spotifyToken.setTokenType(jsonObject.get("token_type").getAsString());
		spotifyToken.setAccessToken(jsonObject.get("access_token").getAsString());
		if (jsonObject.has("refresh_token")){
			spotifyToken.setRefreshToken(jsonObject.get("refresh_token").getAsString());
		}
		spotifyToken.setExpiresIn(jsonObject.get("expires_in").getAsInt());

		return spotifyToken;
	}
}
