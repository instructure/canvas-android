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

  static m0(userName) => "Sie handeln als ${userName}.";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Aufgabennote über ${threshold}";

  static m3(threshold) => "Aufgabennote unter ${threshold}";

  static m4(moduleName) => "Diese Aufgabe wird durch das Modul „${moduleName}“ gesperrt.";

  static m5(studentName, assignmentName) => "Betreffend: ${studentName}, Aufgabe – ${assignmentName}";

  static m6(points) => "${points} Pkte.";

  static m7(points) => "${points} Punkte";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} zu 1 anderen', other: '${authorName} zu ${howMany} anderen')}";

  static m9(authorName, recipientName) => "${authorName} bis ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} zu ${recipientName} & 1 anderen', other: '${authorName} zu ${recipientName} & ${howMany} anderen')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Farbe ändern für ${studentName}";

  static m13(score, pointsPossible) => "${score} von ${pointsPossible} Punkten";

  static m14(studentShortName) => "für ${studentShortName}";

  static m15(threshold) => "Kursnote über ${threshold}";

  static m16(threshold) => "Kursnote unter ${threshold}";

  static m17(date, time) => "Am ${date} um ${time}";

  static m18(alertTitle) => "${alertTitle} verwerfen";

  static m19(canvasGuides, canvasSupport) => "Suchen Sie den Namen der Schule oder des Bezirks, z. B. „Private Kant-Schule“ oder „Goethe-Gymnasium“. Sie können auch direkt eine Canvas-Domäne eingeben, z. B. „kant.instructure.com“.\n\nUm weitere Informationen zum Auffinden des Canvas-Kontos Ihrer Institution zu erhalten, nutzen Sie die ${canvasGuides}, wenden Sie sich an den ${canvasSupport} oder an Ihre Schule.";

  static m20(date, time) => "Fällig am ${date} um ${time}";

  static m21(userName) => "Sie hören auf, zu handeln als ${userName} und werden abgemeldet.";

  static m22(userName) => "Sie hören auf, zu handeln als ${userName} und kehren zu Ihrem ursprünglichen Konto zurück.";

  static m23(studentName, eventTitle) => "Betreffend: ${studentName}, Ereignis – ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Gesamtnote: ${grade}";

  static m26(studentName) => "Betreffend: ${studentName}, Frontseite";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Betreffend: ${studentName}, Noten";

  static m29(pointsLost) => "Strafe für Verspätung (-${pointsLost})";

  static m30(studentName, linkUrl) => "Betreffend: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Muss über ${percentage} liegen";

  static m32(percentage) => "Muss unter ${percentage} liegen";

  static m33(month) => "Nächster Monat: ${month}";

  static m34(date) => "Nächste Woche, beginnend am ${date}";

  static m35(query) => "Es konnte keine mit „${query}“ übereinstimmende Schule gefunden werden";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'von 1 Punkt', other: 'von ${points} Punkten')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} Punkte möglich";

  static m39(month) => "Vorheriger Monat: ${month}";

  static m40(date) => "Vorherige Woche, beginnend am ${date}";

  static m41(termsOfService, privacyPolicy) => "Durch Tippen auf „Konto erstellen“ erklären Sie sich mit den ${termsOfService} und den ${privacyPolicy} einverstanden.";

  static m42(version) => "Vorschläge für Android - Canvas Parent ${version}";

  static m43(month) => "Monat ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} Stern', other: '${position} Sterne')}";

  static m45(date, time) => "Diese Aufgabe wurde am ${date} um ${time} abgegeben und wartet auf die Benotung";

  static m46(studentName) => "Betreffend: ${studentName}, Kursplan";

  static m47(count) => "${count} ungelesen";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("„Handeln als“ heißt im Grunde sich als dieser Benutzer ohne Kennwort die anzumelden. Sie können beliebige Maßnahmen ergreifen, als wären Sie dieser Benutzer, und aus der Sicht anderer Benutzer wird es sein, als ob sie dieser Benutzer ausführt. Die Audit-Protokolle zeichnen auf, dass Sie die Aktionen im Namen des Benutzers durchgeführt haben."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Eine Beschreibung ist erforderlich."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Beim Hinzufügen dieses Studenten ist ein Netzwerkfehler aufgetreten. Überprüfen Sie Ihre Verbindung, und versuchen Sie es erneut."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Ein Betreff ist erforderlich."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Handeln als Benutzer"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Teilnehmer hinzufügen"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Anhang hinzufügen"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Neuen Studenten hinzufügen"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Teilnehmer hinzufügen mit ..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Alarmeinstellungen"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Mich warnen, wenn …"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alle Benotungszeiträume"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Sie haben bereits ein Konto? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Eine E-Mail-Adresse ist erforderlich."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Beim Versuch diesen Link anzuzeigen, ist ein Fehler aufgetreten:"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Fehler beim Speichern Ihrer Auswahl. Bitte versuchen Sie es noch einmal."),
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
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Fuchsienrot"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalender"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Kameraberechtigung"),
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
    "Create Account" : MessageLookupByLibrary.simpleMessage("Konto erstellen"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Dunkel-Modus"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Löschen"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beschreibung"),
    "Device" : MessageLookupByLibrary.simpleMessage("Gerät"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Gerätemodell"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domäne"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domäne:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Nicht nochmal zeigen"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fertig"),
    "Download" : MessageLookupByLibrary.simpleMessage("Herunterladen"),
    "Due" : MessageLookupByLibrary.simpleMessage("Fällig"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("ÄUSSERST KRITISCHER NOTFALL!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elektrisch, blau"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-Mail-Adresse"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-Mail:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-Mail ..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Geben Sie den Studenten-Pairing-Code ein, den Sie erhalten haben. Falls der Pairing-Code nicht funktioniert, ist er möglicherweise abgelaufen"),
    "Event" : MessageLookupByLibrary.simpleMessage("Ereignis"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Entschuldigt"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Abgelaufener QR-Code"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Fehlgeschlagen. Für Optionen antippen."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtern nach"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Feuerrot, orange"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Frontseite"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Vollständiger Name"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Vollständiger Name ..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Vollständige Fehlermeldung"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Zu „heute“ gehen"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Note"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Noten-Prozentsatz"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Benotet"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Noten"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hilfe"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Kennwort ausblenden"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Hochkontrastmodus"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Wie sieht\'s aus?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Welche Auswirkung hat dies auf Sie?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Ich kann nicht weitermachen, bevor ich keine Antwort von euch habe."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Ich besitze kein Canvas-Konto"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Ich besitze ein Canvas-Konto"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Ich brauche Hilfe, aber es eilt nicht."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Ich habe Probleme bei der Anmeldung"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idee für Canvas-Parent-App [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Um Ihnen ein besseres Erlebnis zu bieten, haben wir die Funktionsweise von Erinnerungen aktualisiert. Sie können neue Erinnerungen hinzufügen, indem Sie sich eine Aufgabe oder ein Kalenderereignis ansehen und auf den Schalter unter dem Abschnitt „Mich erinnern“ tippen.\n\nBeachten Sie, dass alle Erinnerungen, die mit älteren Versionen dieser App erstellt wurden, nicht mit den neuen Änderungen kompatibel sind und neu erstellt werden müssen."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Posteingang"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Posteingang Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Unvollständig"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Falsche Domain"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institutsankündigung"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institutsankündigungen"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Anweisungen"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interaktionen auf dieser Seite sind durch ihre Institution eingeschränkt."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Ungültiger QR-Code"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Scheinbar ein großartiger Tag für Ruhe, Entspannung und Energie tanken.."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Wie es aussieht, wurden in diesem Raum noch keine Aufgaben erstellt."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Nur zwanglos eine Frage, ein Kommentar, eine Idee, ein Vorschlag ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Verspätet"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Externes Tool starten"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Rechtliches"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Heller Modus"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Link-Fehler"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Umgebung:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("QR-Code finden"),
    "Location" : MessageLookupByLibrary.simpleMessage("Standort"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Gesperrt"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Abmelden"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Anmeldung Flow: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Anmeldung Flow: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Anmeldung Flow: Website-Administrator"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Anmeldung Flow: Mobile Prüfung überspringen"),
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
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student öffnen"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Im Browser öffnen"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Mit einer anderen Anwendung öffnen"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Pairing-Code"),
    "Password" : MessageLookupByLibrary.simpleMessage("Kennwort"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Passwort ist erforderlich"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Das Kennwort muss mindestens 8 Zeichen lang sein."),
    "Password…" : MessageLookupByLibrary.simpleMessage("Kennwort ..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Planerhinweis"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Geben Sie bitte eine gültige E-Mail-Adresse sein."),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Bitte geben Sie eine E-Mail-Adresse an."),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Bitte geben Sie Ihren vollständigen Namen ein."),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Bitte scannen Sie einen von Canvas generierten QR-Code"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Pflaume, violett"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Vorbereiten ..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Frühere Anmeldungen"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Datenschutzrichtlinien"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Datenschutzrichtlinien (Link)"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Datenschutz, Nutzungsbedingungen, Open-Source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-Code"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("QR-Scan erfordert Zugriff auf die Kamera"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Himbeerrot"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Empfänger"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Aktualisieren"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Erinnerung"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Erinnerungen"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Erinnerungen haben sich geändert!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Antworten"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Allen antworten"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Ein Problem melden"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Hilfe für Anmeldung erbitten"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Schaltfläche „Hilfe für Anmeldung erbitten“"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Anwendung neustarten"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Erneut versuchen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Zurück zur Anmeldung"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("STUDENT"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Screenshot mit der Position der QR-Code-Generierung im Browser"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Screenshot des Ortes der Erzeugung des Pairing-QR-Codes in der Canvas Studenten-App"),
    "Select" : MessageLookupByLibrary.simpleMessage("Auswählen"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Studentenfarbe auswählen"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Empfänger auswählen"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Feedback senden"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Nachricht über diese Aufgabe senden"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Nachricht über diesen Kurs senden"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Nachricht senden"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Datum und Uhrzeit zur Benachrichtigung über dieses Ereignis einstellen."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Datum und Uhrzeit für die Benachrichtigung über diese spezifische Aufgabe einstellen."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Einstellungen"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Kleeblatt, grün"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Teilen Sie Ihre Liebe für die App anderen mit"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Kennwort anzeigen"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Anmelden"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Beim Erstellen Ihres Kontos ging etwas schief. Wenden Sie sich bitte an Ihre Schule."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Irgend etwas ist kaputt. Aber ich kann auch ohne das fertigstellen, was ich noch machen muss."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Nicht mehr handeln als Benutzer"),
    "Student" : MessageLookupByLibrary.simpleMessage("Student"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Studenten-Pairing"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Studenten können einen QR-Code mit der Canvas Studenten-App auf ihrem Mobilgerät erstellen"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Studenten können einen Pairing-Code über die Canvas-Website erhalten"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Betreff"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Abgegeben"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Wurde abgegeben!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Übersicht"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Benutzer wechseln"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Kursplan"),
    "TA" : MessageLookupByLibrary.simpleMessage("Lehrassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LEHRER"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tippen Sie, um die Kurse, die Sie im Kalender sehen möchten, in die Favoritenliste aufzunehmen. Wählen Sie bis zu 10"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Zum Koppeln mit einem neuen Studenten antippen"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Zum Auswählen dieses Studenten antippen"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Zum Anzeigen des Studentenwählers antippen"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lehrer"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Sagen Sie uns, was Sie an der App besonders mögen"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Servicebedingungen"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Servicebedingungen (Link)"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Nutzungsbedingungen"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Der von Ihnen gescannte QR-Code ist möglicherweise abgelaufen. Aktualisieren Sie den Code auf dem Gerät des Studenten und versuchen Sie es erneut."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Die folgende Information hilft uns, Ihre Idee besser zu verstehen:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Der Server, den Sie eingegeben haben, hat keine Berechtigung für diese App."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Der Student, den Sie hinzufügen möchten, gehört zu einer anderen Schule. Melden Sie sich an oder erstellen Sie ein Konto bei dieser Einrichtung, um diesen Code zu scannen."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Der Benutzeragent für diese App hat keine Berechtigung."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Design"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Diese Datei kann mit keiner installierten Anwendung geöffnet werden"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Es steht keine Seiteninformation zur Verfügung."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Es gab ein Problem beim Laden der Nutzungsbedingungen."),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Beim Entfernen des Studenten aus Ihrem Konto ist ein Problem aufgetreten. Bitte überprüfen Sie Ihre Verbindung, und versuchen Sie es erneut."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Beim Laden der Empfänger für diesen Kurs ist ein Fehler aufgetreten"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Beim Laden der Übersichtsdetails für diesen Kurs ist ein Fehler aufgetreten."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Beim Laden dieser Ankündigung ist ein Fehler aufgetreten"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Beim Laden dieses Gesprächs ist ein Fehler aufgetreten"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Beim Laden dieser Datei ist ein Fehler aufgetreten"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden Ihrer Posteingangsmeldungen."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden der Studentenalarme."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Fehler beim Laden Ihres Studentenkalenders"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden Ihrer Studenten."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden der Kurse Ihres Studenten."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Es gab einen Fehler bei der Anmeldung. Bitte generieren Sie einen weiteren QR-Code und versuchen Sie es erneut."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Beim Handeln als dieser Benutzer ist ein Fehler aufgetreten. Bitte überprüfen Sie die Domäne und die Benutzer-ID, und versuchen Sie es erneut."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Es gibt noch nichts, an das erinnert werden kann."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Keine Autorisierung, um diese App zu verwenden."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Für diesen Kurs gibt es noch keine Aufgaben oder Kalenderereignisse."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Diese Datei wird nicht unterstützt und kann mit der App nicht angezeigt werden"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Dadurch werden alle Pairings für Einschreibungen für diesen Studenten aufgehoben und die Einschreibungen werden von Ihrem Konto entfernt."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Endnote"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Oh je!"),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Dunkles Design für Web-Inhalte verwenden"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Benutzer-ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Benutzer-ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versionsnummer"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Beschreibung anzeigen"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Fehlerdetails anzeigen"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Datenschutzrichtlinien anzeigen"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Wir bauen derzeit an dieser Funktion für Sie."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Wir können diesen Link nicht anzeigen. Er könnte zu einer Institution gehören, bei der Sie derzeit nicht angemeldet sind."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Es wurden keine mit diesem Konto verknüpfte Studenten gefunden"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Der Server konnte für die Verwendung mit dieser App nicht verifiziert werden."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Wir sind nicht sicher, was passiert ist, aber es war nicht gut. Kontaktieren Sie uns, falls dies wieder passiert."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Was können wir besser machen?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Sie beobachten keine Studenten."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Sie dürfen nur max. 10 Kalender für die Anzeige auswählen"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Sie müssen eine Benutzer-ID eingeben"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Sie müssen eine gültige Domäne eingeben"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Sie müssen mindestens einen Kalender anzeigen"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Sie werden über diese Aufgabe benachrichtigt am …"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Sie werden benachrichtigt über dieses Ereignis am …"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Sie finden den QR-Code im Internet in Ihrem Kontoprofil. Klicken Sie in der Liste auf \'QR für mobiles Login\'."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Um fortzufahren, müssen Sie die Canvas Studenten-App Ihres Studenten öffnen. Gehen Sie zu Hauptmenü > Einstellungen > Pairing mit Beobachter und scannen Sie den QR-Code, den Sie dort sehen."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Ihr Code ist falsch oder abgelaufen."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Die Kurse sind möglicherweise noch nicht veröffentlicht."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Sie werden alle erfasst!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Benachrichtigungen"),
    "appVersion" : m1,
    "assignmentGradeAboveThreshold" : m2,
    "assignmentGradeBelowThreshold" : m3,
    "assignmentLockedModule" : m4,
    "assignmentSubjectMessage" : m5,
    "assignmentTotalPoints" : m6,
    "assignmentTotalPointsAccessible" : m7,
    "authorToNOthers" : m8,
    "authorToRecipient" : m9,
    "authorToRecipientAndNOthers" : m10,
    "badgeNumberPlus" : m11,
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Kalender"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-Leitfäden"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-Logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas-Support"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("reduzieren"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("reduziert"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kurse"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Wie finde ich meine Schule oder meinen Bezirk?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Schulnamen oder Bezirk eingeben …"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("erweitern"),
    "expanded" : MessageLookupByLibrary.simpleMessage("erweitert"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Schule suchen"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("Ich"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Weiter"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("senden"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("ungelesen"),
    "unreadCount" : m47
  };
}
