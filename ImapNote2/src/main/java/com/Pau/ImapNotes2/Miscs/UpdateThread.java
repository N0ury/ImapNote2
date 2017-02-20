package com.Pau.ImapNotes2.Miscs;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;

import com.Pau.ImapNotes2.Data.Db;
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Data.OneNote;
import com.Pau.ImapNotes2.Listactivity;
import com.Pau.ImapNotes2.NotesListAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.Pau.ImapNotes2.NoteDetailActivity.Colors;

//import com.Pau.ImapNotes2.Data.NotesDb;

// TODO: move arguments from execute to constructor.
public class UpdateThread extends AsyncTask<Object, Void, Boolean> {
    private final ImapNotes2Account imapNotes2Account;
    private final ProgressDialog progressDialog;
    private final NotesListAdapter adapter;
    private final ArrayList<OneNote> notesList;
    private String suid;
    private final String noteBody;
    private final Colors color;
    private boolean bool_to_return;
    private Db storedNotes;
    private final Context applicationContext;
    private final Action action;
    private static final String TAG = "IN_UpdateThread";

    public enum Action {
        Update,
        Insert,
        Delete
    }

    /*
    Assign all fields in the constructor because we never reuse this object.  This makes the code
    typesafe.  Make them final to prevent accidental reuse.
    */
    public UpdateThread(ImapNotes2Account imapNotes2Account,
                        ArrayList<OneNote> noteList,
                        NotesListAdapter listToView,
                        ProgressDialog loadingDialog,
                        String suid,
                        String noteBody,
                        Colors color,
                        Context applicationContext,
                        Action action,
                        Db storedNotes) {
        Log.d(TAG, "UpdateThread: " + noteBody);
        this.imapNotes2Account = imapNotes2Account;
        this.notesList = noteList;
        this.adapter = listToView;
        this.progressDialog = loadingDialog;
        this.suid = suid;
        this.noteBody = noteBody;
        this.color = color;
        this.applicationContext = applicationContext;
        this.action = action;
        this.storedNotes = storedNotes;

    }

    @Override
    protected Boolean doInBackground(Object... stuffs) {

        try {
            // Do we have a note to remove?
            if ((action == Action.Delete) || (action == Action.Update)) {
                //Log.d(TAG,"Received request to delete message #"+suid);
                // Here we delete the note from the local notes list
                //Log.d(TAG,"Delete note in Listview");
                notesList.remove(getIndexByNumber(suid));
                MoveMailToDeleted(suid);
                storedNotes.OpenDb();
                storedNotes.notes.DeleteANote(suid, Listactivity.imapNotes2Account.accountName);
                storedNotes.CloseDb();
                bool_to_return = true;
            }

            // Do we have a note to add?
            if ((action == Action.Insert) || (action == Action.Update)) {
//Log.d(TAG,"Sticky ? "+((ImapNotes2Account)stuffs[1]).GetUsesticky());
//Log.d(TAG,"Color:"+color);
                Log.d(TAG, "Received request to add new message: " + noteBody + "===");
                //String noteTxt = Html.fromHtml(noteBody).toString();
                String noteTxt = noteBody;
                Log.d(TAG, "noteTxt: " + noteTxt + "===");
                // Use the first line as the tile
                String[] tok = Html.fromHtml(noteBody).toString().split("\n", 2);
                String title = tok[0];
                //String position = "0 0 0 0";
                String body = (imapNotes2Account.usesticky) ?
                        noteTxt.replaceAll("\n", "\\\\n") :
                        "<html><head></head><body>" + noteBody + "</body></html>";

                String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ROOT);
                String stringDate = sdf.format(date);
                OneNote currentNote = new OneNote(title, stringDate, "");
                // Add note to database
                if (storedNotes == null) storedNotes = new Db(applicationContext);
                storedNotes.OpenDb();
                suid = storedNotes.notes.GetTempNumber(Listactivity.imapNotes2Account.accountName);
                currentNote.SetUid(suid);
                // Here we ask to add the new note to the new note folder
                // Must be done AFTER uid has been set in currenteNote
                Log.d(TAG, "doInBackground body: " + body);
                WriteMailToNew(currentNote,
                        imapNotes2Account.usesticky,
                        imapNotes2Account.usesAutomaticMerge,
                        body);
                storedNotes.notes.InsertANoteInDb(currentNote, Listactivity.imapNotes2Account.accountName);
                storedNotes.CloseDb();
                // Add note to noteList but chage date format before
                //DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(applicationContext);
                String sdate = DateFormat.getDateTimeInstance().format(date);
                currentNote.SetDate(sdate);
                notesList.add(0, currentNote);
                return true;
            }

        } catch (Exception e) {
            Log.d(TAG, "Action: " + action.toString());
            e.printStackTrace();
            return false;
        } finally {
            progressDialog.dismiss();
        }
        return bool_to_return;
    }

    protected void onPostExecute(Boolean result) {
        if (result) {
            if (bool_to_return) /* note added or removed */
                adapter.notifyDataSetChanged();
        }
    }

    private int getIndexByNumber(String pNumber) {
        for (OneNote _item : notesList) {
            if (_item.GetUid().equals(pNumber))
                return notesList.indexOf(_item);
        }
        return -1;
    }

    /**
     * @param suid IMAP ID of the note.
     */
    private void MoveMailToDeleted(@NonNull String suid) {
        String directory = applicationContext.getFilesDir() + "/" +
                Listactivity.imapNotes2Account.accountName;
        // TODO: Explain why we need to omit the first character of the UID
        File from = new File(directory, suid);
        if (!from.exists()) {
            String positiveUid = suid.substring(1);
            from = new File(directory + "/new", positiveUid);
            // TODO: Explain why it is safe to ignore the result of delete.
            //noinspection ResultOfMethodCallIgnored
            from.delete();
        } else {
            File to = new File(directory + "/deleted/" + suid);
            // TODO: Explain why it is safe to ignore the result of rename.
            //noinspection ResultOfMethodCallIgnored
            from.renameTo(to);
        }
    }

    @NonNull
    private Message MakeMessageWithAttachment(String subject,
                                              String message,
                                              String filePath,
                                              Session session)
            throws IOException, MessagingException {

        Message msg = new MimeMessage(session);


        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // add attachment

        MimeBodyPart attachPart = new MimeBodyPart();

        attachPart.attachFile(filePath);


        multipart.addBodyPart(attachPart);

        // sets the multi-part as e-mail's content
        msg.setContent(multipart);
        return msg;
    }

    private void WriteMailToNew(@NonNull OneNote note,
                                boolean usesticky,
                                boolean usesAutomaticMerge,
                                String noteBody) throws MessagingException, IOException {
        Log.d(TAG, "WriteMailToNew: " + noteBody);
        //String body = null;

        // Here we add the new note to the new note folder
        //Log.d(TAG,"Add new note");
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage message = new MimeMessage(session);

        if (usesticky) {
            String body = "BEGIN:STICKYNOTE\nCOLOR:" + color.name() + "\nTEXT:" + noteBody +
                    "\nPOSITION:0 0 0 0\nEND:STICKYNOTE";
            message.setText(body);
            message.setHeader("Content-Transfer-Encoding", "8bit");
            message.setHeader("Content-Type", "text/x-stickynote; charset=\"utf-8\"");
        } else {
            message.setHeader("X-Uniform-Type-Identifier", "com.apple.mail-note");
            UUID uuid = UUID.randomUUID();
            message.setHeader("X-Universally-Unique-Identifier", uuid.toString());
            String body = noteBody;
            body = body.replaceFirst("<p dir=ltr>", "<div>");
            body = body.replaceFirst("<p dir=\"ltr\">", "<div>");
            body = body.replaceAll("<p dir=ltr>", "<div><br></div><div>");
            body = body.replaceAll("<p dir=\"ltr\">", "<div><br></div><div>");
            body = body.replaceAll("</p>", "</div>");
            body = body.replaceAll("<br>\n", "</div><div>");
            message.setText(body, "utf-8", "html");
            message.setFlag(Flags.Flag.SEEN, true);
        }
        message.setSubject(note.GetTitle());
        MailDateFormat mailDateFormat = new MailDateFormat();
        // Remove (CET) or (GMT+1) part as asked in github issue #13
        String headerDate = (mailDateFormat.format(new Date())).replaceAll("\\(.*$", "");
        message.addHeader("Date", headerDate);
        // Get temporary UID
        String uid = Integer.toString(Math.abs(Integer.parseInt(note.GetUid())));
        File accountDirectory = new File(applicationContext.getFilesDir(),
                Listactivity.imapNotes2Account.accountName);
        File directory = new File(accountDirectory, "new");
        //message.setFrom(new InternetAddress("ImapNotes2", Listactivity.imapNotes2Account.accountName));
        message.setFrom(Listactivity.imapNotes2Account.accountName);
        File outfile = new File(directory, uid);
        OutputStream str = new FileOutputStream(outfile);
        message.writeTo(str);

    }

}
