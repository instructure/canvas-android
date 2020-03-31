// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a en_GB_instukhe locale. All the
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
  String get localeName => 'en_GB_instukhe';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Uppgiftsbedömning över ${threshold}";

  static m2(threshold) => "Uppgiftsbedömning under ${threshold}";

  static m3(moduleName) => "Den här uppgiften har låsts av modulen \"${moduleName}\".";

  static m4(studentName, assignmentName) => "Angående : ${studentName}, uppgift – ${assignmentName}";

  static m5(points) => "${points} poäng";

  static m6(points) => "${points} poäng";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} till 1 annan', other: '${authorName} till ${howMany} andra')}";

  static m8(authorName, recipientName) => "${authorName} till ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} till ${recipientName} och 1 annan', other: '${authorName} till ${recipientName} och ${howMany} andra')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} av ${pointsPossible} poäng";

  static m12(studentShortName) => "för ${studentShortName}";

  static m13(threshold) => "Kursbedömning över ${threshold}";

  static m14(threshold) => "Kursbedömning under ${threshold}";

  static m15(date, time) => "${date} kl. ${time}";

  static m16(canvasGuides, canvasSupport) => "Försök med att söka efter namnet på skolan eller distrikten du vill ansluta till, t.ex. “Allmänna skolan” eller “Skolor i Skåne”. Du kan även ange en Canvas-domän direkt, t.ex. “smith.instructure.com.”\n\nMer information om hur du kan hitta din institutions Canvas-konto finns på ${canvasGuides} eller kontakta ${canvasSupport} eller din skola för att få hjälp.";

  static m17(date, time) => "Ska lämnas in ${date} klockan ${time}";

  static m18(studentName, eventTitle) => "Angående : ${studentName}, händelse – ${eventTitle}";

  static m19(startAt, endAt) => "${startAt}/${endAt}";

  static m20(grade) => "Slutbedömning: ${grade}";

  static m21(studentName) => "Angående : ${studentName}, framsida";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Angående : ${studentName}, bedömningar";

  static m24(pointsLost) => "Förseningsbestraffning (-${pointsLost})";

  static m25(studentName, linkUrl) => "Angående : ${studentName}, ${linkUrl}";

  static m26(percentage) => "Måste vara över ${percentage}";

  static m27(percentage) => "Måste vara under ${percentage}";

  static m30(query) => "Det gick inte att hitta skolar som matchar \"${query}\"";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'En av 1 poäng', other: 'En av ${points} poäng')}";

  static m32(count) => "+${count}";

  static m37(date, time) => "Uppgiften lämnades in ${date} kl. ${time} och väntar på bedömning";

  static m38(studentName) => "Angående : ${studentName}, kursöversikt";

  static m39(count) => "${count} olästa";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Beskrivning är obligatorisk."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Ämne är obligatoriskt."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Lägg till elev"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Lägg till bilaga"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Lägg till ny elev"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Lägg till elev med..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Notisinställningar"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Notifiera mig om..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alla bedömningsperioder"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("E-postadressen är obligatorisk."),
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
    "Cancel" : MessageLookupByLibrary.simpleMessage("Avbryt"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas-elev"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas-lärare"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Välj en kurs att skicka meddelande till"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Välj från galleri"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Fullgjord"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Kontakta support"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Kursmeddelande"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Kursmeddelande"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Kursbedömning över"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Kursbedömning under"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mörkt läge"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Ta bort"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beskrivning"),
    "Device" : MessageLookupByLibrary.simpleMessage("Enhet"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Enhetsmodell"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domän:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Klar"),
    "Download" : MessageLookupByLibrary.simpleMessage("Ladda ned"),
    "Due" : MessageLookupByLibrary.simpleMessage("Inlämningsdatum"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EXTREMT KRITISKT NÖDFALL!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-postadress"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-post:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Ange den elevparkopplingskod du har fått. Om parkopplingskoden inte fungerar kan den ha gått ut"),
    "Event" : MessageLookupByLibrary.simpleMessage("Händelse"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Ursäktad"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Misslyckades. Tryck för alternativ."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtrera"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrera efter"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Framsida"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Fullständigt felmeddelande"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Bedömning"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Bedömningsprocent"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Har bedömts"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Bedömningar"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjälp"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Högt kontrastläge"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hur påverkar detta dig?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Jag kan inte göra något tills jag hör ifrån er."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Jag behöver hjälp men det är inte bråttom."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Jag kan inte logga in"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idéer för appen Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Inkorg"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Inkorg noll"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ej fullständig"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institutionsmeddelande"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institutionsmeddelande"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruktioner"),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Det verkar som om inga uppgifter har skapats för den här platsen än."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Bara en vanlig fråga, kommentar, idé, förslag ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Sen"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Juridik"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Ljust läge"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Plats:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Plats"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Låst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Logga ut"),
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
    "No Grade" : MessageLookupByLibrary.simpleMessage("Ingen bedömning"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Ingen plats specificerad"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Inga elever"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Inget ämne"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Ingen sammanfattning"),
    "No description" : MessageLookupByLibrary.simpleMessage("Ingen beskrivning"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Inga mottagare har valts"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Inte inlämnad"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Är du inte vårdnadshavare?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Påminnelsenotiser om uppgifter och kalenderhändelser"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Operativsystemets version"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observatör"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Någon av våra andra appar kan vara bättre lämpade. Tryck på en för att besöka Play Store."),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Öppna med en annan app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Parkopplingskod"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Förbereder..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Tidigare inloggningar"),
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
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ELEV"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Välj mottagare"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Skicka ett meddelande om den här kursen"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Skicka ett meddelande om den här kursen"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Skicka meddelandet"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Ange ett datum och tid för att få en notis för den här händelsen."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Ange ett datum och tid för att få en notis för den här specifika uppgiften."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Ange påminnelse"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Inställningar"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Dela din kärlek till appen"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Det är något som inte fungerar men jag kan göra det jag ska ändå."),
    "Student" : MessageLookupByLibrary.simpleMessage("Elev"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Ämne"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Inskickad"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Uppgiften har skickats in!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sammanfattning"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Växla användare"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Kursöversikt"),
    "TA" : MessageLookupByLibrary.simpleMessage("Lärarassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("LÄRARE"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tryck för att koppla samman med en ny elev"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tryck för att välja den här eleven"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tryck för att visa elevväljare"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Lärare"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Berätta vad du tycker om mest med appen"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Följande information kommer att hjälpa oss att förstå din idé bättre:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Den server du har angett har inte auktoriserats för den här appen."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Användaragenten för den här appen är inte auktoriserad."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Det finns inga installerade program som kan öppna den här filen"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Det finns ingen sidinformation tillgänglig."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in mottagare för den här kursen"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in sammanfattningsinformationen för den här kursen."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in det här meddelandet"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in den här konversationen"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in den här filen"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in dina meddelanden i inkorgen."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in din elevs notiser."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in dina elever."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Det gick inte att läsa in din elevs kurser."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Det finns inget att avisera om än."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Den här appen har inte auktoriserats för användning."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Den här kursen har inga uppgifter eller kalenderhändelser än."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Den här filen stöds inte och kan inte visas i appen"),
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
    "User ID:" : MessageLookupByLibrary.simpleMessage("Användar-ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versionsnummer"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visa felinformation"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Vi bygger den här funktionen för dig."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Det gick inte att hitta elever kopplade till det här kontot"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Vi kunde inte verifiera servern för användning med den här appen."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Vi vet inte vad som hände, men det fungerar inte. Kontakta oss om detta fortsätter att inträffa."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Du observerar inga elever."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Du kommer att få en notis om den här uppgiften på..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Du kommer att få en notis om den här händelsen den..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Din kod är fel eller har gått ut."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Dina elevkurser kanske inte publicerats än."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Du är helt i kapp!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Notiser"),
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
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logotyp"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas Support"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("minimerad"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kurser"),
    "dateAtTime" : m15,
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hur hittar jag min skola eller distrikt?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Ange skolans namn eller distrikt..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expanded" : MessageLookupByLibrary.simpleMessage("expanderad"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Sök skola"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("jag"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Nästa"),
    "noDomainResults" : m30,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m31,
    "plusRecipientCount" : m32,
    "send" : MessageLookupByLibrary.simpleMessage("skicka"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("olästa"),
    "unreadCount" : m39
  };
}
