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
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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
  private TextView folderTextView;
  private CheckBox stickyCheckBox;
  private Spinner securitySpinner;
  private ImapNotes2Account imapNotes2Account;
  private String security;
  private int security_i;
  private String action;
  private String accountname;
  private ConfigurationFile settings;
  private static Account myAccount = null;
  private static AccountManager accountManager;

  private OnClickListener clickListenerLogin = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          // Click on Login Button
    	  if (((String) accountnameTextView.getText().toString()).contains("'")) {
    	      // Single quotation marks are not allowed in accountname
              Toast.makeText(getApplicationContext(), "Quotation marks are not allowed in accountname",
                      Toast.LENGTH_LONG).show();
    	  } else {
              DoLogin(v);
    	  }
      }
  };

  private OnClickListener clickListenerEdit = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          // Click on Edit Button
    	  if (((String) accountnameTextView.getText().toString()).contains("'")) {
    	      // Single quotation marks are not allowed in accountname
              Toast.makeText(getApplicationContext(), "Quotation marks are not allowed in accountname",
                      Toast.LENGTH_LONG).show();
    	  } else {
              DoLogin(v);
    	  }
      }
  };

  private OnClickListener clickListenerRemove = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          // Clic on Remove Button
          accountManager.removeAccount(myAccount, null, null);
          Toast.makeText(getApplicationContext(), "Account has been removed",
                Toast.LENGTH_LONG).show();
          finish();//finishing activity
      }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.account_selection);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    this.accountnameTextView = (TextView)(findViewById(R.id.accountnameEdit));
    this.usernameTextView = (TextView)findViewById(R.id.usernameEdit);
    this.passwordTextView = (TextView)findViewById(R.id.passwordEdit);
    this.serverTextView = (TextView)findViewById(R.id.serverEdit);
    this.portnumTextView = (TextView)findViewById(R.id.portnumEdit);
    this.syncintervalTextView = (TextView)findViewById(R.id.syncintervalEdit);
    this.folderTextView = (TextView)findViewById(R.id.folderEdit);
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

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
        if (extras.containsKey("action")) {
            action = extras.getString("action");
        }
        if (extras.containsKey("accountname")) {
            accountname = extras.getString("accountname");
        }
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
        this.syncintervalTextView.setText("15");
        this.folderTextView.setText(this.settings.GetFoldername());
    }

    LinearLayout layout = (LinearLayout) findViewById(R.id.bttonsLayout);
    accountManager = AccountManager.get(getApplicationContext());
    Account[] accounts = accountManager.getAccountsByType("com.Pau.ImapNotes2");
    for (Account account : accounts) {
        if (account.name.equals(accountname)) {
            myAccount = account;
            break;
        }
    }

    if ((this.action == null) || (this.myAccount == null)) { this.action = "CREATE_ACCOUNT"; }

    if (this.action.equals("EDIT_ACCOUNT")) {
        // Here we have to edit an existing account
        this.accountnameTextView.setText(this.accountname);
       	this.usernameTextView.setText(this.accountManager.getUserData (myAccount, "username"));
       	this.passwordTextView.setText(this.accountManager.getPassword(myAccount));
        this.serverTextView.setText(this.accountManager.getUserData(myAccount, "server"));
        this.portnumTextView.setText(this.accountManager.getUserData(myAccount, "portnum"));
        this.security = this.accountManager.getUserData (myAccount, "security");
        this.stickyCheckBox.setChecked(Boolean.parseBoolean(this.accountManager.getUserData(myAccount,"usesticky")));
        this.syncintervalTextView.setText(this.accountManager.getUserData(myAccount, "syncinterval"));
        this.folderTextView.setText(this.accountManager.getUserData (myAccount, "imapfolder"));
        if (this.security == null) this.security = "0";
        this.security_i = Integer.parseInt(this.security);
        this.securitySpinner.setSelection(this.security_i);
        Button buttonEdit = new Button(this);
        buttonEdit.setText("Save");
        buttonEdit.setOnClickListener(clickListenerEdit);
        layout.addView(buttonEdit);
        Button buttonRemove = new Button(this);
        buttonRemove.setText("Remove");
        buttonRemove.setOnClickListener(clickListenerRemove);
        layout.addView(buttonRemove);
    } else {
        // Here we have to create a new account
        Button buttonView = new Button(this);
        buttonView.setText("Check & Create Account");
        buttonView.setOnClickListener(clickListenerLogin);
        layout.addView(buttonView);
    }

        // Don't display keyboard when on note detail, only if user touches the screen
    getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
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
    this.imapNotes2Account.SetSyncinterval(this.syncintervalTextView.getText().toString());
    this.imapNotes2Account.SetFoldername(this.folderTextView.getText().toString());
    long SYNC_FREQUENCY = Long.parseLong(syncintervalTextView.getText().toString(), 10) * 60;
    new LoginThread().execute(this.imapFolder, this.imapNotes2Account, loadingDialog, this, this.action, SYNC_FREQUENCY);
    
  }
  
  class LoginThread extends AsyncTask<Object, Void, Boolean> {
    
      private AccontConfigurationActivity accontConfigurationActivity;
      private ImapNotes2Result res = new ImapNotes2Result();
      String action;

      protected Boolean doInBackground(Object... stuffs) {
        this.action = (String)stuffs[4];
        try {
          this.res=((Imaper)stuffs[0]).ConnectToProvider(
            ((ImapNotes2Account)stuffs[1]).GetUsername(),
            ((ImapNotes2Account)stuffs[1]).GetPassword(),
            ((ImapNotes2Account)stuffs[1]).GetServer(),
            ((ImapNotes2Account)stuffs[1]).GetPortnum(),
            ((ImapNotes2Account)stuffs[1]).GetSecurity(),
            ((ImapNotes2Account)stuffs[1]).GetUsesticky(),
            ((ImapNotes2Account)stuffs[1]).GetFoldername());
          accontConfigurationActivity = (AccontConfigurationActivity)stuffs[3];
          if (this.res.returnCode==0) {
            Account account = new Account(((ImapNotes2Account)stuffs[1]).GetAccountname(), "com.Pau.ImapNotes2");
            long SYNC_FREQUENCY = (long)stuffs[5];
            AccountManager am = AccountManager.get(((AccontConfigurationActivity)stuffs[3]));
            accontConfigurationActivity.setResult(AccontConfigurationActivity.TO_REFRESH);
            Bundle result = null;
            if (this.action.equals("EDIT_ACCOUNT")) {
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
                  am.setUserData(account, "imapfolder", ((ImapNotes2Account)stuffs[1]).GetFoldername());
                  // Run the Sync Adapter Periodically
                  ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                  ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                  ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
                  this.res.errorMessage = "Account has been modified";
                  return true;
            } else {
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
                  am.setUserData(account, "imapfolder", ((ImapNotes2Account)stuffs[1]).GetFoldername());
                  // Run the Sync Adapter Periodically
                  ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                  ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                  ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
                  this.res.errorMessage = "Account has been added";
                  return true;
              } else {
                  this.res.errorMessage = "Account already exists or is null";
                  return false;
              }
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
          accontConfigurationActivity.settings.Clear();
          this.accontConfigurationActivity.accountnameTextView.setText("");
          this.accontConfigurationActivity.usernameTextView.setText("");
          this.accontConfigurationActivity.passwordTextView.setText("");
          this.accontConfigurationActivity.serverTextView.setText("");
          this.accontConfigurationActivity.portnumTextView.setText("");
          this.accontConfigurationActivity.syncintervalTextView.setText("15");
          this.accontConfigurationActivity.securitySpinner.setSelection(0);
          this.accontConfigurationActivity.folderTextView.setText("");
          this.accontConfigurationActivity.stickyCheckBox.setChecked(false);
        }
    	final Toast tag = Toast.makeText(getApplicationContext(), this.res.errorMessage,Toast.LENGTH_LONG);
        tag.show();
        new CountDownTimer(5000, 1000) {
          public void onTick(long millisUntilFinished) {tag.show();}
          public void onFinish() {tag.show();}
        }.start();
        if (this.action.equals("EDIT_ACCOUNT")) finish();
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
