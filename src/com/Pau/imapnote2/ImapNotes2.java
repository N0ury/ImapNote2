package com.Pau.imapnote2;

import java.util.ArrayList;

import com.Pau.imapnote2.Data.ConfigurationFile;
import com.Pau.imapnote2.Miscs.Imaper;
import com.Pau.imapnote2.Miscs.OneNote;

import android.app.Application;

public class ImapNotes2 extends Application {
	
	private ConfigurationFile thisSessionConfigurationFile;
	private Imaper thisSessionImapFolder;
	private ArrayList<OneNote> noteList;

	
	public void SetConfigurationFile(ConfigurationFile currentSettings){
		this.thisSessionConfigurationFile = currentSettings;
		
	}
	
	public ConfigurationFile GetConfigurationFile(){
		return this.thisSessionConfigurationFile;
		
	}
	
	public void SetImaper(Imaper currentImaper){
		this.thisSessionImapFolder = currentImaper;
		
	}
	
	public Imaper GetImaper(){
		return this.thisSessionImapFolder;
		
	}
	
	public void SetNotesList(ArrayList<OneNote> currentNotesList){
		this.noteList = currentNotesList;
		
	}
	
	public ArrayList<OneNote> GetNotesList(){
		return this.noteList;
		
	}

}
