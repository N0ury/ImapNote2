package com.Pau.ImapNotes2;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
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

import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Sync.Security;

import java.util.List;

import static com.Pau.ImapNotes2.Miscs.Imaper.ResultCodeSuccess;

public class AccountConfigurationActivity extends AccountAuthenticatorActivity implements OnItemSelectedListener {
    public static final int TO_REFRESH = 999;
    public static final String AUTHORITY = "com.Pau.ImapNotes2.provider";
    private static final String TAG = "AccountConfigurationActivity";

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
    private Security security;
    //private int security_i;
    private String action;
    private String accountname;
    private ConfigurationFile settings;
    private static Account myAccount = null;
    private static AccountManager accountManager;

    private OnClickListener clickListenerLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Click on Login Button
            if (accountnameTextView.getText().toString().contains("'")) {
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
            if (accountnameTextView.getText().toString().contains("'")) {
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
        this.accountnameTextView = (TextView) (findViewById(R.id.accountnameEdit));
        this.usernameTextView = (TextView) findViewById(R.id.usernameEdit);
        this.passwordTextView = (TextView) findViewById(R.id.passwordEdit);
        this.serverTextView = (TextView) findViewById(R.id.serverEdit);
        this.portnumTextView = (TextView) findViewById(R.id.portnumEdit);
        this.syncintervalTextView = (TextView) findViewById(R.id.syncintervalEdit);
        this.folderTextView = (TextView) findViewById(R.id.folderEdit);
        this.stickyCheckBox = (CheckBox) findViewById(R.id.stickyCheckBox);

        securitySpinner = (Spinner) findViewById(R.id.securitySpinner);
        /*List<String> list = new ArrayList<String>();
        list.add("None");
        list.add("SSL/TLS");
        list.add("SSL/TLS (accept all certificates)");
        list.add("STARTTLS");
        list.add("STARTTLS (accept all certificates)");
        */
        List<String> list = Security.Printables();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        securitySpinner.setAdapter(dataAdapter);
        // Spinner item selection Listener
        securitySpinner.setOnItemSelectedListener(this);

        imapNotes2Account = new ImapNotes2Account();
        this.imapFolder = ((ImapNotes2) getApplicationContext()).GetImaper();
        this.settings = new ConfigurationFile();

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
            // Can never be null. if (this.security == null) this.security = "0";
            int security_i = this.security.ordinal();
            this.securitySpinner.setSelection(security_i);
            this.stickyCheckBox.setChecked(this.settings.GetUsesticky());
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

        if ((this.action == null) || (myAccount == null)) {
            this.action = "CREATE_ACCOUNT";
        }

        if (this.action.equals("EDIT_ACCOUNT")) {
            // Here we have to edit an existing account
            this.accountnameTextView.setText(this.accountname);
            this.usernameTextView.setText(accountManager.getUserData(myAccount, "username"));
            this.passwordTextView.setText(accountManager.getPassword(myAccount));
            this.serverTextView.setText(accountManager.getUserData(myAccount, "server"));
            this.portnumTextView.setText(accountManager.getUserData(myAccount, "portnum"));
            this.security = Security.from(accountManager.getUserData(myAccount, "security"));
            this.stickyCheckBox.setChecked(Boolean.parseBoolean(accountManager.getUserData(myAccount, "usesticky")));
            this.syncintervalTextView.setText(accountManager.getUserData(myAccount, "syncinterval"));
            this.folderTextView.setText(accountManager.getUserData(myAccount, "imapfolder"));
            //if (this.security == null) this.security = "0";
            int security_i = security.ordinal();
            this.securitySpinner.setSelection(security_i);
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
        ProgressDialog loadingDialog = ProgressDialog.show(this, "ImapNotes2", "Logging into your account... ", true);
        this.imapNotes2Account.SetAccountname(this.accountnameTextView.getText().toString().trim());
        this.imapNotes2Account.SetUsername(this.usernameTextView.getText().toString().trim());
        this.imapNotes2Account.SetPassword(this.passwordTextView.getText().toString().trim());
        this.imapNotes2Account.SetServer(this.serverTextView.getText().toString().trim());
        this.imapNotes2Account.SetPortnum(this.portnumTextView.getText().toString());
        this.imapNotes2Account.SetSecurity(this.security);
        this.imapNotes2Account.SetUsesticky(this.stickyCheckBox.isChecked());
        this.imapNotes2Account.SetSyncinterval(this.syncintervalTextView.getText().toString());
        this.imapNotes2Account.SetFoldername(this.folderTextView.getText().toString());
        long SYNC_FREQUENCY = Long.parseLong(syncintervalTextView.getText().toString(), 10) * 60;
        new LoginThread(
                this.imapFolder,
                this.imapNotes2Account,
                loadingDialog,
                this,
                this.action,
                SYNC_FREQUENCY).execute();
    }

    class LoginThread extends AsyncTask<Void, Void, Boolean> {
        /*
                private final int ParamImapFolder = 0;
                private final int ParamImapNotes2Account = 1;
                private final int ParamImapLoadingDialog = 2;
                private final int ParamAccountConfigurationActivity = 3;
                private final int ParamAction = 4;
                private final int ParamSyncPeriod = 5;
        */
        private final ImapNotes2Account imapNotes2Account;
        private final ProgressDialog progressDialog;
        private final long SYNC_FREQUENCY;

        private AccountConfigurationActivity accountConfigurationActivity;
        private ImapNotes2Result res = new ImapNotes2Result();
        String action;

        public LoginThread(Imaper mapFolder,
                           ImapNotes2Account imapNotes2Account,
                           ProgressDialog loadingDialog,
                           AccountConfigurationActivity accountConfigurationActivity,
                           String action,
                           long SYNC_FREQUENCY) {
            this.imapNotes2Account = imapNotes2Account;
            this.progressDialog = loadingDialog;
            this.accountConfigurationActivity = accountConfigurationActivity;
            this.action = action;
            this.SYNC_FREQUENCY = SYNC_FREQUENCY;

        }

        protected Boolean doInBackground(Void... none) {
            //this.action = (String) stuffs[ParamAction];
            try {
                //ImapNotes2Account imapNotes2Account= ((ImapNotes2Account) stuffs[ParamImapNotes2Account]);
                this.res = imapFolder.ConnectToProvider(
                        imapNotes2Account.GetUsername(),
                        imapNotes2Account.GetPassword(),
                        imapNotes2Account.GetServer(),
                        imapNotes2Account.GetPortnum(),
                        imapNotes2Account.GetSecurity(),
                        imapNotes2Account.GetUsesticky(),
                        imapNotes2Account.GetFoldername());
                //accountConfigurationActivity = acountConfigurationActivity;
                if (this.res.returnCode == ResultCodeSuccess) {
                    Account account = new Account(imapNotes2Account.GetAccountname(), "com.Pau.ImapNotes2");
                    //long SYNC_FREQUENCY = (long) stuffs[ParamSyncPeriod];
                    AccountManager am = AccountManager.get(accountConfigurationActivity);
                    accountConfigurationActivity.setResult(AccountConfigurationActivity.TO_REFRESH);
                    Bundle result;
                    if (this.action.equals("EDIT_ACCOUNT")) {
                        result = new Bundle();
                        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                        setAccountAuthenticatorResult(result);
                        am.setUserData(account, "username", imapNotes2Account.GetUsername());
                        am.setUserData(account, "server", imapNotes2Account.GetServer());
                        am.setUserData(account, "portnum", imapNotes2Account.GetPortnum());
                        am.setUserData(account, "syncinterval", imapNotes2Account.GetSyncinterval());
                        am.setUserData(account, "security", imapNotes2Account.GetSecurity().name());
                        am.setUserData(account, "usesticky", String.valueOf(imapNotes2Account.GetUsesticky()));
                        am.setUserData(account, "imapfolder", imapNotes2Account.GetFoldername());
                        // Run the Sync Adapter Periodically
                        ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                        ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
                        this.res.errorMessage = "Account has been modified";
                        return true;
                    } else {
                        if (am.addAccountExplicitly(account, imapNotes2Account.GetPassword(), null)) {
                            result = new Bundle();
                            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                            setAccountAuthenticatorResult(result);
                            am.setUserData(account, "username", imapNotes2Account.GetUsername());
                            am.setUserData(account, "server", imapNotes2Account.GetServer());
                            am.setUserData(account, "portnum", imapNotes2Account.GetPortnum());
                            am.setUserData(account, "syncinterval", imapNotes2Account.GetSyncinterval());
                            am.setUserData(account, "security", imapNotes2Account.GetSecurity().name());
                            am.setUserData(account, "usesticky", String.valueOf(imapNotes2Account.GetUsesticky()));
                            am.setUserData(account, "imapfolder", imapNotes2Account.GetFoldername());
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
                progressDialog.dismiss();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                accountConfigurationActivity.settings.Clear();
                this.accountConfigurationActivity.accountnameTextView.setText("");
                this.accountConfigurationActivity.usernameTextView.setText("");
                this.accountConfigurationActivity.passwordTextView.setText("");
                this.accountConfigurationActivity.serverTextView.setText("");
                this.accountConfigurationActivity.portnumTextView.setText("");
                this.accountConfigurationActivity.syncintervalTextView.setText("15");
                this.accountConfigurationActivity.securitySpinner.setSelection(0);
                this.accountConfigurationActivity.folderTextView.setText("");
                this.accountConfigurationActivity.stickyCheckBox.setChecked(false);
            }
            final Toast tag = Toast.makeText(getApplicationContext(), this.res.errorMessage, Toast.LENGTH_LONG);
            tag.show();
            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tag.show();
                }

                public void onFinish() {
                    tag.show();
                }
            }.start();
            if (this.action.equals("EDIT_ACCOUNT")) finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        security = Security.from(position);
        this.portnumTextView.setText(security.defaultPort);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

}
