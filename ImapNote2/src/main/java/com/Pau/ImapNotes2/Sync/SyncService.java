package com.Pau.ImapNotes2.Sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


/**
 * There can be as many SyncService instances as you like but they all return the same statically
 * allocated SyncAdapter.
 * TODO: explain why this should be so.  I have made it non-static and it still seems to work.
 */
public class SyncService extends Service {
    public static final String SYNC_FINISHED = "SYNC_FINISHED";
    private static final String TAG = "SyncService";

    private static final Object sSyncAdapterLock = new Object();
    @Nullable
    private  SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        // TODO: fix these comments now thatsSyncAdapter is no longer static.  Fix the code too.
        // This sync lock is necessary because sSyncAdapter is static so if we have more than one
        // SyncService object we would otherwise have a race condition
        synchronized (sSyncAdapterLock) {
            // We check for null because the sync adapter is static and might have been created by
            // another SyncService instance.
            // TODO: find out if it is possible for there to be more than one SyncService object.
            // If there cannot then we need neither the lock nor the null check.
            // TODO: find out why we do not do this in the constructor.
            // We could then annotate sSyncAdapter as @NonNull.
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service is returning IBinder");
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
