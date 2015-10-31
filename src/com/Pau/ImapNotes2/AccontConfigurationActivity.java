package com.Pau.ImapNotes2;

import java.util.ArrayList;
import java.util.List;

import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;
import com.Pau.ImapNotes2.Miscs.Imaper;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AccontConfigurationActivity extends AccountAuthenticatorActivity implements OnItemSelectedListener{
  public static final int TO_REFRESH = 999;
  public static final String AUTHORITY = "com.Pau.ImapNotes2.provider";
  private static final String TAG = "AccontConfigurationActivity";
  
  private Imaper imapFolder;
  
  private TextView accountnameTextView;
  private TextView usernameTextView;
  private TextView passwordTextView;
  private TextView serverTextView;
  private TextView portnumTextView;
  private TextView syncintervalTextView;
  private CheckBox stickyCheckBox;
  private Spinner securitySpinner;
  private ImapNotes2Account imapNotes2Account;
  private String security;
  private int security_i;
  private ConfigurationFile settings;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//addPreferencesFromResource(R.xml.account_preferences);
    setContentView(R.layout.account_selection);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    this.accountnameTextView = (TextView)(findViewById(R.id.accountnameEdit));
    this.usernameTextView = (TextView)findViewById(R.id.usernameEdit);
    this.passwordTextView = (TextView)findViewById(R.id.passwordEdit);
    this.serverTextView = (TextView)findViewById(R.id.serverEdit);
    this.portnumTextView = (TextView)findViewById(R.id.portnumEdit);
    this.syncintervalTextView = (TextView)findViewById(R.id.syncintervalEdit);
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

    imapNotes2Account = new ImapNotes2Account();
    this.imapFolder = ((ImapNotes2)getApplicationContext()).GetImaper();
    this.settings = new ConfigurationFile(this.getApplicationContext());

Bundle bundle=getIntent().getExtras();
for (String key : bundle.keySet()) {
    Object value = bundle.get(key);
    Log.d(TAG, String.format("%s %s (%s)", key,  
        value.toString(), value.getClass().getName()));
}
    if (this.settings != null) {
        this.accountnameTextView.setText(this.settings.GetAccountname());
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

  }

  // DoLogin method is defined in account_selection.xml (account_selection layout)
  public void DoLogin(View v) {
    ProgressDialog loadingDialog = ProgressDialog.show(this, "ImapNotes2" , "Logging into your account... ", true);
    this.imapNotes2Account.SetAccountname(this.accountnameTextView.getText().toString().trim());
    this.imapNotes2Account.SetUsername(this.usernameTextView.getText().toString().trim());
    this.imapNotes2Account.SetPassword(this.passwordTextView.getText().toString().trim());
    this.imapNotes2Account.SetServer(this.serverTextView.getText().toString().trim());
    this.imapNotes2Account.SetPortnum(this.portnumTextView.getText().toString());
    this.imapNotes2Account.SetSecurity(this.security);
    this.imapNotes2Account.SetUsesticky(String.valueOf(this.stickyCheckBox.isChecked()));
    
    new LoginThread().execute(this.imapFolder, this.imapNotes2Account, loadingDialog, this);
    
  }
  
  class LoginThread extends AsyncTask<Object, Void, Boolean> {
    
      private AccontConfigurationActivity accontConfigurationActivity;
      private ImapNotes2Result res = new ImapNotes2Result();

      protected Boolean doInBackground(Object... stuffs) {
      try {
        this.res=((Imaper)stuffs[0]).ConnectToProvider(
            ((ImapNotes2Account)stuffs[1]).GetUsername(),
            ((ImapNotes2Account)stuffs[1]).GetPassword(),
            ((ImapNotes2Account)stuffs[1]).GetServer(),
            ((ImapNotes2Account)stuffs[1]).GetPortnum(),
            ((ImapNotes2Account)stuffs[1]).GetSecurity(),
            ((ImapNotes2Account)stuffs[1]).GetUsesticky());
        accontConfigurationActivity = (AccontConfigurationActivity)stuffs[3];
        if (this.res.returnCode==0) {
          accontConfigurationActivity.setResult(AccontConfigurationActivity.TO_REFRESH);
          Bundle result = null;
          Account account = new Account(((ImapNotes2Account)stuffs[1]).GetAccountname(), "com.Pau.ImapNotes2");
          long SYNC_FREQUENCY = Long.parseLong(syncintervalTextView.getText().toString(), 10) * 60;
          AccountManager am = AccountManager.get(((AccontConfigurationActivity)stuffs[3]));
          if (am.addAccountExplicitly(account, ((ImapNotes2Account)stuffs[1]).GetPassword(), null)) {
              result = new Bundle();
              result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
              result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
              setAccountAuthenticatorResult(result);
              am.setUserData(account, "username", ((ImapNotes2Account)stuffs[1]).GetUsername());
              am.setUserData(account, "server", ((ImapNotes2Account)stuffs[1]).GetServer());
              am.setUserData(account, "portnum", ((ImapNotes2Account)stuffs[1]).GetPortnum());
              am.setUserData(account, "syncinterval", ((ImapNotes2Account)stuffs[1]).GetSyncinterval());
              am.setUserData(account, "security", ((ImapNotes2Account)stuffs[1]).GetSecurity());
              am.setUserData(account, "usesticky", ((ImapNotes2Account)stuffs[1]).GetUsesticky());
              // Run the Sync Adapter Periodically
              ContentResolver.setIsSyncable(account, AUTHORITY, 1);
              ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
              ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
              return true;
          } else {
              this.res.errorMessage = "Account already exists or is null";
              return false;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        ((ProgressDialog)stuffs[2]).dismiss();
      }
      return false;
    }
    
    protected void onPostExecute(Boolean result){
        if(result){
            Toast.makeText(getApplicationContext(), "Account has been added",
                Toast.LENGTH_LONG).show();
        accontConfigurationActivity.settings.Clear();
        this.accontConfigurationActivity.accountnameTextView.setText("");
        this.accontConfigurationActivity.usernameTextView.setText("");
        this.accontConfigurationActivity.passwordTextView.setText("");
        this.accontConfigurationActivity.serverTextView.setText("");
        this.accontConfigurationActivity.portnumTextView.setText("");
        this.accontConfigurationActivity.syncintervalTextView.setText("15");
        this.accontConfigurationActivity.securitySpinner.setSelection(0);
        this.accontConfigurationActivity.stickyCheckBox.setChecked(false);
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
    if ((position == 0) || (position == 3) || (position == 4))
        this.portnumTextView.setText("143");
    if ((position == 1) || (position == 2))
        this.portnumTextView.setText("993");
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // TODO Auto-generated method stub  
  }

}
