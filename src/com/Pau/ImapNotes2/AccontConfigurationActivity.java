package com.Pau.ImapNotes2;

import java.util.ArrayList;
import java.util.List;

import com.Pau.ImapNotes2.R;

import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class AccontConfigurationActivity extends Activity implements OnItemSelectedListener{
  public static final int TO_REFRESH = 999;
  private static final String TAG = "AccontConfigurationActivity";
  
  private ConfigurationFile settings;
  private Imaper imapFolder;
  
  private TextView usernameTextView;
  private TextView passwordTextView;
  private TextView serverTextView;
  private TextView portnumTextView;
  private CheckBox stickyCheckBox;
  private Spinner securitySpinner;
  private String security;
  private int security_i;
//  private ImapNotes2Result res;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.account_selection);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    this.usernameTextView = (TextView)findViewById(R.id.usernameEdit);
    this.passwordTextView = (TextView)findViewById(R.id.passwordEdit);
    this.serverTextView = (TextView)findViewById(R.id.serverEdit);
    this.portnumTextView = (TextView)findViewById(R.id.portnumEdit);
    this.stickyCheckBox = (CheckBox)findViewById(R.id.stickyCheckBox);

    securitySpinner = (Spinner) findViewById(R.id.securitySpinner);
    List<String> list = new ArrayList<String>();
    list.add("None");
    list.add("SSL/TLS");
    list.add("SSL/TLS (accept all certificates)");
    list.add("STARTTLS");
    list.add("STARTTLS (accept all certificates)");
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
      (this, android.R.layout.simple_spinner_item,list);
    dataAdapter.setDropDownViewResource
      (android.R.layout.simple_spinner_dropdown_item);
    securitySpinner.setAdapter(dataAdapter);
    // Spinner item selection Listener  
    securitySpinner.setOnItemSelectedListener(this);       

    this.settings = ((ImapNotes2)getApplicationContext()).GetConfigurationFile();
    this.imapFolder = ((ImapNotes2)getApplicationContext()).GetImaper();
  
    this.usernameTextView.setText(this.settings.GetUsername());
    this.passwordTextView.setText(this.settings.GetPassword());
    this.serverTextView.setText(this.settings.GetServer());
    this.portnumTextView.setText(this.settings.GetPortnum());
    this.security = this.settings.GetSecurity();
    if (this.security == null) this.security = "0";
    this.security_i = Integer.parseInt(this.security);
    this.securitySpinner.setSelection(this.security_i);
    this.stickyCheckBox.setChecked(Boolean.parseBoolean(this.settings.GetUsesticky()));
  }
  
  // DoLogin method is defined in account_selection.xml (account_selection layout)
  public void DoLogin(View v) {
    ProgressDialog loadingDialog = ProgressDialog.show(this, "ImapNotes2" , "Logging into your account... ", true);
    this.settings.SetUsername(this.usernameTextView.getText().toString().trim());
    this.settings.SetPassword(this.passwordTextView.getText().toString().trim());
    this.settings.SetServer(this.serverTextView.getText().toString().trim());
    this.settings.SetPortnum(this.portnumTextView.getText().toString());
    this.settings.SetSecurity(this.security);
    this.settings.SetUsesticky(String.valueOf(this.stickyCheckBox.isChecked()));
    
    new LoginThread().execute(this.imapFolder, this.settings, loadingDialog, this);
    
  }
  
  class LoginThread extends AsyncTask<Object, Void, Boolean>{
    
      private ImapNotes2Result res = new ImapNotes2Result();
      protected Boolean doInBackground(Object... stuffs) {
      try {
        this.res=((Imaper)stuffs[0]).ConnectToProvider(
            ((ConfigurationFile)stuffs[1]).GetUsername(),
            ((ConfigurationFile)stuffs[1]).GetPassword(),
            ((ConfigurationFile)stuffs[1]).GetServer(),
            ((ConfigurationFile)stuffs[1]).GetPortnum(),
            ((ConfigurationFile)stuffs[1]).GetSecurity(),
            ((ConfigurationFile)stuffs[1]).GetUsesticky());
        if (this.res.returnCode==0) {
          ((ConfigurationFile)stuffs[1]).SaveConfigurationToXML();
          ((AccontConfigurationActivity)stuffs[3]).setResult(AccontConfigurationActivity.TO_REFRESH);
	  ((AccontConfigurationActivity)stuffs[3]).finish();
		return true;
	} else {
		return false;
	}
      } catch (Exception e) {
        Log.v("ImapNotes2", e.getMessage());
      } finally {
        ((ProgressDialog)stuffs[2]).dismiss();
      }
      return false;
    }
    
    protected void onPostExecute(Boolean result){
      if(result){
        Toast.makeText(getApplicationContext(), "Connection Established",
          Toast.LENGTH_LONG).show();
      }else {
    	final Toast tag = Toast.makeText(getApplicationContext(), this.res.errorMessage,Toast.LENGTH_LONG);
        tag.show();
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {tag.show();}
            public void onFinish() {tag.show();}
        }.start();
      }
    }
  }
  
  public boolean onCreateOptionsMenu(Menu menu) {
    return true;
  }

  public boolean onOptionsItemSelected (MenuItem item){
    switch (item.getItemId()){
    case android.R.id.home:
      NavUtils.navigateUpFromSameTask(this);
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    this.security = Integer.toString(position);
    this.security_i = position;
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // TODO Auto-generated method stub  
  }

}
