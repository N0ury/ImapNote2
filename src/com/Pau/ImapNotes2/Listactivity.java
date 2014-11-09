package com.Pau.ImapNotes2;

import java.util.ArrayList;
import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.OneNote;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.ListView;

public class Listactivity extends Activity {
	private static final int LOGIN_BUTTON = 0;
	private static final int REFRESH_BUTTON = 1;
	private static final int SEE_DETAIL = 2;
	private static final int DELETE_BUTTON = 3;
	private static final int NEW_BUTTON = 4;
	private static final int SAVE_BUTTON = 5;
		
	private ArrayList<OneNote> noteList;
	private SimpleAdapter listToView;
	
	private ConfigurationFile settings;
	private Imaper imapFolder;
	private NotesDb storedNotes;
	private static final String TAG = "IN_Listactivity";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	
	this.noteList = new ArrayList<OneNote>();
	((ImapNotes2)this.getApplicationContext()).SetNotesList(this.noteList);
	this.listToView = new SimpleAdapter(
			getApplicationContext(),
			this.noteList,
			R.layout.note_element,
			new String[]{"title","date"},
			new int[]{R.id.noteTitle, R.id.noteInformation});
	((ListView)findViewById(R.id.notesList)).setAdapter(this.listToView);
	
	this.settings = new ConfigurationFile(this.getApplicationContext());
	((ImapNotes2)this.getApplicationContext()).SetConfigurationFile(this.settings);
	
	this.imapFolder = new Imaper();
	((ImapNotes2)this.getApplicationContext()).SetImaper(this.imapFolder);
	
	this.storedNotes = new NotesDb(this.getApplicationContext());
	
	if (this.settings.GetUsername()==null && this.settings.GetPassword()==null && this.settings.GetServer()==null){
	    startActivityForResult(new Intent(this, AccontConfigurationActivity.class), Listactivity.LOGIN_BUTTON);
	
	} else {
		this.storedNotes.OpenDb();
		this.storedNotes.GetStoredNotes(this.noteList);
		this.listToView.notifyDataSetChanged();
		this.storedNotes.CloseDb();
	}
	
	// When item is clicked, we go to NoteDetailActivity
	((ListView)findViewById(R.id.notesList)).setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View widget, int selectedNote, long arg3) {
			Intent toDetail = new Intent(widget.getContext(), NoteDetailActivity.class);
			toDetail.putExtra("selectedNote", (OneNote)noteList.get(selectedNote));
			startActivityForResult(toDetail,SEE_DETAIL); 
		}
	  });
    }

    public void RefreshList(){
		ProgressDialog loadingDialog = ProgressDialog.show(this, "ImapNotes2" , "Refreshing notes list... ", true);

		new RefreshThread().execute(this.imapFolder, this.settings, this.noteList, this.listToView, loadingDialog, this.storedNotes);

    }
    
    class RefreshThread extends AsyncTask<Object, Void, Boolean>{
    	SimpleAdapter adapter;
    	ArrayList<OneNote> notesList;
    	NotesDb storedNotes;
    	
		@Override
		protected Boolean doInBackground(Object... stuffs) {
			this.adapter = ((SimpleAdapter)stuffs[3]);
			this.notesList = ((ArrayList<OneNote>)stuffs[2]);
			this.storedNotes = ((NotesDb)stuffs[5]);
	
			try {
				if(!((Imaper)stuffs[0]).IsConnected())
					((Imaper)stuffs[0]).ConnectToProvider(
						((ConfigurationFile)stuffs[1]).GetUsername(),
						((ConfigurationFile)stuffs[1]).GetPassword(),
						((ConfigurationFile)stuffs[1]).GetServer());
				((Imaper)stuffs[0]).GetNotes(this.notesList);
		    	return true;
			} catch (Exception e) {
				Log.v("ImapNotes2", e.getMessage());
			} finally {
				((ProgressDialog)stuffs[4]).dismiss();
				
			}
			
			return false;
		}
		
		protected void onPostExecute(Boolean result){
			if(result){
				this.storedNotes.OpenDb();
				this.storedNotes.ClearDb();
				for(OneNote n : this.notesList)
					this.storedNotes.InsertANote(n);
				this.storedNotes.CloseDb();
						
				this.adapter.notifyDataSetChanged();
			}
		}
    	
    }
    
    public void NewMessage(){
	Intent editNew = new Intent(this, NewNoteActivity.class);
	startActivityForResult(editNew, NEW_BUTTON);
    }

    public void DeleteMessage(String res){
	Log.d(TAG,"Received request to delete message #"+res);
	Integer resi = new Integer(res);
	try {
		this.imapFolder.DeleteNote(resi);
		this.RefreshList();
	} catch (Exception ex) {
		this.RefreshList();
		Log.d(TAG,"Exception rencontrée: " + ex.getMessage());
	}
    }

    /***************************************************/
    public boolean onCreateOptionsMenu(Menu menu){
	menu.add(0, Listactivity.LOGIN_BUTTON, 0, "Account");
	//.setIcon(R.drawable.ic_menu_barcode);
	menu.add(0, Listactivity.REFRESH_BUTTON, 0, "Refresh");
	menu.add(0, Listactivity.NEW_BUTTON, 0, "New");
	
	return true;

    }
    
    public boolean onOptionsItemSelected (MenuItem item){
	switch (item.getItemId()){
		case Listactivity.LOGIN_BUTTON:
		startActivityForResult(new Intent(this, AccontConfigurationActivity.class), Listactivity.LOGIN_BUTTON);
		return true;
		case Listactivity.REFRESH_BUTTON:
			if(this.settings.GetUsername()==null && this.settings.GetPassword()==null && this.settings.GetServer()==null)
				startActivityForResult(new Intent(this, AccontConfigurationActivity.class), Listactivity.LOGIN_BUTTON);
			else
				this.RefreshList();
			return true;
		case Listactivity.NEW_BUTTON:
			this.NewMessage();
			return true;
		    
	}
	
	return false;
	
    }
    
    /***************************************************/
    protected void onActivityResult(int requestCode, int resultCode, Intent data){ 
    	switch(requestCode) {
    		case Listactivity.LOGIN_BUTTON:
    			if(resultCode==AccontConfigurationActivity.TO_REFRESH)
    				this.RefreshList();
    		case Listactivity.SEE_DETAIL:
			// Returning from NoteDetailActivity
			if (resultCode == this.DELETE_BUTTON) {
				// Delete Message asked for
				// String res will contain the Message Number to delete
				String res = data.getStringExtra("DELETE_ITEM");
				DeleteMessage(res);
			}
    		case Listactivity.NEW_BUTTON:
			// Returning from NewNoteActivity
			if (resultCode == this.SAVE_BUTTON) {
				String res = data.getStringExtra("SAVE_ITEM");
Log.d(TAG,"Received request to save message:"+res);
				this.RefreshList();
			}
    	}
    }
}
