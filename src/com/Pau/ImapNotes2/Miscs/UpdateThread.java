package com.Pau.ImapNotes2.Miscs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MailDateFormat;

import com.Pau.ImapNotes2.ImapNotes2;
import com.Pau.ImapNotes2.Listactivity;
import com.Pau.ImapNotes2.NotesListAdapter;
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Data.NotesDb;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.SimpleAdapter;

public class UpdateThread extends AsyncTask<Object, Void, Boolean>{
    NotesListAdapter adapter;
    ArrayList<OneNote> notesList;
    String suid;
    String noteBody;
    String color;
    Imaper imapFolder;
    boolean bool_to_return;
    OneNote currentNote = null;
    NotesDb storedNotes;
    Context ctx;
    ProgressDialog pDialog;
    String body = null;
    String action;
    private static final String TAG = "UpdateThread";
        
    @Override
    protected Boolean doInBackground(Object... stuffs) {
        this.adapter = ((NotesListAdapter)stuffs[3]);
        this.notesList = ((ArrayList<OneNote>)stuffs[2]);
        this.suid = ((String)stuffs[5]);
        this.noteBody = ((String)stuffs[6]);
        this.color = ((String)stuffs[7]);
        this.imapFolder = ((Imaper)stuffs[0]);
        this.ctx = (Context)stuffs[8];
        this.action = (String)stuffs[9];
        this.storedNotes = (NotesDb)stuffs[10];

        try {
            // Do we have a note to remove?
            if (this.action.equals("delete") || this.action.equals("update")) {
                //Log.d(TAG,"Received request to delete message #"+suid);
                // Here we delete the note from the local notes list
                //Log.d(TAG,"Delete note in Listview");
                this.notesList.remove(getIndexByNumber(this.suid));
                MoveMailToDeleted(this.suid);
                this.storedNotes.OpenDb();
                this.storedNotes.DeleteANote(this.suid, Listactivity.imapNotes2Account.GetAccountname());
                this.storedNotes.CloseDb();
                this.bool_to_return = true;
            }

            // Do we have a note to add?
            if (this.action.equals("insert") || this.action.equals("update")) {
//Log.d(TAG,"Sticky ? "+((ImapNotes2Account)stuffs[1]).GetUsesticky());
//Log.d(TAG,"Color:"+this.color);
                //Log.d(TAG,"Received request to add new message"+this.noteBody+"===");
                String noteTxt = Html.fromHtml(this.noteBody).toString();
                String[] tok = noteTxt.split("\n", 2);
                String title = tok[0];
                String position = "0 0 0 0";
                if (((ImapNotes2Account)stuffs[1]).GetUsesticky().equals("true"))
                    body = noteTxt.replaceAll("\n", "\\\\n");
                else
                    body = "<html><head></head><body>" + this.noteBody + "</body></html>";


                String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                String stringDate = sdf.format(date);
                this.currentNote = new OneNote(title, stringDate, "");
                // Add note to database
                if (this.storedNotes == null) this.storedNotes = new NotesDb(this.ctx);
                this.storedNotes.OpenDb();
                this.suid = this.storedNotes.GetTempNumber(Listactivity.imapNotes2Account.GetAccountname());
                this.currentNote.SetUid(this.suid);
                // Here we ask to add the new note to the "new" folder
                // Must be done AFTER uid has been set in currenteNote
                WriteMailToNew(currentNote, 
                    ((ImapNotes2Account)stuffs[1]).GetUsesticky(), body);
                this.storedNotes.InsertANoteInDb(this.currentNote, Listactivity.imapNotes2Account.GetAccountname());
                this.storedNotes.CloseDb();
                // Add note to noteList but chage date format before
                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this.ctx);
                String sdate = DateFormat.getDateTimeInstance().format(date);
                this.currentNote.SetDate(sdate);
                this.notesList.add(0,this.currentNote);
                this.bool_to_return = true;
            }

        } catch (Exception e) {
        	e.printStackTrace();
            this.bool_to_return=false;
        } finally {
            ((ProgressDialog)stuffs[4]).dismiss();
        }
        return this.bool_to_return;
    }
        
    protected void onPostExecute(Boolean result){
        if (result) {
            if (this.bool_to_return) /* note added or removed */
                this.adapter.notifyDataSetChanged();
        }
    }

    public int getIndexByNumber(String pNumber) {
        for(OneNote _item : this.notesList)
        {
            if(_item.GetUid().equals(pNumber))
                return this.notesList.indexOf(_item);
        }
        return -1;
    }

    private void MoveMailToDeleted (String suid) {
        String directory = (ImapNotes2.getAppContext()).getFilesDir() + "/" +
                Listactivity.imapNotes2Account.GetAccountname();
        String positiveUid = suid.substring(1);
        File from = new File (directory, suid);
        File to = new File (directory + "/deleted/" + suid);
        if (!from.exists()) {
            from = new File (directory + "/new", positiveUid);
            from.delete();
        } else {
            from.renameTo(to);
        }
    }

  public void WriteMailToNew(OneNote note, String usesticky, String noteBody) throws MessagingException, IOException {
    String body = null;

    // Here we add the new note to the "new" folder
    //Log.d(TAG,"Add new note");
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    MimeMessage message = new MimeMessage(session);
    if (usesticky.equals("true")) {
      body = "BEGIN:STICKYNOTE\nCOLOR:" + this.color + "\nTEXT:" + noteBody +
             "\nPOSITION:0 0 0 0\nEND:STICKYNOTE";
      message.setText(body);
      message.setHeader("Content-Transfer-Encoding", "8bit");
      message.setHeader("Content-Type","text/x-stickynote; charset=\"utf-8\"");
    } else {
      message.setHeader("X-Uniform-Type-Identifier","com.apple.mail-note");
      UUID uuid = UUID.randomUUID();
      message.setHeader("X-Universally-Unique-Identifier", uuid.toString());
      body = noteBody;
      body = body.replaceFirst("<p dir=ltr>", "<div>");
      body = body.replaceFirst("<p dir=\"ltr\">", "<div>");
      body = body.replaceAll("<p dir=ltr>", "<div><br></div><div>");
      body = body.replaceAll("<p dir=\"ltr\">", "<div><br></div><div>");
      body = body.replaceAll("</p>", "</div>");
      body = body.replaceAll("<br>\n", "</div><div>");
      message.setText(body, "utf-8", "html");
      message.setFlag(Flags.Flag.SEEN,true);
    }
    message.setSubject(note.GetTitle());
    MailDateFormat mailDateFormat = new MailDateFormat();
    // Remove (CET) or (GMT+1) part as asked in github issue #13
    String headerDate = (mailDateFormat.format(new Date())).replaceAll("\\(.*$", "");
    message.addHeader("Date", headerDate);
    //déterminer l'uid temporaire
    String uid = Integer.toString(Math.abs(Integer.parseInt(note.GetUid())));
    File directory = new File ((ImapNotes2.getAppContext()).getFilesDir() + "/" +
            Listactivity.imapNotes2Account.GetAccountname() + "/new");
    //message.setFrom(new InternetAddress("ImapNotes2", Listactivity.imapNotes2Account.GetAccountname()));
    message.setFrom(Listactivity.imapNotes2Account.GetAccountname());
    File outfile = new File (directory, uid);
    OutputStream str = new FileOutputStream(outfile);
    message.writeTo(str);

  }

}
