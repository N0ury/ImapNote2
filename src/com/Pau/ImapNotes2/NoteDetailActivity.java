package com.Pau.ImapNotes2;

import com.Pau.ImapNotes2.R;
import com.Pau.ImapNotes2.Miscs.OneNote;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.os.Bundle;
import java.util.HashMap;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.content.Intent;
import android.support.v4.app.NavUtils;

public class NoteDetailActivity extends Activity {
	
	private static final int DELETE_BUTTON = 3;
	private static final int EDIT_BUTTON = 6;
	private OneNote currentNote;
	private HashMap hm;
	private Boolean isClicked = false;
	private static final String TAG = "IN_NoteDetailActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail);
	getActionBar().setDisplayHomeAsUpEnabled(true);
        
        this.hm = (HashMap)getIntent().getExtras().get("selectedNote");
	currentNote=new OneNote(this.hm.get("title").toString(),
				this.hm.get("body").toString(),
				this.hm.get("date").toString(),
				(this.hm.get("number").toString()));

        String plainText = Html.fromHtml(this.currentNote.GetBody()).toString();
       ((EditText)findViewById(R.id.bodyView)).setText(plainText);
       this.ResetColors();
       
        
	}
	
	public void onClick(View v){
//		Log.d(TAG,"In onClick");
		this.isClicked = true;
	}
	
	private void ResetColors(){
		((EditText)findViewById(R.id.bodyView)).setBackgroundColor(Color.TRANSPARENT);
	    ((EditText)findViewById(R.id.bodyView)).setTextColor(Color.BLACK);
	}

    public boolean onCreateOptionsMenu(Menu menu){
	getMenuInflater().inflate(R.menu.detail, menu);
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
        	setResult(NoteDetailActivity.EDIT_BUTTON, intent);
        	finish();//finishing activity  
        	return true;
	case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
        	return true;
	default:
		return super.onOptionsItemSelected(item);
        }
    }
}
