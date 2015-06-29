package com.Pau.ImapNotes2;

import com.Pau.ImapNotes2.R;
import com.Pau.ImapNotes2.Miscs.OneNote;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.os.Bundle;
import java.util.HashMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.util.Log;
import android.content.Intent;
import android.support.v4.app.NavUtils;

public class NoteDetailActivity extends Activity {
	
	private static final int DELETE_BUTTON = 3;
	private static final int EDIT_BUTTON = 6;
	private static final int COLOR_WHITE = 1;
	private static final int COLOR_YELLOW = 2;
	private static final int COLOR_PINK = 3;
	private static final int COLOR_GREEN = 4;
	private static final int COLOR_BLUE = 5;
	private OneNote currentNote;
	private HashMap hm;
	private String sticky;
	private String color = "YELLOW";
	private int realColor = R.id.yellow;
	private Boolean isClicked = false;
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
        	this.sticky = (String)getIntent().getExtras().get("usesSticky");
			currentNote=new OneNote(this.hm.get("title").toString(),
				this.hm.get("body").toString(),
				this.hm.get("date").toString(),
				this.hm.get("number").toString(),
				this.hm.get("position").toString(),
				this.hm.get("color").toString());

    		Spanned plainText = Html.fromHtml(this.currentNote.GetBody());
        	((EditText)findViewById(R.id.bodyView)).setText(plainText);
        	this.ResetColors();
        	//invalidateOptionsMenu();
	}
	
	public void onClick(View v){
//		Log.d(TAG,"In onClick");
		this.isClicked = true;
	}
	
	private void ResetColors(){
		((EditText)findViewById(R.id.bodyView)).setBackgroundColor(Color.TRANSPARENT);
    	((EditText)findViewById(R.id.bodyView)).setTextColor(Color.BLACK);
		Colors currentColor = Colors.valueOf(currentNote.get("color"));
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
        if (this.sticky.equals("true")) {
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
//        	Log.d(TAG,"We ask to delete Message #"+this.currentNote.get("number"));
        	intent.putExtra("DELETE_ITEM_NUM_IMAP",this.currentNote.get("number"));  
        	setResult(NoteDetailActivity.DELETE_BUTTON, intent);
        	finish();//finishing activity  
        	return true;
        case R.id.save:
//        	Log.d(TAG,"We ask to modify Message #"+this.currentNote.get("number"));
        	intent.putExtra("EDIT_ITEM_NUM_IMAP",this.currentNote.get("number"));
        	intent.putExtra("EDIT_ITEM_TXT",
			Html.toHtml(((EditText)findViewById(R.id.bodyView)).getText()));
        	if (!this.sticky.equals("true")) {
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
}
