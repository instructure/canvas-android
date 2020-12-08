// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a is locale. All the
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
  String get localeName => 'is';

  static m0(userName) => "Þú virkar sem ${userName}";

  static m1(version) => "útg. ${version}";

  static m2(threshold) => "Verkefnaeinkunn fyrir ofan ${threshold}";

  static m3(threshold) => "Verkefnaeinkunn fyrir neðan ${threshold}";

  static m4(moduleName) => "Verkefninu er læst af einingu \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Varðandi: ${studentName}, Verkefni - ${assignmentName}";

  static m6(points) => "${points} punktar";

  static m7(points) => "${points} punktar";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} til 1 annars', other: '${authorName} til ${howMany} annarra')}";

  static m9(authorName, recipientName) => "${authorName} til ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} til ${recipientName} og 1 annars', other: '${authorName} til ${recipientName} og ${howMany} annarra')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Breyta lit fyrir ${studentName}";

  static m13(score, pointsPossible) => "${score} af ${pointsPossible} stigum";

  static m14(studentShortName) => "fyrir ${studentShortName}";

  static m15(threshold) => "Námskeiðseinkunn fyrir ofan ${threshold}";

  static m16(threshold) => "Námskeiðseinkunn fyrir neðan ${threshold}";

  static m17(date, time) => "${date} klukkan ${time}";

  static m18(alertTitle) => "Hafna ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Prufaðu að leita að nafni skólans eða umdæmisins sem þú reyndir að opna, eins og „Smith Private School“ eða „Smith County Schools.“ Þú getur einnig sett inn Canvas-lén beint, eins og “smith.instructure.com.”\n\nFyrir frekari upplýsingar um hvernig þú finnur Canvas reikning stofnunar þinnar, geturðu farið á ${canvasGuides}, haft samband við ${canvasSupport}, eða hafðu samband við skólann þinn til að fá aðstoð.";

  static m20(date, time) => "Skiladagur ${date} þann ${time}";

  static m21(userName) => "Þú hættir að bregðast við sem ${userName} og verður skráð(ur) út.";

  static m22(userName) => "Þú hættir að bregðast við sem ${userName} og ferð aftur í upprunalegan reikning þinn.";

  static m23(studentName, eventTitle) => "Varðandi: ${studentName}, Viðburður - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Lokaeinkunn: ${grade}";

  static m26(studentName) => "Varðandi: ${studentName}, Forsíða";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Varðandi: ${studentName}, Einkunnir";

  static m29(pointsLost) => "Viðurlög vegna of seinna skila (-${pointsLost})";

  static m30(studentName, linkUrl) => "Varðandi: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Verður að vera yfir ${percentage}";

  static m32(percentage) => "Verður að vera undir ${percentage}";

  static m33(month) => "Næsti mánuður: ${month}";

  static m34(date) => "Næsta vika hefst ${date}";

  static m35(query) => "Ekki tókst að finna skóla sem pössuðu við \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Af 1 stigi', other: 'Af ${points} stigum')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} punktar mögulegir";

  static m39(month) => "Fyrri mánuður: ${month}";

  static m40(date) => "Fyrri vika hefst ${date}";

  static m41(termsOfService, privacyPolicy) => "Með því að pikka á „Búa til reikning“ samþykkir þú ${termsOfService} og staðfestir ${privacyPolicy}";

  static m42(version) => "Uppástungur fyrir Android - Canvas Parent ${version}";

  static m43(month) => "${month} mánuði";

  static m44(position) => "${Intl.plural(position, one: '${position} stjarna', other: '${position} stjörnur')}";

  static m45(date, time) => "Þetta verkefni var sent inn þann ${date} klukkan ${time} og bíður einkunnar";

  static m46(studentName) => "Varðandi: ${studentName}, Kennsluáætlun";

  static m47(count) => "${count} ólesið";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Bregðast við sem\" er í raun og veru að skrá sig inn sem þessi notandi án lykilorðs. Þú munt getað gripið til allra aðgerða eins og að þú værir viðkomandi notandi, frá sjónarhóli annarra notenda, þá mun það vera eins og þessi notandi hafi framkvæmt viðkomandi aðgerðir. Samt sem áður sýna endurskoðaðar skrár að það varst þú sem framkvæmdir aðgerðirnar fyrir hönd þessa notanda."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Lýsingar er krafist."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Netvilla kom upp þegar verið var að bæta þessum nemanda við. Athugaðu tengingu þína og reyndu aftur."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Efni er áskilið."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Bregðast við sem notandi"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Bæta við nemanda"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Bæta við viðhengi"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Bæta við nýjum nemanda"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Bæta nemanda við…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Viðvörunarstillingar"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Vara mig við þegar…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Öll einkunnatímabil"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Áttu nú þegar reikning? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Netfang er áskilið."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sýna þennan tengil"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að vista valið þitt. Vinsamlegast reyndu aftur."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Óvænt villa kom upp"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Útgáfa Android stýrikerfis"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Útlit"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Útgáfa forrits"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Ertu nemandi eða kennari?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Viltu örugglega skrá þig út?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Viltu örugglega loka þessari síðu? Ósend skilaboð tapast."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Upplýsingar um verkefni"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Verkefnaeinkunn fyrir ofan"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Verkefnaeinkunn fyrir neðan"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Verkefni vantar"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, purpuralitur"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Dagatöl"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Heimild fyrir myndavél"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Hætta við"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas nemandi"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas kennari"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas á GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Veldu námskeið til að senda skilaboð til"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Velja úr galleríi"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Lokið"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Hafa samband við aðstoð"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Tilkynning námskeiðs"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Tilkynningar námskeiðs"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Námskeiðseinkunn fyrir ofan"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Námskeiðseinkunn fyrir neðan"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Stofna reikning"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Dökk stilling"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dagsetning"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Eyða"),
    "Description" : MessageLookupByLibrary.simpleMessage("Lýsing"),
    "Device" : MessageLookupByLibrary.simpleMessage("Tæki"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Gerð tækis"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Lén"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Lén:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Ekki sýna aftur"),
    "Done" : MessageLookupByLibrary.simpleMessage("Lokið"),
    "Download" : MessageLookupByLibrary.simpleMessage("Hlaða niður"),
    "Due" : MessageLookupByLibrary.simpleMessage("Skilafrestur"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("GÍFURLEGT NEYÐARÁSTAND!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Rafmagn, blár"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Netfang"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Netfang:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Netfang..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Settu inn pörunarkóða nemanda sem þú fékkst. Ef pörunarkóðinn virkar ekki gæti hann verið útrunninn"),
    "Event" : MessageLookupByLibrary.simpleMessage("Viðburður"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Undanþegið"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Útrunninn QR-kóði"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Tókst ekki. Smelltu fyrir valkosti."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Sía"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Sía eftir"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Eldur, appelsínugulur"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Forsíða"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Fullt nafn"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Fullt nafn..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Heildar villuskilaboð"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Fara í daginn í dag"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Einkunn"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Einkunnahlutfall"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Metið"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Einkunnir"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjálp"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Fela lykilorð"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Háskerpu stilling"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Hvernig gengur okkur?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hvaða áhrif hefur þetta á þig?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Ég get ekkert gert fyrr en þú svarar mér."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Ég er ekki með Canvas-reikning"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Ég er með Canvas-reikning"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Ég þarf smá aðstoð en það liggur ekki á."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Ég á í vandræðum við að skrá mig inn"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Hugmynd að Canvas foreldraappi [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Til þess að veita þér betri upplifun höfum við breytt því hvernig áminningar virka. Þú getur bætt nýjum áminningum við með því að skoða verkefni eða dagatalsviðburð og smella á rofann undir \"minna mig á\" hlutanum.\n\nHafðu í huga að hverskonar áminning sem sem var stofnuð með gömlum útgáfum þessa forrits verða ekki samhæfðar við nýju breytingarnar og þú þarft að stofna þær aftur."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Innhólf"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Innhólf Núll"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ólokið"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Rangt lén"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Stofnana tilkynning"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Stofnana tilkynningar"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Fyrirmæli"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Samskipti á þessari síðu eru takmörkuð af stofnun þinni."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Ógildur QR-kóði"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Þetta virðist vera góður dagur til að hvílast, slaka á og hlaða batteríin."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Það virðast ekki vera nein verkefni búin til í þessu rými enn."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Bara almenn spurning, athugasemd, hugmynd, tillaga…"),
    "Late" : MessageLookupByLibrary.simpleMessage("Seint"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Nota ytra verkfæri"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Lögfræði"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Ljós stilling"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Tengils villa"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Staður:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Finna QR kóða"),
    "Location" : MessageLookupByLibrary.simpleMessage("Staðsetning"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Læst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Útskráning"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Innskráningarflæði: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Innskráningarflæði: Venjulegt"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Innskráningarflæði: Svæðisstjórnandi"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Innskráningarflæði: Sleppa sannreyningu farsíma"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Stjórna nemendum"),
    "Message" : MessageLookupByLibrary.simpleMessage("Skilaboð"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Efni skilaboða"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Vantar"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Verður að vera undir 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Netkerfisvilla"),
    "Never" : MessageLookupByLibrary.simpleMessage("Aldrei"),
    "New message" : MessageLookupByLibrary.simpleMessage("Ný skilaboð"),
    "No" : MessageLookupByLibrary.simpleMessage("Nei"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Engar viðvaranir"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Engin verkefni"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Engin námskeið"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Enginn skiladagur"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Engir viðburðir í dag!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Engin einkunn"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Engin staðsetning tiltekin"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Engir nemendur"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Ekkert efnisheiti"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Engin samantekt"),
    "No description" : MessageLookupByLibrary.simpleMessage("Engin lýsing"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Engir móttakendur valdir"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Ekki metið"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Ekki lagt fram"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Ekki foreldri?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Tilkynningar fyrir áminningar um verkefni og dagatalsviðburði"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Útgáfa stýrikerfis"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Skoðandi"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Eitthvað að hinum öppunum okkar gæti hentað betur. Smelltu á eitt þeirra til að fara í Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Opnaðu Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Opna í vafra"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Opna með öðru appi"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Pörunarkóði"),
    "Password" : MessageLookupByLibrary.simpleMessage("Lykilorð"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Lykilorðs er krafist"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Lykilorðið þarf að hafa a.m.k. 8 stafi"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Lykilorð..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Minnismiði skipuleggjara"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Settu inn gilt netfang"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Settu inn netfang"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Settu inn fullt nafn"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Skannaðu QR kóða sem Canvas bjó til"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Plóma, fjólublár"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Undirbý…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Fyrri innskráningar"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Persónuverndarstefna"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Tengill á persónuverndarstefnu"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Persónuverndarstefna, notkunarskilmálar, opinn hugbúnaður"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR kóði"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Til að skanna QR-kóða þarf aðgang að myndavél"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Hindber, rauður"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Viðtakendur"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Glæða"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Minntu mig á"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Áminningar"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Áminningar hafa breyst!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Svara"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Svara öllum"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Tilkynna vandamál"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Biðja um aðstoð við innskráningu"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Biðja um aðstoð við innskráningu hnappur"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Endurræstu forritið"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Reyna aftur"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Fara aftur í innskráningu"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("NEMANDI"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Skjáskot sem sýnir hvar QR kóði er myndaður í vafra"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Skjáskot sem sýnir hvar hægt er að framkalla pörunar QR-kóða í Canvas Student forritinu"),
    "Select" : MessageLookupByLibrary.simpleMessage("Velja"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Veldu lit nemanda"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Velja móttakendur"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Senda endurgjöf"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Sendu skilaboð um þetta verkefni"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Sendu skilaboð um þetta námskeið"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Senda skilaboð"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Stilltu inn dagsetningu og tíma til að fá áminningu um þennan viðburð."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Stilltu inn dagsetningu og tíma til að fá áminningu um þetta tiltekna verkefni."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Stillingar"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Músasmári, grænn"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Deildu ást þinni á appinu"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Sýna lykilorð"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Innskráning"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Eitthvað fór úrskeiðis þegar verið var að búa til reikninginn þinn, hafðu samband við skólann þinn til að fá aðstoð."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Eitthvað er í ólagi en ég get samt unnið í kringum það til að ljúka því sem ég þarf að ljúka."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Hætta að bregðast við sem notandi"),
    "Student" : MessageLookupByLibrary.simpleMessage("Nemandi"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Pörun nemenda"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Nemendur geta búið til QR-kóða með því að nota Canvas Student forritið á fartæki sínu"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Nemendur geta fengið pörunarkóða í gegnum vefsíðu Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Efni"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Skilað"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Skil tókust!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Samantekt"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Skipta um notendur"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Kennsluáætlun"),
    "TA" : MessageLookupByLibrary.simpleMessage("Aðstoðarkennari"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("KENNARI"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Smelltu til að setja námskeiðin sem þú vilt sjá á dagatalinu sem eftirlæti. Veldu allt að tíu."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Smelltu til að para við nýjan nemanda"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Smelltu til að velja þennan nemanda"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Smelltu til að sýna nemendaval"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Kennari"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Segðu okkur um uppáhalds hluta þína í appinu"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Þjónustuskilmálar"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Tengill á þjónustuskilmála"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Notandaskilmálar"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("QR-kóðinn sem þú skannaðir gæti verið útrunninn. Endurnýjaðu kóðann á tæki nemandans og reyndu aftur."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Eftirfarandi upplýsingar munu hjálpa okkur að skilja hugmynd þína betur:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Miðillinn sem þú settir inn er ekki heimill fyrir þetta app."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Nemandinn sem þú ert að reyna að bæta við tilheyrir öðrum skóla. Skráðu þig inn eða búðu til reikning með þeim skóla til að skanna þennan kóða."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Fulltrúi notanda fyrir þetta app er óheimill."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Þema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Það eru engin uppsett öpp sem geta opnað þessar skrár"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Það eru engar síðuupplýsingar tiltækar."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja notkunarskilmála"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Upp kom vandamál við að fjarlægja þennan nemanda af reikningnum þínum. Athugaðu tengingu þína og reyndu aftur."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja móttakendur fyrir þetta námskeið"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja yfirlitsnámskeið fyrir þetta námskeið."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja þessa tilkynningu"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja þessar samræður"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja þessa skrá"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja skilaboðin þín."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja viðvaranir nemanda þíns."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja dagatal nemanda"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja nemendur þína."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja námskeið nemanda."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Villa kom upp við innskráningu. Búðu til annan QR kóða og reyndu aftur."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að bregðast við sem þessi notandi. Athugaðu lén og auðkenni notanda og reyndu aftur."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Það er ekkert tilkynningavert enn."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Þetta app er ekki heimilt til notkunar."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Þetta námskeið er ekki með nein verkefni eða dagatalsviðburði enn."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Þessi skrá er óstudd og er ekki hægt að skoða í þessu appi"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Þetta mun aftengja og fjarlægja allar skráningar fyrir þennan nemanda af reikningnum þínum."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Heildareinkunn"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Æi!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Ekki tókst að ná í námskeið. Athugaðu tengingu þína og reyndu aftur."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Ekki hægt að sækja þessa mynd"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Ekki er hægt að spila þessa miðlaskrá"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("ekki var hægt að senda skilaboð. Athugaðu tengingu þína og reyndu aftur."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Í byggingu"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Óþekktur notandi"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Óvistaðar breytingar"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Óstudd skrá"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Hlaða upp skrá"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Nota myndavél"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Nota dökkt þema í vefefni"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Auðkenni notanda"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Auðkenni notanda:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Útgáfa númer"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Skoða lýsingu"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Skoða upplýsingar um villu"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Skoða persónuverndarstefnuna"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Núna byggjum við þennan eiginleika þér til hægðarauka."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Við getum ekki sýnt þennan tengil, hann gæti tilheyrt stofnun sem þú ert ekki innskráð(ur) hjá."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Við fundum enga nemendur sem tengjast þessum reikningi"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Við gátum ekki sannreynt miðilinn fyrir notkun með þessu appi."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Við erum ekki viss um hvað gerðist, en það var ekki gott. Hafðu samband við okkur ef þetta heldur áfram að gerast."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Hvernig getum við bætt okkur?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Já"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Þú fylgist ekki með neinum nemendum."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Aðeins er hægt að velja tíu dagatöl til að sýna"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Þú verður að setja inn auðkenni notanda"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Þú verður að setja inn rétt lén"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Velja þarf að minnsta kosti eitt dagatal til að sýna"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Þú færð áminningu um þetta verkefni þann…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Þú færð áminningu um þennan viðburð þann…"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Þú finnur QR kóðann á vefnum á reikningssíðu þinni. Smelltu á ‘QR fyrir innskráningu með snjalltæki‘ í listanum."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Þú þarft að opna Canvas Student forrit nemanda þíns til að halda áfram. Farðu í aðalvalmyndina > Stillingar > Para við eftirlitsaðila og skannaðu QR-kóðann sem þú sérð þar."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Kóðinn þinn er rangur eða útrunninn."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Námskeið nemanda þíns eru ef til vill ekki birt enn."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Þú ert búin(n) með allt!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Viðvaranir"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Dagatal"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas leiðarvísar"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas myndmerki"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas stuðningur"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("fella saman"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("fellt saman"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Námskeið"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hvernig finn ég skólann minn eða umdæmið?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Settu inn nafn skóla eða umdæmis…"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("víkka"),
    "expanded" : MessageLookupByLibrary.simpleMessage("víkkað"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Finna skóla"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("ég"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("mínus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Næsti"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("Í lagi"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("sendi"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("ólesið"),
    "unreadCount" : m47
  };
}
