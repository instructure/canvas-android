// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a nl locale. All the
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
  String get localeName => 'nl';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Opdrachtcijfer hoger dan ${threshold}";

  static m2(threshold) => "Opdrachtcijfer lager dan ${threshold}";

  static m3(moduleName) => "Deze opdracht wordt vergrendeld door de module \"${moduleName}\".";

  static m4(studentName, assignmentName) => "Betreffende: ${studentName}, opdracht - ${assignmentName}";

  static m5(points) => "${points} punten";

  static m6(points) => "${points} punten";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} naar 1 andere', other: '${authorName} naar ${howMany} andere')}";

  static m8(authorName, recipientName) => "${authorName} naar ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} naar ${recipientName} en 1 andere', other: '${authorName} naar ${recipientName} en ${howMany} andere')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} van de ${pointsPossible} punten";

  static m12(studentShortName) => "voor ${studentShortName}";

  static m13(threshold) => "Cursuscijfer hoger dan ${threshold}";

  static m14(threshold) => "Cursuscijfer lager dan ${threshold}";

  static m15(date, time) => "${date} om ${time}";

  static m16(canvasGuides, canvasSupport) => "Zoek naar de gewenste naam van de school of onderwijskoepel, zoals “Spinozacollege” of “INNOVO”. Je kunt ook rechtstreeks een Canvas-domein invoeren, zoals “smith.instructure.com.”\n\nVoor meer informatie over het vinden van het Canvas-account van jouw instelling kun je de ${canvasGuides} bezoeken, contact opnemen met ${canvasSupport} of met je school zelf.";

  static m17(date, time) => "In te leveren op ${date} om ${time}";

  static m18(studentName, eventTitle) => "Betreffende: ${studentName}, gebeurtenis - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Eindcijfer: ${grade}";

  static m21(studentName) => "Betreffende: ${studentName}, voorpagina";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Betreffende: ${studentName}, cijfers";

  static m24(pointsLost) => "Sanctie indien te laat (-${pointsLost})";

  static m25(studentName, linkUrl) => "Betreffende: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Moet hoger zijn dan ${percentage}";

  static m27(percentage) => "Moet lager zijn dan ${percentage}";

  static m28(month) => "Volgende maand: ${month}";

  static m29(date) => "Volgende week vanaf ${date}";

  static m30(query) => "Kan geen scholen vinden met \"${query}\"";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Van 1 punt', other: 'Van ${points} punten')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} punten mogelijk";

  static m34(month) => "Vorige maand: ${month}";

  static m35(date) => "Vorige week vanaf ${date}";

  static m36(month) => "De maand ${month}";

  static m37(date, time) => "Deze opdracht werd ingeleverd op ${date} om ${time} en wacht op beoordeling";

  static m38(studentName) => "Betreffende: ${studentName}, syllabus";

  static m39(count) => "${count} ongelezen";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Er is een beschrijving vereist."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Er is een onderwerp vereist"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Cursist toevoegen"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Bijlage toevoegen"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Nieuwe cursist toevoegen"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Cursist toevoegen met…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Waarschuwingsinstellingen"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Waarschuw me als..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alle beoordelingsperioden"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Er is een e-mailadres vereist."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het weergeven van deze van link."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Er heeft zich een onverwachte fout voorgedaan"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS-versie"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Uiterlijk"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Versie van toepassing"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Ben je cursist of cursusleider?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Weet je zeker dat je je wilt afmelden?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Weet je zeker dat je deze pagina wilt sluiten? Je niet-verzonden bericht gaat verloren."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Opdrachtdetails"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Opdrachtcijfer hoger dan"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Opdrachtcijfer lager dan"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Opdracht ontbreekt"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalenders"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Annuleren"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas op GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Kies een cursus voor bericht"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Kies uit galerij"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Voltooid"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Contact opnemen met ondersteuning"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Cursusaankondiging"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Cursusaankondigingen"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Cursuscijfer hoger dan"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Cursuscijfer lager dan"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Donkere modus"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Verwijderen"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beschrijving"),
    "Device" : MessageLookupByLibrary.simpleMessage("Apparaat"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Apparaatmodel"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domein:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Gereed"),
    "Download" : MessageLookupByLibrary.simpleMessage("Downloaden"),
    "Due" : MessageLookupByLibrary.simpleMessage("Inleverdatum"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EXTREEM NOODGEVAL!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-mailadres"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-mail:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Voer de koppelingscode cursist in die je gekregen hebt. Als de koppelingscode niet werkt, is deze mogelijk verlopen"),
    "Event" : MessageLookupByLibrary.simpleMessage("Gebeurtenis"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Vrijgesteld"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Mislukt. Tik voor opties."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filteren op"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Voorpagina"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Volledig foutbericht"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Cijfer"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Cijferpercentage"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Beoordeeld"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Cijfers"),
    "Help" : MessageLookupByLibrary.simpleMessage("Help"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modus hoog contrast"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Wat voor gevolgen heeft dit voor jou?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Ik kan pas iets voor je doen als je me antwoordt."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Ik heb wat hulp nodig, maar het is niet dringend."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Ik ondervind problemen met inloggen"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idee voor Canvas Parent App [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Inbox"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Inbox Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incompleet"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Aankondiging organisatie"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Aankondigingen van instituut"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instructies"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Dat lijkt een prima dag om lekker uit te rusten, te relaxen en de batterij op te laden."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Het lijkt erop dat er in deze ruimte nog geen opdrachten zijn gemaakt."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Gewoon een informele vraag, opmerking, idee of suggestie..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Te laat"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Extern hulpmiddel lanceren"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Juridisch"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Lichte modus"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Fout met link"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Landinstellingen:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Locatie"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Vergrendeld"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Afmelden"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Cursisten beheren"),
    "Message" : MessageLookupByLibrary.simpleMessage("Bericht"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Onderwerp van bericht"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Ontbrekend"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Moet lager zijn dan 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Netwerkfout"),
    "Never" : MessageLookupByLibrary.simpleMessage("Nooit"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nieuw bericht"),
    "No" : MessageLookupByLibrary.simpleMessage("Nee"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Geen waarschuwingen"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Geen opdrachten"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Geen cursussen"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Geen inleverdatum"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Vandaag geen gebeurtenissen!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Geen cijfer"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Geen locatie opgegeven"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Geen cursisten"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Geen onderwerp"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Geen samenvatting"),
    "No description" : MessageLookupByLibrary.simpleMessage("Geen beschrijving"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Geen ontvangers geselecteerd"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Niet beoordeeld"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Niet ingediend"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Geen ouder?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Meldingen voor herinneringen over opdrachten en kalendergebeurtenissen"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS Versie"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Waarnemer"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Wellicht is een van onze andere apps hiervoor beter geschikt. Tik op een van de apps om de Play Store te openen."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Openen in browser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Openen met een andere app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Koppelingscode"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Wordt voorbereid"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Eerdere aanmeldingen"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Privacybeleid"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Privacybeleid, gebruiksvoor"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-code"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Geadresseerden"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Stuur me een herinnering"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Herinneringen"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Beantwoorden"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Allen beantwoorden"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Een probleem melden"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Help bij inloggen aanvragen"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Knop Help bij inloggen aanvragen"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("App opnieuw starten"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Opnieuw proberen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Terug naar Aanmelden"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("CURSIST"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Ontvangers selecteren"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Een bericht over deze opdracht verzenden"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Een bericht over deze cursus verzenden"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Bericht versturen"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Stel een datum en tijd in waarop je een melding wilt krijgen voor deze gebeurtenis."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Stel een datum en tijd in waarop je een melding wilt krijgen voor deze specifieke opdracht."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Herinnering aan- en uitzetten"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Instellingen"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Laat weten hoe goed je de App vindt"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Er is iets gebroken, maar daar kan ik wel omheen werken om gedaan te krijgen wat ik wil."),
    "Student" : MessageLookupByLibrary.simpleMessage("Cursist"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Vak"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Ingeleverd"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Inlevering is gelukt!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Samenvatting"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Wissel van gebruikers"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Syllabus"),
    "TA" : MessageLookupByLibrary.simpleMessage("Onderwijsassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("CURSUSLEIDER"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tik op de cursussen die je als favoriet in de kalender wilt zien."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tik om te koppelen met een nieuwe cursist"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tik om deze cursist te selecteren"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tik om cursistkiezer weer te geven"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Docent"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Laat ons weten wat jouw favoriete onderdelen van de app zijn"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Gebruiksvoorwaarden"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("De volgende informatie helpt ons een beter idee te krijgen van jouw idee:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("De server die je hebt ingevoerd, is niet gemachtigd voor deze app."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("De gebruikersagent voor deze app heeft geen toestemming."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Thema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Er zijn geen apps geïnstalleerd die dit bestand kunnen openen"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Er is geen pagina-informatie beschikbaar."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Er is een probleem met het laden van de gebruiksvoorwaarden"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van ontvangers voor deze cursus"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van de samenvatting van deze cursus."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van deze aankondiging"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van dit gesprek."),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van dit bestand"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van je inboxberichten."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van de waarschuwingen van je cursist."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van de kalender van je cursist"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Er is een probleem opgetreden bij het laden van je cursisten."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van de cursussen van je cursist."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Er is nog niets om te melden."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Deze app mag niet gebruikt worden."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Deze cursus heeft nog geen opdrachten of kalendergebeurtenissen."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Dit bestand wordt niet ondersteund en kan niet via de app worden bekeken"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Totaalcijfer"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Let op!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Cursussen kunnen niet worden opgehaald. Controleer je verbinding en probeer het opnieuw."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Kan deze afbeelding niet laden"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Kan dit mediabestand niet afspelen"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kan het bericht niet verzenden. Controleer je verbinding en probeer nogmaals."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("In aanbouw"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Onbekende gebruiker"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Niet-opgeslagen wijzigingen"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Niet-ondersteund bestand"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Bestand uploaden"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Camera gebruiken"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Gebruikers ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versienummer"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Foutgegevens weergeven"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("We werken momenteel aan deze functie om de weergave te verbeteren."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("We kunnen deze link niet weergeven omdat de link wellicht van een organisatie is waarbij je momenteel niet bent aangemeld."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("We kunnen geen cursist vinden die aan dit account gekoppeld is."),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("We konden niet verifiëren of de server geschikt is voor deze app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("We weten niet precies wat er is gebeurd, maar goed is het niet. Neem contact met ons op als dit blijft gebeuren."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Je observeert geen cursisten."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Je krijgt een melding over deze opdracht op…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Je krijgt een melding over deze gebeurtenis op…"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Je code is onjuist of verlopen."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("De cursussen van je cursist zijn mogelijk nog niet gepubliceerd."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Je bent helemaal bij!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Waarschuwingen"),
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
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-handleidingen"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas-ondersteuning"),
    "collapse" : MessageLookupByLibrary.simpleMessage("samenvouwen"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("samengevouwen"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cursussen"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("afwijzen"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hoe vind ik mijn school of district?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Voer de naam van de school of onderwijskoepel in…"),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("uitvouwen"),
    "expanded" : MessageLookupByLibrary.simpleMessage("uitgevouwen"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("School zoeken"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("mij"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Volgende"),
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
    "send" : MessageLookupByLibrary.simpleMessage("verzenden"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("ongelezen"),
    "unreadCount" : m39
  };
}
