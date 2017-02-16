package com.Pau.ImapNotes2.Data;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ImapNotes2Account {

    private static final String TAG = "IN_ImapNotes2Account";
    @NonNull
    private final String accountName;
    @NonNull
    public final String username;
    @NonNull
    public final String password;
    @NonNull
    public final String server;
    @NonNull
    public final String portnum;
    @NonNull
    public final Security security;
    public final boolean usesticky;
    public final String syncInterval;
    @NonNull
    public final String imapfolder;
    @Nullable
    private final Account account;
    private boolean usesAutomaticMerge = false;


    public ImapNotes2Account(String accountName,
                             @NonNull String username,
                             @NonNull String password,
                             @NonNull String server,
                             @NonNull String portnum,
                             @NonNull Security security,
                             boolean usesticky,
                             boolean usesAutomaticMerge,
                             String syncinterval,
                             @NonNull String folderName) {
        account = null;
        this.accountName = accountName;
        this.username = username;
        this.password = password;
        this.server = server;
        this.security = security;
        this.portnum = portnum;
        this.usesticky = usesticky;
        this.usesAutomaticMerge = usesAutomaticMerge;
        this.imapfolder = folderName;
        this.syncInterval = syncinterval;
    }

    private File dirForNewFiles;

    private File dirForDeletedFiles;

    private File rootDir;

    public ImapNotes2Account(@NonNull Account account,
                             @NonNull Context applicationContext) {
        this.accountName = account.name;
        rootDir = new File(applicationContext.getFilesDir(), accountName);
        dirForNewFiles = new File(rootDir, "new");
        dirForDeletedFiles = new File(rootDir, "deleted");

        this.account = account;
        AccountManager am = AccountManager.get(applicationContext);
        syncInterval = am.getUserData(account, ConfigurationFieldNames.SyncInterval);
        username = am.getUserData(account, ConfigurationFieldNames.UserName);
        password = am.getPassword(account);
        server = am.getUserData(account, ConfigurationFieldNames.Server);
        portnum = am.getUserData(account, ConfigurationFieldNames.PortNumber);
        security = Security.from(am.getUserData(account, ConfigurationFieldNames.Security));
        usesticky = "true".equals(am.getUserData(account, ConfigurationFieldNames.UseSticky));
        imapfolder = am.getUserData(account, ConfigurationFieldNames.ImapFolder);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void CreateLocalDirectories() {
        Log.d(TAG, "CreateLocalDirs(String: " + accountName);
        dirForNewFiles.mkdirs();
        dirForDeletedFiles.mkdirs();
    }


    public void ClearHomeDir() {
        try {
            FileUtils.deleteDirectory(rootDir);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /*
    @NonNull
    public String toString() {
        return this.accountName + ":" + this.username + ":" + this.password + ":"
                + this.server + ":" + this.portnum + ":" + this.security + ":"
                + this.usesticky + ":" + this.imapfolder + ":" + Boolean.toString(this.accountHasChanged);
    }*/

    public String GetAccountName() {
        return accountName;
    }

    @Nullable
    public Account GetAccount() {
        return this.account;
    }

    //public void SetAccountname(String accountName) {
    //    this.accountName = accountName;
    //}
/*

    @NonNull
    public String GetUsername() {
        return this.username;
    }

    public void SetUsername(@NonNull String Username) {
        this.username = Username;
    }
*/

  /*  @NonNull
    public String GetPassword() {
        return this.password;
    }

    public void SetPassword(@NonNull String Password) {

        this.password = Password;
    }

    @NonNull
    public String GetServer() {
        return this.server;
    }

    public void SetServer(@NonNull String Server) {
        this.server = Server;
    }
*/
  /*  @NonNull
    public String GetPortnum() {
        return this.portnum;
    }

    public void SetPortnum(@NonNull String Portnum) {

        this.portnum = Portnum;
    }

    @NonNull
    public Security GetSecurity() {
        return security;
    }

    public void SetSecurity(@NonNull Security security) {

        this.security = security;
    }

    public void SetSecurity(String security) {
        Log.d(TAG, "Set: " + security);
        SetSecurity(Security.from(security));
    }

    public boolean GetUsesticky() {
        return this.usesticky;
    }
*/
    //public void SetUsesticky(boolean Usesticky) {
    //    this.usesticky = Usesticky;
    //}

    public String GetSyncinterval() {
        return this.syncInterval;
    }

    //public void SetSyncinterval(String Syncinterval) {
    //    this.syncInterval = Syncinterval;
    //}

    /*
    public void SetaccountHasNotChanged() {
        this.accountHasChanged = false;
    }
    */
/*

    @NonNull
    public Boolean GetaccountHasChanged() {
        return this.accountHasChanged;
    }
*/

/*
    @Nullable
    public String GetFoldername() {
        return this.imapfolder;
    }

    private void SetFolderName(@NonNull String folder) {
        this.imapfolder = folder;
    }

    public boolean GetUsesAutomaticMerge() {
    return this.usesAutomaticMerge;
    }
    public void SetUsesAutomaticMerge(boolean usesAutomaticMerge) {
        this.usesAutomaticMerge = usesAutomaticMerge;
    }
*/

/*
    public void Clear() {
        this.username = null;
        this.password = null;
        this.server = null;
        this.portnum = null;
        this.security = Security.None;
        this.usesticky = false;
        this.imapfolder = null;
        this.accountHasChanged = false;
    }*/
}
