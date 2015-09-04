package com.Pau.ImapNotes2.Data;

import java.util.ArrayList;

import com.Pau.ImapNotes2.Miscs.OneNote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NotesDb {

	private static final int NOTES_VERSION = 2;
	private static final String TAG = "IN_NotesDb";
	
	private static final String CREATE_NOTES_DB = "CREATE TABLE IF NOT EXISTS "
            + "notesTable (" 
            + "pk integer primary key autoincrement, "
            + "title text not null, "
            + "body text not null, "
            + "date text not null, "
            + "number text not null, "
            + "position text not null, "
            + "color text not null);";

/*
	private static final String CREATE_LOGINS = "CREATE TABLE IF NOT EXISTS "
            + "loginsTable (" 
            + "username text not null, "
            + "password text not null, "
            + "server text not null, "
            + "portnum text not null, "
            + "security text not null, "
            + "usesticky text not null);";
*/

	private SQLiteDatabase notesDb;
	private NotesDbHelper defaultHelper;
	
	public NotesDb(Context applicationContext){
		this.defaultHelper = new NotesDbHelper(applicationContext, "NotesDb", NOTES_VERSION);
		
	}
	
	public void OpenDb(){ 
        this.notesDb = this.defaultHelper.getWritableDatabase();
        
	}

	public void CloseDb(){ 
        this.notesDb.close();
	
	}
	
    public void InsertANote(OneNote noteElement){ 
        ContentValues tableRow = new ContentValues();
        tableRow.put("title", (noteElement.GetTitle() != null) ? noteElement.GetTitle() : "");
        tableRow.put("body", noteElement.GetBody());
        tableRow.put("date", noteElement.GetDate());
        tableRow.put("number", noteElement.GetNumber());
        tableRow.put("position", noteElement.GetPosition());
        tableRow.put("color", noteElement.GetColor());
        this.notesDb.insert("notesTable", null, tableRow);
    
    }

    public void GetStoredNotes(ArrayList<OneNote> noteList){
    	noteList.clear();
        Cursor resultPointer = this.notesDb.query("notesTable", null,null,null,null,null,null);
        
        if(resultPointer.moveToFirst()){
        	int titleIndex = resultPointer.getColumnIndex("title");
        	int bodyIndex = resultPointer.getColumnIndex("body");
        	int dateIndex = resultPointer.getColumnIndex("date");
        	int numberIndex = resultPointer.getColumnIndex("number");
        	int positionIndex = resultPointer.getColumnIndex("position");
        	int colorIndex = resultPointer.getColumnIndex("color");
            do {
            	noteList.add(new OneNote(resultPointer.getString(titleIndex),
            		 resultPointer.getString(bodyIndex),
            		 resultPointer.getString(dateIndex),
            		 resultPointer.getString(numberIndex),
            		 resultPointer.getString(positionIndex),
            		 resultPointer.getString(colorIndex)));
                } while (resultPointer.moveToNext());
        }
    
    }
	
    public void ClearDb(){
    	this.notesDb.execSQL("delete from notesTable");
    	
    }
    
    /**
     * Database helper that creates and maintains the SQLite database.
     */

    private static class NotesDbHelper extends SQLiteOpenHelper {

       	public NotesDbHelper(Context currentApplicationContext, String dbName, int dbVersion) {
               	super(currentApplicationContext, dbName, null, dbVersion);
       	}

       	@Override
       	public void onCreate(SQLiteDatabase _db) {
               	_db.execSQL(NotesDb.CREATE_NOTES_DB);
       	}

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
          Log.d(TAG,"onUpgrade from:"+oldVersion+" to:"+newVersion);
          for (int i=oldVersion; i<newVersion; i++) {
            PATCHES[i-2].apply(_db);
          }
        }

        private static class Patch {
          public void apply(SQLiteDatabase _db) {}
        }

        private static final Patch[] PATCHES = new Patch[] {
           new Patch() {
              public void apply(SQLiteDatabase _db) {
                //Log.d(TAG,"upgrade: v2 to v3");
//                _db.execSQL(NotesDb.CREATE_LOGINS);
              }
           }
        };
    }
}
