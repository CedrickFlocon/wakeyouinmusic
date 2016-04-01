package org.neige.wakeyouinmusic.android.models;

import java.io.Serializable;

public abstract class Ringtone implements Serializable {

	private String title;
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return getTitle();
	}

}