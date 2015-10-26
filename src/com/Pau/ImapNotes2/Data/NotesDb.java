package com.Pau.ImapNotes2.Data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.Pau.ImapNotes2.Miscs.OneNote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NotesDb {

	private static final int NOTES_VERSION = 3;
	private static final String TAG = "IN_NotesDb";
	private Context ctx;
	
	private static final String CREATE_NOTES_DB = "CREATE TABLE IF NOT EXISTS "
            + "notesTable (" 
            + "pk integer primary key autoincrement, "
            + "title text not null, "
            + "date text not null, "
            + "number text not null, "
            + "accountname text not null);";

	private SQLiteDatabase notesDb;
	private NotesDbHelper defaultHelper;
	
	public NotesDb(Context applicationContext){
		this.defaultHelper = new NotesDbHelper(applicationContext, "NotesDb", NOTES_VERSION);
		this.ctx = applicationContext;
		
	}
	
	public void OpenDb(){ 
        this.notesDb = this.defaultHelper.getWritableDatabase();
        
	}

	public void CloseDb(){ 
        this.notesDb.close();
	
	}
	
    public void InsertANoteInDb(OneNote noteElement, String accountname){ 
        ContentValues tableRow = new ContentValues();
        tableRow.put("title", (noteElement.GetTitle() != null) ? noteElement.GetTitle() : "");
        tableRow.put("date", noteElement.GetDate());
        tableRow.put("number", noteElement.GetUid());
        tableRow.put("accountname", accountname);
        this.notesDb.insert("notesTable", null, tableRow);
        //Log.d(TAG, "note inserted");   
    }

    public void DeleteANote(String number, String accountname){
        this.notesDb.execSQL("delete from notesTable where number = '" + number +
                              "' and accountname = '" + accountname + "'");
    }

    public void UpdateANote(String olduid, String newuid, String accountname){
        String req = "update notesTable set number='" + newuid + "' where number='-" + olduid + "' and accountname='" + accountname + "'";
        this.notesDb.execSQL(req);
    }

    public String GetDate(String uid, String accountname){
        String selectQuery = "select date from notesTable where number = '" + uid + "' and accountname='"+accountname+"'";
        Cursor c = this.notesDb.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
                return c.getString(0);
        }
        return "";
    }

    public String GetTempNumber(String accountname) {
        String selectQuery = "select case when cast(max(abs(number)+1) as int) > 0 then cast(max(abs(number)+1) as int)*-1 else '-1' end from notesTable where number < '0' and accountname='"+accountname+"'";
        Cursor c = this.notesDb.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
                return c.getString(0);
        }
        return "-1";
    }

    public void GetStoredNotes(ArrayList<OneNote> noteList, String accountname){
    	noteList.clear();
    	Date date=null;
        Cursor resultPointer = this.notesDb.query("notesTable", null,"accountname = ?", new String[]{accountname},null,null,"date DESC");
        
        if(resultPointer.moveToFirst()){
        	int titleIndex = resultPointer.getColumnIndex("title");
        	int bodyIndex = resultPointer.getColumnIndex("body");
        	int dateIndex = resultPointer.getColumnIndex("date");
        	int numberIndex = resultPointer.getColumnIndex("number");
        	int positionIndex = resultPointer.getColumnIndex("position");
        	int colorIndex = resultPointer.getColumnIndex("color");
            do {
                String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                try {
                  date = sdf.parse(resultPointer.getString(dateIndex));
                } catch(ParseException e){
                  //Exception handling
                } catch(Exception e){
                  //handle exception
                }
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this.ctx);
                //String sdate = dateFormat.format(date);
                String sdate = DateFormat.getDateTimeInstance().format(date);
 
                noteList.add(new OneNote(resultPointer.getString(titleIndex),
            	 sdate,
            	 resultPointer.getString(numberIndex)));
            } while (resultPointer.moveToNext());
        }
    
    }
	
    public void ClearDb(String accountname){
    	this.notesDb.execSQL("delete from notesTable where accountname = '" + accountname+"'");
    	
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
          //Log.d(TAG,"onUpgrade from:"+oldVersion+" to:"+newVersion);
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
