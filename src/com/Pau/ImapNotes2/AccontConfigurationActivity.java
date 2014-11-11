package com.Pau.ImapNotes2;

import com.Pau.ImapNotes2.R;
import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Miscs.Imaper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;

public class AccontConfigurationActivity extends Activity {
	public static final int TO_REFRESH = 999;
	
	private ConfigurationFile settings;
	private Imaper imapFolder;
	
	private TextView usernameTextView;
	private TextView passwordTextView;
	private TextView serverTextView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_selection);
        this.usernameTextView = (TextView)findViewById(R.id.usernameEdit);
        this.passwordTextView = (TextView)findViewById(R.id.passwordEdit);
        this.serverTextView = (TextView)findViewById(R.id.serverEdit);
        
        this.settings = ((ImapNotes2)getApplicationContext()).GetConfigurationFile();
        this.imapFolder = ((ImapNotes2)getApplicationContext()).GetImaper();
        
        this.usernameTextView.setText(this.settings.GetUsername());
        this.passwordTextView.setText(this.settings.GetPassword());
        this.serverTextView.setText(this.settings.GetServer());
        
	
	}
	
	public void DoLogin(View v) {
		ProgressDialog loadingDialog = ProgressDialog.show(this, "ImapNotes2" , "Logging in to your account... ", true);
		this.settings.SetUsername(this.usernameTextView.getText().toString());
		this.settings.SetPassword(this.passwordTextView.getText().toString());
		this.settings.SetServer(this.serverTextView.getText().toString());
		
		new LoginThread().execute(this.imapFolder, this.settings, loadingDialog, this);
		
	}
	
	class LoginThread extends AsyncTask<Object, Void, Boolean>{
		
		protected Boolean doInBackground(Object... stuffs) {			
			try {
				((Imaper)stuffs[0]).ConnectToProvider(((ConfigurationFile)stuffs[1]).GetUsername(), ((ConfigurationFile)stuffs[1]).GetPassword(), ((ConfigurationFile)stuffs[1]).GetServer());
				((ConfigurationFile)stuffs[1]).SaveConfigurationToXML();
				((AccontConfigurationActivity)stuffs[3]).setResult(AccontConfigurationActivity.TO_REFRESH);
				((AccontConfigurationActivity)stuffs[3]).finish();
				return true;
	        } catch (Exception e) {
				Log.v("ImapNotes2", e.getMessage());
			} finally {
				((ProgressDialog)stuffs[2]).dismiss();
			}
			
			return false;
		}
		
	}
	
}

