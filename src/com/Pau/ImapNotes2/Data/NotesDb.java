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

	private static final String NOTES_TABLE_NAME = "notesTable";
	private static final int NOTES_VERSION = 2;
	private static final String TITLE_LABEL = "title";
	private static final String BODY_LABEL = "body";
	private static final String DATE_LABEL = "date";
	private static final String NUMBER_LABEL = "number";
	private static final String POSITION_LABEL = "position";
	private static final String COLOR_LABEL = "color";
	private static final String TAG = "IN_NotesDb";
	
	private static final String CREATE_NOTES_DB = "CREATE TABLE IF NOT EXISTS "
            + NotesDb.NOTES_TABLE_NAME + " (" 
            + "pk integer primary key autoincrement, "
            + NotesDb.TITLE_LABEL + " text not null, "
            + NotesDb.BODY_LABEL + " text not null, "
            + NotesDb.DATE_LABEL + " text not null, "
            + NotesDb.NUMBER_LABEL + " text not null, "
            + NotesDb.POSITION_LABEL + " text not null, "
            + NotesDb.COLOR_LABEL + " text not null);";
	
	private static final String UPGRADE_NOTES_DB_1 = "alter table "
	    + NotesDb.NOTES_TABLE_NAME + " ADD COLUMN " + NotesDb.POSITION_LABEL + " text not null DEFAULT '0 0 0 0';";
	private static final String UPGRADE_NOTES_DB_2 = "alter table "
	    + NotesDb.NOTES_TABLE_NAME + " ADD COLUMN " + NotesDb.COLOR_LABEL + " text not null DEFAULT 'YELLOW';";

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
        tableRow.put(NotesDb.TITLE_LABEL, (noteElement.GetTitle() != null) ? noteElement.GetTitle() : "");
        tableRow.put(NotesDb.BODY_LABEL, noteElement.GetBody());
        tableRow.put(NotesDb.DATE_LABEL, noteElement.GetDate());
        tableRow.put(NotesDb.NUMBER_LABEL, noteElement.GetNumber());
        tableRow.put(NotesDb.POSITION_LABEL, noteElement.GetPosition());
        tableRow.put(NotesDb.COLOR_LABEL, noteElement.GetColor());
        this.notesDb.insert(NotesDb.NOTES_TABLE_NAME, null, tableRow);
    
    }

    public void GetStoredNotes(ArrayList<OneNote> noteList){
    	noteList.clear();
        Cursor resultPointer = this.notesDb.query(NotesDb.NOTES_TABLE_NAME, null,null,null,null,null,null);
        
        if(resultPointer.moveToFirst()){
        	int titleIndex = resultPointer.getColumnIndex(NotesDb.TITLE_LABEL);
        	int bodyIndex = resultPointer.getColumnIndex(NotesDb.BODY_LABEL);
        	int dateIndex = resultPointer.getColumnIndex(NotesDb.DATE_LABEL);
        	int numberIndex = resultPointer.getColumnIndex(NotesDb.NUMBER_LABEL);
        	int positionIndex = resultPointer.getColumnIndex(NotesDb.POSITION_LABEL);
        	int colorIndex = resultPointer.getColumnIndex(NotesDb.COLOR_LABEL);
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
    	this.notesDb.execSQL("delete from " + NotesDb.NOTES_TABLE_NAME);
    	
    }
    
	private class NotesDbHelper extends SQLiteOpenHelper {

        	public NotesDbHelper(Context currentApplicationContext, String dbName, int dbVersion) {
                	super(currentApplicationContext, dbName, null, dbVersion);
        	}

        	@Override
        	public void onCreate(SQLiteDatabase _db) {
                	_db.execSQL(NotesDb.CREATE_NOTES_DB);
        	}

        	@Override
        	public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
        		//Log.d(TAG,"onUpgrade from:"+oldVersion+" to:"+newVersion);
        		_db.execSQL(NotesDb.UPGRADE_NOTES_DB_1);
        		_db.execSQL(NotesDb.UPGRADE_NOTES_DB_2);
        	}
	}
}
