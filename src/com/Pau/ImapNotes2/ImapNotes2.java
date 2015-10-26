package com.Pau.ImapNotes2;

import java.util.ArrayList;

import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.OneNote;

import android.app.Application;
import android.content.Context;

public class ImapNotes2 extends Application {
	
	private ConfigurationFile thisSessionConfigurationFile;
	private Imaper thisSessionImapFolder;
	private ArrayList<OneNote> noteList;
        private static Context context;

        public void onCreate(){
            super.onCreate();
            ImapNotes2.context = getApplicationContext();
        }

        public static Context getAppContext() {
            return ImapNotes2.context;
        }

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
