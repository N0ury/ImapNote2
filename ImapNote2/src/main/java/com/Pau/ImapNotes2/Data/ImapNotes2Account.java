package com.Pau.ImapNotes2.Data;

import android.accounts.Account;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class ImapNotes2Account {

    private static final String TAG = "IN_ImapNotes2Account";
    private String accountname = "";
    // TODO: Why are the username, password, etc. nullable?
    @NonNull
    private String username = "";
    @NonNull
    private String password = "";
    @NonNull
    private String server = "";
    @NonNull
    private String portnum = "";
    @NonNull
    private Security security = Security.None;
    private boolean usesticky = false;
    private String syncinterval = "15";
    @NonNull
    private String imapfolder = "";
    private boolean accountHasChanged = false;
    @Nullable
    private Account account = null;


    public ImapNotes2Account() {
    }

    @NonNull
    public String toString() {
        return this.accountname + ":" + this.username + ":" + this.password + ":"
                + this.server + ":" + this.portnum + ":" + this.security + ":"
                + this.usesticky + ":" + this.imapfolder + ":" + Boolean.toString(this.accountHasChanged);
    }

    public String GetAccountname() {
        return this.accountname;
    }

    public void SetAccount(Account account) {
        this.account = account;
    }

    @Nullable
    public Account GetAccount() {
        return this.account;
    }

    public void SetAccountname(String Accountname) {
        if (this.accountname.equals(Accountname)) this.accountHasChanged = true;
        this.accountname = Accountname;
    }

    @NonNull
    public String GetUsername() {
        return this.username;
    }

    public void SetUsername(@NonNull String Username) {
        this.username = Username;
    }

    @NonNull
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

    @NonNull
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

    public void SetUsesticky(boolean Usesticky) {
        this.usesticky = Usesticky;
    }

    public String GetSyncinterval() {
        return this.syncinterval;
    }

    public void SetSyncinterval(String Syncinterval) {
        this.syncinterval = Syncinterval;
    }

    public void SetaccountHasChanged() {
        this.accountHasChanged = true;
    }
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

    @Nullable
    public String GetFoldername() {
        return this.imapfolder;
    }

    public void SetFoldername(@NonNull String folder) {
        this.imapfolder = folder;
    }
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
