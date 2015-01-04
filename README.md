imapnote2
=========

Sync your notes between Android, iOs devices and different accounts like Gmail, iCloud and others

This project is a fork of "imapnote" one created by boemianrapsodi (boemianrapsody@gmail.com). Probably Pasquale Boemio.
Some things don't work correctly but it was impossible to contact him to correct these bugs.

Original app is named "imapnote", and is available at https://code.google.com/p/imapnote/,
So I decided to name this one "imapnote2". It is under the GPL v3 License, as is "imapnote"

It is based on Apple way to manage notes. They are stored in an imap folder named "Notes".
imapnote uses Gmail for syncing. As I use my own imap server, I have modified it to be used with any imap server that respects Apple method. It has been tested with Gmail, iCloud (imap.mail.me.com), Yahoo! (imap.mail.yahoo.com), AOL (imap.aol.com) and of course my server. Even if not still tested, it should work with others.

Main changes are:
- app can be used with other servers, not only gmail
- it's possible to open notes
- no permissions are needed by the app
- it works in landscape and portrait modes. Landscape is useful with some devices
- it's possible to delete notes (trash option on detail screen)
- it's possible to create new notes ("new note" option on list screen)
- it's possible to modify notes (change note on detail screen, then save it with disk icon)
- navigation uses ActionBar (minSdkVersion=14)
- app has been modified to handle sticky notes used by Kerio Connect servers
- even if not recommended, untrusted certificates can be used. A new option permits it

This app is available at Google Play under the name "ImapNote2". But for people who don't have access to this market, it will be possible to compile source code. Furthermore, an apk is available in bin directory.
