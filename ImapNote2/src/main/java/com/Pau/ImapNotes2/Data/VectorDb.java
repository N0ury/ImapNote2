package com.Pau.ImapNotes2.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;


/**
 * Created by kj on 2017-01-09 10:28.
 * <p>
 * This class manages the tables used for vector clocks and other data associated with automatic
 * conflict resolution and merging.
 */

class VectorDb {


    private final Db db;
    private final FileTable fileTable;
    private final VectorTable vectorTable;

    public VectorDb(@NonNull Db db) {
        this.db = db;
        vectorTable = new VectorTable(db.notesDb);
        fileTable = new FileTable(db.notesDb);
    }

/*
    public void CreateTables(@NonNull SQLiteDatabase db) {
        fileTable.CreateTable(db);
        vectorTable.CreateTable(db);
    }
*/


    class VectorTable {

        private static final String TAG = "IN_VectorTable";

        private static final String COL_FILE_PATH = "filepath";
        private static final String COL_CLOCK = "clock";
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


        VectorTable(SQLiteDatabase db) {
            CreateTable(db);
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


        void IncrementVectorClock(String relativeFile,
                                  String acountName) {

        }
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


        FileTable(SQLiteDatabase db) {
            CreateTable(db);
        }


        void CreateTable(@NonNull SQLiteDatabase db) {
            db.execSQL(CREATE_FILE_TABLE);
        }

        /**
         * Update the mtime in the database and increment the vetor clock for this device.
         *
         * @param relativeFile
         * @param lastModified
         * @param accountName
         */
        private void UpdateLastModified(String relativeFile,
                                        Long lastModified,
                                        String accountName) {
            ContentValues newMTime = new ContentValues();
            newMTime.put(COL_MTIME, lastModified);

            db.notesDb.update(TABLE_NAME, newMTime,
                    "? = ? and ? = ?",
                    new String[]{COL_FILE_PATH, relativeFile, COL_ACCOUNT_NAME, accountName});
        }

        public void Insert(@NonNull String relativeFile,
                           Long lastModified,
                           @NonNull String accountname,
                           @NonNull File accountDir) {
            ContentValues tableRow = new ContentValues();
            tableRow.put(COL_FILE_PATH, relativeFile);
            tableRow.put(COL_MTIME, lastModified);
            tableRow.put(COL_ACCOUNT_NAME, accountname);
            db.notesDb.insert(TABLE_NAME, null, tableRow);
            Log.d(TAG, "note inserted");
        }



        // TODO: Should use place holders instead of string concatenation.
        public void Delete(@NonNull File file,
                           @NonNull String accountname) {
            db.notesDb.execSQL("delete from " + TABLE_NAME + " where " + COL_FILE_PATH + " = '" + file.getAbsolutePath() +
                    "' and accountname = '" + accountname + "'");
        }


        public long GetMTime(@NonNull String relativeFilePath,
                             @NonNull String accountname) {
            try (Cursor c = GetRecord(relativeFilePath, accountname)) {
                return (c == null) ? 0 : c.getLong(c.getColumnIndex(COL_MTIME));
            }
        }
/*

        public long GetMTime(@NonNull String relativeFilePath,
                             @NonNull String accountname) {
            String selectQuery = "select mtime from " + TABLE_NAME +
                    " where " + COL_FILE_PATH + " = '" + relativeFilePath + "' " + " " +
                    " and accountname = '" + accountname + "'";
            try (Cursor c = db.notesDb.rawQuery(selectQuery, null)) {
                if (c.moveToFirst()) {
                    return c.getLong(Cursor.FIELD_TYPE_NULL);
                }
            }
            return 0;
        }

*/

        public Cursor GetRecord(@NonNull String relativeFilePath,
                                @NonNull String accountname) {
            String selectQuery = "select " +
                    COL_ACCOUNT_NAME + ", " + COL_FILE_PATH + ", " + COL_MTIME +
                    " from " + TABLE_NAME +
                    " where " + COL_FILE_PATH + " = '" + relativeFilePath + "' " + " " +
                    " and " + COL_ACCOUNT_NAME + " = '" + accountname + "'";
            Cursor c = db.notesDb.rawQuery(selectQuery, null);
            return c.moveToFirst() ? c : null;
        }


    }


    /**
     * Update the mtime in the database and increment the vetor clock for this device.
     * TODO: Use a transaction to ensure that either the mtime and vector succeed or neither.
     *
     * @param relativeFile
     * @param lastModified
     * @param accountName
     */
    private void Update(String relativeFile,
                        Long lastModified,
                        String accountName) {
        fileTable.UpdateLastModified(relativeFile, lastModified, accountName);
        vectorTable.IncrementVectorClock(relativeFile, accountName);
    }

    /**
     * Enumerate the file in the given directory and update the mtime and vector clock in the
     * database.
     *
     * @param accountName
     * @param accountRoot
     */
    public void UpdateAccount(String accountName,
                              File accountRoot) {
        for (File file : accountRoot.listFiles()) {
            UpdateFile(file, accountName, accountRoot);
        }
    }

    public void UpdateFile(File file,
                           String accountName,
                           File accountRoot) {

        String relativeFile = RelativePath(accountRoot, file);
        try (Cursor c = fileTable.GetRecord(RelativePath(accountRoot, file), accountName)) {
            if (c == null) {
                // Add new record
                fileTable.Insert(relativeFile, file.lastModified(), accountName, accountRoot);
            } else {
                // Update existing record by incrementing the vector
                Update(relativeFile, file.lastModified(), accountName);
            }
        }
    }

    /**
     * See http://www.java2s.com/Tutorial/Java/0180__File/Getrelativepath.htm
     *
     * @param base
     * @param file
     * @return
     */
    public String RelativePath(final File base, final File file) {
        final int rootLength = base.getAbsolutePath().length();
        final String absFileName = file.getAbsolutePath();
        return absFileName.substring(rootLength + 1);
    }

}
