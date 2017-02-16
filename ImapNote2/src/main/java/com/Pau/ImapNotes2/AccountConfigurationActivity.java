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
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Data.Security;
import com.Pau.ImapNotes2.Miscs.ImapNotes2Result;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.Result;

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
    private CheckBox automaticMergeCheckBox;
    private Spinner securitySpinner;
    @NonNull
    private Security security = Security.None;
    //private int security_i;
    @Nullable
    private Actions action;
    @Nullable
    private String accountname;
    @Nullable
    private static Account myAccount = null;

    private static AccountManager accountManager;

    /**
     * Cannot be final or NonNull because it needs the application context which is not available
     * until onCreate.
     */
    //private ConfigurationFile settings;

    //region Intent item names and values.
    public static final String ACTION = "ACTION";
    public static final String ACCOUNTNAME = "ACCOUNTNAME";
//    public static final String EDIT_ACCOUNT = "EDIT_ACCOUNT";
//    public static final String CREATE_ACCOUNT = "CREATE_ACCOUNT";
    //endregion


    /**
     *
     */
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

    private void Toast(int message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_LONG).show();

    }

    private void CheckNameAndLogIn() {
        if (accountnameTextView.getText().toString().contains("'")) {
            // Single quotation marks are not allowed in accountname
            //Toast.makeText(getApplicationContext(), R.string.quotation_marks_not_allowed,
            // Toast.LENGTH_LONG).show();
            Toast(R.string.quotation_marks_not_allowed);
        } else {
            DoLogin();
        }
    }

    private final OnClickListener clickListenerRemove = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Click on Remove Button
            accountManager.removeAccount(myAccount, null, null);
//            Toast.makeText(getApplicationContext(), R.string.account_removed,
            //                  Toast.LENGTH_LONG).show();
            Toast(R.string.account_removed);
            finish();//finishing activity
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //settings = new ConfigurationFile(getApplicationContext());
        setContentView(R.layout.account_selection);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);
        TextView headingTextView = (TextView) (findViewById(R.id.heading));
        accountnameTextView = (TextView) (findViewById(R.id.accountnameEdit));
        usernameTextView = (TextView) findViewById(R.id.usernameEdit);
        passwordTextView = (TextView) findViewById(R.id.passwordEdit);
        serverTextView = (TextView) findViewById(R.id.serverEdit);
        portnumTextView = (TextView) findViewById(R.id.portnumEdit);
        syncintervalTextView = (TextView) findViewById(R.id.syncintervalEdit);
        folderTextView = (TextView) findViewById(R.id.folderEdit);
        stickyCheckBox = (CheckBox) findViewById(R.id.stickyCheckBox);
        automaticMergeCheckBox = (CheckBox) findViewById(R.id.automaticMergeCheckBox);

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

        //imapNotes2Account = new ImapNotes2Account();
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
/*
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
        automaticMergeCheckBox.setChecked(settings.GetUseAutomaticMerge());
        folderTextView.setText(settings.GetFoldername());
*/
        syncintervalTextView.setText(R.string.default_sync_interval);
        //}

        LinearLayout layout = (LinearLayout) findViewById(R.id.buttonsLayout);
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
            headingTextView.setText(R.string.editAccount);
            accountnameTextView.setText(accountname);
            usernameTextView.setText(GetConfigValue(ConfigurationFieldNames.UserName));
            passwordTextView.setText(accountManager.getPassword(myAccount));
            serverTextView.setText(GetConfigValue(ConfigurationFieldNames.Server));
            portnumTextView.setText(GetConfigValue(ConfigurationFieldNames.PortNumber));
            Log.d(TAG, "Security: " + GetConfigValue(ConfigurationFieldNames.Security));
            security = Security.from(GetConfigValue(ConfigurationFieldNames.Security));
            stickyCheckBox.setChecked(Boolean.parseBoolean(GetConfigValue(ConfigurationFieldNames.UseSticky)));
            automaticMergeCheckBox.setChecked(Boolean.parseBoolean(GetConfigValue(ConfigurationFieldNames.UseAutomaticMerge)));
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
        ImapNotes2Account imapNotes2Account = new ImapNotes2Account(
                GetTextViewText(accountnameTextView),
                GetTextViewText(usernameTextView),
                GetTextViewText(passwordTextView),
                GetTextViewText(serverTextView),
                GetTextViewText(portnumTextView),
                security,
                stickyCheckBox.isChecked(),
                automaticMergeCheckBox.isChecked(),
                GetTextViewText(syncintervalTextView),
                GetTextViewText(folderTextView));
        // No need to check for valid numbers because the field only allows digits.  But it is
        // possible to remove all characters which causes the program to crash.  The easiest fix is
        // to add a zero at the beginning so that we are guaranteed to be able to parse it but that
        // leaves us with a zero sync. interval.
        Result<Integer> synchronizationInterval = GetSynchronizationInterval();
        if (synchronizationInterval.succeeded) {

            new LoginThread(
                    imapNotes2Account,
                    this,
                    action,
                    synchronizationInterval.result).execute();
        }
    }

    Result<Integer> GetSynchronizationInterval() {
        String syncInterval = GetTextViewText(syncintervalTextView).trim();
        int syncIntervalInt = 0;
        boolean status = false;
        try {
            syncIntervalInt = Integer.parseInt(GetTextViewText(syncintervalTextView), 10) * 60;
            if (syncIntervalInt <= 0) {
                Toast.makeText(this, "Synchronization interval must be greater than zero: <" + syncInterval + ">.", Toast.LENGTH_LONG).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Synchronization interval is invalid: <" + syncInterval + ">.", Toast.LENGTH_LONG).show();
        }
        return new Result(syncIntervalInt, status);
    }

    class LoginThread extends AsyncTask<Void, Void, Result<String>> {

        private final ImapNotes2Account imapNotes2Account;
        private final ProgressDialog progressDialog;
        private final int synchronizationInterval;

        private final AccountConfigurationActivity accountConfigurationActivity;

        private final Actions action;

        LoginThread(ImapNotes2Account imapNotes2Account,
                    AccountConfigurationActivity accountConfigurationActivity,
                    Actions action,
                    int synchronizationInterval) {
            this.imapNotes2Account = imapNotes2Account;
            //this.progressDialog = loadingDialog;
            this.accountConfigurationActivity = accountConfigurationActivity;
            this.action = action;
            this.synchronizationInterval = synchronizationInterval;
            this.progressDialog = ProgressDialog.show(accountConfigurationActivity,
                    getString(R.string.app_name),
                    getString(R.string.logging_in),
                    true);

        }

        /*

                class Result{
                    final String message;
                    final boolean succeeded;

                    Result(String message,
                           boolean succeeded) {
                        this.message = message;
                        this.succeeded = succeeded;
                    }
                }
        */
        @NonNull
        protected Result<String> doInBackground(Void... none) {
            try {
                ImapNotes2Result res = imapFolder.ConnectToProvider(
                        imapNotes2Account.username,
                        imapNotes2Account.password,
                        imapNotes2Account.server,
                        imapNotes2Account.portnum,
                        imapNotes2Account.security
                );
                //accountConfigurationActivity = acountConfigurationActivity;
                if (res.returnCode != Imaper.ResultCodeSuccess) {
                    return new Result("IMAP operation failed: " + res.errorMessage, false);
                }
                // TODO: Find out if "com.Pau.ImapNotes2" is the same as getApplicationContext().getPackageName().
                Account account = new Account(imapNotes2Account.GetAccountName(), "com.Pau.ImapNotes2");
                AccountManager am = AccountManager.get(accountConfigurationActivity);
                accountConfigurationActivity.setResult(AccountConfigurationActivity.TO_REFRESH);
                if (action == Actions.EDIT_ACCOUNT) {
                    Bundle result = new Bundle();
                    result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                    result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                    setAccountAuthenticatorResult(result);
                    setUserData(am, account);
                    // Run the Sync Adapter Periodically
                    ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                    ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                    ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), synchronizationInterval);
                    return new Result("Account has been modified", true);
                } else {
                    if (!am.addAccountExplicitly(account, imapNotes2Account.password, null)) {
                        return new Result(getString(R.string.account_already_exists_or_is_null), false);
                    }
                    // TODO: make function for these repeated lines.
                    Bundle result = new Bundle();
                    result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                    result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                    setAccountAuthenticatorResult(result);
                    setUserData(am, account);
                    // Run the Sync Adapter Periodically
                    ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                    ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                    ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), synchronizationInterval);
                    return new Result(getString(R.string.account_added), true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new Result("Unexpected exception: " + e.getMessage(), false);
            } finally {
                progressDialog.dismiss();
            }
        }


        private void setUserData(@NonNull AccountManager am,
                                 @NonNull Account account) {
            am.setUserData(account, ConfigurationFieldNames.UserName, imapNotes2Account.username);
            am.setUserData(account, ConfigurationFieldNames.Server, imapNotes2Account.server);
            am.setUserData(account, ConfigurationFieldNames.PortNumber, imapNotes2Account.portnum);
            am.setUserData(account, ConfigurationFieldNames.SyncInterval, imapNotes2Account.syncInterval);
            am.setUserData(account, ConfigurationFieldNames.Security, imapNotes2Account.security.name());
            am.setUserData(account, ConfigurationFieldNames.UseSticky, String.valueOf(imapNotes2Account.usesticky));
            am.setUserData(account, ConfigurationFieldNames.ImapFolder, imapNotes2Account.imapfolder);
        }

        protected void onPostExecute(@NonNull Result<String> result) {
            if (result.succeeded) {
                accountConfigurationActivity.Clear();
            }
            ShowToast(result.result, 5);
            if (action == Actions.EDIT_ACCOUNT) {
                finish();
            }
        }

        void ShowToast(String message,
                       int durationSeconds) {
            final Toast tag = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            tag.show();
            new CountDownTimer(durationSeconds * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tag.show();
                }

                public void onFinish() {
                    tag.show();
                }
            }.start();
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

    void Clear() {

        accountnameTextView.setText("");
        usernameTextView.setText("");
        passwordTextView.setText("");
        serverTextView.setText("");
        portnumTextView.setText("");
        syncintervalTextView.setText(R.string.default_sync_interval);
        securitySpinner.setSelection(0);
        folderTextView.setText("");
        stickyCheckBox.setChecked(false);
    }
}
