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

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Note de la tâche supérieure à ${threshold}";

  static m2(threshold) => "Note de la tâche inférieure à ${threshold}";

  static m3(moduleName) => "Cette tâche est verrouillée par le module « ${moduleName} ».";

  static m4(studentName, assignmentName) => "En ce qui concerne : ${studentName}, tâche — ${assignmentName}";

  static m5(points) => "${points} pts";

  static m6(points) => "${points} points";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} à un autre', other: '${authorName} à ${howMany} autres')}";

  static m8(authorName, recipientName) => "${authorName} à ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} à ${recipientName} et un autre', other: '${authorName} à ${recipientName} et ${howMany} autres')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} de ${pointsPossible} points";

  static m12(studentShortName) => "pour ${studentShortName}";

  static m13(threshold) => "Note du cours supérieure à ${threshold}";

  static m14(threshold) => "Note du cours inférieure à ${threshold}";

  static m15(date, time) => "${date} à ${time}";

  static m16(canvasGuides, canvasSupport) => "Essayez de rechercher le nom de l’école ou du district auquel vous tentez d’accéder, comme « École privée Smith » ou « Écoles régionales Smith ». Vous pouvez également saisir un domaine Canvas directement, comme « smith.instructure.com. »\n\nPour obtenir de plus amples renseignements sur la façon de trouver le compte Canvas de votre établissement, vous pouvez consulter ${canvasGuides}, communiquer avec ${canvasSupport}, ou contacter votre école pour obtenir de l’aide.";

  static m17(date, time) => "Dû le ${date} à ${time}";

  static m18(studentName, eventTitle) => "En ce qui concerne : ${studentName}, événement — ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} — ${endAt}";

  static m20(grade) => "Note finale : ${grade}";

  static m21(studentName) => "En ce qui concerne : ${studentName}, page de couverture";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "En ce qui concerne : ${studentName}, notes";

  static m24(pointsLost) => "Pénalité de retard (-${pointsLost})";

  static m25(studentName, linkUrl) => "En ce qui concerne : ${studentName}, ${linkUrl}";

  static m26(percentage) => "Doit être supérieure à ${percentage}";

  static m27(percentage) => "Doit être inférieure à ${percentage}";

  static m28(month) => "Mois suivant : ${month}";

  static m29(date) => "Semaine prochaine commençant le ${date}";

  static m30(query) => "Impossible de trouver les écoles correspondant à « ${query} »";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Sur 1 point', other: 'Sur ${points} points')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} points possibles";

  static m34(month) => "Mois précédent : ${month}";

  static m35(date) => "Semaine précédente commençant le ${date}";

  static m36(month) => "Mois de ${month}";

  static m37(date, time) => "Cette tâche a été soumise le ${date} à ${time} et attend d’être notée";

  static m38(studentName) => "En ce qui concerne : ${studentName}, programme";

  static m39(count) => "${count} non lu";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Description obligatoire."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Un objet est requis."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Ajouter un étudiant"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Ajouter une pièce jointe"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Ajouter un nouvel étudiant"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Ajouter étudiant avec…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Réglages de l’alerte"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("M’alerter lorsque…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Toutes les périodes de notation"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Adresse de courriel nécessaire."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Une erreur s\'est produite au moment de l’affichage de ce lien"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendriers"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mode de couleur foncée"),
    "Date" : MessageLookupByLibrary.simpleMessage("Date"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Supprimer"),
    "Description" : MessageLookupByLibrary.simpleMessage("Description"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositif"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modèle de l’appareil"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domaine :"),
    "Done" : MessageLookupByLibrary.simpleMessage("Terminé"),
    "Download" : MessageLookupByLibrary.simpleMessage("Télécharger"),
    "Due" : MessageLookupByLibrary.simpleMessage("Échéance"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("ÉTAT D’URGENCE EXTRÊMEMENT SÉRIEUX!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adresse de courriel"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Courriel :"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Saisir le code d’appariement de l’étudiant qui vous a été fourni. Si le code d’appariement ne fonctionne pas, il peut avoir expiré"),
    "Event" : MessageLookupByLibrary.simpleMessage("Événement"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Exempté"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Échec. Appuyez pour des options."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtre"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrer par"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Page de couverture"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Message d’erreur complet"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Note"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Pourcentage de notes"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Noté"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Notes"),
    "Help" : MessageLookupByLibrary.simpleMessage("Aide"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Mode haut contraste"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Comment cela vous affecte-t-il?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Je ne peux plus rien faire jusqu’à ce que vous me contactiez."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("J’ai besoin d’aide, mais ce n’est pas urgent"),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("J’ai des problèmes à me connecter"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idée pour l’application Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Boîte de réception"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Boîte de réception Zéro"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incomplet"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Annonce de l’institution"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Annonces de l’institution"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instructions"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("C\'est une belle journée pour se reposer, se détendre et recharger nos batteries."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Il semblerait qu’il n’y ait pas encore de tâche créée dans cet espace."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Juste une question informelle, commentaire, idée, suggestion…"),
    "Late" : MessageLookupByLibrary.simpleMessage("En retard"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lancer l\'outil externe"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Légal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Mode Jour"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Erreur de lien"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Paramètres régionaux :"),
    "Location" : MessageLookupByLibrary.simpleMessage("Emplacement"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Verrouillé"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Déconnexion"),
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
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Ouvrir dans le navigateur"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Ouvrir avec une autre application"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Jumelage du code"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("En préparation…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Connexions précédentes"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Politique de confidentialité"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Politique de confidentialité, conditions d’utilisation, source ouverte"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Code QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinataires"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Me prévenir"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Rappels"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Répondre"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Répondre à tous"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Signaler un problème"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Demander de l’aide pour se connecter"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Demander de l’aide pour le bouton de connexion"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Redémarrer l’application"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Réessayer"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Revenir à l’ouverture de session"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ÉTUDIANT"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Sélectionner les destinataires"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Envoyer un message à propos de cette tâche"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Envoyer un message à propos de ce cours"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Envoyer un message"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Définissez une date et une heure afin d’être averti de cet événement."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Définissez une date et une heure afin d’être averti de cette tâche spécifique."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Activer la balise de rappel planifié"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Paramètres"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Partager votre engouement pour l’application"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Quelque chose ne fonctionne plus, mais je peux quand même réaliser ce que je dois faire."),
    "Student" : MessageLookupByLibrary.simpleMessage("Étudiant"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Objet"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Envoyé"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Envoyé avec succès!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Résumé"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Changer d’utilisateurs"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programme"),
    "TA" : MessageLookupByLibrary.simpleMessage("Instructeur-assistant"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("ENSEIGNANT"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Appuyez pour mettre dans les favori les cours que vous souhaitez voir sur le calendrier."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Appuyez pour apparier avec un nouvel étudiant"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Appuyez pour sélectionner cet étudiant"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Appuyez pour afficher le sélecteur d’étudiant"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Enseignant"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Dites-nous quelles fonctions de l’application vous plaisent le plus"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Conditions d\'utilisation"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Les informations suivantes nous aideront à mieux comprendre votre idée :"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Le serveur que vous avez saisi n’est pas autorisé pour cette application."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("L’agent utilisateur de cette application n’est pas autorisé."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Thème"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Aucune application installée ne peut ouvrir ce fichier"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Il n’y a aucune information de page disponible."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Un problème est survenu lors du chargement des conditions d’utilisation"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des destinataires de ce cours."),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des détails de résumé de ce cours."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de cette annonce"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de cette conversation"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de ce fichier"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de votre boîte de réception."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des alertes de votre étudiant."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement du calendrier de vos étudiants."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de vos étudiants."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des cours de votre étudiant."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Il n’y a encore rien à signaler."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Cette application n’est pas autorisée à l’utilisation."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Ce cours n’a pas encore de tâches ou d’événements de calendrier."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Ce fichier n’est pas pris en charge et ne peut pas être consulté par l’application"),
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
    "User ID:" : MessageLookupByLibrary.simpleMessage("Identifiant de l’utilisateur :"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Numéro de version"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Afficher les détails de l’erreur"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Nous construisons actuellement cette fonctionnalité pour votre plaisir de visionnement."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Nous sommes dans l\'impossibilité d\'afficher ce lien, il peut appartenir à une institution à laquelle vous n\'êtes pas actuellement connecté."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nous n’avons pas pu trouver d’étudiant associé à ce compte"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Nous n’avons pas pu vérifier le serveur pour son utilisation avec cette application."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nous ne sommes pas sûrs de ce qui s’est passé, mais ce n’était pas bon. Contactez-nous si cela continue."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Oui"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Vous n’observez aucun étudiant."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Vous serez informé de cette tâche le…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Vous serez informé de cet événement le…"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Votre code est incorrect ou expiré."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Les cours de votre étudiant peuvent ne pas être encore publiés."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Vous êtes coincé!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alertes"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendrier"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guides de Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo de Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Soutien technique de Canvas"),
    "collapse" : MessageLookupByLibrary.simpleMessage("réduire"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("réduit"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cours"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("ignorer"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Comment puis-je trouver mon école ou district?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Saisir le nom de l’établissement scolaire ou du district…"),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("développer"),
    "expanded" : MessageLookupByLibrary.simpleMessage("développé"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Trouver une école"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("moi"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("moins"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Suivant"),
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
    "send" : MessageLookupByLibrary.simpleMessage("envoyer"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("non lu"),
    "unreadCount" : m39
  };
}
