package org.neige.wakeyouinmusic.android.database;

import android.provider.BaseColumns;

public final class RingtoneContract {

	public static abstract class RingtoneEntry implements BaseColumns {
		public static final String TABLE_NAME = "ringtone";
		public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_LABEL = "label";
		public static final String COLUMN_NAME_SHUFFLE = "shuffle";

		public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME +";";
		public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
				" (" + COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
				COLUMN_NAME_LABEL + " TEXT " +
				");";

		public static final String SQL_PATCH_VERSION_2 = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME_SHUFFLE + " INT DEFAULT 0;";

	}
}
