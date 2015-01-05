package com.Pau.ImapNotes2.Miscs;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Date;
import android.util.Log;

import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags;
import java.util.UUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.util.MailSSLSocketFactory;
import java.util.regex.*;

import org.apache.commons.io.IOUtils;

public class Imaper {
	
	private Store store;
	private Session session;
	private static final String TAG = "IN_Imaper";
	private String proto;
	
	public int ConnectToProvider(String username, String password, String server, String acceptcrt, String usesticky) throws MessagingException{
		if (this.IsConnected())
			this.store.close();
		
		Properties props = System.getProperties();
		if (props.getProperty("mail.imaps.socketFactory.class") != null) props.remove("mail.imaps.socketFactory.class");
		MailSSLSocketFactory sf = null;
		try {
			sf = new MailSSLSocketFactory();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		if (!(acceptcrt.equals("true"))) {
			props.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		} else {

			sf.setTrustedHosts(new String[] { server});
			//sf.setTrustAllHosts(true);
		}
		props.put("mail.imaps.socketFactory", sf);
		this.proto = "imaps";
		props.setProperty("mail.store.protocol", this.proto);

		this.session = Session.getDefaultInstance(props, null);
//this.session.setDebug(true);
		this.store = this.session.getStore(this.proto);
		try {
			this.store.connect(server, username, password);
			return 0;
		} catch (Exception e) {
			Log.v(TAG, e.getMessage());
			return -1;
		}

	}
	
	public void GetNotes(ArrayList<OneNote> notesList) throws MessagingException, IOException{
		String stringres = new String();
		String charset = new String();
		Folder notesFolder = this.store.getFolder("Notes");
		if (notesFolder.isOpen()) {
			if ((notesFolder.getMode() & Folder.READ_ONLY) != 0)
				notesFolder.open(Folder.READ_ONLY);
		} else {
			notesFolder.open(Folder.READ_ONLY);
		}
		Message[] notesMessages = notesFolder.getMessages();
		//Log.d(TAG,"number of messages in folder="+(notesMessages.length));
		notesList.clear();
		for(int index=notesMessages.length-1; index>=0; index--){
			InputStream iis = (InputStream)notesMessages[index].getContent();
			ContentType contentType = new ContentType(notesMessages[index].getContentType() );
			charset = contentType.getParameter("charset");
			stringres = IOUtils.toString(iis, charset);
			// if it's a stickynote than Content-Type is "text/x-stickynote"
			if (((String)notesMessages[index].getContentType()).startsWith("text/x-stickynote")) {
			    Pattern p = Pattern.compile("TEXT:(.*?)(END:|POSITION)",Pattern.DOTALL);
			    Matcher m = p.matcher(stringres);
			    if (m.find()) {
			    	stringres = m.group(1);
			    	// Kerio Connect puts CR+LF+space every 78 characters from line 2
			    	// first line seem to be smaller. We remove these characters
			    	stringres = stringres.replaceAll("\r\n ", "");
			    	// newline in Kerio is the string (not the character) "\n"
			    	stringres = stringres.replaceAll("\\\\n", "<br>");
			    }
			}
			OneNote aNote = new OneNote(
			notesMessages[index].getSubject(),
			stringres,
			notesMessages[index].getReceivedDate().toLocaleString(),
			new Integer (notesMessages[index].getMessageNumber()).toString());
			notesList.add(aNote);
			//Log.d(TAG,"title:"+(String)notesMessages[index].getSubject());
			//Log.d(TAG,"content:"+stringres);
		}
		//Log.d(TAG,"number of messages read="+notesList.size());
		
	}
	
	public boolean IsConnected(){
		return this.store!=null && this.store.isConnected();
	}

	public void DeleteNote(int numMessage) throws MessagingException, IOException {
		Folder notesFolder = this.store.getFolder("Notes");
		if (notesFolder.isOpen()) {
			if ((notesFolder.getMode() & Folder.READ_WRITE) != 0)
				notesFolder.open(Folder.READ_WRITE);
		} else {
			notesFolder.open(Folder.READ_WRITE);
		}
		//Log.d(TAG,"Mark as deleted message #"+numMessage);
		final int[] msgs = {numMessage};
		notesFolder.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
		notesFolder.expunge();
	}

	public void AddNote(OneNote note, String usesticky) throws MessagingException, IOException {
		String body = null;

		// Here we add the new note to the "Notes" folder
		Folder notesFolder = this.store.getFolder("Notes");
		if (notesFolder.isOpen()) {
			if ((notesFolder.getMode() & Folder.READ_WRITE) != 0)
				notesFolder.open(Folder.READ_WRITE);
		} else {
			notesFolder.open(Folder.READ_WRITE);
		}
		//Log.d(TAG,"Add new note");
		MimeMessage message = new MimeMessage(this.session);
		if (usesticky.equals("true")) {
			body = "BEGIN:STICKYNOTE\nCOLOR:YELLOW\nTEXT:" + note.GetBody() + "\nPOSITION:0 0 0 0\nEND:STICKYNOTE";
			note.SetBody(note.GetBody().replaceAll("\\\\n", "<br>"));
			message.setText(body);
			message.setHeader("Content-Transfer-Encoding", "8bit");
			message.setHeader("Content-Type","text/x-stickynote; charset=\"utf-8\"");
		} else {
			message.setHeader("X-Uniform-Type-Identifier","com.apple.mail-note");
			UUID uuid = UUID.randomUUID();
			message.setHeader("X-Universally-Unique-Identifier", uuid.toString());
			body = note.GetBody();
			message.setText(body, "utf-8", "html");
		}
		message.setSubject(note.GetTitle());
		message.setSentDate(new Date());
		final MimeMessage[] msgs = {message};
		final Message[] msgs2;
		//notesFolder.appendMessages(msgs);
		msgs2 = ((IMAPFolder)notesFolder).addMessages(msgs);
		// message was inserted in folder but note still doesn't have number
		// put in it the one of message just inserted
		note.SetNumber(String.valueOf(msgs2[0].getMessageNumber()));
		//Log.d(TAG,"NUM:"+msgs[0].getMessageNumber()+"==="+notesFolder.getMessageCount()+"==="+msgs2.length+"===="+msgs2[0].getMessageNumber());
	}
}
