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

  static m0(version) => "útg. ${version}";

  static m1(threshold) => "Verkefnaeinkunn fyrir ofan ${threshold}";

  static m2(threshold) => "Verkefnaeinkunn fyrir neðan ${threshold}";

  static m3(moduleName) => "Verkefninu er læst af einingu \"${moduleName}\".";

  static m4(studentName, assignmentName) => "Varðandi: ${studentName}, Verkefni - ${assignmentName}";

  static m5(points) => "${points} punktar";

  static m6(points) => "${points} punktar";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} til 1 annars', other: '${authorName} til ${howMany} annarra')}";

  static m8(authorName, recipientName) => "${authorName} til ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} til ${recipientName} og 1 annars', other: '${authorName} til ${recipientName} og ${howMany} annarra')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} af ${pointsPossible} stigum";

  static m12(studentShortName) => "fyrir ${studentShortName}";

  static m13(threshold) => "Námskeiðseinkunn fyrir ofan ${threshold}";

  static m14(threshold) => "Námskeiðseinkunn fyrir neðan ${threshold}";

  static m15(date, time) => "${date} klukkan ${time}";

  static m16(canvasGuides, canvasSupport) => "Prufaðu að leita að nafni skólans eða umdæmisins sem þú reyndir að opna, eins og „Smith Private School“ eða „Smith County Schools.“ Þú getur einnig sett inn Canvas-lén beint, eins og “smith.instructure.com.”\n\nFyrir frekari upplýsingar um hvernig þú finnur Canvas reikning stofnunar þinnar, geturðu farið á ${canvasGuides}, haft samband við ${canvasSupport}, eða hafðu samband við skólann þinn til að fá aðstoð.";

  static m17(date, time) => "Skiladagur ${date} þann ${time}";

  static m18(studentName, eventTitle) => "Varðandi: ${studentName}, Viðburður - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Lokaeinkunn: ${grade}";

  static m21(studentName) => "Varðandi: ${studentName}, Forsíða";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Varðandi: ${studentName}, Einkunnir";

  static m24(pointsLost) => "Viðurlög vegna of seinna skila (-${pointsLost})";

  static m25(studentName, linkUrl) => "Varðandi: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Verður að vera yfir ${percentage}";

  static m27(percentage) => "Verður að vera undir ${percentage}";

  static m28(month) => "Næsti mánuður: ${month}";

  static m29(date) => "Næsta vika hefst ${date}";

  static m30(query) => "Ekki tókst að finna skóla sem pössuðu við \"${query}\"";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Af 1 stigi', other: 'Af ${points} stigum')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} punktar mögulegir";

  static m34(month) => "Fyrri mánuður: ${month}";

  static m35(date) => "Fyrri vika hefst ${date}";

  static m36(month) => "${month} mánuði";

  static m37(date, time) => "Þetta verkefni var sent inn þann ${date} klukkan ${time} og bíður einkunnar";

  static m38(studentName) => "Varðandi: ${studentName}, Kennsluáætlun";

  static m39(count) => "${count} ólesið";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Lýsingar er krafist."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Efni er áskilið."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Bæta við nemanda"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Bæta við viðhengi"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Bæta við nýjum nemanda"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Bæta nemanda við…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Viðvörunarstillingar"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Vara mig við þegar…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Öll einkunnatímabil"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Netfang er áskilið."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sýna þennan tengil"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("Dagatöl"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Dökk stilling"),
    "Date" : MessageLookupByLibrary.simpleMessage("Dagsetning"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Eyða"),
    "Description" : MessageLookupByLibrary.simpleMessage("Lýsing"),
    "Device" : MessageLookupByLibrary.simpleMessage("Tæki"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Gerð tækis"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Lén:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Lokið"),
    "Download" : MessageLookupByLibrary.simpleMessage("Hlaða niður"),
    "Due" : MessageLookupByLibrary.simpleMessage("Skilafrestur"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("GÍFURLEGT NEYÐARÁSTAND!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Netfang"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Netfang:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Settu inn pörunarkóða nemanda sem þú fékkst. Ef pörunarkóðinn virkar ekki gæti hann verið útrunninn"),
    "Event" : MessageLookupByLibrary.simpleMessage("Viðburður"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Undanþegið"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Tókst ekki. Smelltu fyrir valkosti."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Sía"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Sía eftir"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Forsíða"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Heildar villuskilaboð"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Einkunn"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Einkunnahlutfall"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Metið"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Einkunnir"),
    "Help" : MessageLookupByLibrary.simpleMessage("Hjálp"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Háskerpu stilling"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Hvaða áhrif hefur þetta á þig?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Ég get ekkert gert fyrr en þú svarar mér."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Ég þarf smá aðstoð en það liggur ekki á."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Ég á í vandræðum við að skrá mig inn"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Hugmynd að Canvas foreldraappi [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Innhólf"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Innhólf Núll"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Ólokið"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Stofnana tilkynning"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Stofnana tilkynningar"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Fyrirmæli"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Þetta virðist vera góður dagur til að hvílast, slaka á og hlaða batteríin."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Það virðast ekki vera nein verkefni búin til í þessu rými enn."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Bara almenn spurning, athugasemd, hugmynd, tillaga…"),
    "Late" : MessageLookupByLibrary.simpleMessage("Seint"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Nota ytra verkfæri"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Lögfræði"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Ljós stilling"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Tengils villa"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Staður:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Staðsetning"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Læst"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Útskráning"),
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
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Opna í vafra"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Opna með öðru appi"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Pörunarkóði"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Undirbý…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Fyrri innskráningar"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Persónuverndarstefna"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Persónuverndarstefna, notkunarskilmálar, opinn hugbúnaður"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR kóði"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Viðtakendur"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Minntu mig á"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Áminningar"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Svara"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Svara öllum"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Tilkynna vandamál"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Biðja um aðstoð við innskráningu"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Biðja um aðstoð við innskráningu hnappur"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Endurræstu forritið"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Reyna aftur"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Fara aftur í innskráningu"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("NEMANDI"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Velja móttakendur"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Sendu skilaboð um þetta verkefni"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Sendu skilaboð um þetta námskeið"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Senda skilaboð"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Stilltu inn dagsetningu og tíma til að fá áminningu um þennan viðburð."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Stilltu inn dagsetningu og tíma til að fá áminningu um þetta tiltekna verkefni."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Stilla inn áminningarrofa"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Stillingar"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Deildu ást þinni á appinu"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Eitthvað er í ólagi en ég get samt unnið í kringum það til að ljúka því sem ég þarf að ljúka."),
    "Student" : MessageLookupByLibrary.simpleMessage("Nemandi"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Efni"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Skilað"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Skil tókust!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Samantekt"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Skipta um notendur"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Kennsluáætlun"),
    "TA" : MessageLookupByLibrary.simpleMessage("Aðstoðarkennari"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("KENNARI"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Smelltu til að setja námskeiðin sem þú vilt sjá á dagatalinu sem eftirlæti."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Smelltu til að para við nýjan nemanda"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Smelltu til að velja þennan nemanda"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Smelltu til að sýna nemendaval"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Kennari"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Segðu okkur um uppáhalds hluta þína í appinu"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Notandaskilmálar"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Eftirfarandi upplýsingar munu hjálpa okkur að skilja hugmynd þína betur:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Miðillinn sem þú settir inn er ekki heimill fyrir þetta app."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Fulltrúi notanda fyrir þetta app er óheimill."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Þema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Það eru engin uppsett öpp sem geta opnað þessar skrár"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Það eru engar síðuupplýsingar tiltækar."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja notkunarskilmála"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja móttakendur fyrir þetta námskeið"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja yfirlitsnámskeið fyrir þetta námskeið."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja þessa tilkynningu"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja þessar samræður"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Villa varð við að sækja þessa skrá"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja skilaboðin þín."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja viðvaranir nemanda þíns."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja dagatal nemanda"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja nemendur þína."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Villa kom upp við að sækja námskeið nemanda þíns."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Það er ekkert tilkynningavert enn."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Þetta app er ekki heimilt til notkunar."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Þetta námskeið er ekki með nein verkefni eða dagatalsviðburði enn."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Þessi skrá er óstudd og er ekki hægt að skoða í þessu appi"),
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
    "User ID:" : MessageLookupByLibrary.simpleMessage("Auðkenni notanda:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Útgáfa númer"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Skoða upplýsingar um villu"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Núna byggjum við þennan eiginleika þér til hægðarauka."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Við getum ekki sýnt þennan tengil, hann gæti tilheyrt stofnun sem þú ert ekki innskráð(ur) hjá."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Við fundum enga nemendur sem tengjast þessum reikningi"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Við gátum ekki sannreynt miðilinn fyrir notkun með þessu appi."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Við erum ekki viss um hvað gerðist, en það var ekki gott. Hafðu samband við okkur ef þetta heldur áfram að gerast."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Já"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Þú fylgist ekki með neinum nemendum."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Þú færð áminningu um þetta verkefni þann…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Þú færð áminningu um þennan viðburð þann…"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Kóðinn þinn er rangur eða útrunninn."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Námskeið nemanda þíns eru ef til vill ekki birt enn."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Þú ert búin(n) með allt!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Viðvaranir"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Dagatal"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas leiðarvísar"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas myndmerki"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas stuðningur"),
    "collapse" : MessageLookupByLibrary.simpleMessage("fella saman"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("fellt saman"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Námskeið"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("vísa frá"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Hvernig finn ég skólann minn eða umdæmið?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Settu inn nafn skóla eða umdæmis…"),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("víkka"),
    "expanded" : MessageLookupByLibrary.simpleMessage("víkkað"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Finna skóla"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("ég"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("mínus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Næsti"),
    "nextMonth" : m28,
    "nextWeek" : m29,
    "noDomainResults" : m30,
    "ok" : MessageLookupByLibrary.simpleMessage("Í lagi"),
    "outOfPoints" : m31,
    "plusRecipientCount" : m32,
    "pointsPossible" : m33,
    "previousMonth" : m34,
    "previousWeek" : m35,
    "selectedMonthLabel" : m36,
    "send" : MessageLookupByLibrary.simpleMessage("sendi"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("ólesið"),
    "unreadCount" : m39
  };
}
