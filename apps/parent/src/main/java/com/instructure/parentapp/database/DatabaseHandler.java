/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.instructure.parentapp.models.CalendarWrapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

public class DatabaseHandler {

    private static final String DATABASE_NAME = "alarmDatabase";
    private static final String DATABASE_TABLE = "alarms";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ROWID = "_id";
    public static final String KEY_YEAR = "_year";
    public static final String KEY_MONTH = "_month";
    public static final String KEY_DAY = "_day";
    public static final String KEY_HOUR = "_hour";
    public static final String KEY_MINUTE = "_minute";
    public static final String KEY_ASSIGNMENT_ID = "_assignment_id";
    public static final String KEY_TITLE = "_title";
    public static final String KEY_SUBTITLE = "_sub_title";

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" + KEY_ROWID
                            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_YEAR + " INTEGER, " +  KEY_MONTH
                            + " INTEGER, " + KEY_DAY + " INTEGER, " + KEY_HOUR
                            + " INTEGER, " + KEY_MINUTE + " INTEGER, " + KEY_ASSIGNMENT_ID + " INTEGER, " + KEY_TITLE + " TEXT, "
                            + KEY_SUBTITLE + " TEXT);"

            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }




    }

    public DatabaseHandler(Context c) {
        ourContext = c;
    }

    public DatabaseHandler open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        ourHelper.close();
    }

    public long createAlarm(int year, int month, int day, int hour, int minute, long assignmentId, String title, String subTitle) {
        ContentValues values = new ContentValues();
        values.put(KEY_YEAR, year);
        values.put(KEY_MONTH, month);
        values.put(KEY_DAY, day);
        values.put(KEY_HOUR, hour);
        values.put(KEY_MINUTE, minute);
        values.put(KEY_ASSIGNMENT_ID, assignmentId);
        values.put(KEY_TITLE, title);
        values.put(KEY_SUBTITLE, subTitle);
        return ourDatabase.insert(DATABASE_TABLE, null, values);
    }

    public Cursor getData() {

        String[] columns = new String[] { KEY_ROWID, KEY_MONTH, KEY_DAY,
                KEY_HOUR, KEY_MINUTE };

        return ourDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);

    }
    public Integer deleteAlarm (Integer id)
    {
        return ourDatabase.delete(DATABASE_TABLE,
                KEY_ROWID + " = ? ",
                new String[]{Integer.toString(id)});
    }

    public boolean updateAlarm (Integer id, int year, int month, int day, int hour, int minute, long assignmentId, String title, String subTitle)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_YEAR, year);
        values.put(KEY_MONTH, month);
        values.put(KEY_DAY, day);
        values.put(KEY_HOUR, hour);
        values.put(KEY_MINUTE, minute);
        values.put(KEY_ASSIGNMENT_ID, assignmentId);
        values.put(KEY_TITLE, title);
        values.put(KEY_SUBTITLE, subTitle);
        ourDatabase.update(DATABASE_TABLE, values, KEY_ROWID + " = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public int getRowIdByAssignmentId(long assignmentId) {
        String[] columns = new String[]{KEY_ROWID};
        Cursor cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_ASSIGNMENT_ID + " = " + Long.toString(assignmentId), null, null, null, null);
        int rowId = -1;
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getColumnCount() > 0) {
                rowId = cursor.getInt(0);
            }
            cursor.close();
        }
        return rowId;
    }

    public Calendar getAlarmByAssignmentId(long assignmentId) {

        String[] columns = new String[] { KEY_ROWID, KEY_YEAR, KEY_MONTH, KEY_DAY,
                KEY_HOUR, KEY_MINUTE, KEY_ASSIGNMENT_ID };

        Cursor cursor =  ourDatabase.query(DATABASE_TABLE, columns, KEY_ASSIGNMENT_ID + " = " + Long.toString(assignmentId), null, null, null, null);
        if(cursor != null) {
            cursor.moveToFirst();
            if(cursor.getCount() == 0) {
                cursor.close();
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, cursor.getInt(1));
            calendar.set(Calendar.MONTH, cursor.getInt(2));
            calendar.set(Calendar.DAY_OF_MONTH, cursor.getInt(3));
            calendar.set(Calendar.HOUR_OF_DAY, cursor.getInt(4));
            calendar.set(Calendar.MINUTE, cursor.getInt(5));
            cursor.close();
            return calendar;
        }
        return null;
    }

    public ArrayList<CalendarWrapper> getAllAlarms() {

        String[] columns = new String[] { KEY_ROWID, KEY_YEAR, KEY_MONTH, KEY_DAY,
                KEY_HOUR, KEY_MINUTE, KEY_ASSIGNMENT_ID, KEY_TITLE, KEY_SUBTITLE };

        ArrayList<CalendarWrapper> calendars = new ArrayList<>();

        Cursor cursor =  ourDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
        if(cursor != null) {
            cursor.moveToFirst();
            if(cursor.getCount() == 0) {
                return null;
            }
            while (!cursor.isAfterLast()) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, cursor.getInt(1));
                calendar.set(Calendar.MONTH, cursor.getInt(2));
                calendar.set(Calendar.DAY_OF_MONTH, cursor.getInt(3));
                calendar.set(Calendar.HOUR_OF_DAY, cursor.getInt(4));
                calendar.set(Calendar.MINUTE, cursor.getInt(5));
                long assignmentId = cursor.getInt(6);
                String title = cursor.getString(7);
                String subTitle = cursor.getString(8);

                //only add the calendar if the date is after today
                Calendar today = Calendar.getInstance();
                if(calendar.after(today)) {
                    calendars.add(new CalendarWrapper(calendar, assignmentId, title, subTitle));
                }
                cursor.moveToNext();
            }
            cursor.close();
            return calendars;
        }

        return null;
    }
}
