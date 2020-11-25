// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a da_instk12 locale. All the
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
  String get localeName => 'da_instk12';

  static m0(userName) => "Du fungerer som ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Opgavevurdering over ${threshold}";

  static m3(threshold) => "Opgavevurdering under ${threshold}";

  static m4(moduleName) => "Denne opgave er låst af forløbet \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Vedrørende: ${studentName}, Opgave - ${assignmentName}";

  static m6(points) => "${points} point";

  static m7(points) => "${points} point";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} til 1 anden', other: '${authorName} til ${howMany} andre')}";

  static m9(authorName, recipientName) => "${authorName} til ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} til ${recipientName} & 1 anden', other: '${authorName} til ${recipientName} & ${howMany} andre')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Skift farve til ${studentName}";

  static m13(score, pointsPossible) => "${score} ud af ${pointsPossible} point";

  static m14(studentShortName) => "for ${studentShortName}";

  static m15(threshold) => "Fagvurdering oven ${threshold}";

  static m16(threshold) => "Fagvurdering under ${threshold}";

  static m17(date, time) => "${date} kl. ${time}";

  static m18(alertTitle) => "Afvis ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Prøv at søge efter navnet på den skole eller det distrikt, du forsøger at få adgang til, fx “Smith Private School” eller “Smith County Schools.” Du kan også indtaste et Canvas-domæne direkte, som fx “smith.instructure.com.”\n\nFor mere information om hvordan du finder din institutions Canvas-konto, kan du besøge ${canvasGuides}, kontakte ${canvasSupport} eller kontakte din skole for at få hjælp.";

  static m20(date, time) => "Forfalder d. ${date} kl. ${time}";

  static m21(userName) => "Du vil stoppe med at fungere som ${userName} og bliver logget ud.";

  static m22(userName) => "Du vil stoppe med at fungere som ${userName} og vende tilbage til din originale konto.";

  static m23(studentName, eventTitle) => "Vedrørende: ${studentName}, Begivenhed - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Endelig vurdering: ${grade}";

  static m26(studentName) => "Vedrørende: ${studentName}, Forside";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Vedrørende: ${studentName}, Vurderinger";

  static m29(pointsLost) => "Straf for sen aflevering (-${pointsLost})";

  static m30(studentName, linkUrl) => "Vedrørende: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Skal være over ${percentage}";

  static m32(percentage) => "Skal være under ${percentage}";

  static m33(month) => "Næste måned: ${month}";

  static m34(date) => "Næste uge, der starter ${date}";

  static m35(query) => "Kan ikke finde skoler, der matcher \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Ud af 1 point', other: 'Ud af ${points} point')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} mulige point";

  static m39(month) => "Forrige måned: ${month}";

  static m40(date) => "Forrige uge, der starter ${date}";

  static m41(termsOfService, privacyPolicy) => "Ved at trykke på \'Opret konto\' accepterer jeg ${termsOfService} og accepterer ${privacyPolicy}.";

  static m42(version) => "Forslag til Android - Canvas Parent ${version}";

  static m43(month) => "Måneden ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} stjerne', other: '${position} stjerner')}";

  static m45(date, time) => "Denne opgave blev afleveret den ${date} kl. ${time} og venter på at blive bedømt";

  static m46(studentName) => "Vedrørende: ${studentName}, Fagplan";

  static m47(count) => "${count} ulæst";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Funger som\" betyder i bund og grund, at du logger ind som denne bruger uden adgangskode. Du kan foretage dig alt, som om du var denne bruger, og for andre brugere, er det lige som om, det var denne bruger, der gjorde det. Dog registrerer audit-logs, at det faktisk var dig, der udførte handlingerne på vegne af denne bruger."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Der kræves en beskrivelse."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Der opstod en netværksfejl under dit forsøg på at tilføje denne elev. Kontrollér forbindelsen, og prøv igen."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Der kræves et emne."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Funger som bruger"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Tilføj elever"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Tilføj vedhæftet fil"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Tilføj ny elev"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Tilføj elev med ..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Varslingsindstillinger"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Send mig en varsling når ..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alle vurderingsperioder"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Har du allerede en konto? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Der kræves en e-mailadresse."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved forsøg på at vise dette link"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl, da du forsøgte at gemme dit valg. Prøv igen."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Der opstod en uventet fejl"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS-version"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Udseende"),
    "Application version" : MessageLookupByLibrary.simpleMessage("App-version"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Er du elev eller lærer?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Er du sikker på, du vil logge af?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Er du sikker på, at du vil lukke denne side? Din usendte besked vil gå tabt."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Opgaveoplysninger"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Opgavevurdering over"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Opgavevurdering under"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Manglende opgave"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, fuschia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendere"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Kameratilladelse"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Annullér"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas-elev"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas på GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Vælg et fag at sende en meddelelse til"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Vælg fra galleri"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Fuldført"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Kontakt support"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Besked til faget"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Fag-beskeder"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Fagvurdering over"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Fagvurdering under"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Opret konto"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mørk tilstand"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dato"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Slet"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beskrivelse"),
    "Device" : MessageLookupByLibrary.simpleMessage("Enhed"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Enhedsmodel"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domæne"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domæne:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Vis ikke igen"),
    "Done" : MessageLookupByLibrary.simpleMessage("Udført"),
    "Download" : MessageLookupByLibrary.simpleMessage("Download"),
    "Due" : MessageLookupByLibrary.simpleMessage("Forfalder"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EKSTREMT KRITISK NØDSTILFÆLDE!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elektrisk, blå"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-mail-adresse"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-mail:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-mail ..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Indtast elevens parringskode, der er blevet givet til dig. Hvis parringskoden ikke fungerer, kan den være udløbet"),
    "Event" : MessageLookupByLibrary.simpleMessage("Begivenhed"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Undskyldt"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Udløbet QR-kode"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Mislykkedes. Tryk for indstillinger."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrere efter"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Ild, orange"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Forside"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Fulde navn"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Fulde navn ..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Hel fejlmeddelelse"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Gå til I dag"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Vurdering"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Vurdering procent"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Bedømt"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Vurderinger"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjælp"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Skjul adgangskode"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Høj kontrast-tilstand"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Hvordan klarer vi os?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hvordan påvirker det dig?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Jeg kan ikke fortsætte, før jeg har fået svar."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Jeg har ikke en Canvas-konto"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Jeg har en Canvas-konto"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Jeg behøver hjælp, men det er ikke presserende."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Jeg har problemer med at logge ind"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ide for Canvas Parent App [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("For at give dig en bedre oplevelse har vi opdateret, hvordan påmindelser fungerer. Du kan tilføje nye påmindelser ved at se en opgave eller kalenderbegivenhed og trykke på kontakten i sektionen \"Påmind mig\".\n\nVær opmærksom på, at alle påmindelser oprettet med ældre versioner af denne app ikke vil være kompatible med de nye ændringer, og du derfor må oprette dem igen."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Indbakke"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Indbakke nul"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ufuldstændig"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Forkert domæne"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Besked til institutionen"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institutionsmeddelelser"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruktioner"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interaktioner på denne side er begrænset af din institution."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Ugyldig QR-kode"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Det er en alle tiders dag til at tage den med ro og slappe af."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Det ser ud til, at opgaver ikke er blevet oprettet i dette rum endnu."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Blot et simpelt spørgsmål, kommentar, ide, forslag..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Sen"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Start eksternt værktøj"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Juridisk"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Lys tilstand"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Linkfejl"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Sted:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Find QR-kode"),
    "Location" : MessageLookupByLibrary.simpleMessage("Placering"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Låst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Log ud"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Login-flow: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Login-flow: Normalt"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Login-flow: Websideadministrator"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Login-flow: Spring mobil bekræftelse over"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Administrer elever"),
    "Message" : MessageLookupByLibrary.simpleMessage("Besked"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Beskedens emne"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Mangler"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Skal være under 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Netværksfejl"),
    "Never" : MessageLookupByLibrary.simpleMessage("Aldrig"),
    "New message" : MessageLookupByLibrary.simpleMessage("Ny besked"),
    "No" : MessageLookupByLibrary.simpleMessage("Nej"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Ingen varslinger"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Ingen opgaver"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Ingen fag"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Ingen afleveringsdato"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Ingen begivenheder i dag!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Ingen vurdering"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Ingen lokation specificeret"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Ingen elever"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Intet emne"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Intet sammendrag"),
    "No description" : MessageLookupByLibrary.simpleMessage("Ingen beskrivelse"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Ingen modtagere valgt"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Ikke bedømt"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Ikke indsendt"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Ikke forælder?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Meddelelser for påmindelser om opgaver og kalenderbegivenheder"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS-version"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observatør"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("En af vores andre apps kan være bedre egnet. Tryk på et for at besøge Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Åbn Canvas-appen for elev"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Åbn i browser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Åbn med en anden app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Parringskode"),
    "Password" : MessageLookupByLibrary.simpleMessage("Adgangskode"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Adgangskode er påkrævet"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Adgangskode skal indeholde mindst 8 tegn"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Adgangskode ..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Planlægger - bemærkning"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Indtast gyldig e-mailadresse"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Indtast en e-mailadresse"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Indtast det fulde navn"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Scan en QR-kode genereret af Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Blomme, lilla"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Forbereder ..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Tidligere logins"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Datapolitik"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Link til Datapolitik"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Fortrolighedspolitik, betingelser for brug, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-kode"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("QR-scanning kræver kameraadgang"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Hindbær, rød"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Modtagere"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Opdater"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Påmind mig"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Påmindelser"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Påmindelser er blevet ændret!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Svar"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Svar til alle"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rapporter et problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Anmod om hjælp til login"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Knap til anmodning om hjælp til login"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Genstart app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Prøv igen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Retur til login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ELEV"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Skærmbillede, der viser placeringen af QR-kodegenerering i browseren"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Skærmbillede, der viser placering af QR-koden i Åbn Canvas-appen for elev"),
    "Select" : MessageLookupByLibrary.simpleMessage("Vælg"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Vælg denne elev farve"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Vælg modtagere"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Send feedback"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Send en besked om denne opgave"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Send en besked om dette fag"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Send besked"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Indstil en dato og tid for, hvornår der skal meddeles om denne begivenhed."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Indstil en dato og tid for at blive meddelt om denne specifikke opgave."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Indstillinger"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Firkløver, grøn"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Del din kærlighed for denne app"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Vis adgangskode"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Log på"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Der er noget, der ikke virker, men jeg kan godt få det gjort, jeg skal gøre."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Hold op med at fungere som bruger"),
    "Student" : MessageLookupByLibrary.simpleMessage("Elev"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Parring af konti for elever"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Elever kan oprette en QR-kode ved hjælp af Canvas-appen for elev på deres mobilenhed"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Elever kan få en parringskode via Canvas-webstedet"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Emne"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Afleveret"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Blev indsendt!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sammendrag"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Skift brugere"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Fagplan"),
    "TA" : MessageLookupByLibrary.simpleMessage("Undervisningsassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LÆRER"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tryk for at få vist dine favoritfag i kalenderen. Vælg op til 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tryk for at parre med en ny elev"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tryk for at vælge denne elev"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tryk for at vise elevvælger"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lærer"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Fortæl os om, hvad du bedst kan lide af denne app"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Betingelser for service"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Link til Betingelser for service"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Betingelser for brug"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Den QR-kode, du scannede, kan være udløbet. Opdater koden på elevens enhed, og prøv igen."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Følgende oplysninger hjælper os med bedre at forstå din idé:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Den server, du indtastede, er ikke autoriseret til denne app."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Den elev, du prøver at tilføje, tilhører en anden skole. Log ind eller opret en konto med denne skole for at scanne denne kode."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Brugeragenten for denne app er ikke autoriseret."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Der er ingen installerede applikationer, der kan åbne denne fil"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Der er ingen tilgængelige sideinformationer."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Der opstod et problem ved indlæsning af Betingelser for brug"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Der opstod et problem under forsøg på at fjerne denne elev fra din konto. Kontrollér forbindelsen, og prøv igen."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af modtagere for dette fag"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved indlæsning af sammendragsoplysningerne for dette fag."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af denne besked"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af denne diskussion"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved indlæsning af denne fil"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af beskederne i indbakken."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af elevens varslinger."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af elevens kalender"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af eleverne."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af elevens fag."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved indlogning. Generer en QR-kode til, og prøv igen."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved at fungere som denne bruger. Kontroller domænet og bruger-ID\'et, og prøv igen."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Der er intet at blive underrettet om endnu."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Denne app er ikke autoriseret for brug."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Dette fag har endnu ingen opgaver eller kalenderbegivenheder."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Denne fil understøttes ikke og kan ikke vises i appen"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Dette vil fjerne parring og alle tilmeldinger for denne elev fra din konto."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Vurdering i alt"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Åh nej!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kan ikke hente fag. Kontrollér forbindelsen, og prøv igen."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Kan ikke indlæse dette billede"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Denne mediefil kunne ikke afspilles"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kan ikke sende besked. Kontrollér forbindelsen, og prøv igen."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Under opbygning"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Ukendt bruger"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Ugemte ændringer"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Ikke-understøttet fil"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Overfør fil"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Brug kamera"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Brug mørkt tema i webindhold"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Bruger-id"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Bruger ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versionsnummer"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Vis beskrivelse"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Vis fejldetaljer"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Vis datapolitikken"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Vi er i øjeblikket ved at bygge denne funktion."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Vi kan ikke vise dette link, måske hører det til en institution, som du i øjeblikket ikke er logget ind på."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Vi kunne ikke finde nogen elever tilknyttet den konto."),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Vi kunne ikke bekræfte serveren til brug med denne app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Vi ved ikke helt, hvad der skete, men det var ikke godt. Kontakt os, hvis dette fortsætter."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Hvad kan vi gøre bedre?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Du observerer ikke nogen elever."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Du kan kun vælge at vise 10 kalendere"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Du skal indtaste et bruger-ID"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Du skal indtaste et gyldigt domæne"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Du skal vælge mindst en kalender at vise"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Du får besked om denne opgave den …"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Du får besked om denne begivenhed den ..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Du finder QR-koden på nettet i din kontoprofil. Klik på \'QR for mobil login\' på listen."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Du skal åbne elevens Åbn Canvas-appen for elev for at fortsætte. Gå til Hovedmenu > Indstillinger> Par med observatør, og scan den QR-kode, du ser der."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Din kode er forkert eller udløbet."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Din elevs fag kan muligvis ikke offentliggøres endnu."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Du har set det hele!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Varslinger"),
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
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-vejledningerne"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas-support"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("skjul"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("skjult"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Fag"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hvordan finder jeg min skole eller distrikt?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Indtast skolens navn eller distrikt ..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("udvid"),
    "expanded" : MessageLookupByLibrary.simpleMessage("udvidet"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Find skole"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("mig"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Næste"),
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
    "unread" : MessageLookupByLibrary.simpleMessage("ulæst"),
    "unreadCount" : m47
  };
}
