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

import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.util.MailSSLSocketFactory;
import java.util.regex.*;
import org.apache.commons.io.IOUtils;

import com.Pau.ImapNotes2.Miscs.Sticky;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;

public class Imaper {
  
  private Store store;
  private Session session;
  private static final String TAG = "IN_Imaper";
  private String proto;
  private String acceptcrt;
  private static String sfolder = "Notes";
  private Folder notesFolder = null;
  private ImapNotes2Result res;
private Boolean useProxy = false;
  
  public ImapNotes2Result ConnectToProvider(String username, String password, String server, String portnum, String security, String usesticky) throws MessagingException{
    if (this.IsConnected())
      this.store.close();
    
  res = new ImapNotes2Result();
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
    default: this.proto = "Invalid proto";
       break;
  }
    MailSSLSocketFactory sf = null;
    try {
      sf = new MailSSLSocketFactory();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      this.res.errorMessage = "Can't connect to server";
      this.res.returnCode = -1;
      return this.res;
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
    if (this.useProxy) {
        props.put("mail.imap.socks.host","10.0.2.2");
        props.put("mail.imap.socks.port","1080");
/*
        props.put("proxySet","true");
        props.put("socksProxyHost","10.0.2.2");
        props.put("socksProxyPort","1080");
        props.put("sun.net.spi.nameservice.provider.1", "dns,sun");
        props.put("sun.net.spi.nameservice.nameservers", "192.168.0.99");
*/
    }
    this.session = Session.getInstance(props, null);
//this.session.setDebug(true);
    this.store = this.session.getStore(this.proto);
    try {
      this.store.connect(server, username, password);
      Folder[] folders = store.getPersonalNamespaces();
      Folder folder = folders[0];
Log.d(TAG,"FULLNAME="+folder.getFullName());
      if (folder.getFullName().length() == 0) {
          Imaper.sfolder = "Notes";
      } else {
	      char separator = folder.getSeparator();
	      Imaper.sfolder = folder.getFullName() + separator + "Notes";            	
      }
      this.res.errorMessage = "";
      this.res.returnCode = 0;
      return this.res;
    } catch (Exception e) {
      e.printStackTrace();
      Log.d(TAG, e.getMessage());
      this.res.errorMessage = e.getMessage();
      this.res.returnCode = -2;
      return this.res;
    }

  }
  
  public void GetNotes(ArrayList<OneNote> notesList) throws MessagingException, IOException{
    String stringres = new String();
    String position = new String("0 0 0 0");
    String color = new String("NONE");
    String charset = new String();
    this.notesFolder = this.store.getFolder(Imaper.sfolder);
    if (this.notesFolder.isOpen()) {
      if ((this.notesFolder.getMode() & Folder.READ_ONLY) != 0)
        this.notesFolder.open(Folder.READ_ONLY);
    } else {
      this.notesFolder.open(Folder.READ_ONLY);
    }
    Message[] notesMessages = this.notesFolder.getMessages();
    //Log.d(TAG,"number of messages in folder="+(notesMessages.length));
    notesList.clear();
    for(int index=notesMessages.length-1; index>=0; index--){
      InputStream iis = (InputStream)notesMessages[index].getContent();
      ContentType contentType = new ContentType(notesMessages[index].getContentType() );
      charset = contentType.getParameter("charset");
      stringres = IOUtils.toString(iis, charset);
      // if it's a stickynote than Content-Type is "text/x-stickynote"
//Log.d(TAG,"contentType.getSubType():"+contentType.getSubType());
      if (((String)notesMessages[index].getContentType()).startsWith("text/x-stickynote")) {
        Sticky sticky = new Sticky();
        sticky = ReadStickynote(stringres);
        stringres = sticky.GetText();
        position = sticky.GetPosition();
        color = sticky.GetColor();
      } else if (contentType.getSubType().equalsIgnoreCase("html")) {
          //Log.d(TAG,"From server (html):"+stringres);
          Spanned spanres = Html.fromHtml(stringres);
          stringres = Html.toHtml(spanres);
          stringres = stringres.replaceFirst("<p dir=ltr>", "");
          stringres = stringres.replaceFirst("<p dir=\"ltr\">", "");
          stringres = stringres.replaceAll("<p dir=ltr>", "<br>");
          stringres = stringres.replaceAll("<p dir=\"ltr\">", "<br>");
          stringres = stringres.replaceAll("</p>", "");
      } else if (contentType.getSubType().equalsIgnoreCase("plain")) {
//    	  Log.d(TAG,"From server (plain):"+stringres);
          stringres = stringres.replaceAll("\n", "<br>");
      }
      //Log.d(TAG,"UID read:"+((IMAPFolder)this.notesFolder).getUID(notesMessages[index]));
      OneNote aNote = new OneNote(
      notesMessages[index].getSubject(),
      stringres,
      notesMessages[index].getReceivedDate().toLocaleString(),
      Long.toString(((IMAPFolder)this.notesFolder).getUID(notesMessages[index])),
      position,
      color);
      notesList.add(aNote);
      //Log.d(TAG,"Got title:"+(String)notesMessages[index].getSubject());
      //Log.d(TAG,"Got content:"+stringres);
    }
    //Log.d(TAG,"number of messages read="+notesList.size());
  }

  private Sticky ReadStickynote(String stringres) {
    String color=new String("");
    String position=new String("");
    String text=new String("");
    Pattern p = null;
    Matcher m = null;

    p = Pattern.compile("^COLOR:(.*?)$",Pattern.MULTILINE);
    m = p.matcher(stringres);
    if (m.find()) { color = m.group(1); }

    p = Pattern.compile("^POSITION:(.*?)$",Pattern.MULTILINE);
    m = p.matcher(stringres);
    if (m.find()) { position = m.group(1); }

    p = Pattern.compile("TEXT:(.*?)(END:|POSITION:)",Pattern.DOTALL);
    m = p.matcher(stringres);
    if (m.find()) {
      text = m.group(1);
      // Kerio Connect puts CR+LF+space every 78 characters from line 2
      // first line seem to be smaller. We remove these characters
      text = text.replaceAll("\r\n ", "");
      // newline in Kerio is the string (not the character) "\n"
      text = text.replaceAll("\\\\n", "<br>");
    }
    return new Sticky(text, position, color);
  }
/***************************************************
***************************************************/

  public boolean IsConnected(){
    return this.store!=null && this.store.isConnected();
  }

  public void DeleteNote(int numMessage) throws MessagingException, IOException {
    this.notesFolder = this.store.getFolder(Imaper.sfolder);
    if (this.notesFolder.isOpen()) {
      if ((this.notesFolder.getMode() & Folder.READ_WRITE) != 0)
        this.notesFolder.open(Folder.READ_WRITE);
    } else {
      this.notesFolder.open(Folder.READ_WRITE);
    }
    //Log.d(TAG,"Mark as deleted message #"+numMessage);
 //   final int[] msgs = {numMessage};
 //   this.notesFolder.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
    //Log.d(TAG,"UID to remove:"+numMessage);
    Message[] msgs = {((IMAPFolder)this.notesFolder).getMessageByUID(numMessage)};
    ((IMAPFolder)this.notesFolder).setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
    this.notesFolder.expunge();
  }

  public void AddNote(OneNote note, String usesticky) throws MessagingException, IOException {
    String body = null;

    // Here we add the new note to the "Notes" folder
    this.notesFolder = this.store.getFolder(Imaper.sfolder);
    if (this.notesFolder.isOpen()) {
      if ((this.notesFolder.getMode() & Folder.READ_WRITE) != 0)
        this.notesFolder.open(Folder.READ_WRITE);
    } else {
      this.notesFolder.open(Folder.READ_WRITE);
    }
    //Log.d(TAG,"Add new note");
    MimeMessage message = new MimeMessage(this.session);
    if (usesticky.equals("true")) {
      body = "BEGIN:STICKYNOTE\nCOLOR:" + note.GetColor() + "\nTEXT:" + note.GetBody() + "\nPOSITION:" + note.GetPosition() + "\nEND:STICKYNOTE";
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
    //this.notesFolder.appendMessages(msgs);
//    String uid = Long.toString(((IMAPFolder)this.notesFolder).getUIDNext());
//msgs2 = ((IMAPFolder)this.notesFolder).addMessages(msgs);
    AppendUID[] uids = ((IMAPFolder)this.notesFolder).appendUIDMessages(msgs);
    //String uid = Long.toString(((IMAPFolder)this.notesFolder).getUID(msgs2[0]));
    String uid = Long.toString(uids[0].uid);
    //Log.d(TAG,"UID to add:"+uid);
    // message was inserted in folder but note still doesn't have number
    // put in it the one of message just inserted
    //note.SetNumber(String.valueOf(msgs2[0].getMessageNumber()));
	note.SetNumber(uid);
    //Log.d(TAG,"NUM:"+uid);
    //Log.d(TAG,"Title sent to server:"+message.getSubject());
    //Log.d(TAG,"Content sent to server:"+body);
  }
}
