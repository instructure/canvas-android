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

  static m0(userName) => "Stai agendo come ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Voto compito sopra ${threshold}";

  static m3(threshold) => "Voto compito sotto ${threshold}";

  static m4(moduleName) => "Questo compito è bloccato dal modulo \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Su: ${studentName}, Compito - ${assignmentName}";

  static m6(points) => "${points} pt.";

  static m7(points) => "${points} punti";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} a 1 altro', other: '${authorName} ad altri ${howMany}')}";

  static m9(authorName, recipientName) => "${authorName} a ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} a ${recipientName} e 1 altro', other: '${authorName} a ${recipientName} e altri ${howMany}')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Cambia colore per ${studentName}";

  static m13(score, pointsPossible) => "${score} su ${pointsPossible} punti";

  static m14(studentShortName) => "per ${studentShortName}";

  static m15(threshold) => "Voto corso sopra ${threshold}";

  static m16(threshold) => "Voto corso sotto ${threshold}";

  static m17(date, time) => "Il ${date} alle ${time}";

  static m18(alertTitle) => "Ignora ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Prova a cercare il nome della scuola o del distretto a cui stai tentando di accedere, ad esempio “Scuola privata Rossi” o “Scuole statali Rossi”. Puoi anche entrare direttamente in un dominio Canvas, ad esempio “rossi.instructure.com.”\n\nPer ulteriori informazioni su come trovare l’account Canvas del tuo istituto, puoi visitare le ${canvasGuides}, contattare l’${canvasSupport} o la scuola per assistenza.";

  static m20(date, time) => "Scade il ${date} alle ${time}";

  static m21(userName) => "Non agirai più come ${userName} e sarai disconnesso.";

  static m22(userName) => "Non agirai più come ${userName} e tornerai al tuo account originale.";

  static m23(studentName, eventTitle) => "Su: ${studentName}, Evento - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Voto finale: ${grade}";

  static m26(studentName) => "Su: ${studentName}, Pagina iniziale";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Su: ${studentName}, Voti";

  static m29(pointsLost) => "Penale ritardo (-${pointsLost})";

  static m30(studentName, linkUrl) => "Su: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Deve essere sopra ${percentage}";

  static m32(percentage) => "Deve essere sotto ${percentage}";

  static m33(month) => "Prossimo mese: ${month}";

  static m34(date) => "Prossima settimana a partire da ${date}";

  static m35(query) => "Impossibile trovare delle scuole corrispondenti a \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Di 1 punto', other: 'Di ${points} punti')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} punti possibili";

  static m39(month) => "Mese precedente: ${month}";

  static m40(date) => "Settimana precedente a partire da ${date}";

  static m41(termsOfService, privacyPolicy) => "Toccando “Crea account”, accetti i ${termsOfService} e confermi l’${privacyPolicy}";

  static m42(version) => "Suggerimenti per Android - Canvas Parent ${version}";

  static m43(month) => "Mese di ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} stella', other: '${position} stelle')}";

  static m45(date, time) => "Questo compito è stato inviato il ${date} alle ${time} ed è in attesa della valutazione";

  static m46(studentName) => "Su: ${studentName}, Piano di studio";

  static m47(count) => "${count} non letto";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Agisci come\" consiste sostanzialmente nell\'effettuare l\'accesso assumendo l\'identità di questo utente senza inserire alcuna password. Potrai eseguire qualsiasi azione come se fossi questo utente e, dal punto di vista degli altri utenti, sarà come se queste azioni fossero state eseguite da questo utente. Tuttavia, i log di controllo registrano che sei stato tu a eseguire le azioni per conto di questo utente."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("La descrizione è obbligatoria."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Durante l’aggiunta di questo studente, si è verificato un errore di rete. Verifica la tua connessione e riprova."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("L’argomento è obbligatorio."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Agisci come utente"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Aggiungi studente"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Aggiungi allegato"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Aggiungi nuovo studente"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Aggiungi studente con…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Impostazioni avviso"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Avvisami quando…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Tutti i periodi di valutazione"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Hai già un account? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("L’indirizzo e-mail è obbligatorio."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il tentativo di visualizzare questo link"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il salvataggio della selezione. Riprova."),
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
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, fucsia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendari"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Autorizzazione della fotocamera"),
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
    "Create Account" : MessageLookupByLibrary.simpleMessage("Crea account"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Modalità Scura"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Elimina"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descrizione"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositivo"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modello dispositivo"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Dominio"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Dominio:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Non mostrarlo di nuovo"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fatto"),
    "Download" : MessageLookupByLibrary.simpleMessage("Download"),
    "Due" : MessageLookupByLibrary.simpleMessage("Scadenza"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGENZA CRITICA."),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elettrico, blu"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Indirizzo e-mail"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-mail:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-mail..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Inserisci il codice accoppiamento studente fornito. Se il codice accoppiamento non funziona, potrebbe essere scaduto"),
    "Event" : MessageLookupByLibrary.simpleMessage("Evento"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Giustificato"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Codice QR scaduto"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Non riuscito. Tocca per le opzioni."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtra"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtra per"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Fuoco, arancione"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Pagina iniziale"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Nome completo"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Nome completo..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Messaggio di errore pieno"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Vai a oggi"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Voto"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Percentuale voto"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Valutato"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Voti"),
    "Help" : MessageLookupByLibrary.simpleMessage("Guida"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Nascondi password"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modalità alto contrasto"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Come stiamo andando?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Quali sono le ripercussioni per te?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Non riesco a terminare il lavoro finché non ricevo una tua risposta."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Non ho un account Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Ho un account Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Ho bisogno di aiuto ma non è urgente."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Ho dei problemi di login"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idea per l’app Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Per fornirti un’esperienza migliore, abbiamo aggiornato il modo in cui funzionano i promemoria. Puoi aggiungere nuovi promemoria visualizzando un compito o un evento del calendario e toccando l’interruttore sotto la sezione \"Promemoria\".\n\nNon dimenticare che tutti i promemoria creati con le versioni precedenti di questa app non saranno compatibili con le nuove modifiche e sarà necessario ricrearli."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Posta in arrivo"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Zero in posta in arrivo"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Non completato"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Dominio errato"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Annuncio istituto"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Annunci istituto"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Istruzioni"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Le interazioni su questa pagina sono limitate dal tuo istituto."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Codice QR non valido"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Ottima occasione per riposarsi, rilassarsi e ricaricare le batterie."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Sembra che i compiti non siano ancora stati creati in questo spazio."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Solo una domanda casuale, un commento, un\'idea, un suggerimento…"),
    "Late" : MessageLookupByLibrary.simpleMessage("In ritardo"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Avvia strumento esterno"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legale"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modalità chiara"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Errore link"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Impostazioni internazionali:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Individua codice QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Posizione"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Bloccato"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Disconnetti"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Flusso di login: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Flusso di login: Normale"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Flusso di login: Amministratore del sito"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Flusso di login: Salta verifica dispositivo mobile"),
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
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Apri Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Apri nel browser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Apri con un’altra app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Codice accoppiamento"),
    "Password" : MessageLookupByLibrary.simpleMessage("Password"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Password obbligatoria"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("La password deve contenere almeno 8 caratteri"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Password..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Nota agenda"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Inserisci un indirizzo e-mail valido"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Inserisci un indirizzo e-mail"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Inserisci il nome completo"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Acquisisci un codice QR generato da Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Prugna, viola"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Preparazione in corso..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Login precedenti"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Informativa sulla privacy"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Link dell’informativa sulla privacy"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Informativa sulla privacy, termini di utilizzo, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Codice QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Per la scansione QR è necessario l’accesso alla fotocamera"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Lampone, rosso"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinatari"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Aggiorna"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Promemoria"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Promemoria"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("I promemoria sono cambiati!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Rispondi"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Rispondi a tutti"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Segnala un problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Richiedi aiuto per login"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Pulsante Richiedi aiuto per login"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Riavvia app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Riprova"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Torna alla pagina di login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("STUDENTE"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Screenshot che mostra la posizione della generazione del codice QR nel browser"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Schermata che mostra la posizione per la generazione del codice QR di abbinamento nell\'applicazione Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Seleziona"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Seleziona colore studente"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Seleziona destinatari"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Invia feedback"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Invia un messaggio su questo compito"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Invia un messaggio su questo corso"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Invia messaggio"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Imposta una data e un’ora per ricevere la notifica su questo evento."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Imposta una data e un’ora per ricevere la notifica su questo compito specifico."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Impostazioni"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Trifoglio, verde"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Condividi cosa ti piace dell’app"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Mostra password"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Accedi"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Si è verificato un problema ma posso aggirarlo e fare ciò che devo."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Non agire più come utente"),
    "Student" : MessageLookupByLibrary.simpleMessage("Studente"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Abbinamento studente"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Gli studenti possono creare un codice QR tramite l\'applicazione Canvas Student sul loro dispositivo mobile"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Gli studenti possono ricevere un codice di abbinamento sul sito web Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Oggetto"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Inviato"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Inviato correttamente!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Riepilogo"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Cambia studenti"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Piano di studio"),
    "TA" : MessageLookupByLibrary.simpleMessage("Assistente"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("INSEGNANTE"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tocca per mettere nei preferiti i corsi che vuoi vedere sul calendario. Seleziona fino a 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tocca per accoppiare con un nuovo studente"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tocca per selezionare questo studente"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tocca per mostrare il selettore studente"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Insegnante"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Raccontaci quali sono le tue parti preferite dell’app"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Termini di servizio"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Link dei termini di servizio"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Termini di utilizzo"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Il codice QR scansionato potrebbe essere scaduto. Aggiorna il codice sul dispositivo dello studente e riprova."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Le seguenti informazioni ci aiutano a comprendere meglio la tua idea:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Il server in cui sei entrato non è autorizzato per questa app."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Lo studente che stai cercando di aggiungere fa parte di un\'altra scuola. Per scansionare il codice, accedi o crea un account per quella scuola."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("L’agente utente per questa app non è autorizzata."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Non ci sono applicazioni installate per aprire questo file"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Non ci sono informazioni pagina disponibili."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Si è verificato un problema durante il caricamento delle Condizioni d’uso"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Si è verificato un problema durante la rimozione di questo studente dall’account. Verifica la connessione e riprova."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei destinatari per questo corso"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei dettagli di riepilogo per questo corso."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento di questo annuncio"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento di questa conversazione"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento di questo file"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei messaggi di posta in arrivo."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento degli avvisi dello studente."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Si è verificato un errore di caricamento del calendario del tuo studente"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei tuoi studenti."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il caricamento dei corsi dello studente."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore di login. Genera un altro codice QR e riprova."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Si è verificato un errore durante il tentativo di agire come questo utente. Controlla il dominio e l’ID utente e riprova."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Non c’è ancora nulla su cui ricevere delle notifiche."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Questa app non è autorizzata per l’uso."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Questo corso non ha ancora alcun compito o eventi di calendario."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Questo file non è supportato e non può essere visualizzato attraverso l’app"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Quest’azione disassocierà e rimuoverà tutte le iscrizioni per questo studente dal tuo account."),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Usa tema scuro nel contenuto web"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID utente"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID utente:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Numero versione"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Visualizza descrizione"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visualizza dettagli errori"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Visualizza informativa sulla privacy"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Stiamo attualmente realizzando questa funzione che puoi visualizzare."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Impossibile visualizzare questo link, può appartenere ad un istituto a cui non sei attualmente connesso."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Impossibile trovare alcuno studente associato con questo account"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Non siamo riusciti a verificare il server per l’uso con questa app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Non siamo sicuri di cos’è successo, ma non è stata una cosa positiva. Contattaci se continua a succedere."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Cosa possiamo migliorare?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sì"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Non stati monitorando alcuno studente."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Puoi scegliere solo 10 calendari da visualizzare"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Devi inserire un ID utente"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Devi inserire un dominio valido"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Devi selezionare almeno un calendario da visualizzare"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Riceverai una notifica su questo compito il…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Riceverai una notifica su questo evento il…"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Troverai il codice QR sul web nel tuo profilo account. Fai clic su “QR per login da dispositivo mobile” nell’elenco."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Per continuare, devi aprire l\'applicazione Canvas Student del tuo studente. Vai in Menu principale > Impostazioni > Abbina con l\'Osservatore e scansiona il codice QR che vedi."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Il tuo codice non è corretto o è scaduto."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("I corsi dello studente potrebbero non essere stati ancora pubblicati."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Sei in pari!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Avvisi"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendario"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guide Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Assistenza Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("comprimi"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("compresso"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Corsi"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Come posso trovare la mia scuola o il mio distretto?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Inserisci nome scuola o distretto…"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("estendi"),
    "expanded" : MessageLookupByLibrary.simpleMessage("esteso"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Trova scuola"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("me"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("meno"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Successivo"),
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
    "send" : MessageLookupByLibrary.simpleMessage("invia"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("non letto"),
    "unreadCount" : m47
  };
}
