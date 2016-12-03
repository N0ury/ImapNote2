package com.Pau.ImapNotes2.Sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.Pau.ImapNotes2.Data.ConfigurationFieldNames;
import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Data.Security;
import com.Pau.ImapNotes2.Listactivity;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;
import com.sun.mail.imap.AppendUID;

import java.io.File;
import java.io.IOException;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.Pau.ImapNotes2.Miscs.Imaper.ResultCodeSuccess;

/// A SyncAdapter provides methods to be called by the Android
/// framework when the framework is ready for the synchronization to
/// occur.  The application does not need to consider threading
/// because the sync happens under Android control not under control
/// of the application.
class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    private final Context applicationContext;
    private NotesDb storedNotes;
    // TODO: Why is this static?
    private Account account;
// --Commented out by Inspection START (11/26/16 11:49 PM):
//    /// See RFC 3501: http://www.faqs.org/rfcs/rfc3501.html
//    @NonNull
//    private Long UIDValidity = (long) -1;
// --Commented out by Inspection STOP (11/26/16 11:49 PM)
    // --Commented out by Inspection (11/26/16 11:49 PM):private final static int NEW = 1;
    // --Commented out by Inspection (11/26/16 11:49 PM):private final static int DELETED = 2;

    // --Commented out by Inspection (12/2/16 8:51 PM):private final ContentResolver mContentResolver;

    SyncAdapter(@NonNull Context applicationContext) {
        super(applicationContext, true);
        //mContentResolver = applicationContext.getContentResolver();
        // TODO: do we really need a copy of the applicationContext reference?
        this.applicationContext = applicationContext;
    }


// --Commented out by Inspection START (12/2/16 8:50 PM):
//    /**
//     * TODO: What does allowParallelSyncs do and is it useful to us?
//     *
//     * @param applicationContext In our case this is always the global application context but it
//     *                           might not need to be.
//     * @param autoInitialize     I've read the documentation for this argument and am no wiser.
//     * @param allowParallelSyncs Allow sync for different accounts to run at the same time.
//     */
//    public SyncAdapter(@NonNull Context applicationContext,
//                       boolean autoInitialize, // ?
//                       boolean allowParallelSyncs // always false, set in syncadapter.xml
//    ) {
//        super(applicationContext, autoInitialize, allowParallelSyncs);
//        mContentResolver = applicationContext.getContentResolver();
//        this.applicationContext = applicationContext;
//    }
// --Commented out by Inspection STOP (12/2/16 8:50 PM)

    @Override
    public void onPerformSync(@NonNull Account accountArg,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {
        //Log.d(TAG, "Beginning network synchronization of account: "+account.name);
        // TODO: should the account be static?  Should it be local?  If static then why do we not
        // provide it in the constructor?  What happens if we allow parallel syncs?
        account = accountArg;

        SyncUtils.CreateLocalDirectories(accountArg.name, applicationContext);

        storedNotes = new NotesDb(applicationContext);
        storedNotes.OpenDb();

        AccountManager am = AccountManager.get(applicationContext);
        String syncInterval = am.getUserData(accountArg, "syncinterval");

        // Connect to remote and get UIDValidity
        ImapNotes2Result res = ConnectToRemote();
        if (res.returnCode != ResultCodeSuccess) {
            storedNotes.CloseDb();

            // Notify Listactivity that it's finished, but it can't
            // refresh display
            /*Intent i = new Intent(SyncService.SYNC_FINISHED);
            i.putExtra(Listactivity.ACCOUNTNAME, account.name);
            i.putExtra(Listactivity.CHANGED, false);
            i.putExtra(Listactivity.SYNCED, false);
            i.putExtra(Listactivity.SYNCINTERVAL, syncInterval);
            applicationContext.sendBroadcast(i);
            */
            NotifySyncFinished(false, false, syncInterval);
            return;
        }
        // Compare UIDValidity to old saved one
        //
        if (!(res.UIDValidity.equals(
                SyncUtils.GetUIDValidity(accountArg, applicationContext)))) {
            // Replace local data by remote.  UIDs are no longer valid.
            try {
                // delete notes in NotesDb for this account
                storedNotes.ClearDb(accountArg.name);
                // delete notes in folders for this account and recreate dirs
                SyncUtils.ClearHomeDir(accountArg, applicationContext);
                SyncUtils.CreateLocalDirectories(accountArg.name, applicationContext);
                // Get all notes from remote and replace local
                SyncUtils.GetNotes(accountArg,
                        res.notesFolder,
                        applicationContext, storedNotes);
                storedNotes.CloseDb();
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            SyncUtils.SetUIDValidity(accountArg, res.UIDValidity, applicationContext);
            // Notify Listactivity that it's finished, and that it can refresh display
            NotifySyncFinished(true, true, syncInterval);
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
        String usesticky = am.getUserData(accountArg, ConfigurationFieldNames.UseSticky);
        try {
            remoteNotesManaged = SyncUtils.handleRemoteNotes(applicationContext, res.notesFolder,
                    storedNotes, accountArg.name, usesticky);
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
        NotifySyncFinished(isChanged, true, syncInterval);
    }

    private void NotifySyncFinished(boolean isChanged,
                                    boolean isSynced,
                                    String syncInterval) {
        Intent i = new Intent(SyncService.SYNC_FINISHED);
        i.putExtra(Listactivity.ACCOUNTNAME, account.name);
        i.putExtra(Listactivity.CHANGED, isChanged);
        i.putExtra(Listactivity.SYNCED, isSynced);
        i.putExtra(Listactivity.SYNCINTERVAL, syncInterval);
        applicationContext.sendBroadcast(i);

    }

    /* It is possible for this function to throw exceptions; the original code caught
    MessagingException but just logged it instead of handling it.  This results in a possibility of
    returning null.  Removing the catch fixes the possible null reference but of course means that
    the caller becomes responsible.  This is the correct approach.
     */
    @NonNull
    private ImapNotes2Result ConnectToRemote() {
        AccountManager am = AccountManager.get(applicationContext);
        ImapNotes2Result res = SyncUtils.ConnectToRemote(
                am.getUserData(account, ConfigurationFieldNames.UserName),
                am.getPassword(account),
                am.getUserData(account, ConfigurationFieldNames.Server),
                am.getUserData(account, ConfigurationFieldNames.PortNumber),
                Security.from(am.getUserData(account, ConfigurationFieldNames.Security)),
                am.getUserData(account, ConfigurationFieldNames.ImapFolder));
        if (res.returnCode != ResultCodeSuccess) {
            // TODO: Notify the user?
            Log.d(TAG, "Connection problem: " + res.errorMessage);
        }
        return res;
    }

    private boolean handleNewNotes() {
        //Message message = null;
        boolean newNotesManaged = false;
        //AppendUID[] uids = null;
        //String rootString = applicationContext.getFilesDir() + File.separator + account.name;
        //File rootDir = new File(rootString);
        File accountDir = new File(applicationContext.getFilesDir(), account.name);
        File dirNew = new File(accountDir, "new");
        Log.d(TAG, "dn path: " + dirNew.getAbsolutePath());
        Log.d(TAG, "dn exists: " + Boolean.toString(dirNew.exists()));
        String[] listOfNew = dirNew.list();
        for (String fileNew : listOfNew) {
            //Log.d(TAG,"New Note to process:"+fileNew);
            newNotesManaged = true;
            // Read local new message from file
            Message message = SyncUtils.ReadMailFromFileNew(fileNew, dirNew);
            try {
                message.setFlag(Flags.Flag.SEEN, true); // set message as seen
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Send this new message to remote
            final MimeMessage[] msg = {(MimeMessage) message};

            try {
                AppendUID[] uids = SyncUtils.sendMessageToRemote(msg);
                // Update uid in database entry
                String newuid = Long.toString(uids[0].uid);
                storedNotes.UpdateANote(fileNew, newuid, account.name);
                // move new note from new dir, one level up
                File fileInNew = new File(dirNew, fileNew);
                File to = new File(accountDir, newuid);
                //noinspection ResultOfMethodCallIgnored
                fileInNew.renameTo(to);
            } catch (Exception e) {
                // TODO: Handle message properly.
                Log.d(TAG, e.getMessage());
            }
        }
        return newNotesManaged;
    }

    private boolean handleDeletedNotes() {
        //Message message = null;
        boolean deletedNotesManaged = false;
        String rootString = applicationContext.getFilesDir() + "/" + account.name;
        File rootDir = new File(rootString);
        File dirDeleted = new File(rootDir + "/deleted");
        String[] listOfDeleted = dirDeleted.list();
        for (String fileDeleted : listOfDeleted) {
            try {
                SyncUtils.DeleteNote(Integer.parseInt(fileDeleted));
            } catch (Exception e) {
                Log.d(TAG, "DeletNote failed:");
                e.printStackTrace();
            }

            // remove file from deleted
            File toDelete = new File(dirDeleted, fileDeleted);
            //noinspection ResultOfMethodCallIgnored
            toDelete.delete();
            deletedNotesManaged = true;
        }
        return deletedNotesManaged;
    }

}
