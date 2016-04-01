package org.neige.wakeyouinmusic.android.spotify.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class PlaylistBase {
	public Boolean collaborative;
	public Map<String, String> external_urls;
	public String href;
	public String id;
	public List<Image> images;
	public String name;
	public UserSimple owner;
	@SerializedName("public")
	public Boolean is_public;
	public String type;
	public String uri;
}