package org.neige.wakeyouinmusic.android.database;

import android.provider.BaseColumns;

public class SpotifyRingtoneContract {

	public static abstract class SpotifyRingtoneEntry implements BaseColumns {
		public static final String TABLE_NAME = "spotify_ringtone";
		public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID = "ringtone_id";
		public static final String COLUMN_NAME_SPOTIFY_PLAYLIST_ID = "spotify_playlist_id";


		public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
		public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
				" (" + COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
				COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + " INTEGER REFERENCES " + RingtoneContract.RingtoneEntry.TABLE_NAME + "(" + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID + ") ON DELETE CASCADE ON UPDATE CASCADE, " +
				COLUMN_NAME_SPOTIFY_PLAYLIST_ID + " TEXT" +
				");";
		public static final String SQL_CREATE_INDEX = "CREATE INDEX "  + TABLE_NAME + "_FK ON " + TABLE_NAME + "(" + COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + ");";
	}
}