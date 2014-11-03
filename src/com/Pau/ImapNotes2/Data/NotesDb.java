package com.Pau.ImapNotes2.Data;

import java.util.ArrayList;

import com.Pau.ImapNotes2.Miscs.OneNote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesDb {

	private static final String NOTES_TABLE_NAME = "notesTable";
	private static final String TITLE_LABEL = "title";
	private static final String BODY_LABEL = "body";
	private static final String DATE_LABEL = "date";
	private static final String NUMBER_LABEL = "number";
	
	private static final String CREATE_NOTES_DB = "CREATE TABLE IF NOT EXISTS "
            + NotesDb.NOTES_TABLE_NAME + " (" 
            + "pk integer primary key autoincrement, "
            + NotesDb.TITLE_LABEL + " text not null, "
            + NotesDb.BODY_LABEL + " text not null, "
            + NotesDb.DATE_LABEL + " text not null, "
            + NotesDb.NUMBER_LABEL + " text not null);";
	
	private SQLiteDatabase notesDb;
	private NotesDbHelper defaultHelper;
	
	public NotesDb(Context applicationContext){
		this.defaultHelper = new NotesDbHelper(applicationContext, "NotesDb", 1);
		
	}
	
	public void OpenDb(){ 
        this.notesDb = this.defaultHelper.getWritableDatabase();
        
	}

	public void CloseDb(){ 
        this.notesDb.close();
	
	}
	
    public void InsertANote(OneNote noteElement){ 
        ContentValues tableRow = new ContentValues();
        tableRow.put(NotesDb.TITLE_LABEL, noteElement.GetTitle());
        tableRow.put(NotesDb.BODY_LABEL, noteElement.GetBody());
        tableRow.put(NotesDb.DATE_LABEL, noteElement.GetDate());
        tableRow.put(NotesDb.NUMBER_LABEL, noteElement.GetNumber());
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
            do {
            	noteList.add(new OneNote(resultPointer.getString(titleIndex),
            							 resultPointer.getString(bodyIndex),
            							 resultPointer.getString(dateIndex),
            							 resultPointer.getString(numberIndex)));
                } while (resultPointer.moveToNext());
        }
    
    }
	
    public void ClearDb(){
    	this.notesDb.execSQL("delete from " + NotesDb.NOTES_TABLE_NAME);
    	
    }
    
	private class NotesDbHelper extends SQLiteOpenHelper {

        public NotesDbHelper(Context currentApplicationContext, String dbName,int dbVersion) {
                super(currentApplicationContext, dbName, null, dbVersion);
                
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
                _db.execSQL(NotesDb.CREATE_NOTES_DB);
                
        }

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

	}
}
