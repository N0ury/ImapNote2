package com.Pau.ImapNotes2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import static com.Pau.ImapNotes2.NoteDetailActivity.Colors;

public class NewNoteActivity extends Activity {

    private static final int SAVE_BUTTON = 5;
    @SuppressWarnings("unused")
    private static final String TAG = "IN_NewNoteActivity";
    private boolean sticky;
    @NonNull
    private Colors color = Colors.NONE;
    //region Intent item names
    public static final String usesSticky = "usesSticky";
    //endregion

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        this.ResetColors();
        this.sticky = (boolean) getIntent().getExtras().get(usesSticky);
    }

    private void ResetColors() {
        findViewById(R.id.editNote).setBackgroundColor(Color.TRANSPARENT);
        ((EditText) findViewById(R.id.editNote)).setTextColor(Color.BLACK);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.newnote, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                Intent intent = new Intent();
                intent.putExtra("SAVE_ITEM", Html.toHtml(((EditText) findViewById(R.id.editNote)).getText()));
                if (this.sticky) {
                    this.color = Colors.YELLOW;
                }
                intent.putExtra("SAVE_ITEM_COLOR", this.color);
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
