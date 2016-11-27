package com.Pau.ImapNotes2.Miscs;

import android.support.annotation.NonNull;

import static com.Pau.ImapNotes2.NoteDetailActivity.Colors;

public class Sticky {

    public final String text;
    // --Commented out by Inspection (11/26/16 11:50 PM):private final String position;
    @NonNull
    public final Colors color;

    public Sticky(String text,
                  String position,
                  @NonNull Colors color) {
        this.text = text;
        // this.position = position;
        this.color = color;
    }
/*
    public String GetPosition() {
        return Sticky.position;
    }

    public String GetText() {
        return Sticky.text;
    }

    public Colors GetColor() {
        return Sticky.color;
    }

    public void SetText(String text) {
        Sticky.text = text;
    }

    public void SetPosition(String position) {
        Sticky.position = position;
    }

    public void SetColor(Colors color) {
        Sticky.color = color;
    }*/
}
