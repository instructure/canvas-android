// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a nb_instk12 locale. All the
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
  String get localeName => 'nb_instk12';

  static m0(userName) => "Du opptrer som ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Oppgavevurdering over ${threshold}";

  static m3(threshold) => "Oppgavevurdering under ${threshold}";

  static m4(moduleName) => "Denne oppgaven er låst av modulen \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Vedrørende: ${studentName}, Oppgave - ${assignmentName}";

  static m6(points) => "${points} poeng";

  static m7(points) => "${points} poeng";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} til 1 annen', other: '${authorName} til ${howMany} andre')}";

  static m9(authorName, recipientName) => "${authorName} til ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} til ${recipientName} og 1 annen', other: '${authorName} til ${recipientName} og ${howMany} andre')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Endre farge for ${studentName}";

  static m13(score, pointsPossible) => "${score} av ${pointsPossible} poeng";

  static m14(studentShortName) => "for ${studentShortName}";

  static m15(threshold) => "Fagvurdering over ${threshold}";

  static m16(threshold) => "Fagvurdering under ${threshold}";

  static m17(date, time) => "${date} på ${time}";

  static m18(alertTitle) => "Avvise ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Prøv å søke på navnet til skolen eller området du forsøker å få tilgang til som f.eks. “Smith privatskole” eller “Smith kommunal skole.” Du kan også angi et Canvas-domene direkte som f.eks. “smith.instructure.com.”\n\nHvis du vil ha mer informasjon om hvordan du finner Canvas-kontoen til din institusjon, besøk ${canvasGuides}, spør etter ${canvasSupport} eller kontakt skolen din for hjelp.";

  static m20(date, time) => "Frist ${date} klokken ${time}";

  static m21(userName) => "Du vil slutte å opptre som ${userName} og bli logget ut.";

  static m22(userName) => "Du vil slutte å opptre som ${userName} og gå tilbake til din originale konto.";

  static m23(studentName, eventTitle) => "Vedrørende: ${studentName}, Hendelse - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Avsluttende vurdering: ${grade}";

  static m26(studentName) => "Vedrørende: ${studentName}, Forside";

  static m27(score, pointsPossible) => "${score}/${pointsPossible}";

  static m28(studentName) => "Vedrørende: ${studentName}, Vurderinger";

  static m29(pointsLost) => "Forsinkelsesstraff (-${pointsLost})";

  static m30(studentName, linkUrl) => "Vedrørende: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Må være over ${percentage}";

  static m32(percentage) => "Må være under ${percentage}";

  static m33(month) => "Neste måned: ${month}";

  static m34(date) => "Neste uke begynner ${date}";

  static m35(query) => "Kan ikke finne skoler som stemmer med \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'av 1 poeng', other: 'av ${points} poenger')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} poeng oppnåelig";

  static m39(month) => "Forrige måned: ${month}";

  static m40(date) => "Forrige uke begynte ${date}";

  static m41(termsOfService, privacyPolicy) => "Ved å trykke på Opprett konto, samtykker du til ${termsOfService} og godtar ${privacyPolicy}";

  static m42(version) => "Forslag for Android - Canvas foreldre ${version}";

  static m43(month) => "Måned ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} stjerne', other: '${position} stjerner')}";

  static m45(date, time) => "Denne oppgaven ble levert den ${date} klokken ${time} og avventer vurdering.";

  static m46(studentName) => "Vedrørende: ${studentName}, Fagoversikt";

  static m47(count) => "${count} ulest";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Oppfør deg som\" er i realiteten å logge inn som denne brukeren uten passord. Du vil kunne utføre alle handlinger som om du var denne brukeren, og for andre brukere vil det se ut som om at denne brukeren utførte handlingene. Overvåkingloggen vil likevel notere at det var du som utførte handlingene på vegne av denne brukeren."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Du må ha en beskrivelse."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Det oppsto en nettverksfeil da du la til denne eleven. Kontroller tilkoblingen og prøv på nytt."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Du må ha en tittel."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Opptre som bruker"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Legg til elev"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Legg til vedlegg"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Legg til ny elev"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Legg til en elev til..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Varselsinnstillinger"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Varsle meg når..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alle vurderingsperioder"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Har du allerede en konto? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Du må ha en e-postadresse."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Feil oppsto da du ønsket å vise denne lenken."),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Det oppsto en feil ved lagring av valget ditt. Vennligst prøv igjen."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Det oppsto en uventet feil"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS-versjon"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Utseende"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Applikasjonsversjon"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Er du lærer eller elev?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Er du sikker på at du vil logge ut?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Er du sikker på at du vil lukke denne siden? Dine usendte meldinger vil bli slettet."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Oppgavedetaljer"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Oppgavevurdering over"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Oppgavevurdering under"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Oppgave mangler"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, fuchsia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendere"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Kameratillatelse"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Avbryt"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas-elev"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas på GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Velg et fag du vil sende melding til"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Velg fra galleri"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Fullført"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("ta kontakt med kundestøtte"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Fagbeskjed"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Fagbeskjed"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Fagvurdering over"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Fagvurdering under"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Opprett konto"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mørk modus"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dato"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Slett"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beskrivelse"),
    "Device" : MessageLookupByLibrary.simpleMessage("Enhet"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Enhetsmodell"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domene"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domene:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Ikke vis igjen"),
    "Done" : MessageLookupByLibrary.simpleMessage("Ferdig"),
    "Download" : MessageLookupByLibrary.simpleMessage("Last ned"),
    "Due" : MessageLookupByLibrary.simpleMessage("Forfall"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EKSTREMT KRITISK NØDSITUASJON!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elektrisk, blå"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-postadresse"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-post:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-post…"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Angi paringskoden til elev som ble gitt til deg. Hvis paringskoden ikke fungerer kan det hende at den er utgått"),
    "Event" : MessageLookupByLibrary.simpleMessage("Hendelse"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Fritatt"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Utgått QR-kode"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Mislyktes. Trykk for alternativer."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrer etter"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Ild, oransje"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Forside"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Fullt navn"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Fullt navn…"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Fullstendig feilmelding"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Gå til idag"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Vurdering"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Vurderingsprosent"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Vurdert"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Vurderinger"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjelp"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Skjul passord"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Høy kontrast-modus"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Hvordan klarer vi oss?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hvordan påvirker dette deg?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Jeg får gjort noenting før jeg hører i fra deg."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Jeg har ikke en Canvas-konto"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Jeg har en Canvas-konto"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Jeg trenger litt hjelp men det haster ikke."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Jeg har problemer med å logge inn"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idé for appen Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("For å gi deg en bedre opplevelse, har vi oppdatert hvordan påminnelser fungerer. Du kan legge til påminnelser ved å vise en oppgave eller kalenderoppføring og trykke på bryteren under avsnittet ”Påminn meg”.\n\nVær oppmerksom på at påminnelser som ble opprettet med den gamle versjonen av denne appen ikke er kompatible med de nye endringene, og at du må opprette disse på nytt."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Innboks"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Innboks null"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ikke fullført"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Ugyldig domene"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institusjonsbeskjed"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institusjonsbeskjed"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruksjoner"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interaksjoner på denne siden er begrenset av institusjonen din."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Ugyldig QR-kode"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Det ser ut som dette er en flott dag til å slappe av og lade batteriene."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Det ser ut som det ikke er opprettet oppgaver i dette området enda."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Bare et tilfeldig spørsmål, kommentar, idé eller forslag..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Sent"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Åpne eksternt verktøy"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Rettslig"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Lys modus"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Avvik på lenke"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Sted:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Lokalisere QR-kode"),
    "Location" : MessageLookupByLibrary.simpleMessage("Sted"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Låst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Logg Ut"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Innloggingsflyt: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Innloggingsflyt: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Innloggingsflyt: Site-admin"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Innloggingsflyt: Hopp over bekreftelse med mobil"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Administrer elever"),
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
    "No Courses" : MessageLookupByLibrary.simpleMessage("Ingen fag"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Ingen forfallsdato"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Ingen arrangementer i dag!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Ingen vurdering"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Ingen lokalisasjon spesifisert"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Ingen elever"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Intet tittel"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Ingen sammendrag"),
    "No description" : MessageLookupByLibrary.simpleMessage("Ingen beskrivelse"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Ingen mottakere er valgt"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Ikke vurdert"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Ikke levert"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Ikke en foresatt?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Gir deg påminnelser om oppgaver og kalenderhendelser"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS-versjon"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observatør"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("En av våre andre applikasjoner kan være mer tilpasset. Trykk på en for å gå til Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Åpne Canvas Elev"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Åpne i nettleser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Åpne med en annen app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Paringskode"),
    "Password" : MessageLookupByLibrary.simpleMessage("Passord"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Passord kreves"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Passord må inneholde minst åtte tegn"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Passord…"),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Planlegger-notat"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Oppgi en gyldig e-postadresse"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Angi en e-postadresse"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Angi fullt navn"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Skann en QR-kode generert av Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Plomme, blå"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Forbereder..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Tidligere innlogginger"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("retningslinjer for personvern"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Lenke til retningslinjer for personvern"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Personvernregler, bruksvilkår, åpen kilde"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-kode"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("QR-skanning krever kameratilgang"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Bringebær, rød"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Mottakere"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Oppdater"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Påminnelse"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Påminnelser"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Påminnelser er endret!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Svar"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Svar alle"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rapporter et problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Be om hjelp til å logge inn"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("“Be om hjelp til å logge inn”-knapp"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Start app på nytt"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Forsøk igjen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Tilbake til logg-inn"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ELEV"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Skjermbilde som viser plassering av QR-kodegenerering i nettleser"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Skjermbilde som viser plasseringen av QR-koden som genereres i Canvas Elev-appen"),
    "Select" : MessageLookupByLibrary.simpleMessage("Velg"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Velg elevfarge"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Velg mottakere"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Send tilbakemelding"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Send en melding om denne oppgaven"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Send en melding om dette faget"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Send melding"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Angi tid og dato for påminnelse om denne oppgaven."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Angi tid og dato for påminnelse om denne bestemte oppgaven."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Innstillinger"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Kløver, grønn"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Del din kjærlighet for appen"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Vis passord"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Logg inn"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Noe er ødelagt, men jeg kan jobbe rundt problemet for å få gjort det jeg trenger å gjøre."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Avslutt opptre som bruker"),
    "Student" : MessageLookupByLibrary.simpleMessage("Elev"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Elevparing"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Elever kan opprette en QR-kode ved å bruke Canvas Elev-appen på mobilenheten"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Elever kan få en paringskode gjennom Canvas-nettstedet"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Tittel"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Innlevert"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Vellykket innlevering!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sammendrag"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Bytt brukere"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Fagoversikt"),
    "TA" : MessageLookupByLibrary.simpleMessage("LA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LÆRER"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Trykk for å velge fagene du ønsker på se på kalenderen. Velg opptil 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Trykk for å pare opp mot ny elev"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Trykk for å velge denne eleven"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Trykk for å vise elev velger"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lærer"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Fortell oss om dine favorittdeler av appen"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Brukervilkår"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Lenke til tjenestevilkår"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("bruksvilkår"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("QR-koden du skannet kan ha utgått Gjenopprett koden på elevens enhet og prøv igjen."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Følgende informasjon vil hjelpe oss bedre til å forstå din idé:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Serveren du oppga er ikke tillatt for denne appen."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Eleven du forsøker å legge til tilhører en annen skole. Logg inn eller opprett en konto med den skolen for å skanne denne koden."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Brukeragenten for denne appen er ikke autorisert."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Det finnes ingen installerte applikasjoner som kan åpne denne filen"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Det er ingen sideinformasjon tilgjengelig."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Det oppsto et problem under opplastingen av bruksvilkårene"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Det oppsto et problem ved fjerning av denne eleven fra kontoen din. Kontroller tilkoblingen og prøv på nytt."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av mottakere for dette faget."),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av sammendragsdetaljer for dette faget."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av denne beskjeden"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av denne samtalen"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av denne filen"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av innboksmeldingene dine."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av elev-varslene dine."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av din elevkalender."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av elevene dine."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil under lasting av fagene til elevene dine."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Det oppstod en feil ved innlogging. Vennligst lag en ny QR-kode og prøv på nytt."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Det var en feil med å opptre som denne brukeren. Sjekk domenet og bruker-ID-en og prøv igjen."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Det er ingenting du trenger å bli varslet om enda."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Denne appen er ikke autorisert for bruk."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Dette faget mangler fortsatt oppgaver eller kalenderhendelser."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Denne filtypen støttes ikke og kan ikke vises på appen"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Dette vil koble fra og fjerne alle påmeldinger for denne eleven fra kontoen din."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Samlet vurdering"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Oi sann!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kan ikke hente fag. Kontroller tilkoblingen og prøv på nytt."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Kan ikke laste bildet"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Kan ikke spille av mediefilen"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kan ikke sende melding. Kontroller tilkoblingen og prøv på nytt."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Under bygging"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Ukjent bruker"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Ulagrede endringer"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Filtypen støttes ikke"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Last opp fil"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Bruk kamera"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Bruk Mørkt tema i webinnhold"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Bruker-ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Bruker-ID"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versjon nummer"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Vise beskrivelsen"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Vis avviksdetaljer"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Se retningslinjer for personvern"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Vi jobber for tiden med denne funksjonen slik at du kan få glede av den senere."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Vi kan ikke vise denne lenken, den kan eies av en instutisjon som du for øyeblikket ikke er innlogget i."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Vi kunne ikke finne noen elever knyttet til denne kontoen"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Vi klarte ikke å verifisere serveren for bruk med denne appen."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Vi vet ikke hva som skjedde her, men det ser ikke bra ut. Ta kontakt med oss hvis denne situasjonen vedvarer."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Hva kan vi gjøre bedre?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Du har ingen elever under observasjon."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Du kan kun velge 10 kalendere som skal vises"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Du må skrive inn en bruker-ID"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Du må oppgi et gyldig domene"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Du må velge minst én kalender som skal vises"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Du vil få en påminnelse om denne oppgaven den..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Du vil få en påminnelse om denne hendelsen den..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Du finner QR-koden på nettet i kontoprofilen din. Klikk på “QR for mobil-innlogging” i listen."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Du må åpne Canvas Elev-appen til eleven din for å fortsette. Gå til Hovedmeny > Innstillinger > Paring med observatør og skann QR-koden du ser der."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Koden din stemmer ikke eller den er utgått"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Fagene til elevene dine er kanskje ikke publisert enda."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Du er oppdatert!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Varsler"),
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
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-guider"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas Support"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("skjult"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("skjult"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Fag"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hvordan finner jeg skolen eller området mitt?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Skriv inn skolenavn eller område..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("utvid"),
    "expanded" : MessageLookupByLibrary.simpleMessage("utvidet"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Finne skole"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("meg"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Neste"),
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
    "send" : MessageLookupByLibrary.simpleMessage("send"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("ulest"),
    "unreadCount" : m47
  };
}
