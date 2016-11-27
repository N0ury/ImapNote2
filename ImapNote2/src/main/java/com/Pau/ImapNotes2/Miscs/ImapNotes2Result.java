package com.Pau.ImapNotes2.Miscs;

import android.support.annotation.Nullable;

import javax.mail.Folder;

public class ImapNotes2Result {

    public int returnCode;
    public String errorMessage;
    public Long UIDValidity;
    // --Commented out by Inspection (11/26/16 11:45 PM):public boolean hasUIDPLUS;
    @Nullable
    public Folder notesFolder;

    public ImapNotes2Result() {
        returnCode = -1;
        errorMessage = "";
        UIDValidity = (long) -1;
        //hasUIDPLUS = true;
        notesFolder = null;
    }
}
