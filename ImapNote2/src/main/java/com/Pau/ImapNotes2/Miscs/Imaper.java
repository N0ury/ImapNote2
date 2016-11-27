package com.Pau.ImapNotes2.Miscs;

import android.support.annotation.NonNull;
import android.util.Log;

import com.Pau.ImapNotes2.Data.Security;
import com.sun.mail.util.MailSSLSocketFactory;

import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class Imaper {

    private Store store;
    private static final String TAG = "IN_Imaper";

    private Long UIDValidity;
    // --Commented out by Inspection (11/26/16 11:43 PM):public static final String PREFS_NAME = "PrefsFile";

    public static final int ResultCodeSuccess = 0;
    public static final int ResultCodeException = -2;
    public static final int ResultCodeCantConnect = -1;

    @NonNull
    public ImapNotes2Result ConnectToProvider(String username,
                                              String password,
                                              String server,
                                              String portnum,
                                              @NonNull Security security) throws MessagingException {
        if (IsConnected()) {
            store.close();
        }

        ImapNotes2Result res = new ImapNotes2Result();
        String proto = security.proto;
        boolean acceptcrt = security.acceptcrt;
/*        int security_i = Integer.parseInt(security);
        switch (security_i) {
            case 0:
                // None
                proto = "imap";
                acceptcrt = "";
                break;
            case 1:
                // SSL/TLS
                proto = "imaps";
                acceptcrt = "false";
                break;
            case 2:
                // SSL/TLS/TRUST ALL
                proto = "imaps";
                acceptcrt = "true";
                break;
            case 3:
                // STARTTLS
                proto = "imap";
                acceptcrt = "false";
                break;
            case 4:
                // STARTTLS/TRUST ALL
                proto = "imap";
                acceptcrt = "true";
                break;
////////////////////// Change this
            default:
                proto = "Invalid proto";
                break;
        }*/
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            res.errorMessage = "Can't connect to server";
            res.returnCode = -1;
            return res;
        }

        Properties props = new Properties();

        props.setProperty(String.format("mail.%s.host", proto), server);
        props.setProperty(String.format("mail.%s.port", proto), portnum);
        props.setProperty("mail.store.protocol", proto);

        if (acceptcrt) {
            sf.setTrustedHosts(new String[]{server});
            if (proto.equals("imap")) {
                props.put("mail.imap.ssl.socketFactory", sf);
                props.put("mail.imap.starttls.enable", "true");
            }
        } else if (security != Security.None) {
            props.put(String.format("mail.%s.ssl.checkserveridentity", proto), "true");
            if (proto.equals("imap")) {
                props.put("mail.imap.starttls.enable", "true");
            }
        }

        if (proto.equals("imaps")) {
            props.put("mail.imaps.socketFactory", sf);
        }

        props.setProperty("mail.imap.connectiontimeout", "1000");
        Boolean useProxy = false;
        //noinspection ConstantConditions
        // TODO: implement proxy handling properly.
        //noinspection ConstantConditions
        if (useProxy) {
            props.put("mail.imap.socks.host", "10.0.2.2");
            props.put("mail.imap.socks.port", "1080");
/*
        props.put("proxySet","true");
        props.put("socksProxyHost","10.0.2.2");
        props.put("socksProxyPort","1080");
        props.put("sun.net.spi.nameservice.provider.1", "dns,sun");
        props.put("sun.net.spi.nameservice.nameservers", "192.168.0.99");
*/
        }
        Session session = Session.getInstance(props, null);
//session.setDebug(true);
        store = session.getStore(proto);
        try {
            store.connect(server, username, password);
            //Boolean hasUIDPLUS = ((IMAPStore) store).hasCapability("UIDPLUS");
//Log.d(TAG, "has UIDPLUS="+hasUIDPLUS);

            //Folder[] folders = store.getPersonalNamespaces();
            //Folder folder = folders[0];
//Log.d(TAG,"Personal Namespaces="+folder.getFullName());
            /*if (folderoverride.length() > 0) {
                Imaper.sfolder = folderoverride;
            } else if (folder.getFullName().length() == 0) {
                Imaper.sfolder = "Notes";
            } else {
                char separator = folder.getSeparator();
                Imaper.sfolder = folder.getFullName() + separator + "Notes";
            }
*/
            res.errorMessage = "";
            res.returnCode = ResultCodeSuccess;
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
            res.errorMessage = e.getMessage();
            res.returnCode = ResultCodeException;
            return res;
        }

    }

    private boolean IsConnected() {
        return store != null && store.isConnected();
    }

// --Commented out by Inspection START (11/26/16 11:44 PM):
//    // Put values in shared preferences:
//    public void SetPrefs() {
//        SharedPreferences preferences = ImapNotes2k.getAppContext().getSharedPreferences(Listactivity.imapNotes2Account.GetAccountname(), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("Name", "valid_data");
//        editor.putLong("UIDValidity", UIDValidity);
//        editor.apply();
//    }
// --Commented out by Inspection STOP (11/26/16 11:44 PM)

// --Commented out by Inspection START (11/26/16 11:44 PM):
//    // Retrieve values from shared preferences:
//    public void GetPrefs() {
//        SharedPreferences preferences = (ImapNotes2k.getAppContext()).getSharedPreferences(Listactivity.imapNotes2Account.GetAccountname(), Context.MODE_PRIVATE);
//        String name = preferences.getString("Name", "");
//        if (!name.equalsIgnoreCase("")) {
//            UIDValidity = preferences.getLong("UIDValidity", -1);
//        }
//    }
// --Commented out by Inspection STOP (11/26/16 11:44 PM)
}
