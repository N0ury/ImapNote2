package com.Pau.ImapNotes2.Data;

import android.support.annotation.NonNull;

import java.util.HashMap;

/**
 * Represents metadata about a note in a way that can be used by a ListAdapter.  The list adapter
 * needs objects that have a map interface because it must fetch the items by string name.
 */
public class OneNote extends HashMap<String, String> {
    public static final String TITLE = "title";
    public static final String DATE = "date";
    private static final String UID = "uid";


    /**
     *
     */
    private static final long serialVersionUID = 1L;


    public OneNote(String title, String date, String uid) {
        super();
        put(TITLE, title);
        put(DATE, date);
        put(UID, uid);
    }

    @NonNull
    public String GetTitle() {
        return this.get(TITLE);
    }

    @NonNull
    String GetDate() {
        return this.get(DATE);
    }

    @NonNull
    public String GetUid() {
        return this.get(UID);
    }


    public void SetDate(String date) {
        this.put(DATE, date);
    }

    public void SetUid(String uid) {
        this.put(UID, uid);
    }

    @NonNull
    @Override
    public String toString() {
        return ("Title:" + this.GetTitle() +
                " Date: " + this.GetDate() +
                " Uid: " + this.GetUid());
    }
}
