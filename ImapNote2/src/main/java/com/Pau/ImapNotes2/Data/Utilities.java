package com.Pau.ImapNotes2.Data;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by kj on 2016-11-12 17:21.
 * <p>
 * Reduce repetition by providing static fields and methods for common operations.
 */
public class Utilities {

    /**
     * The notes have a time stamp associated with thme and this is stored as a string on the
     * server so we must define a fixed format for it.
     */
    @NonNull
    private static final String internalDateFormatString = "yyyy-MM-dd HH:mm:ss";
    @NonNull
    public static final SimpleDateFormat internalDateFormat = new SimpleDateFormat(internalDateFormatString, Locale.ROOT);
}
