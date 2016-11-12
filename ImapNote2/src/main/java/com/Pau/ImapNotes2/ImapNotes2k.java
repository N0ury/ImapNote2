package com.Pau.ImapNotes2;

import java.io.File;
import java.util.ArrayList;

import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.OneNote;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;


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

    // ?
    public void SetConfigurationFile(ConfigurationFile currentSettings) {
        this.thisSessionConfigurationFile = currentSettings;
    }

    // ?
    public ConfigurationFile GetConfigurationFile() {
        return this.thisSessionConfigurationFile;
    }

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

    // ?
    public ArrayList<OneNote> GetNotesList() {
        return this.noteList;
    }

}
