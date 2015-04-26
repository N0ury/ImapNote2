package com.Pau.ImapNotes2.Miscs;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Date;

import android.text.Html;
import android.text.Spanned;
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
  private String acceptcrt;
  
  public int ConnectToProvider(String username, String password, String server, String portnum, String security, String usesticky) throws MessagingException{
    if (this.IsConnected())
      this.store.close();
    
  this.proto = "";
  this.acceptcrt = "";
  int security_i = Integer.parseInt(security);
  switch (security_i) {
    case 0:
      // None
      this.proto = "imap";
      this.acceptcrt = "";
      break;
    case 1:
      // SSL/TLS
      this.proto = "imaps";
      this.acceptcrt = "false";
      break;
    case 2:
      // SSL/TLS/TRUST ALL
      this.proto = "imaps";
      this.acceptcrt = "true";
      break;
    case 3:
      // STARTTLS
      this.proto = "imap";
      this.acceptcrt = "false";
      break;
    case 4:
      // STARTTLS/TRUST ALL
      this.proto = "imap";
      this.acceptcrt = "true";
      break;
////////////////////// Change this
    default: this.proto = "Invalid month";
       break;
  }
    MailSSLSocketFactory sf = null;
    try {
      sf = new MailSSLSocketFactory();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }

    Properties props = new Properties();

    props.setProperty(String.format("mail.%s.host", this.proto), server);
    props.setProperty(String.format("mail.%s.port", this.proto), portnum);
    props.setProperty("mail.store.protocol", this.proto);

    if ((this.acceptcrt.equals("true"))) {
      sf.setTrustedHosts(new String[] {server});
      if (this.proto.equals("imap")) {
        props.put("mail.imap.ssl.socketFactory", sf);
        props.put("mail.imap.starttls.enable", "true");
      }
    } else if (this.acceptcrt.equals("false")) {
      props.put(String.format("mail.%s.ssl.checkserveridentity", this.proto), "true");
      if (this.proto.equals("imap")) {
        props.put("mail.imap.starttls.enable", "true");
      }
    }

    if (this.proto.equals("imaps")) {
      props.put("mail.imaps.socketFactory", sf);
    }

    props.setProperty("mail.imap.connectiontimeout","1000");
    this.session = Session.getInstance(props, null);
//this.session.setDebug(true);
    this.store = this.session.getStore(this.proto);
    try {
      this.store.connect(server, username, password);
      return 0;
    } catch (Exception e) {
      e.printStackTrace();
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
      } else if (contentType.getSubType().equalsIgnoreCase("html")) {
          //Log.d(TAG,"From server:"+stringres);
          Spanned spanres = Html.fromHtml(stringres);
          stringres = Html.toHtml(spanres);
          stringres = stringres.replaceFirst("<p dir=ltr>", "");
          stringres = stringres.replaceFirst("<p dir=\"ltr\">", "");
          stringres = stringres.replaceAll("<p dir=ltr>", "<br>");
          stringres = stringres.replaceAll("<p dir=\"ltr\">", "<br>");
          stringres = stringres.replaceAll("</p>", "");
      }
      OneNote aNote = new OneNote(
      notesMessages[index].getSubject(),
      stringres,
      notesMessages[index].getReceivedDate().toLocaleString(),
      new Integer (notesMessages[index].getMessageNumber()).toString());
      notesList.add(aNote);
      //Log.d(TAG,"Got title:"+(String)notesMessages[index].getSubject());
      //Log.d(TAG,"Got content:"+stringres);
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
      body = body.replaceFirst("<p dir=ltr>", "<div>");
      body = body.replaceFirst("<p dir=\"ltr\">", "<div>");
      body = body.replaceAll("<p dir=ltr>", "<div><br></div><div>");
      body = body.replaceAll("<p dir=\"ltr\">", "<div><br></div><div>");
      body = body.replaceAll("</p>", "</div>");
      body = body.replaceAll("<br>\n", "</div><div>");
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
    Log.d(TAG,"Title sent to server:"+message.getSubject());
    Log.d(TAG,"Content sent to server:"+body);
  }
}
