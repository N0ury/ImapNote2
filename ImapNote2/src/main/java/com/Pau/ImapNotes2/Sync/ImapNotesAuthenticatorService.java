package com.Pau.ImapNotes2.Sync;

import com.Pau.ImapNotes2.AccountConfigurationActivity;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ImapNotesAuthenticatorService extends Service {

    private static final String TAG = "ImapNotesAuthenticationService";
    private Authenticator imapNotesAuthenticator;

    @Override
    public void onCreate() {
        this.imapNotesAuthenticator = new Authenticator(this);
    }

    public IBinder onBind(@NonNull Intent intent) {
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

        private final Context mContext;

        public Authenticator(Context context) {
            super(context);
            this.mContext = context;
        }

        @Override
        public Bundle getAccountRemovalAllowed(
                AccountAuthenticatorResponse response, @NonNull Account account)
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

        @NonNull
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {

            Intent toLoginActivity = new Intent(this.mContext, AccountConfigurationActivity.class);
            toLoginActivity.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, toLoginActivity);

            return bundle;
        }

        @Nullable
        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {

            return null;
        }

        // TODO: Describe the purpose of this method.
        @Nullable
        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {

            return null;
        }

        // TODO: Describe the purpose of this method.
        @Nullable
        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

            return null;
        }

        // TODO: Describe the purpose of this method.
        @Nullable
        @Override
        public String getAuthTokenLabel(String authTokenType) {

            return null;
        }

        // TODO: Describe the purpose of this method.
        @Nullable
        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {

            return null;
        }

        // TODO: Describe the purpose of this method.
        @Nullable
        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

            return null;
        }

    }

}
