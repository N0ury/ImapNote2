package com.Pau.ImapNotes2.Miscs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class Imaper {
	
	private Store store;
	
	public void ConnectToProvider(String username, String password, String server) throws MessagingException{
		if (this.IsConnected())
			this.store.close();
		
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		Session session = Session.getDefaultInstance(props, null);
		this.store = session.getStore("imaps");
		this.store.connect(server, username, password);

	}
	
	public void GetNotes(ArrayList<OneNote> notesList) throws MessagingException, IOException{
		Folder notesFolder = this.store.getFolder("Notes");
		notesFolder.open(Folder.READ_ONLY);
		Message[] notesMessages = notesFolder.getMessages();
		
		notesList.clear();
		for(int index=notesMessages.length-1; index>=0; index--){
			OneNote aNote = new OneNote(notesMessages[index].getSubject(), ((String)notesMessages[index].getContent()), notesMessages[index].getReceivedDate().toLocaleString());
			notesList.add(aNote);
		}
		
	}
	
	public boolean IsConnected(){
		return this.store!=null && this.store.isConnected();
		
	}

}
