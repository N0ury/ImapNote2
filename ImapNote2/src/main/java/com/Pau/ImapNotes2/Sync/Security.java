package com.Pau.ImapNotes2.Sync;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kj on 11/1/16.
 * <p>
 * <p>
 * Use this instead of integers in the account configuration.  Store the name of the security type instead.
 */

public enum Security {
    None("None"),
    SSL_TLS("SSL/TLS"),
    SSL_TLS_accept_all_certificates("SSL/TLS (accept all certificates)"),
    STARTTLS("STARTTLS"),
    STARTTLS_accept_all_certificates("STARTTLS (accept all certificates)");

    private final String printable;

    Security(String printable) {
        this.printable = printable;
    }

    public static List<String> Printables() {
        List<String> list = new ArrayList<String>();
        for (Security e : Security.values()) {
            list.add(e.printable);
        }
        return list;
    }
}
