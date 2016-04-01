package org.neige.wakeyouinmusic.android.database;

import android.provider.BaseColumns;

public final class AlarmContract {

	public static abstract class AlarmEntry implements BaseColumns {
		public static final String TABLE_NAME = "alarm";
		public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID = "ringtone_id";
		public static final String COLUMN_NAME_LABEL = "label";
		public static final String COLUMN_NAME_VIBRATE = "vibrate";
		public static final String COLUMN_NAME_ENABLE = "enable";
		public static final String COLUMN_NAME_TIME = "time";
		public static final String COLUMN_NAME_LAST_MODIFIED = "last_modified";
		public static final String COLUMN_NAME_DAY_OF_WEEK = "day_of_week";

		public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
		public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
				" (" + COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
				COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + " INTEGER REFERENCES " + RingtoneContract.RingtoneEntry.TABLE_NAME + "(" + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID + ") ON DELETE CASCADE ON UPDATE CASCADE, " +
				COLUMN_NAME_LABEL + " TEXT, " +
				COLUMN_NAME_ENABLE + " INT, " +
				COLUMN_NAME_VIBRATE + " INT, " +
				COLUMN_NAME_TIME + " TEXT, " +
				COLUMN_NAME_DAY_OF_WEEK + " INT, " +
				COLUMN_NAME_LAST_MODIFIED + " INT " +
				")";
	}
}