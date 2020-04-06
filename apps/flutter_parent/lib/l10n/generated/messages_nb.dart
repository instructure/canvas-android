// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a nb locale. All the
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
  String get localeName => 'nb';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Oppgavevurdering over ${threshold}";

  static m2(threshold) => "Oppgavevurdering under ${threshold}";

  static m3(moduleName) => "Denne oppgaven er låst av modulen \"${moduleName}\".";

  static m4(studentName, assignmentName) => "Vedrørende: ${studentName}, Oppgave - ${assignmentName}";

  static m5(points) => "${points} poeng";

  static m6(points) => "${points} poeng";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} til 1 annen', other: '${authorName} til ${howMany} andre')}";

  static m8(authorName, recipientName) => "${authorName} til ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} til ${recipientName} og 1 annen', other: '${authorName} til ${recipientName} og ${howMany} andre')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} av ${pointsPossible} poeng";

  static m12(studentShortName) => "for ${studentShortName}";

  static m13(threshold) => "Emnevurdering over ${threshold}";

  static m14(threshold) => "Emnevurdering under ${threshold}";

  static m15(date, time) => "${date} på ${time}";

  static m16(canvasGuides, canvasSupport) => "Prøv å søke på navnet til skolen eller området du forsøker å få tilgang til som f.eks. “Smith privatskole” eller “Smith kommunal skole.” Du kan også angi et Canvas-domene direkte som f.eks. “smith.instructure.com.”\n\nHvis du vil ha mer informasjon om hvordan du finner Canvas-kontoen til din institusjon, besøk ${canvasGuides}, spør etter ${canvasSupport} eller kontakt skolen din for hjelp.";

  static m17(date, time) => "Frist ${date} klokken ${time}";

  static m18(studentName, eventTitle) => "Vedrørende: ${studentName}, Hendelse - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Avsluttende karakter: ${grade}";

  static m21(studentName) => "Vedrørende: ${studentName}, Forside";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Vedrørende: ${studentName}, Karakterer";

  static m24(pointsLost) => "Forsinkelsesstraff (-${pointsLost})";

  static m25(studentName, linkUrl) => "Vedrørende: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Må være over ${percentage}";

  static m27(percentage) => "Må være under ${percentage}";

  static m28(month) => "Neste måned: ${month}";

  static m29(date) => "Neste uke begynner ${date}";

  static m30(query) => "Kan ikke finne skoler som stemmer med \"${query}\"";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'av 1 poeng', other: 'av ${points} poenger')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} poeng oppnåelig";

  static m34(month) => "Forrige måned: ${month}";

  static m35(date) => "Forrige uke begynte ${date}";

  static m36(month) => "Måned ${month}";

  static m37(date, time) => "Denne oppgaven ble levert den ${date} klokken ${time} og avventer vurdering.";

  static m38(studentName) => "Vedrørende: ${studentName}, Emneoversikt";

  static m39(count) => "${count} ulest";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Du må ha en beskrivelse."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Du må ha en tittel."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Legg til student"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Legg til vedlegg"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Legg til ny student"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Legg til en student til..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Varselsinnstillinger"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Varsle meg når..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alle vurderingsperioder"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Du må ha en e-postadresse."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Feil oppsto da du ønsket å vise denne lenken."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Det oppsto en uventet feil"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS-versjon"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Utseende"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Applikasjonsversjon"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Er du lærer eller student?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Er du sikker på at du vil logge ut?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Er du sikker på at du vil lukke denne siden? Dine usendte meldinger vil bli slettet."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Oppgavedetaljer"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Oppgavevurdering over"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Oppgavevurdering under"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Oppgave mangler"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendere"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Avbryt"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas-student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas på GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Velg et emne du vil sende melding til"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Velg fra galleri"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Fullført"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("ta kontakt med kundestøtte"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Emne-kunngjøring"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Emnekunngjøringer"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Emnevurdering over"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Emnevurdering under"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mørk modus"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dato"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Slett"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beskrivelse"),
    "Device" : MessageLookupByLibrary.simpleMessage("Enhet"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Enhetsmodell"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domene:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Ferdig"),
    "Download" : MessageLookupByLibrary.simpleMessage("Last ned"),
    "Due" : MessageLookupByLibrary.simpleMessage("Forfall"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EKSTREMT KRITISK NØDSITUASJON!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-postadresse"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-post:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Angi studentparingskoden som ble gitt til deg. Hvis paringskoden ikke fungerer kan det hende at den er utgått"),
    "Event" : MessageLookupByLibrary.simpleMessage("Hendelse"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Fritatt"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Mislyktes. Trykk for alternativer."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrer etter"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Forside"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Fullstendig feilmelding"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Karakter"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Karakterskala i prosent"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Karaktersatt"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Karakterer"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjelp"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Høy kontrast-modus"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hvordan påvirker dette deg?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Jeg får gjort noenting før jeg hører i fra deg."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Jeg trenger litt hjelp men det haster ikke."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Jeg har problemer med å logge inn"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idé for appen Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Innboks"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Innboks null"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ikke godkjent"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institusjons-kunngjøring"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institusjonskunngjøringer"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruksjoner"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Det ser ut som dette er en flott dag til å slappe av og lade batteriene."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Det ser ut som det ikke er opprettet oppgaver i dette området enda."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Bare et tilfeldig spørsmål, kommentar, idé eller forslag..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Sent"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Åpne eksternt verktøy"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Rettslig"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Lys modus"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Avvik på lenke"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Sted:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Sted"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Låst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Logg Ut"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Administrer studenter"),
    "Message" : MessageLookupByLibrary.simpleMessage("Melding"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Meldingstittel"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Mangler"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Må være under 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Nettverksfeil"),
    "Never" : MessageLookupByLibrary.simpleMessage("Aldri"),
    "New message" : MessageLookupByLibrary.simpleMessage("Ny melding"),
    "No" : MessageLookupByLibrary.simpleMessage("Nei"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Ingen varsler"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Ingen oppgaver"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Ingen emner"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Ingen forfallsdato"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Ingen arrangementer i dag!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Ingen karakter"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Ingen lokalisasjon spesifisert"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Ingen studenter"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Intet tittel"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Ingen sammendrag"),
    "No description" : MessageLookupByLibrary.simpleMessage("Ingen beskrivelse"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Ingen mottakere er valgt"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Ikke karaktersatt"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Ikke levert"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Ikke en foresatt?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Gir deg påminnelser om oppgaver og kalenderhendelser"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS-versjon"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observatør"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("En av våre andre applikasjoner kan være mer tilpasset. Trykk på en for å gå til Play Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Åpne i nettleser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Åpne med en annen app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Paringskode"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Forbereder..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Tidligere innlogginger"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("retningslinjer for personvern"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Personvernregler, bruksvilkår, åpen kilde"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-kode"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Mottakere"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Påminnelse"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Påminnelser"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Svar"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Svar alle"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rapporter et problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Be om hjelp til å logge inn"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("“Be om hjelp til å logge inn”-knapp"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Start app på nytt"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Forsøk igjen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Tilbake til logg-inn"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("STUDENT"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Velg mottakere"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Send en melding om denne oppgaven"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Send en melding om dette emnet"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Send melding"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Angi tid og dato for påminnelse om denne oppgaven."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Angi tid og dato for påminnelse om denne bestemte oppgaven."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Angi når påminnelser skal endres"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Innstillinger"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Del din kjærlighet for appen"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Noe er ødelagt, men jeg kan jobbe rundt problemet for å få gjort det jeg trenger å gjøre."),
    "Student" : MessageLookupByLibrary.simpleMessage("Student"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Tittel"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Innlevert"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Vellykket innlevering!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sammendrag"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Bytt brukere"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Emneoversikt"),
    "TA" : MessageLookupByLibrary.simpleMessage("LA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LÆRER"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Trykk for å velge emnene du ønsker på se på kalenderen."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Trykk for å pare opp mot ny student"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Trykk for å velge denne studenten"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Trykk for å vise studentvelger"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lærer"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Fortell oss om dine favorittdeler av appen"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("bruksvilkår"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Følgende informasjon vil hjelpe oss bedre til å forstå din idé:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Serveren du oppga er ikke tillatt for denne appen."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Brukeragenten for denne appen er ikke autorisert."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Det finnes ingen installerte applikasjoner som kan åpne denne filen"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Det er ingen sideinformasjon tilgjengelig."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Det oppsto et problem under opplastingen av bruksvilkårene"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av mottakere for dette emnet."),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av sammendragsdetaljer for dette emnet."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av denne kunngjøringen"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av denne samtalen"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av denne filen"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av innboksmeldingene dine."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av studentvarslene dine."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av din studentkalender."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av studentene dine."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av emnene til studentene dine."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Det er ingenting du trenger å bli varslet om enda."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Denne appen er ikke autorisert for bruk."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Dette emnet mangler fortsatt oppgaver eller kalenderhendelser."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Denne filtypen støttes ikke og kan ikke vises på appen"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Samlet karakter"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Oi sann!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kan ikke hente emner. Kontroller tilkoblingen og prøv på nytt."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Kan ikke laste bildet"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Kan ikke spille av mediefilen"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kan ikke sende melding. Kontroller tilkoblingen og prøv på nytt."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Under bygging"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Ukjent bruker"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Ulagrede endringer"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Filtypen støttes ikke"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Last opp fil"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Bruk kamera"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Bruker-ID"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versjon nummer"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Vis avviksdetaljer"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Vi jobber for tiden med denne funksjonen slik at du kan få glede av den senere."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Vi kan ikke vise denne lenken, den kan eies av en instutisjon som du for øyeblikket ikke er innlogget i."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Vi kunne ikke finne noen studenter knyttet til denne kontoen"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Vi klarte ikke å verifisere serveren for bruk med denne appen."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Vi vet ikke hva som skjedde her, men det ser ikke bra ut. Ta kontakt med oss hvis denne situasjonen vedvarer."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Du har ingen studenter under observasjon."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Du vil få en påminnelse om denne oppgaven den..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Du vil få en påminnelse om denne hendelsen den..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Koden din stemmer ikke eller den er utgått"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Emnene til studentene dine er kanskje ikke publisert enda."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Du er oppdatert!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Varsler"),
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
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-guider"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas Support"),
    "collapse" : MessageLookupByLibrary.simpleMessage("skjult"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("skjult"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Emner"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("avvise"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hvordan finner jeg skolen eller området mitt?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Skriv inn skolenavn eller område..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("utvid"),
    "expanded" : MessageLookupByLibrary.simpleMessage("utvidet"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Finne skole"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("meg"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Neste"),
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
    "send" : MessageLookupByLibrary.simpleMessage("send"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("ulest"),
    "unreadCount" : m39
  };
}
