package com.Pau.ImapNotes2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;

import com.Pau.ImapNotes2.R;
import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.OneNote;
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Miscs.UpdateThread;
import com.Pau.ImapNotes2.Miscs.SyncThread;
import com.Pau.ImapNotes2.Sync.SyncService;
import com.Pau.ImapNotes2.Sync.SyncUtils;

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
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Listactivity extends Activity  implements OnItemSelectedListener,Filterable {
    private static final int SEE_DETAIL = 2;
    private static final int DELETE_BUTTON = 3;
    private static final int NEW_BUTTON = 4;
    private static final int SAVE_BUTTON = 5;
    private static final int EDIT_BUTTON = 6;
        
    private ArrayList<OneNote> noteList;
    private NotesListAdapter listToView;
    private ArrayAdapter<String> spinnerList;
    
    private Imaper imapFolder;
    private static NotesDb storedNotes = null;
    private Spinner accountSpinner;
    public static ImapNotes2Account imapNotes2Account;
    private static AccountManager accountManager;
    private static Account[] accounts;
    private static List<String> currentList;
    private TextView status = null;
    private static String OldStatus;
    private Button editAccountButton=null;
    private ListView listview;
    public static final String AUTHORITY = "com.Pau.ImapNotes2.provider";
    private static final String TAG = "IN_Listactivity";
    

    private OnClickListener clickListenerEditAccount = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Clic on editAccount Button
            Intent res = new Intent();
            String mPackage = "com.Pau.ImapNotes2";
            String mClass = ".AccontConfigurationActivity";
            res.setComponent(new ComponentName(mPackage,mPackage+mClass));
            res.putExtra("action", "EDIT_ACCOUNT");
            res.putExtra("accountname", Listactivity.imapNotes2Account.GetAccountname());
            startActivity(res);
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    // Accounts spinner
    this.accountSpinner = (Spinner) findViewById(R.id.accountSpinner);
    Listactivity.currentList = new ArrayList<String>();
    // Spinner item selection Listener
    this.accountSpinner.setOnItemSelectedListener(this);

    imapNotes2Account = new ImapNotes2Account();
    Listactivity.accountManager = AccountManager.get(getApplicationContext());
    Listactivity.accountManager.addOnAccountsUpdatedListener((OnAccountsUpdateListener)
        new AccountsUpdateListener(), null, true);

    status = (TextView)findViewById(R.id.status);

    this.spinnerList = new ArrayAdapter<String>
        (this, android.R.layout.simple_spinner_item,Listactivity.currentList);
    spinnerList.setDropDownViewResource
        (android.R.layout.simple_spinner_dropdown_item);
    this.accountSpinner.setAdapter(spinnerList);

    this.noteList = new ArrayList<OneNote>();
    ((ImapNotes2)this.getApplicationContext()).SetNotesList(this.noteList);
    this.listToView = new NotesListAdapter(
        getApplicationContext(),
        this.noteList,
        R.layout.note_element,
        new String[]{"title","date"},
        new int[]{R.id.noteTitle, R.id.noteInformation});
    listview = (ListView) findViewById(R.id.notesList);
    listview.setAdapter(this.listToView);

    listview.setTextFilterEnabled(true);
    
    this.imapFolder = new Imaper();
    ((ImapNotes2)this.getApplicationContext()).SetImaper(this.imapFolder);
    
    if (Listactivity.storedNotes == null)
        storedNotes = new NotesDb(getApplicationContext());
    
    // When item is clicked, we go to NoteDetailActivity
    listview.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View widget, int selectedNote, long arg3) {
            Intent toDetail = new Intent(widget.getContext(), NoteDetailActivity.class);
            toDetail.putExtra("selectedNote", (OneNote)arg0.getItemAtPosition(selectedNote));
            toDetail.putExtra("useSticky", Listactivity.imapNotes2Account.GetUsesticky());
            startActivityForResult(toDetail,SEE_DETAIL); 
        }
      });

      editAccountButton = (Button) findViewById(R.id.editAccountButton);
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
        int len = this.accounts == null ? 0 : this.accounts.length;
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

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String accountname = intent.getStringExtra("ACCOUNTNAME");
            Boolean isChanged = intent.getBooleanExtra("CHANGED", false);
            Boolean isSynced = intent.getBooleanExtra("SYNCED", false);
            String syncInterval = intent.getStringExtra("SYNCINTERVAL");
            if (accountname.equals(Listactivity.imapNotes2Account.GetAccountname())) {
                if (isSynced) {
                    // Display last sync date
                    DateFormat dateFormat =
                        android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    Date date = new Date();
                    String sdate = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(date);
                    String sinterval = " (interval:" + String.valueOf(syncInterval) + " min)";
                    status.setText("Last sync: " + sdate + sinterval);
                } else {
                    status.setText(OldStatus);
                }

                if (isChanged) {
                    if (Listactivity.storedNotes == null)
                         storedNotes = new NotesDb(getApplicationContext());
                    storedNotes.OpenDb();
                    storedNotes.GetStoredNotes(noteList, accountname);
                    listToView.notifyDataSetChanged();
                    storedNotes.CloseDb();
                }
            }
        }
    };

    public void RefreshList(){
        ProgressDialog loadingDialog = ProgressDialog.show(this, "ImapNotes2" , "Refreshing notes list... ", true);

        new SyncThread().execute(this.imapFolder, Listactivity.imapNotes2Account, this.noteList, this.listToView, loadingDialog, this.storedNotes, this.getApplicationContext());
        status.setText("Welcome");
    }
    
    public void UpdateList(String suid, String noteBody, String color, String action){
        ProgressDialog loadingDialog = ProgressDialog.show(this, "imapnote2" , "Updating notes list... ", true);

        new UpdateThread().execute(this.imapFolder, Listactivity.imapNotes2Account, this.noteList, this.listToView, loadingDialog, suid, noteBody, color, this.getApplicationContext(), action, this.storedNotes);

    }
    
    public boolean onCreateOptionsMenu(Menu menu){
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
    
    public boolean onOptionsItemSelected (MenuItem item){
    switch (item.getItemId()){
        case R.id.login:
            Intent res = new Intent();
            String mPackage = "com.Pau.ImapNotes2";
            String mClass = ".AccontConfigurationActivity";
            res.setComponent(new ComponentName(mPackage,mPackage+mClass));
            res.putExtra("action", "CREATE_ACCOUNT");
            startActivity(res);
            return true;
        case R.id.refresh:
            this.TriggerSync(this.status);
            return true;
        case R.id.newnote:
            Intent toNew = new Intent(this, NewNoteActivity.class);
            toNew.putExtra("usesSticky", Listactivity.imapNotes2Account.GetUsesticky());
            startActivityForResult(toNew,Listactivity.NEW_BUTTON);
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
                Log.d("XXXXX","except");
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){ 
        switch(requestCode) {
            case Listactivity.SEE_DETAIL:
            // Returning from NoteDetailActivity
            if (resultCode == Listactivity.DELETE_BUTTON) {
                // Delete Message asked for
                // String suid will contain the Message Imap UID to delete
                String suid = data.getStringExtra("DELETE_ITEM_NUM_IMAP");
                this.UpdateList(suid, null, null, "delete");
            }
            if (resultCode == Listactivity.EDIT_BUTTON) {
                String txt = data.getStringExtra("EDIT_ITEM_TXT");
                String suid = data.getStringExtra("EDIT_ITEM_NUM_IMAP");
                String color = data.getStringExtra("EDIT_ITEM_COLOR");
                //Log.d(TAG,"Received request to delete message:"+suid);
                //Log.d(TAG,"Received request to replace message with:"+txt);
                this.UpdateList(suid, txt, color, "update");
            }
            case Listactivity.NEW_BUTTON:
            // Returning from NewNoteActivity
            if (resultCode == Listactivity.SAVE_BUTTON) {
                String res = data.getStringExtra("SAVE_ITEM");
                //Log.d(TAG,"Received request to save message:"+res);
                String color = data.getStringExtra("SAVE_ITEM_COLOR");
                this.UpdateList(null, res, color, "insert");
            }
        }
    }

    // Spinner item selected listener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        ((TextView) this.accountSpinner.getSelectedView()).setBackgroundColor(0xFFB6B6B6);
        Account account = Listactivity.accounts[pos];
        // Check periodic sync. If set to 86400 (once a day), set it to 900 (15 minutes)
        // this is due to bad upgrade to v4 which handles offline mode and syncing
        // Remove this code after V4.0 if version no more used
        List<PeriodicSync> currentSyncs = ContentResolver.getPeriodicSyncs (account, AUTHORITY);
        for (PeriodicSync onesync : currentSyncs) {
            if (onesync.period == 86400) {
                ContentResolver.setIsSyncable(account, AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), 60);
                Toast.makeText(getApplicationContext(), "Recreating this account is recommended to manage sync interval. Set to 15 minutes in the meantime",
                        Toast.LENGTH_LONG).show();
            }
        }
        // End of code
        Listactivity.imapNotes2Account.SetAccountname(account.name);
        Listactivity.imapNotes2Account.SetUsername(Listactivity.accountManager.getUserData (account, "username"));
        String pwd = Listactivity.accountManager.getPassword(account);
        Listactivity.imapNotes2Account.SetPassword(pwd);
        Listactivity.imapNotes2Account.SetServer(Listactivity.accountManager.getUserData (account, "server"));
        Listactivity.imapNotes2Account.SetPortnum(Listactivity.accountManager.getUserData (account, "portnum"));
        Listactivity.imapNotes2Account.SetSecurity(Listactivity.accountManager.getUserData (account, "security"));
        Listactivity.imapNotes2Account.SetUsesticky(accountManager.getUserData (account, "usesticky"));
        Listactivity.imapNotes2Account.SetSyncinterval(Listactivity.accountManager.getUserData (account, "syncinterval"));
        Listactivity.imapNotes2Account.SetaccountHasChanged();
        Listactivity.imapNotes2Account.SetAccount(account);
        this.RefreshList();
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
        
    }

    private void updateAccountSpinner () {

        this.spinnerList.notifyDataSetChanged();
        //this.accountSpinner.setSelection(spinnerList.getPosition(currentAccountname));
        if (this.accountSpinner.getSelectedItemId() == android.widget.AdapterView.INVALID_ROW_ID) {
        this.accountSpinner.setSelection(0);
        }
    
        if (Listactivity.currentList.size() == 1) {
            Account account = Listactivity.accounts[0];
            Listactivity.imapNotes2Account.SetUsername(Listactivity.accountManager.getUserData (account, "username"));
            String pwd = Listactivity.accountManager.getPassword(account);
            Listactivity.imapNotes2Account.SetPassword(pwd);
            Listactivity.imapNotes2Account.SetServer(Listactivity.accountManager.getUserData (account, "server"));
            Listactivity.imapNotes2Account.SetPortnum(Listactivity.accountManager.getUserData (account, "portnum"));
            Listactivity.imapNotes2Account.SetSecurity(Listactivity.accountManager.getUserData (account, "security"));
            Listactivity.imapNotes2Account.SetUsesticky(accountManager.getUserData (account, "usesticky"));
            Listactivity.imapNotes2Account.SetSyncinterval(Listactivity.accountManager.getUserData (account, "syncinterval"));
            Listactivity.imapNotes2Account.SetaccountHasChanged();
        }
    }

    private class AccountsUpdateListener implements OnAccountsUpdateListener {
        private ArrayList<Account> newAccounts;
        
        @Override
        public void onAccountsUpdated(Account[] accounts) {
            List<String> newList;
            Integer newListSize = 0;
            //invoked when the AccountManager starts up and whenever the account set changes
            this.newAccounts = new ArrayList<Account>();
            for (final Account account : accounts) {
                if (account.type.equals("com.Pau.ImapNotes2")) {
                    this.newAccounts.add(account);
                }
            }
            if (this.newAccounts.size() > 0) {
                Account[] imapNotes2Accounts = new Account[this.newAccounts.size()] ;
                int i = 0;
                for (final Account account : this.newAccounts) {
                    imapNotes2Accounts[i] = account;
                    i++;
                }
                Listactivity.accounts = imapNotes2Accounts;
                newList = new ArrayList<String>();
                for (Account account: Listactivity.accounts ) {
                    newList.add(account.name);
                }
                if (newList.size() == 0) return;
        
                Boolean equalLists = true;
                ListIterator<String> iter = Listactivity.currentList.listIterator();
                while(iter.hasNext()){
                    String s = iter.next();
                    if (!(newList.contains(s))) {
                        iter.remove();
                        String stringDir = (ImapNotes2.getAppContext()).getFilesDir() + "/" + s;
                        try {
                            FileUtils.deleteDirectory(new File (stringDir));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        equalLists = false;
                    }
                }
                for (String accountName: newList ) {
                    if (!(Listactivity.currentList.contains(accountName))) {
                        Listactivity.currentList.add(accountName);
                        SyncUtils.CreateDirs (accountName, ImapNotes2.getAppContext());
        
                        equalLists = false;
                    }
                }
                if (equalLists) return;
                updateAccountSpinner();
            } else {
                File filesDir = (ImapNotes2.getAppContext()).getFilesDir();
                try {
                    FileUtils.cleanDirectory(filesDir);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Intent res = new Intent();
                String mPackage = "com.Pau.ImapNotes2";
                String mClass = ".AccontConfigurationActivity";
                res.setComponent(new ComponentName(mPackage,mPackage+mClass));
                startActivity(res);
            }
        }
    }

    // In case of neccessary debug  with user approval
    // This function will be called from onDestroy
    public void SendLogcatMail(){
        String emailData="";
        try {
          Process process = Runtime.getRuntime().exec("logcat -d");
          BufferedReader bufferedReader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));
    
          StringBuilder sb=new StringBuilder();
          String line;
          while ((line = bufferedReader.readLine()) != null) {
            sb.append(line + "\n");
          }
          emailData=sb.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        String to[] = {"nb@dagami.org"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent .putExtra(Intent.EXTRA_TEXT, emailData);
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Logcat content for ImapNotes2 debugging");
        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

    public static void TriggerSync(TextView statusField) {
        OldStatus=statusField.getText().toString();
        statusField.setText("Syncing...");
        Account mAccount = Listactivity.imapNotes2Account.GetAccount();
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        //Log.d(TAG,"Request a sync for:"+mAccount);
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
    }

	@Override
	public Filter getFilter() {
		return null;
	}
}
     
