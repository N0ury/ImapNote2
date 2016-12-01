package com.Pau.ImapNotes2.Data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.Pau.ImapNotes2.ImapNotes2k;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

//import android.util.Log;

public class ConfigurationFile {

    // For logging.
    private static final String TAG = "IN_ConfigurationFile";

    // TODO: make all fields final.
    // The account name is the concatenation of the username and server.
    @Nullable
    private String accountname;
    // User name on the IMAP server.
    @Nullable
    private String username;
    @Nullable
    private String password;
    // Address of the IMAP server
    @Nullable
    private String server;
    // Port number.
    @Nullable
    private String portnum;
    // TLS, etc.
    @NonNull
    private Security security = Security.None;
    // ?
    private boolean usesticky;
    // The name of the IMAP folder to be used.
    @Nullable
    private String imapfolder;

    @NonNull
    private Context applicationContext;


    public ConfigurationFile(@NonNull Context applicationContext) {
        this.applicationContext = applicationContext;
        try {
            Document fileToLoad = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new File(ImapNotes2k.ConfigurationFilePath(applicationContext)));
            username = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.UserName);
            password = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.Password);
            server = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.Server);
            imapfolder = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.ImapFolder);
            accountname = username + "@" + server;
            // All of these can be simplified by initializing the fields to the default values and
            // only setting when the value exists in the file.
            if (LoadItemFromXML(fileToLoad, ConfigurationFieldNames.Security).getLength() != 0) {
                Log.d(TAG, "nfx: " + NodeValueFromXML(fileToLoad, ConfigurationFieldNames.Security));
                security = Security.from(NodeValueFromXML(fileToLoad, ConfigurationFieldNames.Security));
            }
            if (LoadItemFromXML(fileToLoad, ConfigurationFieldNames.PortNumber).getLength() == 0) {
                // portnum option doesn't exist
                portnum = security.defaultPort;
            } else {
                portnum = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.PortNumber);
            }
            // usesticky option doesn't exist, say no
            usesticky = LoadItemFromXML(fileToLoad, ConfigurationFieldNames.UseSticky).getLength() != 0 &&
                    Boolean.parseBoolean(NodeValueFromXML(fileToLoad, ConfigurationFieldNames.UseSticky));

//Log.d(TAG, "conf file present, we read data");
        } catch (Exception e) {
            // TODO: This catch should be turned into a simple if then and the catch
            // reserved for conditions that cannot be checked for.
//Log.d(TAG, "Conf file absent, go to the exception that initializes variables");
            accountname = "";
            username = "";
            password = "";
            server = "";
            security = Security.None;
            portnum = security.defaultPort;
            usesticky = false;
            imapfolder = "";
        }
    }

    @Nullable
    public String GetAccountname() {
        return accountname;
    }

    @Nullable
    public String GetUsername() {
        return username;
    }

// --Commented out by Inspection START (11/26/16 11:42 PM):
//    public void SetUsername(String Username) {
//        username = Username;
//    }
// --Commented out by Inspection STOP (11/26/16 11:42 PM)

    @Nullable
    public String GetPassword() {
        return password;
    }

// --Commented out by Inspection START (11/26/16 11:42 PM):
//    public void SetPassword(String Password) {
//        password = Password;
//    }
// --Commented out by Inspection STOP (11/26/16 11:42 PM)

    @Nullable
    public String GetServer() {
        return server;
    }

// --Commented out by Inspection START (11/26/16 11:42 PM):
//    public void SetServer(String Server) {
//        server = Server;
//    }
// --Commented out by Inspection STOP (11/26/16 11:42 PM)

    @Nullable
    public String GetPortnum() {
        return portnum;
    }

// --Commented out by Inspection START (11/26/16 11:42 PM):
//    public void SetPortnum(String Portnum) {
//        portnum = Portnum;
//    }
// --Commented out by Inspection STOP (11/26/16 11:42 PM)

    @NonNull
    public Security GetSecurity() {
        return security;
    }
/*

    public void SetSecurity(Security security) {
        security = security;
    }
*/

    public boolean GetUsesticky() {
        return usesticky;
    }
/*

    public void SetUsesticky(boolean usesticky) {
        this.usesticky = usesticky;
    }
*/

    @Nullable
    public String GetFoldername() {
        return imapfolder;
    }


    public void Clear() {
        //noinspection ResultOfMethodCallIgnored
        new File(ImapNotes2k.ConfigurationFilePath(applicationContext)).delete();
        username = null;
        password = null;
        server = null;
        portnum = null;
        security = Security.None;
        usesticky = false;
        imapfolder = null;
    }

// --Commented out by Inspection START (11/26/16 11:42 PM):
//    // This function could take the context as an argument.
//    // In addition the name of the file should be a named constant
//    // because it is used elewhere.
//    public void SaveConfigurationToXML()
//            throws IllegalArgumentException, IllegalStateException, IOException {
//        FileOutputStream configurationFile
//                = ImapNotes2k.getAppContext().openFileOutput(ImapNotes2k.ConfigurationFilePath(),
//                Context.MODE_PRIVATE);
//        XmlSerializer serializer = Xml.newSerializer();
//        serializer.setOutput(configurationFile, "UTF-8");
//        serializer.startDocument(null, true);
//        serializer.startTag(null, "Configuration");
//        SerializeText(serializer, ConfigurationFieldNames.UserName, username);
//        SerializeText(serializer, ConfigurationFieldNames.Password, password);
//        SerializeText(serializer, ConfigurationFieldNames.Server, server);
//        SerializeText(serializer, ConfigurationFieldNames.PortNumber, portnum);
//        SerializeText(serializer, ConfigurationFieldNames.Security, security.name());
//        SerializeText(serializer, ConfigurationFieldNames.ImapFolder, imapfolder);
//        SerializeText(serializer, ConfigurationFieldNames.UseSticky, String.valueOf(usesticky));
//        serializer.endTag(null, "Configuration");
//        serializer.endDocument();
//        serializer.flush();
//        configurationFile.close();
//    }
// --Commented out by Inspection STOP (11/26/16 11:42 PM)
/*
    // Avoid repeated literal tag names.
    private void SerializeText(@NonNull XmlSerializer serializer,
                               String tag,
                               String text)
            throws IOException {
        serializer.startTag(null, tag);
        serializer.text(text);
        serializer.endTag(null, tag);
    }*/

    private NodeList LoadItemFromXML(@NonNull Document fileLoaded,
                                     String tag) {
        return fileLoaded.getElementsByTagName(tag);
    }

    // Reduce clutter and improve maintainability.
    private String NodeValueFromXML(@NonNull Document fileLoaded,
                                    String tag) {
        return LoadItemFromXML(fileLoaded, tag).item(0).getChildNodes().item(0).getNodeValue();
    }


}
