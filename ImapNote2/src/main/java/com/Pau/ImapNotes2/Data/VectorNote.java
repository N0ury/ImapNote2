package com.Pau.ImapNotes2.Data;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;


/**
 * Created by kj on 2017-01-09 10:59.
 * <p>
 * Represents metadata about a note in a way that can be used by a ListAdapter.  The list adapter
 * needs objects that have a map interface because it must fetch the items by string name.
 */
public class VectorNote extends HashMap<String, String> {
    public static final String TITLE = "title";
    public static final String DATE = "date";
    //private static final String UID = "uid";


    public VectorNote(@NonNull String title,
                      long mtime) {
        super();
        put(TITLE, title);
        put(DATE, new Date(mtime).toString());
    }

    @NonNull
    public String GetTitle() {
        return this.get(TITLE);
    }

    @NonNull
    String GetDate() {
        return this.get(DATE);
    }


    public void SetDate(@NonNull String date) {
        this.put(DATE, date);
    }


    @NonNull
    @Override
    public String toString() {
        return ("Title:" + this.GetTitle() +
                " Date: " + this.GetDate());
    }
}
