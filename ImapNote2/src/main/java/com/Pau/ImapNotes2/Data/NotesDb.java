package com.Pau.ImapNotes2.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class NotesDb {

    private static final int NOTES_VERSION = 3;
    private static final String TAG = "IN_NotesDb";

    private static final String COL_TITLE = "title";
    private static final String COL_DATE = "date";
    private static final String COL_NUMBER = "number";
    private static final String COL_ACCOUNT_NAME = "accountname";
    private static final String TABLE_NAME = "notesTable";
    private static final String DATABASE_NAME = "NotesDb";

    private static final String CREATE_NOTES_DB = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " (pk integer primary key autoincrement, "
            + COL_TITLE + " text not null, "
            + COL_DATE + " text not null, "
            + COL_NUMBER + " text not null, "
            + COL_ACCOUNT_NAME + " text not null);";

    private SQLiteDatabase notesDb;
    @NonNull
    private final NotesDbHelper defaultHelper;

    public NotesDb(Context applicationContext) {
        this.defaultHelper = new NotesDbHelper(applicationContext);

    }

    public void OpenDb() {
        this.notesDb = this.defaultHelper.getWritableDatabase();

    }

    public void CloseDb() {
        this.notesDb.close();

    }

    public void InsertANoteInDb(@NonNull OneNote noteElement, String accountname) {
        ContentValues tableRow = new ContentValues();
        tableRow.put(COL_TITLE, noteElement.GetTitle());
        tableRow.put(COL_DATE, noteElement.GetDate());
        tableRow.put(COL_NUMBER, noteElement.GetUid());
        tableRow.put(COL_ACCOUNT_NAME, accountname);
        this.notesDb.insert(TABLE_NAME, null, tableRow);
        //Log.d(TAG, "note inserted");
    }

    public void DeleteANote(String number, String accountname) {
        this.notesDb.execSQL("delete from notesTable where number = '" + number +
                "' and accountname = '" + accountname + "'");
    }

    public void UpdateANote(String olduid, String newuid, String accountname) {
        /* TODO: use sql template and placeholders instead of string concatenation.
                 */
        String req = "update notesTable set number='" + newuid + "' where number='-" + olduid + "' and accountname='" + accountname + "'";
        this.notesDb.execSQL(req);
    }

    public String GetDate(String uid, String accountname) {
       /* Returns a string representing the modification time of the note.
          TODO: use date class.
        */
        String selectQuery = "select date from notesTable where number = '" + uid + "' and accountname='" + accountname + "'";
        try (Cursor c = this.notesDb.rawQuery(selectQuery, null)) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        }
        return "";
    }

    public String GetTempNumber(String accountname) {
        String selectQuery = "select case when cast(max(abs(number)+1) as int) > 0 then cast(max(abs(number)+1) as int)*-1 else '-1' end from notesTable where number < '0' and accountname='" + accountname + "'";
        try (Cursor c = this.notesDb.rawQuery(selectQuery, null)) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        }
        return "-1";
    }

    public void GetStoredNotes(@NonNull ArrayList<OneNote> noteList,
                               @NonNull String accountName) {
        noteList.clear();
        Date date = null;
        try (Cursor resultPointer = this.notesDb.query(TABLE_NAME, null, "accountname = ?",
                new String[]{accountName}, null, null, "date DESC")) {

            if (resultPointer.moveToFirst()) {
                int titleIndex = resultPointer.getColumnIndex(COL_TITLE);
                //int bodyIndex = resultPointer.getColumnIndex("body");
                int dateIndex = resultPointer.getColumnIndex(COL_DATE);
                int numberIndex = resultPointer.getColumnIndex(COL_NUMBER);
                //int positionIndex = resultPointer.getColumnIndex("position");
                //int colorIndex = resultPointer.getColumnIndex("color");
                do {
                    //String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
                    //SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ROOT);
                    try {
                        date = Utilities.internalDateFormat.parse(resultPointer.getString(dateIndex));
                    } catch (ParseException e) {
                        Log.d(TAG, "Parsing data from database failed: " + e.getMessage());
                    }
                    //DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this.ctx);
                    //String sdate = dateFormat.format(date);
                    String sdate = DateFormat.getDateTimeInstance().format(date);

                    noteList.add(new OneNote(resultPointer.getString(titleIndex),
                            sdate,
                            resultPointer.getString(numberIndex)));
                } while (resultPointer.moveToNext());
            }
        }

    }

    public void ClearDb(String accountname) {
        this.notesDb.execSQL("delete from notesTable where accountname = '" + accountname + "'");

    }

    /**
     * Database helper that creates and maintains the SQLite database.
     */

    private static class NotesDbHelper extends SQLiteOpenHelper {

        NotesDbHelper(Context currentApplicationContext) {
            super(currentApplicationContext, NotesDb.DATABASE_NAME, null, NotesDb.NOTES_VERSION);
        }

        @Override
        public void onCreate(@NonNull SQLiteDatabase _db) {
            _db.execSQL(NotesDb.CREATE_NOTES_DB);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            //Log.d(TAG,"onUpgrade from:"+oldVersion+" to:"+newVersion);
            for (int i = oldVersion; i < newVersion; i++) {
                PATCHES[i - 2].apply(_db);
            }
        }

        private static class Patch {
            public void apply(SQLiteDatabase _db) {
            }
        }

        private static final Patch[] PATCHES = new Patch[]{
                new Patch() {
                    public void apply(@NonNull SQLiteDatabase _db) {
                        //Log.d(TAG,"upgrade: v2 to v3");
                        _db.execSQL("Drop table notesTable;");
                        _db.execSQL(NotesDb.CREATE_NOTES_DB);
                    }
                }
/*
           ,new Patch() {
              public void apply(SQLiteDatabase _db) {
                Log.d(TAG,"upgrade: v3 to v4");
                _db.execSQL("Drop table notesTable;");
                _db.execSQL(NotesDb.CREATE_NOTES_DB);
              }
           }
*/
        };
    }
}
