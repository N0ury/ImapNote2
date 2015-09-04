package com.Pau.ImapNotes2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import com.Pau.ImapNotes2.R;
import com.Pau.ImapNotes2.Data.NotesDb;
import com.Pau.ImapNotes2.Miscs.Imaper;
import com.Pau.ImapNotes2.Miscs.OneNote;
import com.Pau.ImapNotes2.Data.ImapNotes2Account;
import com.Pau.ImapNotes2.Miscs.UpdateThread;
import com.Pau.ImapNotes2.Miscs.SyncThread;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class Listactivity extends Activity  implements OnItemSelectedListener {
    private static final int SEE_DETAIL = 2;
    private static final int DELETE_BUTTON = 3;
    private static final int NEW_BUTTON = 4;
    private static final int SAVE_BUTTON = 5;
    private static final int EDIT_BUTTON = 6;
        
    private ArrayList<OneNote> noteList;
    private SimpleAdapter listToView;
    private ArrayAdapter<String> spinnerList;
    
    private Imaper imapFolder;
    private NotesDb storedNotes;
    private Spinner accountSpinner;
    public static ImapNotes2Account imapNotes2Account;
    private static AccountManager accountManager;
    private static Account[] accounts;
    private static List<String> currentList;
    private static final String TAG = "IN_Listactivity";
    
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

    this.spinnerList = new ArrayAdapter<String>
        (this, android.R.layout.simple_spinner_item,Listactivity.currentList);
    spinnerList.setDropDownViewResource
        (android.R.layout.simple_spinner_dropdown_item);
    this.accountSpinner.setAdapter(spinnerList);

    this.noteList = new ArrayList<OneNote>();
    ((ImapNotes2)this.getApplicationContext()).SetNotesList(this.noteList);
    this.listToView = new SimpleAdapter(
        getApplicationContext(),
        this.noteList,
        R.layout.note_element,
        new String[]{"title","date"},
        new int[]{R.id.noteTitle, R.id.noteInformation});
    ((ListView)findViewById(R.id.notesList)).setAdapter(this.listToView);
    
    this.imapFolder = new Imaper();
    ((ImapNotes2)this.getApplicationContext()).SetImaper(this.imapFolder);
    
    this.storedNotes = new NotesDb(this.getApplicationContext());
    this.storedNotes.OpenDb();
    this.storedNotes.GetStoredNotes(this.noteList);
    this.listToView.notifyDataSetChanged();
    this.storedNotes.CloseDb();
    
    // When item is clicked, we go to NoteDetailActivity
    ((ListView)findViewById(R.id.notesList)).setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View widget, int selectedNote, long arg3) {
            Intent toDetail = new Intent(widget.getContext(), NoteDetailActivity.class);
            toDetail.putExtra("selectedNote", (OneNote)noteList.get(selectedNote));
            toDetail.putExtra("usesSticky", Listactivity.imapNotes2Account.GetUsesticky());
            startActivityForResult(toDetail,SEE_DETAIL); 
        }
      });
    }

    public void onStart() {
        super.onStart();
        int len = this.accounts == null ? 0 : this.accounts.length;
        if (len > 0) updateAccountSpinner();
    }
    
    public void RefreshList(){
        ProgressDialog loadingDialog = ProgressDialog.show(this, "ImapNotes2" , "Refreshing notes list... ", true);

        new SyncThread().execute(this.imapFolder, Listactivity.imapNotes2Account, this.noteList, this.listToView, loadingDialog, this.storedNotes, this.getApplicationContext());

    }
    
    public void UpdateList(String numInImap, String snote, String color){
        ProgressDialog loadingDialog = ProgressDialog.show(this, "imapnote2" , "Updating notes list... ", true);

        new UpdateThread().execute(this.imapFolder, Listactivity.imapNotes2Account, this.noteList, this.listToView, loadingDialog, numInImap, snote, color, this.getApplicationContext());

    }
    
    public boolean onCreateOptionsMenu(Menu menu){
    getMenuInflater().inflate(R.menu.list, menu);
    
    return true;

    }
    
    public boolean onOptionsItemSelected (MenuItem item){
    switch (item.getItemId()){
        case R.id.login:
            Intent res = new Intent();
            String mPackage = "com.Pau.ImapNotes2";
            String mClass = ".AccontConfigurationActivity";
            res.setComponent(new ComponentName(mPackage,mPackage+mClass));
            startActivity(res);
            return true;
        case R.id.refresh:
            this.RefreshList();
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
                // String numInImap will contain the Message Imap Number to delete
                String numInImap = data.getStringExtra("DELETE_ITEM_NUM_IMAP");
                this.UpdateList(numInImap, null, null);
            }
            if (resultCode == Listactivity.EDIT_BUTTON) {
                String txt = data.getStringExtra("EDIT_ITEM_TXT");
                String numInImap = data.getStringExtra("EDIT_ITEM_NUM_IMAP");
                String color = data.getStringExtra("EDIT_ITEM_COLOR");
                //Log.d(TAG,"Received request to delete message:"+numInImap);
                //Log.d(TAG,"Received request to replace message with:"+txt);
                this.UpdateList(numInImap, txt, color);
            }
            case Listactivity.NEW_BUTTON:
            // Returning from NewNoteActivity
            if (resultCode == Listactivity.SAVE_BUTTON) {
                String res = data.getStringExtra("SAVE_ITEM");
                //Log.d(TAG,"Received request to save message:"+res);
                String color = data.getStringExtra("SAVE_ITEM_COLOR");
                this.UpdateList(null, res, color);
            }
        }
    }

    // Spinner item selected listener
@Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        ((TextView) this.accountSpinner.getSelectedView()).setBackgroundColor(0xFFB6B6B6);
        Account account = Listactivity.accounts[pos];
        Listactivity.imapNotes2Account.SetAccountname(account.name);
        Listactivity.imapNotes2Account.SetUsername(Listactivity.accountManager.getUserData (account, "username"));
        String pwd = Listactivity.accountManager.getPassword(account);
        Listactivity.imapNotes2Account.SetPassword(pwd);
        Listactivity.imapNotes2Account.SetServer(Listactivity.accountManager.getUserData (account, "server"));
        Listactivity.imapNotes2Account.SetPortnum(Listactivity.accountManager.getUserData (account, "portnum"));
        Listactivity.imapNotes2Account.SetSecurity(Listactivity.accountManager.getUserData (account, "security"));
        Listactivity.imapNotes2Account.SetUsesticky(accountManager.getUserData (account, "usesticky"));
        Listactivity.imapNotes2Account.SetaccountHasChanged();

        this.RefreshList();
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
        
    }

    private void updateAccountSpinner () {
        List<String> newList;
        Integer newListSize = 0;

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
                equalLists = false;
            }
        }
        for (String accountName: newList ) {
            if (!(Listactivity.currentList.contains(accountName))) {
                Listactivity.currentList.add(accountName);
                equalLists = false;
            }
        }
        if (equalLists) return;

        this.spinnerList.notifyDataSetChanged();
        //this.accountSpinner.setSelection(spinnerList.getPosition(currentAccountname));
        if (this.accountSpinner.getSelectedItemId() == android.widget.AdapterView.INVALID_ROW_ID) {
	    this.accountSpinner.setSelection(0);
        }
    
        if (Listactivity.currentList.size() == 1) {
            Account account = Listactivity.accounts[0];
            Listactivity.imapNotes2Account.SetUsername(account.name);
            String pwd = Listactivity.accountManager.getPassword(account);
            Listactivity.imapNotes2Account.SetPassword(pwd);
            Listactivity.imapNotes2Account.SetServer(Listactivity.accountManager.getUserData (account, "server"));
            Listactivity.imapNotes2Account.SetPortnum(Listactivity.accountManager.getUserData (account, "portnum"));
            Listactivity.imapNotes2Account.SetSecurity(Listactivity.accountManager.getUserData (account, "security"));
            Listactivity.imapNotes2Account.SetUsesticky(accountManager.getUserData (account, "usesticky"));
            Listactivity.imapNotes2Account.SetaccountHasChanged();
        }
    }

    private class AccountsUpdateListener implements OnAccountsUpdateListener {
        private ArrayList<Account> newAccounts;
    	
    	@Override
        public void onAccountsUpdated(Account[] accounts) {
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
                updateAccountSpinner();
            } else {
                Intent res = new Intent();
                String mPackage = "com.Pau.ImapNotes2";
                String mClass = ".AccontConfigurationActivity";
                res.setComponent(new ComponentName(mPackage,mPackage+mClass));
                startActivity(res);
            }
        }
    }
}
