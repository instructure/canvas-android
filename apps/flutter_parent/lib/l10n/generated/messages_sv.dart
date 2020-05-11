// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a sv locale. All the
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
  String get localeName => 'sv';

  static m0(userName) => "Du agerar som ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Uppgiftsomdöme över ${threshold}";

  static m3(threshold) => "Uppgiftsomdöme under ${threshold}";

  static m4(moduleName) => "Den här uppgiften har låsts av modulen \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Angående : ${studentName}, uppgift – ${assignmentName}";

  static m6(points) => "${points} poäng";

  static m7(points) => "${points} poäng";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} till 1 annan', other: '${authorName} till ${howMany} andra')}";

  static m9(authorName, recipientName) => "${authorName} till ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} till ${recipientName} och 1 annan', other: '${authorName} till ${recipientName} och ${howMany} andra')}";

  static m11(count) => "${count}+";

  static m12(score, pointsPossible) => "${score} av ${pointsPossible} poäng";

  static m13(studentShortName) => "för ${studentShortName}";

  static m14(threshold) => "Kursomdöme över ${threshold}";

  static m15(threshold) => "Kursomdöme under ${threshold}";

  static m16(date, time) => "${date} kl. ${time}";

  static m17(canvasGuides, canvasSupport) => "Försök med att söka efter namnet på skolan eller distrikten du vill ansluta till, t.ex. “Allmänna skolan” eller “Skolor i Skåne”. Du kan även ange en Canvas-domän direkt, t.ex. “smith.instructure.com.”\n\nMer information om hur du kan hitta din institutions Canvas-konto finns på ${canvasGuides} eller kontakta ${canvasSupport} eller din skola för att få hjälp.";

  static m18(date, time) => "Ska lämnas in ${date} klockan ${time}";

  static m19(userName) => "Du kommer att sluta agera som ${userName} och loggas ut.";

  static m20(userName) => "Du kommer att sluta agera som ${userName} och återgå till ditt ursprungliga konto.";

  static m21(studentName, eventTitle) => "Angående : ${studentName}, händelse – ${eventTitle}";

  static m22(startAt, endAt) => "${startAt}/${endAt}";

  static m23(grade) => "Totalt omdöme: ${grade}";

  static m24(studentName) => "Angående : ${studentName}, framsida";

  static m25(score, pointsPossible) => "${score}/${pointsPossible}";

  static m26(studentName) => "Angående : ${studentName}, omdömen";

  static m27(pointsLost) => "Förseningsbestraffning (-${pointsLost})";

  static m28(studentName, linkUrl) => "Angående : ${studentName}, ${linkUrl}";

  static m29(percentage) => "Måste vara över ${percentage}";

  static m30(percentage) => "Måste vara under ${percentage}";

  static m31(month) => "Nästa månad: ${month}";

  static m32(date) => "Nästa vecka börjar ${date}";

  static m33(query) => "Det gick inte att hitta skolar som matchar \"${query}\"";

  static m34(points, howMany) => "${Intl.plural(howMany, one: 'En av 1 poäng', other: 'En av ${points} poäng')}";

  static m35(count) => "+${count}";

  static m36(points) => "${points} möjliga poäng";

  static m37(month) => "Föregående månad: ${month}";

  static m38(date) => "Föregående vecka startade ${date}";

  static m39(month) => "Månaden ${month}";

  static m40(date, time) => "Uppgiften lämnades in ${date} kl. ${time} och väntar på bedömning";

  static m41(studentName) => "Angående : ${studentName}, kursöversikt";

  static m42(count) => "${count} olästa";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Att uppträda som\", loggar huvudsakligen in som den här användaren utan lösenord. Du kommer att kunna vidta åtgärder som om du var den här användaren, och från andra användares synpunkter kommer det att upplevas som om den här användaren utförde dem. I historik-loggar registreras dock att du var den som utförde åtgärderna på den här användarens vägnar."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Beskrivning är obligatorisk."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Ämne är obligatoriskt."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Agera som användare"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Lägg till student"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Lägg till bilaga"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Lägg till ny student"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Lägg till student med..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Notisinställningar"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Notifiera mig om..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alla bedömningsperioder"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("E-postadressen är obligatorisk."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Ett fel uppstod när länken skulle visas"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Ett oväntat fel inträffade"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android AS-version"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Utseende"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Programversion"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Är du student eller lärare?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Vill du logga ut?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Är du säker på att du vill stänga den här sidan? Ditt meddelande du ännu inte skickat kommer att tas bort."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Uppgiftens detaljer"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Uppgiftsomdöme över"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Uppgiftsomdöme under"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Uppgift saknas"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendrar"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Avbryt"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas-student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas-lärare"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas på GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Välj en kurs att skicka meddelande till"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Välj från galleri"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Färdig"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Kontakta support"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Anslag"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Anslag"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Kursomdöme över"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Kursomdöme under"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mörkt läge"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Ta bort"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beskrivning"),
    "Device" : MessageLookupByLibrary.simpleMessage("Enhet"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Enhetsmodell"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domän"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domän:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Klar"),
    "Download" : MessageLookupByLibrary.simpleMessage("Ladda ned"),
    "Due" : MessageLookupByLibrary.simpleMessage("Inlämningsdatum"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EXTREMT KRITISKT NÖDFALL!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-postadress"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-post:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Ange den studentparkopplingskod du har fått. Om parkopplingskoden inte fungerar kan den ha gått ut"),
    "Event" : MessageLookupByLibrary.simpleMessage("Händelse"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Ursäktad"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Misslyckades. Tryck för alternativ."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtrera"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrera efter"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Framsida"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Fullständigt felmeddelande"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Omdöme"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Omdömesprocent"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Har bedömts"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Omdömen"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjälp"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Högt kontrastläge"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hur påverkar detta dig?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Jag kan inte göra något tills jag hör ifrån er."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Jag behöver hjälp men det är inte bråttom."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Jag kan inte logga in"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idéer för appen Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Inkorg"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Inbox Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ej fullständig"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institutionsmeddelande"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institutionsannonseringar"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruktioner"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interaktioner på den här sidan har begränsats av din institution."),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Det verkar vara en bra dag för vila, avslappning och omladdning."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Det verkar som om inga uppgifter har skapats för den här platsen än."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Bara en vanlig fråga, kommentar, idé, förslag ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Sen"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Starta externt verktyg"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Juridik"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Ljust läge"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Länkfel"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Plats:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Plats"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Låst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Logga ut"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Inloggningsflöde: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Inloggningsflöde: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Inloggningsflöde: Webbplatsadmin"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Inloggningsflöde: Hoppa över mobilverifiering"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Hantera studenter"),
    "Message" : MessageLookupByLibrary.simpleMessage("Meddelande"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Meddelandeämne"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Saknad"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Måste vara under 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Nätverksfel"),
    "Never" : MessageLookupByLibrary.simpleMessage("Aldrig"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nytt meddelande"),
    "No" : MessageLookupByLibrary.simpleMessage("Nej"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Inga notiser"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Inga uppgifter"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Inga kurser"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Inget inlämningsdatum"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Inga händelser idag!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Ingen omdöme"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Ingen plats specificerad"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Inga studenter"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Inget ämne"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Ingen sammanfattning"),
    "No description" : MessageLookupByLibrary.simpleMessage("Ingen beskrivning"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Inga mottagare har valts"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Inte bedömd"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Inte inskickad"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Är du inte vårdnadshavare?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Påminnelsenotiser om uppgifter och kalenderhändelser"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Operativsystemets version"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observatör"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Någon av våra andra appar kan vara bättre lämpade. Tryck på en för att besöka Play Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Öppna i webbläsare"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Öppna med en annan app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Parkopplingskod"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Förbereder..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Tidigare inloggningar"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Integritetspolicy"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Integritetspolicy, användarvillkor, öppen källkod"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-kod"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Mottagare"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Påminn mig"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Påminnelser"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Svara"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Svara alla"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rapportera ett problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Begär hjälp med inloggning"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Hjälpknapp för inloggningsförfrågningar"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Starta om appen"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Försök igen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Gå tillbaka till inloggning"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("STUDENT"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Välj mottagare"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Skicka ett meddelande om den här kursen"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Skicka ett meddelande om den här kursen"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Skicka meddelandet"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Ange ett datum och tid för att få en notis för den här händelsen."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Ange ett datum och tid för att få en notis för den här specifika uppgiften."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Inställningar"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Dela din kärlek till appen"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Det är något som inte fungerar men jag kan göra det jag ska ändå."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Sluta att agera som en användare"),
    "Student" : MessageLookupByLibrary.simpleMessage("Student"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Ämne"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Skickad"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Uppgiften har skickats in!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sammanfattning"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Växla användare"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Kursöversikt"),
    "TA" : MessageLookupByLibrary.simpleMessage("Lärarassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LÄRARE"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tryck för att favoritmarkera de kurser du vill se i kalendern."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tryck för att koppla samman med en ny student"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tryck för att välja den här studenten"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tryck för att visa studentväljare"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lärare"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Berätta vad du tycker om mest med appen"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Användarvillkor"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Följande information kommer att hjälpa oss att förstå din idé bättre:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Den server du har angett har inte auktoriserats för den här appen."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Användaragenten för den här appen är inte auktoriserad."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Det finns inga installerade program som kan öppna den här filen"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Det finns ingen sidinformation tillgänglig."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in användarvillkoren"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in mottagare för den här kursen"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in sammanfattningsinformationen för den här kursen."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in den här annonsen"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in den här konversationen"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in den här filen"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in dina meddelanden i inkorgen."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in din students notiser."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in din studentkalender."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in dina studenter."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in din students kurser."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Ett fel inträffade under Agera som den här användaren. Kontrollera domänen och användar-ID:t och försök igen."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Det finns inget att avisera om än."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Den här appen har inte auktoriserats för användning."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Den här kursen har inga uppgifter eller kalenderhändelser än."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Den här filen stöds inte och kan inte visas i appen"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Total omdöme"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Oj då!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Det gick inte att hämta kurser. Kontrollera din anslutning och försök igen."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Det går inte att läsa in den här bilden"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Det går inte att spela upp den här mediefilen"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Det gick inte att skicka meddelandet. Kontrollera din anslutning och försök igen."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Under uppbyggnad"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Okänd användare"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Osparade ändringar"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Filtyp som inte stöds"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Ladda upp fil"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Använd kamera"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Användar-ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Användar-ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versionsnummer"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visa felinformation"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Vi bygger den här funktionen för dig."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Det går inte att visa den här länken. Den kan tillhöra en institution du för närvarande inte är inloggad på."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Det gick inte att hitta studenter kopplade till det här kontot"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Vi kunde inte verifiera servern för användning med den här appen."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Vi vet inte vad som hände, men det fungerar inte. Kontakta oss om detta fortsätter att inträffa."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Du observerar inga studenter."),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Du måste ange ett giltigt användar-ID"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Du måste ange en giltig domän"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Du kommer att få en notis om den här uppgiften på..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Du kommer att få en notis om den här händelsen den..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Din kod är fel eller har gått ut."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Dina studentkurser kanske inte publicerats än."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Du är helt i kapp!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Notiser"),
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
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logotyp"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas Support"),
    "collapse" : MessageLookupByLibrary.simpleMessage("dölj"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("minimerad"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kurser"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hur hittar jag min skola eller distrikt?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Ange skolans namn eller distrikt..."),
    "dueDateAtTime" : m18,
    "endMasqueradeLogoutMessage" : m19,
    "endMasqueradeMessage" : m20,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("visa"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expanderad"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Sök skola"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("jag"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("Nästa"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("skicka"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("olästa"),
    "unreadCount" : m42
  };
}
