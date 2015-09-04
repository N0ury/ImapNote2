package com.Pau.ImapNotes2.Miscs;

import java.util.ArrayList;

import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Data.NotesDb;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SyncThread extends AsyncTask<Object, Void, Boolean> {
    SimpleAdapter adapter;
    ArrayList<OneNote> notesList;
    NotesDb storedNotes;
    boolean bool_to_return;
    ImapNotes2Result res = new ImapNotes2Result();
    Context ctx;
    private static final String TAG = "SyncThread";
    
    @Override
    protected Boolean doInBackground(Object... stuffs) {
        String username = null;
        String password = null;
        String server = null;
        String portnum = null;
        String security = null;
        String usesticky = null;
        this.adapter = ((SimpleAdapter)stuffs[3]);
        this.notesList = ((ArrayList<OneNote>)stuffs[2]);
        this.storedNotes = ((NotesDb)stuffs[5]);
        this.ctx = (Context)stuffs[6];
        username = ((ImapNotes2Account)stuffs[1]).GetUsername();
        password = ((ImapNotes2Account)stuffs[1]).GetPassword();
        server = ((ImapNotes2Account)stuffs[1]).GetServer();
        portnum = ((ImapNotes2Account)stuffs[1]).GetPortnum();
        security = ((ImapNotes2Account)stuffs[1]).GetSecurity();
        usesticky = ((ImapNotes2Account)stuffs[1]).GetUsesticky();

        try {
            if(((ImapNotes2Account)stuffs[1]).GetaccountHasChanged()) {
                this.res = ((Imaper)stuffs[0]).ConnectToProvider(
                    username, password, server, portnum, security, usesticky);
                if (this.res.returnCode != 0) {
                    Toast.makeText(this.ctx, this.res.errorMessage,
                        Toast.LENGTH_LONG).show();
                }
            }
		((ImapNotes2Account)stuffs[1]).SetaccountHasNotChanged();
            ((Imaper)stuffs[0]).GetNotes(this.notesList);
            this.bool_to_return=true;
        } catch (Exception e) {
            e.printStackTrace();
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
