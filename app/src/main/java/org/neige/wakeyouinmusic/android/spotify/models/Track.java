package org.neige.wakeyouinmusic.android.spotify.models;

import java.util.Map;

public class Track extends TrackSimple {
    public AlbumSimple album;
    public Map<String, String> external_ids;
    public Integer popularity;
}