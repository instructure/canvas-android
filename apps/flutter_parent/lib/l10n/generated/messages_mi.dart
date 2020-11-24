// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a mi locale. All the
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
  String get localeName => 'mi';

  static m0(userName) => "Kei te mahi koe pēnei ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Kōeke Whakataunga Runga ${threshold}";

  static m3(threshold) => "Kōeke Whakataunga Raro ${threshold}";

  static m4(moduleName) => "Kua rakaina tō whakataunga e te kōwai \"${moduleName}\".";

  static m5(studentName, assignmentName) => "E pā ana: ${studentName}, Whakataunga: ${assignmentName}";

  static m6(points) => "${points} ngā koinga";

  static m7(points) => "${points} ngā koinga";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} ki 1 tētahi atu', other: '${authorName} kī ${howMany} ētahi atu')}";

  static m9(authorName, recipientName) => "${authorName} ki te ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} kī ${recipientName} & 1 tētahi atu', other: '${authorName} kī ${recipientName} & ${howMany} ētahi atu')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Huria tae mō ${studentName}";

  static m13(score, pointsPossible) => "${score} waho ō ${pointsPossible} ngā koinga";

  static m14(studentShortName) => "mo ${studentShortName}";

  static m15(threshold) => "Kōeke Akoranga Runga ${threshold}";

  static m16(threshold) => "Kōeke Akoranga Raro ${threshold}";

  static m17(date, time) => "${date} ī ${time}";

  static m18(alertTitle) => "Whakakore ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Ngana ki te rapu haere te ingoa o te kura, rohe rānei e hiahia ana koe ki te whakauru, “Smith Kura Tūmataiti” “Smith Kura Rohe” rānei. Ka taea e koe te whwkauru he Canvas rohe hāngai tonu pēnei “smith.instructure.com”.\n\nMō ētahi atu pūrongo hei rapu i tō whakanōhanga pūkete Canvas, ka taea e koe te haere ki te ${canvasGuides} whakapā atu kī ${canvasSupport} are whakapā ki te kura hei āwhina.";

  static m20(date, time) => "Rā tika ${date} ī ${time}";

  static m21(userName) => "Ka mutu tō mahi hei ${userName} ana ka takiwahotia.";

  static m22(userName) => "Ka mutu tō mahi hei ${userName} ka hoki ki tāu pūkete.ake.";

  static m23(studentName, eventTitle) => "E pā ana: ${studentName}, Tawhainga: ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} / ${endAt}";

  static m25(grade) => "Kōeke whakamutunga: ${grade}";

  static m26(studentName) => "E pā ana: ${studentName}, Whārangi Mua";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "E pā ana: ${studentName}, Ngā Kōeke";

  static m29(pointsLost) => "Whiu tōmuri (-${pointsLost})";

  static m30(studentName, linkUrl) => "E pā ana: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Me runga ake ${percentage}";

  static m32(percentage) => "Me raro iho ${percentage}";

  static m33(month) => "Marama i muri mai: ${month}";

  static m34(date) => "Tīmata te wiki e heke mai nei ${date}";

  static m35(query) => "Kaore e taea te kitea ngā kura ōrite \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'I waho i te 1 koinga', other: 'I waho i te ${points} ngā kōinga')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} ngā koinga e taea";

  static m39(month) => "Marama o muri nei: ${month}";

  static m40(date) => "Tīmata te wiki o muri ${date}";

  static m41(termsOfService, privacyPolicy) => "Ma te patopato i \'Hanga Pūkete\', e whakāe ana koe ki te ${termsOfService} me te whakamihi te ${privacyPolicy}";

  static m42(version) => "He whakāro pūaki mo Android - Canvas Matua ${version}";

  static m43(month) => "Marama ō ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} tīmataī', other: '${position} ngā whetu')}";

  static m45(date, time) => "I tukuna tēnei whakataunga i runga i ${date} kī ${time} ana kei te tatari ki te kōeketia";

  static m46(studentName) => "E pā ana: ${studentName}, Marautanga";

  static m47(count) => "${count} kaore i pānuihia";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Ko te Mahi hei\" kei te takiuru tēnei kaiwhakamahi me te kore kupuhipa. Ka taea e koe te mahi i tētahi mahi me te mea ko koe tēnei kaiwhakamahi, me ētahi atu whakaaro ō ngā kaiwhakamahi, \'ka mahia anō nei ngā te kaiwhakamahi i mahi. Heoi, ka tuhia e ngā kaute tuhinga ko koe te kaihanga i ngā mahi mo tēnei kaiwhakamahi."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("E hiahia ana he whakāturanga."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("He hapa whatunga i puta i te wā e tāpiri ana ki tēnei ākonga Tirohia tō hononga ana ka ngana anō."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("He kaupapa e hiahiatia ana."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Mahi Rite Kaiwhakamahi"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Tapiri ākonga"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Tāpiri āpitihanga"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Tāpiri ākonga hou"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Tapiri ākonga me..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Matohi Tautuhinga"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Matohi ahau ina..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Takiwā kōeke katoa"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("He pūkete tāū? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("He whakaaturanga e hiahiatia ana."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("He hapa i puta i te wā e ngana ana ki te whakātu i tēnei whārangi.hono"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("He hapa i puta i te wā e tiaki ana i tō tīpako Tēnā koa ngana anō."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("He hapa ohorere i puta"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS Putanga"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Āhua"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Taupānga whakāturanga"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("He ākonga he kaiako rānei koe?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Kei te tino hiahia koe ki te takiuru atu?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Kei te tino hiahia koe ki te kati i tēnei whārangi? Ka ngāro tō karere kaore anō nei i tukuna."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Taipitopito whakataunga"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Kōeke whakataunga runga"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Kōeke whakataunga raro"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("E ngaro ana whakataunga"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, Ngenge"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Ngā Maramataka"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Kāmera Whakaaetanga"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Whakakore"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Ākonga"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Kaiako"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas i runga GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Kōwhiri he akoranga hei karare"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Kōwhiri mai i te Taiwhanga"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Oti"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Tautoko Whakapā"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Akoranga Pānuitanga"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Pānuitanga Akoranga"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Kōeke akoranga i runga"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Kōeke akoranga i raro"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Waihanga Pūkete"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Aratau Pouri"),
    "Date" : MessageLookupByLibrary.simpleMessage("Rā"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Muku"),
    "Description" : MessageLookupByLibrary.simpleMessage("Whakāhuatanga"),
    "Device" : MessageLookupByLibrary.simpleMessage("Pūrere"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Pūrere tauira"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Rohe"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Rohe:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Kaua e whakātu anō"),
    "Done" : MessageLookupByLibrary.simpleMessage("Kua mahia"),
    "Download" : MessageLookupByLibrary.simpleMessage("Tikiake"),
    "Due" : MessageLookupByLibrary.simpleMessage("E tika ana"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("TINO URUPARE NUI!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Hikohiko, Kahurangi"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Īmēra Wāhitau"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Īmēra:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Īmēra ..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Whakauru te ākonga waihere hono kua tukuna atu ki a koe. Mehema kaore te waihere hono e mahi; kua pau pea te wā"),
    "Event" : MessageLookupByLibrary.simpleMessage("Tauwhāinga"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Whakawātea"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Kua pau QR Waehere"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("I hapa. Pātō mo ngā kōwhiringa."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Tātari"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Tātari mā"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Ahi, Ārani"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Whārangi mua"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Ingoa Katoa"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Ingoa Katoa ..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Nui karere Hapa"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Haere ki te rā"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Kōeke"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Kōeke pai hēneti"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Kōekehia"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Ngā Kōeke"),
    "Help" : MessageLookupByLibrary.simpleMessage("Āwhina"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Huna Kupuhipa"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Pūrata nui Aratau"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("E pēhea ana tātou?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Kia pehea e pa ana tenei ki a koe?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Kaore e taea e ahau te whakaoti ki a hoki rongo rānō ahau mai ia koe."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Kaore he pūkete Canvas tāku"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("He Pūkete Canvas tāku"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("E hiahia ana ahau ētahi awhina ēngari e kore te kōhukihuki."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Kei te raru ahau ki te takiuru"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Whakaaro mō te Taupānga Canvas Matua [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("I roto i te hoatu atu ki a koe he wheako pai, kua whakahoutia e mātou te whakamahi te pēhea te mahi ngā maumahara. Ka taea e koe te tāpiri maumahara hou ma te titiro he whakataunga ara maramataka Tauwhāinga rānei me te pātō te huringa i raro i te \"Maumahara Ahau\" wāhanga.\n\nKia matāra ngā mauamahara kua hangatia mai ngā wāhanga tawhito o tēnei taupānga kaore e uru atu ki ngā huringa hou ana me hanga e koe anō."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Pouakauru"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Pouakaroto Kore"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Kaore i te oti"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Rohe hē"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Panuitanga o te Whakanōhanga"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Whaaknōhanga pānutianga"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Ngā Tohutohu"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Ngā whakapā i runga i tēnei whārangi ka aukatitia e tō wharenōhanga."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("QR waehere koremana"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Ko te āhua nei he rā pai ki te whakatā me te whakahou anō."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Ko te āhua nei kaore ngā whakataunga i hāngaia i roto i tēnei wāhi."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("He pātai tüao, kōrero, whakāro, whakāro pūaki..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Tūreiti"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Neke Taputapu Waho"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Ture"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Aratau Mārama"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Hono Hapa"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Tauwāhi:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Kimihia QR Waehere"),
    "Location" : MessageLookupByLibrary.simpleMessage("Wāhi"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Kua rakaina"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Takiputa"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Rere o te takiuru: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Rere o te takiuru: Noa"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Rere o te takiuru: Pae Whakahaere"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Rere o te takiuru: Peke tautoko i te waea haerēre"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Whakahaere ngā ākonga"),
    "Message" : MessageLookupByLibrary.simpleMessage("Karere"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Kaupapa karere"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Ngaro"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Me raro iho i te 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Hapa whatunga"),
    "Never" : MessageLookupByLibrary.simpleMessage("Kaua rawa"),
    "New message" : MessageLookupByLibrary.simpleMessage("Karere hōu"),
    "No" : MessageLookupByLibrary.simpleMessage("Kahore"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Kaore he whakamataara"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Kaore ngā whakataunga"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Kāore he Akoranga"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Kaore He Rā Tika"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Kaore he tauwhāinga i tēnei rā!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Kaore he Kōeke"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Tauwhāititia Kāore he Wāhi"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Kāore he ākonga"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Kāore he Kaupapa"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Kaore he Whakarāpopototanga"),
    "No description" : MessageLookupByLibrary.simpleMessage("Kāore he whakaahuatanga"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Kaore ngā kaiwhiwhi i tīpakohia"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Kāore i kōekehia"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("E Kore E Tukua"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Kaore i te matua?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Ngā whakamōhiotanga mo ngā whakataunga me ngā tauwhāinga māramataka"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS Putanga"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Kaimātakitaki"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Ko tētahi atu o a mātou taupānga pea he pai ake te uru. Pātō kotahi ki te toro atu ki te Toa Tākaro."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Huaki Canvas Ākonga"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Tuwhera I roto i te Pūtirotiro"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Tuwhera me tētahi atu taupānga"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Waihere Hono"),
    "Password" : MessageLookupByLibrary.simpleMessage("Kupuhipa"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("E hiahiatia naa te kupuhipa"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Me mau te kupuhipa  iti atu i te 8 ngā pūāhua"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Kupuhipa ..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Mahere Tuhipoka"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Tēnā koa whakauru he īmēra wāhitau whiwhi mana"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Tēnā koa whakauru he īmēra wāhitau"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Tēnā koa whakauru he ingoa hou.tonu"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Tēnā koa mātai he QR Waehere kua whakamahia e Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Paramū, Waiporoporo"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("E whakareri ana..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Ngā takiurunga o mua"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Kaupapahere Tūmataiti"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Kaupapahere Tūmataiti Hono"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Kaupapahere tūmataitinga, tikanga whakamahi, puna tuwhera"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR Waehere"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Ka hiahiatia te QR mātai i te whakauru ki te kāmera."),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Rāpere, Whero"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Ngā Kaiwhiwhi"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Whakahouhia"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Whakamaumahara mai ki au"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Ngā Whakamaumahara"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Kua huria ngā Maumahara!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Whakautu"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Whakautu ki te katoa"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Pūrongo te raruraru"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Tonoa Takiuru Awhina"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Tonoa Takiuru Awhina Pātene"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Whakamahi anō te taupanga"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Ngana anō"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Hoki ki te takiuru"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ĀKONGA"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Tangirua e whkātu ana rohe o te QR Waehere whakamahia i roto i te pūtirotiro"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Pere mata e tohu ana te wāhi o te hono QR Waehere whakatupuranga i roto i te Canvas Ākonga Taupanga"),
    "Select" : MessageLookupByLibrary.simpleMessage("Tīpakohia"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Tīpakohia te Ākonga Rōpū"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Tīpako ngā kaiwhiwhi"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Tuku Urupare"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Tukuna he karere mō tēnei whakataunga"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Tukuna he karere mō tēnei akoranga"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Tuku karere"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Whakatau he rā me te wā hei whakamōhiotia mo tēnei tauwhainga."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Whakatau he rā me te wā hei whakamōhiotia mo tēnei whakataunga ake."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Ngā Tautuhinga"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Rangirua, Kākariki"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Tuari Tō Aroha mō te Taupānga"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Whakatu Kupuhipa"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Waitohu I roto i"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Kua pakaru tētahi mea ēngari ka taea e ahau te mahi a tawhio noa ki a oti ai ngā mea e hiahia ana ahau."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Kati te mahi rite tonu i te kaiwhakamahi"),
    "Student" : MessageLookupByLibrary.simpleMessage("Ākonga"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Akonga Hono"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Ka taea e ng aākonga te hanga he QR waehere ma te mahi i te Canvas Ākonga Taupanga i runga i tā rātou taonga haerēre"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Ka taea e ngā ākonga te tiki waihere hono ma te mahi i te Canvas nipurangi"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Kaupapa"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Kua Tukuna"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Oti pai te tuku!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Whakarāpopototanga"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Whakakā ngā Kaiwhakamahi"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Marautanga"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("KAIAKO"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Pātō ki te hiahia o te akoranga e hiahia ana koe ki te kite i runga i te Maramataka. Tīpakohia ki 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Pātō ki te hono me tētahi ākonga hou"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Pātō ki te tīpako i tēnei ākonga"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Pātō ki te whakātu te kaitīpako o te ākonga"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Kaiako"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Korerotia mai ki a matou ngā wahi tino makau o te taupānga ki a koe"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Ngā Ture o te Ratonga"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Ngā Ture o te Ratonga Hono"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Ngā ritenga whakamahi"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Te QR Waehere kua matawaiatia e koe kua pau Whakahou te waeherre i runga i te taputapu a te ākonga ana ka ngana anō"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Ka awhinatia mātou ki te mōhio i tō whakāro i ngā mōhiohio e whai ake nei:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("E kore te tūmau kua whakauru koe te mana mo tenei taupānga."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Ko te akonga e ngana ana koe ki te tāpiri nō tētahi kura rerekē. Whakauru ara hanga rānei he pūkete me tēra kura ki te matawai ki tēnei waehere"),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Te kaihoko kaiwhakamahi mo tenei taupānga kaore i manatia."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Kaupapa"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Kaore he taupānga kua whakaurutia ka taea te huaki i tēnei kōnae"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Kāore he mōhiohio whārangi e wātea ana."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("He raruraru i te wā e uta ana i ngā Ture Whakamahia"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("He raruraru i te wā e tango ana i tēnei ākonga mai i tō pūkete Tēnā koa tirohia tō hononga ana ka tarai anō."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i ngā kaiwhiwhi mo tēnei akoranga"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i ngā taipitopito whakarāpopotanga mo tēnei akoranga."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i tēnei pānui"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i tēnei kōrerorero"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i tēnei kōnae"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana tō karere pouaka whakauru."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana ngā matohi a tō ākonga."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana te maramataka a tō ākonga"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i ō ākonga."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i tō ākonga akoranga."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("He hapa i puta i te wā e takiuru ana. Tēnā koa whakamahi he QR Waehere anō ka ngana anō."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e ngana ana ki te mahi hei kaiwhakamahi. Tēnā koa āta titiro te Rohe me Kaiwhakamahi ID ana ka ngana anō."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Kaore he mea hei whakamōhio i tēnei wā."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Kaore e mana tēnei taupānga ki te whakamahi."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Kaore he whakataunga mo tēnei akoranga ara maramataka tauwhainga rānei i tēnei wā."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Kaore tēnei konae i te tautokotia ana kaore e taea te kitea ma te taupānga"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Ka kore hono tēnei ara ka tango ngā whakaurunga mo tēnei ākonga mai i tō pūkete"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Kōeke Tapeke"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Aue!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kaore e taea te tiki i ngā akoranga. Tēnā koa tirohia tō hononga ana ka tarai anō."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Kaore e taea te uta tēnei āhua"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Kaore e taea te whakamahi i tēnei kōnae Pāpāho"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Kaore e taea tuku Karere. Tirohia tō hononga ana ka ngana anō."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Kei raro i te hanga"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Kaiwhakamahi kaore e mōhiotia"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Ngā whakarerekētanga kaore i tiakina"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Kaore e tautokotia kōnae"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Tukuake Kōnae"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Whakamahi te Kāmera"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Whakamahia Kaupapa Pouri i roto i te Ipurangi Ihirangi"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Kaiwhakamahi ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID Kaiwhakamahi:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Whakāturanga Tau"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Tirohia te whakāturanga"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Tirohia ngā hapa taipitopito"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Tirohia te Kaupapahere Tūmataiti"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Kei te hanga mātou i tēnei āhuatanga kia pai ai tō mātakitaki."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Kaore e taea e mātou te whakātu i tēnei hono, ekene e mau ana ki tētahi whakanōhanga kaore koe i te takiuru i tēnei wā."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Kaore i kitea e mātou ngā ākonga e hono ana ki tēnei pūkete"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("I taea e mātou ki te manatoko i te tūmau mō te whakamahi ki tenei taupānga matou."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Kaore mātou i te tino mōhio he aha te mahi, ngari kaore i te pai. Whakapā mai ki a mātou mehemea ka mahi pēnei tonu tēnei."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Me pēhea e whakapiki ake ai te mahi e mātou?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ae"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Kaore koe e tirotiro ana ētahi ākonga."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Me kōwhiria e koe 10 maramataka anake ki te whakātu"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Me whakauru e koe he kaiwhakamahi id"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Me whakauru e koe i te rohe whaimana"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Me tīpako i te iti rawa tētahi maramataka ki te whakātu"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Ka whakamōhiotia koe mo tēnei whakataunga i runga i..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Ka whakamōhiotia koe mo tēnei tauwhainga i runga i..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Ka kitea e koe te QR waehere i runga i te ipurangi i roto i tō pūkete kōtaha. Pātō QR mo te Waea Haerēre Takiuru i roto i te rārangi."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Me huakina tō Canvas Ākonga Taupanga ki te haere tonu Haere ki roto i te Tahua Matua > Ngā Tautuhinga me te Kaitirotiro  me te matawai i te QR Waehere ka kitea e koe i reira"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Kei te hē kua pau rānei tō waihere."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Kaore e pānuitia ō ākonga akoranga i tēnei wā."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Kua mau katoa koe!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("He whakamataara"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Maramataka"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas Kaiārahi"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas moko"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas Tautoko"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("hinga"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("hinga"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Ngā Akoranga"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Me pehea e kitea ai e ahau taku kura rohe rānei?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Whakauru kura ingoa rohe rānei..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("whakawhānui"),
    "expanded" : MessageLookupByLibrary.simpleMessage("whakawhānui"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Kimihia taku kura"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("ahau"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("tango"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("E haere ake nei"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("Ae"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("tukua"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("kaore i pānuitia"),
    "unreadCount" : m47
  };
}
