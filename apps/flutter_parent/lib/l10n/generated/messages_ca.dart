// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a ca locale. All the
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
  String get localeName => 'ca';

  static m0(userName) => "Esteu fent de ${userName}";

  static m1(version) => "versió ${version}";

  static m2(threshold) => "Qualificació de la tasca per sobre de ${threshold}";

  static m3(threshold) => "Qualificació de la tasca per sota de ${threshold}";

  static m4(moduleName) => "El mòdul “${moduleName}” bloqueja aquesta tasca.";

  static m5(studentName, assignmentName) => "Sobre: ${studentName}, tasca - ${assignmentName}";

  static m6(points) => "${points} punts";

  static m7(points) => "${points} punts";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} a 1 altre', other: '${authorName} a ${howMany} més')}";

  static m9(authorName, recipientName) => "De ${authorName} a ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} a ${recipientName} i 1 altre', other: '${authorName} a ${recipientName} i ${howMany} més')}";

  static m11(count) => "${count}+";

  static m12(score, pointsPossible) => "${score} de ${pointsPossible} punts";

  static m13(studentShortName) => "per a ${studentShortName}";

  static m14(threshold) => "Qualificació del curs per sobre de ${threshold}";

  static m15(threshold) => "Qualificació del curs per sota de ${threshold}";

  static m16(date, time) => "${date} a les ${time}";

  static m17(canvasGuides, canvasSupport) => "Proveu de cercar pel nom de l\'escola o districte al qual intenteu accedir, com ara “Escola Privada Smith” o “Escoles de la Regió d\'Smith”. També podeu introduir un domini del Canvas directament, com ara “smith.instructure.com.”\n\nPer obtenir més informació sobre com cercar el compte del Canvas de la vostra institució, podeu consultar les ${canvasGuides}, contactar amb l\'${canvasSupport}, o posar-vos en contacte amb la vostra escola per obtenir assistència.";

  static m18(date, time) => "Venç el ${date} a les ${time}";

  static m19(userName) => "Deixareu d\'actuar com a ${userName} i es tancarà la vostra sessió.";

  static m20(userName) => "Deixareu d\'actuar com a ${userName} i tornareu al vostre compte original.";

  static m21(studentName, eventTitle) => "Sobre: ${studentName}, esdeveniment - ${eventTitle}";

  static m22(startAt, endAt) => "${startAt} - ${endAt}";

  static m23(grade) => "Qualificació final: ${grade}";

  static m24(studentName) => "Sobre: ${studentName}, pàgina frontal";

  static m25(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m26(studentName) => "Sobre: ${studentName}, qualificacions";

  static m27(pointsLost) => "Sanció per endarreriment (-${pointsLost})";

  static m28(studentName, linkUrl) => "Sobre: ${studentName}, ${linkUrl}";

  static m29(percentage) => "Ha de ser superior a ${percentage}";

  static m30(percentage) => "Ha de ser inferior a ${percentage}";

  static m31(month) => "El mes que ve: ${month}";

  static m32(date) => "La setmana que ve a partir del ${date}";

  static m33(query) => "No s\'han trobat escoles que coincideixin amb “${query}”";

  static m34(points, howMany) => "${Intl.plural(howMany, one: 'D\'1 punt', other: 'De ${points} punts')}";

  static m35(count) => "+${count}";

  static m36(points) => "${points} punts possibles";

  static m37(month) => "El mes passat: ${month}";

  static m38(date) => "La setmana passada a partir del ${date}";

  static m39(month) => "Mes de ${month}";

  static m40(date, time) => "Aquesta tasca es va entregar el ${date} a les ${time} i està a l\'espera de qualificació";

  static m41(studentName) => "Sobre: ${studentName}, temari";

  static m42(count) => "${count} sense llegir";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("“Actua com” vol dir bàsicament iniciar la sessió com aquest usuari sense una contrasenya. Podreu fer qualsevol acció com si fóssiu aquest usuari, i des del punt de vista dels altres usuaris, serà com si les hagués dut a terme aquest usuari. Tanmateix, els registres d\'auditoria enregistren que heu sigut vós qui ha dut a terme les accions en nom de l\'altre usuari."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Cal una descripció."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Cal un assumpte."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Actua com un usuari"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Afegeix un estudiant"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Afegeix un fitxer adjunt"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Afegeix un estudiant nou"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Afegeix un estudiant amb..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Configuració de l\'avís"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Avisa\'m quan..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Tots els períodes de qualificació"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Cal una adreça electrònica."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en provar de mostrar aquest enllaç"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error inesperat"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Versió del sistema operatiu Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Aspecte"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Versió de l\'aplicació"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Sou un estudiant o professor?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Segur que voleu tancar la sessió?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Segur que voleu tancar aquesta pàgina? El vostre missatge no enviat es perdrà."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Detalls de la tasca"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Qualificació de la tasca per sobre de"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Qualificació de la tasca per sota de"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Falta la tasca"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendaris"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Cancel·la"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas a GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Trieu un curs per enviar el missatge"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Tria de la galeria"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Complet"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Contacta amb l\'assistència tècnica"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Anunci del curs"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Anuncis del curs"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Qualificació del curs per sobre de"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Qualificació del curs per sota de"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mode fosc"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Suprimeix"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descripció"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositiu"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Model del dispositiu"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domini"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domini:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fet"),
    "Download" : MessageLookupByLibrary.simpleMessage("Baixa"),
    "Due" : MessageLookupByLibrary.simpleMessage("Venciment"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGÈNCIA DE CRÍTICA IMPORTÀNCIA!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adreça electrònica"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Adreça electrònica:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Introduïu el codi d\'emparellament d\'estudiants que se us ha proporcionat. Si el codi d\'emparellament no funciona, pot ser que hagi caducat"),
    "Event" : MessageLookupByLibrary.simpleMessage("Esdeveniment"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Excusat"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error. Toqueu per veure les opcions."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtre"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtra per"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Pàgina frontal"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Missatge d\'error complet"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Qualificació"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Percentatge de la qualificació"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Qualificat"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Qualificacions"),
    "Help" : MessageLookupByLibrary.simpleMessage("Ajuda"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Mode de contrast alt"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Com us afecta això?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("No podré fer res fins que em respongueu."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Necessito ajuda, però no és urgent."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Tinc problemes per iniciar la sessió"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idea per a l\'aplicació Canvas Parent (Android)"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Safata d\'entrada"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Safata d\'entrada buida"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incomplet"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Anunci de la institució"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Anuncis de la institució"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruccions"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("La vostra institució ha limitat les interaccions en aquesta pàgina."),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Sembla un dia fabulós per descansar, relaxar-se i carregar piles."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Sembla que en aquest espai encara no s\'ha creat cap tasca."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Simplement una pregunta, un comentari, una idea, un suggeriment informal…"),
    "Late" : MessageLookupByLibrary.simpleMessage("Tardà"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Inicieu l\'eina externa"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Jurídic"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Mode clar"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Error d\'enllaç"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Zona:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Ubicació"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Bloquejat"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Tanca la sessió"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Flux d\'inici de sessió: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Flux d\'inici de sessió: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Flux d\'inici de sessió: Administrador del lloc web"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Flux d\'inici de sessió: Omet la verificació mòbil"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Administra els estudiants"),
    "Message" : MessageLookupByLibrary.simpleMessage("Missatge"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Assumpte del missatge"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Falta"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Ha de ser inferior a 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Error de xarxa"),
    "Never" : MessageLookupByLibrary.simpleMessage("Mai"),
    "New message" : MessageLookupByLibrary.simpleMessage("Missatge nou"),
    "No" : MessageLookupByLibrary.simpleMessage("No"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Cap avís"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("No hi ha cap tasca"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("No hi ha cap curs"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Sense data de venciment"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Avui no hi ha cap esdeveniment."),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Sense qualificació"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("No s\'ha especificat cap ubicació"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Cap estudiant"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Sense assumpte"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Cap resum"),
    "No description" : MessageLookupByLibrary.simpleMessage("Sense descripció"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Cap destinatari seleccionat"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Sense qualificació"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("No entregat"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("No sou un progenitor?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notificacions de recordatoris sobre tasques i esdeveniments del calendari"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Versió del sistema operatiu"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observador"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("És possible que alguna de les altres aplicacions nostres us sigui més útil. Toqueu-ne una per anar a Play Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Obre\'l al navegador"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Obre amb una altra aplicació"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Codi d\'emparellament"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("S\'està preparant..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Inicis de sessió anteriors"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Política de privacitat"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Política de privacitat, condicions d\'ús, font oberta"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Codi QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinataris"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Recorda-m\'ho"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Recordatoris"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Respon"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Respon a tots"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Informeu d\'un problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Sol·liciteu ajuda per iniciar la sessió"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Botó Sol·liciteu ajuda per iniciar la sessió"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reinicia l\'aplicació"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Torna-ho a provar"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Torna a l\'inici de sessió"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ESTUDIANT"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Selecciona els destinataris"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Envia un missatge sobre aquesta tasca"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Envia un missatge sobre aquest curs"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Envia el missatge"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Establiu una data i hora per rebre una notificació sobre aquest esdeveniment."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Establiu una data i hora per rebre una notificació sobre aquesta tasca concreta."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Configuració"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Compartiu l\'interès que us desperta l\'aplicació"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Alguna cosa no funciona bé però per ara puc seguir treballant per tal d\'acabar de fer allò que necessito."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Deixa d\'actuar com a usuari"),
    "Student" : MessageLookupByLibrary.simpleMessage("Estudiant"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Assumpte"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Entregat"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("S\'ha enviat correctament."),
    "Summary" : MessageLookupByLibrary.simpleMessage("Resum"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Canvia els usuaris"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Temari"),
    "TA" : MessageLookupByLibrary.simpleMessage("Auxiliar de professor"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PROFESSOR"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Toqueu per afegir als preferits els cursos que voleu veure al Calendari."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Toqueu per emparellar amb un estudiant nou"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Toqueu per seleccionar aquest estudiant"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Toqueu per mostrar el selector d\'estudiants"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Professor"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Expliqueu-nos quines són les parts preferides de l\'aplicació"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Condicions d\'ús"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("La informació següent ens ajudarà a conèixer millor la vostra idea:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("El servidor que heu introduït no té autorització per a aquesta aplicació."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("L\'agent d\'usuari per a aquesta aplicació no està autoritzat."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("No hi ha cap aplicació instal·lada que pugui obrir aquest fitxer"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("No hi ha informació disponible sobre la pàgina."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("S\'ha produït un problema en carregar les condicions d\'ús"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els destinataris d\'aquest curs"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els detalls de resumen d\'aquest curs."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar aquest anunci"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar aquesta conversa"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar aquest fitxer"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els missatges de la vostra safata d\'entrada."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els vostres avisos de l\'estudiant."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar el vostre calendari de l\'estudiant"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els vostres estudiants."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els vostres cursos de l\'estudiant."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en intentar actuar com a aquest usuari. Comproveu l\'ID d\'usuari i el domini i torneu-ho a provar."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Encara no hi ha res que s\'hagi de notificar."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Aquesta aplicació no té autorització perquè s\'utilitzi."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Aquest curs encara no té cap tasca o esdeveniment al calendari."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Aquest fitxer no s\'admet i no es pot visualitzar a través de l\'aplicació"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Qualificació total"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Oh!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("No es poden obtenir els cursos. Reviseu la connexió i torneu-ho a provar."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("No es pot carregar aquesta imatge"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("No es pot reproduir aquest fitxer multimèdia"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("No es pot enviar el missatge. Reviseu la connexió i torneu-ho a provar."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("En construcció"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Usuari desconegut"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Canvis no desats"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Fitxer no admès"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Penja el fitxer"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Utilitza la càmera"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID d\'usuari"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID d\'usuari:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Número de versió"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visualitza els detalls de l\'error"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Estem creant aquesta característica perquè pugueu gaudir de la visualització."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("No podem mostrar aquest enllaç, pot ser que pertanyi a una institució en la que actualment no teniu la sessió iniciada."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("No hem trobat cap estudiant associat amb aquest compte"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("No hem pogut verificar si el servidor es pot utilitzar amb aquesta aplicació."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("No estem segurs de què ha passat, però no ha sigut res bo. Poseu-vos en contacte amb nosaltres si us segueix passant."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sí"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("No esteu observant cap estudiant."),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Heu d\'introduir un ID d\'usuari"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Heu d\'introduir un domini vàlid"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Rebreu una notificació sobre aquesta tasca el..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Rebreu una notificació sobre aquest esdeveniment el..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("El vostre codi es incorrecte o ha caducat."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("És possible que encara no s\'hagin publicat els vostres cursos de l\'estudiant."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Esteu completament al dia!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Avisos"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendari"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guies del Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logotip del Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Assistència tècnica del Canvas"),
    "collapse" : MessageLookupByLibrary.simpleMessage("redueix"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("reduït"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cursos"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Com puc trobar la meva escola o districte?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Introduïu el nom de l\'escola o el districte..."),
    "dueDateAtTime" : m18,
    "endMasqueradeLogoutMessage" : m19,
    "endMasqueradeMessage" : m20,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("desplega"),
    "expanded" : MessageLookupByLibrary.simpleMessage("desplegat"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Cerca una escola"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("mi"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("menys"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("Següent"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("D\'acord"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("envia"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("sense llegir"),
    "unreadCount" : m42
  };
}
