package com.Pau.ImapNotes2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class NewNoteActivity extends Activity{
	
    private static final int SAVE_BUTTON = 5;
	private static final String TAG = "IN_NewNoteActivity";
	private String sticky;
	private String color = "NONE";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.ResetColors();
		this.sticky = (String)getIntent().getExtras().get("usesSticky");
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
                    if (this.sticky.equals("true")) {
                		this.color="YELLOW";
                    }
                    intent.putExtra("SAVE_ITEM_COLOR",this.color);
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
