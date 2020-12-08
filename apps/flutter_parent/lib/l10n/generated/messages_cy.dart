// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a cy locale. All the
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
  String get localeName => 'cy';

  static m0(userName) => "Rydych chi’n gweithredu fel ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Gradd yr Aseiniad yn Uwch na ${threshold}";

  static m3(threshold) => "Gradd yr Aseiniad yn Is na ${threshold}";

  static m4(moduleName) => "Mae\'r aseiniad hwn wedi\'i gloi gan y modiwl \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Ynghylch: ${studentName}, Aseiniad - ${assignmentName}";

  static m6(points) => "${points} pwynt";

  static m7(points) => "${points} pwynt";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} i 1 arall', other: '${authorName} i ${howMany} arall')}";

  static m9(authorName, recipientName) => "${authorName} i ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} i ${recipientName} ac 1 arall', other: '${authorName} i ${recipientName} a ${howMany} arall')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Newid lliw ${studentName}";

  static m13(score, pointsPossible) => "${score} allan o ${pointsPossible} pwynt";

  static m14(studentShortName) => "ar gyfer ${studentShortName}";

  static m15(threshold) => "Gradd y Cwrs yn Uwch na ${threshold}";

  static m16(threshold) => "Gradd y Cwrs yn Is na ${threshold}";

  static m17(date, time) => "${date} at ${time}";

  static m18(alertTitle) => "Gwrthod ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Ceisiwch chwilio am enw’r ysgol neu’r ardal rydych chi’n ceisio cael mynediad atynt, fel “Smith Private School” neu “Smith County Schools.” Gallwch chi hefyd roi parth Canvas yn uniongyrchol, fel “smith.instructure.com.”\n\nI gael rhagor o wybodaeth ynglŷn â chanfod cyfrif Canvas eich sefydliad, ewch i ${canvasGuides}, gofynnwch i ${canvasSupport}, neu cysylltwch â’ch ysgol i gael help.";

  static m20(date, time) => "Erbyn ${date} am ${time}";

  static m21(userName) => "Byddwch chi’n stopio gweithredu fel ${userName} ac yn cael eich allgofnodi.";

  static m22(userName) => "Byddwch chi’n stopio gweithredu fel ${userName} ac yn dychwelyd i’ch cyfrif gwreiddiol.";

  static m23(studentName, eventTitle) => "Ynghylch: ${studentName}, Digwyddiad - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Gradd Derfynol: ${grade}";

  static m26(studentName) => "Ynghylch: ${studentName}, Tudalen Flaen";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Ynghylch: ${studentName}, Graddau";

  static m29(pointsLost) => "Cosb am fod yn hwyr (-${pointsLost})";

  static m30(studentName, linkUrl) => "Ynghylch: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Rhaid bod yn uwch na ${percentage}";

  static m32(percentage) => "Rhaid bod yn is na ${percentage}";

  static m33(month) => "Mis nesaf: ${month}";

  static m34(date) => "Wythnos nesaf yn cychwyn ${date}";

  static m35(query) => "Doedd dim modd dod o hyd i ysgolion yn cyfateb â \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Allan o 1 pwynt', other: 'Allan o ${points} pwynt')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} pwynt yn bosib";

  static m39(month) => "Mis blaenorol: ${month}";

  static m40(date) => "Wythnos flaenorol yn cychwyn ${date}";

  static m41(termsOfService, privacyPolicy) => "Wrth daro ‘Creu Cyfrif’, rydych chi’n cytuno i’r ${termsOfService} ac yn cydnabod y ${privacyPolicy}";

  static m42(version) => "Awgrymiadau i Android - Canvas Parent ${version}";

  static m43(month) => "Mis ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} seren', other: '${position} seren')}";

  static m45(date, time) => "Cafodd yr aseiniad hwn ei gyflwyno ar ${date} am ${time} ac mae’n aros i gael ei raddio";

  static m46(studentName) => "Ynghylch: ${studentName}, Maes Llafur";

  static m47(count) => "${count} heb eu darllen";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("Bydd \"Gweithredu fel\" yn golygu eich bod yn mewngofnodi fel y defnyddiwr hwn heb gyfrinair. Byddwch chi’n gallu gwneud unrhyw beth fel petai chi yw’r defnyddiwr hwn. O safbwynt defnyddwyr eraill, bydd yn edrych fel mai’r defnyddiwr hwn sydd wedi gwneud hynny. Ond, bydd logiau archwilio’n cofnodi mai chi wnaeth hynny ar ran y defnyddiwr hwn."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Rhaid rhoi disgrifiad."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Gwall ar y rhwydwaith wrth ychwanegu\'r myfyriwr hwn. Gwiriwch eich cysylltiad a rhowch gynnig arall arni."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Rhaid rhoi pwnc."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Gweithredu fel Defnyddiwr"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Ychwanegu Myfyriwr"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Ychwanegu atodiad"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Ychwanegu myfyriwr newydd"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Ychwanegu myfyriwr gyda..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Gosodiadau Hysbysu"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Rhowch wybod i mi pan..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Pob Cyfnod Graddio"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Oes gennych chi gyfrif yn barod? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Rhaid rhoi cyfeiriad e-bost."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Gwall wrth geisio dangos y ddolen hon"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Gwall wrth gadw eich dewis. Rhowch gynnig arall arni."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Gwall annisgwyl"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Fersiwn OS Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Ymddangdosiad"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Fersin o’r rhaglen"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Ai myfyriwr neu athro ydych chi?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Ydych chi’n siŵr eich bod am allgofnodi?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Ydych chi’n siŵr eich bod chi eisiau cau’r dudalen hon? Od nad yw eich neges wedi’i anfon bydd yn cael ei golli."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Manylion Aseiniad"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Gradd yr aseiniad yn uwch na"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Gradd yr aseiniad yn is na"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Aseiniad ar goll"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, Ffiwsia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendrau"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Hawl Camera"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Canslo"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Myfyriwr Canvas"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas ar GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Dewiswch gwrs i anfon neges ato"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Dewis o’r Oriel"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Cwblhau"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Cysylltwch â\'r adran Gymorth"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Cyhoeddiad Cwrs"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Cyhoeddiadau Cwrs"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Gradd y cwrs yn uwch na"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Gradd y cwrs yn is na"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Creu Cyfrif"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Modd Tywyll"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dyddiad"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Dileu"),
    "Description" : MessageLookupByLibrary.simpleMessage("Disgrifiad"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dyfais"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Model o’r ddyfais"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Parth"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Parth:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Peidio â dangos eto"),
    "Done" : MessageLookupByLibrary.simpleMessage("Wedi gorffen"),
    "Download" : MessageLookupByLibrary.simpleMessage("Llwytho i Lawr"),
    "Due" : MessageLookupByLibrary.simpleMessage("Erbyn"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("ARGYFWNG - DIFRIFOL IAWN!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Trydan, Glas"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Cyfeiriad E-bost"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E-bost:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("E-bost..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Rhowch y cod paru myfyriwr a roddwyd i chi. Os nad yw’r cod paru yn gweithio, mae’n bosib ei fod wedi dod i ben"),
    "Event" : MessageLookupByLibrary.simpleMessage("Digwyddiad"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Wedi esgusodi"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Cod QR wedi dod i ben"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Wedi methu. Tapiwch i gael opsiynau."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Hidlo"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Hidlo yn ôl"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Tân, Oren"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Tudalen Flaen"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Enw Llawn"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Enw Llawn..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Neges gwall llawn"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Mynd i heddiw"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Gradd"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Canran gradd"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Wedi graddio"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Graddau"),
    "Help" : MessageLookupByLibrary.simpleMessage("Help"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Cuddio Cyfrinair"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modd â Chyferbyniad Uchel"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Sut hwyl rydyn ni’n ei gael arni?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Sut mae hyn yn effeithio arnoch chi?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Alla i ddim bwrw ymlaen nes bydda i wedi cael ateb gennych chi."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Does gen i ddim cyfrif Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Mae gen i gyfrif Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Mae angen help arna i, ond dydy’r mater ddim yn un brys."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Rwy’n cael trafferth yn mewngofnodi"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Syniad ar gyfer Ap Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Er mwyn rhoi profiad gwell i chi, rydyn ni wedi diweddaru sut mae nodiadau atgoffa’n gweithio. Gallwch chi ychwanegu nodiadau atgoffa newydd drwy edrych at aseiniad neu ddigwyddiad calendr a thapio’r switsh o dan yr adran \"Fy Atgoffa\".\n\nByddwch yn ymwybodol na fydd unrhyw nodiadau atgoffa sydd wedi cael eu creu gyda hen fersiynau o’r ap hwn yn gydnaws a’r newidiadau newydd a bydd angen i chi eu creu eto."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Blwch Derbyn"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Inbox Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Heb Gwblhau"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Parth Anghywir"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Cyhoeddiad Sefydliad"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Cyhoeddiadau Sefydliadau"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Cyfarwyddiadau"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Mae achosion o ryngweithio ar y dudalen hon wedi’u cyfyngu gan eich sefydliad."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Cod QR Annilys"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Mae’n edrych fel diwrnod gwych i orffwys, ymlacio a dod at eich hun."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Mae’n ymddangos nad oes aseiniadau wedi cael eu creu yn y gofod hwn eto."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Dim ond awgrym, syniad, sylw neu gwestiwn anffurfiol..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Yn Hwyr"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lansio Adnodd Allanol"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Cyfreithiol"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modd Golau"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Gwall Dolen"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Lleoliad:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Dod o hyd i God QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Lleoliad"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Wedi Cloi"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Allgofnodi"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Llif Mewngofnodi: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Llif Mewngofnodi: Arferol"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Llif Mewngofnodi: Gweinyddwr Safle"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Llif Mewngofnodi: Anwybyddu cadarnhau symudol"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Rheoli Myfyrwyr"),
    "Message" : MessageLookupByLibrary.simpleMessage("Neges"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Pwnc y neges"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Ar goll"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Rhaid bod is na 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Gwall ar y rhwydwaith"),
    "Never" : MessageLookupByLibrary.simpleMessage("Byth"),
    "New message" : MessageLookupByLibrary.simpleMessage("Neges newydd"),
    "No" : MessageLookupByLibrary.simpleMessage("Na"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Dim Negeseuon Hysbysu"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Dim Aseiniadau"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Dim Cyrsiau"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Dim Dyddiad Erbyn"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Dim Digwyddiadau Heddiw!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Dim Gradd"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Dim Lleoliad wedi’i Nodi"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Dim Myfyrwyr"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Dim Pwnc"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Dim Crynodeb"),
    "No description" : MessageLookupByLibrary.simpleMessage("Dim disgrifiad"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Dim derbynwyr wedi’u dewis"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Heb eu graddio"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Heb Gyflwyno"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Ddim yn rhiant?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Hysbysiadau ar gyfer nodiadau atgoffa am aseiniadau a digwyddiadau calendr"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Fersiwn OS"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Arsyllwr"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Efallai y byddai un o\'r apiau eraill yn fwy addas. Tapiwch un i fynd i\'r Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Agor Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Agor mewn Porwr"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Agor gydag ap arall"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Cod Paru"),
    "Password" : MessageLookupByLibrary.simpleMessage("Cyfrinair"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Rhaid rhoi cyfrinair"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Rhaid i gyfrinair gynnwys o leiaf 8 nod"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Cyfrinair..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Nodyn gan y Trefnydd"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Rhowch gyfeiriad e-bost dilys"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Rhowch gyfeiriad e-bost"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Rhowch enw llawn"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Sganiwch god QR sydd wedi cael ei greu gan Canvas."),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Eirinen, Porffor"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Wrthi’n paratoi..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Manylion Mewngofnodi Blaenorol"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Polisi Preifatrwydd"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Dolen y Polisi Preifatrwydd"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Polisi preifatrwydd, telerau defnyddio, ffynhonnell agored"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Cod QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Mae sganio QR angen mynediad at eich camera"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Mafonen, Coch"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Derbynwyr"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Adnewyddu"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Fy Atgoffa"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Nodiadau atgoffa"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Mae’r nodiadau atgoffa wedi newid!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Ateb"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Ateb Pawb"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Rhoi gwybod am broblem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Gofyn am Help i Fewngofnodi"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Botwm Gofyn am Help i Fewngofnodi"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Ailddechrau’r ap"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Ailgynnig"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Yn ôl i Fewngofnodi"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("MYFYRIWR"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Ciplun yn dangos y lleoliad creu cod QR yn y porwr."),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Ciplun yn dangos y lleoliad creu cod QR paru yn ap Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Dewiswch"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Dewis Lliw Myfyriwr"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Dewis derbynwyr"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Anfon Adborth"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Anfon neges am yr aseiniad hwn"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Anfon neges am y cwrs hwn"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Anfon neges"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Gosod dyddiad ac amser i gael eich atgoffa am y digwyddiad hwn."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Gosod dyddiad ac amser i gael eich atgoffa am yr aseiniad penodol hwn."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Gosodiadau"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Meillionen, Gwyrdd"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Rhowch eich barn am yr ap"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Dangos Cyfrinair"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Mewngofnodi"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Aeth rhywbeth o’i le wrth geisio creu eich cyfrif, gofynnwch i’ch ysgol am gymorth."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Mae rhywbeth wedi mynd o’i le, ond fe alla i ddal i wneud yr hyn rydw i angen ei wneud."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Stopio Gweithredu fel Defnyddiwr"),
    "Student" : MessageLookupByLibrary.simpleMessage("Myfyriwr"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Paru Myfyrwyr"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Mae myfyrwyr yn gallu creu cod QR drwy ddefnyddio app Canvas Student ar eu dyfais symudol"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Mae myfyrwyr yn gallu cael cod paru trwy wefan Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Pwnc"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Wedi Cyflwyno"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Wedi llwyddo i gyflwyno!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Crynodeb"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Newid Defnyddwyr"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Maes Llafur"),
    "TA" : MessageLookupByLibrary.simpleMessage("Cynorthwyydd Dysgu"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("ATHRO"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tapiwch i nodi fel ffefrynau’r cyrsiau rydych chi am eu gweld ar y Calendr. Dewiswch hyd at 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tapiwch i baru â myfyriwr newydd"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tapiwch i ddewis y myfyriwr hwn"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tapiwch i ddangos y dewisydd myfyrwyr"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Athro"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Rhowch wybod i ni am eich hoff rannau o’r ap"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Telerau Gwasanaeth"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Dolen y Telerau Gwasanaeth"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Telerau Defnyddio"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Efallai fod y cod QR y gwnaethoch chi ei sganio wedi dod i ben. Adnewyddwch y cod ar ddyfais y myfyriwr a rhowch gynnig arall arni."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Bydd y wybodaeth ganlynol yn ein helpu ni i ddeall eich syniad yn well:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Dydy’r gweinydd rydych chi wedi\'i roi ddim wedi’i awdurdodi ar gyfer yr ap hwn."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Mae’r myfyriwr rydych chi’n ceisio ei ychwanegu’n perthyn i ysgol arall. Rhaid i chi fewngofnodi neu greu cyfrif gyda’r ysgol honno i sganio’r cod hwn."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Dydy’r asiant defnyddiwr ar gyfer yr ap hwn ddim wedi’i awdurdodi."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Thema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Does dim rhaglen wedi’i gosod a all agor y ffeil hon"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Does dim gwybodaeth tudalen ar gael."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Problem wrth lwytho’r Telerau Defnyddio"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Roedd problem wrth dynnu’r myfyriwr hwn o’ch cyfrif. Gwiriwch eich cysylltiad a rhowch gynnig arall arni."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho derbynwyr ar gyfer y cwrs hwn"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho’r manylion cryno ar gyfer y cwrs hwn."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho\'r cyhoeddiad hwn"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho\'r sgwrs hon"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho\'r ffeil hon"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho’ch negeseuon blwch derbyn."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho hysbysiadau eich myfyriwr."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho calendr eich myfyriwr"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho’ch myfyrwyr."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Gwall wrth lwytho cyrsiau eich myfyriwr."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Gwall wrth fewngofnodi. Crëwch God QR arall a rhoi cynnig arall arni."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Gwall wrth geisio gweithredu fel y defnyddiwr hwn. Gwnewch yn sîwr bod y Parth a’r ID Defnyddiwr yn iawn a rhoi cynnig arall arni."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Does dim i’w hysbysu eto."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Dydi’r ap hwn ddim wedi’i awdurdodi i’w ddefnyddio."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Does gan y cwrs hwn ddim aseiniadau neu ddigwyddiadau calendr eto."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Mae’r ffeil hon yn anghydnaws ac nid oes modd ei gweld drwy’r ap"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Bydd hyn yn dadbaru ac yn tynnu’r holl ymrestriadau ar gyfer y myfyriwr hwn o’ch cyfrif."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Gradd Gyffredinol"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("O na!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Doedd dim modd nôl cyrsiau. Gwiriwch eich cysylltiad a rhoi cynnig arall arni."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Doedd dim modd llwytho’r ddelwedd hon"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Doedd dim modd chwarae’r ffeil gyfryngau"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Doedd dim modd anfon y neges. Gwiriwch eich cysylltiad a rhoi cynnig arall arni."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Wrthi’n cael ei greu"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Defnyddiwr Dieithr"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Newidiadau heb eu cadw"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Ffeil Anghydnaws"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Llwytho Ffeil i Fyny"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Defnyddio Camera"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Defnyddio Thema Dywyll mewn Cynnwys Gwe"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID Defnyddiwr"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID Defnyddiwr:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Rhif Fersiwn"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Gweld Disgrifiad"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Gweld manylion gwall"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Gweld y Polisi Preifatrwydd"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Rydyn ni’n adeiladu’r nodwedd hon ar hyn o bryd er mwyn i chi allu gweld."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Does dim modd i ni ddangos y ddolen hon, efallai ei bod hi’n perthyn i sefydliad nad ydych chi wedi mewngofnodi iddo ar hyn o bryd."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Doedd dim modd dod o hyd i unrhyw fyfyrwyr sy’n gysylltiedig â’r cyfrif hwn"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Doedd dim modd dilysu’r gweinydd i’w ddefnyddio gyda’r ap hwn."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Dydyn ni ddim yn siŵr beth ddigwyddodd, ond doedd o ddim yn dd. Cysylltwch â ni os ydy hyn yn parhau i ddigwydd."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Beth fydden ni’n gallu ei wneud yn well?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Iawn"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Dydych chi ddim yn arsyllu unrhyw fyfyrwyr."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Dim ond 10 calendr y cewch chi eu dewis i’w dangos"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Mae’n rhaid i chi roi ID defnyddiwr"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Mae’n rhaid i chi roi parth dilys"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Mae’n rhaid i chi ddewis o leiaf un calendr i\'w ddangos"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Byddwch chi’n cael eich hysbysu am yr aseiniad hwn ar..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Byddwch chi’n cael eich hysbysu am y digwyddiad hwn ar..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Fe ddewch chi o hyd i’r cod QR ar y we yn eich proffil cyfrif. Cliciwch \'QR ar gyfer Mewngofnodi Symudol\' yn y rhestr."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Bydd angen i chi agor app Canvas Student eich myfyriwr i barhau. Ewch i\'r Brif Ddewislen > Gosodiadau > Paru ag Arsyllwr a sganiwch y cod QR rydych chi’n ei weld yno."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Mae eich cod yn anghywir neu mae wedi dod i ben."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Efallai nad yw cyrsiau eich myfyriwr wedi cael eu cyhoeddi eto."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Rydych chi wedi dal i fyny!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Negeseuon Hysbysu"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendr"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canllawiau Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Cymorth Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("crebachu"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("wedi crebachu"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cyrsiau"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Sut ydw i’n dod o hyd i fy ysgol neu ardal?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Rhowch ardal neu enw’r ysgol..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("ehangu"),
    "expanded" : MessageLookupByLibrary.simpleMessage("wedi ehangu"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Dod o hyd i Ysgol"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("fi"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minws"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Nesaf"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("Iawn"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("anfon"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("heb eu darllen"),
    "unreadCount" : m47
  };
}
