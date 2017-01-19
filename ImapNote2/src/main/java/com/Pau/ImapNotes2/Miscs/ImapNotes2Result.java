package com.Pau.ImapNotes2.Miscs;

import android.support.annotation.Nullable;

import javax.mail.Folder;

public class ImapNotes2Result {

    public final int returnCode;
    public final String errorMessage;
    public final Long UIDValidity;
    // --Commented out by Inspection (11/26/16 11:45 PM):public boolean hasUIDPLUS;
    @Nullable
    public final Folder notesFolder;

    public ImapNotes2Result(int returnCode,
                            String errorMessage,
                            long UIDValidity,
                            Folder notesFolder) {
        this.returnCode = returnCode;
        this.errorMessage = errorMessage ;
        this.UIDValidity = UIDValidity ;
        this.notesFolder = notesFolder;
    }

/*
    public ImapNotes2Result() {
        returnCode = -1;
        errorMessage = "";
        UIDValidity = (long) -1;
        //hasUIDPLUS = true;
        notesFolder = null;
    }
*/

}
