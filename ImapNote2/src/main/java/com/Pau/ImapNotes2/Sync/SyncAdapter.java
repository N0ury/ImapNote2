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
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.Pau.ImapNotes2.Data.ConfigurationFieldNames;
import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;
import com.sun.mail.imap.AppendUID;

import com.Pau.ImapNotes2.Listactivity;

import static com.Pau.ImapNotes2.Miscs.Imaper.ResultCodeSuccess;

/// A SyncAdapter provides methods to be called by the Android
/// framework when the framework is ready for the synchronization to
/// occur.  The application does not need to consider threading
/// because the sync happens under Android control not under control
/// of the application.
class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    private static Context context;
    private NotesDb storedNotes;
    private static Account account;
    /// See RFC 3501: http://www.faqs.org/rfcs/rfc3501.html
    @NonNull
    private Long UIDValidity = (long) -1;
    private final static int NEW = 1;
    private final static int DELETED = 2;

    private final ContentResolver mContentResolver;

    public SyncAdapter(@NonNull Context context,
                       boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        // TODO: do we really need a copy of the context reference?
        SyncAdapter.context = context;
    }

    public SyncAdapter(@NonNull Context context,
                       boolean autoInitialize, // ?
                       boolean allowParallelSyncs // always false, set in syncadapter.xml
    ) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        SyncAdapter.context = context;
    }

    @Override
    public void onPerformSync(@NonNull Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {
        //Log.d(TAG, "Beginning network synchronization of account: "+account.name);
        // TODO: should the account be static?  Should it be local?  If static then why do we not
        // provide it in the constructor?
        SyncAdapter.account = account;
        Boolean isSynced = false;
        String syncinterval;

        SyncUtils.CreateDirs(account.name, context);

        storedNotes = new NotesDb(context);
        storedNotes.OpenDb();

        AccountManager am = AccountManager.get(context);
        syncinterval = am.getUserData(account, "syncinterval");

        // Connect to remote and get UIDValidity
        ImapNotes2Result res = ConnectToRemote();
        if (res.returnCode != ResultCodeSuccess) {
            storedNotes.CloseDb();

            // Notify Listactivity that it's finished, but it can't
            // refresh display
            Intent i = new Intent(SyncService.SYNC_FINISHED);
            i.putExtra(Listactivity.ACCOUNTNAME, account.name);
            i.putExtra(Listactivity.CHANGED, false);
            i.putExtra(Listactivity.SYNCED, false);
            i.putExtra(Listactivity.SYNCINTERVAL, syncinterval);
            context.sendBroadcast(i);
            return;
        }
        // Compare UIDValidity to old saved one
        //
        if (!(res.UIDValidity.equals
                (SyncUtils.GetUIDValidity(SyncAdapter.account, context)))) {
            // Replace local data by remote
            try {
                // delete notes in NotesDb for this account
                storedNotes.ClearDb(account.name);
                // delete notes in folders for this account and recreate dirs
                SyncUtils.ClearHomeDir(account, context);
                SyncUtils.CreateDirs(account.name, context);
                // Get all notes from remote and replace local
                SyncUtils.GetNotes(account,
                        res.notesFolder,
                        context, storedNotes);
                storedNotes.CloseDb();
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            SyncUtils.SetUIDValidity(account, res.UIDValidity, context);
            // Notify Listactivity that it's finished, and that it can refresh display
            Intent i = new Intent(SyncService.SYNC_FINISHED);
            i.putExtra(Listactivity.ACCOUNTNAME, account.name);
            i.putExtra(Listactivity.CHANGED, true);
            i.putExtra(Listactivity.SYNCED, true);
            i.putExtra(Listactivity.SYNCINTERVAL, syncinterval);
            context.sendBroadcast(i);
            return;
        }

        boolean isChanged = false;

        // Send new local messages to remote, move them to local folder
        // and update uids in database
        boolean newNotesManaged = handleNewNotes();
        if (newNotesManaged) isChanged = true;

        // Delete on remote messages that are deleted locally (if necessary)
        boolean deletedNotesManaged = handleDeletedNotes();
        if (deletedNotesManaged) isChanged = true;

        // handle notes created or removed on remote
        boolean remoteNotesManaged = false;
        String usesticky = am.getUserData(account, ConfigurationFieldNames.UseSticky);
        try {
            remoteNotesManaged = SyncUtils.handleRemoteNotes(context, res.notesFolder, storedNotes, account.name, usesticky);
        } catch (MessagingException e) {
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
        i.putExtra(Listactivity.ACCOUNTNAME, account.name);
        i.putExtra(Listactivity.CHANGED, isChanged);
        isSynced = true;
        i.putExtra(Listactivity.SYNCED, isSynced);
        i.putExtra(Listactivity.SYNCINTERVAL, syncinterval);
        context.sendBroadcast(i);
    }

    /* It is possible for this function to throw exceptions; the original code caught
    MessagingException but just logged it instead of handling it.  This results in a possibility of
    returning null.  Removing the catch fixies the possible null reference but of course means that
    the caller becomes responsible.  This is the correct approach.

     */
    @NonNull
    private ImapNotes2Result ConnectToRemote() {
        AccountManager am = AccountManager.get(context);
        ImapNotes2Result res = SyncUtils.ConnectToRemote(
                    am.getUserData(account, ConfigurationFieldNames.UserName),
                    am.getPassword(account),
                    am.getUserData(account, ConfigurationFieldNames.Server),
                    am.getUserData(account, ConfigurationFieldNames.PortNumber),
                    Security.from(am.getUserData(account, ConfigurationFieldNames.Security)),
                    am.getUserData(account, ConfigurationFieldNames.UseSticky),
                    am.getUserData(account, ConfigurationFieldNames.ImapFolder));
        if (res.returnCode != ResultCodeSuccess) {
            Log.d(TAG, "Connection problem: " + res.errorMessage);
        }
        return res;
    }

    private boolean handleNewNotes() {
        Message message = null;
        boolean newNotesManaged = false;
        AppendUID[] uids = null;
        String rootString = context.getFilesDir() + "/" + account.name;
        File rootDir = new File(rootString);
        File dirNew = new File(rootDir + "/new");
        String[] listOfNew = dirNew.list();
        for (String fileNew : listOfNew) {
            //Log.d(TAG,"New Note to process:"+fileNew);
            newNotesManaged = true;
            // Read local new message from file
            message = SyncUtils.ReadMailFromFile(fileNew, NEW, false, rootString);
            try {
                message.setFlag(Flags.Flag.SEEN, true); // set message as seen
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Send this new message to remote
            final MimeMessage[] msg = {(MimeMessage) message};

            try {
                uids = SyncUtils.sendMessageToRemote(msg);
                // Update uid in database entry
                String newuid = Long.toString(uids[0].uid);
                storedNotes.UpdateANote(fileNew, newuid, account.name);
                // move new note from new dir, one level up
                File fileInNew = new File(dirNew, fileNew);
                File to = new File(rootDir, newuid);
                fileInNew.renameTo(to);
            } catch (Exception e) {
                // TODO: Handle message properly.
                Log.d(TAG, e.getMessage());
            }
        }
        return newNotesManaged;
    }

    private boolean handleDeletedNotes() {
        Message message = null;
        boolean deletedNotesManaged = false;
        String rootString = context.getFilesDir() + "/" + account.name;
        File rootDir = new File(rootString);
        File dirDeleted = new File(rootDir + "/deleted");
        String[] listOfDeleted = dirDeleted.list();
        for (String fileDeleted : listOfDeleted) {
            try {
                SyncUtils.DeleteNote(Integer.parseInt(fileDeleted));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // remove file from deleted
            File toDelete = new File(dirDeleted, fileDeleted);
            toDelete.delete();
            deletedNotesManaged = true;
        }
        return deletedNotesManaged;
    }

}
