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

  static m0(points) => "${points} Pkte.";

  static m1(courseName) => "Aufgabe für ${courseName}";

  static m2(date, time) => "Am ${date} um ${time}";

  static m3(date, time) => "Fällig am ${date} um ${time}";

  static m4(month) => "Nächster Monat: ${month}";

  static m5(date) => "Nächste Woche, beginnend am ${date}";

  static m6(points) => "${points} Punkte möglich";

  static m7(month) => "Vorheriger Monat: ${month}";

  static m8(date) => "Vorherige Woche, beginnend am ${date}";

  static m9(month) => "Monat ${month}";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS-Version"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Anwendungsversion"),
    "Are You Sure?" : MessageLookupByLibrary.simpleMessage("Sind Sie sicher?"),
    "Are you sure you wish to close this page? Your unsaved changes will be lost." : MessageLookupByLibrary.simpleMessage("Möchten Sie diese Seite wirklich schließen? Alle nicht gespeicherten Daten gehen verloren."),
    "Calendar" : MessageLookupByLibrary.simpleMessage("Kalender"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalender"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Abbrechen"),
    "Course (optional)" : MessageLookupByLibrary.simpleMessage("Kurs (optional)"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Löschen"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beschreibung"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Gerätemodell"),
    "Do you want to delete this To Do item?" : MessageLookupByLibrary.simpleMessage("Möchten Sie diese Aufgabe wirklich löschen?"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fertig"),
    "Edit" : MessageLookupByLibrary.simpleMessage("Ändern"),
    "Edit To Do" : MessageLookupByLibrary.simpleMessage("Aufgabe bearbeiten"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Entschuldigt"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Vollständige Fehlermeldung"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Zu „heute“ gehen"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Benotet"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Scheinbar ein großartiger Tag für Ruhe, Entspannung und Energie tanken.."),
    "Missing" : MessageLookupByLibrary.simpleMessage("Fehlt"),
    "New To Do" : MessageLookupByLibrary.simpleMessage("Neue Aufgabe"),
    "No" : MessageLookupByLibrary.simpleMessage("Nein"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Keine Kurse"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Heute keine Ereignisse!"),
    "None" : MessageLookupByLibrary.simpleMessage("Keine"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Erneut versuchen"),
    "Save" : MessageLookupByLibrary.simpleMessage("Speichern"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Abgegeben"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tippen Sie, um die Kurse, die Sie im Kalender sehen möchten, in die Favoritenliste aufzunehmen."),
    "There was an error deleting this To Do. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Fehler beim Löschen dieser Aufgabe. Bitte überprüfen Sie Ihre Verbindung, und versuchen Sie es erneut."),
    "There was an error loading your calendar" : MessageLookupByLibrary.simpleMessage("Fehler beim Laden Ihres Kalenders"),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Fehler beim Laden der Kurse Ihres Studenten."),
    "There was an error saving this To Do. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Fehler beim Speichern dieser Aufgabe Bitte überprüfen Sie Ihre Verbindung, und versuchen Sie es erneut."),
    "There\'s no description yet" : MessageLookupByLibrary.simpleMessage("Es ist noch keine Beschreibung vorhanden"),
    "Title" : MessageLookupByLibrary.simpleMessage("Titel"),
    "Title must not be empty" : MessageLookupByLibrary.simpleMessage("Titel darf nicht leer sein"),
    "To Do" : MessageLookupByLibrary.simpleMessage("Zu erledigen"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Oh je!"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Nicht gespeicherte Änderungen"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Fehlerdetails anzeigen"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Wir sind nicht sicher, was passiert ist, aber es war nicht gut. Kontaktieren Sie uns, falls dies wieder passiert."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Die Kurse sind möglicherweise noch nicht veröffentlicht."),
    "assignmentTotalPoints" : m0,
    "collapse" : MessageLookupByLibrary.simpleMessage("reduzieren"),
    "courseToDo" : m1,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kurse"),
    "dateAtTime" : m2,
    "dueDateAtTime" : m3,
    "expand" : MessageLookupByLibrary.simpleMessage("erweitern"),
    "nextMonth" : m4,
    "nextWeek" : m5,
    "pointsPossible" : m6,
    "previousMonth" : m7,
    "previousWeek" : m8,
    "selectedMonthLabel" : m9
  };
}
