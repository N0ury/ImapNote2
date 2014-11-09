package com.Pau.imapnote2;

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
import com.Pau.imapnote2.Miscs.OneNote;

public class NewNoteActivity extends Activity{
	
        private static final int SAVE_BUTTON = 5;
	private OneNote currentNote;
	private static final String TAG = "IN_NewNoteActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	setContentView(R.layout.new_note);
	}
	
    public void AddMessage(){
        Log.d(TAG,"Received request to add new message");
        try {
		String[] tok = ((EditText)findViewById(R.id.editNote)).getText().toString().split("\n", 2);
		String title = tok[0];
		String body = Html.toHtml(((EditText)findViewById(R.id.editNote)).getText()).toString();
		this.currentNote = new OneNote(title,body,"","");
// Alimenter OneNote avec les données saisies + uuid...
		// Here we ask to add the new note to the "Notes" folder
                ((ImapNotes2)this.getApplicationContext()).GetImaper().AddNote(this.currentNote);
		Intent intent=new Intent();  
intent.putExtra("SAVE_ITEM","Mon titre de test à moi.");  
		setResult(NewNoteActivity.SAVE_BUTTON, intent);
		finish();//finishing activity  
        } catch (Exception ex) {
                Log.d(TAG,"Exception rencontrée: " + ex.getMessage());
        }
    }

    /***************************************************/
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0, NewNoteActivity.SAVE_BUTTON, 0, "Save");
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
                case NewNoteActivity.SAVE_BUTTON:
		Log.d(TAG,"Save button has been clicked");
		this.AddMessage();
        }
	return false;
    }
}
