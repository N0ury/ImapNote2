package com.Pau.ImapNotes2;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.content.Intent;
import com.Pau.ImapNotes2.R;
import com.Pau.ImapNotes2.Miscs.OneNote;

public class NewNoteActivity extends Activity{
	
    private static final int SAVE_BUTTON = 5;
	private OneNote currentNote;
	private static final String TAG = "IN_NewNoteActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	setContentView(R.layout.new_note);
	}
	
    /***************************************************/
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0, NewNoteActivity.SAVE_BUTTON, 0, "Save");
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
                case NewNoteActivity.SAVE_BUTTON:
//		Log.d(TAG,"Save button has been clicked");
                Intent intent=new Intent();
		intent.putExtra("SAVE_ITEM",Html.toHtml(((EditText)findViewById(R.id.editNote)).getText()));
                setResult(SAVE_BUTTON, intent);
                finish();//finishing activity
                return true;
        }
	return false;
    }
}
