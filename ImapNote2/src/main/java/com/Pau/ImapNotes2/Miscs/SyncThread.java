package com.Pau.ImapNotes2.Miscs;

import java.util.ArrayList;

import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Listactivity;
import com.Pau.ImapNotes2.NotesListAdapter;
import com.Pau.ImapNotes2.Data.NotesDb;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class SyncThread extends AsyncTask<Object, Void, Boolean> {
    NotesListAdapter adapter;
    ArrayList<OneNote> notesList;
    NotesDb storedNotes;
    boolean bool_to_return;
    ImapNotes2Result res = new ImapNotes2Result();
    Context ctx;
    private static final String TAG = "SyncThread";

    // TODO: remove unused arguments.
    public SyncThread(Object imapFolder,
                      ImapNotes2Account imapNotes2Account,
                      ArrayList<OneNote> noteList,
                      NotesListAdapter listToView,
                      ProgressDialog loadingDialog,
                      NotesDb storedNotes,
                      Context applicationContext) {
        //this.imapFolder = imapFolder;
        //this.imapNotes2Account = imapNotes2Account;
        this.notesList = noteList;
        this.adapter = listToView;
        //this.loadingDialog = loadingDialog;
        this.storedNotes = storedNotes;
        this.ctx = applicationContext;

    }

    // Do not pass arguments via execute; the object is never reused so it is quite safe to pass
    // the arguments in the constructor.
    @Override
    protected Boolean doInBackground(Object... stuffs) {
        /*String username = null;
        String password = null;
        String server = null;
        String portnum = null;
        String security = null;
        String usesticky = null;
*/
         /*       this.adapter = ((NotesListAdapter) stuffs[3]);
        this.notesList = ((ArrayList<OneNote>) stuffs[2]);
        this.storedNotes = ((NotesDb) stuffs[5]);
        this.ctx = (Context) stuffs[6];
 */
        //username = ((ImapNotes2Account) stuffs[1]).GetUsername();
        //password = ((ImapNotes2Account) stuffs[1]).GetPassword();
        //server = ((ImapNotes2Account) stuffs[1]).GetServer();
        //portnum = ((ImapNotes2Account) stuffs[1]).GetPortnum();
        //security = ((ImapNotes2Account) stuffs[1]).GetSecurity();
        //usesticky = ((ImapNotes2Account) stuffs[1]).GetUsesticky();


        if (this.storedNotes == null) this.storedNotes = new NotesDb(this.ctx);
        this.storedNotes.OpenDb();
        this.storedNotes.GetStoredNotes(this.notesList, Listactivity.imapNotes2Account.GetAccountname());
        this.storedNotes.CloseDb();
        ((ProgressDialog) stuffs[4]).dismiss();
        return true;
    }

    protected void onPostExecute(Boolean result) {
        if (result) {
            this.adapter.notifyDataSetChanged();
        }
    }
}
