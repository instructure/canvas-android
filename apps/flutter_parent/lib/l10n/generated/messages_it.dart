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

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Voto compito sopra ${threshold}";

  static m2(threshold) => "Voto compito sotto ${threshold}";

  static m3(moduleName) => "Questo compito è bloccato dal modulo  \"${moduleName}\".";

  static m4(studentName, assignmentName) => "Su: ${studentName}, Compito - ${assignmentName}";

  static m5(points) => "${points} pt.";

  static m6(points) => "${points} punti";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} a 1 altro', other: '${authorName} ad altri ${howMany}')}";

  static m8(authorName, recipientName) => "${authorName} a ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} a ${recipientName} e 1 altro', other: '${authorName} a ${recipientName} e altri ${howMany}')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} su ${pointsPossible} punti";

  static m12(studentShortName) => "per ${studentShortName}";

  static m13(threshold) => "Voto corso sopra ${threshold}";

  static m14(threshold) => "Voto corso sotto ${threshold}";

  static m15(date, time) => "Il ${date} alle ${time}";

  static m16(canvasGuides, canvasSupport) => "Prova a cercare il nome della scuola o del distretto a cui stai tentando di accedere, ad esempio “Scuola privata Rossi” o “Scuole statali Rossi”. Puoi anche entrare direttamente in un dominio Canvas, ad esempio “rossi.instructure.com.”\n\nPer ulteriori informazioni su come trovare l’account Canvas del tuo istituto, puoi visitare le ${canvasGuides}, contattare l’${canvasSupport} o la scuola per assistenza.";

  static m17(date, time) => "Scade il ${date} alle ${time}";

  static m18(studentName, eventTitle) => "Su: ${studentName}, Evento - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Voto finale: ${grade}";

  static m21(studentName) => "Su: ${studentName}, Pagina iniziale";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Su: ${studentName}, Voti";

  static m24(pointsLost) => "Penale ritardo (-${pointsLost})";

  static m25(studentName, linkUrl) => "Su: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Deve essere sopra ${percentage}";

  static m27(percentage) => "Deve essere sotto ${percentage}";

  static m28(month) => "Prossimo mese: ${month}";

  static m29(date) => "Prossima settimana a partire da ${date}";

  static m30(query) => "Impossibile trovare delle scuole corrispondenti a \"${query}\"";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Di 1 punto', other: 'Di ${points} punti')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} punti possibili";

  static m34(month) => "Mese precedente: ${month}";

  static m35(date) => "Settimana precedente a partire da ${date}";

  static m36(month) => "Mese di ${month}";

  static m37(date, time) => "Questo compito è stato inviato il ${date} alle ${time} ed è in attesa della valutazione";

  static m38(studentName) => "Su: ${studentName}, Piano di studio";

  static m39(count) => "${count} non letto";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("La descrizione è obbligatoria."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("L’argomento è obbligatorio."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Aggiungi studente"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Aggiungi allegato"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Aggiungi nuovo studente"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Aggiungi studente con…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Impostazioni avviso"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Avvisami quando…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Tutti i periodi di valutazione"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("L’indirizzo e-mail è obbligatorio."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il tentativo di visualizzare questo link"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Si verificato un errore imprevisto"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Versione SO Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Aspetto"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Versione applicazione"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Sei uno studente o un insegnante?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Disconnettersi?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Vuoi chiudere questa pagina? Il messaggio non inviato andrà perso."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Dettagli compiti"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Voto compito sopra"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Voto compito sotto"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Compito mancante"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendari"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Annulla"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Studente Canvas"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Insegnante Canvas"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas su GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Scegli un corso da messaggiare"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Scegli da galleria"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Completa"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Contatta l’assistenza"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Annuncio corso"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Annunci corso"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Voto corso sopra"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Voto corso sotto"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Modalità Scura"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Elimina"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descrizione"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositivo"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modello dispositivo"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Dominio:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fatto"),
    "Download" : MessageLookupByLibrary.simpleMessage("Download"),
    "Due" : MessageLookupByLibrary.simpleMessage("Scadenza"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGENZA CRITICA."),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Indirizzo e-mail"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-mail:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Inserisci il codice accoppiamento studente fornito. Se il codice accoppiamento non funziona, potrebbe essere scaduto"),
    "Event" : MessageLookupByLibrary.simpleMessage("Evento"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Giustificato"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Non riuscito. Tocca per le opzioni."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtra"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtra per"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Pagina iniziale"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Messaggio di errore pieno"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Voto"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Percentuale voto"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Valutato"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Voti"),
    "Help" : MessageLookupByLibrary.simpleMessage("Guida"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modalità alto contrasto"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Quali sono le ripercussioni per te?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Non riesco a terminare il lavoro finché non ricevo una tua risposta."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Ho bisogno di aiuto ma non è urgente."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Ho dei problemi di login"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idea per l’app Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Posta in arrivo"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Zero in posta in arrivo"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Non completato"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Annuncio istituto"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Annunci istituto"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Istruzioni"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Ottima occasione per riposarsi, rilassarsi e ricaricare le batterie."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Sembra che i compiti non siano ancora stati creati in questo spazio."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Solo una domanda casuale, un commento, un\'idea, un suggerimento…"),
    "Late" : MessageLookupByLibrary.simpleMessage("In ritardo"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Avvia strumento esterno"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legale"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modalità chiara"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Errore link"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Impostazioni internazionali:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Posizione"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Bloccato"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Disconnetti"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Gestisci studenti"),
    "Message" : MessageLookupByLibrary.simpleMessage("Messaggio"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Oggetto del messaggio"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Mancante"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Deve essere sotto 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Errore di rete"),
    "Never" : MessageLookupByLibrary.simpleMessage("Mai"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nuovo messaggio"),
    "No" : MessageLookupByLibrary.simpleMessage("No"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Nessun avviso"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Nessun compito"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Nessun corso"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Nessuna data di scadenza"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Nessun evento oggi!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Nessun voto"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Nessuna posizione specificata"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Nessuno studente"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Nessun oggetto"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Nessun riepilogo"),
    "No description" : MessageLookupByLibrary.simpleMessage("Nessuna descrizione"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Nessun destinatario selezionato"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Non valutato"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Non inviato"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Non è principale?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notifiche per i promemoria sui compiti e gli eventi di calendario"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Versione SO"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Osservatore"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Una delle altre nostre app potrebbe essere la scelta migliore. Toccane una per visitare Play Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Apri nel browser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Apri con un’altra app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Codice accoppiamento"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Preparazione in corso..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Login precedenti"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Informativa sulla privacy"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Informativa sulla privacy, termini di utilizzo, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Codice QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinatari"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Promemoria"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Promemoria"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Rispondi"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Rispondi a tutti"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Segnala un problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Richiedi aiuto per login"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Pulsante Richiedi aiuto per login"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Riavvia app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Riprova"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Torna alla pagina di login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("STUDENTE"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Seleziona destinatari"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Invia un messaggio su questo compito"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Invia un messaggio su questo corso"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Invia messaggio"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Imposta una data e un’ora per ricevere la notifica su questo evento."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Imposta una data e un’ora per ricevere la notifica su questo compito specifico."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Imposta commutatore promemoria"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Impostazioni"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Condividi cosa ti piace dell’app"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Si è verificato un problema ma posso aggirarlo e fare ciò che devo."),
    "Student" : MessageLookupByLibrary.simpleMessage("Studente"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Oggetto"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Inviato"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Inviato correttamente!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Riepilogo"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Cambia studenti"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Piano di studio"),
    "TA" : MessageLookupByLibrary.simpleMessage("Assistente"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("INSEGNANTE"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tocca per mettere nei preferiti i corsi che vuoi vedere sul calendario."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tocca per accoppiare con un nuovo studente"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tocca per selezionare questo studente"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tocca per mostrare il selettore studente"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Insegnante"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Raccontaci quali sono le tue parti preferite dell’app"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Termini di utilizzo"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Le seguenti informazioni ci aiutano a comprendere meglio la tua idea:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Il server in cui sei entrato non è autorizzato per questa app."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("L’agente utente per questa app non è autorizzata."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Non ci sono applicazioni installate per aprire questo file"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Non ci sono informazioni pagina disponibili."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Si è verificato un problema durante il caricamento delle Condizioni d’uso"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei destinatari per questo corso"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei dettagli di riepilogo per questo corso."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento di questo annuncio"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento di questa conversazione"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento di questo file"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei messaggi di posta in arrivo."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento degli avvisi dello studente."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore di caricamento del calendario del tuo studente"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei tuoi studenti."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei corsi dello studente."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Non c’è ancora nulla su cui ricevere delle notifiche."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Questa app non è autorizzata per l’uso."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Questo corso non ha ancora alcun compito o eventi di calendario."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Questo file non è supportato e non può essere visualizzato attraverso l’app"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Voto totale"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Spiacenti."),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Impossibile recuperare i corsi. Verificare la connessione e riprovare."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Impossibile caricare questa immagine"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Impossibile riprodurre questo file multimediale"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Impossibile inviare il messaggio. Verifica la tua connessione e riprova."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("In costruzione"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Utente sconosciuto"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Modifiche non salvate"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("File non supportato"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Carica File"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Usa videocamera"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID utente:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Numero versione"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visualizza dettagli errori"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Stiamo attualmente realizzando questa funzione che puoi visualizzare."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Impossibile visualizzare questo link, può appartenere ad un istituto a cui non sei attualmente connesso."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Impossibile trovare alcuno studente associato con questo account"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Non siamo riusciti a verificare il server per l’uso con questa app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Non siamo sicuri di cos’è successo, ma non è stata una cosa positiva. Contattaci se continua a succedere."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sì"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Non stati monitorando alcuno studente."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Riceverai una notifica su questo compito il…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Riceverai una notifica su questo evento il…"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Il tuo codice non è corretto o è scaduto."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("I corsi dello studente potrebbero non essere stati ancora pubblicati."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Sei in pari!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Avvisi"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendario"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guide Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Assistenza Canvas"),
    "collapse" : MessageLookupByLibrary.simpleMessage("comprimi"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("compresso"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Corsi"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("ignora"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Come posso trovare la mia scuola o il mio distretto?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Inserisci nome scuola o distretto…"),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("estendi"),
    "expanded" : MessageLookupByLibrary.simpleMessage("esteso"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Trova scuola"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("me"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("meno"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Successivo"),
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
    "send" : MessageLookupByLibrary.simpleMessage("invia"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("non letto"),
    "unreadCount" : m39
  };
}
