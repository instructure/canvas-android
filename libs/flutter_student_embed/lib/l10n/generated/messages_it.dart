// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a it locale. All the
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
  String get localeName => 'it';

  static m0(points) => "${points} pt.";

  static m1(courseName) => "${courseName} Elenco azioni";

  static m2(date, time) => "Il ${date} alle ${time}";

  static m3(date, time) => "Scade il ${date} alle ${time}";

  static m4(month) => "Prossimo mese: ${month}";

  static m5(date) => "Prossima settimana a partire da ${date}";

  static m6(points) => "${points} punti possibili";

  static m7(month) => "Mese precedente: ${month}";

  static m8(date) => "Settimana precedente a partire da ${date}";

  static m9(month) => "Mese di ${month}";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Versione SO Android"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Versione applicazione"),
    "Are You Sure?" : MessageLookupByLibrary.simpleMessage("Continuare?"),
    "Are you sure you wish to close this page? Your unsaved changes will be lost." : MessageLookupByLibrary.simpleMessage("Vuoi chiudere questa pagina? Le modifiche non salvate saranno perse."),
    "Calendar" : MessageLookupByLibrary.simpleMessage("Calendario"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendari"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Annulla"),
    "Course (optional)" : MessageLookupByLibrary.simpleMessage("Corso (opzionale)"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Elimina"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descrizione"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modello dispositivo"),
    "Do you want to delete this To Do item?" : MessageLookupByLibrary.simpleMessage("Vuoi eliminare questo elemento dall’elenco azioni?"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fatto"),
    "Edit" : MessageLookupByLibrary.simpleMessage("Modifica"),
    "Edit To Do" : MessageLookupByLibrary.simpleMessage("Modifica elenco azioni"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Giustificato"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Messaggio di errore pieno"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Vai a oggi"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Valutato"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Ottima occasione per riposarsi, rilassarsi e ricaricare le batterie."),
    "Missing" : MessageLookupByLibrary.simpleMessage("Mancante"),
    "New To Do" : MessageLookupByLibrary.simpleMessage("Nuovo elenco azioni"),
    "No" : MessageLookupByLibrary.simpleMessage("No"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Nessun corso"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Nessun evento oggi!"),
    "None" : MessageLookupByLibrary.simpleMessage("Nessuno"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Riprova"),
    "Save" : MessageLookupByLibrary.simpleMessage("Salva"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Inviato"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tocca per mettere nei preferiti i corsi che vuoi vedere sul calendario."),
    "There was an error deleting this To Do. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore di eliminazione di questo elenco azioni. Verificare la connessione e riprovare."),
    "There was an error loading your calendar" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento del tuo calendario"),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei corsi dello studente."),
    "There was an error saving this To Do. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il salvataggio dell’elenco azioni. Verificare la connessione e riprovare."),
    "There\'s no description yet" : MessageLookupByLibrary.simpleMessage("Non c’è ancora nessuna descrizione"),
    "Title" : MessageLookupByLibrary.simpleMessage("Titolo"),
    "Title must not be empty" : MessageLookupByLibrary.simpleMessage("Il titolo non dev’essere vuoto"),
    "To Do" : MessageLookupByLibrary.simpleMessage("Elenco attività"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Spiacenti."),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Modifiche non salvate"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visualizza dettagli errori"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Non siamo sicuri di cos’è successo, ma non è stata una cosa positiva. Contattaci se continua a succedere."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sì"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("I corsi dello studente potrebbero non essere stati ancora pubblicati."),
    "assignmentTotalPoints" : m0,
    "collapse" : MessageLookupByLibrary.simpleMessage("comprimi"),
    "courseToDo" : m1,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Corsi"),
    "dateAtTime" : m2,
    "dueDateAtTime" : m3,
    "expand" : MessageLookupByLibrary.simpleMessage("estendi"),
    "nextMonth" : m4,
    "nextWeek" : m5,
    "pointsPossible" : m6,
    "previousMonth" : m7,
    "previousWeek" : m8,
    "selectedMonthLabel" : m9
  };
}
