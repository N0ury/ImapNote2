package com.Pau.ImapNotes2.Miscs;

import com.Pau.ImapNotes2.AccontConfigurationActivity;
import com.Pau.ImapNotes2.Sync.SyncUtils;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class ImapNotesAuthenticatorService extends Service{

	private static final String TAG = "ImapNotesAuthenticationService";
	private Authenticator imapNotesAuthenticator;

    @Override
    public void onCreate() {
        this.imapNotesAuthenticator = new Authenticator(this);
    }

    public IBinder onBind(Intent intent) {
    	IBinder ret = null;
    	if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT))
    		ret = getAuthenticator().getIBinder();
    	
    	return ret;
    }
    
    private Authenticator getAuthenticator() {
    	if (this.imapNotesAuthenticator == null)
    	   this.imapNotesAuthenticator = new Authenticator(this);
    	  
    	return this.imapNotesAuthenticator;
    }
    
    private static class Authenticator extends AbstractAccountAuthenticator {

    	private Context mContext;
    	
		public Authenticator(Context context) {
			super(context);
			this.mContext = context;
		}

	@Override
	public Bundle getAccountRemovalAllowed(
			AccountAuthenticatorResponse response, Account account)
			throws NetworkErrorException {
		Bundle ret = super.getAccountRemovalAllowed(response, account);
		if (ret.getBoolean(AccountManager.KEY_BOOLEAN_RESULT))
                    SyncUtils.RemoveAccount(this.mContext, account);
/*
			mContext.getContentResolver().delete(ListProvider.getClearUri(),
					null, null);
*/
		return ret;
	}
		@Override
		public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
			
			Intent toLoginActivity = new Intent(this.mContext, AccontConfigurationActivity.class);
			toLoginActivity.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			Bundle bundle = new Bundle();
			bundle.putParcelable(AccountManager.KEY_INTENT, toLoginActivity);
			
			return bundle;
		}

		@Override
		public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {

			return null;
		}

		@Override
		public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {

			return null;
		}

		@Override
		public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

			return null;
		}

		@Override
		public String getAuthTokenLabel(String authTokenType) {

			return null;
		}

		@Override
		public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {

			return null;
		}

		@Override
		public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
			
			return null;
		}
    	
    }

}
