package org.neige.wakeyouinmusic.android.spotify.models;

import java.util.List;

public class Pager<T> {
	public String href;
	public List<T> items;
	public int limit;
	public String next;
	public int offset;
	public String previous;
	public int total;
}
