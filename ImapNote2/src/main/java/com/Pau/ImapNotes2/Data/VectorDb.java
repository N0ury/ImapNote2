package com.Pau.ImapNotes2.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by kj on 2017-01-09 10:28.
 * <p>
 * This class manages the tables used for vector clocks and other data associated with automatic
 * conflict resolution and merging.
 */

class VectorDb {


    private final Db db;
    private final FileTable fileTable = new FileTable();
    private final VectorTable vectorTable = new VectorTable();

    public VectorDb(@NonNull Db db) {
        this.db = db;

    }

    public void CreateTables(@NonNull SQLiteDatabase db) {
        fileTable.CreateTable(db);
        vectorTable.CreateTable(db);
    }


    class VectorTable {

        private static final String TAG = "IN_FileTable";

        private static final String COL_FILE_PATH = "filepath";
        private static final String COL_CLOCK= "clock";
        private static final String COL_ACCOUNT_NAME = "accountname";
        private static final String TABLE_NAME = "vector";
        //private static final String DATABASE_NAME = "NotesDb";

        private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + " ("
                + COL_FILE_PATH + " text not null, "
                + COL_CLOCK + " text not null, "
                + COL_ACCOUNT_NAME + " text not null, "
                + " PRIMARY KEY (" + COL_FILE_PATH + ", " + COL_ACCOUNT_NAME + "));";


        VectorTable() {

        }

        void CreateTable(@NonNull SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        public void Insert(@NonNull File file,
                           @NonNull String accountname,
                           int clock) {
            ContentValues tableRow = new ContentValues();
            tableRow.put(COL_FILE_PATH, file.getName());
            tableRow.put(COL_ACCOUNT_NAME, accountname);
            tableRow.put(COL_CLOCK, clock);
            db.notesDb.insertWithOnConflict(TABLE_NAME, null, tableRow,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
/*
        // TODO: Should use place holders instead of string concatenation.
        public void Delete(@NonNull File file,
                           @NonNull String accountname) {
            db.notesDb.execSQL("delete from " + TABLE_NAME + " where " + COL_FILE_PATH + " = '" + file.getAbsolutePath() +
                    "' and accountname = '" + accountname + "'");
        }*/


    }

    class FileTable {

        private static final String TAG = "IN_FileTable";

        private static final String COL_FILE_PATH = "filepath";
        private static final String COL_MTIME = "mtime";
        private static final String COL_ACCOUNT_NAME = "accountname";
        private static final String TABLE_NAME = "file";
        //private static final String DATABASE_NAME = "NotesDb";

        private static final String CREATE_FILE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + " (pk integer primary key autoincrement, "
                + COL_FILE_PATH + " text not null, "
                + COL_MTIME + " text not null, "
                + COL_ACCOUNT_NAME + " text not null);";


        FileTable() {

        }

        void CreateTable(@NonNull SQLiteDatabase db) {
            db.execSQL(CREATE_FILE_TABLE);
        }

        public void Insert(@NonNull File file,
                           @NonNull String accountname) {
            ContentValues tableRow = new ContentValues();
            tableRow.put(COL_FILE_PATH, file.getAbsolutePath());
            tableRow.put(COL_MTIME, file.lastModified());
            tableRow.put(COL_ACCOUNT_NAME, accountname);
            db.notesDb.insert(TABLE_NAME, null, tableRow);
            //Log.d(TAG, "note inserted");
        }

        // TODO: Should use place holders instead of string concatenation.
        public void Delete(@NonNull File file,
                           @NonNull String accountname) {
            db.notesDb.execSQL("delete from " + TABLE_NAME + " where " + COL_FILE_PATH + " = '" + file.getAbsolutePath() +
                    "' and accountname = '" + accountname + "'");
        }


        public long GetMTime(@NonNull String filePath,
                             @NonNull String accountname) {
            String selectQuery = "select mtime from " + TABLE_NAME +
                    " where " + COL_FILE_PATH + " = '" + filePath + "' " + " " +
                    " and accountname = '" + accountname + "'";
            try (Cursor c = db.notesDb.rawQuery(selectQuery, null)) {
                if (c.moveToFirst()) {
                    return c.getLong(Cursor.FIELD_TYPE_NULL);
                }
            }
            return 0;
        }

    }

}
