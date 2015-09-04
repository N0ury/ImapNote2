package com.Pau.ImapNotes2.Miscs;

import java.util.ArrayList;
import java.util.Date;

import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Data.NotesDb;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class UpdateThread extends AsyncTask<Object, Void, Boolean>{
    SimpleAdapter adapter;
    ArrayList<OneNote> notesList;
    String numInImap;
    String snote;
    String color;
    Imaper imapFolder;
    boolean bool_to_return;
    OneNote currentNote = null;
    NotesDb storedNotes;
    Context ctx;
    ProgressDialog pDialog;
    String body = null;
    ImapNotes2Result res = new ImapNotes2Result();
    private static final String TAG = "UpdateThread";
        
    @Override
    protected Boolean doInBackground(Object... stuffs) {
        this.adapter = ((SimpleAdapter)stuffs[3]);
        this.notesList = ((ArrayList<OneNote>)stuffs[2]);
        this.numInImap = ((String)stuffs[5]);
        this.snote = ((String)stuffs[6]);
        this.color = ((String)stuffs[7]);
        this.imapFolder = ((Imaper)stuffs[0]);
        this.ctx = (Context)stuffs[8];

        try {
            if(((ImapNotes2Account)stuffs[1]).GetaccountHasChanged()) {
                this.res = ((Imaper)stuffs[0]).ConnectToProvider(
                    ((ImapNotes2Account)stuffs[1]).GetUsername(),
                    ((ImapNotes2Account)stuffs[1]).GetPassword(),
                    ((ImapNotes2Account)stuffs[1]).GetServer(),
                    ((ImapNotes2Account)stuffs[1]).GetPortnum(),
                    ((ImapNotes2Account)stuffs[1]).GetSecurity(),
                    ((ImapNotes2Account)stuffs[1]).GetUsesticky());
                ((ImapNotes2Account)stuffs[1]).SetaccountHasNotChanged();
                if (this.res.returnCode != 0) {
                    Toast.makeText(ctx, this.res.errorMessage,
                        Toast.LENGTH_LONG).show();
                }
            }

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
String position = "0 0 0 0";
                if (((ImapNotes2Account)stuffs[1]).GetUsesticky().equals("true"))
                            body = noteTxt.replaceAll("\n", "\\\\n");
                else
                            body = "<html><head></head><body>" + this.snote + "</body></html>";
                        this.currentNote = new OneNote(title, body, new Date().toLocaleString(), "", position, this.color);
                        // Here we ask to add the new note to the "Notes" folder
                try {
                            this.imapFolder.AddNote(currentNote, ((ImapNotes2Account)stuffs[1]).GetUsesticky());
                            this.bool_to_return=true;
                } catch (Exception ex) {
                            ex.printStackTrace();
                            Log.d(TAG,"Exception catched (add): " + ex.getMessage());
                            this.bool_to_return=false;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
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

    public int getIndexByNumber(String pNumber) {
        for(OneNote _item : this.notesList)
        {
            if(_item.GetNumber().equals(pNumber))
                return this.notesList.indexOf(_item);
        }
        return -1;
    }

}
