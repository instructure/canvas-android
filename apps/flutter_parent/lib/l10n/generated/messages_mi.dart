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

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Kōeke Whakataunga Runga ${threshold}";

  static m3(threshold) => "Kōeke Whakataunga Raro ${threshold}";

  static m4(moduleName) => "Kua rakaina tō whakataunga e te kōwai \"${moduleName}\"";

  static m5(studentName, assignmentName) => "E pā ana: ${studentName}, Whakataunga: ${assignmentName}";

  static m6(points) => "${points} ngā koinga";

  static m7(points) => "${points} ngā koinga";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} ki 1 tētahi atu', other: '${authorName} kī ${howMany} ētahi atu')}";

  static m9(authorName, recipientName) => "${authorName} ki te ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} kī ${recipientName} & 1 tētahi atu', other: '${authorName} kī ${recipientName} & ${howMany} ētahi atu')}";

  static m11(count) => "${count}+";

  static m12(score, pointsPossible) => "${score} waho ō ${pointsPossible} ngā koinga";

  static m13(studentShortName) => "mo ${studentShortName}";

  static m14(threshold) => "Kōeke Akoranga Runga ${threshold}";

  static m15(threshold) => "Kōeke Akoranga Raro ${threshold}";

  static m16(date, time) => "${date} ī ${time}";

  static m17(canvasGuides, canvasSupport) => "Ngana ki te rapu haere te ingoa o te kura, rohe rānei e hiahia ana koe ki te whakauru, “Smith Kura Tūmataiti” “Smith Kura Rohe” rānei. Ka taea e koe te whwkauru he Canvas rohe hāngai tonu pēnei “smith.instructure.com”.\n\nMō ētahi atu pūrongo hei rapu i tō whakanōhanga pūkete Canvas, ka taea e koe te haere ki te ${canvasGuides} whakapā atu kī ${canvasSupport} are whakapā ki te kura hei āwhina.";

  static m18(date, time) => "Rā tika ${date} ī ${time}";

  static m21(studentName, eventTitle) => "E pā ana: ${studentName}, Tawhainga: ${eventTitle}";

  static m22(startAt, endAt) => "${startAt} - ${endAt}";

  static m23(grade) => "Kōeke whakamutunga: ${grade}";

  static m24(studentName) => "E pā ana: ${studentName}, Whārangi Mua";

  static m25(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m26(studentName) => "E pā ana: ${studentName}, Ngā Kōeke";

  static m27(pointsLost) => "Whiu tōmuri (-${pointsLost})";

  static m28(studentName, linkUrl) => "E pā ana: ${studentName}, ${linkUrl}";

  static m29(percentage) => "Me runga ake ${percentage}";

  static m30(percentage) => "Me raro iho ${percentage}";

  static m31(month) => "Marama i muri mai: ${month}";

  static m32(date) => "Tīmata te wiki e heke mai nei ${date}";

  static m33(query) => "Kaore e taea te kitea ngā kura ōrite \"${query}\"";

  static m34(points, howMany) => "${Intl.plural(howMany, one: 'I waho i te 1 koinga', other: 'I waho i te ${points} ngā kōinga')}";

  static m35(count) => "+${count}";

  static m36(points) => "${points} ngā koinga e taea";

  static m37(month) => "Marama o muri nei: ${month}";

  static m38(date) => "Tīmata  te wiki o muri ${date}";

  static m39(month) => "Marama ō ${month}";

  static m40(date, time) => "I tukuna tēnei whakataunga i runga i ${date} kī ${time} ana kei te tatari ki te kōeketia";

  static m41(studentName) => "E pā ana: ${studentName}, Marautanga";

  static m42(count) => "${count} kaore i pānuihia";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("E hiahia ana he whakāturanga."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("He kaupapa e hiahiatia ana."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Tapiri ākonga"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Tāpiri āpitihanga"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Tāpiri ākonga hou"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Tapiri ākonga me..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Matohi Tautuhinga"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Matohi ahau ina..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Takiwā kōeke katoa"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("He whakaaturanga e hiahiatia ana."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("He hapa i puta i te wā e ngana ana ki te whakātu i tēnei hono"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("Ngā Maramataka"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Aratau Pouri"),
    "Date" : MessageLookupByLibrary.simpleMessage("Rā"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Muku"),
    "Description" : MessageLookupByLibrary.simpleMessage("Whakāhuatanga"),
    "Device" : MessageLookupByLibrary.simpleMessage("Pūrere"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Pūrere tauira"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Rohe:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Kua mahia"),
    "Download" : MessageLookupByLibrary.simpleMessage("Tikiake"),
    "Due" : MessageLookupByLibrary.simpleMessage("E tika ana"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("TINO URUPARE NUI!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Īmēra Wāhitau"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Īmēra:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Whakauru te ākonga waihere hono kua tukuna atu ki a koe. Mehema kaore te waihere hono e mahi; kua pau pea te wā"),
    "Event" : MessageLookupByLibrary.simpleMessage("Tauwhāinga"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Whakawātea"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("I hapa. Pātō mo ngā kōwhiringa."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Tātari"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Tātari mā"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Whārangi mua"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Nui karere Hapa"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Kōeke"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Kōeke pai hēneti"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Kōekehia"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Ngā Kōeke"),
    "Help" : MessageLookupByLibrary.simpleMessage("Āwhina"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Pūrata nui Aratau"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Kia pehea  e pa ana tenei ki a koe?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Kaore e taea e ahau te whakaoti ki a hoki rongo rānō ahau mai ia koe."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("E hiahia ana ahau ētahi awhina ēngari e kore te kōhukihuki."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Kei te raru ahau ki te takiuru"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Whakaaro mō te Taupānga Canvas Matua [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Pouakauru"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Pouakaroto Kore"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Kaore i te oti"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Panuitanga o te Whakanōhanga"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Whaaknōhanga pānutianga"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Ngā Tohutohu"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Ko te āhua nei he rā pai ki te whakatā me te whakahou anō."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Ko te āhua nei kaore ngā whakataunga i hāngaia i roto i tēnei wāhi."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("He pātai tüao, kōrero, whakāro, whakāro pūaki..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Tūreiti"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Neke Taputapu Waho"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Ture"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Aratau Mārama"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("He hapa hono"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Tauwāhi:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Wāhi"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Kua rakaina"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Takiputa"),
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
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Tuwhera I roto i te Pūtirotiro"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Tuwhera me tētahi atu taupānga"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Waihere Hono"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("E whakareri ana..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Ngā takiurunga o mua"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Kaupapahere Tūmataiti"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Kaupapahere tūmataitinga, tikanga whakamahi, puna tuwhera"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR Waehere"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Ngā Kaiwhiwhi"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Whakamaumahara mai ki au"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Ngā Whakamaumahara"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Whakautu"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Whakautu ki te katoa"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Pūrongo te raruraru"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Tonoa Takiuru Awhina"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Tonoa Takiuru Awhina Pātene"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Whakamahi anō te taupanga"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Ngana anō"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Hoki ki te takiuru"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ĀKONGA"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Tīpako ngā kaiwhiwhi"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Tukuna he karere mō tēnei whakataunga"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Tukuna he karere mō tēnei akoranga"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Tuku karere"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Whakatau he rā me te wā hei whakamōhiotia mo tēnei tauwhainga."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Whakatau he rā me te wā hei whakamōhiotia mo tēnei whakataunga ake."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Ngā Tautuhinga"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Tuari Tō Aroha mō te Taupānga"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Kua pakaru tētahi mea ēngari ka taea e ahau te mahi a tawhio noa ki a oti ai ngā mea e hiahia ana ahau."),
    "Student" : MessageLookupByLibrary.simpleMessage("Ākonga"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Kaupapa"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Kua Tukuna"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Oti pai te tuku!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Whakarāpopototanga"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Whakakā ngā Kaiwhakamahi"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Marautanga"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("KAIAKO"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Pātō ki tō akoranga e hiahia ana koe ki te kite i te Maramataka"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Pātō ki te hono me tētahi ākonga hou"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Pātō ki te tīpako i tēnei ākonga"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Pātō ki te whakātu te kaitīpako o te ākonga"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Kaiako"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Korerotia mai ki a matou ngā wahi tino makau o te taupānga ki a koe"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Ngā ritenga whakamahi"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Ka awhinatia mātou ki te mōhio i tō whakāro i ngā mōhiohio e whai ake nei:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("E kore te tūmau kua whakauru koe te mana mo tenei taupānga."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Te kaihoko kaiwhakamahi mo tenei taupānga kaore i manatia."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Kaupapa"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Kaore he taupānga kua whakaurutia ka taea te huaki i tēnei kōnae"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Kāore he mōhiohio whārangi e wātea ana."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("He raruraru i te wā e uta ana i ngā Ture Whakamahia"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i ngā kaiwhiwhi mo tēnei akoranga"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i ngā taipitopito whakarāpopotanga mo tēnei akoranga."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i tēnei pānui"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i tēnei kōrerorero"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i tēnei kōnae"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana tō karere pouaka whakauru."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana ngā matohi a tō ākonga."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana te maramataka a tō ākonga."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i ō ākonga."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("He hapa i te wā e uta ana i tō ākonga akoranga."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Kaore he mea hei whakamōhio i tēnei wā."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Kaore e mana tēnei taupānga ki te whakamahi."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Kaore he whakataunga mo tēnei akoranga  ara maramataka tauwhainga rānei i tēnei wā."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Kaore tēnei konae i te tautokotia ana kaore e taea te kitea ma te taupānga"),
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
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID Kaiwhakamahi:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Whakāturanga Tau"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Tirohia ngā hapa taipitopito"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Kei te hanga mātou i tēnei āhuatanga kia pai ai tō mātakitaki."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Kaore e taea e mātou te whakātu i tēnei hono, ekene e mau ana i tētahi wharekōhanga kaore koe i te takiuru i tēnei wā."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Kaore i kitea e mātou ngā ākonga e hono ana ki tēnei pūkete"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("I taea e mātou ki te manatoko i te tūmau mō te whakamahi ki tenei taupānga matou."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Kaore mātou i te tino mōhio he aha te mahi, ngari kaore i te pai. Whakapā mai ki a mātou mehemea ka mahi pēnei tonu tēnei."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Ae"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Kaore koe e tirotiro ana ētahi ākonga."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Ka whakamōhiotia koe mo tēnei whakataunga i runga i..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Ka whakamōhiotia koe mo tēnei tauwhainga i runga i..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Kei te hē kua pau rānei tō waihere."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Kaore e pānuitia ō ākonga akoranga i tēnei wā."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Kua mau katoa koe!"),
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
    "collapse" : MessageLookupByLibrary.simpleMessage("hinga"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("hinga"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Ngā Akoranga"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Me pehea e kitea ai e ahau taku kura rohe rānei?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Whakauru kura ingoa rohe rānei..."),
    "dueDateAtTime" : m18,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("whakawhānui"),
    "expanded" : MessageLookupByLibrary.simpleMessage("whakawhānui"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Kimihia taku kura"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("ahau"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("tango"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("E haere ake nei"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("Ae"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("tukua"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("kaore i pānuitia"),
    "unreadCount" : m42
  };
}
