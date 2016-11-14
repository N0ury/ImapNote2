package com.Pau.ImapNotes2.Data;

import android.accounts.Account;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ImapNotes2Account {

    private String accountname = "";
    // TODO: Why are the username, password, etc. nullable?
    @Nullable
    private String username = "";
    @Nullable
    private String password = "";
    @Nullable
    private String server = "";
    @Nullable
    private String portnum = "";
    @Nullable
    private Security security = null;
    private boolean usesticky = false;
    private String syncinterval = "15";
    @Nullable
    private String imapfolder = "";
    @NonNull
    private Boolean accountHasChanged = false;
    @Nullable
    private Account account = null;


    public ImapNotes2Account() {
    }

    @NonNull
    public String toString() {
        return this.accountname + ":" + this.username + ":" + this.password + ":"
                + this.server + ":" + this.portnum + ":" + this.security + ":"
                + this.usesticky + ":" + this.imapfolder + ":" + this.accountHasChanged.toString();
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

    @Nullable
    public String GetUsername() {
        return this.username;
    }

    public void SetUsername(String Username) {
        this.username = Username;
    }

    @Nullable
    public String GetPassword() {
        return this.password;
    }

    public void SetPassword(String Password) {
        this.password = Password;
    }

    @Nullable
    public String GetServer() {
        return this.server;
    }

    public void SetServer(String Server) {
        this.server = Server;
    }

    @Nullable
    public String GetPortnum() {
        return this.portnum;
    }

    public void SetPortnum(String Portnum) {
        this.portnum = Portnum;
    }

    @Nullable
    public Security GetSecurity() {
        return security;
    }

    public void SetSecurity(Security security) {
        this.security = security;
    }

    public void SetSecurity(String security) {
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

    public void SetaccountHasNotChanged() {
        this.accountHasChanged = false;
    }

    @NonNull
    public Boolean GetaccountHasChanged() {
        return this.accountHasChanged;
    }

    @Nullable
    public String GetFoldername() {
        return this.imapfolder;
    }

    public void SetFoldername(String folder) {
        this.imapfolder = folder;
    }

    public void Clear() {
        this.username = null;
        this.password = null;
        this.server = null;
        this.portnum = null;
        this.security = null;
        this.usesticky = false;
        this.imapfolder = null;
        this.accountHasChanged = false;
    }
}
