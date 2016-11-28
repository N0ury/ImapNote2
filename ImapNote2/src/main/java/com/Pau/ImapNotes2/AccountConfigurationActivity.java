package com.Pau.ImapNotes2;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.Pau.ImapNotes2.Data.ConfigurationFieldNames;
import com.Pau.ImapNotes2.Data.ConfigurationFile;
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Data.Security;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;
import com.Pau.ImapNotes2.Miscs.Imaper;

import java.util.List;

public class AccountConfigurationActivity extends AccountAuthenticatorActivity implements OnItemSelectedListener {
    private static final int TO_REFRESH = 999;
    private static final String AUTHORITY = "com.Pau.ImapNotes2.provider";
    private static final String TAG = "IN_AccountConfActivity";

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
    // TODO: move this to dologin becauae it is the only use.
    private ImapNotes2Account imapNotes2Account;
    @NonNull
    private Security security = Security.None;
    //private int security_i;
    @Nullable
    private Actions action;
    @Nullable
    private String accountname;
    @NonNull
    private final ConfigurationFile settings = new ConfigurationFile();

    @Nullable
    private static Account myAccount = null;
    private static AccountManager accountManager;

    //region Intent item names and values.
    public static final String ACTION = "ACTION";
    public static final String ACCOUNTNAME = "ACCOUNTNAME";
//    public static final String EDIT_ACCOUNT = "EDIT_ACCOUNT";
//    public static final String CREATE_ACCOUNT = "CREATE_ACCOUNT";
    //endregion


    enum Actions {
        CREATE_ACCOUNT,
        EDIT_ACCOUNT
    }

    private final OnClickListener clickListenerLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Click on Login Button
            CheckNameAndLogIn();
        }
    };

    private final OnClickListener clickListenerEdit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Click on Edit Button
            CheckNameAndLogIn();
        }
    };

    private void CheckNameAndLogIn() {
        if (accountnameTextView.getText().toString().contains("'")) {
            // Single quotation marks are not allowed in accountname
            Toast.makeText(getApplicationContext(), R.string.quotation_marks_not_allowed,
                    Toast.LENGTH_LONG).show();
        } else {
            DoLogin();
        }
    }

    private final OnClickListener clickListenerRemove = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Clic on Remove Button
            accountManager.removeAccount(myAccount, null, null);
            Toast.makeText(getApplicationContext(), R.string.account_removed,
                    Toast.LENGTH_LONG).show();
            finish();//finishing activity
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_selection);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        accountnameTextView = (TextView) (findViewById(R.id.accountnameEdit));
        usernameTextView = (TextView) findViewById(R.id.usernameEdit);
        passwordTextView = (TextView) findViewById(R.id.passwordEdit);
        serverTextView = (TextView) findViewById(R.id.serverEdit);
        portnumTextView = (TextView) findViewById(R.id.portnumEdit);
        syncintervalTextView = (TextView) findViewById(R.id.syncintervalEdit);
        folderTextView = (TextView) findViewById(R.id.folderEdit);
        stickyCheckBox = (CheckBox) findViewById(R.id.stickyCheckBox);

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
        imapFolder = ((ImapNotes2k) getApplicationContext()).GetImaper();
        //settings = new ConfigurationFile();

        Bundle extras = getIntent().getExtras();
        // TODO: find out if extras can be null.
        if (extras != null) {
            if (extras.containsKey(ACTION)) {
                action = (Actions) (extras.getSerializable(ACTION));
            }
            if (extras.containsKey(ACCOUNTNAME)) {
                accountname = extras.getString(ACCOUNTNAME);
            }
        }

        // Settings can never be null so there is no need to guard it
        //if (settings != null) {
        accountnameTextView.setText(settings.GetAccountname());
        usernameTextView.setText(settings.GetUsername());
        passwordTextView.setText(settings.GetPassword());
        serverTextView.setText(settings.GetServer());
        portnumTextView.setText(settings.GetPortnum());
        security = settings.GetSecurity();
        // Can never be null. if (security == null) security = "0";
        //int security_i = security.ordinal();
        securitySpinner.setSelection(security.ordinal());
        stickyCheckBox.setChecked(settings.GetUsesticky());
        syncintervalTextView.setText(R.string.default_sync_interval);
        folderTextView.setText(settings.GetFoldername());
        //}

        LinearLayout layout = (LinearLayout) findViewById(R.id.bttonsLayout);
        accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.Pau.ImapNotes2");
        for (Account account : accounts) {
            if (account.name.equals(accountname)) {
                myAccount = account;
                break;
            }
        }

        // action can never be null
        if (myAccount == null) {
            action = Actions.CREATE_ACCOUNT;
        }

        if (action == Actions.EDIT_ACCOUNT) {
            // Here we have to edit an existing account
            accountnameTextView.setText(accountname);
            usernameTextView.setText(GetConfigValue(ConfigurationFieldNames.UserName));
            passwordTextView.setText(accountManager.getPassword(myAccount));
            serverTextView.setText(GetConfigValue(ConfigurationFieldNames.Server));
            portnumTextView.setText(GetConfigValue(ConfigurationFieldNames.PortNumber));
            Log.d(TAG, "Security: " + GetConfigValue(ConfigurationFieldNames.Security));
            security = Security.from(GetConfigValue(ConfigurationFieldNames.Security));
            stickyCheckBox.setChecked(Boolean.parseBoolean(GetConfigValue(ConfigurationFieldNames.UseSticky)));
            syncintervalTextView.setText(GetConfigValue(ConfigurationFieldNames.SyncInterval));
            folderTextView.setText(GetConfigValue(ConfigurationFieldNames.ImapFolder));
            //if (security == null) security = "0";
            //security_i = security.ordinal();
            securitySpinner.setSelection(security.ordinal());
            Button buttonEdit = new Button(this);
            buttonEdit.setText(R.string.save);
            buttonEdit.setOnClickListener(clickListenerEdit);
            layout.addView(buttonEdit);
            Button buttonRemove = new Button(this);
            buttonRemove.setText(R.string.remove);
            buttonRemove.setOnClickListener(clickListenerRemove);
            layout.addView(buttonRemove);
        } else {
            // Here we have to create a new account
            Button buttonView = new Button(this);
            buttonView.setText(R.string.check_and_create_account);
            buttonView.setOnClickListener(clickListenerLogin);
            layout.addView(buttonView);
        }

        // Don't display keyboard when on note detail, only if user touches the screen
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private String GetConfigValue(@NonNull String name) {
        return accountManager.getUserData(myAccount, name);
    }

    private String GetTextViewText(@NonNull TextView textView) {
        return textView.getText().toString().trim();
    }

    // DoLogin method is defined in account_selection.xml (account_selection layout)
    private void DoLogin() {
        ProgressDialog loadingDialog = ProgressDialog.show(this, getString(R.string.app_name),
                getString(R.string.logging_in), true);
        imapNotes2Account.SetAccountname(GetTextViewText(accountnameTextView));
        imapNotes2Account.SetUsername(GetTextViewText(usernameTextView));
        imapNotes2Account.SetPassword(GetTextViewText(passwordTextView));
        imapNotes2Account.SetServer(GetTextViewText(serverTextView));
        imapNotes2Account.SetPortnum(GetTextViewText(portnumTextView));
        imapNotes2Account.SetSecurity(security);
        imapNotes2Account.SetUsesticky(stickyCheckBox.isChecked());
        imapNotes2Account.SetSyncinterval(GetTextViewText(syncintervalTextView));
        imapNotes2Account.SetFoldername(GetTextViewText(folderTextView));
        // No need to check for valid numbers because the field only allows digits.  But it is
        // possible to remove all characters which causes the program to crash.  The easiest fix is
        // to add a zero at the beginning so that we are guaranteed to be able to parse it but that
        // leaves us with a zero sync. interval.
        long SYNC_FREQUENCY = Long.parseLong(GetTextViewText(syncintervalTextView), 10) * 60;
        new LoginThread(
                imapNotes2Account,
                loadingDialog,
                this,
                action,
                SYNC_FREQUENCY).execute();
    }

    class LoginThread extends AsyncTask<Void, Void, Boolean> {

        private final ImapNotes2Account imapNotes2Account;
        private final ProgressDialog progressDialog;
        private final long SYNC_FREQUENCY;

        private final AccountConfigurationActivity accountConfigurationActivity;
        //@NonNull
        //private ImapNotes2Result res = new ImapNotes2Result();
        private String statusMessage = "";
        private final Actions action;

        LoginThread(ImapNotes2Account imapNotes2Account,
                    ProgressDialog loadingDialog,
                    AccountConfigurationActivity accountConfigurationActivity,
                    Actions action,
                    long SYNC_FREQUENCY) {
            this.imapNotes2Account = imapNotes2Account;
            this.progressDialog = loadingDialog;
            this.accountConfigurationActivity = accountConfigurationActivity;
            this.action = action;
            this.SYNC_FREQUENCY = SYNC_FREQUENCY;

        }

        @NonNull
        protected Boolean doInBackground(Void... none) {
            //action = (String) stuffs[ParamAction];
            try {
                //ImapNotes2Account imapNotes2Account= ((ImapNotes2Account) stuffs[ParamImapNotes2Account]);
                ImapNotes2Result res = imapFolder.ConnectToProvider(
                        imapNotes2Account.GetUsername(),
                        imapNotes2Account.GetPassword(),
                        imapNotes2Account.GetServer(),
                        imapNotes2Account.GetPortnum(),
                        imapNotes2Account.GetSecurity()
                );
                //accountConfigurationActivity = acountConfigurationActivity;
                if (res.returnCode == Imaper.ResultCodeSuccess) {
                    // TODO: Find out if "com.Pau.ImapNotes2" is the same as getApplicationContext().getPackageName().
                    Account account = new Account(imapNotes2Account.GetAccountname(), "com.Pau.ImapNotes2");
                    //long SYNC_FREQUENCY = (long) stuffs[ParamSyncPeriod];
                    AccountManager am = AccountManager.get(accountConfigurationActivity);
                    accountConfigurationActivity.setResult(AccountConfigurationActivity.TO_REFRESH);
                    //Bundle result;
                    if (action == Actions.EDIT_ACCOUNT) {
                        Bundle result = new Bundle();
                        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                        setAccountAuthenticatorResult(result);
                        setUserData(am, account);
                        /*am.setUserData(account, ConfigurationFieldNames.UserName, imapNotes2Account.GetUsername());
                        am.setUserData(account, ConfigurationFieldNames.Server, imapNotes2Account.GetServer());
                        am.setUserData(account, ConfigurationFieldNames.PortNumber, imapNotes2Account.GetPortnum());
                        am.setUserData(account, ConfigurationFieldNames.SyncInterval, imapNotes2Account.GetSyncinterval());
                        am.setUserData(account, ConfigurationFieldNames.Security, imapNotes2Account.GetSecurity().name());
                        am.setUserData(account, ConfigurationFieldNames.UseSticky, String.valueOf(imapNotes2Account.GetUsesticky()));
                        am.setUserData(account, ConfigurationFieldNames.ImapFolder, imapNotes2Account.GetFoldername());
                        */
                        // Run the Sync Adapter Periodically
                        ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                        ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
                        statusMessage = "Account has been modified";
                        return true;
                    } else {
                        if (am.addAccountExplicitly(account, imapNotes2Account.GetPassword(), null)) {
                            // TODO: make function for these repeated lines.
                            Bundle result = new Bundle();
                            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                            setAccountAuthenticatorResult(result);
                            setUserData(am, account);
                            /*am.setUserData(account, ConfigurationFieldNames.UserName, imapNotes2Account.GetUsername());
                            am.setUserData(account, ConfigurationFieldNames.Server, imapNotes2Account.GetServer());
                            am.setUserData(account, ConfigurationFieldNames.PortNumber, imapNotes2Account.GetPortnum());
                            am.setUserData(account, ConfigurationFieldNames.SyncInterval, imapNotes2Account.GetSyncinterval());
                            am.setUserData(account, ConfigurationFieldNames.Security, imapNotes2Account.GetSecurity().name());
                            am.setUserData(account, ConfigurationFieldNames.UseSticky, String.valueOf(imapNotes2Account.GetUsesticky()));
                            am.setUserData(account, ConfigurationFieldNames.ImapFolder, imapNotes2Account.GetFoldername());
                            */
                            // Run the Sync Adapter Periodically
                            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                            ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);
                            statusMessage = getString(R.string.account_added);
                            return true;
                        } else {
                            statusMessage = getString(R.string.account_already_exists_or_is_null);
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

        private void setUserData(AccountManager am,
                                 Account account) {
            am.setUserData(account, ConfigurationFieldNames.UserName, imapNotes2Account.GetUsername());
            am.setUserData(account, ConfigurationFieldNames.Server, imapNotes2Account.GetServer());
            am.setUserData(account, ConfigurationFieldNames.PortNumber, imapNotes2Account.GetPortnum());
            am.setUserData(account, ConfigurationFieldNames.SyncInterval, imapNotes2Account.GetSyncinterval());
            am.setUserData(account, ConfigurationFieldNames.Security, imapNotes2Account.GetSecurity().name());
            am.setUserData(account, ConfigurationFieldNames.UseSticky, String.valueOf(imapNotes2Account.GetUsesticky()));
            am.setUserData(account, ConfigurationFieldNames.ImapFolder, imapNotes2Account.GetFoldername());
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                accountConfigurationActivity.settings.Clear();
                accountConfigurationActivity.accountnameTextView.setText("");
                accountConfigurationActivity.usernameTextView.setText("");
                accountConfigurationActivity.passwordTextView.setText("");
                accountConfigurationActivity.serverTextView.setText("");
                accountConfigurationActivity.portnumTextView.setText("");
                accountConfigurationActivity.syncintervalTextView.setText(R.string.default_sync_interval);
                accountConfigurationActivity.securitySpinner.setSelection(0);
                accountConfigurationActivity.folderTextView.setText("");
                accountConfigurationActivity.stickyCheckBox.setChecked(false);
            }
            final Toast tag = Toast.makeText(getApplicationContext(), statusMessage, Toast.LENGTH_LONG);
            tag.show();
            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tag.show();
                }

                public void onFinish() {
                    tag.show();
                }
            }.start();
            if (action == Actions.EDIT_ACCOUNT) {
                finish();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
        portnumTextView.setText(security.defaultPort);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

}
