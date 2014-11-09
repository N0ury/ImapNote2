package com.Pau.imapnote2.Data;

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
	
	private String username;
	private String password;
	private String server;
	
	
	public ConfigurationFile(Context myContext){
		this.applicationContext = myContext;
		
		try {
			Document fileToLoad = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
									new File(this.applicationContext.getFilesDir()+"/imapnote2.conf"));
			this.username = this.LoadItemFromXML(fileToLoad, "username").item(0).getChildNodes().item(0).getNodeValue();
            		this.password = this.LoadItemFromXML(fileToLoad, "password").item(0).getChildNodes().item(0).getNodeValue();
            		this.server = this.LoadItemFromXML(fileToLoad, "server").item(0).getChildNodes().item(0).getNodeValue();
            
			
		} catch (Exception e) {
			Log.v("ImapNotes2", e.getMessage());
			this.username = null;
            		this.password = null;
            		this.server = null;
            
		}
		
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
	
	public void Clear(){
        new File(this.applicationContext.getFilesDir()+"/imapnote2.conf").delete();
        this.username=null;
        this.password=null;
        this.server=null;
        
	}
	
    public void SaveConfigurationToXML() throws IllegalArgumentException, IllegalStateException, IOException{
        FileOutputStream configurationFile = this.applicationContext.openFileOutput("imapnote2.conf", Context.MODE_PRIVATE);
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
        serializer.endTag(null, "Configuration"); 
        serializer.endDocument();
        serializer.flush();
        configurationFile.close();
        
    }
	
    private NodeList LoadItemFromXML(Document fileLoaded, String tag){
        return fileLoaded.getElementsByTagName(tag);
        
    }
	
	
}
