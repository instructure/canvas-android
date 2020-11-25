// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a ht locale. All the
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
  String get localeName => 'ht';

  static m0(userName) => "W ap aji tankou ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Nòt Devwa Plis Pase ${threshold}";

  static m3(threshold) => "Nòt Devwa Mwens Pase ${threshold}";

  static m4(moduleName) => "Modil \"${moduleName}\" bloke devwa sa a.";

  static m5(studentName, assignmentName) => "Konsènan: ${studentName}, Devwa - ${assignmentName}";

  static m6(points) => "${points} pwen";

  static m7(points) => "${points} pwen";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} a 1 lòt', other: '${authorName} a ${howMany} lòt')}";

  static m9(authorName, recipientName) => "${authorName} a ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} a ${recipientName} & 1 lòt', other: '${authorName} a ${recipientName} & ${howMany} lòt')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Chanje koulè pou ${studentName}";

  static m13(score, pointsPossible) => "${score} sou ${pointsPossible} pwen";

  static m14(studentShortName) => "pou ${studentShortName}";

  static m15(threshold) => "Nòt Kou Plis Pase ${threshold}";

  static m16(threshold) => "Nòt Kou Mwens Pase ${threshold}";

  static m17(date, time) => "${date} a ${time}";

  static m18(alertTitle) => "Rejte ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Eseye chèche non lekòl oswa distri ou vle ale sou li a, pa egzanp “Smith Private School” or “Smith County Schools.” Ou kapab antre yon domèn Canvas dirèkteman, tankou pa egzanp “smith.instructure.com.”\n\nPou plis enfòmasyon pou jwenn kont Canvas enstitisyon ou an, ou ka vizite ${canvasGuides}, ale sou ${canvasSupport}, oswa pran kontak ak lekòl ou a pou plis asistans.";

  static m20(date, time) => "Delè ${date} a ${time}";

  static m21(userName) => "W ap sispann pase pou ${userName} epi w ap dekonekte.";

  static m22(userName) => "W ap sispann pase pou ${userName} epi retounen sou kont orijinal ou.";

  static m23(studentName, eventTitle) => "Konsènan: ${studentName}, Aktivite - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Nòt Final: ${grade}";

  static m26(studentName) => "Konsènan: ${studentName}, Premye Paj";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Konsènan: ${studentName}, Nòt";

  static m29(pointsLost) => "Penalite pou reta (-${pointsLost})";

  static m30(studentName, linkUrl) => "Konsènan: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Dwe siperyè a ${percentage}";

  static m32(percentage) => "Dwe enferyè a ${percentage}";

  static m33(month) => "Pwochen mwa: ${month}";

  static m34(date) => "Semenn pwochen kòmanse ${date}";

  static m35(query) => "Nou paka jwenn lekòl ki koresponn a \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Sou 1 pwen', other: 'Sou ${points} pwen')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} pwen posib";

  static m39(month) => "Mwa pase: ${month}";

  static m40(date) => "Semenn pase kòmanse ${date}";

  static m41(termsOfService, privacyPolicy) => "Lè w tape \'Kreye Kont\', ou dakò Tèm ak Kondisyon yo ${termsOfService} epi ou rekonèt ${privacyPolicy}";

  static m42(version) => "Sijesyon pou Android - Canvas Parent ${version}";

  static m43(month) => "Mwa ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} etwal', other: '${position} etwal')}";

  static m45(date, time) => "Devwa sa a te soumèt nan dat ${date} a ${time} li an atant pou yo evalye li";

  static m46(studentName) => "Konsènan: ${studentName}, Pwogram";

  static m47(count) => "${count} poko li";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Aji tankou\" se koneksyon an tan ke moun sa a san modpas. W ap kapab fè nenpòt bagay kòmsi ou te itilizatè sa a, e pou lòt moun yo, se kòmsi se itilizatè sa a ki t ap fè yo. Men, jounal odit yo endike ke se te ou menm ki te fè aksyon yo nan non itilizatè sa a."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Yon deskripsyon obligatwa."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Gen yon erè rezo ki fèt pandan w ap ajoute elèv la. Verifye koneksyon ou a epi eseye ankò."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Yon sijè obligatwa."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Pase pou Itilizatè"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Ajoute Elèv"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Ajoute atachman"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Ajoute nouvo elèv"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Ajoute elèv ak..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Paramèt Alèt"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Alète m lè..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Tout Peryòd Klasman"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Ou gen yon kont deja? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Yon adrès imèl obligatwa."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Gen yon erè ki fèt pandan w ap eseye afiche lyen sa a"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Te gen yon erè ki fpet pandan anrejistreman seleksyone ou an, Tanpri eseye ankò."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Yon erè fèt sanzatann"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Vèsyon OS Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Aparans"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Vèsyon aplikasyon"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Ou se pwofesè oswa elèv?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Ou kwè vrèman ou vle dekonekte?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Ou kwè vrèman ou vle fèmen paj sa a? Mesaj ou genyen ki pa delivre yo ap elimine."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Detay Sesyon"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Npot sesyon pi wo a"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Npot sesyon pi ba a"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Sesyon ki manke"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, Fuschia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalandriye"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Pèmisyon Kamera"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Anile"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Pwofesè Canvas"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas sou GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Chwazi yon kou pou voye mesaj"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Chwazi nan Galri"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Fini"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Kontakte Sipò"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Anons Kou"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Anons Kou"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Nòt kou pi wo a"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Nòt kou pi ba a"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Kreye Kont"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mòd Fonse"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dat"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Efase"),
    "Description" : MessageLookupByLibrary.simpleMessage("Deskripsyon"),
    "Device" : MessageLookupByLibrary.simpleMessage("Aparèy"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modèl aparèy"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domèn"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domèn:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Pa afiche ankò"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fini"),
    "Download" : MessageLookupByLibrary.simpleMessage("Telechaje"),
    "Due" : MessageLookupByLibrary.simpleMessage("Delè"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("IJANS KRITIK EKSTRÈM!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elektrik, ble"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adrès Imèl"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Imèl:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Imèl..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Antre kòd kouplaj yo ba ou pou elèv la. Si kòd kouplaj la pa fonksyone, li kapab ekspire"),
    "Event" : MessageLookupByLibrary.simpleMessage("Aktivite"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Egzante"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Kòd QR ekspire"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Echwe. Tape pou opsyon."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtè"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Klase pa"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Dife, Zoranj"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Premye Paj"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Non Konplè"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Non Konplè..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Erè mesaj konplè"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Ale nan jodi a"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Klas"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Pousantaj nòt"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Klase"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Nòt"),
    "Help" : MessageLookupByLibrary.simpleMessage("Èd"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Kache Modpas"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Mòd Kontras Elve"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Kisa w ap fè la a?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Nan ki sans sa afekte w?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Mwen paka fè bagay yo jiskaske mwen tounen tande w."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Mwen pa gen yon kont Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Mwen gen yon kont Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Mwen bezwen èd men se pa ijan."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Mwen gen difikilte pou konekte"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ide pou app Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Pou nouka ofri w pi bon eksperyans, nou aktyalize mòd fonksyonman rapèl yo. Ou ka ajoute nouvo rapèl, pou fè sa w ap afiche yon devwa oswa yon aktivite ki nan kalandriye a epi peze bouton ki nan seksyon \"Raple m\" nan.\n\nSonje tout rapèl ki kreye ak ansyen vèsyon aplikasyon sa a pa p konpatib a nouvo chanjman yo, kidonk w ap gen pou kreye yo ankò."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Bwat resepsyon"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Bwat Resepsyon Zewo"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Enkonplè"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Domèn Enkòrèk"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Anons Enstitisyon"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Anons Enstitisyon"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Enstriksyon"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Enstitisyon ou an limite entèaksyon sou paj sa a."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Kòd QR envalid"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Sanble yon bon jou pou repoze w, amize w epi mete enèji.."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Ta sanble poko gen devwa ki kreye nan espas sa a."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Sèlman yon kesyon, yon kòmantè, yon ide... enfòmèl"),
    "Late" : MessageLookupByLibrary.simpleMessage("An reta"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lanse Zouti Eksteryè"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Mòd Klè"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Lyen Erè"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Lokal:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Jwenn Kòd QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Anplasman"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Bloke"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Dekonekte"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Flux koneksyon: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Flux koneksyon: Nòmal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Flux koneksyon: Administratè Sit"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Flux koneksyon: Sote verifikasyon mobil"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Jere Elèv"),
    "Message" : MessageLookupByLibrary.simpleMessage("Mesaj"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Sijè mesaj"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Manke"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Dwe enferyè a 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Erè Rezo"),
    "Never" : MessageLookupByLibrary.simpleMessage("Jamè"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nouvo mesaj"),
    "No" : MessageLookupByLibrary.simpleMessage("Non"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Pa gen Alèt"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Okenn Sesyon"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Pa gen Kou"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("San Delè"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Pa gen Aktivite Jodi a!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Pa gen Klas"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Okenn Anplasman Espesyal"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Pa gen Elèv"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Pa gen Sijè"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Okenn Rezime"),
    "No description" : MessageLookupByLibrary.simpleMessage("Pa gen deskripsyon"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Okenn destinatè seleksyone"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Pa Klase"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Pa Soumèt"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Ou pa yon paran?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notifikasyon pou rapèl osijè de devwa ak kalandriye aktivite yo"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Vèsyon OS"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Obsèvatè"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Yonn nan lòt aplikasyon nou yo kapab pi bon. Tape sou li pou vizite Play Store la."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Ouvri Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Ouvri nan Navigatè"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Ouvri ak yon lòt app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Kòd Kouplaj"),
    "Password" : MessageLookupByLibrary.simpleMessage("Modpas"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Modpas obligatwa"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Modpas la dwe genyen omwen 8 karaktè"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Modpas..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Nòt Planifikatè"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Tanpri antre yon adrès imèl valid"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Tanpri ekri yon adrès imèl"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Tanpri antre non konplè a"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Tanpri eskane yon kòd QR Canvas jenere"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Prin, vyolèt"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Preparasyon..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Ansyen Koneksyon"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Politik Konfidansyalite"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Lyen Politik Konfidansyalite"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Politik konfidansyalite, kondisyon itilizasyon, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Kòd QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Eske kòd QR la mande pou l gen aksè a kamera a"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Franbwaz, Wouj"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinatè"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Aktyalize"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Raple m"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Rapèl"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Rapèl la chanje!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Reponn"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Reponn Tout"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rapòte yon Pwoblèm"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Mande Èd pou Koneksyon"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Bouton pou Mande Èd pou Koneksyon"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reinisyalize app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Re eseye"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Retounen nan Koneksyon"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ELÈV"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Kapti ekran ki montre anplasman jenerasyon kòd QR nan navigatè"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Kapti ekran ki montre anplasman jenerasyon kòd kouplaj QR la nan aplikasyon Canvas Student lan."),
    "Select" : MessageLookupByLibrary.simpleMessage("Seleksyone"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Seleksyone Koulè Elèv"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Seleksyone destinatè"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Voye Kòmantè"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Voye yon mesaj osijè de devwa sa a"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Voye yon mesaj osijè e kou sa a"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Voye mesaj"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Antre yon dat ak yon lè pou yo raple w aktivite sa a."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Antre yon dat ak yon lè pou yo raple w devwa espesyal sa a."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Paramèt"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Trèf, Vèt"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Pataje lanmou ou genyen pou App la"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Afiche Modpas"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Konekte"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Gen yon entèripsyon men mwen ka kontounen li pou mwen jwenn sa mwen bezwen an."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Sispann Pase pou Itilizatè"),
    "Student" : MessageLookupByLibrary.simpleMessage("Elèv"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Kouplaj Elèv"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Elèv yo kapab kreye yon kòd QR, pou fè sa y ap itilize aplikasyon Canvas Student lan sou pòtab yo."),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Elèv yo ka jwenn yon kòd kouplaj sou sit entènèt Canvas la"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Sijè"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Soumèt"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Ale avèk siksè!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Rezime"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Chanje Itilizatè"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Pwogram"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PWOFESÈ"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tape pou pou ajoute kou ou vle wè nan Kalandriye yo nan favori. Seleksyone jiska 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tape pou kapab asosye ak yon nouvo elèv"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tape pou ka seleksyone elèv sa a"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tape pou afiche seleksyonè elèv"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Pwofesè"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Di nou kisa w pi renmen nan app la"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Kondisyon Itilizasyon"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Lyen Kondisyon Itilizasyon"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Kondisyon Itilizasyon"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Ta sanble kòd QR ou eskane a ekspire. Aktyalize kòd QR sou telefòn elèv la epi eseye ankò."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Enfòmasyon annapre yo ap ede nou konprann ide ou a pi byen:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Sèvè w antre a pa otorize pou app sa a."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Elèv w ap eseye ajoute a nan yon lòt lekòl. Konekte oswa kreye yon kont nan lekòl sa a pou w kapab eskane kòd sa a."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Ajan itilizatè app sa a pa otorize."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tèm"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Pa gen aplikasyon nan sa ki enstale yo ki kapab ouvri fichye sa a"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Pa gen oken enfòmasyon paj disponib."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Te gen yon pwoblèm nan chajman Kondisyon Itilizasyon yo"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("TE gen yon pwoblèm pou retire elèv sa a sou kont ou. Tanpri verifye koneksyon ou a epi eseye ankò."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman destinatè pou kou sa a"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman rezime detay kou sa a."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman anons sa a"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman konvèsasyon sa a"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman fichye sa a"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Te gen yo erè pandan chajman mesaj ki nan bwat resepsyon w yo."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman alèt elèv ou a."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman kalandriye elèv ou a"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman elèv ou yo."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman kou elèv ou a"),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Te gen yon erè pandan koneksyon an. Tanpri jenere yon lòt Kòd QR epi eseye ankò."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Te gen yon erè pandan w ap eseye pase pou itilizatè sa a. Tanpri verifye Domèn nan ak ID Itilizatè a epi eseye ankò."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Poko gen anyen ki merite pou yo avèti w."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Pa gen otorizasyon pou itilize app sa a."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Kou sa a poko gen devwa oswa kalandriye aktivite."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Nou pa pran fichye sa a an chaj e li pa kapab afiche nan app la"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Aksyon sa a ap dekonekte epi elimine tout enskripsyon elèv sa a te fè sou kont ou."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Klas Total"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Uh oh!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Enposib pou ale chèche kou. Tanpri verifye koneksyon ou a epi eseye ankò."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Enposib pou chaje imaj sa a"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Enposib pou jwe fichye miltimedya sa a"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Enposib pou voye mesaj. Verifye koneksyon ou a epi eseye ankò."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("An Konstriksyon"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Itilizatè Enkoni"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Chanjman ki pa anrejistre"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Fichye pa pran an chaj"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Transfere Fichye"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Itilize Kamera"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Itilize Motif Fonse nan Kontni Web"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID Itilizatè"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Non Itilizatè:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Nimewo Vèsyon"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Afiche Deskripsyon"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Afiche detay erè"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Afiche Politik Konfidansyalite a"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Pou kounye a n ap devlope fonksyon sa a pou nou ka fè w plezi."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Nou pa kapab afiche lyen sa a, petèt ke li se pou yon enstitisyon ke ou pa konekte sou li nan moman an."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nou paka jwenn okenn elèv ki asosye a kont sa a"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Nou pa kapab verifye sèvè a pou li ka itilize ak app sa a."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nou pa twò konnen kisa k pase a, men li pa enteresan. Pran kontak ak nou si sa repwodui."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Kisa nou ka amelyore?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Wi"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Ou pa p obsève okenn elèv."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Ou ka chwazi sèlman 10 kalandriye pou afiche"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Ou dwe antre yon ID itilizatè"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Ou dwe antre yon domèn valid"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Ou dwe seleksyone omwen yon kalandriye pou afiche"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Y ap raple w devwa sa a le..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Y ap raple w de aktivite sa a le..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("W ap jwenn kòd QR la sou entènèt nan pwofil ou. Klike sou \'QR pou Koneksyon Mobil\' nan lis la."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Ou dwe ouvri aplikasyon Canvas Student lan elèv ou a pou w ka kontinye. Ale nan Meni Prensipal > Paramèt > Asosye ak Obsèvatè a epi eskane kòd QR ou wè a."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Kòd ou a enkòrèk oswa li ekspire."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Kou elèv ou a ta dwe gentan pibliye."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Yo pran w!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alèt"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Kalandriye"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Gid Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Sipò Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("ratresi"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("retresi"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kou"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Kijan mwen kapab jwenn lekòl oswa distri mwen an?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Antre non lekòl la oswa distri a..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("elaji"),
    "expanded" : MessageLookupByLibrary.simpleMessage("elaji"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Jwenn Lekòl"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("mwen menm"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("mwens"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Pwochen"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("DAKO"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("voye"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("poko li"),
    "unreadCount" : m47
  };
}
