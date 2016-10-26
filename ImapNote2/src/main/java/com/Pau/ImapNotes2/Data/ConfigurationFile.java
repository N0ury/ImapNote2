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

public class ConfigurationFile {

    // For logging.
    private static final String TAG = "IN_ConfigurationFile";

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
    private String security;
    // ?
    private String usesticky;
    // The name of the IMAP folder to be used.
    private String imapfolder;


    public ConfigurationFile() {
        try {
            Document fileToLoad = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new File(ImapNotes2.ConfigurationFilePath()));
            this.username = this.NodeValueFromXML(fileToLoad, "username");
            this.password = this.NodeValueFromXML(fileToLoad, "password");
            this.server = this.NodeValueFromXML(fileToLoad, "server");
            this.imapfolder = this.NodeValueFromXML(fileToLoad, "imapfolder");
            this.accountname = this.username + "@" + this.server;
            // All of these can be simplified by initializing the fields to the default values and
            // only setting when the value exists in the file.
            if (this.LoadItemFromXML(fileToLoad, "portnum").getLength() == 0)
                // portnum option doesn't exist
                this.portnum = "";
            else
                this.portnum = this.NodeValueFromXML(fileToLoad, "portnum");
            if (this.LoadItemFromXML(fileToLoad, "security").getLength() == 0)
                // security option doesn't exist, say "0"
                this.security = "0";
            else
                this.security = this.NodeValueFromXML(fileToLoad, "security");
            if (this.LoadItemFromXML(fileToLoad, "usesticky").getLength() == 0)
                // usesticky option doesn't exist, say no
                this.usesticky = "false";
            else
                this.usesticky = this.NodeValueFromXML(fileToLoad, "usesticky");

//Log.d(TAG, "conf file present, we read data");
        } catch (Exception e) {
            // This catch should be turned into a simple if then and the catch
            // reserved for conditions that cannot be checked for.
//Log.d(TAG, "Conf file absent, go to the exception that initializes variables");
            this.accountname = "";
            this.username = "";
            this.password = "";
            this.server = "";
            this.portnum = "";
            this.security = "0";
            this.usesticky = "false";
            this.imapfolder = "";
        }
    }

    public String GetAccountname() {
        return this.accountname;
    }

    public String GetUsername() {
        return this.username;
    }

    public void SetUsername(String Username) {
        this.username = Username;
    }

    public String GetPassword() {
        return this.password;
    }

    public void SetPassword(String Password) {
        this.password = Password;
    }

    public String GetServer() {
        return this.server;
    }

    public void SetServer(String Server) {
        this.server = Server;
    }

    public String GetPortnum() {
        return this.portnum;
    }

    public void SetPortnum(String Portnum) {
        this.portnum = Portnum;
    }

    public String GetSecurity() {
        return this.security;
    }

    public void SetSecurity(String Security) {
        this.security = Security;
    }

    public String GetUsesticky() {
        return this.usesticky;
    }

    public void SetUsesticky(String Usesticky) {
        this.usesticky = Usesticky;
    }

    public String GetFoldername() {
        return this.imapfolder;
    }


    public void Clear() {
        new File(ImapNotes2.ConfigurationFilePath()).delete();
        this.username = null;
        this.password = null;
        this.server = null;
        this.portnum = null;
        this.security = null;
        this.usesticky = null;
        this.imapfolder = null;
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
        SerializeText(serializer, "username", this.username);
        SerializeText(serializer, "password", this.password);
        SerializeText(serializer, "server", this.server);
        SerializeText(serializer, "portnum", this.portnum);
        SerializeText(serializer, "security", this.security);
        SerializeText(serializer, "imapfolder", this.imapfolder);
        SerializeText(serializer, "usesticky", this.usesticky);
        serializer.endTag(null, "Configuration");
        serializer.endDocument();
        serializer.flush();
        configurationFile.close();
    }

    // Avoid repeated literal tag names.
    private void SerializeText(XmlSerializer serializer, String tag, String text)
            throws IOException {
        serializer.startTag(null, tag);
        serializer.text(text);
        serializer.endTag(null, tag);
    }

    private NodeList LoadItemFromXML(Document fileLoaded, String tag) {
        return fileLoaded.getElementsByTagName(tag);

    }

    // Reduce clutter and improve maintainability.
    private String NodeValueFromXML(Document fileLoaded, String tag) {
        return LoadItemFromXML(fileLoaded, tag).item(0).getChildNodes().item(0).getNodeValue();
    }


}
