package com.Pau.ImapNotes2.Miscs;

import java.util.ArrayList;

import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Listactivity;
import com.Pau.ImapNotes2.NotesListAdapter;
import com.Pau.ImapNotes2.Data.NotesDb;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SyncThread extends AsyncTask<Object, Void, Boolean> {
    private final ProgressDialog progressDialog;
    private NotesListAdapter adapter;
    private ArrayList<OneNote> notesList;
    // TODO: NoteDb should probably never be null.
    @Nullable
    private NotesDb storedNotes;
    boolean bool_to_return;
    @NonNull
    ImapNotes2Result res = new ImapNotes2Result();
    private static final String TAG = "SyncThread";

    // TODO: remove unused arguments.
    public SyncThread(Object imapFolder,
                      ImapNotes2Account imapNotes2Account,
                      ArrayList<OneNote> noteList,
                      NotesListAdapter listToView,
                      ProgressDialog loadingDialog,
                      @Nullable NotesDb storedNotes,
                      Context applicationContext) {
        //this.imapFolder = imapFolder;
        //this.imapNotes2Account = imapNotes2Account;
        this.notesList = noteList;
        this.adapter = listToView;
        this.progressDialog = loadingDialog;
        //this.storedNotes = storedNotes;
        this.storedNotes = (storedNotes == null) ? new NotesDb(applicationContext) : storedNotes;

    }

    // Do not pass arguments via execute; the object is never reused so it is quite safe to pass
    // the arguments in the constructor.
    @NonNull
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


        storedNotes.OpenDb();
        storedNotes.GetStoredNotes(this.notesList, Listactivity.imapNotes2Account.GetAccountname());
        storedNotes.CloseDb();
        progressDialog.dismiss();
        return true;
    }

    protected void onPostExecute(Boolean result) {
        if (result) {
            this.adapter.notifyDataSetChanged();
        }
    }
}
