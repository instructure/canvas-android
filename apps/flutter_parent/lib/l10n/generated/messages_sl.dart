// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a sl locale. All the
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
  String get localeName => 'sl';

  static m0(userName) => "Nastopate v vlogi ${userName}";

  static m1(version) => "r. ${version}";

  static m2(threshold) => "Ocena pri nalogi višja od ${threshold}";

  static m3(threshold) => "Ocena pri nalogi nižja od ${threshold}";

  static m4(moduleName) => "To nalogo je zaklenil modul »${moduleName}«.";

  static m5(studentName, assignmentName) => "Zadeva: ${studentName}, Naloga – ${assignmentName}";

  static m6(points) => "${points} točk";

  static m7(points) => "${points} točk";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} do 1 drugi', other: '${authorName} do ${howMany} drugih')}";

  static m9(authorName, recipientName) => "${authorName} do ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} do ${recipientName} in 1 drugi', other: '${authorName} do ${recipientName} in ${howMany} drugih')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Spremeni barvo za ${studentName}";

  static m13(score, pointsPossible) => "${score} od ${pointsPossible} točk";

  static m14(studentShortName) => "za ${studentShortName}";

  static m15(threshold) => "Ocena pri predmetu višja od ${threshold}";

  static m16(threshold) => "Ocena pri predmetu nižja od ${threshold}";

  static m17(date, time) => "${date} ob ${time}";

  static m18(alertTitle) => "Opusti ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Poskusite poiskati ime šole ali okrožja, do katerega poskušate dostopati, na primer »Smith Private School« ali »Smith County Schools«. V domeno sistema Canvas lahko vstopite neposredno, na primer »smith.instructure.com«.\n\nZa več informacij o iskanju računa Canvas vaše ustanove, obiščite spletno mesto ${canvasGuides} in se za pomoč obrnite na ${canvasSupport} ali na šolo.";

  static m20(date, time) => "Roki ${date} ob ${time}";

  static m21(userName) => "Prenehali boste nastopati v vlogi ${userName} in boste odjavljeni.";

  static m22(userName) => "Prenehali boste nastopati v vlogi ${userName} in se vrnili v svoj prvotni račun.";

  static m23(studentName, eventTitle) => "Zadeva: ${studentName}, Dogodek – ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} – ${endAt}";

  static m25(grade) => "Končna ocena: ${grade}";

  static m26(studentName) => "Zadeva: ${studentName}, Prva stran";

  static m27(score, pointsPossible) => "${score}/${pointsPossible}";

  static m28(studentName) => "Zadeva: ${studentName}, Ocene";

  static m29(pointsLost) => "Kazen za zamudo (-${pointsLost})";

  static m30(studentName, linkUrl) => "Zadeva: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Mora biti višje od ${percentage}";

  static m32(percentage) => "Mora biti nižje od ${percentage}";

  static m33(month) => "Naslednji mesec: ${month}";

  static m34(date) => "Naslednji teden z začetkom ${date}";

  static m35(query) => "Ni mogoče najti šol, ki se ujemajo s/z »${query}«";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Od 1 točke', other: 'Od ${points} točk')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} možnih točk";

  static m39(month) => "Predhodni mesec: ${month}";

  static m40(date) => "Predhodni teden z začetkom ${date}";

  static m41(termsOfService, privacyPolicy) => "Če tapnete »Ustvari račun«, pristajate na ${termsOfService} in potrdite ${privacyPolicy}.";

  static m42(version) => "Predlogi za sistem Android – Canvas Parent ${version}";

  static m43(month) => "Mesec ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} zvezda', other: '${position} zvezde')}";

  static m45(date, time) => "Ta naloga je bila poslana dne ${date} ob ${time} in čaka na ocenjevanje";

  static m46(studentName) => "Zadeva: ${studentName}, Učni načrt predmeta";

  static m47(count) => "${count} neprebranih";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("»Nastopajoči v vlogi« se v osnovi prijavlja kot ta uporabnik brez gesla. Lahko boste izvedli katero koli dejanje, kot bi bili ta uporabnik, z vidika drugih uporabnikov pa bo videti, kot da je dejanja izvedel ta uporabnik. Vendar dnevniki dogodkov beležijo, da ste v imenu tega uporabnika dejanja izvedli vi."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Opis je obvezen."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Pri dodajanju tega študenta je prišlo do napake v omrežju. Preverite svojo povezavo in poskusite znova."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Polje za zadevo mora biti izpolnjeno."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Prevzemi vlogo uporabnika"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Dodaj študenta"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Dodaj prilogo"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Dodaj novega študenta"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Dodaj študenta s/z ..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Nastavitve opozoril"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Opozori me, ko ..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Vsa ocenjevalna obdobja"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Ali že imate račun? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Potreben je e-poštni naslov."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Med prikazovanjem te povezave je prišlo do napake"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Med shranjevanjem vaše izbire je prišlo do napake. Poskusite znova."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Prišlo je do nepričakovane napake"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Različica sistema Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Videz"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Različica aplikacije"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Ste študent ali izvajalec?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Ali ste prepričani, da se želite odjaviti?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Ali ste prepričani, da želite zapreti to stran? Neposlana sporočila bodo izgubljena."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Podrobnosti o nalogi"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Ocena pri nalogi višja od"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Ocena pri nalogi nižja od"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Manjkajoča naloga"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, rožnato-vijolična"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Koledarji"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Dovoljenje za uporabo kamere"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Prekliči"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Študent v sistemu Canvas"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Izvajalec v sistemu Canvas"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas na GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Izberite predmet za pošiljanje sporočila"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Izberi iz galerije"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Zaključeno"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Stik s podporo"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Obvestilo o predmetu"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Obvestila o predmetu"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Ocena pri predmetu višja od"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Ocena pri predmetu nižja od"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Ustvari račun"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Temni način"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Odstrani"),
    "Description" : MessageLookupByLibrary.simpleMessage("Opis"),
    "Device" : MessageLookupByLibrary.simpleMessage("Naprava"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Model naprave"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domena"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domena:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Ne prikaži znova"),
    "Done" : MessageLookupByLibrary.simpleMessage("Dokončano"),
    "Download" : MessageLookupByLibrary.simpleMessage("Prenesi"),
    "Due" : MessageLookupByLibrary.simpleMessage("Roki"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("SKRAJNO KRITIČNI NUJNI PRIMER"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elektrika, modra"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-poštni naslov"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-pošta:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-pošta ..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Vnesite kodo za povezovanje s študentom, ki ste jo prejeli. Če koda za povezovanje ne deluje, je morda potekla"),
    "Event" : MessageLookupByLibrary.simpleMessage("Dogodek"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Opravičeno"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Koda QR je potekla"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Ni uspelo. Tapnite za možnosti."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtriraj glede na"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Ogenj, oranžna"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Prva stran"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Polno ime"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Polno ime ..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Polni prikaz sporočila o napaki"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Pojdi na danes"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Ocena"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Odstotek ocene"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Ocenjeno"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Ocene"),
    "Help" : MessageLookupByLibrary.simpleMessage("Pomoč"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Skrij geslo"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Visokokontrastni način"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Kako nam gre?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Kako to vpliva na vas?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Ne morem dokončati zadev, dokler ne prejmem vašega odziva."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Nimam računa Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Imam račun Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Potrebujem pomoč, ni pa nujno."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Imam težave pri prijavi"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Zamisel za aplikacijo Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Da vam zagotovimo boljšo izkušnjo, smo posodobili delovanje opomnikov. Nove opomnike lahko dodate, tako da si ogledate nalogo ali dogodek v koledarju in tapnete stikalo v razdelku »Opomni me«.\n\nUpoštevajte, da noben opomnik, ustvarjen v starejši različici te aplikacije, ne bo združljiv z novimi spremembami in boste te morali ustvariti znova."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Pošta"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Nabiralnik je prazen"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Nezaključeno"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Nepravilna domena"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Obvestilo ustanove"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Obvestila ustanove"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Navodila"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interakcije na tej strani omejuje vaša ustanova."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Neveljavna koda QR"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Videti je, da je to krasen dan za počitek, sprostitev in regeneracijo."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Videti je, da v tem prostoru naloge še niso bile ustvarjene."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Samo neobvezno vprašanje, komentar, zamisel, predlog ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Zamuda"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Zaženi zunanje orodje"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Pravne informacije"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Svetli način"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Napaka povezave"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Območna nastavitev:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Poiščite kodo QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Mesto"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Zaklenjeno"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Odjava"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Potek prijave: Sistem Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Potek prijave: Aktivno"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Potek prijave: Skrbnik mesta"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Potek prijave: Preskoči mobilno potrditev"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Uredi študente"),
    "Message" : MessageLookupByLibrary.simpleMessage("Sporočilo"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Zadeva sporočila"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Manjkajoče"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Mora biti nižje od 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Napaka v omrežju"),
    "Never" : MessageLookupByLibrary.simpleMessage("Nikoli"),
    "New message" : MessageLookupByLibrary.simpleMessage("Novo sporočilo"),
    "No" : MessageLookupByLibrary.simpleMessage("Ne"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Ni opozoril"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Ni nalog"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Ni predmetov"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Ni roka."),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Danes ni dogodkov."),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Ni ocen"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Mesto ni določeno"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Ni študentov"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Ni zadeve"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Ni povzetka"),
    "No description" : MessageLookupByLibrary.simpleMessage("Brez opisa"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Izbran ni noben prejemnik"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Ni ocenjeno"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Ni oddano."),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Niste eden od staršev?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Sporočila za opomnike o nalogah in koledarskih dogodkih"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Različica OS"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Skriti bralec"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Morda bi bolj ustrezala kakšna druga aplikacija. Tapnite eno od njih, da obiščete trgovino Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Odpiranje Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Odpri v brskalniku"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Odpri z drugo aplikacijo"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Koda za seznanjanje naprave"),
    "Password" : MessageLookupByLibrary.simpleMessage("Geslo"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Geslo je obvezno"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Geslo mora vsebovati vsaj 8 znakov."),
    "Password…" : MessageLookupByLibrary.simpleMessage("Geslo ..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Opomba orodja Planner"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Vnesite veljavni e-poštni naslov."),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Vnesite e-poštni naslov"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Vnesite polno ime"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Preberite kodo QR, ustvarjeno v sistemu Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Sliva, vijolična"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Pripravljam ..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Predhodne prijave"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Pravilnik o zasebnosti"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Povezava na Pravilnik o zasebnosti"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Politika zasebnosti, pogoji uporabe, odprti vir"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Koda QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Za branje kode QR je potreben dostop do kamere"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Malina, rdeča"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Prejemniki"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Osveži"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Opomni me"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Opomniki"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Opomniki so se spremenili!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Odgovori"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Odgovori vsem"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Poročajte o težavi"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Zahtevaj pomoč pri prijavi"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Zahtevaj gumb za pomoč pri prijavi"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Ponovno zaženite aplikacijo"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Ponovno poskusi"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Nazaj na prijavo"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ŠTUDENT"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Posnetek zaslona, ki prikazuje, kje v brskalniku ustvarite kodo QR"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Posnetek zaslona, ki prikazuje lokacijo za ustvarjanje kode QR za seznanjanje v aplikaciji Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Izberi"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Izberite barvo študenta"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Izberi prejemnike"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Pošlji povratne informacije"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Pošlji sporočilo o tej nalogi"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Pošlji sporočilo o tem predmetu"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Pošlji sporočilo"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Nastavite datum in čas obvestila o tem dogodku."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Nastavite datum in čas obvestila o tej določeni nalogi."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Nastavitve"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Detelja, zelena"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Delite svoje navdušenje nad aplikacijo"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Pokaži geslo"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Prijava"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Pri poskusu ustvarjanja računa je prišlo do napake. Za pomoč se obrnite na šolo."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Nekaj ne deluje, a znam to rešiti, da dokončam, kar je treba."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Opustite vlogo uporabnika"),
    "Student" : MessageLookupByLibrary.simpleMessage("Študent"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Seznanjanje študenta"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Študenti lahko ustvarijo kodo QR z aplikacijo Canvas Student na svoji mobilni napravi"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Študenti lahko pridobijo kodo za seznanitev prek spletnega mesta Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Zadeva"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Poslano"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Uspešno poslano."),
    "Summary" : MessageLookupByLibrary.simpleMessage("Povzetek"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Preklopi uporabnika"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Učni načrt"),
    "TA" : MessageLookupByLibrary.simpleMessage("Demonstrator"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("IZVAJALEC"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tapnite za dodajanje predmetov, ki jih želite prikazane na koledarju, med priljubljene. Izberite do 10 elementov."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tapnite za povezavo z novim študentom"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tapnite za izbiro tega študenta"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tapnite za prikaz izbirnika študentov"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Izvajalec"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Opišite nam, kateri deli aplikacije so vam najljubši"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Pogoji storitve"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Povezava na Pogoji za uporabo storitve"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Pogoji uporabe"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Veljavnost kode QR, ki ste jo odčitali, je potekla. Osvežite kodo na študentovi napravi in poskusite znova."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Z naslednjimi informacijami bomo bolje razumeli vašo zamisel:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Strežnik, ki ste ga vnesli, nima pooblastila za to aplikacijo."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Študent, ki ga poskušate dodati, spada v drugo šolo. Prijavite se ali ustvarite račun pri tej šoli, če želite odčitati to kodo."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Zastopnik uporabnika za to aplikacijo ni pooblaščen."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Nameščene nimate nobene aplikacije, ki bi lahko odprla to datoteko"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Informacije o strani niso na voljo."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Prišlo je do težav pri nalaganju pogojev uporabe."),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Prišlo je do težave pri odstranjevanju tega študenta iz računa. Preverite svojo povezavo in poskusite znova."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju prejemnikov za ta predmet"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju povzetka podrobnosti za ta predmet."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju tega obvestila"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju tega pogovora"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju te datoteke"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Pri nalaganju sporočil vašega nabiralnika je prišlo do napake."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Pri nalaganju opozoril vašega študenta je prišlo do napake."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Pri nalaganju koledarja vašega študenta je prišlo do napake"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Pri nalaganju vaših študentov je prišlo do napake."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Pri nalaganju predmetov vašega študenta je prišlo do napake."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Pri prijavi je prišlo do napake. Ustvarite novo kodo QR in poskusite znova."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri prevzemanju vloge tega uporabnika. Preverite domeno in ID uporabnika ter poskusite znova."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Ni ničesar, o čemer bi bilo treba obveščati."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Te aplikacije ni dovoljeno uporabljati."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Ta predmet še nima nalog ali koledarskih dogodkov."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Datoteka ni podprta in si je ni mogoče ogledati v aplikaciji"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Ta možnost bo odstranila seznanitev in vse vpise za študenta v vašem računu."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Skupna ocena"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Ojoj."),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Predmetov ni bilo mogoče pridobiti. Preverite svojo povezavo in poskusite znova."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Te slike ni mogoče naložiti"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Te predstavnostne datoteke ni mogoče predvajati"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Sporočila ni bilo mogoče poslati. Preverite svojo povezavo in poskusite znova."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("V delu"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Neznani uporabnik"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Neshranjene spremembe"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Nepodprta datoteka"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Naloži datoteko"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Uporabi fotoaparat"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Uporabi temno temo za spletno vsebino"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID uporabnika"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID uporabnika:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Številka različice"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Prikaz opisa"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Prikaz podrobnosti o napaki"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Ogled Pravilnika o zasebnosti"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Ta funkcija je v izdelavi za vaš užitek ob gledanju."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Te povezave ne moremo prikazati; morda pripada ustanovi, v katero trenutno niste prijavljeni."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nismo uspeli najti nobenega študenta, povezanega s tem računom"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Strežnika za uporabo pri tej aplikaciji nismo uspeli preveriti."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nismo prepričano, kaj se je zgodilo, ni pa bilo dobro. Če se napaka ponovi, nas kontaktirajte."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Kaj lahko izboljšamo?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Da"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Ne opazujete nobenega študenta."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Za prikaz lahko izberete le 10 koledarjev."),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Vnesti morate ID uporabnika"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Vnesti morate veljavno domeno"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Izbrati morate vsaj en koledar za prikaz."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("O tej nalogi boste obveščeni dne ..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("O tem dogodku boste obveščeni dne ..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Kodo QR boste našli v svojem profilu računa na spletu. Na seznamu kliknite »QR za mobilno prijavo«."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Če želite nadaljevati, odprite aplikacijo Canvas Student za študente. Pojdite v glavni meni > Nastavitve > Seznani s skritim bralcem in odčitajte prikazano kodo QR"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Vaša koda je nepravilna ali potekla."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Predmeti vašega študenta morda še niso objavljeni."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Seznanjeni ste z vsem!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Opozorila"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Koledar"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Vodniki po sistemu Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logotip sistema Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Podpora za sistem Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("strni"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("strnjeno"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Predmeti"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Kako najdem svojo šolo ali okrožje?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Vnos imena šole ali okrožja ..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("razširi"),
    "expanded" : MessageLookupByLibrary.simpleMessage("razširjeno"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Poišči šolo"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("jaz"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Naprej"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("V redu"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("pošlji"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("neprebrano"),
    "unreadCount" : m47
  };
}
