// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a da locale. All the
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
  String get localeName => 'da';

  static m0(points) => "${points} point";

  static m1(courseName) => "${courseName} Opgaveliste";

  static m2(date, time) => "${date} kl. ${time}";

  static m3(date, time) => "Forfalder d. ${date} kl. ${time}";

  static m4(month) => "Næste måned: ${month}";

  static m5(date) => "Næste uge, der starter ${date}";

  static m6(points) => "${points} mulige point";

  static m7(month) => "Forrige måned: ${month}";

  static m8(date) => "Forrige uge, der starter ${date}";

  static m9(month) => "Måneden ${month}";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS-version"),
    "Application version" : MessageLookupByLibrary.simpleMessage("App-version"),
    "Are You Sure?" : MessageLookupByLibrary.simpleMessage("Er du sikker?"),
    "Are you sure you wish to close this page? Your unsaved changes will be lost." : MessageLookupByLibrary.simpleMessage("Er du sikker på, at du vil lukke denne side? Dine ikke-gemte ændringer vil gå tabt."),
    "Calendar" : MessageLookupByLibrary.simpleMessage("Kalender"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendere"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Annullér"),
    "Course (optional)" : MessageLookupByLibrary.simpleMessage("Fag (valgfrit)"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dato"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Slet"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beskrivelse"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Enhedsmodel"),
    "Do you want to delete this To Do item?" : MessageLookupByLibrary.simpleMessage("Vil du slette dette element på opgavelisten?"),
    "Done" : MessageLookupByLibrary.simpleMessage("Udført"),
    "Edit" : MessageLookupByLibrary.simpleMessage("Redigér"),
    "Edit To Do" : MessageLookupByLibrary.simpleMessage("Rediger opgavelisten"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Undskyldt"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Hel fejlmeddelelse"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Gå til I dag"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Bedømt"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Det er en alle tiders dag til at tage den med ro og slappe af."),
    "Missing" : MessageLookupByLibrary.simpleMessage("Mangler"),
    "New To Do" : MessageLookupByLibrary.simpleMessage("Nyt på opgavelisten"),
    "No" : MessageLookupByLibrary.simpleMessage("Nej"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Ingen kurser"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Ingen begivenheder i dag!"),
    "None" : MessageLookupByLibrary.simpleMessage("Ingen"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Prøv igen"),
    "Save" : MessageLookupByLibrary.simpleMessage("Gem"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Afleveret"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tryk for at få vist dine favoritfag i kalenderen."),
    "There was an error deleting this To Do. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under sletning af denne opgaveliste. Kontrollér forbindelsen, og prøv igen."),
    "There was an error loading your calendar" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af din kalender"),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsningen af din studerendes fag."),
    "There was an error saving this To Do. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved lagring af denne opgaveliste. Kontrollér forbindelsen, og prøv igen."),
    "There\'s no description yet" : MessageLookupByLibrary.simpleMessage("Der er ingen beskrivelse endnu"),
    "Title" : MessageLookupByLibrary.simpleMessage("Titel"),
    "Title must not be empty" : MessageLookupByLibrary.simpleMessage("Titel kan ikke være tom"),
    "To Do" : MessageLookupByLibrary.simpleMessage("Opgaveliste"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Åh ååh!"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Ikke-gemte ændringer"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Vis fejldetaljer"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Vi ved ikke helt, hvad der skete, men det var ikke godt. Kontakt os, hvis dette fortsætter."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Din studerendes fag kan muligvis ikke offentliggøres endnu."),
    "assignmentTotalPoints" : m0,
    "collapse" : MessageLookupByLibrary.simpleMessage("skjul"),
    "courseToDo" : m1,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Fag"),
    "dateAtTime" : m2,
    "dueDateAtTime" : m3,
    "expand" : MessageLookupByLibrary.simpleMessage("udvid"),
    "nextMonth" : m4,
    "nextWeek" : m5,
    "pointsPossible" : m6,
    "previousMonth" : m7,
    "previousWeek" : m8,
    "selectedMonthLabel" : m9
  };
}
