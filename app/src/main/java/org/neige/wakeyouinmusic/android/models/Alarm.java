package org.neige.wakeyouinmusic.android.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.EnumSet;

public class Alarm implements Serializable {

	private long id;
	private String label;
	private Calendar time;
	private boolean enable;
	private EnumSet<DayOfWeek> dayOfWeeks;
	private Ringtone ringtone;
	private boolean vibrate;
	private Calendar lastModified;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Calendar getTime() {
		return time;
	}

	public void setTime(Calendar time) {
		this.time = time;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public EnumSet<DayOfWeek> getDayOfWeeks() {
		return dayOfWeeks;
	}

	public void setDayOfWeeks(EnumSet<DayOfWeek> dayOfWeeks) {
		this.dayOfWeeks = dayOfWeeks;
	}

	public Ringtone getRingtone() {
		return ringtone;
	}

	public void setRingtone(Ringtone ringtone) {
		this.ringtone = ringtone;
	}

	public boolean isVibrate() {
		return vibrate;
	}

	public void setVibrate(boolean vibrate) {
		this.vibrate = vibrate;
	}

	public Calendar getLastModified() {
		return lastModified;
	}

	public void setLastModified(Calendar lastModified) {
		this.lastModified = lastModified;
	}

}