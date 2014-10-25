package com.Pau.ImapNotes2;

import com.Pau.ImapNotes2.Miscs.OneNote;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.os.Bundle;
import java.util.HashMap;

public class NoteDetailActivity extends Activity{
	
	private OneNote currentNote;
	private HashMap hm;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail);
        
        //this.currentNote = getIntent().getIntExtra("selection", -1);
        this.hm = (HashMap)getIntent().getExtras().get("selectedNote");
	currentNote=new OneNote(this.hm.get("title").toString(),
				this.hm.get("body").toString(),
				this.hm.get("date").toString());


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

}
