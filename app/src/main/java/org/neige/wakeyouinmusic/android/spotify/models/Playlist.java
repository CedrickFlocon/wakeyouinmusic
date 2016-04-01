package org.neige.wakeyouinmusic.android.spotify.models;

public class Playlist extends PlaylistBase {
    public String description;
    public Followers followers;
    public String snapshot_id;
    public Pager<PlaylistTrack> tracks;
}