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

        MailSSLSocketFactory sf;
        try {
            sf = new MailSSLSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return new ImapNotes2Result(-1,
                    "Can't connect to server",
                    -1,
                    null);
        }

        String proto = security.proto;

        Properties props = new Properties();

        props.setProperty(String.format("mail.%s.host", proto), server);
        props.setProperty(String.format("mail.%s.port", proto), portnum);
        props.setProperty("mail.store.protocol", proto);

        if (security.acceptcrt) {
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
        //noinspection ConstantConditions,ConstantConditions
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

            return new ImapNotes2Result(ResultCodeSuccess,
                    "",
                    -1,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
            return new ImapNotes2Result(ResultCodeException,
                    e.getMessage(),
                    -1,
                    null);
        }
    }

    private boolean IsConnected() {
        return store != null && store.isConnected();
    }

}
