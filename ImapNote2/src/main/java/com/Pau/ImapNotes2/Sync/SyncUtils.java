package com.Pau.ImapNotes2.Sync;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Data.OneNote;
import com.Pau.ImapNotes2.Data.Security;
import com.Pau.ImapNotes2.Data.Utilities;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.Sticky;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.util.MailSSLSocketFactory;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;

import static com.Pau.ImapNotes2.NoteDetailActivity.Colors;

public class SyncUtils {

    private static Store store;
    private static final String TAG = "IN_SyncUtils";
    // TODO: Why do we have two folder fields and why are they both nullable?
    @NonNull
    private static String sfolder = "Notes";
    @Nullable
    private static Folder notesFolder = null;
    private static final ImapNotes2Result res = new ImapNotes2Result();
    private static Long UIDValidity;
    //private final static int NEW = 1;
    //private final static int DELETED = 2;
    //private final static int ROOT_AND_NEW = 3;

    public enum Where {
        // --Commented out by Inspection (11/26/16 11:46 PM):NEW, DELETED, ROOT_AND_NEW
    }

    @NonNull
    static ImapNotes2Result ConnectToRemote(@NonNull String username,
                                            @NonNull String password,
                                            @NonNull String server,
                                            String portnum,
                                            @NonNull Security security,
                                            @NonNull String folderOverride) {
        if (IsConnected()) {
            try {
                store.close();
            } catch (MessagingException e) {
                // Log the error but do not propagate the exception because the connection is now
                // closed even if an exception was thrown.
                Log.d(TAG, e.getMessage());
            }
        }

        String proto = security.proto;
        boolean acceptcrt = security.acceptcrt;

        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            res.errorMessage = "Can't connect to server: " + e.getMessage();
            res.returnCode = Imaper.ResultCodeCantConnect;
            return res;
        }

        Properties props = new Properties();

        props.setProperty(String.format("mail.%s.host", proto), server);
        props.setProperty(String.format("mail.%s.port", proto), portnum);
        props.setProperty("mail.store.protocol", proto);

        if ((acceptcrt)) {
            sf.setTrustedHosts(new String[]{server});
            if (proto.equals("imap")) {
                props.put("mail.imap.ssl.socketFactory", sf);
                props.put("mail.imap.starttls.enable", "true");
            }
        } else if (security != Security.None) {
            props.put(String.format("mail.%s.ssl.checkserveridentity", proto), "true");
            if (proto.equals("imap")) {
                props.put("mail.imap.starttls.enable", "true");
            }
        }

        if (proto.equals("imaps")) {
            props.put("mail.imaps.socketFactory", sf);
        }

        props.setProperty("mail.imap.connectiontimeout", "1000");
        // TODO: use user defined proxy.
        Boolean useProxy = false;
        //noinspection ConstantConditions
        if (useProxy) {
            props.put("mail.imap.socks.host", "10.0.2.2");
            props.put("mail.imap.socks.port", "1080");
        }
        try {
            Session session = Session.getInstance(props, null);
//this.session.setDebug(true);
            store = session.getStore(proto);
            store.connect(server, username, password);
            //res.hasUIDPLUS = ((IMAPStore) store).hasCapability("UIDPLUS");
//Log.d(TAG, "has UIDPLUS="+res.hasUIDPLUS);

            Folder[] folders = store.getPersonalNamespaces();
            Folder folder = folders[0];
//Log.d(TAG,"Personal Namespaces="+folder.getFullName());
            if (folderOverride.length() > 0) {
                sfolder = folderOverride;
            } else if (folder.getFullName().length() == 0) {
                sfolder = "Notes";
            } else {
                char separator = folder.getSeparator();
                sfolder = folder.getFullName() + separator + "Notes";
            }
            // Get UIDValidity
            notesFolder = store.getFolder(sfolder);
            res.UIDValidity = ((IMAPFolder) notesFolder).getUIDValidity();
            res.errorMessage = "";
            res.returnCode = Imaper.ResultCodeSuccess;
            res.notesFolder = notesFolder;
            return res;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            res.errorMessage = e.getMessage();
            res.returnCode = Imaper.ResultCodeException;
            return res;
        }

    }

    /* Copy all notes from the IMAP server to the local directory using the UID as the file name.

     */
    public static void GetNotes(@NonNull Account account,
                                @NonNull Folder notesFolder,
                                @NonNull Context ctx,
                                @NonNull NotesDb storedNotes) throws MessagingException {
        //Long UIDM;
        //Message notesMessage;
        File directory = new File(ctx.getFilesDir() + "/" + account.name);
        if (notesFolder.isOpen()) {
            if ((notesFolder.getMode() & Folder.READ_ONLY) != 0)
                notesFolder.open(Folder.READ_ONLY);
        } else {
            notesFolder.open(Folder.READ_ONLY);
        }
        UIDValidity = GetUIDValidity(account, ctx);
        SetUIDValidity(account, UIDValidity, ctx);
        Message[] notesMessages = notesFolder.getMessages();
        //Log.d(TAG,"number of messages in folder="+(notesMessages.length));
        for (int index = notesMessages.length - 1; index >= 0; index--) {
            Message notesMessage = notesMessages[index];
            // write every message in files/{accountname} directory
            // filename is the original message uid
            Long UIDM = ((IMAPFolder) notesFolder).getUID(notesMessage);
            String suid = UIDM.toString();
            File outfile = new File(directory, suid);
            GetOneNote(outfile, notesMessage, storedNotes, account.name, suid, true);
        }
    }

    private static final Pattern patternColor = Pattern.compile("^COLOR:(.*?)$", Pattern.MULTILINE);
    private static final Pattern patternPosition = Pattern.compile("^POSITION:(.*?)$", Pattern.MULTILINE);
    private static final Pattern patternText = Pattern.compile("TEXT:(.*?)(END:|POSITION:)", Pattern.DOTALL);

    @NonNull
    public static Sticky ReadStickynote(@NonNull String stringres) {
/*
        Matcher matcherColor = patternColor.matcher(stringres);
        Colors color = Colors.NONE;
        if (matcherColor.find()) {
            String colorName = matcherColor.group(1);
            color = ((colorName == null) || colorName.equals("null")) ?
                    Colors.NONE :
                    Colors.valueOf(colorName);
        } else {
            color = Colors.NONE;
        }*/
/*

        Matcher matcherPosition = patternPosition.matcher(stringres);
        String position = matcherPosition.find() ?
                matcherPosition.group(1) :
                "";
*/

        /*Matcher matcherText = patternText.matcher(stringres);
        String text = "";
        if (matcherText.find()) {
            text = matcherText.group(1);
            // Kerio Connect puts CR+LF+space every 78 characters from line 2
            // first line seem to be smaller. We remove these characters
            text = text.replaceAll("\r\n ", "");
            // newline in Kerio is the string (not the character) "\n"
            text = text.replaceAll("\\\\n", "<br>");
        }
        */
        return new Sticky(getText(stringres),
                getPosition(stringres), getColor(stringres));
    }

    private static String getPosition(String stringres) {

        Matcher matcherPosition = patternPosition.matcher(stringres);
        return matcherPosition.find() ?
                matcherPosition.group(1) :
                "";
    }

    private static String getText(String stringres) {
        Matcher matcherText = patternText.matcher(stringres);
        String text = "";
        if (matcherText.find()) {
            text = matcherText.group(1);
            // Kerio Connect puts CR+LF+space every 78 characters from line 2
            // first line seem to be smaller. We remove these characters
            text = text.replaceAll("\r\n ", "");
            // newline in Kerio is the string (not the character) "\n"
            text = text.replaceAll("\\\\n", "<br>");
        }
        return text;
    }

    private static Colors getColor(String stringres) {
        Matcher matcherColor = patternColor.matcher(stringres);
        Colors color = Colors.NONE;
        if (matcherColor.find()) {
            String colorName = matcherColor.group(1);
            return ((colorName == null) || colorName.equals("null")) ?
                    Colors.NONE :
                    Colors.valueOf(colorName);
        } else {
            return Colors.NONE;
        }

    }

    private static boolean IsConnected() {
        return store != null && store.isConnected();
    }

    public static void DeleteNote(int numMessage) throws MessagingException {
        Folder notesFolder = store.getFolder(sfolder);
        if (notesFolder.isOpen()) {
            if ((notesFolder.getMode() & Folder.READ_WRITE) != 0) {
                notesFolder.open(Folder.READ_WRITE);
            }
        } else {
            notesFolder.open(Folder.READ_WRITE);
        }

        //Log.d(TAG,"UID to remove:"+numMessage);
        Message[] msgs = {((IMAPFolder) notesFolder).getMessageByUID(numMessage)};
        notesFolder.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
        ((IMAPFolder) notesFolder).expunge(msgs);
    }

    // Put values in shared preferences
    public static void SetUIDValidity(@NonNull Account account, Long UIDValidity, @NonNull Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(account.name, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Name", "valid_data");
        //Log.d(TAG, "UIDValidity set to in shared_prefs:"+UIDValidity);
        editor.putLong("UIDValidity", UIDValidity);
        editor.apply();
    }

    // Retrieve values from shared preferences:
    public static Long GetUIDValidity(@NonNull Account account, @NonNull Context ctx) {
        UIDValidity = (long) -1;
        SharedPreferences preferences = ctx.getSharedPreferences(account.name, Context.MODE_MULTI_PROCESS);
        String name = preferences.getString("Name", "");
        if (!name.equalsIgnoreCase("")) {
            UIDValidity = preferences.getLong("UIDValidity", -1);
            //Log.d(TAG, "UIDValidity got from shared_prefs:"+UIDValidity);
        }
        return UIDValidity;
    }

    public static void DisconnectFromRemote() {
        try {
            store.close();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
/*

    */
/**
 * @param uid ID of the message as created by the IMAP server
 * @param where TODO: what is this?
 * @param removeMinus TODO: Why?
 * @param nameDir Name of the account with which this message is associated, used to create the
 *                directory in which to store it.
 * @return A Java mail message object.
 *//*

    @Nullable
    public static Message ReadMailFromFile(@NonNull String uid,
                                           Where where,
                                           boolean removeMinus,
                                           @NonNull String nameDir) {
        File mailFile;
        Message message = null;
        mailFile = new File(nameDir, uid);

        switch (where) {
            case NEW:
                nameDir = nameDir + "/new";
                if (removeMinus) uid = uid.substring(1);
                break;
            case DELETED:
                nameDir = nameDir + "/deleted";
                break;
            case ROOT_AND_NEW:
                if (!mailFile.exists()) {
                    nameDir = nameDir + "/new";
                    if (removeMinus) uid = uid.substring(1);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unrecognized value for where argument: " + where);
        }

        mailFile = new File(nameDir, uid);
        InputStream mailFileInputStream = null;
        try {
            mailFileInputStream = new FileInputStream(mailFile);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            message = new MimeMessage(session, mailFileInputStream);
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return message;
    }
*/

    /**
     * @param uid         ID of the message as created by the IMAP server
     * @param newFilesDir Directory in which it is stored.
     * @return A Java mail message object.
     */
    @Nullable
    public static Message ReadMailFromFileNew(@NonNull String uid,
                                              @NonNull File newFilesDir) {
        //File mailFile;
        //Message message = null;
        //mailFile = new File(nameDir, uid);

        if (false) {
            uid = uid.substring(1);
        }
        return ReadMailFromFile(newFilesDir, uid);
    }

    /**
     * @param uid         ID of the message as created by the IMAP server
     * @param fileDir     Name of the account with which this message is associated, used to find the
     *                    directory in which it is stored.
     * @return A Java mail message object.
     */
    @Nullable
    public static Message ReadMailFromFileRootAndNew(@NonNull String uid,
                                                     @NonNull File fileDir) {
        //File mailFile;
        //Message message = null;
        File mailFile = new File(fileDir, uid);

        if (!mailFile.exists()) {
            fileDir = new File(fileDir, "new");
            uid = uid.substring(1);
        }

        return ReadMailFromFile(fileDir, uid);
    }

// --Commented out by Inspection START (11/26/16 11:46 PM):
//    /**
//     * @param uid         ID of the message as created by the IMAP server
//     * @param nameDir     Name of the account with which this message is associated, used to find the
//     *                    directory in which it is stored.
//     * @return A Java mail message object.
//     */
//    @Nullable
//    public static Message ReadMailFromFileDeleted(@NonNull String uid,
//                                                  @NonNull String nameDir) {
//        return ReadMailFromFile(new File(nameDir, "deleted"), uid);
//    }
// --Commented out by Inspection STOP (11/26/16 11:46 PM)


    /**
     * @param uid     ID of the message as created by the IMAP server
     * @param nameDir Name of the account with which this message is associated, used to find the
     *                directory in which it is stored.
     * @return A Java mail message object.
     */
    @Nullable
    private static Message ReadMailFromFile(@NonNull File nameDir,
                                            @NonNull String uid) {
        File mailFile = new File(nameDir, uid);

        try (InputStream mailFileInputStream = new FileInputStream(mailFile)) {
            try {
                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);
                return new MimeMessage(session, mailFileInputStream);
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "Exception getting MimeMessage.");
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            Log.d(TAG, "File not found opening mailFile: " + mailFile.getAbsolutePath());
            e1.printStackTrace();
        } catch (IOException exIO) {
            //TODO: handle this properly
            Log.d(TAG, "IO exception opening mailFile: " + mailFile.getAbsolutePath());
            exIO.printStackTrace();
        }
        return null;
    }

    public static AppendUID[] sendMessageToRemote(@NonNull Message[] message) throws MessagingException {
        notesFolder = store.getFolder(sfolder);
        if (notesFolder.isOpen()) {
            if ((notesFolder.getMode() & Folder.READ_WRITE) != 0)
                notesFolder.open(Folder.READ_WRITE);
        } else {
            notesFolder.open(Folder.READ_WRITE);
        }
        return ((IMAPFolder) notesFolder).appendUIDMessages(message);
    }

    public static void ClearHomeDir(@NonNull Account account, @NonNull Context ctx) {
        File directory = new File(ctx.getFilesDir() + "/" + account.name);
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Do we really need the Context argument or could we call getApplicationContext instead?
     *
     * @param accountName Name of the account as defined by the user, this is not the email address.
     * @param applicationContext Global context not an activity context.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void CreateDirs(@NonNull String accountName, @NonNull Context applicationContext) {
        Log.d(TAG, "CreateDirs(String: " + accountName);
        File dir = new File(applicationContext.getFilesDir(), accountName);
        //File directory = new File(stringDir);
        //directory.mkdirs();
        (new File(dir, "new")).mkdirs();
        (new File(dir, "deleted")).mkdirs();
    }

    private static void GetOneNote(@NonNull File outfile,
                                   @NonNull Message notesMessage,
                                   @NonNull NotesDb storedNotes,
                                   String accountName,
                                   String suid, boolean updateDb) {

        try (OutputStream str = new FileOutputStream(outfile)) {
            notesMessage.writeTo(str);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!updateDb) return;

        String title = null;
        String[] rawvalue = null;
        // Some servers (such as posteo.de) don't encode non us-ascii characters in subject
        // This is a workaround to handle them
        // "lä ö ë" subject should be stored as =?charset?encoding?encoded-text?=
        // either =?utf-8?B?bMOkIMO2IMOr?=  -> Quoted printable
        // or =?utf-8?Q?l=C3=A4 =C3=B6 =C3=AB?=  -> Base64
        try {
            rawvalue = notesMessage.getHeader("Subject");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            title = notesMessage.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rawvalue[0].length() >= 2) {
            if (!(rawvalue[0].substring(0, 2).equals("=?"))) {
                try {
                    title = new String(title.getBytes("ISO-8859-1"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                title = new String(title.getBytes("ISO-8859-1"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Get INTERNALDATE
        //String internaldate = null;
        Date MessageInternaldate = null;
        try {
            MessageInternaldate = notesMessage.getReceivedDate();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //String DATE_FORMAT = "yyyy-MM-dd HH:MM:ss";
        //SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ROOT);
        String internaldate = Utilities.internalDateFormat.format(MessageInternaldate);

        OneNote aNote = new OneNote(
                title,
                internaldate,
                suid);
        storedNotes.InsertANoteInDb(aNote, accountName);
    }

    public static boolean handleRemoteNotes(@NonNull Context context, @NonNull Folder notesFolder, @NonNull NotesDb storedNotes, String accountName, @NonNull String usesticky)
            throws MessagingException {

        Message notesMessage;
        boolean result = false;
        ArrayList<Long> uids = new ArrayList<>();
        ArrayList<String> localListOfNotes = new ArrayList<>();
        String remoteInternaldate;
        String localInternaldate;
        Flags flags;

        if (notesFolder.isOpen()) {
            if ((notesFolder.getMode() & Folder.READ_ONLY) != 0)
                notesFolder.open(Folder.READ_WRITE);
        } else {
            notesFolder.open(Folder.READ_WRITE);
        }

        // Get local list of notes uids
        String rootString = context.getFilesDir() + "/" + accountName;
        File rootDir = new File(rootString);
        File[] files = rootDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                localListOfNotes.add(file.getName());
            }
        }

        // Add to local device, new notes added to remote
        Message[] notesMessages = ((IMAPFolder) notesFolder).getMessagesByUID(1, UIDFolder.LASTUID);
        for (int index = notesMessages.length - 1; index >= 0; index--) {
            notesMessage = notesMessages[index];
            Long uid = ((IMAPFolder) notesFolder).getUID(notesMessage);
            // Get FLAGS
            flags = notesMessage.getFlags();
            boolean deleted = notesMessage.isSet(Flags.Flag.DELETED);
            // Builds remote list while in the loop, but only if not deleted on remote
            if (!deleted) {
                uids.add(((IMAPFolder) notesFolder).getUID(notesMessage));
            }
            String suid = uid.toString();
            if (!(localListOfNotes.contains(suid))) {
                File outfile = new File (rootDir, suid);
                GetOneNote(outfile, notesMessage, storedNotes, accountName, suid, true);
                result = true;
            } else if (usesticky.equals("true")) {
                //Log.d (TAG,"MANAGE STICKY");
                remoteInternaldate = notesMessage.getSentDate().toLocaleString();
                localInternaldate = storedNotes.GetDate(suid, accountName);
                if (!(remoteInternaldate.equals(localInternaldate))) {
                    File outfile = new File(rootDir, suid);
                    GetOneNote(outfile, notesMessage, storedNotes, accountName, suid, false);
                    result = true;
                }
            }
        }

        // Remove from local device, notes removed from remote
        for (String suid : localListOfNotes) {
            Long uid = Long.valueOf(suid);
            if (!(uids.contains(uid))) {
                // remove file from deleted
                File toDelete = new File(rootDir, suid);
                toDelete.delete();
                // Remove note from database
                storedNotes.DeleteANote(suid, accountName);
                result = true;
            }
        }

        return result;
    }

    public static void RemoveAccount(@NonNull Context context, @NonNull Account account) {
        // remove Shared Preference file
        String rootString = context.getFilesDir().getParent() +
                File.separator + "shared_prefs";
        File rootDir = new File (rootString);
        File toDelete = new File (rootDir, account.name + ".xml");
        toDelete.delete();
        // Remove all files and sub directories
        File filesDir = context.getFilesDir();
        File[] files = filesDir.listFiles();
        for (File file : files) {
            file.delete();
        }
        // Delete account name entries in database
        NotesDb storedNotes = new NotesDb(context);
        storedNotes.OpenDb();
        storedNotes.ClearDb(account.name);
        storedNotes.CloseDb();
    }
}
