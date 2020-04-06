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

  static m0(version) => "r. ${version}";

  static m1(threshold) => "Ocena pri nalogi višja od ${threshold}";

  static m2(threshold) => "Ocena pri nalogi nižja od ${threshold}";

  static m3(moduleName) => "To nalogo je zaklenil modul »${moduleName}«.";

  static m4(studentName, assignmentName) => "Zadeva: ${studentName}, Naloga – ${assignmentName}";

  static m5(points) => "${points} točk";

  static m6(points) => "${points} točk";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} do 1 drugi', other: '${authorName} do ${howMany} drugih')}";

  static m8(authorName, recipientName) => "${authorName} do ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} do ${recipientName} in 1 drugi', other: '${authorName} do ${recipientName} in ${howMany} drugih')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} od ${pointsPossible} točk";

  static m12(studentShortName) => "za ${studentShortName}";

  static m13(threshold) => "Ocena pri predmetu višja od ${threshold}";

  static m14(threshold) => "Ocena pri predmetu nižja od ${threshold}";

  static m15(date, time) => "${date} ob ${time}";

  static m16(canvasGuides, canvasSupport) => "Poskusite poiskati ime šole ali okrožja, do katerega poskušate dostopati, na primer »Smith Private School« ali »Smith County Schools«. V domeno sistema Canvas lahko vstopite neposredno, na primer »smith.instructure.com«.\n\nZa več informacij o iskanju računa Canvas vaše ustanove, obiščite spletno mesto ${canvasGuides} in se za pomoč obrnite na ${canvasSupport} ali na šolo.";

  static m17(date, time) => "Roki ${date} ob ${time}";

  static m18(studentName, eventTitle) => "Zadeva: ${studentName}, Dogodek – ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} – ${endAt}";

  static m20(grade) => "Končna ocena: ${grade}";

  static m21(studentName) => "Zadeva: ${studentName}, Prva stran";

  static m22(score, pointsPossible) => "${score}/${pointsPossible}";

  static m23(studentName) => "Zadeva: ${studentName}, Ocene";

  static m24(pointsLost) => "Kazen za zamudo (-${pointsLost})";

  static m25(studentName, linkUrl) => "Zadeva: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Mora biti višje od ${percentage}";

  static m27(percentage) => "Mora biti nižje od ${percentage}";

  static m28(month) => "Naslednji mesec: ${month}";

  static m29(date) => "Naslednji teden z začetkom ${date}";

  static m30(query) => "Ni mogoče najti šol, ki se ujemajo s/z »${query}«";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Od 1 točke', other: 'Od ${points} točk')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} možnih točk";

  static m34(month) => "Predhodni mesec: ${month}";

  static m35(date) => "Predhodni teden z začetkom ${date}";

  static m36(month) => "Mesec ${month}";

  static m37(date, time) => "Ta naloga je bila poslana dne ${date} ob ${time} in čaka na ocenjevanje";

  static m38(studentName) => "Zadeva: ${studentName}, Učni načrt predmeta";

  static m39(count) => "${count} neprebranih";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Opis je obvezen."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Polje za zadevo mora biti izpolnjeno."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Dodaj študenta"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Dodaj prilogo"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Dodaj novega študenta"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Dodaj študenta s/z ..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Nastavitve opozoril"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Opozori me, ko ..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Vsa ocenjevalna obdobja"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Potreben je e-poštni naslov."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Med prikazovanjem te povezave je prišlo do napake"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("Koledarji"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Temni način"),
    "Date" : MessageLookupByLibrary.simpleMessage("Datum"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Odstrani"),
    "Description" : MessageLookupByLibrary.simpleMessage("Opis"),
    "Device" : MessageLookupByLibrary.simpleMessage("Naprava"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Model naprave"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domena:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Dokončano"),
    "Download" : MessageLookupByLibrary.simpleMessage("Prenesi"),
    "Due" : MessageLookupByLibrary.simpleMessage("Roki"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("SKRAJNO KRITIČNI NUJNI PRIMER"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E-poštni naslov"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-pošta:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Vnesite kodo za povezovanje s študentom, ki ste jo prejeli. Če koda za povezovanje ne deluje, je morda potekla"),
    "Event" : MessageLookupByLibrary.simpleMessage("Dogodek"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Opravičeno"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Ni uspelo. Tapnite za možnosti."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtriraj glede na"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Prva stran"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Polni prikaz sporočila o napaki"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Ocena"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Odstotek ocene"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Ocenjeno"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Ocene"),
    "Help" : MessageLookupByLibrary.simpleMessage("Pomoč"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Visokokontrastni način"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Kako to vpliva na vas?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Ne morem dokončati zadev, dokler ne prejmem vašega odziva."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Potrebujem pomoč, ni pa nujno."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Imam težave pri prijavi"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Zamisel za aplikacijo Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Pošta"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Nabiralnik je prazen"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Nezaključeno"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Obvestilo ustanove"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Obvestila ustanove"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Navodila"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Videti je, da je to krasen dan za počitek, sprostitev in regeneracijo."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Videti je, da v tem prostoru naloge še niso bile ustvarjene."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Samo neobvezno vprašanje, komentar, zamisel, predlog ..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Zamuda"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Zaženi zunanje orodje"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Pravne informacije"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Svetli način"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Napaka povezave"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Območna nastavitev:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Mesto"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Zaklenjeno"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Odjava"),
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
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Niste starš?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Sporočila za opomnike o nalogah in koledarskih dogodkih"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Različica OS"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Skriti bralec"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Morda bi bolj ustrezala kakšna druga aplikacija. Tapnite eno od njih, da obiščete trgovino Play Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Odpri v brskalniku"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Odpri z drugo aplikacijo"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Koda za seznanjanje naprave"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Pripravljam ..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Predhodne prijave"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Pravilnik o zasebnosti"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Politika zasebnosti, pogoji uporabe, odprti vir"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Koda QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Prejemniki"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Opomni me"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Opomniki"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Odgovori"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Odgovori vsem"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Poročajte o težavi"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Zahtevaj pomoč pri prijavi"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Zahtevaj gumb za pomoč pri prijavi"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Ponovno zaženite aplikacijo"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Ponovno poskusi"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Nazaj na prijavo"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ŠTUDENT"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Izberi prejemnike"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Pošlji sporočilo o tej nalogi"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Pošlji sporočilo o tem predmetu"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Pošlji sporočilo"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Nastavite datum in čas obvestila o tem dogodku."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Nastavite datum in čas obvestila o tej določeni nalogi."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Nastavi preklop opomnika"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Nastavitve"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Delite svoje navdušenje nad aplikacijo"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Nekaj ne deluje, a znam to rešiti, da dokončam, kar je treba."),
    "Student" : MessageLookupByLibrary.simpleMessage("Študent"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Zadeva"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Poslano"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Uspešno poslano."),
    "Summary" : MessageLookupByLibrary.simpleMessage("Povzetek"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Preklopi uporabnika"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Učni načrt"),
    "TA" : MessageLookupByLibrary.simpleMessage("Demonstrator"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("IZVAJALEC"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tapnite za dodajanje predmetov, ki jih želite prikazane na koledarju, med priljubljene."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tapnite za povezavo z novim študentom"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tapnite za izbiro tega študenta"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tapnite za prikaz izbirnika študentov"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Izvajalec"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Opišite nam, kateri deli aplikacije so vam najljubši"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Pogoji uporabe"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Z naslednjimi informacijami bomo bolje razumeli vašo zamisel:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Strežnik, ki ste ga vnesli, nima pooblastila za to aplikacijo."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Zastopnik uporabnika za to aplikacijo ni pooblaščen."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Nameščene nimate nobene aplikacije, ki bi lahko odprla to datoteko"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Informacije o strani niso na voljo."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Prišlo je do težav pri nalaganju pogojev uporabe."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju prejemnikov za ta predmet"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju povzetka podrobnosti za ta predmet."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju tega obvestila"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju tega pogovora"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Prišlo je do napake pri nalaganju te datoteke"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Pri nalaganju sporočil vašega nabiralnika je prišlo do napake."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Pri nalaganju opozoril vašega študenta je prišlo do napake."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Pri nalaganju koledarja vašega študenta je prišlo do napake"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Pri nalaganju vaših študentov je prišlo do napake."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Pri nalaganju predmetov vašega študenta je prišlo do napake."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Ni ničesar, o čemer bi bilo treba obveščati."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Te aplikacije ni dovoljeno uporabljati."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Ta predmet še nima nalog ali koledarskih dogodkov."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Datoteka ni podprta in si je ni mogoče ogledati v aplikaciji"),
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
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID uporabnika:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Številka različice"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Prikaz podrobnosti o napaki"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Ta funkcija je v izdelavi za vaš užitek ob gledanju."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Te povezave ne moremo prikazati; morda pripada ustanovi, v katero trenutno niste prijavljeni."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nismo uspeli najti nobenega študenta, povezanega s tem računom"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Strežnika za uporabo pri tej aplikaciji nismo uspeli preveriti."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nismo prepričano, kaj se je zgodilo, ni pa bilo dobro. Če se napaka ponovi, nas kontaktirajte."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Da"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Ne opazujete nobenega študenta."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("O tej nalogi boste obveščeni dne ..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("O tem dogodku boste obveščeni dne ..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Vaša koda je nepravilna ali potekla."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Predmeti vašega študenta morda še niso objavljeni."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Seznanjeni ste z vsem!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Opozorila"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Koledar"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Vodniki po sistemu Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logotip sistema Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Podpora za sistem Canvas"),
    "collapse" : MessageLookupByLibrary.simpleMessage("strni"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("strnjeno"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Predmeti"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("opusti"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Kako najdem svojo šolo ali okrožje?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Vnos imena šole ali okrožja ..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("razširi"),
    "expanded" : MessageLookupByLibrary.simpleMessage("razširjeno"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Poišči šolo"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("jaz"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Naprej"),
    "nextMonth" : m28,
    "nextWeek" : m29,
    "noDomainResults" : m30,
    "ok" : MessageLookupByLibrary.simpleMessage("V redu"),
    "outOfPoints" : m31,
    "plusRecipientCount" : m32,
    "pointsPossible" : m33,
    "previousMonth" : m34,
    "previousWeek" : m35,
    "selectedMonthLabel" : m36,
    "send" : MessageLookupByLibrary.simpleMessage("pošlji"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("neprebrano"),
    "unreadCount" : m39
  };
}
