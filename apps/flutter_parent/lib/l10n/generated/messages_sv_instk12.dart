// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a sv_instk12 locale. All the
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
  String get localeName => 'sv_instk12';

  static m0(userName) => "Du agerar som ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Uppgiftsbedömning över ${threshold}";

  static m3(threshold) => "Uppgiftsbedömning under ${threshold}";

  static m4(moduleName) => "Den här uppgiften har låsts av modulen \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Angående : ${studentName}, uppgift – ${assignmentName}";

  static m6(points) => "${points} poäng";

  static m7(points) => "${points} poäng";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} till 1 annan', other: '${authorName} till ${howMany} andra')}";

  static m9(authorName, recipientName) => "${authorName} till ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} till ${recipientName} och 1 annan', other: '${authorName} till ${recipientName} och ${howMany} andra')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Ändra färg för ${studentName}";

  static m13(score, pointsPossible) => "${score} av ${pointsPossible} poäng";

  static m14(studentShortName) => "för ${studentShortName}";

  static m15(threshold) => "Kursbedömning över ${threshold}";

  static m16(threshold) => "Kursbedömning under ${threshold}";

  static m17(date, time) => "${date} kl. ${time}";

  static m18(alertTitle) => "Avvisa ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Försök med att söka efter namnet på skolan eller distrikten du vill ansluta till, t.ex. “Allmänna skolan” eller “Skolor i Skåne”. Du kan även ange en Canvas-domän direkt, t.ex. “smith.instructure.com.”\n\nMer information om hur du kan hitta din institutions Canvas-konto finns på ${canvasGuides} eller kontakta ${canvasSupport} eller din skola för att få hjälp.";

  static m20(date, time) => "Ska lämnas in ${date} klockan ${time}";

  static m21(userName) => "Du kommer att sluta agera som ${userName} och loggas ut.";

  static m22(userName) => "Du kommer att sluta agera som ${userName} och återgå till ditt ursprungliga konto.";

  static m23(studentName, eventTitle) => "Angående : ${studentName}, händelse – ${eventTitle}";

  static m24(startAt, endAt) => "${startAt}/${endAt}";

  static m25(grade) => "Slutbedömning: ${grade}";

  static m26(studentName) => "Angående : ${studentName}, framsida";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Angående : ${studentName}, bedömningar";

  static m29(pointsLost) => "Förseningsbestraffning (-${pointsLost})";

  static m30(studentName, linkUrl) => "Angående : ${studentName}, ${linkUrl}";

  static m31(percentage) => "Måste vara över ${percentage}";

  static m32(percentage) => "Måste vara under ${percentage}";

  static m33(month) => "Nästa månad: ${month}";

  static m34(date) => "Nästa vecka börjar ${date}";

  static m35(query) => "Det gick inte att hitta skolar som matchar \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'En av 1 poäng', other: 'En av ${points} poäng')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} möjliga poäng";

  static m39(month) => "Föregående månad: ${month}";

  static m40(date) => "Föregående vecka startade ${date}";

  static m41(termsOfService, privacyPolicy) => "Genom att trycka på Skapa konto samtycker du till ${termsOfService} och ${privacyPolicy}";

  static m42(version) => "Förslag för Android – Canvas Parent ${version}";

  static m43(month) => "Månaden ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} stjärna', other: '${position} stjärnor')}";

  static m45(date, time) => "Uppgiften lämnades in ${date} kl. ${time} och väntar på bedömning";

  static m46(studentName) => "Angående : ${studentName}, kursöversikt";

  static m47(count) => "${count} olästa";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Att uppträda som\", loggar huvudsakligen in som den här användaren utan lösenord. Du kommer att kunna vidta åtgärder som om du var den här användaren, och från andra användares synpunkter kommer det att upplevas som om den här användaren utförde dem. I revisionsloggar registreras dock att du var den som utförde åtgärderna på den här användarens vägnar."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Beskrivning är obligatorisk."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Ett nätverksfel inträffade när du lade till den här eleven. Kontrollera din anslutning och försök igen."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Ämne är obligatoriskt."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Agera som användare"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Lägg till elev"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Lägg till bilaga"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Lägg till ny elev"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Lägg till elev med..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Notisinställningar"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Notifiera mig om..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alla bedömningsperioder"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Har du redan ett konto? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("E-postadressen är obligatorisk."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Ett fel uppstod när länken skulle visas"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Ett fel inträffade när ditt val sparades. Försök igen."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Ett oväntat fel inträffade"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android AS-version"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Utseende"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Programversion"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Är du elev eller lärare?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Vill du logga ut?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Är du säker på att du vill stänga den här sidan? Ditt meddelande du ännu inte skickat kommer att tas bort."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Uppgiftens detaljer"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Uppgiftsbedömning över"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Uppgiftsbedömning under"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Uppgift saknas"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, Fuschia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendrar"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Kamerabehörighet"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Avbryt"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas-elev"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas-lärare"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas på GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Välj en kurs att skicka meddelande till"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Välj från galleri"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Fullgjord"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Kontakta support"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Kursmeddelande"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Kursmeddelande"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Kursbedömning över"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Kursbedömning under"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Skapa konto"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mörkt läge"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Ta bort"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beskrivning"),
    "Device" : MessageLookupByLibrary.simpleMessage("Enhet"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Enhetsmodell"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domän"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domän:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Visa inte igen"),
    "Done" : MessageLookupByLibrary.simpleMessage("Klar"),
    "Download" : MessageLookupByLibrary.simpleMessage("Ladda ned"),
    "Due" : MessageLookupByLibrary.simpleMessage("Inlämningsdatum"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EXTREMT KRITISKT NÖDFALL!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elektrisk, blå"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-postadress"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-post:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-post …"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Ange den elevparkopplingskod du har fått. Om parkopplingskoden inte fungerar kan den ha gått ut"),
    "Event" : MessageLookupByLibrary.simpleMessage("Händelse"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Ursäktad"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Utgången QR-kod"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Misslyckades. Tryck för alternativ."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtrera"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrera efter"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Eld, orange"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Framsida"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Fullständigt namn"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Fullständigt namn ..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Fullständigt felmeddelande"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Gå till i dag"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Bedömning"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Bedömningsprocent"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Har bedömts"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Bedömningar"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjälp"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Dölj lösenord"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Högt kontrastläge"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Hur går det för oss?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hur påverkar detta dig?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Jag kan inte göra något tills jag hör ifrån er."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Jag har inte ett Canvas-konto"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Jag har ett Canvas-konto"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Jag behöver hjälp men det är inte bråttom."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Jag kan inte logga in"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idéer för appen Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("För att tillhandahålla dig en förbättrad upplevelse har vi uppdaterat påminnelsefunktionen. Du kan lägga till nya påminnelser genom att visa en uppgift eller en kalenderhändelse och trycka på reglaget under avsnittet \"Påminn mig\".\n\nTänk på att påminnelser som skapats i äldre versionen av den här appen inte är kompatibla med de nya ändringarna och måste därför skapas om igen."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Inkorg"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Inkorg noll"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ej fullständig"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Felaktig domän"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institutionsmeddelande"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institutionsmeddelande"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruktioner"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interaktioner på den här sidan har begränsats av din institution."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Ogiltig QR-kod"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Det verkar vara en bra dag för vila, avslappning och omladdning."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Det verkar som om inga uppgifter har skapats för den här platsen än."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Bara en vanlig fråga, kommentar, idé, förslag ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Sen"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Starta externt verktyg"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Juridik"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Ljust läge"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Länkfel"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Plats:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Lokalisera QR-kod"),
    "Location" : MessageLookupByLibrary.simpleMessage("Plats"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Låst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Logga ut"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Inloggningsflöde: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Inloggningsflöde: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Inloggningsflöde: Webbplatsadmin"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Inloggningsflöde: Hoppa över mobilverifiering"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Hantera elever"),
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
    "No Grade" : MessageLookupByLibrary.simpleMessage("Ingen bedömning"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Ingen plats specificerad"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Inga elever"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Inget ämne"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Ingen sammanfattning"),
    "No description" : MessageLookupByLibrary.simpleMessage("Ingen beskrivning"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Inga mottagare har valts"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Inte bedömd"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Inte inlämnad"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Är du inte vårdnadshavare?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Påminnelsenotiser om uppgifter och kalenderhändelser"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Operativsystemets version"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observatör"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Någon av våra andra appar kan vara bättre lämpade. Tryck på en för att besöka Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Öppna Canvas Elev"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Öppna i webbläsare"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Öppna med en annan app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Parkopplingskod"),
    "Password" : MessageLookupByLibrary.simpleMessage("Lösenord"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Lösenord krävs"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Lösenord måste innehålla minst 8 tecken"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Lösenord ..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Planner-anteckning"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Ange en giltig e-postadress"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Ange en e-postadress"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Ange fullständigt namn"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Skanna en QR-kod som genererats i Canvas."),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Plommon, lila"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Förbereder..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Tidigare inloggningar"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Integritetspolicy"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Länk till sekretesspolicy"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Integritetspolicy, användarvillkor, öppen källkod"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-kod"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("QR-skanning kräver kameraåtkomst"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Hallon, röd"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Mottagare"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Uppdatera"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Påminn mig"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Påminnelser"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Påminnelser har ändrats!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Svara"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Svara alla"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rapportera ett problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Begär hjälp med inloggning"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Hjälpknapp för inloggningsförfrågningar"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Starta om appen"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Försök igen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Gå tillbaka till inloggning"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ELEV"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Skärmdumpen visar platsen för QR-kodgenerering i webbläsaren"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Skärmbild som visar var QR-kodens parkopplingsgenerering görs i Canvas Elev-appen"),
    "Select" : MessageLookupByLibrary.simpleMessage("Välj"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Välj elevfärg"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Välj mottagare"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Skicka återkoppling"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Skicka ett meddelande om den här kursen"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Skicka ett meddelande om den här kursen"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Skicka meddelandet"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Ange ett datum och tid för att få en notis för den här händelsen."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Ange ett datum och tid för att få en notis för den här specifika uppgiften."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Inställningar"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Klöver, grön"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Dela din kärlek till appen"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Visa lösenord"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Logga in"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Något gick fel när ditt konto skulle skapas. Kontakta din skola för hjälp."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Det är något som inte fungerar men jag kan göra det jag ska ändå."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Sluta att agera som en användare"),
    "Student" : MessageLookupByLibrary.simpleMessage("Elev"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Elev-parkoppling"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Elever kan skapa en QR-kod i Canvas Elev-appen i sina mobiler."),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Elever kan få en parkopplingskod genom att använda Canvas Elev-appen i sina mobiler."),
    "Subject" : MessageLookupByLibrary.simpleMessage("Ämne"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Inskickad"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Uppgiften har skickats in!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sammanfattning"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Växla användare"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Kursöversikt"),
    "TA" : MessageLookupByLibrary.simpleMessage("Lärarassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LÄRARE"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tryck för att favoritmarkera de kurser du vill se i kalendern. Välj upp till 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tryck för att koppla samman med en ny elev"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tryck för att välja den här eleven"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tryck för att visa elevväljare"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lärare"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Berätta vad du tycker om mest med appen"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Tjänstvillkor"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Länk till tjänstvillkor"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Användningsvillkor"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("QR-koden du skannade kan har gått ut. Uppdatera koden på elevens enhet och försök igen."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Följande information kommer att hjälpa oss att förstå din idé bättre:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Den server du har angett har inte auktoriserats för den här appen."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Eleven du försöker lägga tillhör en annan skola. Logga in eller skapa ett konto med den skolans för att skanna den här koden."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Användaragenten för den här appen är inte auktoriserad."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Det finns inga installerade program som kan öppna den här filen"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Det finns ingen sidinformation tillgänglig."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in användarvillkoren"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Det gick inte att ta bort eleven från ditt konto. Kontrollera din anslutning och försök igen."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in mottagare för den här kursen"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in sammanfattningsinformationen för den här kursen."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in det här meddelandet"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in den här konversationen"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in den här filen"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in dina meddelanden i inkorgen."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in din elevs notiser."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in din elevkalender."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in dina elever."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in din elevs kurser."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Det gick inte att logga in. Generera en annan QR-kod och försök igen."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Ett fel inträffade under Agera som den här användaren. Kontrollera domänen och användar-ID:t och försök igen."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Det finns inget att avisera om än."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Den här appen har inte auktoriserats för användning."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Den här kursen har inga uppgifter eller kalenderhändelser än."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Den här filen stöds inte och kan inte visas i appen"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Detta kommer att koppla från och ta bort alla registreringar för den här Eleven från ditt konto."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Totalt bedömning"),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Använd mörkt tema i webbinnehållet"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Användar-ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Användar-ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versionsnummer"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Visa beskrivning"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visa felinformation"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Visa sekretesspolicyn"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Vi bygger den här funktionen för dig."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Det går inte att visa den här länken. Den kan tillhöra en institution du för närvarande inte är inloggad på."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Det gick inte att hitta elever kopplade till det här kontot"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Vi kunde inte verifiera servern för användning med den här appen."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Vi vet inte vad som hände, men det fungerar inte. Kontakta oss om detta fortsätter att inträffa."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Vad kan vi förbättra?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Du observerar inga elever."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Du kan endast välja 10 kalendrar att visa"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Du måste ange ett giltigt användar-ID"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Du måste ange en giltig domän"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Du måste välja minst en kalender att visa"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Du kommer att få en notis om den här uppgiften på..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Du kommer att få en notis om den här händelsen den..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Du hittar QR-koden på webben i din kontoprofil. Klicka på \"QR för mobil inloggning\" i listan."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Du måste öppna din Elevs Canvas Elev-app för att fortsätta. Gå till Huvudmenyn > Inställningar > Parkoppla med observatör och skanna QR-koden du ser där."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Din kod är fel eller har gått ut."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Dina elevkurser kanske inte publicerats än."),
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
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("dölj"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("minimerad"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kurser"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hur hittar jag min skola eller distrikt?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Ange skolans namn eller distrikt..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("visa"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expanderad"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Sök skola"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("jag"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Nästa"),
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
    "send" : MessageLookupByLibrary.simpleMessage("skicka"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("olästa"),
    "unreadCount" : m47
  };
}
