// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a fi locale. All the
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
  String get localeName => 'fi';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Tehtävän arvosana yllä ${threshold}";

  static m2(threshold) => "Tehtävän arvosana alla ${threshold}";

  static m3(moduleName) => "Tehtävä on lukittu moduulilla \"${moduleName}\".";

  static m4(studentName, assignmentName) => "Koskee: ${studentName}, Tehtävä - ${assignmentName}";

  static m5(points) => "${points} pistettä";

  static m6(points) => "${points} pistettä";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} 1 muu', other: '${authorName} ${howMany} muuhun')}";

  static m8(authorName, recipientName) => "${authorName} - ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} ${recipientName} ja 1 muu', other: '${authorName} ${recipientName} ja ${howMany} muuta')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score}/${pointsPossible} pistettä";

  static m12(studentShortName) => "${studentShortName}";

  static m13(threshold) => "Kurssin arvosana yllä ${threshold}";

  static m14(threshold) => "Kurssin arvosana alla ${threshold}";

  static m15(date, time) => "${date} kohteessa ${time}";

  static m16(canvasGuides, canvasSupport) => "Yritä etsiä sen koulun tai alueen nimeä, jolle yrität mennä, kuten “Smithin yksityiskoulu” tai “Smithin kunnan koulut”. Voit myös syöttää Canvasin verkko-osoitteen suoraan, kuten “smith.instructure.com”.\n\nLisätietoja laitoksesi Canvas-tilin etsimisestä löytyy seuraavasta ${canvasGuides}, ota yhteyttä ${canvasSupport}, tai pyydä apua koulustasi.";

  static m17(date, time) => "Määräpäivä ${date} kohteessa ${time}";

  static m18(studentName, eventTitle) => "Koskee: ${studentName}, Tapahtuma - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Lopullinen arvosana: ${grade}";

  static m21(studentName) => "Koskee: ${studentName}, Etusivu";

  static m22(score, pointsPossible) => "${score}/${pointsPossible}";

  static m23(studentName) => "Koskee: ${studentName}, Arvosanat";

  static m24(pointsLost) => "Rangaistus myöhästymisestä (-${pointsLost})";

  static m25(studentName, linkUrl) => "Koskee: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Täytyy olla yli ${percentage}";

  static m27(percentage) => "Täytyy olla alle ${percentage}";

  static m28(month) => "Seuraava kuukausi: ${month}";

  static m29(date) => "Seuraava viikko alkaen ${date}";

  static m30(query) => "Ei löydy kouluja, jotka täsmäävät haun \"${query}\" kanssa";

  static m31(points, howMany) => "${Intl.plural(howMany, one: '/1 piste', other: '/${points} pistettä')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} pistettä mahdollista";

  static m34(month) => "Edellinen kuukausi: ${month}";

  static m35(date) => "Edellinen viikko alkaen ${date}";

  static m36(month) => "Kuukausi ${month}";

  static m37(date, time) => "Tämä tehtävä lähetettiin ${date} ${time} ja odottaa arvosanan antoa";

  static m38(studentName) => "Koskee: ${studentName}, Opinto-ohjelma";

  static m39(count) => "${count} lukematon";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Kuvaus vaaditaan."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Vaaditaan aihe."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Lisää opiskelija"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Lisää liite"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Lisää uusi opiskelija"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Lisää opiskelija, jolla on..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Hälytykse asetukset"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Hälytä minua, kun..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Kaikki arvosanojen antojaksot"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Sähköpostiosoite vaaditaan."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Ilmeni virhe yritettäessä näyttää tätä linkkiä."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Ilmeni odottamaton virhe"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android-käyttöjärjestelmän versio"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Ulkoasu"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Sovelluksen versio"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Oletko opiskelija vai opettaja?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Haluatko varmasti kirjautua ulos?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Haluatko varmasti sulkea tämän sivun? Lähettämätön viestisi menetetään."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Tehtävän tiedot"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Tehtävän arvosana yllä"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Tehtävän arvosana alla"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Tehtävä puuttuu"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalenterit"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Peruuta"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas-opiskelija"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas-opettaja"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas GitHubissa"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Valitse kurssi, jolle lähetetään viesti"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Valitse galleriasta"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Valmis"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Ota yhteyttä tukeen"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Kurssin ilmoitus"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Kurssin ilmoitukset"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Kurssin arvosana yläpuolella"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Kurssin arvosana alapuolella"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Tumma tila"),
    "Date" : MessageLookupByLibrary.simpleMessage("Päivämäärä"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Poista"),
    "Description" : MessageLookupByLibrary.simpleMessage("Kuvaus"),
    "Device" : MessageLookupByLibrary.simpleMessage("Laite"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Laitteen malli"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Verkkotunnus:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Valmis"),
    "Download" : MessageLookupByLibrary.simpleMessage("Lataa"),
    "Due" : MessageLookupByLibrary.simpleMessage("Määräpäivä"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("ERITTÄIN KIIREELLINEN HÄTÄTAPAUS!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Sähköpostiosoite"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Sähköposti:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Kirjoita opiskelijan parinmuodostuskoodi, joka on toimitettu sinulle. Jos parinmuodostuskoodi ei toimi, se saattaa olla vanhentunut"),
    "Event" : MessageLookupByLibrary.simpleMessage("Tapahtuma"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Annettu anteeksi"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Epäonnistui. Asetukset napauttamalla."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Suodatin"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Suodatusperuste"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Etusivu"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Koko virhesanoma"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Arvosana"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Arvosanan prosenttiosuus"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Arvosteltu"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Arvosanat"),
    "Help" : MessageLookupByLibrary.simpleMessage("Ohje"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Korkean kontrastin tila"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Kuinka tämä vaikuttaa sinuun?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("En saa asioita tehtyä, ennen kuin kuulen sinusta."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Tarvitsen apua, mutta tämä ei ele kiireellistä."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Sisäänkirjautumisessani on ongelma"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idea Canvas Parent App -sovellukselle [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Tulevien laatikko"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Saapuvia posteja nolla!"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Keskeneräinen"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Laitoksen ilmoitus"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Laitoksen ilmoitukset"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Rubriikki"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Näyttää olevan hyvä päivä levätä, rentoutua ja latautua."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Näyttää siltä kuin tehtäviä ei vielä olisi luotu tässä tilassa."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Vain satunnainen kysymys, kommentti, idea, ehdotus..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Myöhään"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Käynnistä ulkoinen työkalu"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Lakitiedot"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Vaalea tila"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Linkin virhe"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Kielialue:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Sijainti"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Lukittu"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Kirjaudu ulos"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Hallitse opiskelijoita"),
    "Message" : MessageLookupByLibrary.simpleMessage("Viesti"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Viestin otsikko"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Puuttuu"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Täytyy olla alle 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Verkkovirhe."),
    "Never" : MessageLookupByLibrary.simpleMessage("Ei koskaan"),
    "New message" : MessageLookupByLibrary.simpleMessage("Uusi viesti"),
    "No" : MessageLookupByLibrary.simpleMessage("Ei"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Ei hälytyksiä"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Ei tehtäviä"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Ei kursseja"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Ei määräpäivää"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Ei tapahtumia tänään!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Ei arvosanaa"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Sijaintia ei ole määritetty"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Ei opiskelijoita"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Ei aihetta"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Ei yhteenvetoa"),
    "No description" : MessageLookupByLibrary.simpleMessage("Ei kuvausta"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Vastaanottajia ei valittu"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Ei arvosteltu"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Ei lähetetty"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Et ole vanhempi?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Muistutusilmoitukset tehtävistä ja kalenteritapahtumista"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Käyttöjärjestelmäversio"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Havainnoija"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Yksi muista sovelluksistamme saattaa olla paremmin sopiva. Napsauta yhtä vieraillaksesi Play Storessa."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Avaa selaimessa"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Avaa toisella sovelluksella"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Parinmuodostuskoodi"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Valmistellaan..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Edelliset kirjautumiset"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Tietosuojakäytäntö"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Tietosuojakäytäntö, käyttöehdot, avoin lähde"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Viivakoodi"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Vastaanottajat"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Muistuta minua"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Muistutukset"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Vastaus"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Vastaa kaikille"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Raportoi ongelma"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Pyydä sisäänkirjautumisohje"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Pyydä sisäänkirjautumisohje -painike"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Käynnistä sovellus uudelleen"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Yritä uudelleen"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Palaa kirjautumiseen"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("OPISKELIJA"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Valitse vastaanottajat"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Lähetä viesti tästä tehtävästä"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Lähetä viesti tästä kurssista"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Lähetä viesti"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Määritä päivämäärä ja aika, jolloin sinulle ilmoitetaan tästä tapahtumasta."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Määritä päivämäärä ja aika, jolloin sinulle ilmoitetaan tästä määrätystä tehtävästä."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Aseta muistuttimen kytkin"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Asetukset"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Jaa rakkautesi sovellusta kohtaan"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Jotakin on rikki, mutta löydän tavan saada tehdyksi, mitä minun pitää saada tehdyksi."),
    "Student" : MessageLookupByLibrary.simpleMessage("Opiskelija"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Aihe"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Lähetetty"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Lähetetty onnistuneesti!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Yhteenveto"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Vaihda käyttäjiä"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Opinto-ohjelma"),
    "TA" : MessageLookupByLibrary.simpleMessage("Apuopettaja"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("OPETTAJA"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Valitse napauttamalla kurssit, jotka haluat nähdä kalenterissa."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Muodosta yhteys uuden opiskelijan kanssa napauttamalla"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Valitse tämä opiskelija napauttamalla"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Näytä opiskelijan valintatoiminto napauttamalla"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Opettaja"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Kerro meille sovelluksen suosikkiosista"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Käyttöehdot"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Seuraavat tiedot auttavat meitä ymmärtämään ideasi paremmin:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Syöttämääsi palvelinta ei ole valtuutettu tälle sovellukselle."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Tämän sovelluksen käyttäjän edustajalle ei ole annettu valtuutuksia."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Teema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Ei ole asennettuna sovelluksia, joilla tämä tiedosto voidaan avata"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Sivutietoja ei ole saatavilla."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Ilmeni ongelma käyttöehtoja ladattaessa"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Tämän kurssin vastaanottajien lataamisessa ilmeni virhe"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Tämän kurssin yhteenvetotietojen lataamisessa ilmeni virhe."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Tämän ilmoituksen latauksessa ilmeni virhe"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Tämän keskustelun latauksessa ilmeni virhe"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Tämän tiedoston latauksessa ilmeni virhe"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Tulevien laatikon latauksessa ilmeni virhe."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Opiskelijahälytysten latauksessa ilmeni virhe."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Opiskelijakalenterin latauksessa ilmeni virhe"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Opiskelijoidesi latauksessa ilmeni virhe."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Opiskelijasi kurssien latauksessa ilmeni virhe."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Ei ole vielä mitään ilmoitettavaa."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Tätä sovellusta ei ole valtuutettu käyttöön."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Kurssilla ei ole vielä tehtäviä tai kalenterin tehtäviä."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Tätä tiedostoa ei tueta eikä sitä voida tarkastella sovelluksen läpi"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Kokonaisarvosana"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Voi ei!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Ei voida noutaa kursseja. Tarkasta yhteytesi ja yritä uudelleen."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Tätä kuvaa ei voida ladata"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Tätä mediatiedostoa ei voida toistaa"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Viestin lähetys ei onnistunut. Tarkasta verkkoyhteys ja yritä uudelleen."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Rakenteilla"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Tuntematon käyttäjä"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Tallentamattomat muutokset"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Tukematon tiedosto"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Lataa tiedosto"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Käytä kameraa"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Käyttäjätunnus:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Version numero"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Näytä virhetiedot"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Rakennamme tätä ominaisuutta tarkastelun käytännöllisyyden vuoksi."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Emme pysty näyttämään tätä linkkiä. Se saattaa kuulua laitokselle, johon et ole parhaillaan kirjautuneena sisään."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Emme löytäneet opiskelijoita, jotka liittyisivät tähän tiliin"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Emme voineet vahvistaa tämän sovelluksen kanssa käytettävää palvelinta."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Emme ole varmoja, mitä tapahtui, mutta se ei ollut hyvä. Ota meihin yhteyttä, jos tätä tapahtuu edelleen."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Kyllä"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Et tarkkaile yhtään opiskelijaa."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Saat ilmoituksen tästä tehtävästä..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Saat ilmoituksen tästä tapahtumasta..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Koodisi on virheellinen tai vanhentunut."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Tämän opiskelijan kursseja ei ehkä ole vielä julkaistu."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Olet ajan tasalla!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Hälytykset"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Kalenteri"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas-oppaat"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas-logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas-tuki"),
    "collapse" : MessageLookupByLibrary.simpleMessage("kutista"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("kutistettu"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kurssit"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("hylkää"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Miten löydän kouluni tai alueeni?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Kirjoita koulun nimi tai alue..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("laajenna"),
    "expanded" : MessageLookupByLibrary.simpleMessage("laajennettu"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Etsi koulu"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("minä"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("miinus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Seuraava"),
    "nextMonth" : m28,
    "nextWeek" : m29,
    "noDomainResults" : m30,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m31,
    "plusRecipientCount" : m32,
    "pointsPossible" : m33,
    "previousMonth" : m34,
    "previousWeek" : m35,
    "selectedMonthLabel" : m36,
    "send" : MessageLookupByLibrary.simpleMessage("lähettää"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("lukematon"),
    "unreadCount" : m39
  };
}
