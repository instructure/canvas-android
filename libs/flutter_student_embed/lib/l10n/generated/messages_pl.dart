// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a pl locale. All the
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
  String get localeName => 'pl';

  static m0(points) => "${points} pkt";

  static m1(courseName) => "Lista zadań ${courseName}";

  static m2(date, time) => "${date} o ${time}";

  static m3(date, time) => "Termin ${date} o ${time}";

  static m4(month) => "Następny miesiąc: ${month}";

  static m5(date) => "Następny tydzień od ${date}";

  static m6(points) => "${points} pkt do zdobycia";

  static m7(month) => "Poprzedni miesiąc: ${month}";

  static m8(date) => "Poprzedni tydzień od ${date}";

  static m9(month) => "Miesiąc: ${month}";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Wersja systemu Android"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Wersja aplikacji"),
    "Are You Sure?" : MessageLookupByLibrary.simpleMessage("Czy na pewno?"),
    "Are you sure you wish to close this page? Your unsaved changes will be lost." : MessageLookupByLibrary.simpleMessage("Czy na pewno chcesz zamknąć tę stronę? Niezapisane zmiany zostaną utracone."),
    "Calendar" : MessageLookupByLibrary.simpleMessage("Kalendarz"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendarze"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Anuluj"),
    "Course (optional)" : MessageLookupByLibrary.simpleMessage("Kurs (opcjonalnie)"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Usuń"),
    "Description" : MessageLookupByLibrary.simpleMessage("Opis"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Model urządzenia"),
    "Do you want to delete this To Do item?" : MessageLookupByLibrary.simpleMessage("Czy na pewno chcesz usunąć ten element listy zadań?"),
    "Done" : MessageLookupByLibrary.simpleMessage("Gotowe"),
    "Edit" : MessageLookupByLibrary.simpleMessage("Edytuj"),
    "Edit To Do" : MessageLookupByLibrary.simpleMessage("Edytuj listę zadań"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Usprawiedliwiony"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Pełny komunikat o błędzie"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Przejdź do dziś"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Oceniono"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Wygląda to na świetny dzień do odpoczynku, relaksu i regeneracji."),
    "Missing" : MessageLookupByLibrary.simpleMessage("Brak"),
    "New To Do" : MessageLookupByLibrary.simpleMessage("Nowa lista zadań"),
    "No" : MessageLookupByLibrary.simpleMessage("Nie"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Brak kursów"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Brak wydarzeń na dziś!"),
    "None" : MessageLookupByLibrary.simpleMessage("Brak"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Ponów próbę"),
    "Save" : MessageLookupByLibrary.simpleMessage("Zapisz"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Przesłano"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Stuknij, aby dodać do ulubionych kursy, które chcesz wyświetlić w Kalendarzu."),
    "There was an error deleting this To Do. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas usuwania tej listy zadań. Sprawdź połączenie i spróbuj ponownie."),
    "There was an error loading your calendar" : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania kalendarza"),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania kursów uczestnika."),
    "There was an error saving this To Do. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas zapisywania tej listy zadań. Sprawdź połączenie i spróbuj ponownie."),
    "There\'s no description yet" : MessageLookupByLibrary.simpleMessage("Brak opisu"),
    "Title" : MessageLookupByLibrary.simpleMessage("Tytuł"),
    "Title must not be empty" : MessageLookupByLibrary.simpleMessage("Tytuł nie może być pusty"),
    "To Do" : MessageLookupByLibrary.simpleMessage("Lista zadań"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("O, nie!"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Niezapisane zmiany"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Wyświetl szczegóły błędu"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nie mamy pewności, co się wydarzyło, ale nie było to dobre. Jeśli to będzie się powtarzać, skontaktuj się z nami."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Tak"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Być może kursy uczestnika nie zostały jeszcze opublikowane."),
    "assignmentTotalPoints" : m0,
    "collapse" : MessageLookupByLibrary.simpleMessage("zwiń"),
    "courseToDo" : m1,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kursy"),
    "dateAtTime" : m2,
    "dueDateAtTime" : m3,
    "expand" : MessageLookupByLibrary.simpleMessage("rozwiń"),
    "nextMonth" : m4,
    "nextWeek" : m5,
    "pointsPossible" : m6,
    "previousMonth" : m7,
    "previousWeek" : m8,
    "selectedMonthLabel" : m9
  };
}
