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

  static m12(studentName) => "Canvia el color de ${studentName}";

  static m13(score, pointsPossible) => "${score} de ${pointsPossible} punts";

  static m14(studentShortName) => "per a ${studentShortName}";

  static m15(threshold) => "Qualificació del curs per sobre de ${threshold}";

  static m16(threshold) => "Qualificació del curs per sota de ${threshold}";

  static m17(date, time) => "${date} a les ${time}";

  static m18(alertTitle) => "Descarta ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Proveu de cercar pel nom de l\'escola o districte al qual intenteu accedir, com ara “Escola Privada Smith” o “Escoles de la Regió d\'Smith”. També podeu introduir un domini del Canvas directament, com ara “smith.instructure.com.”\n\nPer obtenir més informació sobre com cercar el compte del Canvas de la vostra institució, podeu consultar les ${canvasGuides}, contactar amb l\'${canvasSupport}, o posar-vos en contacte amb la vostra escola per obtenir assistència.";

  static m20(date, time) => "Venç el ${date} a les ${time}";

  static m21(userName) => "Deixareu d\'actuar com a ${userName} i es tancarà la vostra sessió.";

  static m22(userName) => "Deixareu d\'actuar com a ${userName} i tornareu al vostre compte original.";

  static m23(studentName, eventTitle) => "Sobre: ${studentName}, esdeveniment - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Qualificació final: ${grade}";

  static m26(studentName) => "Sobre: ${studentName}, pàgina frontal";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Sobre: ${studentName}, qualificacions";

  static m29(pointsLost) => "Sanció per endarreriment (-${pointsLost})";

  static m30(studentName, linkUrl) => "Sobre: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Ha de ser superior a ${percentage}";

  static m32(percentage) => "Ha de ser inferior a ${percentage}";

  static m33(month) => "El mes que ve: ${month}";

  static m34(date) => "La setmana que ve a partir del ${date}";

  static m35(query) => "No s\'han trobat escoles que coincideixin amb “${query}”";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'D\'1 punt', other: 'De ${points} punts')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} punts possibles";

  static m39(month) => "El mes passat: ${month}";

  static m40(date) => "La setmana passada a partir del ${date}";

  static m41(termsOfService, privacyPolicy) => "En tocar \"Crea un compte\", accepteu les ${termsOfService} i reconeixeu la ${privacyPolicy}.";

  static m42(version) => "Suggeriments per a Android - Canvas Parent ${version}";

  static m43(month) => "Mes de ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} estrella', other: '${position} estrelles')}";

  static m45(date, time) => "Aquesta tasca es va entregar el ${date} a les ${time} i està a l\'espera de qualificació";

  static m46(studentName) => "Sobre: ${studentName}, temari";

  static m47(count) => "${count} sense llegir";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("“Actua com” vol dir bàsicament iniciar la sessió com aquest usuari sense una contrasenya. Podreu fer qualsevol acció com si fóssiu aquest usuari, i des del punt de vista dels altres usuaris, serà com si les hagués dut a terme aquest usuari. Tanmateix, els registres d\'auditoria enregistren que heu sigut vós qui ha dut a terme les accions en nom de l\'altre usuari."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Cal una descripció."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error de xarxa en afegir aquest estudiant. Reviseu la connexió i torneu-ho a provar."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Cal un assumpte."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Actua com un usuari"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Afegeix un estudiant"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Afegeix un fitxer adjunt"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Afegeix un estudiant nou"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Afegeix un estudiant amb..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Configuració de l\'avís"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Avisa\'m quan..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Tots els períodes de qualificació"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Ja teniu un compte? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Cal una adreça electrònica."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en provar de mostrar aquest enllaç"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en desar la vostra selecció. Torneu-ho a provar."),
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
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, fúcsia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendaris"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Permís per a la càmera"),
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
    "Create Account" : MessageLookupByLibrary.simpleMessage("Crea un compte"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mode fosc"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Suprimeix"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descripció"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositiu"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Model del dispositiu"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domini"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domini:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("No ho tornis a mostrar"),
    "Done" : MessageLookupByLibrary.simpleMessage("Fet"),
    "Download" : MessageLookupByLibrary.simpleMessage("Baixa"),
    "Due" : MessageLookupByLibrary.simpleMessage("Venciment"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGÈNCIA DE CRÍTICA IMPORTÀNCIA!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elèctric, blau"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adreça electrònica"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Adreça electrònica:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Adreça electrònica..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Introduïu el codi d\'emparellament d\'estudiants que se us ha proporcionat. Si el codi d\'emparellament no funciona, pot ser que hagi caducat"),
    "Event" : MessageLookupByLibrary.simpleMessage("Esdeveniment"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Excusat"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Codi QR caducat"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error. Toqueu per veure les opcions."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtre"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtra per"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Foc, taronja"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Pàgina frontal"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Nom complet"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Nom complet..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Missatge d\'error complet"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Vés a avui"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Qualificació"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Percentatge de la qualificació"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Qualificat"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Qualificacions"),
    "Help" : MessageLookupByLibrary.simpleMessage("Ajuda"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Amaga la contrasenya"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Mode de contrast alt"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Com ho estem fem?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Com us afecta això?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("No podré fer res fins que em respongueu."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("No tinc un compte del Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Tinc un compte del Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Necessito ajuda, però no és urgent."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Tinc problemes per iniciar la sessió"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idea per a l\'aplicació Canvas Parent (Android)"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Per tal de proporcionar-vos una millor experiència, hem actualitzat la manera com funcionen els recordatoris. Podeu afegir recordatoris nous en visualitzar una tasca o esdeveniment del calendari i tocant el botó a sota de la secció \"Recorda-m\'ho\".\n\nTingueu en compte que els recordatoris creats amb les versions anteriors de l\'aplicació no seran compatibles amb els nous canvis i els haureu de tornar a crear."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Safata d\'entrada"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Safata d\'entrada buida"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incomplet"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Domini incorrecte"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Anunci de la institució"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Anuncis de la institució"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruccions"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("La vostra institució ha limitat les interaccions en aquesta pàgina."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Codi QR no vàlid"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Sembla un dia fabulós per descansar, relaxar-se i carregar piles."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Sembla que en aquest espai encara no s\'ha creat cap tasca."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Simplement una pregunta, un comentari, una idea, un suggeriment informal…"),
    "Late" : MessageLookupByLibrary.simpleMessage("Tardà"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Inicieu l\'eina externa"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Jurídic"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Mode clar"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Error d\'enllaç"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Zona:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Localitzeu el codi QR"),
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
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Obriu Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Obre\'l al navegador"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Obre amb una altra aplicació"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Codi d\'emparellament"),
    "Password" : MessageLookupByLibrary.simpleMessage("Contrasenya"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("La contrasenya és obligatòria"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("La contrasenya ha de tenir com a mínim 8 caràcters"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Contrasenya..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Nota del planificador"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Introduïu una adreça electrònica vàlida"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Introduïu una adreça electrònica"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Introduïu un nom complet"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Escanegeu un codi QR generat pel Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Pruna, porpra"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("S\'està preparant..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Inicis de sessió anteriors"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Política de privacitat"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Enllaç a la Política de privacitat"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Política de privacitat, condicions d\'ús, font oberta"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Codi QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Per escanejar el codi QR, cal tenir accés a la càmera."),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Gerd, vermell"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinataris"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Actualitza"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Recorda-m\'ho"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Recordatoris"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Els recordatoris han canviat!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Respon"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Respon a tots"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Informeu d\'un problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Sol·liciteu ajuda per iniciar la sessió"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Botó Sol·liciteu ajuda per iniciar la sessió"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reinicia l\'aplicació"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Torna-ho a provar"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Torna a l\'inici de sessió"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ESTUDIANT"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Captura de pantalla que mostra la ubicació de la generació d\'un codi QR al navegador"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Captura de pantalla en què es mostra la ubicació de la generació del codi QR d\'emparellament a l\'aplicació Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Selecciona"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Selecciona el color de l’estudiant"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Selecciona els destinataris"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Envia els comentaris"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Envia un missatge sobre aquesta tasca"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Envia un missatge sobre aquest curs"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Envia el missatge"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Establiu una data i hora per rebre una notificació sobre aquest esdeveniment."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Establiu una data i hora per rebre una notificació sobre aquesta tasca concreta."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Configuració"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Trèvol, verd"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Compartiu l\'interès que us desperta l\'aplicació"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Mostra la contrasenya"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Inicia la sessió"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Alguna cosa no ha anat bé en intentar crear el vostre compte, poseu-vos en contacte amb la vostra escola per obtenir assistència."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Alguna cosa no funciona bé però per ara puc seguir treballant per tal d\'acabar de fer allò que necessito."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Deixa d\'actuar com a usuari"),
    "Student" : MessageLookupByLibrary.simpleMessage("Estudiant"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Emparellament de l\'estudiant"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Al dispositiu mòbil, els estudiants poden crear un codi QR mitjançant l’aplicació Canvas Student"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Els estudiants poden obtenir un codi d\'emparellament a través del lloc web de Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Assumpte"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Entregat"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("S\'ha enviat correctament."),
    "Summary" : MessageLookupByLibrary.simpleMessage("Resum"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Canvia els usuaris"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Temari"),
    "TA" : MessageLookupByLibrary.simpleMessage("Auxiliar de professor"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PROFESSOR"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Toqueu per afegir als preferits els cursos que voleu veure al Calendari. Seleccioneu-ne fins a 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Toqueu per emparellar amb un estudiant nou"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Toqueu per seleccionar aquest estudiant"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Toqueu per mostrar el selector d\'estudiants"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Professor"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Expliqueu-nos quines són les parts preferides de l\'aplicació"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Condicions de servei"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Enllaç a les Condicions de servei"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Condicions d\'ús"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("És possible que el codi QR que heu escanejat ja hagi caducat. Actualitzeu el codi al dispositiu de l’estudiant i torneu-ho a provar."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("La informació següent ens ajudarà a conèixer millor la vostra idea:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("El servidor que heu introduït no té autorització per a aquesta aplicació."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("L\'estudiant que esteu provant d\'afegir pertany a una altra escola. Inicieu la sessió o creeu un compte amb aquesta escola per escanejar aquest codi."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("L\'agent d\'usuari per a aquesta aplicació no està autoritzat."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("No hi ha cap aplicació instal·lada que pugui obrir aquest fitxer"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("No hi ha informació disponible sobre la pàgina."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("S\'ha produït un problema en carregar les condicions d\'ús"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("S’ha produït un problema en suprimir aquest estudiant del vostre compte. Reviseu la connexió i torneu-ho a provar."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els destinataris d\'aquest curs"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els detalls de resumen d\'aquest curs."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar aquest anunci"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar aquesta conversa"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar aquest fitxer"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els missatges de la vostra safata d\'entrada."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els vostres avisos de l\'estudiant."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar el vostre calendari de l\'estudiant"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els vostres estudiants."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en carregar els vostres cursos de l\'estudiant."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en iniciar la sessió. Genereu un altre codi QR i torneu-ho a provar."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("S\'ha produït un error en intentar actuar com a aquest usuari. Comproveu l\'ID d\'usuari i el domini i torneu-ho a provar."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Encara no hi ha res que s\'hagi de notificar."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Aquesta aplicació no té autorització perquè s\'utilitzi."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Aquest curs encara no té cap tasca o esdeveniment al calendari."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Aquest fitxer no s\'admet i no es pot visualitzar a través de l\'aplicació"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Amb aquesta acció es cancel·larà l’emparellament i se suprimiran del vostre compte totes les inscripcions per a aquest estudiant."),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Utilitza el tema fosc al contingut web"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID d\'usuari"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID d\'usuari:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Número de versió"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Mostra la descripció"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Visualitza els detalls de l\'error"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Visualitza la política de privacitat"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Estem creant aquesta característica perquè pugueu gaudir de la visualització."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("No podem mostrar aquest enllaç, pot ser que pertanyi a una institució en la que actualment no teniu la sessió iniciada."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("No hem trobat cap estudiant associat amb aquest compte"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("No hem pogut verificar si el servidor es pot utilitzar amb aquesta aplicació."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("No estem segurs de què ha passat, però no ha sigut res bo. Poseu-vos en contacte amb nosaltres si us segueix passant."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Què podem fer millor?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sí"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("No esteu observant cap estudiant."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Només podeu triar 10 calendaris perquè es mostrin"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Heu d\'introduir un ID d\'usuari"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Heu d\'introduir un domini vàlid"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Heu de seleccionar com a mínim un calendari perquè es mostri"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Rebreu una notificació sobre aquesta tasca el..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Rebreu una notificació sobre aquest esdeveniment el..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Trobareu el codi QR a la web, al perfil del vostre compte. Feu clic a \"QR per a inici de sessió mòbil\" a la llista."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Per continuar, haureu d\'obrir l’aplicació Canvas Student de l’estudiant. Aneu a Menú principal > Configuració > Emparella amb l\'observador i escanegeu el codi QR que s’hi mostra."),
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
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("redueix"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("reduït"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cursos"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Com puc trobar la meva escola o districte?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Introduïu el nom de l\'escola o el districte..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("desplega"),
    "expanded" : MessageLookupByLibrary.simpleMessage("desplegat"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Cerca una escola"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("mi"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("menys"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Següent"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("D\'acord"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("envia"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("sense llegir"),
    "unreadCount" : m47
  };
}
