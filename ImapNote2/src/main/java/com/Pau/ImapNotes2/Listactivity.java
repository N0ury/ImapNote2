package com.Pau.ImapNotes2;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.PeriodicSync;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.Pau.ImapNotes2.Data.ConfigurationFieldNames;
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Data.OneNote;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.SyncThread;
import com.Pau.ImapNotes2.Miscs.UpdateThread;
import com.Pau.ImapNotes2.Sync.SyncService;
import com.Pau.ImapNotes2.Sync.SyncUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import static com.Pau.ImapNotes2.AccountConfigurationActivity.ACTION;
import static com.Pau.ImapNotes2.NoteDetailActivity.Colors;

//import com.Pau.ImapNotes2.R;
//import android.widget.SimpleAdapter;


public class Listactivity extends Activity implements OnItemSelectedListener, Filterable {
    private static final int SEE_DETAIL = 2;
    public static final int DELETE_BUTTON = 3;
    private static final int NEW_BUTTON = 4;
    private static final int SAVE_BUTTON = 5;
    private static final int EDIT_BUTTON = 6;


    //region Intent item names
    public static final String EDIT_ITEM_NUM_IMAP = "EDIT_ITEM_NUM_IMAP";
    public static final String EDIT_ITEM_TXT = "EDIT_ITEM_TXT";
    public static final String EDIT_ITEM_COLOR = "EDIT_ITEM_COLOR";
    private static final String SAVE_ITEM_COLOR = "SAVE_ITEM_COLOR";
    private static final String SAVE_ITEM = "SAVE_ITEM";
    public static final String DELETE_ITEM_NUM_IMAP = "DELETE_ITEM_NUM_IMAP";
    public static final String ACCOUNTNAME = "ACCOUNTNAME";
    public static final String SYNCINTERVAL = "SYNCINTERVAL";
    public static final String CHANGED = "CHANGED";
    public static final String SYNCED = "SYNCED";
    //endregion

    private ArrayList<OneNote> noteList;
    private NotesListAdapter listToView;
    private ArrayAdapter<String> spinnerList;

    @Nullable
    private static NotesDb storedNotes = null;
    private Spinner accountSpinner;
    public static ImapNotes2Account imapNotes2Account;
    private static AccountManager accountManager;
    // Ensure that we never have to check for null by initializing reference.
    @NonNull
    private static Account[] accounts = new Account[0];
    private static List<String> currentList;
    //@Nullable
    private TextView status;
    private static String OldStatus;
    private static final String AUTHORITY = "com.Pau.ImapNotes2.provider";
    private static final String TAG = "IN_Listactivity";


    private final OnClickListener clickListenerEditAccount = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent res = new Intent();
            String mPackage = "com.Pau.ImapNotes2";
            String mClass = ".AccountConfigurationActivity";
            res.setComponent(new ComponentName(mPackage, mPackage + mClass));
            res.putExtra(ACTION, AccountConfigurationActivity.Actions.EDIT_ACCOUNT);
            res.putExtra(AccountConfigurationActivity.ACCOUNTNAME, Listactivity.imapNotes2Account.GetAccountName());
            startActivity(res);
        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.accountSpinner = (Spinner) findViewById(R.id.accountSpinner);
        Listactivity.currentList = new ArrayList<>();

        this.accountSpinner.setOnItemSelectedListener(this);

        imapNotes2Account = new ImapNotes2Account();
        Listactivity.accountManager = AccountManager.get(getApplicationContext());
        Listactivity.accountManager.addOnAccountsUpdatedListener(
                new AccountsUpdateListener(), null, true);

        status = (TextView) findViewById(R.id.status);

        this.spinnerList = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, Listactivity.currentList);
        spinnerList.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        this.accountSpinner.setAdapter(spinnerList);

        this.noteList = new ArrayList<>();
        //((ImapNotes2k) this.getApplicationContext()).SetNotesList(this.noteList);
        this.listToView = new NotesListAdapter(
                getApplicationContext(),
                this.noteList,
                new String[]{OneNote.TITLE, OneNote.DATE},
                new int[]{R.id.noteTitle, R.id.noteInformation});
        ListView listview = (ListView) findViewById(R.id.notesList);
        listview.setAdapter(this.listToView);

        listview.setTextFilterEnabled(true);

        Imaper imapFolder = new Imaper();
        ((ImapNotes2k) this.getApplicationContext()).SetImaper(imapFolder);

        if (Listactivity.storedNotes == null)
            storedNotes = new NotesDb(getApplicationContext());

        // When item is clicked, we go to NoteDetailActivity
        listview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View widget, int selectedNote, long rowId) {
                Intent toDetail = new Intent(widget.getContext(), NoteDetailActivity.class);
                toDetail.putExtra(NoteDetailActivity.selectedNote, (OneNote) parent.getItemAtPosition(selectedNote));
                toDetail.putExtra(NoteDetailActivity.useSticky, Listactivity.imapNotes2Account.GetUsesticky());
                startActivityForResult(toDetail, SEE_DETAIL);

                TriggerSync(status);
            }
        });

        Button editAccountButton = (Button) findViewById(R.id.editAccountButton);
        editAccountButton.setOnClickListener(clickListenerEditAccount);

    }

    public void onDestroy() {
        super.onDestroy();
        // in case of debug, uncomment next instruction
        // logcat will be sent by mail
        // send mail action is done by the user, so he can refuse
        // SendLogcatMail();
//        this.imapFolder.SetPrefs();
    }

    public void onStart() {
        super.onStart();
        int len = accounts.length;
        if (len > 0) updateAccountSpinner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(syncFinishedReceiver, new IntentFilter(SyncService.SYNC_FINISHED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncFinishedReceiver);
    }


    @NonNull
    private final BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, @NonNull Intent intent) {
            Log.d(TAG, "BroadcastReceiver.onReceive");
            String accountName = intent.getStringExtra(ACCOUNTNAME);
            Boolean isChanged = intent.getBooleanExtra(CHANGED, false);
            Boolean isSynced = intent.getBooleanExtra(SYNCED, false);
            String syncInterval = intent.getStringExtra(SYNCINTERVAL);
            Log.d(TAG, "if " + accountName + " " + Listactivity.imapNotes2Account.GetAccountName());
            if (accountName.equals(Listactivity.imapNotes2Account.GetAccountName())) {
                String statusText;
                if (isSynced) {
                    // Display last sync date
                    //DateFormat dateFormat =
                    //        android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    Date date = new Date();
                    String sdate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
                    String sinterval = " (interval:" + String.valueOf(syncInterval) + " min)";
                    statusText = "Last sync: " + sdate + sinterval;
                } else {
                    statusText = OldStatus;
                }
                //TextView status = (TextView) findViewById(R.id.status);
                status.setText(statusText);

                if (isChanged) {
                    if (storedNotes == null) {
                        storedNotes = new NotesDb(getApplicationContext());
                    }
                    storedNotes.OpenDb();
                    storedNotes.GetStoredNotes(noteList, accountName);
                    listToView.notifyDataSetChanged();
                    storedNotes.CloseDb();
                }
            }
        }
    };

    private void RefreshList() {
        new SyncThread(
                noteList,
                listToView,
                ShowProgress(R.string.refreshing_notes_list),
                storedNotes,
                this.getApplicationContext()).execute();
        //TextView status = (TextView) findViewById(R.id.status);
        status.setText(R.string.welcome);
    }

    private void UpdateList(String suid,
                            String noteBody,
                            Colors color,
                            UpdateThread.Action action) {
        new UpdateThread(Listactivity.imapNotes2Account,
                noteList,
                listToView,
                ShowProgress(R.string.updating_notes_list),
                suid,
                noteBody,
                color,
                getApplicationContext(),
                action,
                storedNotes).execute();
    }

    private ProgressDialog ShowProgress(int detailId) {
        return ProgressDialog.show(this, getString(R.string.app_name),
                getString(detailId), true);
    }

    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.list, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                listToView.getFilter().filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // this is your adapter that will be filtered
                listToView.getFilter().filter(query);
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);

        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                Intent res = new Intent();
                String mPackage = "com.Pau.ImapNotes2";
                String mClass = ".AccountConfigurationActivity";
                res.setComponent(new ComponentName(mPackage, mPackage + mClass));
                res.putExtra(ACTION, AccountConfigurationActivity.Actions.CREATE_ACCOUNT);
                startActivity(res);
                return true;
            case R.id.refresh:
                //TextView status = (TextView) findViewById(R.id.status);
                TriggerSync(status);
                return true;
            case R.id.newnote:
                Intent toNew = new Intent(this, NewNoteActivity.class);
                toNew.putExtra(NewNoteActivity.usesSticky, Listactivity.imapNotes2Account.GetUsesticky());
                startActivityForResult(toNew, Listactivity.NEW_BUTTON);
                return true;
            case R.id.about:
                try {
                    ComponentName comp = new ComponentName(this.getApplicationContext(), Listactivity.class);
                    PackageInfo pinfo = this.getApplicationContext().getPackageManager().getPackageInfo(comp.getPackageName(), 0);
                    String versionName = "Version: " + pinfo.versionName;
                    String versionCode = "Code: " + String.valueOf(pinfo.versionCode);

                    new AlertDialog.Builder(this)
                            .setTitle("About ImapNotes2")
                            .setMessage(versionName + "\n" + versionCode)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }
                            })
                            .show();
                } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                    Log.d(TAG, "except");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        switch (requestCode) {
            case Listactivity.SEE_DETAIL:
                // Returning from NoteDetailActivity
                if (resultCode == Listactivity.DELETE_BUTTON) {
                    // Delete Message asked for
                    // String suid will contain the Message Imap UID to delete
                    String suid = data.getStringExtra(DELETE_ITEM_NUM_IMAP);
                    this.UpdateList(suid, null, null, UpdateThread.Action.Delete);
                }
                if (resultCode == Listactivity.EDIT_BUTTON) {
                    String txt = data.getStringExtra(EDIT_ITEM_TXT);
                    String suid = data.getStringExtra(EDIT_ITEM_NUM_IMAP);
                    Colors color = (Colors) data.getSerializableExtra(EDIT_ITEM_COLOR);
                    //Log.d(TAG,"Received request to edit message:"+suid);
                    //Log.d(TAG,"Received request to replace message with:"+txt);
                    this.UpdateList(suid, txt, color, UpdateThread.Action.Update);
                    //TextView status = (TextView) findViewById(R.id.status);
                    TriggerSync(status);
                }
            case Listactivity.NEW_BUTTON:
                // Returning from NewNoteActivity
                if (resultCode == Listactivity.SAVE_BUTTON) {
                    String res = data.getStringExtra(SAVE_ITEM);
                    //Log.d(TAG,"Received request to save message:"+res);
                    Colors color = (Colors) data.getSerializableExtra(SAVE_ITEM_COLOR);
                    this.UpdateList(null, res, color, UpdateThread.Action.Insert);
                }
        }
    }

    // Spinner item selected listener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        (this.accountSpinner.getSelectedView()).setBackgroundColor(0xFFB6B6B6);
        Account account = Listactivity.accounts[pos];
        // Check periodic sync. If set to 86400 (once a day), set it to 900 (15 minutes)
        // this is due to bad upgrade to v4 which handles offline mode and syncing
        // Remove this code after V4.0 if version no more used
        List<PeriodicSync> currentSyncs = ContentResolver.getPeriodicSyncs(account, AUTHORITY);
        for (PeriodicSync onesync : currentSyncs) {
            if (onesync.period == 86400) {
                ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), 60);
                Toast.makeText(getApplicationContext(), "Recreating this account is recommended to manage sync interval. Set to 15 minutes in the meantime",
                        Toast.LENGTH_LONG).show();
            }
        }

        ImapNotes2Account imapNotes2Account = Listactivity.imapNotes2Account;
        imapNotes2Account.SetAccount(account, getApplicationContext());
        //imapNotes2Account.SetAccountname(account.name);
        //imapNotes2Account.SetUsername(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.UserName));
        //String pwd = Listactivity.accountManager.getPassword(account);
        //imapNotes2Account.SetPassword(pwd);
        //imapNotes2Account.SetServer(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.Server));
        //imapNotes2Account.SetPortnum(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.PortNumber));
        //imapNotes2Account.SetSecurity(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.Security));
        //imapNotes2Account.SetUsesticky("true".equals(accountManager.getUserData(account, ConfigurationFieldNames.UseSticky)));
        //imapNotes2Account.SetSyncinterval(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.SyncInterval));
        //imapNotes2Account.SetaccountHasChanged();
        this.RefreshList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }

    private void updateAccountSpinner() {

        this.spinnerList.notifyDataSetChanged();
        //this.accountSpinner.setSelection(spinnerList.getPosition(currentAccountname));
        if (this.accountSpinner.getSelectedItemId() == android.widget.AdapterView.INVALID_ROW_ID) {
            this.accountSpinner.setSelection(0);
        }

        if (Listactivity.currentList.size() == 1) {
            Account account = Listactivity.accounts[0];
            ImapNotes2Account imapNotes2Account = Listactivity.imapNotes2Account;
            imapNotes2Account.SetUsername(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.UserName));
            String pwd = Listactivity.accountManager.getPassword(account);
            imapNotes2Account.SetPassword(pwd);
            imapNotes2Account.SetServer(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.Server));
            imapNotes2Account.SetPortnum(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.PortNumber));
            imapNotes2Account.SetSecurity(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.Security));
            imapNotes2Account.SetUsesticky("true".equals(accountManager.getUserData(account, ConfigurationFieldNames.UseSticky)));
            imapNotes2Account.SetSyncinterval(Listactivity.accountManager.getUserData(account, ConfigurationFieldNames.SyncInterval));
            //imapNotes2Account.SetaccountHasChanged();
        }
    }

    private class AccountsUpdateListener implements OnAccountsUpdateListener {
        private ArrayList<Account> newAccounts;

        @Override
        public void onAccountsUpdated(@NonNull Account[] accounts) {
            List<String> newList;
            //Integer newListSize = 0;
            //invoked when the AccountManager starts up and whenever the account set changes
            this.newAccounts = new ArrayList<>();
            for (final Account account : accounts) {
                if (account.type.equals("com.Pau.ImapNotes2")) {
                    this.newAccounts.add(account);
                }
            }
            if (this.newAccounts.size() > 0) {
                Account[] imapNotes2Accounts = new Account[this.newAccounts.size()];
                int i = 0;
                for (final Account account : this.newAccounts) {
                    imapNotes2Accounts[i] = account;
                    i++;
                }
                Listactivity.accounts = imapNotes2Accounts;
                newList = new ArrayList<>();
                for (Account account : Listactivity.accounts) {
                    newList.add(account.name);
                }
                if (newList.size() == 0) return;

                Boolean equalLists = true;
                ListIterator<String> iter = Listactivity.currentList.listIterator();
                while (iter.hasNext()) {
                    String s = iter.next();
                    if (!(newList.contains(s))) {
                        iter.remove();
                        // Why try here?
                        try {
                            String stringDir = ImapNotes2k.ConfigurationDirPath(getApplicationContext()) + "/" + s;
                            FileUtils.deleteDirectory(new File(stringDir));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        equalLists = false;
                    }
                }
                for (String accountName : newList) {
                    if (!(Listactivity.currentList.contains(accountName))) {
                        Listactivity.currentList.add(accountName);
                        SyncUtils.CreateLocalDirectories(accountName, getApplicationContext());

                        equalLists = false;
                    }
                }
                if (equalLists) return;
                updateAccountSpinner();
            } else {
                File filesDir = ImapNotes2k.ConfigurationDir(getApplicationContext());
                try {
                    FileUtils.cleanDirectory(filesDir);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Intent res = new Intent();
                String mPackage = "com.Pau.ImapNotes2";
                String mClass = ".AccountConfigurationActivity";
                res.setComponent(new ComponentName(mPackage, mPackage + mClass));
                startActivity(res);
            }
        }
    }

// --Commented out by Inspection START (12/2/16 8:49 PM):
//    // In case of neccessary debug  with user approval
//    // This function will be called from onDestroy
//    public void SendLogcatMail() {
//        String emailData = "";
//        try {
//            Process process = Runtime.getRuntime().exec("logcat -d");
//            BufferedReader bufferedReader = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));
//
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                sb.append(line).append("\n");
//            }
//            emailData = sb.toString();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        //send file using email
//        Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        String to[] = {"nb@dagami.org"};
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
//        // the attachment
//        emailIntent.putExtra(Intent.EXTRA_TEXT, emailData);
//        // the mail subject
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Logcat content for ImapNotes2 debugging");
//        emailIntent.setType("message/rfc822");
//        startActivity(Intent.createChooser(emailIntent, "Send email..."));
//    }
// --Commented out by Inspection STOP (12/2/16 8:49 PM)

    private static void TriggerSync(@NonNull TextView statusField) {
        OldStatus = statusField.getText().toString();
        statusField.setText(R.string.syncing);
        Account mAccount = Listactivity.imapNotes2Account.GetAccount();
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        //Log.d(TAG,"Request a sync for:"+mAccount);
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
    }

    @Nullable
    @Override
    public Filter getFilter() {
        return null;
    }
}

