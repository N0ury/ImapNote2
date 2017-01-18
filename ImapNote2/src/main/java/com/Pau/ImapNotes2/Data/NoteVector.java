package com.Pau.ImapNotes2.Data;

import android.support.annotation.NonNull;

import java.util.HashMap;

/**
 * Created by kj on 2017-01-09 11:13.
 */

public class NoteVector extends HashMap<String, Integer> {

    enum ComparisonResult {
        Later,
        Equal,
        Earlier,
        Conflict;
    }

    NoteVector() {

    }


    /**
     * @param deviceID
     * @return Clock or zero if the deviceID is not found.
     */
    int get(String deviceID) {
        return containsKey(deviceID)
                ? get(deviceID)
                : 0;
    }


    // Returns EARLIER if this object is unambiguously earlier than other.
    public ComparisonResult compareTo(@NonNull NoteVector other) {
        ComparisonResult result = ComparisonResult.Equal;
        for (Entry<String, Integer> entry : this.entrySet()) {
            Integer otherClock = other.get(entry.getKey());
            ComparisonResult singleResult = compareEntries(entry.getValue(), otherClock);
            switch (result) {
                case Equal:
                    result = singleResult;
                    break;
                case Earlier:
                    if (singleResult == ComparisonResult.Later) {
                        return ComparisonResult.Conflict;
                    }
                    break;
                case Later:
                    if (singleResult == ComparisonResult.Earlier) {
                        return ComparisonResult.Conflict;
                    }
                case Conflict:
                    // cannot occur
                    break;
                default: {
                    // Nothing to do, cannot occur.
                }
            }
        }
        return result;
    }

    // Returns EARLIER if a is earlier than b.
    public ComparisonResult compareEntries(@NonNull Integer aClock,
                                           @NonNull Integer bClock) {
        return (aClock < bClock)
                ? ComparisonResult.Earlier
                : ((bClock < aClock)
                ? ComparisonResult.Later
                : ComparisonResult.Equal);
    }
}

