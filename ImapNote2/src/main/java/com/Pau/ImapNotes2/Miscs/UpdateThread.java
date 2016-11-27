package com.Pau.ImapNotes2.Miscs;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;

import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Data.OneNote;
import com.Pau.ImapNotes2.ImapNotes2k;
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
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;

import static com.Pau.ImapNotes2.NoteDetailActivity.Colors;

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
    private NotesDb storedNotes;
    private final Context ctx;
    private final Action action;
    private static final String TAG = "IN_UpdateThread";

    public enum Action {
        Update,
        Insert,
        Delete
    }

    /*
    Assign all fields in the constructor because we never reuse this object.  This makes the code
    typesafe.  Make them final to preven accidental reuse.
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
                        NotesDb storedNotes) {

        this.imapNotes2Account = imapNotes2Account;
        this.notesList = noteList;
        this.adapter = listToView;
        this.progressDialog = loadingDialog;
        this.suid = suid;
        this.noteBody = noteBody;
        this.color = color;
        this.ctx = applicationContext;
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
                storedNotes.DeleteANote(suid, Listactivity.imapNotes2Account.GetAccountname());
                storedNotes.CloseDb();
                bool_to_return = true;
            }

            // Do we have a note to add?
            if ((action == Action.Insert) || (action == Action.Update)) {
//Log.d(TAG,"Sticky ? "+((ImapNotes2Account)stuffs[1]).GetUsesticky());
//Log.d(TAG,"Color:"+color);
                //Log.d(TAG,"Received request to add new message"+noteBody+"===");
                String noteTxt = Html.fromHtml(noteBody).toString();
                String[] tok = noteTxt.split("\n", 2);
                String title = tok[0];
                //String position = "0 0 0 0";
                String body = (imapNotes2Account.GetUsesticky()) ?
                        noteTxt.replaceAll("\n", "\\\\n") :
                        "<html><head></head><body>" + noteBody + "</body></html>";

                String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ROOT);
                String stringDate = sdf.format(date);
                OneNote currentNote = new OneNote(title, stringDate, "");
                // Add note to database
                if (storedNotes == null) storedNotes = new NotesDb(ctx);
                storedNotes.OpenDb();
                suid = storedNotes.GetTempNumber(Listactivity.imapNotes2Account.GetAccountname());
                currentNote.SetUid(suid);
                // Here we ask to add the new note to the new note folder
                // Must be done AFTER uid has been set in currenteNote
                WriteMailToNew(currentNote,
                        imapNotes2Account.GetUsesticky(), body);
                storedNotes.InsertANoteInDb(currentNote, Listactivity.imapNotes2Account.GetAccountname());
                storedNotes.CloseDb();
                // Add note to noteList but chage date format before
                //DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(ctx);
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
        String directory = ImapNotes2k.getAppContext().getFilesDir() + "/" +
                Listactivity.imapNotes2Account.GetAccountname();
        // TODO: Explain why we need to omit the first character of the UID
        File from = new File(directory, suid);
        if (!from.exists()) {
            String positiveUid = suid.substring(1);
            from = new File(directory + "/new", positiveUid);
            // TODO: Explain why it is safe to ignore the result of delete.
            final boolean delete = from.delete();
        } else {
            File to = new File(directory + "/deleted/" + suid);
            // TODO: Explain why it is safe to ignore the result of rename.
            //noinspection ResultOfMethodCallIgnored
            from.renameTo(to);
        }
    }

    private void WriteMailToNew(@NonNull OneNote note,
                                boolean usesticky,
                                String noteBody) throws MessagingException, IOException {
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
        File directory = new File((ImapNotes2k.getAppContext()).getFilesDir() + "/" +
                Listactivity.imapNotes2Account.GetAccountname() + "/new");
        //message.setFrom(new InternetAddress("ImapNotes2", Listactivity.imapNotes2Account.GetAccountname()));
        message.setFrom(Listactivity.imapNotes2Account.GetAccountname());
        File outfile = new File(directory, uid);
        OutputStream str = new FileOutputStream(outfile);
        message.writeTo(str);

    }

}
