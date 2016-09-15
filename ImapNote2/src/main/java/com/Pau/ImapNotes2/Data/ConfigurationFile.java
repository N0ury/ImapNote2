package com.Pau.ImapNotes2.Data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class ConfigurationFile {

    private Context applicationContext;
    private static final String TAG = "IN_ConfigurationFile";
    
    private String accountname;
    private String username;
    private String password;
    private String server;
    private String portnum;
    private String security;
    private String usesticky;
    private String imapfolder;
    
    
    public ConfigurationFile(Context myContext){
        this.applicationContext = myContext;
        
        try {
            Document fileToLoad = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new File(this.applicationContext.getFilesDir()+"/ImapNotes2.conf"));
            this.username = this.LoadItemFromXML(fileToLoad, "username").item(0).getChildNodes().item(0).getNodeValue();
            this.password = this.LoadItemFromXML(fileToLoad, "password").item(0).getChildNodes().item(0).getNodeValue();
            this.server = this.LoadItemFromXML(fileToLoad, "server").item(0).getChildNodes().item(0).getNodeValue();
            this.imapfolder = this.LoadItemFromXML(fileToLoad, "imapfolder").item(0).getChildNodes().item(0).getNodeValue();
            this.accountname = this.username + "@" + this.server;
            if (this.LoadItemFromXML(fileToLoad, "portnum").getLength() == 0)
                // portnum option doesn't exist
                this.portnum = "";
            else
                this.portnum = this.LoadItemFromXML(fileToLoad, "portnum").item(0).getChildNodes().item(0).getNodeValue();
            if (this.LoadItemFromXML(fileToLoad, "security").getLength() == 0)
                // security option doesn't exist, say "0"
                this.security = "0";
            else
                this.security = this.LoadItemFromXML(fileToLoad, "security").item(0).getChildNodes().item(0).getNodeValue();
            if (this.LoadItemFromXML(fileToLoad, "usesticky").getLength() == 0)
                // usesticky option doesn't exist, say no
                this.usesticky = "false";
            else
                this.usesticky = this.LoadItemFromXML(fileToLoad, "usesticky").item(0).getChildNodes().item(0).getNodeValue();

//Log.d(TAG, "conf file present, we read data");
        } catch (Exception e) {
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
    
    public String GetAccountname(){
        return this.accountname;
    }
 
    public String GetUsername(){
        return this.username;
    }
 
    public void SetUsername(String Username){
        this.username = Username;
    }
 
    public String GetPassword(){
        return this.password;
    }
 
    public void SetPassword(String Password){
        this.password = Password;
    }
    
    public String GetServer(){
        return this.server;
    }
 
    public void SetServer(String Server){
        this.server = Server;
    }
    
    public String GetPortnum(){
        return this.portnum;
    }
 
    public void SetPortnum(String Portnum){
        this.portnum = Portnum;
    }
    
    public String GetSecurity(){
        return this.security;
    }
 
    public void SetSecurity(String Security){
        this.security = Security;
    }
    
    public String GetUsesticky(){
        return this.usesticky;
    }
 
    public void SetUsesticky(String Usesticky){
        this.usesticky = Usesticky;
    }

    public String GetFoldername(){
        return this.imapfolder;
    }
    
    public void Clear(){
        new File(this.applicationContext.getFilesDir()+"/ImapNotes2.conf").delete();
        this.username=null;
        this.password=null;
        this.server=null;
        this.portnum=null;
        this.security=null;
        this.usesticky=null;
        this.imapfolder = null;
    }
    
    public void SaveConfigurationToXML() throws IllegalArgumentException, IllegalStateException, IOException{
        FileOutputStream configurationFile = this.applicationContext.openFileOutput("ImapNotes2.conf", Context.MODE_PRIVATE);
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(configurationFile, "UTF-8");
        serializer.startDocument(null, Boolean.valueOf(true)); 
        serializer.startTag(null, "Configuration"); 
        serializer.startTag(null, "username");
        serializer.text(this.username);
        serializer.endTag(null, "username");
        serializer.startTag(null, "password");
        serializer.text(this.password);
        serializer.endTag(null, "password");
        serializer.startTag(null, "server");
        serializer.text(this.server);
        serializer.endTag(null, "server");
        serializer.startTag(null, "portnum");
        serializer.text(this.portnum);
        serializer.endTag(null, "portnum");
        serializer.startTag(null, "security");
        serializer.text(this.security);
        serializer.endTag(null, "security");
        serializer.startTag(null,"imapfolder");
        serializer.text(this.imapfolder);
        serializer.endTag(null, "imapfolder");
        serializer.startTag(null, "usesticky");
        serializer.text(this.usesticky);
        serializer.endTag(null, "usesticky");
        serializer.endTag(null, "Configuration"); 
        serializer.endDocument();
        serializer.flush();
        configurationFile.close();
    }
    
    private NodeList LoadItemFromXML(Document fileLoaded, String tag){
        return fileLoaded.getElementsByTagName(tag);
        
    }
}
