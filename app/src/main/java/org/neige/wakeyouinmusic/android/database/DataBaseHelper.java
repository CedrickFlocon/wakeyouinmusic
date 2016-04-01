package org.neige.wakeyouinmusic.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.models.DayOfWeek;
import org.neige.wakeyouinmusic.android.models.DeezerRingtone;
import org.neige.wakeyouinmusic.android.models.DefaultRingtone;
import org.neige.wakeyouinmusic.android.models.PlaylistRingtone;
import org.neige.wakeyouinmusic.android.models.Ringtone;
import org.neige.wakeyouinmusic.android.models.SpotifyRingtone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DataBaseHelper";
	private static final String DATABASE_NAME = "wakeyouinmusic.db";
	private static final int DATABASE_VERSION = 2;

	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

	public DataBaseHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, RingtoneContract.RingtoneEntry.SQL_CREATE_TABLE);
		db.execSQL(RingtoneContract.RingtoneEntry.SQL_CREATE_TABLE);

		Log.i(TAG, DeezerRingtoneContract.DeezerRingtoneEntry.SQL_CREATE_TABLE);
		db.execSQL(DeezerRingtoneContract.DeezerRingtoneEntry.SQL_CREATE_TABLE);

		Log.i(TAG, DeezerRingtoneContract.DeezerRingtoneEntry.SQL_CREATE_INDEX);
		db.execSQL(DeezerRingtoneContract.DeezerRingtoneEntry.SQL_CREATE_INDEX);

		Log.i(TAG, SpotifyRingtoneContract.SpotifyRingtoneEntry.SQL_CREATE_TABLE);
		db.execSQL(SpotifyRingtoneContract.SpotifyRingtoneEntry.SQL_CREATE_TABLE);

		Log.i(TAG, SpotifyRingtoneContract.SpotifyRingtoneEntry.SQL_CREATE_INDEX);
		db.execSQL(SpotifyRingtoneContract.SpotifyRingtoneEntry.SQL_CREATE_INDEX);

		Log.i(TAG, DefaultRingtoneContract.DefaultRingtoneContractEntry.SQL_CREATE_TABLE);
		db.execSQL(DefaultRingtoneContract.DefaultRingtoneContractEntry.SQL_CREATE_TABLE);

		Log.i(TAG, DefaultRingtoneContract.DefaultRingtoneContractEntry.SQL_CREATE_INDEX);
		db.execSQL(DefaultRingtoneContract.DefaultRingtoneContractEntry.SQL_CREATE_INDEX);

		Log.i(TAG, AlarmContract.AlarmEntry.SQL_CREATE_TABLE);
		db.execSQL(AlarmContract.AlarmEntry.SQL_CREATE_TABLE);

		Log.i(TAG, RingtoneContract.RingtoneEntry.SQL_PATCH_VERSION_2);
		db.execSQL(RingtoneContract.RingtoneEntry.SQL_PATCH_VERSION_2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		for (int patch = oldVersion; patch <= oldVersion; patch++){
			if (patch == 1){
				Log.i(TAG, RingtoneContract.RingtoneEntry.SQL_PATCH_VERSION_2);
				db.execSQL(RingtoneContract.RingtoneEntry.SQL_PATCH_VERSION_2);
			}
		}
	}

	public List<Alarm> selectAlarms() {
		List<Alarm> alarms = new ArrayList<>();
		String query = "SELECT " +
				AlarmContract.AlarmEntry.TABLE_NAME + "." + AlarmContract.AlarmEntry.COLUMN_NAME_ID + " AS " + AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_ID + ", " +
				AlarmContract.AlarmEntry.TABLE_NAME + "." + AlarmContract.AlarmEntry.COLUMN_NAME_LABEL + " AS " + AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_LABEL + ", " +
				AlarmContract.AlarmEntry.TABLE_NAME + "." + AlarmContract.AlarmEntry.COLUMN_NAME_ENABLE + " AS " + AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_ENABLE +", " +
				AlarmContract.AlarmEntry.TABLE_NAME + "." + AlarmContract.AlarmEntry.COLUMN_NAME_VIBRATE + " AS " + AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_VIBRATE + ", " +
				AlarmContract.AlarmEntry.TABLE_NAME + "." + AlarmContract.AlarmEntry.COLUMN_NAME_TIME + " AS " + AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_TIME + ", " +
				AlarmContract.AlarmEntry.TABLE_NAME + "." + AlarmContract.AlarmEntry.COLUMN_NAME_DAY_OF_WEEK + " AS " + AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_DAY_OF_WEEK + ", " +
				AlarmContract.AlarmEntry.TABLE_NAME + "." + AlarmContract.AlarmEntry.COLUMN_NAME_LAST_MODIFIED + " AS " + AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_LAST_MODIFIED + ", " +

				RingtoneContract.RingtoneEntry.TABLE_NAME + "." + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID + " AS " + RingtoneContract.RingtoneEntry.TABLE_NAME + "_" + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID + ", " +
				RingtoneContract.RingtoneEntry.TABLE_NAME + "." + RingtoneContract.RingtoneEntry.COLUMN_NAME_LABEL + " AS " + RingtoneContract.RingtoneEntry.TABLE_NAME + "_" + RingtoneContract.RingtoneEntry.COLUMN_NAME_LABEL + ", " +
				RingtoneContract.RingtoneEntry.TABLE_NAME + "." + RingtoneContract.RingtoneEntry.COLUMN_NAME_SHUFFLE + " AS " + RingtoneContract.RingtoneEntry.TABLE_NAME + "_" + RingtoneContract.RingtoneEntry.COLUMN_NAME_SHUFFLE + ", " +

				DeezerRingtoneContract.DeezerRingtoneEntry.TABLE_NAME + "." + DeezerRingtoneContract.DeezerRingtoneEntry.COLUMN_NAME_DEEZER_PLAYLIST_ID + " AS " + DeezerRingtoneContract.DeezerRingtoneEntry.TABLE_NAME + "_" + DeezerRingtoneContract.DeezerRingtoneEntry.COLUMN_NAME_DEEZER_PLAYLIST_ID + ", " +
				SpotifyRingtoneContract.SpotifyRingtoneEntry.TABLE_NAME + "." + SpotifyRingtoneContract.SpotifyRingtoneEntry.COLUMN_NAME_SPOTIFY_PLAYLIST_ID + " AS " + SpotifyRingtoneContract.SpotifyRingtoneEntry.TABLE_NAME + "_" + SpotifyRingtoneContract.SpotifyRingtoneEntry.COLUMN_NAME_SPOTIFY_PLAYLIST_ID + ", " +
				DefaultRingtoneContract.DefaultRingtoneContractEntry.TABLE_NAME + "." + DefaultRingtoneContract.DefaultRingtoneContractEntry.COLUMN_NAME_URI + " AS " + DefaultRingtoneContract.DefaultRingtoneContractEntry.TABLE_NAME + "_" + DefaultRingtoneContract.DefaultRingtoneContractEntry.COLUMN_NAME_URI +

		" FROM " + AlarmContract.AlarmEntry.TABLE_NAME +
			   " LEFT JOIN " + RingtoneContract.RingtoneEntry.TABLE_NAME + " ON " + RingtoneContract.RingtoneEntry.TABLE_NAME + "." + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID + " = " + AlarmContract.AlarmEntry.TABLE_NAME + "." + AlarmContract.AlarmEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID +
			   " LEFT JOIN " + DeezerRingtoneContract.DeezerRingtoneEntry.TABLE_NAME + " ON " + DeezerRingtoneContract.DeezerRingtoneEntry.TABLE_NAME + "." + DeezerRingtoneContract.DeezerRingtoneEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + " = " + RingtoneContract.RingtoneEntry.TABLE_NAME + "." + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID +
			   " LEFT JOIN " + SpotifyRingtoneContract.SpotifyRingtoneEntry.TABLE_NAME + " ON " + SpotifyRingtoneContract.SpotifyRingtoneEntry.TABLE_NAME+ "." + SpotifyRingtoneContract.SpotifyRingtoneEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + " = " + RingtoneContract.RingtoneEntry.TABLE_NAME + "." + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID +
			   " LEFT JOIN " + DefaultRingtoneContract.DefaultRingtoneContractEntry.TABLE_NAME + " ON " + DefaultRingtoneContract.DefaultRingtoneContractEntry.TABLE_NAME + "." + DefaultRingtoneContract.DefaultRingtoneContractEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + " = " + RingtoneContract.RingtoneEntry.TABLE_NAME + "." + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID +
				" ORDER BY " + AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_ID;
		Log.i(TAG, query);

		Cursor cursor = this.getReadableDatabase().rawQuery(query, null);
		while(cursor.moveToNext()){

			//Ringtone
			Ringtone ringtone;
			long deezerPlaylistId;
			String spotifyPlaylistId;
			String defaultRingtoneUri;
			boolean shuffle = cursor.getInt(cursor.getColumnIndex(RingtoneContract.RingtoneEntry.TABLE_NAME + "_" + RingtoneContract.RingtoneEntry.COLUMN_NAME_SHUFFLE)) == 1;
			if ((deezerPlaylistId = cursor.getLong(cursor.getColumnIndex(DeezerRingtoneContract.DeezerRingtoneEntry.TABLE_NAME + "_" +
					DeezerRingtoneContract.DeezerRingtoneEntry.COLUMN_NAME_DEEZER_PLAYLIST_ID))) > 0){
				ringtone = new DeezerRingtone();
				((DeezerRingtone)ringtone).setDeezerId(deezerPlaylistId);
				((DeezerRingtone)ringtone).setShuffle(shuffle);
			}else if ((spotifyPlaylistId = cursor.getString(cursor.getColumnIndex( SpotifyRingtoneContract.SpotifyRingtoneEntry.TABLE_NAME + "_" + SpotifyRingtoneContract.SpotifyRingtoneEntry.COLUMN_NAME_SPOTIFY_PLAYLIST_ID))) != null){
				ringtone = new SpotifyRingtone();
				((SpotifyRingtone)ringtone).setSpotifyId(spotifyPlaylistId);
				((SpotifyRingtone)ringtone).setShuffle(shuffle);
			}else if ((defaultRingtoneUri = cursor.getString(cursor.getColumnIndex(DefaultRingtoneContract.DefaultRingtoneContractEntry.TABLE_NAME + "_" + DefaultRingtoneContract.DefaultRingtoneContractEntry.COLUMN_NAME_URI))) != null){
				ringtone = new DefaultRingtone();
				((DefaultRingtone)ringtone).setUri(defaultRingtoneUri);
			}else {
				throw new RuntimeException("Incompatible Ringtone type");
			}
			ringtone.setId(cursor.getInt(cursor.getColumnIndex(RingtoneContract.RingtoneEntry.TABLE_NAME + "_" + RingtoneContract.RingtoneEntry.COLUMN_NAME_ID)));
			ringtone.setTitle(cursor.getString(cursor.getColumnIndex(RingtoneContract.RingtoneEntry.TABLE_NAME + "_" + RingtoneContract.RingtoneEntry.COLUMN_NAME_LABEL)));

			//Alarm
			Alarm alarm = new Alarm();
			alarm.setId(cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_ID)));
			alarm.setLabel(cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_LABEL)));
			alarm.setVibrate(cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_VIBRATE)) == 1);
			alarm.setEnable(cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_ENABLE)) == 1);
			alarm.setDayOfWeeks(bitwiseToEnumSet(DayOfWeek.class, cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_DAY_OF_WEEK))));
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_TIME))));
				alarm.setTime(cal);
			} catch (ParseException e) {
				Log.e(TAG, "Can't parse time : " + cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_TIME)), e);
			}
			Calendar c = new GregorianCalendar();
			c.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(AlarmContract.AlarmEntry.TABLE_NAME + "_" + AlarmContract.AlarmEntry.COLUMN_NAME_LAST_MODIFIED)));
			alarm.setLastModified(c);

			alarm.setRingtone(ringtone);

			alarms.add(alarm);
		}
		cursor.close();
		return alarms;
	}

	public void insertAlarm(Alarm alarm){
		SQLiteDatabase sqLiteOpenHelper = this.getWritableDatabase();

		long ringtoneId = sqLiteOpenHelper.insert(RingtoneContract.RingtoneEntry.TABLE_NAME, null, getRingtoneContentValue(alarm.getRingtone()));

		ContentValues ringtoneAbstractValue = getAbstractRingtoneContentValue(alarm.getRingtone(), ringtoneId);
		if (alarm.getRingtone() instanceof DeezerRingtone){
			sqLiteOpenHelper.insert(DeezerRingtoneContract.DeezerRingtoneEntry.TABLE_NAME, null, ringtoneAbstractValue);
		}else if (alarm.getRingtone() instanceof SpotifyRingtone){
			sqLiteOpenHelper.insert(SpotifyRingtoneContract.SpotifyRingtoneEntry.TABLE_NAME, null, ringtoneAbstractValue);
		}else if (alarm.getRingtone() instanceof DefaultRingtone) {
			sqLiteOpenHelper.insert(DefaultRingtoneContract.DefaultRingtoneContractEntry.TABLE_NAME, null, ringtoneAbstractValue);
		}else {
			throw new UnsupportedOperationException("Not implemented ringtone type : " + alarm.getRingtone().getClass().getSimpleName());
		}

		alarm.setId(this.getWritableDatabase().insert(AlarmContract.AlarmEntry.TABLE_NAME,null,getAlarmContentValue(alarm,ringtoneId)));
	}

	public void updateAlarm(Alarm alarm){
		SQLiteDatabase sqLiteOpenHelper = this.getWritableDatabase();

		deleteRingtone(alarm.getRingtone());
		long ringtoneId = sqLiteOpenHelper.insert(RingtoneContract.RingtoneEntry.TABLE_NAME, null, getRingtoneContentValue(alarm.getRingtone()));

		ContentValues ringtoneAbstractValue = getAbstractRingtoneContentValue(alarm.getRingtone(), ringtoneId);
		if (alarm.getRingtone() instanceof DeezerRingtone){
			sqLiteOpenHelper.insert(DeezerRingtoneContract.DeezerRingtoneEntry.TABLE_NAME, null, ringtoneAbstractValue);
		}else if (alarm.getRingtone() instanceof SpotifyRingtone) {
			sqLiteOpenHelper.insert(SpotifyRingtoneContract.SpotifyRingtoneEntry.TABLE_NAME, null, ringtoneAbstractValue);
		}else if (alarm.getRingtone() instanceof DefaultRingtone) {
			sqLiteOpenHelper.insert(DefaultRingtoneContract.DefaultRingtoneContractEntry.TABLE_NAME, null, ringtoneAbstractValue);
		}else {
			throw new UnsupportedOperationException("Not implemented ringtone type : " + alarm.getRingtone().getClass().getSimpleName());
		}

		sqLiteOpenHelper.update(AlarmContract.AlarmEntry.TABLE_NAME, getAlarmContentValue(alarm,ringtoneId), AlarmContract.AlarmEntry.COLUMN_NAME_ID + " = " + alarm.getId(), null);
	}

	private ContentValues getRingtoneContentValue(Ringtone ringtone){
		ContentValues ringtoneValue = new ContentValues();
		ringtoneValue.put(RingtoneContract.RingtoneEntry.COLUMN_NAME_LABEL, ringtone.getTitle());
		if (ringtone instanceof PlaylistRingtone){
			ringtoneValue.put(RingtoneContract.RingtoneEntry.COLUMN_NAME_SHUFFLE,((PlaylistRingtone)ringtone).isShuffle());
		}else {
			ringtoneValue.put(RingtoneContract.RingtoneEntry.COLUMN_NAME_SHUFFLE, 0);
		}

		return ringtoneValue;
	}

	private ContentValues getAbstractRingtoneContentValue(Ringtone abstractRingtone, long ringtoneId){
		ContentValues ringtoneAbstractValue = new ContentValues();
		ringtoneAbstractValue.put(DeezerRingtoneContract.DeezerRingtoneEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID, ringtoneId);
		if (abstractRingtone instanceof DeezerRingtone){
			ringtoneAbstractValue.put(DeezerRingtoneContract.DeezerRingtoneEntry.COLUMN_NAME_DEEZER_PLAYLIST_ID,((DeezerRingtone)abstractRingtone).getDeezerId());
		}else if (abstractRingtone instanceof SpotifyRingtone) {
			ringtoneAbstractValue.put(SpotifyRingtoneContract.SpotifyRingtoneEntry.COLUMN_NAME_SPOTIFY_PLAYLIST_ID,((SpotifyRingtone)abstractRingtone).getSpotifyId());
		}else if (abstractRingtone instanceof DefaultRingtone) {
			ringtoneAbstractValue.put(DefaultRingtoneContract.DefaultRingtoneContractEntry.COLUMN_NAME_URI, ((DefaultRingtone)abstractRingtone).getUri());
		}else {
			throw new UnsupportedOperationException("Not implemented ringtone type : " + abstractRingtone.getClass().getSimpleName());
		}
		return ringtoneAbstractValue;
	}

	private ContentValues getAlarmContentValue(Alarm alarm, long ringtoneId){
		ContentValues alarmValue = new ContentValues();
		alarmValue.put(AlarmContract.AlarmEntry.COLUMN_NAME_LABEL, alarm.getLabel());
		alarmValue.put(AlarmContract.AlarmEntry.COLUMN_NAME_VIBRATE, alarm.isVibrate());
		alarmValue.put(AlarmContract.AlarmEntry.COLUMN_NAME_ENABLE, alarm.isEnable());
		alarmValue.put(AlarmContract.AlarmEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID, ringtoneId);
		alarmValue.put(AlarmContract.AlarmEntry.COLUMN_NAME_TIME, simpleDateFormat.format(alarm.getTime().getTime()));
		alarmValue.put(AlarmContract.AlarmEntry.COLUMN_NAME_DAY_OF_WEEK, enumSetToBitwise(alarm.getDayOfWeeks()));
		alarmValue.put(AlarmContract.AlarmEntry.COLUMN_NAME_LAST_MODIFIED, alarm.getLastModified().getTimeInMillis());
		return alarmValue;
	}

	public void deleteAlarm(Alarm alarm){
		deleteRingtone(alarm.getRingtone());
		SQLiteDatabase sqLiteOpenHelper = this.getWritableDatabase();
		sqLiteOpenHelper.delete(AlarmContract.AlarmEntry.TABLE_NAME, AlarmContract.AlarmEntry.COLUMN_NAME_ID + " = " + alarm.getId(), null);
	}

	public void deleteRingtone(Ringtone ringtone){
		SQLiteDatabase sqLiteOpenHelper = this.getWritableDatabase();
		sqLiteOpenHelper.delete(DeezerRingtoneContract.DeezerRingtoneEntry.TABLE_NAME, DeezerRingtoneContract.DeezerRingtoneEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + " = " + ringtone.getId(), null);
		sqLiteOpenHelper.delete(SpotifyRingtoneContract.SpotifyRingtoneEntry.TABLE_NAME, SpotifyRingtoneContract.SpotifyRingtoneEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + " = " + ringtone.getId(), null);
		sqLiteOpenHelper.delete(DefaultRingtoneContract.DefaultRingtoneContractEntry.TABLE_NAME, DefaultRingtoneContract.DefaultRingtoneContractEntry.COLUMN_NAME_RINGTONE_FOREIGN_KEY_ID + " = " + ringtone.getId(), null);
		sqLiteOpenHelper.delete(RingtoneContract.RingtoneEntry.TABLE_NAME, RingtoneContract.RingtoneEntry.COLUMN_NAME_ID + " = " + ringtone.getId(), null);
	}

	private static <T extends Enum<T>> int enumSetToBitwise(EnumSet<T> enumSet){
		int bitwise = 0;
		for(Enum<T> enumValue : enumSet){
			bitwise += (int) Math.pow(2,enumValue.ordinal());
		}
		return bitwise;
	}

	private static <T extends Enum<T>> EnumSet bitwiseToEnumSet(Class<T> classType, int bitwise){
		EnumSet<T> enumSet = EnumSet.noneOf(classType);
		for (T enumValue : EnumSet.allOf(classType)){
			if ((bitwise & (int) Math.pow(2,enumValue.ordinal())) == (int) Math.pow(2,enumValue.ordinal()))
				enumSet.add(enumValue);
		}
		return enumSet;
	}

}
