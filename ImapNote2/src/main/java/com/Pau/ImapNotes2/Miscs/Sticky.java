package com.Pau.ImapNotes2.Miscs;

import static com.Pau.ImapNotes2.NoteDetailActivity.Colors;

public class Sticky {

    // TODO: Why are these fields static when the class is not?
    private static String text;
    private static String position;
    private static Colors color;

    public Sticky() {
        Sticky.text = "";
        Sticky.position = "0 0 0 0";
        Sticky.color = Colors.YELLOW;
    }

    // TODO: why is a constructor allowed to modify static members?
    public Sticky(String text,
                  String position,
                  Colors color) {
        Sticky.text = text;
        Sticky.position = position;
        Sticky.color = color;
    }

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
    }
}
