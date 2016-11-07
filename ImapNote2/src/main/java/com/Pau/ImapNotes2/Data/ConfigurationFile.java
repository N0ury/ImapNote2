package com.Pau.ImapNotes2.Data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
//import android.util.Log;
import android.util.Xml;

import com.Pau.ImapNotes2.ImapNotes2;
import com.Pau.ImapNotes2.Sync.Security;

public class ConfigurationFile {

    // For logging.
    private static final String TAG = "IN_ConfigurationFile";

    // TODO: make all fields final.
    // The account name is the concatenation of the username and server.
    private String accountname;
    // User name on the IMAP server.
    private String username;
    private String password;
    // Address of the IMAP server
    private String server;
    // Port number.
    private String portnum;
    // TLS, etc.
    private Security security = Security.None;
    // ?
    private boolean usesticky;
    // The name of the IMAP folder to be used.
    private String imapfolder;


    public ConfigurationFile() {
        try {
            Document fileToLoad = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new File(ImapNotes2.ConfigurationFilePath()));
            username = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.UserName);
            password = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.Password);
            server = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.Server);
            imapfolder = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.ImapFolder);
            accountname = username + "@" + server;
            // All of these can be simplified by initializing the fields to the default values and
            // only setting when the value exists in the file.
            if (LoadItemFromXML(fileToLoad, ConfigurationFieldNames.Security).getLength() != 0) {
                security = Security.from(NodeValueFromXML(fileToLoad, ConfigurationFieldNames.Security));
            }
            if (LoadItemFromXML(fileToLoad, ConfigurationFieldNames.PortNumber).getLength() == 0) {
                // portnum option doesn't exist
                portnum = security.defaultPort;
            } else {
                portnum = NodeValueFromXML(fileToLoad, ConfigurationFieldNames.PortNumber);
            }
            if (LoadItemFromXML(fileToLoad, ConfigurationFieldNames.UseSticky).getLength() == 0)
                // usesticky option doesn't exist, say no
                usesticky = false;
            else
                usesticky = Boolean.parseBoolean(NodeValueFromXML(fileToLoad, ConfigurationFieldNames.UseSticky));

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

    public String GetAccountname() {
        return accountname;
    }

    public String GetUsername() {
        return username;
    }

    public void SetUsername(String Username) {
        username = Username;
    }

    public String GetPassword() {
        return password;
    }

    public void SetPassword(String Password) {
        password = Password;
    }

    public String GetServer() {
        return server;
    }

    public void SetServer(String Server) {
        server = Server;
    }

    public String GetPortnum() {
        return portnum;
    }

    public void SetPortnum(String Portnum) {
        portnum = Portnum;
    }

    public Security GetSecurity() {
        return security;
    }

    public void SetSecurity(Security security) {
        security = security;
    }

    public boolean GetUsesticky() {
        return usesticky;
    }
/*

    public void SetUsesticky(boolean usesticky) {
        this.usesticky = usesticky;
    }
*/

    public String GetFoldername() {
        return imapfolder;
    }


    public void Clear() {
        //noinspection ResultOfMethodCallIgnored
        new File(ImapNotes2.ConfigurationFilePath()).delete();
        username = null;
        password = null;
        server = null;
        portnum = null;
        security = null;
        usesticky = false;
        imapfolder = null;
    }

    // This function could take the context as an argument.
    // In addition the name of the file should be a named constant
    // because it is used elewhere.
    public void SaveConfigurationToXML()
            throws IllegalArgumentException, IllegalStateException, IOException {
        FileOutputStream configurationFile
                = ImapNotes2.getAppContext().openFileOutput(ImapNotes2.ConfigurationFilePath(),
                Context.MODE_PRIVATE);
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(configurationFile, "UTF-8");
        serializer.startDocument(null, true);
        serializer.startTag(null, "Configuration");
        SerializeText(serializer, ConfigurationFieldNames.UserName, username);
        SerializeText(serializer, ConfigurationFieldNames.Password, password);
        SerializeText(serializer, ConfigurationFieldNames.Server, server);
        SerializeText(serializer, ConfigurationFieldNames.PortNumber, portnum);
        SerializeText(serializer, ConfigurationFieldNames.Security, security.name());
        SerializeText(serializer, ConfigurationFieldNames.ImapFolder, imapfolder);
        SerializeText(serializer, ConfigurationFieldNames.UseSticky, String.valueOf(usesticky));
        serializer.endTag(null, "Configuration");
        serializer.endDocument();
        serializer.flush();
        configurationFile.close();
    }

    // Avoid repeated literal tag names.
    private void SerializeText(XmlSerializer serializer,
                               String tag,
                               String text)
            throws IOException {
        serializer.startTag(null, tag);
        serializer.text(text);
        serializer.endTag(null, tag);
    }

    private NodeList LoadItemFromXML(Document fileLoaded,
                                     String tag) {
        return fileLoaded.getElementsByTagName(tag);
    }

    // Reduce clutter and improve maintainability.
    private String NodeValueFromXML(Document fileLoaded,
                                    String tag) {
        return LoadItemFromXML(fileLoaded, tag).item(0).getChildNodes().item(0).getNodeValue();
    }


}
