package com.Pau.ImapNotes2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.Pau.ImapNotes2.Miscs.Sticky;
import com.Pau.ImapNotes2.Sync.SyncUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;



public class NoteDetailActivity extends Activity {

    //private static final int DELETE_BUTTON = 3;
    private static final int EDIT_BUTTON = 6;
    private boolean usesticky;
    private Colors color;
    private int realColor = R.id.yellow;
    private String suid; // uid as string
    private final static int ROOT_AND_NEW = 3;
    private static final String TAG = "IN_NoteDetailActivity";

    //region Intent item names
    public static final String useSticky = "useSticky";
    public static final String selectedNote = "selectedNote";
    //endregion


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // Don't display keyboard when on note detail, only if user touches the screen
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        Bundle extras = getIntent().getExtras();
        HashMap hm = (HashMap) extras.get(selectedNote);
        usesticky = (boolean) extras.get(useSticky);
        assert hm != null;
        suid = hm.get("uid").toString();
        String rootDir = (ImapNotes2k.getAppContext()).getFilesDir() + "/" +
                Listactivity.imapNotes2Account.GetAccountname();
        Message message = SyncUtils.ReadMailFromFile(suid, ROOT_AND_NEW, true, rootDir);
        Sticky sticky = GetInfoFromMessage(message);
        String stringres = sticky.text;
        String position = sticky.position;
        color = sticky.color;
        Spanned plainText = Html.fromHtml(stringres);
        EditText editText = ((EditText) findViewById(R.id.bodyView));
        editText.setText(plainText);
        // TODO: Watch for changes to that we can auto save.
        // See http://stackoverflow.com/questions/7117209/how-to-know-key-presses-in-edittext#14251047
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //here is your code
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

        });
        ResetColors();
        //invalidateOptionsMenu();
    }

    // TODO: delete this?
    public void onClick(View v) {
        Boolean isClicked = true;
    }

    // realColor is misnamed.  It is the ID of the radio button widget that chooses the background
    // colour.
    private void ResetColors() {
        EditText bodyView = (EditText) findViewById(R.id.bodyView);
        bodyView.setBackgroundColor(Color.TRANSPARENT);
        bodyView.setTextColor(Color.BLACK);
        (findViewById(R.id.scrollView)).setBackgroundColor(color.colorCode);
        realColor = color.id;
        invalidateOptionsMenu();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.color);
        super.onPrepareOptionsMenu(menu);
        //depending on your conditions, either enable/disable
        item.setVisible(usesticky);
        menu.findItem(realColor).setChecked(true);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.delete:
                //Log.d(TAG,"We ask to delete Message #"+currentNote.get("number"));
                intent.putExtra(Listactivity.DELETE_ITEM_NUM_IMAP, suid);
                setResult(Listactivity.DELETE_BUTTON, intent);
                finish();//finishing activity
                return true;
            case R.id.save:
                Save();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.blue:
            case R.id.white:
            case R.id.yellow:
            case R.id.pink:
            case R.id.green:
                item.setChecked(true);
                color = Colors.fromId(itemId);
                (findViewById(R.id.scrollView)).setBackgroundColor(color.colorCode);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void Save() {
        Log.d(TAG, "Save");
        Intent intent = new Intent();
        intent.putExtra(Listactivity.EDIT_ITEM_NUM_IMAP, suid);
        intent.putExtra(Listactivity.EDIT_ITEM_TXT,
                Html.toHtml(((EditText) findViewById(R.id.bodyView)).getText()));
        if (!usesticky) {
            color = Colors.NONE;
        }
        intent.putExtra(Listactivity.EDIT_ITEM_COLOR, color);
        setResult(NoteDetailActivity.EDIT_BUTTON, intent);
        finish();//finishing activity

    }

    // List the colours together with the ids of the option widgets used to select them and the
    // RGB values used as the actual colours.  Doing this means that we do not need so much code
    // in switch statements, etc.
    public enum Colors {
        BLUE(R.id.blue, 0xFFA6CAFD),
        WHITE(R.id.white, 0xFFFFFFFF),
        YELLOW(R.id.yellow, 0xFFFFFFCC),
        PINK(R.id.pink, 0xFFFFCCCC),
        GREEN(R.id.green, 0xFFCCFFCC),
        // Is NONE ever used?
        NONE(0, 0);

        public final int id;
        public final int colorCode;

        private Colors(int id,
                       int colorCode) {
            this.id = id;
            this.colorCode = colorCode;
        }

        public static Colors fromId(int id) {

            for (Colors color : Colors.values()) {
                if (color.id == id)
                    return color;
            }
            throw new IllegalArgumentException("id not found in Colors: " + Integer.toString(id));
        }
    }

    private Sticky GetInfoFromMessage(Message message) {
        ContentType contentType = null;
        String stringres = null;
        InputStream iis = null;
        //Colors color = NONE;
        String charset;
        Sticky sticky = null;
        try {
//Log.d(TAG, "Contenttype as string:"+message.getContentType());
            contentType = new ContentType(message.getContentType());
            charset = contentType.getParameter("charset");
            iis = (InputStream) message.getContent();
            stringres = IOUtils.toString(iis, charset);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//Log.d(TAG,"contentType:"+contentType);
        if (contentType.match("text/x-stickynote")) {
            sticky = SyncUtils.ReadStickynote(stringres);
        } else if (contentType.match("TEXT/HTML")) {
            sticky = ReadHtmlnote(stringres);
        } else if (contentType.match("TEXT/PLAIN")) {
            sticky = ReadPlainnote(stringres);
        } else if (contentType.match("multipart/related")) {
// All next is a workaround
// All function need to be rewritten to handle correctly multipart and images
            if (contentType.getParameter("type").equalsIgnoreCase("TEXT/HTML")) {
                sticky = ReadHtmlnote(stringres);
            } else if (contentType.getParameter("type").equalsIgnoreCase("TEXT/PLAIN")) {
                sticky = ReadPlainnote(stringres);
            }
        } else if (contentType.getParameter("BOUNDARY") != null) {
            sticky = ReadHtmlnote(stringres);
        }
        return sticky;
    }

    private void GetPart(Part message) throws Exception {
        if (message.isMimeType("text/plain")) {
            Log.d(TAG, "+++ isMimeType text/plain (contentType):" + message.getContentType());
        } else if (message.isMimeType("multipart/*")) {
            Log.d(TAG, "+++ isMimeType multipart/* (contentType):" + message.getContentType());
            Object content = message.getContent();
            Multipart mp = (Multipart) content;
            int count = mp.getCount();
            for (int i = 0; i < count; i++) GetPart(mp.getBodyPart(i));
        } else if (message.isMimeType("message/rfc822")) {
            Log.d(TAG, "+++ isMimeType message/rfc822/* (contentType):" + message.getContentType());
            GetPart((Part) message.getContent());
        } else if (message.isMimeType("image/jpeg")) {
            Log.d(TAG, "+++ isMimeType image/jpeg (contentType):" + message.getContentType());
        } else if (message.getContentType().contains("image/")) {
            Log.d(TAG, "+++ isMimeType image/jpeg (contentType):" + message.getContentType());
        } else {
            Object o = message.getContent();
            if (o instanceof String) {
                Log.d(TAG, "+++ instanceof String");
            } else if (o instanceof InputStream) {
                Log.d(TAG, "+++ instanceof InputStream");
            } else Log.d(TAG, "+++ instanceof ???");
        }
    }

    private Sticky ReadHtmlnote(String stringres) {
//        Log.d(TAG,"From server (html):"+stringres);
        Spanned spanres = Html.fromHtml(stringres);
        stringres = Html.toHtml(spanres);
        stringres = stringres.replaceFirst("<p dir=ltr>", "");
        stringres = stringres.replaceFirst("<p dir=\"ltr\">", "");
        stringres = stringres.replaceAll("<p dir=ltr>", "<br>");
        stringres = stringres.replaceAll("<p dir=\"ltr\">", "<br>");
        stringres = stringres.replaceAll("</p>", "");

        return new Sticky(stringres, "", Colors.NONE);
    }

    private Sticky ReadPlainnote(String stringres) {
//        Log.d(TAG,"From server (plain):"+stringres);
        stringres = stringres.replaceAll("\n", "<br>");

        return new Sticky(stringres, "", Colors.NONE);
    }

    private void WriteMailToFile(String suid, Message message) {
        String directory = (ImapNotes2k.getAppContext()).getFilesDir() + "/" +
                Listactivity.imapNotes2Account.GetAccountname();
        try {
            File outfile = new File(directory, suid);
            OutputStream str = new FileOutputStream(outfile);
            message.writeTo(str);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
