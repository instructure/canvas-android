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

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Opgavevurdering over ${threshold}";

  static m2(threshold) => "Opgavevurdering under ${threshold}";

  static m3(moduleName) => "Denne opgave er låst af forløbet \"${moduleName}\".";

  static m4(studentName, assignmentName) => "Vedrørende: ${studentName}, Opgave - ${assignmentName}";

  static m5(points) => "${points} point";

  static m6(points) => "${points} point";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} til 1 anden', other: '${authorName} til ${howMany} andre')}";

  static m8(authorName, recipientName) => "${authorName} til ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} til ${recipientName} & 1 anden', other: '${authorName} til ${recipientName} & ${howMany} andre')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} ud af ${pointsPossible} point";

  static m12(studentShortName) => "for ${studentShortName}";

  static m13(threshold) => "Fagvurdering oven ${threshold}";

  static m14(threshold) => "Fagvurdering under ${threshold}";

  static m15(date, time) => "${date} kl. ${time}";

  static m16(canvasGuides, canvasSupport) => "Prøv at søge efter navnet på den skole eller det distrikt, du forsøger at få adgang til, fx “Smith Private School” eller “Smith County Schools.” Du kan også indtaste et Canvas-domæne direkte, som fx “smith.instructure.com.”\n\nFor mere information om hvordan du finder din institutions Canvas-konto, kan du besøge ${canvasGuides}, kontakte ${canvasSupport} eller kontakte din skole for at få hjælp.";

  static m17(date, time) => "Forfalder d. ${date} kl. ${time}";

  static m18(studentName, eventTitle) => "Vedrørende: ${studentName}, Begivenhed - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Endelig vurdering: ${grade}";

  static m21(studentName) => "Vedrørende: ${studentName}, Forside";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Vedrørende: ${studentName}, Vurderinger";

  static m24(pointsLost) => "Straf for sen aflevering (-${pointsLost})";

  static m25(studentName, linkUrl) => "Vedrørende: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Skal være over ${percentage}";

  static m27(percentage) => "Skal være under ${percentage}";

  static m28(month) => "Næste måned: ${month}";

  static m29(date) => "Næste uge, der starter ${date}";

  static m30(query) => "Kan ikke finde skoler, der matcher \"${query}\"";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Ud af 1 point', other: 'Ud af ${points} point')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} mulige point";

  static m34(month) => "Forrige måned: ${month}";

  static m35(date) => "Forrige uge, der starter ${date}";

  static m36(month) => "Måneden ${month}";

  static m37(date, time) => "Denne opgave blev afleveret den ${date} kl. ${time} og venter på at blive bedømt";

  static m38(studentName) => "Vedrørende: ${studentName}, Fagplan";

  static m39(count) => "${count} ulæst";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Der kræves en beskrivelse."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Der kræves et emne."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Tilføj elever"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Tilføj vedhæftet fil"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Tilføj ny elev"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Tilføj elev med ..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Varslingsindstillinger"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Send mig en varsling når ..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alle vurderingsperioder"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Der kræves en e-mailadresse."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved forsøg på at vise dette link"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendere"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mørk tilstand"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dato"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Slet"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beskrivelse"),
    "Device" : MessageLookupByLibrary.simpleMessage("Enhed"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Enhedsmodel"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domæne:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Udført"),
    "Download" : MessageLookupByLibrary.simpleMessage("Download"),
    "Due" : MessageLookupByLibrary.simpleMessage("Forfalder"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EKSTREMT KRITISK NØDSTILFÆLDE!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-mail-adresse"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-mail:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Indtast elevens parringskode, der er blevet givet til dig. Hvis parringskoden ikke fungerer, kan den være udløbet"),
    "Event" : MessageLookupByLibrary.simpleMessage("Begivenhed"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Undskyldt"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Mislykkedes. Tryk for indstillinger."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrere efter"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Forside"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Hel fejlmeddelelse"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Vurdering"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Vurdering procent"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Bedømt"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Vurderinger"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjælp"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Høj kontrast-tilstand"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hvordan påvirker det dig?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Jeg kan ikke fortsætte, før jeg har fået svar."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Jeg behøver hjælp, men det er ikke presserende."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Jeg har problemer med at logge ind"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ide for Canvas Parent App [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Indbakke"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Indbakke nul"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ufuldstændig"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Besked til institutionen"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institutionsmeddelelser"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruktioner"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Det er en alle tiders dag til at tage den med ro og slappe af."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Det ser ud til, at opgaver ikke er blevet oprettet i dette rum endnu."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Blot et simpelt spørgsmål, kommentar, ide, forslag..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Sen"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Start eksternt værktøj"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Juridisk"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Lys tilstand"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Linkfejl"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Sted:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Placering"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Låst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Log ud"),
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
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Åbn i browser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Åbn med en anden app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Parringskode"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Forbereder ..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Tidligere logins"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Datapolitik"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Fortrolighedspolitik, betingelser for brug, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-kode"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Modtagere"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Påmind mig"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Påmindelser"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Svar"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Svar til alle"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rapporter et problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Anmod om hjælp til login"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Knap til anmodning om hjælp til login"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Genstart app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Prøv igen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Retur til login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ELEV"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Vælg modtagere"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Send en besked om denne opgave"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Send en besked om dette fag"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Send besked"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Indstil en dato og tid for, hvornår der skal meddeles om denne begivenhed."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Indstil en dato og tid for at blive meddelt om denne specifikke opgave."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Sæt påmindelsesskifter"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Indstillinger"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Del din kærlighed for denne app"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Der er noget, der ikke virker, men jeg kan godt få det gjort, jeg skal gøre."),
    "Student" : MessageLookupByLibrary.simpleMessage("Elev"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Emne"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Afleveret"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Blev indsendt!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sammendrag"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Skift brugere"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Fagplan"),
    "TA" : MessageLookupByLibrary.simpleMessage("Undervisningsassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LÆRER"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tryk for at få vist dine favoritfag i kalenderen."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tryk for at parre med en ny elev"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tryk for at vælge denne elev"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tryk for at vise elevvælger"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lærer"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Fortæl os om, hvad du bedst kan lide af denne app"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Betingelser for brug"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Følgende oplysninger hjælper os med bedre at forstå din idé:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Den server, du indtastede, er ikke autoriseret til denne app."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Brugeragenten for denne app er ikke autoriseret."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Der er ingen installerede applikationer, der kan åbne denne fil"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Der er ingen tilgængelige sideinformationer."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Der opstod et problem ved indlæsning af Betingelser for brug"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af modtagere for dette fag"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved indlæsning af sammendragsoplysningerne for dette fag."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af denne besked"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af denne diskussion"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl ved indlæsning af denne fil"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af beskederne i indbakken."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af elevens varslinger."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af elevens kalender"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsning af eleverne."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Der opstod en fejl under indlæsningen af din elevs fag."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Der er intet at blive underrettet om endnu."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Denne app er ikke autoriseret for brug."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Dette fag har endnu ingen opgaver eller kalenderbegivenheder."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Denne fil understøttes ikke og kan ikke vises i appen"),
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
    "User ID:" : MessageLookupByLibrary.simpleMessage("Bruger ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versionsnummer"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Vis fejldetaljer"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Vi er i øjeblikket ved at bygge denne funktion."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Vi kan ikke vise dette link, måske hører det til en institution, som du i øjeblikket ikke er logget ind på."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Vi kunne ikke finde nogen elever tilknyttet den konto."),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Vi kunne ikke bekræfte serveren til brug med denne app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Vi ved ikke helt, hvad der skete, men det var ikke godt. Kontakt os, hvis dette fortsætter."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Du observerer ikke nogen elever."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Du får besked om denne opgave den …"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Du får besked om denne begivenhed den ..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Din kode er forkert eller udløbet."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Din elevs fag kan muligvis ikke offentliggøres endnu."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Du har set det hele!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Varslinger"),
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
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-vejledningerne"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas-support"),
    "collapse" : MessageLookupByLibrary.simpleMessage("skjul"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("skjult"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Fag"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("afvis"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hvordan finder jeg min skole eller distrikt?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Indtast skolens navn eller distrikt ..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("udvid"),
    "expanded" : MessageLookupByLibrary.simpleMessage("udvidet"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Find skole"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("mig"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Næste"),
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
    "unread" : MessageLookupByLibrary.simpleMessage("ulæst"),
    "unreadCount" : m39
  };
}
