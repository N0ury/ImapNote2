package com.Pau.ImapNotes2;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Data.OneNote;
import com.Pau.ImapNotes2.Miscs.Imaper;

import java.io.File;
import java.util.ArrayList;


/*
Changed name by appending a k so that I can have this and the original installed side by side,
perhaps.
 */
public class ImapNotes2k extends Application {

    private ConfigurationFile thisSessionConfigurationFile;
    private Imaper thisSessionImapFolder;
    private ArrayList<OneNote> noteList;

    private static Context context;
    private static final String configurationFileName = "ImapNotes2.conf";

    // Called when starting the application.
    public void onCreate() {
        super.onCreate();
        // Save the context in a static so that it is easy to access everywhere.
        ImapNotes2k.context = getApplicationContext();
    }

    // Simplify access to the application context.  But why not just use getApplicationContext
    // everywhere?
    public static Context getAppContext() {
        return ImapNotes2k.context;
    }

    @NonNull
    public static String ConfigurationFilePath() {
        return ConfigurationDirPath() + "/" + configurationFileName;
    }

    public static String ConfigurationDirPath() {
        return ImapNotes2k.ConfigurationDir().getPath();
    }

    public static File ConfigurationDir() {
        return ImapNotes2k.getAppContext().getFilesDir();
    }

// --Commented out by Inspection START (11/26/16 11:44 PM):
//    // ?
//    public void SetConfigurationFile(ConfigurationFile currentSettings) {
//        this.thisSessionConfigurationFile = currentSettings;
//    }
// --Commented out by Inspection STOP (11/26/16 11:44 PM)

// --Commented out by Inspection START (11/26/16 11:44 PM):
//    // ?
//    public ConfigurationFile GetConfigurationFile() {
//        return this.thisSessionConfigurationFile;
//    }
// --Commented out by Inspection STOP (11/26/16 11:44 PM)

    // ?
    public void SetImaper(Imaper currentImaper) {
        this.thisSessionImapFolder = currentImaper;
    }

    // ?
    public Imaper GetImaper() {
        return this.thisSessionImapFolder;
    }

    // ?
    public void SetNotesList(ArrayList<OneNote> currentNotesList) {
        this.noteList = currentNotesList;
    }

// --Commented out by Inspection START (11/26/16 11:44 PM):
//    // ?
//    public ArrayList<OneNote> GetNotesList() {
//        return this.noteList;
//    }
// --Commented out by Inspection STOP (11/26/16 11:44 PM)

}
