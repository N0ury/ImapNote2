package com.Pau.ImapNotes2.Miscs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Properties;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.MailSSLSocketFactory;
import java.util.regex.*;

import com.Pau.ImapNotes2.Miscs.Sticky;
import com.Pau.ImapNotes2.ImapNotes2;
import com.Pau.ImapNotes2.Listactivity;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;

public class Imaper {
  
  private Store store;
  private Session session;
  private static final String TAG = "IN_Imaper";
  private String proto;
  private String acceptcrt;
  private static String sfolder = "Notes";
  private String folderoverride;
  private Folder notesFolder = null;
  private ImapNotes2Result res;
  private Long UIDValidity;
private Boolean useProxy = false;
public static final String PREFS_NAME = "PrefsFile";
  
  public ImapNotes2Result ConnectToProvider(String username, String password, String server, String portnum, String security, String usesticky, String override) throws MessagingException{
    if (this.IsConnected())
      this.store.close();
    
  res = new ImapNotes2Result();
    if (override==null) {
      this.folderoverride = "";
    } else {
      this.folderoverride = override;
    }
  this.proto = "";
  this.acceptcrt = "";
  int security_i = Integer.parseInt(security);
  switch (security_i) {
    case 0:
      // None
      this.proto = "imap";
      this.acceptcrt = "";
      break;
    case 1:
      // SSL/TLS
      this.proto = "imaps";
      this.acceptcrt = "false";
      break;
    case 2:
      // SSL/TLS/TRUST ALL
      this.proto = "imaps";
      this.acceptcrt = "true";
      break;
    case 3:
      // STARTTLS
      this.proto = "imap";
      this.acceptcrt = "false";
      break;
    case 4:
      // STARTTLS/TRUST ALL
      this.proto = "imap";
      this.acceptcrt = "true";
      break;
////////////////////// Change this
    default: this.proto = "Invalid proto";
       break;
  }
    MailSSLSocketFactory sf = null;
    try {
      sf = new MailSSLSocketFactory();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      this.res.errorMessage = "Can't connect to server";
      this.res.returnCode = -1;
      return this.res;
    }

    Properties props = new Properties();

    props.setProperty(String.format("mail.%s.host", this.proto), server);
    props.setProperty(String.format("mail.%s.port", this.proto), portnum);
    props.setProperty("mail.store.protocol", this.proto);

    if ((this.acceptcrt.equals("true"))) {
      sf.setTrustedHosts(new String[] {server});
      if (this.proto.equals("imap")) {
        props.put("mail.imap.ssl.socketFactory", sf);
        props.put("mail.imap.starttls.enable", "true");
      }
    } else if (this.acceptcrt.equals("false")) {
      props.put(String.format("mail.%s.ssl.checkserveridentity", this.proto), "true");
      if (this.proto.equals("imap")) {
        props.put("mail.imap.starttls.enable", "true");
      }
    }

    if (this.proto.equals("imaps")) {
      props.put("mail.imaps.socketFactory", sf);
    }

    props.setProperty("mail.imap.connectiontimeout","1000");
    if (this.useProxy) {
        props.put("mail.imap.socks.host","10.0.2.2");
        props.put("mail.imap.socks.port","1080");
/*
        props.put("proxySet","true");
        props.put("socksProxyHost","10.0.2.2");
        props.put("socksProxyPort","1080");
        props.put("sun.net.spi.nameservice.provider.1", "dns,sun");
        props.put("sun.net.spi.nameservice.nameservers", "192.168.0.99");
*/
    }
    this.session = Session.getInstance(props, null);
//this.session.setDebug(true);
    this.store = this.session.getStore(this.proto);
    try {
      this.store.connect(server, username, password);
Boolean hasUIDPLUS = ((IMAPStore) this.store).hasCapability("UIDPLUS");
//Log.d(TAG, "has UIDPLUS="+hasUIDPLUS);

      Folder[] folders = store.getPersonalNamespaces();
      Folder folder = folders[0];
//Log.d(TAG,"Personal Namespaces="+folder.getFullName());
      if (this.folderoverride.length() > 0) {
          Imaper.sfolder = this.folderoverride;
      } else if (folder.getFullName().length() == 0) {
          Imaper.sfolder = "Notes";
      } else {
          char separator = folder.getSeparator();
          Imaper.sfolder = folder.getFullName() + separator + "Notes";
      }
      this.res.errorMessage = "";
      this.res.returnCode = 0;
      return this.res;
    } catch (Exception e) {
      e.printStackTrace();
      Log.d(TAG, e.getMessage());
      this.res.errorMessage = e.getMessage();
      this.res.returnCode = -2;
      return this.res;
    }

  }
  
  public boolean IsConnected(){
    return this.store!=null && this.store.isConnected();
  }

  // Put values in shared preferences:
  public void SetPrefs() {
    SharedPreferences preferences = ImapNotes2.getAppContext().getSharedPreferences(Listactivity.imapNotes2Account.GetAccountname(), Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString("Name","valid_data");
    editor.putLong("UIDValidity", this.UIDValidity);
    editor.apply();
  }

  // Retrieve values from shared preferences:
  public void GetPrefs() {
    SharedPreferences preferences = (ImapNotes2.getAppContext()).getSharedPreferences(Listactivity.imapNotes2Account.GetAccountname(), Context.MODE_PRIVATE);
    String name = preferences.getString("Name", "");
    if(!name.equalsIgnoreCase("")) {
      this.UIDValidity = preferences.getLong("UIDValidity", -1);
    }
  }
}
