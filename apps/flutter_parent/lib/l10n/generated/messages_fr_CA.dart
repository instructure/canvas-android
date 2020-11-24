// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a fr_CA locale. All the
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
  String get localeName => 'fr_CA';

  static m0(userName) => "Vous agissez en tant que ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Note de la tâche supérieure à ${threshold}";

  static m3(threshold) => "Note de la tâche inférieure à ${threshold}";

  static m4(moduleName) => "Cette tâche est verrouillée par le module « ${moduleName} ».";

  static m5(studentName, assignmentName) => "En ce qui concerne : ${studentName}, tâche — ${assignmentName}";

  static m6(points) => "${points} pts";

  static m7(points) => "${points} points";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} à un autre', other: '${authorName} à ${howMany} autres')}";

  static m9(authorName, recipientName) => "${authorName} à ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} à ${recipientName} et un autre', other: '${authorName} à ${recipientName} et ${howMany} autres')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Changer la couleur pour ${studentName}";

  static m13(score, pointsPossible) => "${score} de ${pointsPossible} points";

  static m14(studentShortName) => "pour ${studentShortName}";

  static m15(threshold) => "Note du cours supérieure à ${threshold}";

  static m16(threshold) => "Note du cours inférieure à ${threshold}";

  static m17(date, time) => "${date} à ${time}";

  static m18(alertTitle) => "Ignorer ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Essayez de rechercher le nom de l’école ou du district auquel vous tentez d’accéder, comme « École privée Smith » ou « Écoles régionales Smith ». Vous pouvez également saisir un domaine Canvas directement, comme « smith.instructure.com. »\n\nPour obtenir de plus amples renseignements sur la façon de trouver le compte Canvas de votre établissement, vous pouvez consulter ${canvasGuides}, communiquer avec ${canvasSupport}, ou contacter votre école pour obtenir de l’aide.";

  static m20(date, time) => "Dû le ${date} à ${time}";

  static m21(userName) => "Vous allez cesser d’agir comme ${userName} et serez déconnecté.";

  static m22(userName) => "Vous allez cesser d’agir comme ${userName} et retournerez à votre compte original.";

  static m23(studentName, eventTitle) => "En ce qui concerne : ${studentName}, événement — ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} — ${endAt}";

  static m25(grade) => "Note finale : ${grade}";

  static m26(studentName) => "En ce qui concerne : ${studentName}, page de couverture";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "En ce qui concerne : ${studentName}, notes";

  static m29(pointsLost) => "Pénalité de retard (-${pointsLost})";

  static m30(studentName, linkUrl) => "En ce qui concerne : ${studentName}, ${linkUrl}";

  static m31(percentage) => "Doit être supérieure à ${percentage}";

  static m32(percentage) => "Doit être inférieure à ${percentage}";

  static m33(month) => "Mois suivant : ${month}";

  static m34(date) => "Semaine prochaine commençant le ${date}";

  static m35(query) => "Impossible de trouver les écoles correspondant à « ${query} »";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Sur 1 point', other: 'Sur ${points} points')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} points possibles";

  static m39(month) => "Mois précédent : ${month}";

  static m40(date) => "Semaine précédente commençant le ${date}";

  static m41(termsOfService, privacyPolicy) => "En tapotant sur « Créer un compte », vous acceptez les ${termsOfService} ainsi que ${privacyPolicy}";

  static m42(version) => "Suggestions pour Android - Canvas Parent ${version}";

  static m43(month) => "Mois de ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} étoile', other: '${position} étoiles')}";

  static m45(date, time) => "Cette tâche a été soumise le ${date} à ${time} et attend d’être notée";

  static m46(studentName) => "En ce qui concerne : ${studentName}, programme";

  static m47(count) => "${count} non lu";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("« Agir en tant que » (Act as) est essentiellement d\'ouvrir une session en tant que cet utilisateur sans mot de passe. Vous serez en mesure de prendre toute action comme si vous étiez cet utilisateur, et selon les points de vue des autres utilisateurs, ce sera comme si cet utilisateur aurait effectué ces actions. Toutefois, le journal d\'événements a identifié que vous étiez celui qui a effectué les actions au nom de cet étudiant."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Description obligatoire."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Une erreur réseau s’est produite lors de l’ajout de cet étudiant. Vérifiez votre connexion puis réessayez."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Un objet est requis."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Agir en tant qu\'utilisateur"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Ajouter un étudiant"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Ajouter une pièce jointe"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Ajouter un nouvel étudiant"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Ajouter étudiant avec…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Réglages de l’alerte"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("M’alerter lorsque…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Toutes les périodes de notation"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Vous possédez déjà un compte? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Adresse de courriel nécessaire."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Une erreur s\'est produite au moment de l’affichage de ce lien"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Une erreur s\'est produite lors de l’enregistrement de votre sélection. Veuillez réessayer."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Une erreur inattendue s’est produite"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Version du SE Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Apparence"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Version de l’application"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Êtes-vous étudiant ou enseignant?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Voulez-vous vraiment vous déconnecter?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Voulez-vous vraiment fermer cette page? Vos messages non envoyés seront perdus."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Détails de la tâche"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Note de la tâche supérieure à"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Note de la tâche inférieure à"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Tâche manquante"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Fuschia Barney"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendriers"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Autorisation de caméra"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Annuler"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Étudiant Canvas"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Enseignant Canvas"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas sur GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Choisissez un cours pour lequel envoyer un message"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Choisir dans la galerie"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Terminé"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Contacter l’équipe de soutien"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Annonce du cours"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Annonces de cours"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Note du cours supérieure à"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Note du cours inférieure à"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Créer un compte"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mode de couleur foncée"),
    "Date" : MessageLookupByLibrary.simpleMessage("Date"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Supprimer"),
    "Description" : MessageLookupByLibrary.simpleMessage("Description"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositif"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modèle de l’appareil"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domaine"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domaine :"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Ne plus afficher à nouveau"),
    "Done" : MessageLookupByLibrary.simpleMessage("Terminé"),
    "Download" : MessageLookupByLibrary.simpleMessage("Télécharger"),
    "Due" : MessageLookupByLibrary.simpleMessage("Échéance"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("ÉTAT D’URGENCE EXTRÊMEMENT SÉRIEUX!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Bleu électrique"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adresse de courriel"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Courriel :"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Adresse courriel..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Saisir le code d’appariement de l’étudiant qui vous a été fourni. Si le code d’appariement ne fonctionne pas, il peut avoir expiré"),
    "Event" : MessageLookupByLibrary.simpleMessage("Événement"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Exempté"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Code QR expiré"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Échec. Appuyez pour des options."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtre"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrer par"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Orange feu"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Page de couverture"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Nom complet"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Nom complet..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Message d’erreur complet"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Aller à aujourd\'hui"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Note"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Pourcentage de notes"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Noté"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Notes"),
    "Help" : MessageLookupByLibrary.simpleMessage("Aide"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Cacher le mot de passe"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Mode haut contraste"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Comment nous débrouillons-nous?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Comment cela vous affecte-t-il?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Je ne peux plus rien faire jusqu’à ce que vous me contactiez."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Je n’ai pas de compte Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("J’ai un compte Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("J’ai besoin d’aide, mais ce n’est pas urgent"),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("J’ai des problèmes à me connecter"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idée pour l’application Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Afin de vous offrir une meilleure expérience, nous avons mis à jour le fonctionnement des rappels. Vous pouvez ajouter de nouveaux rappels en visualisant une tâche ou un événement de calendrier et en appuyant sur le curseur dans la section « Me rappeler ».\n\nSachez que les rappels créés avec les anciennes versions de cette application ne seront pas compatibles avec les nouvelles modifications et que vous devrez les créer à nouveau."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Boîte de réception"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Boîte de réception Zéro"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incomplet"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Domaine incorrect"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Annonce de l’institution"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Annonces de l’institution"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instructions"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Les interactions sur cette page sont limitées par votre institution."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Code QR non valide"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("C\'est une belle journée pour se reposer, se détendre et recharger nos batteries."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Il semblerait qu’il n’y ait pas encore de tâche créée dans cet espace."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Juste une question informelle, commentaire, idée, suggestion…"),
    "Late" : MessageLookupByLibrary.simpleMessage("En retard"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lancer l\'outil externe"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Légal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Mode Jour"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Erreur de lien"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Paramètres régionaux :"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Trouvez le code QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Emplacement"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Verrouillé"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Déconnexion"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Flux de connexion : Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Flux de connexion : Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Flux de connexion : Administrateur du site"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Flux de connexion : Ignorer la vérification mobile"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Gestion des participants"),
    "Message" : MessageLookupByLibrary.simpleMessage("Message"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Objet du message"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Manquant"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Moins de 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Erreur de réseau"),
    "Never" : MessageLookupByLibrary.simpleMessage("Jamais"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nouveau message"),
    "No" : MessageLookupByLibrary.simpleMessage("Non"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Aucune alerte"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Aucune tâche"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Aucun cours"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Aucune date d’échéance"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Aucun événement d\'aujourd\'hui!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Non noté"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Aucun lieu spécifié"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Aucun étudiant"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Aucun sujet"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Aucun résumé"),
    "No description" : MessageLookupByLibrary.simpleMessage("Aucune description"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Aucun destinataire sélectionné pour l’instant"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Non noté"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Non soumis"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Pas un parent?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notifications pour les rappels sur les tâches et les événements du calendrier"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Version du SE"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observateur"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("L’une de nos autres applis pourrait mieux convenir. Tapoter sur l’une d’entre elles pour vous rendre sur l’App Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Ouvrir Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Ouvrir dans le navigateur"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Ouvrir avec une autre application"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Jumelage du code"),
    "Password" : MessageLookupByLibrary.simpleMessage("Mot de passe"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Le mot de passe est requis"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Le mot de passe doit contenir moins de 8 caractères"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Mot de passe..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Remarque du planificateur"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Veuillez saisir une adresse courriel valide"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Veuillez saisir une adresse courriel."),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Veuillez saisir votre nom complet"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Veuillez scanner un code QR généré par Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Prune, violet"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("En préparation…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Connexions précédentes"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Politique de confidentialité"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Lien vers la politique de confidentialité"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Politique de confidentialité, conditions d’utilisation, source ouverte"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Code QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("La numérisation par balayage du code QR nécessite un accès à la caméra"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Framboise rouge"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinataires"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Actualiser"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Me prévenir"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Rappels"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Les rappels ont changé!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Répondre"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Répondre à tous"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Signaler un problème"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Demander de l’aide pour se connecter"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Demander de l’aide pour le bouton de connexion"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Redémarrer l’application"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Réessayer"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Revenir à l’ouverture de session"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ÉTUDIANT"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Capture d\'écran montrant l\'emplacement de la génération de code QR dans le navigateur"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Capture d’écran montrant l’emplacement du jumelage de la génération de code QR dans l’application Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Sélectionner"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Sélectionner la couleur de l’étudiant"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Sélectionner les destinataires"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Envoyer une rétroaction"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Envoyer un message à propos de cette tâche"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Envoyer un message à propos de ce cours"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Envoyer un message"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Définissez une date et une heure afin d’être averti de cet événement."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Définissez une date et une heure afin d’être averti de cette tâche spécifique."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Paramètres"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Vert Shamrock"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Partager votre engouement pour l’application"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Afficher mot de passe"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Se connecter"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Quelque chose ne fonctionne plus, mais je peux quand même réaliser ce que je dois faire."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Arrêter d\'agir en tant qu\'utilisateur"),
    "Student" : MessageLookupByLibrary.simpleMessage("Étudiant"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Jumelage de l’étudiant"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Les étudiants peuvent créer un code QR à l’aide de l’application Canvas Student sur leur appareil mobile"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Les étudiants peuvent obtenir un code de jumelage sur le site Web de Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Objet"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Envoyé"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Envoyé avec succès!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Résumé"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Changer d’utilisateurs"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programme"),
    "TA" : MessageLookupByLibrary.simpleMessage("Instructeur-assistant"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("ENSEIGNANT"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Appuyez pour mettre dans les favori les cours que vous souhaitez voir sur le calendrier. Sélectionnez-en jusqu\'à 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Appuyez pour apparier avec un nouvel étudiant"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Appuyez pour sélectionner cet étudiant"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Appuyez pour afficher le sélecteur d’étudiant"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Enseignant"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Dites-nous quelles fonctions de l’application vous plaisent le plus"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Conditions d\'utilisation"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Lien vers les conditions d\'utilisation"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Conditions d\'utilisation"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Le code QR que vous avez scanné peut avoir expiré. Actualisez le code sur l’appareil de l’étudiant et essayez de nouveau."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Les informations suivantes nous aideront à mieux comprendre votre idée :"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Le serveur que vous avez saisi n’est pas autorisé pour cette application."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("L’étudiant que vous tentez d’ajouter appartient à une autre école. Connectez-vous ou créez un compte auprès de cette école pour scanner ce code."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("L’agent utilisateur de cette application n’est pas autorisé."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Thème"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Aucune application installée ne peut ouvrir ce fichier"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Il n’y a aucune information de page disponible."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Un problème est survenu lors du chargement des conditions d’utilisation"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Un problème est survenu lors du retrait de cet étudiant de votre compte. Veuillez vérifier votre connexion et réessayer."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des destinataires de ce cours."),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des détails de résumé de ce cours."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de cette annonce"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de cette conversation"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de ce fichier"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de votre boîte de réception."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des alertes de votre étudiant."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement du calendrier de vos étudiants."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de vos étudiants."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des cours de votre étudiant."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors de la connexion. Veuillez générer un autre code QR et réessayer."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Une erreur s\'est produite lors de la tentative d’agir en tant que cet utilisateur. Veuillez vérifier le domaine et l\'ID utilisateur et réessayer."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Il n’y a encore rien à signaler."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Cette application n’est pas autorisée à l’utilisation."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Ce cours n’a pas encore de tâches ou d’événements de calendrier."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Ce fichier n’est pas pris en charge et ne peut pas être consulté par l’application"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Cela éliminera le jumelage et supprimera toutes les inscriptions de cet étudiant de votre compte."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Note totale"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Oh oh!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Impossible de récupérer les cours. Veuillez vérifier votre connexion et réessayer."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Impossible de charger cette image"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Impossible de lire ce fichier multimédia"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Impossible d’envoyer un message. Vérifiez votre connexion puis réessayez."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("En construction"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Utilisateur inconnu"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Changements non enregistrés"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Type de fichier non pris en charge"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Téléverser le fichier"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Utiliser la caméra"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Utiliser le thème sombre dans le contenu Web"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID utilisateur"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Identifiant de l’utilisateur :"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Numéro de version"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Voir la description"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Afficher les détails de l’erreur"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Voir la politique de confidentialité"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Nous construisons actuellement cette fonctionnalité pour votre plaisir de visionnement."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Nous sommes dans l\'impossibilité d\'afficher ce lien, il peut appartenir à une institution à laquelle vous n\'êtes pas actuellement connecté."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nous n’avons pas pu trouver d’étudiant associé à ce compte"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Nous n’avons pas pu vérifier le serveur pour son utilisation avec cette application."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nous ne sommes pas sûrs de ce qui s’est passé, mais ce n’était pas bon. Contactez-nous si cela continue."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Que pourrions-nous faire plus efficacement?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Oui"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Vous n’observez aucun étudiant."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Vous pouvez seulement choisir 10 calendriers à afficher"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Vous devez saisir un ID d’utilisateur"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Vous devez saisir un nom de domaine valide"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Vous devez sélectionner au moins un calendrier à afficher."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Vous serez informé de cette tâche le…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Vous serez informé de cet événement le…"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Vous trouverez le code QR sur le Web dans votre profil de compte. Cliquez sur « \'QR for Mobile Login » (QR pour connexion mobile) dans la liste."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Vous devez ouvrir l’application Canvas Student de votre étudiant pour continuer. Allez dans Menu principal > Paramètres > Jumeler avec l’observateur et scannez le code QR que vous y voyez."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Votre code est incorrect ou expiré."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Les cours de votre étudiant peuvent ne pas être encore publiés."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Vous êtes coincé!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alertes"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendrier"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guides de Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo de Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Soutien technique de Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("réduire"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("réduit"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cours"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Comment puis-je trouver mon école ou district?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Saisir le nom de l’établissement scolaire ou du district…"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("développer"),
    "expanded" : MessageLookupByLibrary.simpleMessage("développé"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Trouver une école"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("moi"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("moins"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Suivant"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("envoyer"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("non lu"),
    "unreadCount" : m47
  };
}
