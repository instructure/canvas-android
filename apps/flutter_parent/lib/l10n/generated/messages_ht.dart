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

  static m12(score, pointsPossible) => "${score} sou ${pointsPossible} pwen";

  static m13(studentShortName) => "pou ${studentShortName}";

  static m14(threshold) => "Nòt Kou Plis Pase ${threshold}";

  static m15(threshold) => "Nòt Kou Mwens Pase ${threshold}";

  static m16(date, time) => "${date} a ${time}";

  static m17(canvasGuides, canvasSupport) => "Eseye chèche non lekòl oswa distri ou vle ale sou li a, pa egzanp “Smith Private School” or “Smith County Schools.” Ou kapab antre yon domèn Canvas dirèkteman, tankou pa egzanp “smith.instructure.com.”\n\nPou plis enfòmasyon pou jwenn kont Canvas enstitisyon ou an, ou ka vizite ${canvasGuides}, ale sou ${canvasSupport}, oswa pran kontak ak lekòl ou a pou plis asistans.";

  static m18(date, time) => "Delè ${date} a ${time}";

  static m19(userName) => "W ap sispann pase pou ${userName} epi w ap dekonekte.";

  static m20(userName) => "W ap sispann pase pou ${userName} epi retounen sou kont orijinal ou.";

  static m21(studentName, eventTitle) => "Konsènan: ${studentName}, Aktivite - ${eventTitle}";

  static m22(startAt, endAt) => "${startAt} - ${endAt}";

  static m23(grade) => "Nòt Final: ${grade}";

  static m24(studentName) => "Konsènan: ${studentName}, Premye Paj";

  static m25(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m26(studentName) => "Konsènan: ${studentName}, Nòt";

  static m27(pointsLost) => "Penalite pou reta (-${pointsLost})";

  static m28(studentName, linkUrl) => "Konsènan: ${studentName}, ${linkUrl}";

  static m29(percentage) => "Dwe siperyè a ${percentage}";

  static m30(percentage) => "Dwe enferyè a ${percentage}";

  static m31(month) => "Pwochen mwa: ${month}";

  static m32(date) => "Semenn pwochen kòmanse ${date}";

  static m33(query) => "Nou paka jwenn lekòl ki koresponn a \"${query}\"";

  static m34(points, howMany) => "${Intl.plural(howMany, one: 'Sou 1 pwen', other: 'Sou ${points} pwen')}";

  static m35(count) => "+${count}";

  static m36(points) => "${points} pwen posib";

  static m37(month) => "Mwa pase: ${month}";

  static m38(date) => "Semenn pase kòmanse ${date}";

  static m39(month) => "Mwa ${month}";

  static m40(date, time) => "Devwa sa a te soumèt nan dat ${date} a ${time} li an atant pou yo evalye li";

  static m41(studentName) => "Konsènan: ${studentName}, Pwogram";

  static m42(count) => "${count} poko li";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Aji tankou\" se koneksyon an tan ke moun sa a san modpas. W ap kapab fè nenpòt bagay kòmsi ou te itilizatè sa a, e pou lòt moun yo, se kòmsi se itilizatè sa a ki t ap fè yo. Men, jounal odit yo endike ke se te ou menm ki te fè aksyon yo nan non itilizatè sa a."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Yon deskripsyon obligatwa."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Yon sijè obligatwa."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Pase pou Itilizatè"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Ajoute Elèv"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Ajoute atachman"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Ajoute nouvo elèv"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Ajoute elèv ak..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Paramèt Alèt"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Alète m lè..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Tout Peryòd Klasman"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Yon adrès imèl obligatwa."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Gen yon erè ki fèt pandan w ap eseye afiche lyen sa a"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalandriye"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mòd Fonse"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dat"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Efase"),
    "Description" : MessageLookupByLibrary.simpleMessage("Deskripsyon"),
    "Device" : MessageLookupByLibrary.simpleMessage("Aparèy"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modèl aparèy"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domèn"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domèn:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fini"),
    "Download" : MessageLookupByLibrary.simpleMessage("Telechaje"),
    "Due" : MessageLookupByLibrary.simpleMessage("Delè"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("IJANS KRITIK EKSTRÈM!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adrès Imèl"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Imèl:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Antre kòd kouplaj yo ba ou pou elèv la. Si kòd kouplaj la pa fonksyone, li kapab ekspire"),
    "Event" : MessageLookupByLibrary.simpleMessage("Aktivite"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Egzante"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Echwe. Tape pou opsyon."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtè"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Klase pa"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Premye Paj"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Erè mesaj konplè"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Klas"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Pousantaj nòt"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Klase"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Nòt"),
    "Help" : MessageLookupByLibrary.simpleMessage("Èd"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Mòd Kontras Elve"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Nan ki sans sa afekte w?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Mwen paka fè bagay yo jiskaske mwen tounen tande w."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Mwen bezwen èd men se pa ijan."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Mwen gen difikilte pou konekte"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ide pou app Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Bwat resepsyon"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Bwat Resepsyon Zewo"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Enkonplè"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Anons Enstitisyon"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Anons Enstitisyon"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Enstriksyon"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Enstitisyon ou an limite entèaksyon sou paj sa a."),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Sanble yon bon jou pou repoze w, amize w epi mete enèji.."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Ta sanble poko gen devwa ki kreye nan espas sa a."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Sèlman yon kesyon, yon kòmantè, yon ide... enfòmèl"),
    "Late" : MessageLookupByLibrary.simpleMessage("An reta"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lanse Zouti Eksteryè"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Mòd Klè"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Lyen Erè"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Lokal:"),
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
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Ouvri nan Navigatè"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Ouvri ak yon lòt app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Kòd Kouplaj"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Preparasyon..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Ansyen Koneksyon"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Politik Konfidansyalite"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Politik konfidansyalite, kondisyon itilizasyon, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Kòd QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinatè"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Raple m"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Rapèl"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Reponn"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Reponn Tout"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rapòte yon Pwoblèm"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Mande Èd pou Koneksyon"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Bouton pou Mande Èd pou Koneksyon"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reinisyalize app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Re eseye"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Retounen nan Koneksyon"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ELÈV"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Seleksyone destinatè"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Voye yon mesaj osijè de devwa sa a"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Voye yon mesaj osijè e kou sa a"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Voye mesaj"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Antre yon dat ak yon lè pou yo raple w aktivite sa a."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Antre yon dat ak yon lè pou yo raple w devwa espesyal sa a."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Paramèt"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Pataje lanmou ou genyen pou App la"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Gen yon entèripsyon men mwen ka kontounen li pou mwen jwenn sa mwen bezwen an."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Sispann Pase pou Itilizatè"),
    "Student" : MessageLookupByLibrary.simpleMessage("Elèv"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Sijè"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Soumèt"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Ale avèk siksè!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Rezime"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Chanje Itilizatè"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Pwogram"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PWOFESÈ"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tape pou pou ajoute kou ou vle wè nan Kalandriye yo nan favori."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tape pou kapab asosye ak yon nouvo elèv"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tape pou ka seleksyone elèv sa a"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tape pou afiche seleksyonè elèv"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Pwofesè"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Di nou kisa w pi renmen nan app la"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Kondisyon Itilizasyon"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Enfòmasyon annapre yo ap ede nou konprann ide ou a pi byen:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Sèvè w antre a pa otorize pou app sa a."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Ajan itilizatè app sa a pa otorize."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tèm"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Pa gen aplikasyon nan sa ki enstale yo ki kapab ouvri fichye sa a"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Pa gen oken enfòmasyon paj disponib."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Te gen yon pwoblèm nan chajman Kondisyon Itilizasyon yo"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman destinatè pou kou sa a"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman rezime detay kou sa a."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman anons sa a"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman konvèsasyon sa a"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman fichye sa a"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Te gen yo erè pandan chajman mesaj ki nan bwat resepsyon w yo."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman alèt elèv ou a."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman kalandriye elèv ou a"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman elèv ou yo."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Te gen yon erè nan chajman kou elèv ou a."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Te gen yon erè pandan w ap eseye pase pou itilizatè sa a. Tanpri verifye Domèn nan ak ID Itilizatè a epi eseye ankò."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Poko gen anyen ki merite pou yo avèti w."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Pa gen otorizasyon pou itilize app sa a."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Kou sa a poko gen devwa oswa kalandriye aktivite."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Nou pa pran fichye sa a an chaj e li pa kapab afiche nan app la"),
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
    "User ID" : MessageLookupByLibrary.simpleMessage("ID Itilizatè"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Non Itilizatè:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Nimewo Vèsyon"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Afiche detay erè"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Pou kounye a n ap devlope fonksyon sa a pou nou ka fè w plezi."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Nou pa kapab afiche lyen sa a, petèt ke li se pou yon enstitisyon ke ou pa konekte sou li nan moman an."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nou paka jwenn okenn elèv ki asosye a kont sa a"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Nou pa kapab verifye sèvè a pou li ka itilize ak app sa a."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nou pa twò konnen kisa k pase a, men li pa enteresan. Pran kontak ak nou si sa repwodui."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Wi"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Ou pa p obsève okenn elèv."),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Ou dwe antre yon ID itilizatè"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Ou dwe antre yon domèn valid"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Y ap raple w devwa sa a le..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Y ap raple w de aktivite sa a le..."),
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
    "collapse" : MessageLookupByLibrary.simpleMessage("ratresi"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("retresi"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kou"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Kijan mwen kapab jwenn lekòl oswa distri mwen an?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Antre non lekòl la oswa distri a..."),
    "dueDateAtTime" : m18,
    "endMasqueradeLogoutMessage" : m19,
    "endMasqueradeMessage" : m20,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("elaji"),
    "expanded" : MessageLookupByLibrary.simpleMessage("elaji"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Jwenn Lekòl"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("mwen menm"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("mwens"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("Pwochen"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("DAKO"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("voye"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("poko li"),
    "unreadCount" : m42
  };
}
