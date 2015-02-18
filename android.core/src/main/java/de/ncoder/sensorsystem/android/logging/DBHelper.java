/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Niko Fink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.ncoder.sensorsystem.android.logging;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "sensor.db";
	private static final int DATABASE_VERSION = 2;

	public static final String TABLE_LOG = "log";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SOURCE = "source";
	public static final String COLUMN_KEY = "key";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_VALUE = "value";
	public static final String COLUMN_COUNT = "entry_count";

	private static final String DATABASE_CREATE =
			"CREATE TABLE " + TABLE_LOG + " ( "
					+ COLUMN_ID + " integer primary key autoincrement, "
					+ COLUMN_SOURCE + " text, "
					+ COLUMN_KEY + " text, "
					+ COLUMN_TIMESTAMP + " integer, "
					+ COLUMN_VALUE + " text "
					+ ");";

	private static final String SELECT_LOG_ENTRY_COUNT =
			"SELECT Count(*) AS " + COLUMN_COUNT + " FROM " + TABLE_LOG;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
		onCreate(db);
	}

	public int getLogEntryCount() {
		Cursor cursor = getReadableDatabase().rawQuery(SELECT_LOG_ENTRY_COUNT, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));
			} else {
				return 0;
			}
		} finally {
			cursor.close();
		}
	}
}
