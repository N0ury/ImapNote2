package com.Pau.ImapNotes2.Sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;
import com.Pau.ImapNotes2.Sync.SyncUtils;
import com.sun.mail.imap.AppendUID;

class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";
    private static Context context;
    private static Boolean isChanged;
    private static Boolean isSynced;
    private NotesDb storedNotes;
    private String[] listOfNew;
    private String[] listOfDeleted;
    private static Account account;
    private Long UIDValidity = (long) -1;
    private static ImapNotes2Result res;
    private final static int NEW = 1;
    private final static int DELETED = 2;

    private final ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        this.context = context;
    }

    public SyncAdapter(Context context, boolean autoInitialize, 
                       boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        this.context = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        //Log.d(TAG, "Beginning network synchronization of account: "+account.name);
        this.account = account;
        isChanged = false;
        isSynced = false;
        String syncinterval;
        
        SyncUtils.CreateDirs (account.name, this.context);

        storedNotes = new NotesDb(this.context);
        storedNotes.OpenDb();

        AccountManager am = AccountManager.get(this.context);
        syncinterval = am.getUserData(account, "syncinterval");
/*
// Temporary workaround for a bug
// Portnum was put into account manager sync interval (143 or 993 or ... minutes)
if (syncinterval != null)
if (syncinterval.equals("143") || syncinterval.equals("993")) am.setUserData(account, "syncinterval", "15");
else am.setUserData(account, "syncinterval", "15");
*/

        // Connect to remote and get UIDValidity
        this.res = ConnectToRemote();
        if (this.res.returnCode != 0) {
            storedNotes.CloseDb();

            // Notify Listactivity that it's finished, but it can't refresh display
            Intent i = new Intent(SyncService.SYNC_FINISHED);
            i.putExtra("ACCOUNTNAME",account.name);
            isChanged = false;
            isSynced = false;
            i.putExtra("CHANGED", isChanged);
            i.putExtra("SYNCED", isSynced);
            i.putExtra("SYNCINTERVAL", syncinterval);
            context.sendBroadcast(i);
            return;
        }
        // Compare UIDValidity to old saved one
        if (!(this.res.UIDValidity.equals
                (SyncUtils.GetUIDValidity(this.account, this.context)))) {
            // Replace local data by remote
            try {
                // delete notes in NotesDb for this account
                storedNotes.ClearDb(account.name);
                // delete notes in folders for this account and recreate dirs
                SyncUtils.ClearHomeDir(account, this.context);
                SyncUtils.CreateDirs (account.name, this.context);
                // Get all notes from remote and replace local
                SyncUtils.GetNotes(account,this.res.notesFolder,this.context,storedNotes);
                storedNotes.CloseDb();
            } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            }
            SyncUtils.SetUIDValidity(account, this.res.UIDValidity, this.context);
            // Notify Listactivity that it's finished, and that it can refresh display
            Intent i = new Intent(SyncService.SYNC_FINISHED);
            i.putExtra("ACCOUNTNAME",account.name);
            isChanged = true;
            isSynced = true;
            i.putExtra("CHANGED", isChanged);
            i.putExtra("SYNCED", isSynced);
            i.putExtra("SYNCINTERVAL", syncinterval);
            context.sendBroadcast(i);
            return;
        }
        
        // Send new local messages to remote, move them to local folder
        // and update uids in database
        boolean newNotesManaged = handleNewNotes();
        if (newNotesManaged) isChanged = true;

        // Delete on remote messages that are deleted locally (if necessary)
        boolean deletedNotesManaged = handleDeletedNotes();
        if (deletedNotesManaged) isChanged = true;

        // handle notes created or removed on remote
        boolean remoteNotesManaged = false;
        String usesticky = am.getUserData(account, "usesticky");
    try {
        remoteNotesManaged = SyncUtils.handleRemoteNotes(context, res.notesFolder, storedNotes, account.name, usesticky);
    } catch (MessagingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
        if (remoteNotesManaged) isChanged = true;

        storedNotes.CloseDb();

        // Disconnect from remote
        SyncUtils.DisconnectFromRemote();
        //Log.d(TAG, "Network synchronization complete of account: "+account.name);
        // Notify Listactivity that it's finished, and that it can refresh display
        Intent i = new Intent(SyncService.SYNC_FINISHED);
        i.putExtra("ACCOUNTNAME",account.name);
        i.putExtra("CHANGED", isChanged);
        isSynced = true;
        i.putExtra("SYNCED", isSynced);
            i.putExtra("SYNCINTERVAL", syncinterval);
        context.sendBroadcast(i);
    }

    ImapNotes2Result ConnectToRemote() {
        AccountManager am = AccountManager.get(this.context);
        ImapNotes2Result res = null;
        try {
        res = SyncUtils.ConnectToRemote(
              am.getUserData(account, "username"),
              am.getPassword(account),
              am.getUserData(account, "server"),
              am.getUserData(account, "portnum"),
              am.getUserData(account, "security"),
              am.getUserData(account, "usesticky"),
              am.getUserData(account, "imapfolder"));
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (res.returnCode != 0) {
            Log.d(TAG,"Connection problem !!!");
        }
        return res;
    }

    private boolean handleNewNotes() {
        Message message = null;
        boolean newNotesManaged = false;
        AppendUID[] uids = null;
        String rootString = context.getFilesDir() + "/" + account.name;
        File rootDir = new File (rootString);
        File dirNew = new File (rootDir + "/new");
        listOfNew = dirNew.list();
        for (String fileNew : listOfNew) {
            //Log.d(TAG,"New Note to process:"+fileNew);
            newNotesManaged = true;
            // Read local new message from file
            message = SyncUtils.ReadMailFromFile(fileNew, NEW, false, rootString);
            try {
                message.setFlag(Flags.Flag.SEEN,true); // set message as seen
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Send this new message to remote
            final MimeMessage[] msg = {(MimeMessage)message};
            
            try {
                uids = SyncUtils.sendMessageToRemote(msg);
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Update uid in database entry
            String newuid = Long.toString(uids[0].uid);
            storedNotes.UpdateANote(fileNew,newuid,account.name);
            // move new note from new dir, one level up
            File fileInNew = new File (dirNew, fileNew);
            File to = new File (rootDir, newuid);
            fileInNew.renameTo(to);
        }
        return newNotesManaged;
    }

    private boolean handleDeletedNotes() {
        Message message = null;
        boolean deletedNotesManaged = false;
        String rootString = context.getFilesDir() + "/" + account.name;
        File rootDir = new File (rootString);
        File dirDeleted = new File (rootDir + "/deleted");
        listOfDeleted = dirDeleted.list();
        for (String fileDeleted : listOfDeleted) {
            try {
                SyncUtils.DeleteNote(this.res.notesFolder, Integer.parseInt(fileDeleted));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // remove file from deleted
            File toDelete = new File (dirDeleted, fileDeleted);
            toDelete.delete();
            deletedNotesManaged = true;
        }
        return deletedNotesManaged;
    }

}
