package com.Pau.ImapNotes2.Miscs;

import javax.mail.Folder;

public class ImapNotes2Result {

	public int returnCode;
	public String errorMessage;
	public Long UIDValidity;
	public boolean hasUIDPLUS;
	public Folder notesFolder;
	
	public ImapNotes2Result () {
		returnCode = -1;
		errorMessage = "";
		UIDValidity = (long) -1;
		hasUIDPLUS = true;
		notesFolder = null;
	}
}
