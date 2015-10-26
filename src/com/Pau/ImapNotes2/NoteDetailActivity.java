package com.Pau.ImapNotes2;

import java.util.HashMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;

import com.Pau.ImapNotes2.Miscs.OneNote;
import com.Pau.ImapNotes2.Miscs.Sticky;
import com.Pau.ImapNotes2.Sync.SyncUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class NoteDetailActivity extends Activity {
    
    private static final int DELETE_BUTTON = 3;
    private static final int EDIT_BUTTON = 6;
    private HashMap hm;
    private String usesticky;
    private Sticky sticky;
    private String stringres;
    private String color;
    private String position;
    private int realColor = R.id.yellow;
    private Boolean isClicked = false;
    private Message message;
    private String suid; // uid as string
    private final static int ROOT_AND_NEW = 3;
    private static final String TAG = "IN_NoteDetailActivity";
    
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.note_detail);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            // Don't display keyboard when on note detail, only if user touches the screen
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );
    
            this.hm = (HashMap)getIntent().getExtras().get("selectedNote");
            this.usesticky = (String)getIntent().getExtras().get("useSticky");

           suid = this.hm.get("uid").toString();
           String rootDir = (ImapNotes2.getAppContext()).getFilesDir() + "/" +
                   Listactivity.imapNotes2Account.GetAccountname();
           message = SyncUtils.ReadMailFromFile(suid, ROOT_AND_NEW, true, rootDir);
           sticky = GetInfoFromMessage(message);
           stringres = sticky.GetText();
           position = sticky.GetPosition();
           color = sticky.GetColor();
           Spanned plainText = Html.fromHtml(stringres);
           ((EditText)findViewById(R.id.bodyView)).setText(plainText);
            this.ResetColors();
            //invalidateOptionsMenu();
    }
    
    public void onClick(View v){
        this.isClicked = true;
    }
    
    private void ResetColors(){
        ((EditText)findViewById(R.id.bodyView)).setBackgroundColor(Color.TRANSPARENT);
        ((EditText)findViewById(R.id.bodyView)).setTextColor(Color.BLACK);
        Colors currentColor = Colors.valueOf(color);
        switch (currentColor) {
          case BLUE:
                (findViewById(R.id.scrollView)).setBackgroundColor(0xFFA6CAFD);
                this.realColor = R.id.blue;
                break;
          case WHITE:
                (findViewById(R.id.scrollView)).setBackgroundColor(0xFFFFFFFF);
                this.realColor = R.id.white;
                break;
          case YELLOW:
                (findViewById(R.id.scrollView)).setBackgroundColor(0xFFFFFFCC);
                this.realColor = R.id.yellow;
                break;
          case PINK:
                (findViewById(R.id.scrollView)).setBackgroundColor(0xFFFFCCCC);
                this.realColor = R.id.pink;
                break;
          case GREEN:
                (findViewById(R.id.scrollView)).setBackgroundColor(0xFFCCFFCC);
                this.realColor = R.id.green;
                break;
          default:
                (findViewById(R.id.scrollView)).setBackgroundColor(Color.TRANSPARENT);
        }
        invalidateOptionsMenu();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item= menu.findItem(R.id.color);
        super.onPrepareOptionsMenu(menu);
        //depending on your conditions, either enable/disable
        if (this.usesticky.equals("true")) {
                item.setVisible(true);
        } else {
                item.setVisible(false);
        }
        menu.findItem(this.realColor).setChecked(true);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item){
        Intent intent=new Intent();  
        switch (item.getItemId()){
        case R.id.delete:
            //Log.d(TAG,"We ask to delete Message #"+this.currentNote.get("number"));
            intent.putExtra("DELETE_ITEM_NUM_IMAP",suid);  
            setResult(NoteDetailActivity.DELETE_BUTTON, intent);
            finish();//finishing activity  
            return true;
        case R.id.save:
            //Log.d(TAG,"We ask to modify Message #"+this.currentNote.get("number"));
            intent.putExtra("EDIT_ITEM_NUM_IMAP",suid);
            intent.putExtra("EDIT_ITEM_TXT",
            Html.toHtml(((EditText)findViewById(R.id.bodyView)).getText()));
            if (!this.usesticky.equals("true")) {
                this.color="NONE";
            }
            intent.putExtra("EDIT_ITEM_COLOR",this.color);
            setResult(NoteDetailActivity.EDIT_BUTTON, intent);
            finish();//finishing activity  
            return true;
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        case R.id.blue:
            item.setChecked(true);
            this.color = "BLUE";
            (findViewById(R.id.scrollView)).setBackgroundColor(0xFFA6CAFD);
            return true;
        case R.id.white:
            item.setChecked(true);
            this.color = "WHITE";
            (findViewById(R.id.scrollView)).setBackgroundColor(0xFFFFFFFF);
            return true;
        case R.id.yellow:
            item.setChecked(true);
            this.color = "YELLOW";
            (findViewById(R.id.scrollView)).setBackgroundColor(0xFFFFFFCC);
            return true;
        case R.id.pink:
            item.setChecked(true);
            this.color = "PINK";
            (findViewById(R.id.scrollView)).setBackgroundColor(0xFFFFCCCC);
            return true;
        case R.id.green:
            item.setChecked(true);
            this.color = "GREEN";
            (findViewById(R.id.scrollView)).setBackgroundColor(0xFFCCFFCC);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public enum Colors {
      BLUE,
      WHITE,
      YELLOW,
      PINK,
      GREEN,
      NONE
    }

    private Sticky GetInfoFromMessage (Message message) {
        ContentType contentType = null;
        String stringres = null;
        InputStream iis = null;
        String color = "NONE";
        String charset;
        Sticky sticky = null;
        try {
//Log.d(TAG, "Contenttype as string:"+message.getContentType());
            contentType = new ContentType(message.getContentType() );
            charset = contentType.getParameter("charset");
            iis = (InputStream)message.getContent();
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
Log.d(TAG,"+++ isMimeType text/plain (contentType):"+message.getContentType());
} else if (message.isMimeType("multipart/*")) {
Log.d(TAG,"+++ isMimeType multipart/* (contentType):"+message.getContentType());
Object content = message.getContent();
         Multipart mp = (Multipart) content;
         int count = mp.getCount();
         for (int i = 0; i < count; i++) GetPart(mp.getBodyPart(i));
} else if (message.isMimeType("message/rfc822")) {
Log.d(TAG,"+++ isMimeType message/rfc822/* (contentType):"+message.getContentType());
GetPart((Part) message.getContent());
} else if (message.isMimeType("image/jpeg")) {
Log.d(TAG,"+++ isMimeType image/jpeg (contentType):"+message.getContentType());
} else if (message.getContentType().contains("image/")) {
Log.d(TAG,"+++ isMimeType image/jpeg (contentType):"+message.getContentType());
} else {
  Object o = message.getContent();
  if (o instanceof String) {
      Log.d(TAG,"+++ instanceof String");
  } else if (o instanceof InputStream) {
      Log.d(TAG,"+++ instanceof InputStream");
  } else Log.d(TAG,"+++ instanceof ???");
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

        return new Sticky(stringres, "", "NONE");
    }

    private Sticky ReadPlainnote(String stringres) {
//        Log.d(TAG,"From server (plain):"+stringres);
        stringres = stringres.replaceAll("\n", "<br>");

        return new Sticky(stringres, "", "NONE");
    }

    private void WriteMailToFile (String suid, Message message) {
        String directory = (ImapNotes2.getAppContext()).getFilesDir() + "/" +
                Listactivity.imapNotes2Account.GetAccountname();
        try {
            File outfile = new File (directory, suid);
            OutputStream str = new FileOutputStream(outfile);
            message.writeTo(str);
        }  catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



}
