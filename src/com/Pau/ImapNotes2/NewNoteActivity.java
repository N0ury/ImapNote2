package com.Pau.ImapNotes2;

import android.app.Activity;
import android.text.Html;
import android.widget.EditText;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.content.Intent;
import android.graphics.Color;

import com.Pau.ImapNotes2.R;
import com.Pau.ImapNotes2.Miscs.OneNote;
import android.support.v4.app.NavUtils;

public class NewNoteActivity extends Activity{
	
    private static final int SAVE_BUTTON = 5;
	private static final String TAG = "IN_NewNoteActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.ResetColors();
	}
	
	private void ResetColors(){
		((EditText)findViewById(R.id.editNote)).setBackgroundColor(Color.TRANSPARENT);
	    ((EditText)findViewById(R.id.editNote)).setTextColor(Color.BLACK);
	}
	
    public boolean onCreateOptionsMenu(Menu menu){
    	getMenuInflater().inflate(R.menu.newnote, menu);
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
		case R.id.save:
                	Intent intent=new Intent();
			intent.putExtra("SAVE_ITEM",Html.toHtml(((EditText)findViewById(R.id.editNote)).getText()));
                	setResult(SAVE_BUTTON, intent);
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
