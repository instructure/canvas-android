// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a de locale. All the
// messages from the main program should be duplicated here with the same
// function name.

// Ignore issues from commonly used lints in this file.
// ignore_for_file:unnecessary_brace_in_string_interps, unnecessary_new
// ignore_for_file:prefer_single_quotes,comment_references, directives_ordering
// ignore_for_file:annotate_overrides,prefer_generic_function_type_aliases
// ignore_for_file:unused_import, file_names

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';

final messages = new MessageLookup();

typedef String MessageIfAbsent(String messageStr, List<dynamic> args);

class MessageLookup extends MessageLookupByLibrary {
  String get localeName => 'de';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Aufgabennote über ${threshold}";

  static m2(threshold) => "Aufgabennote unter ${threshold}";

  static m3(moduleName) => "Diese Aufgabe wird durch das Modul „${moduleName}“ gesperrt.";

  static m4(studentName, assignmentName) => "Betreffend: ${studentName}, Aufgabe – ${assignmentName}";

  static m5(points) => "${points} Pkte.";

  static m6(points) => "${points} Punkte";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} zu 1 anderen', other: '${authorName} zu ${howMany} anderen')}";

  static m8(authorName, recipientName) => "${authorName} bis ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} zu ${recipientName} & 1 anderen', other: '${authorName} zu ${recipientName} & ${howMany} anderen')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} von ${pointsPossible} Punkten";

  static m12(studentShortName) => "für ${studentShortName}";

  static m13(threshold) => "Kursnote über ${threshold}";

  static m14(threshold) => "Kursnote unter ${threshold}";

  static m15(date, time) => "Am ${date} um ${time}";

  static m16(canvasGuides, canvasSupport) => "Suchen Sie den Namen der Schule oder des Bezirks, z. B. „Private Kant-Schule“ oder „Goethe-Gymnasium“. Sie können auch direkt eine Canvas-Domäne eingeben, z. B. „kant.instructure.com“.\n\nUm weitere Informationen zum Auffinden des Canvas-Kontos Ihrer Institution zu erhalten, nutzen Sie die ${canvasGuides}, wenden Sie sich an den ${canvasSupport} oder an Ihre Schule.";

  static m17(date, time) => "Fällig am ${date} um ${time}";

  static m18(studentName, eventTitle) => "Betreffend: ${studentName}, Ereignis – ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Gesamtnote: ${grade}";

  static m21(studentName) => "Betreffend: ${studentName}, Frontseite";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Betreffend: ${studentName}, Noten";

  static m24(pointsLost) => "Strafe für Verspätung (-${pointsLost})";

  static m25(studentName, linkUrl) => "Betreffend: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Muss über ${percentage} liegen";

  static m27(percentage) => "Muss unter ${percentage} liegen";

  static m28(month) => "Nächster Monat: ${month}";

  static m29(date) => "Nächste Woche, beginnend am ${date}";

  static m30(query) => "Es konnte keine mit „${query}“ übereinstimmende Schule gefunden werden";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'von 1 Punkt', other: 'von ${points} Punkten')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} Punkte möglich";

  static m34(month) => "Vorheriger Monat: ${month}";

  static m35(date) => "Vorherige Woche, beginnend am ${date}";

  static m36(month) => "Monat ${month}";

  static m37(date, time) => "Diese Aufgabe wurde am ${date} um ${time} abgegeben und wartet auf die Benotung";

  static m38(studentName) => "Betreffend: ${studentName}, Kursplan";

  static m39(count) => "${count} ungelesen";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Eine Beschreibung ist erforderlich."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Ein Betreff ist erforderlich."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Teilnehmer hinzufügen"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Anhang hinzufügen"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Neuen Studenten hinzufügen"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Teilnehmer hinzufügen mit ..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Alarmeinstellungen"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Mich warnen, wenn …"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alle Benotungszeiträume"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Eine E-Mail-Adresse ist erforderlich."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Beim Versuch diesen Link anzuzeigen, ist ein Fehler aufgetreten:"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Es ist ein unerwarteter Fehler aufgetreten"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS-Version"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Aussehen"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Anwendungsversion"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Sie sind Student oder Lehrer?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Möchten Sie sich wirklich abmelden?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Möchten Sie diese Seite wirklich schließen? Die nicht gesendete Nachricht geht verloren."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Aufgabendetails"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Aufgabennote über"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Aufgabennote unter"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Aufgabe fehlt"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalender"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Abbrechen"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas-Student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas-Lehrer"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas bei GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Einen Kurs zur Benachrichtigung wählen"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Aus Galerie auswählen"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Fertigstellen"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Den Support kontaktieren"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Kursankündigung"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Kursankündigungen"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Kursnote über"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Kursnote unter"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Dunkel-Modus"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Löschen"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beschreibung"),
    "Device" : MessageLookupByLibrary.simpleMessage("Gerät"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Gerätemodell"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domäne:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fertig"),
    "Download" : MessageLookupByLibrary.simpleMessage("Herunterladen"),
    "Due" : MessageLookupByLibrary.simpleMessage("Fällig"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("ÄUSSERST KRITISCHER NOTFALL!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-Mail-Adresse"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-Mail:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Geben Sie den Studenten-Pairing-Code ein, den Sie erhalten haben. Falls der Pairing-Code nicht funktioniert, ist er möglicherweise abgelaufen"),
    "Event" : MessageLookupByLibrary.simpleMessage("Ereignis"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Entschuldigt"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Fehlgeschlagen. Für Optionen antippen."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtern nach"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Frontseite"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Vollständige Fehlermeldung"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Note"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Noten-Prozentsatz"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Benotet"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Noten"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hilfe"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Hochkontrastmodus"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Welche Auswirkung hat dies auf Sie?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Ich kann nicht weitermachen, bevor ich keine Antwort von euch habe."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Ich brauche Hilfe, aber es eilt nicht."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Ich habe Probleme bei der Anmeldung"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idee für Canvas-Parent-App [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Posteingang"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Posteingang Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Unvollständig"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institutsankündigung"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institutsankündigungen"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Anweisungen"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Scheinbar ein großartiger Tag für Ruhe, Entspannung und Energie tanken.."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Wie es aussieht, wurden in diesem Raum noch keine Aufgaben erstellt."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Nur zwanglos eine Frage, ein Kommentar, eine Idee, ein Vorschlag ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Verspätet"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Externes Tool starten"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Rechtliches"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Heller Modus"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Link-Fehler"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Umgebung:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Standort"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Gesperrt"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Abmelden"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Teilnehmer verwalten"),
    "Message" : MessageLookupByLibrary.simpleMessage("Nachricht"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Betreff der Nachricht"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Fehlt"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Muss unter 100 liegen"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Netzwerkfehler"),
    "Never" : MessageLookupByLibrary.simpleMessage("Nie"),
    "New message" : MessageLookupByLibrary.simpleMessage("Neue Nachricht"),
    "No" : MessageLookupByLibrary.simpleMessage("Nein"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Keine Benachrichtigungen"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Keine Aufgaben"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Keine Kurse"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Kein Abgabetermin"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Heute keine Ereignisse!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Keine Note"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Kein Standort angegeben"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Keine Studenten"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Kein Betreff"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Keine Übersicht"),
    "No description" : MessageLookupByLibrary.simpleMessage("Keine Beschreibung"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Es wurden keine Empfänger ausgewählt"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Unbenotet"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Nicht abgegeben"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Kein Elternteil?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Benachrichtigungen zu Erinnerungen über Zuweisungen und Kalenderereignisse"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("BS-Version"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Beobachter"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Eine unserer anderen Apps passt möglicherweise besser. Tippen Sie auf eine, um den Play Store zu besuchen."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Im Browser öffnen"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Mit einer anderen Anwendung öffnen"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Pairing-Code"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Vorbereiten ..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Frühere Anmeldungen"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Datenschutzrichtlinien"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Datenschutz, Nutzungsbedingungen, Open-Source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-Code"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Empfänger"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Erinnerung"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Erinnerungen"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Antworten"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Allen antworten"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Ein Problem melden"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Hilfe für Anmeldung erbitten"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Schaltfläche „Hilfe für Anmeldung erbitten“"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Anwendung neustarten"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Erneut versuchen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Zurück zur Anmeldung"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("STUDENT"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Empfänger auswählen"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Nachricht über diese Aufgabe senden"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Nachricht über diesen Kurs senden"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Nachricht senden"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Datum und Uhrzeit zur Benachrichtigung über dieses Ereignis einstellen."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Datum und Uhrzeit für die Benachrichtigung über diese spezifische Aufgabe einstellen."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Erinnerungsschalter einstellen"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Einstellungen"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Teilen Sie Ihre Liebe für die App anderen mit"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Irgend etwas ist kaputt. Aber ich kann auch ohne das fertigstellen, was ich noch machen muss."),
    "Student" : MessageLookupByLibrary.simpleMessage("Student"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Betreff"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Abgegeben"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Wurde abgegeben!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Übersicht"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Benutzer wechseln"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Kursplan"),
    "TA" : MessageLookupByLibrary.simpleMessage("Lehrassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LEHRER"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tippen Sie, um die Kurse, die Sie im Kalender sehen möchten, in die Favoritenliste aufzunehmen."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Zum Koppeln mit einem neuen Studenten antippen"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Zum Auswählen dieses Studenten antippen"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Zum Anzeigen des Studentenwählers antippen"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lehrer"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Sagen Sie uns, was Sie an der App besonders mögen"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Nutzungsbedingungen"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Die folgende Information hilft uns, Ihre Idee besser zu verstehen:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Der Server, den Sie eingegeben haben, hat keine Berechtigung für diese App."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Der Benutzeragent für diese App hat keine Berechtigung."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Design"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Diese Datei kann mit keiner installierten Anwendung geöffnet werden"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Es steht keine Seiteninformation zur Verfügung."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Es gab ein Problem beim Laden der Nutzungsbedingungen."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Beim Laden der Empfänger für diesen Kurs ist ein Fehler aufgetreten"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Beim Laden der Übersichtsdetails für diesen Kurs ist ein Fehler aufgetreten."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Beim Laden dieser Ankündigung ist ein Fehler aufgetreten"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Beim Laden dieses Gesprächs ist ein Fehler aufgetreten"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Beim Laden dieser Datei ist ein Fehler aufgetreten"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden Ihrer Posteingangsmeldungen."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden der Studentenalarme."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Fehler beim Laden Ihres Studentenkalenders"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden Ihrer Studenten."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden der Kurse Ihres Studenten."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Es gibt noch nichts, an das erinnert werden kann."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Keine Autorisierung, um diese App zu verwenden."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Für diesen Kurs gibt es noch keine Aufgaben oder Kalenderereignisse."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Diese Datei wird nicht unterstützt und kann mit der App nicht angezeigt werden"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Endnote"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Oh jeh!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kurse konnten nicht abgerufen werden. Bitte überprüfen Sie Ihre Verbindung, und versuchen Sie es erneut."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Dieses Bild kann nicht geladen werden"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Diese Mediendatei kann nicht wiedergegeben werden"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Die Nachricht kann nicht gesendet werden. Überprüfen Sie Ihre Verbindung, und versuchen Sie es erneut."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("In Bau"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Unbekannter Benutzer"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Nicht gespeicherte Änderungen"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Nichtunterstützte Datei"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Datei hochladen"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Kamera verwenden"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Benutzer-ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versionsnummer"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Fehlerdetails anzeigen"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Wir bauen derzeit an dieser Funktion für Sie."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Wir können diesen Link nicht anzeigen. Er könnte zu einer Institution gehören, bei der Sie derzeit nicht angemeldet sind."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Es wurden keine mit diesem Konto verknüpfte Studenten gefunden"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Der Server konnte für die Verwendung mit dieser App nicht verifiziert werden."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Wir sind nicht sicher, was passiert ist, aber es war nicht gut. Kontaktieren Sie uns, falls dies wieder passiert."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Sie beobachten keine Studenten."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Sie werden über diese Aufgabe benachrichtigt am …"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Sie werden benachrichtigt über dieses Ereignis am …"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Ihr Code ist falsch oder abgelaufen."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Die Kurse sind möglicherweise noch nicht veröffentlicht."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Sie werden alle erfasst!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Benachrichtigungen"),
    "appVersion" : m0,
    "assignmentGradeAboveThreshold" : m1,
    "assignmentGradeBelowThreshold" : m2,
    "assignmentLockedModule" : m3,
    "assignmentSubjectMessage" : m4,
    "assignmentTotalPoints" : m5,
    "assignmentTotalPointsAccessible" : m6,
    "authorToNOthers" : m7,
    "authorToRecipient" : m8,
    "authorToRecipientAndNOthers" : m9,
    "badgeNumberPlus" : m10,
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Kalender"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-Leitfäden"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-Logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas-Support"),
    "collapse" : MessageLookupByLibrary.simpleMessage("reduzieren"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("ausgeblendet"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kurse"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("Verwerfen"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Wie finde ich meine Schule oder meinen Bezirk?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Schulnamen oder Bezirk eingeben …"),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("erweitern"),
    "expanded" : MessageLookupByLibrary.simpleMessage("eingeblendet"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Schule suchen"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("Ich"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Weiter"),
    "nextMonth" : m28,
    "nextWeek" : m29,
    "noDomainResults" : m30,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m31,
    "plusRecipientCount" : m32,
    "pointsPossible" : m33,
    "previousMonth" : m34,
    "previousWeek" : m35,
    "selectedMonthLabel" : m36,
    "send" : MessageLookupByLibrary.simpleMessage("senden"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("ungelesen"),
    "unreadCount" : m39
  };
}
