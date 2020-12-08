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

  static m0(userName) => "Je treedt op als ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Opdrachtcijfer hoger dan ${threshold}";

  static m3(threshold) => "Opdrachtcijfer lager dan ${threshold}";

  static m4(moduleName) => "Deze opdracht wordt vergrendeld door de module \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Betreffende: ${studentName}, opdracht - ${assignmentName}";

  static m6(points) => "${points} punten";

  static m7(points) => "${points} punten";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} naar 1 andere', other: '${authorName} naar ${howMany} andere')}";

  static m9(authorName, recipientName) => "${authorName} naar ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} naar ${recipientName} en 1 andere', other: '${authorName} naar ${recipientName} en ${howMany} andere')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Kleur wijzigen voor ${studentName}";

  static m13(score, pointsPossible) => "${score} van de ${pointsPossible} punten";

  static m14(studentShortName) => "voor ${studentShortName}";

  static m15(threshold) => "Cursuscijfer hoger dan ${threshold}";

  static m16(threshold) => "Cursuscijfer lager dan ${threshold}";

  static m17(date, time) => "${date} om ${time}";

  static m18(alertTitle) => "${alertTitle} afwijzen";

  static m19(canvasGuides, canvasSupport) => "Zoek naar de gewenste naam van de school of onderwijskoepel, zoals “Spinozacollege” of “INNOVO”. Je kunt ook rechtstreeks een Canvas-domein invoeren, zoals “smith.instructure.com.”\n\nVoor meer informatie over het vinden van het Canvas-account van jouw instelling kun je de ${canvasGuides} bezoeken, contact opnemen met ${canvasSupport} of met je school zelf.";

  static m20(date, time) => "In te leveren op ${date} om ${time}";

  static m21(userName) => "Je stopt met optreden als ${userName} en je wordt afgemeld.";

  static m22(userName) => "Je stopt met optreden als ${userName} en keert terug naar je oorspronkelijke account.";

  static m23(studentName, eventTitle) => "Betreffende: ${studentName}, gebeurtenis - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Eindcijfer: ${grade}";

  static m26(studentName) => "Betreffende: ${studentName}, voorpagina";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Betreffende: ${studentName}, cijfers";

  static m29(pointsLost) => "Sanctie indien te laat (-${pointsLost})";

  static m30(studentName, linkUrl) => "Betreffende: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Moet hoger zijn dan ${percentage}";

  static m32(percentage) => "Moet lager zijn dan ${percentage}";

  static m33(month) => "Volgende maand: ${month}";

  static m34(date) => "Volgende week vanaf ${date}";

  static m35(query) => "Kan geen scholen vinden met \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Van 1 punt', other: 'Van ${points} punten')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} punten mogelijk";

  static m39(month) => "Vorige maand: ${month}";

  static m40(date) => "Vorige week vanaf ${date}";

  static m41(termsOfService, privacyPolicy) => "Door op \'Account maken’ te tikken ga je akkoord met de ${termsOfService} en accepteer je het ${privacyPolicy}";

  static m42(version) => "Suggesties voor Android - Canvas Parent ${version}";

  static m43(month) => "De maand ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} ster', other: '${position} sterren')}";

  static m45(date, time) => "Deze opdracht werd ingeleverd op ${date} om ${time} en wacht op beoordeling";

  static m46(studentName) => "Betreffende: ${studentName}, syllabus";

  static m47(count) => "${count} ongelezen";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Optreden als\" is in essentie hetzelfde als aanmelden als deze gebruiker zonder wachtwoord. Je kunt alle acties uitvoeren alsof je die gebruiker bent. Gezien vanuit andere gebruikers lijkt het dan ook alsof die acties door die gebruiker zijn uitgevoerd. De controleogboeken laten echter zien dat jij degene was die namens deze gebruiker die acties verricht hebt."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Er is een beschrijving vereist."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Er is een netwerkfout opgetreden bij het toevoegen van deze cursist. Controleer je verbinding en probeer het opnieuw."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Er is een onderwerp vereist"),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Optreden als gebruiker"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Cursist toevoegen"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Bijlage toevoegen"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Nieuwe cursist toevoegen"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Cursist toevoegen met…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Waarschuwingsinstellingen"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Waarschuw me als..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Alle beoordelingsperioden"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Heb je al een account? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Er is een e-mailadres vereist."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het weergeven van deze van link."),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden tijdens het opslaan van je selectie. Probeer het opnieuw."),
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
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, fuchsia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalenders"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Cameramachtiging"),
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
    "Create Account" : MessageLookupByLibrary.simpleMessage("Account maken"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Donkere modus"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Verwijderen"),
    "Description" : MessageLookupByLibrary.simpleMessage("Beschrijving"),
    "Device" : MessageLookupByLibrary.simpleMessage("Apparaat"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Apparaatmodel"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domein"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domein:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Niet nogmaals tonen"),
    "Done" : MessageLookupByLibrary.simpleMessage("Gereed"),
    "Download" : MessageLookupByLibrary.simpleMessage("Downloaden"),
    "Due" : MessageLookupByLibrary.simpleMessage("Inleverdatum"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EXTREEM NOODGEVAL!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elektrisch, blauw"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-mailadres"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-mail:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-mail..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Voer de koppelingscode cursist in die je gekregen hebt. Als de koppelingscode niet werkt, is deze mogelijk verlopen"),
    "Event" : MessageLookupByLibrary.simpleMessage("Gebeurtenis"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Vrijgesteld"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Verlopen QR-code"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Mislukt. Tik voor opties."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filteren op"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Vuur, oranje"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Voorpagina"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Volledige naam"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Volledige naam..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Volledig foutbericht"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Ga naar vandaag"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Cijfer"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Cijferpercentage"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Beoordeeld"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Cijfers"),
    "Help" : MessageLookupByLibrary.simpleMessage("Help"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Wachtwoord verbergen"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modus hoog contrast"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Hoe gaat het?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Wat voor gevolgen heeft dit voor jou?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Ik kan pas iets voor je doen als je me antwoordt."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Ik heb geen Canvas-account"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Ik heb een Canvas-account"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Ik heb wat hulp nodig, maar het is niet dringend."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Ik ondervind problemen met inloggen"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idee voor Canvas Parent App [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Om je een betere gebruikerservaring te geven, hebben we de wijze waarop herinneringen functioneren bijgewerkt. Je kunt nieuwe herinneringen toevoegen door een opdracht of kalendergebeurtenis te bekijken en op de schakelaar te tikken onder de sectie \"Stuur me een herinnering\".\n\nHoud er rekening mee dat herinneringen die zijn gemaakt met oudere versies van deze app niet compatibel zijn met de nieuwe wijzigingen en je deze herinneringen opnieuw moet maken."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Inbox"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Inbox leeg"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incompleet"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Verkeerd domein"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Aankondiging organisatie"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Aankondigingen van instituut"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instructies"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interacties op deze pagina zijn beperkt door je organisatie."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Ongeldige QR-code"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Dat lijkt een prima dag om lekker uit te rusten, te relaxen en de batterij op te laden."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Het lijkt erop dat er in deze ruimte nog geen opdrachten zijn gemaakt."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Gewoon een informele vraag, opmerking, idee of suggestie..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Te laat"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Extern hulpmiddel lanceren"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Juridisch"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Lichte modus"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Fout met link"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Landinstellingen:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("QR-code zoeken"),
    "Location" : MessageLookupByLibrary.simpleMessage("Locatie"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Vergrendeld"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Afmelden"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Aanmeldingsstroom: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Aanmeldingsstroom: Normaal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Aanmeldingsstroom: Sitebeheerder"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Aanmeldingsstroom: Mobiel verifiëren overslaan"),
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
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student openen"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Openen in browser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Openen met een andere app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Koppelingscode"),
    "Password" : MessageLookupByLibrary.simpleMessage("Wachtwoord"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Wachtwoord is vereist"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Wachtwoord moet minstens 8 tekens bevatten."),
    "Password…" : MessageLookupByLibrary.simpleMessage("Wachtwoord..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Planneraantekening"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Voer een geldig e-mailadres in"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Voer een e-mailadres in"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Voer volledige naam in"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Scan een door Canvas gegenereerde QR-code"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Pruim, paars"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Wordt voorbereid"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Eerdere aanmeldingen"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Privacybeleid"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Link naar Privacybeleid"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Privacybeleid, gebruiksvoor"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-code"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Scannen van QR-code vereist toegang tot camera"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Framboos, rood"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Geadresseerden"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Vernieuwen"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Stuur me een herinnering"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Herinneringen"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Herinneringen zijn veranderd!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Beantwoorden"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Allen beantwoorden"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Een probleem melden"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Help bij inloggen aanvragen"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Knop Help bij inloggen aanvragen"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("App opnieuw starten"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Opnieuw proberen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Terug naar Aanmelden"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("CURSIST"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Screenshot met locatie van het genereren van een QR-code in een browser"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Schermafbeelding die de locatie toont waar de QR-code voor de koppeling in de Canvas Student-app wordt gegenereerd"),
    "Select" : MessageLookupByLibrary.simpleMessage("Selecteren"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Cursistkleur selecteren"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Ontvangers selecteren"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Feedback versturen"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Een bericht over deze opdracht verzenden"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Een bericht over deze cursus verzenden"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Bericht versturen"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Stel een datum en tijd in waarop je een melding wilt krijgen voor deze gebeurtenis."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Stel een datum en tijd in waarop je een melding wilt krijgen voor deze specifieke opdracht."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Instellingen"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Klaver, groen"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Laat weten hoe goed je de App vindt"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Wachtwoord weergeven"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Aanmelden"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Er is iets mis gegaan bij het aanmaken van je account. Neem voor hulp contact op met je school."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Er is iets gebroken, maar daar kan ik wel omheen werken om gedaan te krijgen wat ik wil."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Stop met optreden als gebruiker"),
    "Student" : MessageLookupByLibrary.simpleMessage("Cursist"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Cursistkoppeling"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Cursisten kunnen een QR-code maken met behulp van de Canvas Student-app op hun mobiele apparaat"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Cursisten kunnen een koppelingscode ophalen via de Canvas-website."),
    "Subject" : MessageLookupByLibrary.simpleMessage("Vak"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Ingeleverd"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Inlevering is gelukt!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Samenvatting"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Wissel van gebruikers"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Syllabus"),
    "TA" : MessageLookupByLibrary.simpleMessage("Onderwijsassistent"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("CURSUSLEIDER"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tik op de cursussen die je als favoriet in de kalender wilt zien. Tot maximaal 10 selecteren."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tik om te koppelen met een nieuwe cursist"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tik om deze cursist te selecteren"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tik om cursistkiezer weer te geven"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Docent"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Laat ons weten wat jouw favoriete onderdelen van de app zijn"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Servicevoorwaarden"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Link naar Servicevoorwaarden"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Gebruiksvoorwaarden"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("De QR-code die je hebt gescand is wellicht verlopen. Ververs de code op het apparaat van de cursist en probeer het opnieuw."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("De volgende informatie helpt ons een beter idee te krijgen van jouw idee:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("De server die je hebt ingevoerd, is niet gemachtigd voor deze app."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("De cursist die je probeert toe te voegen behoort tot een andere school. Log in of maak een account aan bij die school om deze code te scannen."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("De gebruikersagent voor deze app heeft geen toestemming."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Thema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Er zijn geen apps geïnstalleerd die dit bestand kunnen openen"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Er is geen pagina-informatie beschikbaar."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Er is een probleem met het laden van de gebruiksvoorwaarden"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Er is een probleem met het verwijderen van deze cursist uit je account. Controleer je verbinding en probeer het opnieuw."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van ontvangers voor deze cursus"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van de samenvatting van deze cursus."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van deze aankondiging"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van dit gesprek."),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van dit bestand"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van je inboxberichten."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van de waarschuwingen van je cursist."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van de kalender van je cursist"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Er is een probleem opgetreden bij het laden van je cursisten."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het laden van de cursussen van je cursist."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het aanmelden. Genereer een andere QR-code en probeer het opnieuw."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Er is een fout opgetreden bij het optreden als deze gebruiker. Controleer het domein en de gebruikers-ID en probeer het opnieuw."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Er is nog niets om te melden."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Deze app mag niet gebruikt worden."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Deze cursus heeft nog geen opdrachten of kalendergebeurtenissen."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Dit bestand wordt niet ondersteund en kan niet via de app worden bekeken"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Dit zal alle inschrijvingen voor deze cursist van jouw account ontkoppelen en verwijderen."),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Donker thema gebruiken voor webcontent"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Gebruikers-ID:"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Gebruikers ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Versienummer"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Beschrijving bekijken"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Foutgegevens weergeven"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Privacybeleid weergeven"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("We werken momenteel aan deze functie om de weergave te verbeteren."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("We kunnen deze link niet weergeven omdat de link wellicht van een organisatie is waarbij je momenteel niet bent aangemeld."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("We kunnen geen cursist vinden die aan dit account gekoppeld is."),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("We konden niet verifiëren of de server geschikt is voor deze app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("We weten niet precies wat er is gebeurd, maar goed is het niet. Neem contact met ons op als dit blijft gebeuren."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Wat kunnen we beter doen?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ja"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Je observeert geen cursisten."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Je kan slecht 10 kalenders kiezen om te tonen"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Je moet een gebruikers-ID invoeren"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Je moet een geldig domein invoeren"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Je moet minimaal één kalender selecteren om te tonen"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Je krijgt een melding over deze opdracht op…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Je krijgt een melding over deze gebeurtenis op…"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Je vindt de QR-code op het web in je accountprofiel. Klik op \'QR voor mobiel inloggen\' in de lijst."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Je moet de Canvas Student-app van je cursist openen om door te kunnen gaan. Ga naar Hoofdmenu > Instellingen > Koppelen met waarnemer en scan de QR-code die je daar ziet."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Je code is onjuist of verlopen."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("De cursussen van je cursist zijn mogelijk nog niet gepubliceerd."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Je bent helemaal bij!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Waarschuwingen"),
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
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-handleidingen"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas-ondersteuning"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("samenvouwen"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("samengevouwen"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cursussen"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hoe vind ik mijn school of district?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Voer de naam van de school of onderwijskoepel in…"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("uitvouwen"),
    "expanded" : MessageLookupByLibrary.simpleMessage("uitgevouwen"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("School zoeken"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("mij"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Volgende"),
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
    "send" : MessageLookupByLibrary.simpleMessage("verzenden"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("ongelezen"),
    "unreadCount" : m47
  };
}
