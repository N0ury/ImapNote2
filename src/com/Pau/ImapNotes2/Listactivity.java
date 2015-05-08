package com.Pau.ImapNotes2;

import java.util.ArrayList;
import java.util.Date;

import com.Pau.ImapNotes2.R;
import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.OneNote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
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
import android.widget.Toast;
import android.text.Html;

public class Listactivity extends Activity {
	private static final int LOGIN_BUTTON = 0;
	private static final int REFRESH_BUTTON = 1;
	private static final int SEE_DETAIL = 2;
	private static final int DELETE_BUTTON = 3;
	private static final int NEW_BUTTON = 4;
	private static final int SAVE_BUTTON = 5;
	private static final int EDIT_BUTTON = 6;
		
	private ArrayList<OneNote> noteList;
	private SimpleAdapter listToView;
	
	private ConfigurationFile settings;
	private Imaper imapFolder;
	private NotesDb storedNotes;
	private OneNote currentNote;
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

	if ((this.settings.GetUsername()==null && this.settings.GetPassword()==null && this.settings.GetServer()==null) || (this.settings.GetPortnum()=="")) {
	    startActivityForResult(new Intent(this, AccontConfigurationActivity.class), Listactivity.LOGIN_BUTTON);
	} else {
	    this.storedNotes.OpenDb();
	    this.storedNotes.GetStoredNotes(this.noteList);
	    this.listToView.notifyDataSetChanged();
	    this.storedNotes.CloseDb();
	    if (savedInstanceState==null) this.RefreshList();
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
	boolean bool_to_return;
    	
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
						((ConfigurationFile)stuffs[1]).GetServer(),
						((ConfigurationFile)stuffs[1]).GetPortnum(),
						((ConfigurationFile)stuffs[1]).GetSecurity(),
						((ConfigurationFile)stuffs[1]).GetUsesticky());
				((Imaper)stuffs[0]).GetNotes(this.notesList);
				this.bool_to_return=true;
			} catch (Exception e) {
				Log.v("ImapNotes2", e.getMessage());
				this.bool_to_return=false;
			} finally {
				((ProgressDialog)stuffs[4]).dismiss();
			}
			return this.bool_to_return;
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
    
//=================================================================================
    public void UpdateList(String numInImap, String snote){
		ProgressDialog loadingDialog = ProgressDialog.show(this, "imapnote2" , "Updating notes list... ", true);

		new UpdateThread().execute(this.imapFolder, this.settings, this.noteList, this.listToView, loadingDialog, numInImap, snote);

    }
    
    class UpdateThread extends AsyncTask<Object, Void, Boolean>{
    	SimpleAdapter adapter;
    	ArrayList<OneNote> notesList;
	String numInImap;
	String snote;
	Imaper imapFolder;
	boolean bool_to_return;
	OneNote currentNote = null;
	String body = null;
    	
		@Override
		protected Boolean doInBackground(Object... stuffs) {
			this.adapter = ((SimpleAdapter)stuffs[3]);
			this.notesList = ((ArrayList<OneNote>)stuffs[2]);
			this.numInImap = ((String)stuffs[5]);
			this.snote = ((String)stuffs[6]);
			this.imapFolder = ((Imaper)stuffs[0]);
	
			try {
				if(!((Imaper)stuffs[0]).IsConnected())
					((Imaper)stuffs[0]).ConnectToProvider(
						((ConfigurationFile)stuffs[1]).GetUsername(),
						((ConfigurationFile)stuffs[1]).GetPassword(),
						((ConfigurationFile)stuffs[1]).GetServer(),
						((ConfigurationFile)stuffs[1]).GetPortnum(),
						((ConfigurationFile)stuffs[1]).GetSecurity(),
						((ConfigurationFile)stuffs[1]).GetUsesticky());

				// Do we have a note to remove?
				if (this.numInImap != null) {
					//Log.d(TAG,"Received request to delete message #"+numInImap);
					Integer numInImapInt = new Integer(this.numInImap);
					try {
						this.imapFolder.DeleteNote(numInImapInt);
						this.bool_to_return=true;
					} catch (Exception ex) {
                				Log.d(TAG,"Exception catched (remove): " + ex.getMessage());
						this.bool_to_return=false;
        				}
				}
				// Do we have a note to add?
				if (this.snote != null) {
				        //Log.d(TAG,"Received request to add new message"+this.snote+"===");
				        String noteTxt = Html.fromHtml(this.snote).toString();
                			String[] tok = noteTxt.split("\n", 2);
                			String title = tok[0];
					if (((ConfigurationFile)stuffs[1]).GetUsesticky().equals("true"))
                				body = noteTxt.replaceAll("\n", "\\\\n");
					else
                				body = "<html><head></head><body>" + this.snote + "</body></html>";
                			this.currentNote = new OneNote(title, body, new Date().toLocaleString(), "");
                			// Here we ask to add the new note to the "Notes" folder
					try {
                				this.imapFolder.AddNote(currentNote, ((ConfigurationFile)stuffs[1]).GetUsesticky());
                				this.bool_to_return=true;
					} catch (Exception ex) {
								ex.printStackTrace();
                				Log.d(TAG,"Exception catched (add): " + ex.getMessage());
                				this.bool_to_return=false;
        			}
				}
			} catch (Exception e) {
				Log.v(TAG, e.getMessage());
				this.bool_to_return=false;
			} finally {
				((ProgressDialog)stuffs[4]).dismiss();
			}
			return this.bool_to_return;
		}
		
		protected void onPostExecute(Boolean result){
			if (result) {
				if (this.numInImap != null) /* remove note */ {
                		// Here we delete the note from the local notes list
						//Log.d(TAG,"Delete note in Listview");
						this.notesList.remove(getIndexByNumber(this.numInImap));
				}
				if (this.snote != null) /* add note */ {
                		// Here we add the new note to the local notes list
						//Log.d(TAG,"Add note in Listview");
						this.notesList.add(0,this.currentNote);
				}
				if (this.bool_to_return) /* note added or removed */
					this.adapter.notifyDataSetChanged();
			}
		}
    }

    public int getIndexByNumber(String pNumber)
    {
        for(OneNote _item : this.noteList)
        {
            if(_item.GetNumber().equals(pNumber))
                return this.noteList.indexOf(_item);
        }
        return -1;
    }

    public boolean onCreateOptionsMenu(Menu menu){
	getMenuInflater().inflate(R.menu.list, menu);
	
	return true;

    }
    
    public boolean onOptionsItemSelected (MenuItem item){
	switch (item.getItemId()){
		case R.id.login:
			startActivityForResult(new Intent(this, AccontConfigurationActivity.class), Listactivity.LOGIN_BUTTON);
			return true;
		case R.id.refresh:
			if(this.settings.GetUsername()==null && this.settings.GetPassword()==null && this.settings.GetServer()==null)
				startActivityForResult(new Intent(this, AccontConfigurationActivity.class), Listactivity.LOGIN_BUTTON);
			else
				this.RefreshList();
			return true;
		case R.id.newnote:
			startActivityForResult(new Intent(this, NewNoteActivity.class), Listactivity.NEW_BUTTON);
			return true;
		case R.id.about:
			try {
				ComponentName comp = new ComponentName(this.getApplicationContext(), Listactivity.class);
				PackageInfo pinfo = this.getApplicationContext().getPackageManager().getPackageInfo(comp.getPackageName(), 0);
				String version = "Version: " + pinfo.versionName;

				new AlertDialog.Builder(this)
					.setTitle("About ImapNotes2")
					.setMessage("Version: " + pinfo.versionName)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog, int which) { 
            					// Do nothing
        					}
     					})
					.show();
			} catch (android.content.pm.PackageManager.NameNotFoundException e) {
				Log.d("XXXXX","except");
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
	}
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){ 
    	switch(requestCode) {
    		case Listactivity.LOGIN_BUTTON:
    			if(resultCode==AccontConfigurationActivity.TO_REFRESH) {
    				this.RefreshList();
			}
    		case Listactivity.SEE_DETAIL:
			// Returning from NoteDetailActivity
			if (resultCode == this.DELETE_BUTTON) {
				// Delete Message asked for
				// String numInImap will contain the Message Imap Number to delete
				String numInImap = data.getStringExtra("DELETE_ITEM_NUM_IMAP");
				this.UpdateList(numInImap, null);
			}
			if (resultCode == this.EDIT_BUTTON) {
				String txt = data.getStringExtra("EDIT_ITEM_TXT");
				String numInImap = data.getStringExtra("EDIT_ITEM_NUM_IMAP");
				//Log.d(TAG,"Received request to delete message:"+numInImap);
				//Log.d(TAG,"Received request to replace message with:"+txt);
				this.UpdateList(numInImap, txt);
			}
    		case Listactivity.NEW_BUTTON:
			// Returning from NewNoteActivity
			if (resultCode == this.SAVE_BUTTON) {
				String res = data.getStringExtra("SAVE_ITEM");
				//Log.d(TAG,"Received request to save message:"+res);
				this.UpdateList(null, res);
			}
    	}
    }
}
