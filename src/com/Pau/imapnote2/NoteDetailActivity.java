package com.Pau.imapnote2;

import com.Pau.imapnote2.Miscs.OneNote;

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

public class NoteDetailActivity extends Activity{
	
	private static final int DELETE_BUTTON = 3;
	private OneNote currentNote;
	private HashMap hm;
	private static final String TAG = "IN_NoteDetailActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail);
        
        this.hm = (HashMap)getIntent().getExtras().get("selectedNote");
	currentNote=new OneNote(this.hm.get("title").toString(),
				this.hm.get("body").toString(),
				this.hm.get("date").toString(),
				(this.hm.get("number").toString()));

        String plainText = Html.fromHtml(this.currentNote.GetBody()).toString();
       ((EditText)findViewById(R.id.bodyView)).setText(plainText);
       this.ResetColors();
       
        
	}
	
	public void BeginEditMode(View v){
		((EditText)findViewById(R.id.bodyView)).setEnabled(true);
	}
	
	private void ResetColors(){
		((EditText)findViewById(R.id.bodyView)).setBackgroundColor(Color.TRANSPARENT);
	    ((EditText)findViewById(R.id.bodyView)).setTextColor(Color.BLACK);
	}

    /***************************************************/
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0, NoteDetailActivity.DELETE_BUTTON, 0, "Delete");

        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
                case NoteDetailActivity.DELETE_BUTTON:
		Log.d(TAG,"We ask to delete Message #"+this.currentNote.get("number"));
		Intent intent=new Intent();  
		intent.putExtra("DELETE_ITEM",this.currentNote.get("number"));  
		setResult(NoteDetailActivity.DELETE_BUTTON, intent);
		finish();//finishing activity  
		return true;
        }
	return false;
    }
}
