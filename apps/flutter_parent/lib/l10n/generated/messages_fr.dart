// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a fr locale. All the
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
  String get localeName => 'fr';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Note du devoir supérieure à ${threshold}";

  static m2(threshold) => "Note du devoir inférieure à ${threshold}";

  static m3(moduleName) => "Ce travail est verrouillé par le module « ${moduleName}. »";

  static m4(studentName, assignmentName) => "À propos : ${studentName}, Travaux - ${assignmentName}";

  static m5(points) => "${points} pts";

  static m6(points) => "${points} points";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} à 1 autre', other: '${authorName} à ${howMany} autres')}";

  static m8(authorName, recipientName) => "${authorName} à ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} à ${recipientName} et 1 autre', other: '${authorName} à ${recipientName} et ${howMany} autres')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} sur ${pointsPossible} points";

  static m12(studentShortName) => "pour ${studentShortName}";

  static m13(threshold) => "Note de cours supérieure à ${threshold}";

  static m14(threshold) => "Note de cours inférieure à ${threshold}";

  static m15(date, time) => "le ${date} à ${time}";

  static m16(canvasGuides, canvasSupport) => "Essayez de rechercher le nom de l’école ou du district auquel vous essayez d\'accéder, par ex. « École privée Smith » ou « Écoles du comté de Smith ». Vous pouvez également saisir directement un domaine Canvas, par ex. « smith.instructure.com ».\n\nPour de plus amples informations sur la recherche du compte Canvas de votre établissement, vous pouvez consulter le ${canvasGuides}, contacter ${canvasSupport}, ou encore contacter votre école pour recevoir de l\'aide.";

  static m17(date, time) => "Dû le ${date} à ${time}";

  static m18(studentName, eventTitle) => "À propos : ${studentName}, Événement - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Note finale : ${grade}";

  static m21(studentName) => "À propos : ${studentName}, Première page";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "À propos : ${studentName}, Notes";

  static m24(pointsLost) => "Pénalité de retard (-${pointsLost})";

  static m25(studentName, linkUrl) => "À propos : ${studentName}, ${linkUrl}";

  static m26(percentage) => "Doit être supérieur à ${percentage}";

  static m27(percentage) => "Doit être inférieur à ${percentage}";

  static m28(month) => "Mois suivant : ${month}";

  static m29(date) => "La semaine suivante démarre le ${date}";

  static m30(query) => "Impossible de trouver une école correspondant à « ${query} »";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Sur 1 point', other: 'Sur ${points} points')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} points possibles";

  static m34(month) => "Mois précédent : ${month}";

  static m35(date) => "La semaine précédente démarrait le ${date}";

  static m36(month) => "Mois de ${month}";

  static m37(date, time) => "Ce travail a été soumis le ${date} à ${time} et attend sa note.";

  static m38(studentName) => "À propos : ${studentName}, Programme";

  static m39(count) => "${count} non lu";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Une description est requise."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Un sujet est requis."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Ajouter un élève"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Ajouter une pièce jointe"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Ajouter un nouvel élève"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Ajouter un élève avec..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Paramètres d\'alertes"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("M\'alerter quand..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Toutes les périodes de notation"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Une adresse e-mail est requise."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors de la tentative d’affichage de ce lien"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Une erreur inattendue s\'est produite"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Version de l’OS Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Apparence"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Version de l\'application"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Êtes-vous un élève ou bien un enseignant ?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Voulez-vous vraiment vous déconnecter ?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Voulez-vous vraiment fermer cette page ? Votre message n\'a pas été envoyé et sera perdu."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Informations sur le devoir"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Note du devoir au dessus de"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Note du devoir en dessous de"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Devoir manquant"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendriers"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Annuler"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Élève"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Enseignant"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas sur GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Sélectionnez un cours à envoyer en message"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Choisir dans la galerie"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Terminé"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Contacter l’assistance"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Annonce de cours"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Annonces de cours"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Note du cours au dessus de"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Note du cours en dessous de"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mode de couleur foncée"),
    "Date" : MessageLookupByLibrary.simpleMessage("Date"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Supprimer"),
    "Description" : MessageLookupByLibrary.simpleMessage("Description"),
    "Device" : MessageLookupByLibrary.simpleMessage("Appareil"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modèle d\'appareil"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domaine :"),
    "Done" : MessageLookupByLibrary.simpleMessage("Terminé"),
    "Download" : MessageLookupByLibrary.simpleMessage("Télécharger"),
    "Due" : MessageLookupByLibrary.simpleMessage("À rendre le"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("URGENCE CRITIQUE EXTRÊME !!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adresse électronique"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Courriel :"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Saisissez le code d’appariement élève qui vous a été transmis. Si le code ne fonctionne pas, il a peut-être expiré."),
    "Event" : MessageLookupByLibrary.simpleMessage("Événement"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Excusé"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Échec. Appuyez pour les options"),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtre"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrer par"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Première page"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Message d’erreur complet"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Note"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Note en pourcentage"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Noté"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Notes"),
    "Help" : MessageLookupByLibrary.simpleMessage("Aide"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Mode contraste élevé"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Comment cela vous affecte-t-il ?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Je ne peux plus rien faire jusqu\'à ce que vous me contactiez."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("J\'ai besoin d\'aide mais ce n\'est pas urgent."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("J\'ai du mal à me connecter"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idée pour l\'application Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Boîte de réception"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Boîte de réception Zéro"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incomplet"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Annonce d\'institution"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Annonces de l\'institution"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instructions"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Voilà une bien belle journée pour se reposer, se relaxer et faire le plein d\'énergie."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Il semble qu\'aucun travail n’a été créé à cet endroit pour le moment."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Juste une question informelle, un commentaire, une idée, une suggestion..."),
    "Late" : MessageLookupByLibrary.simpleMessage("En retard"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lancer l\'outil externe"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Légal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Mode de couleur claire"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Erreur de lien"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Langue :"),
    "Location" : MessageLookupByLibrary.simpleMessage("Lieu"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Verrouillé"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Se déconnecter"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Gérer les élèves"),
    "Message" : MessageLookupByLibrary.simpleMessage("Message"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Sujet du message"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Manquant"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Doit être inférieur à 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Erreur de réseau"),
    "Never" : MessageLookupByLibrary.simpleMessage("Jamais"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nouveau message"),
    "No" : MessageLookupByLibrary.simpleMessage("Non"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Pas d’alerte"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Aucun travail"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Aucun cours"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Pas de date limite de rendu"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Aucun événement aujourd\'hui !"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Aucune note"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Aucun lieu spécifié"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Pas d\'étudiants"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Aucun sujet"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Pas de résumé"),
    "No description" : MessageLookupByLibrary.simpleMessage("Aucune description"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Aucun destinataire sélectionné"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Non noté"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Non soumis"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Vous n\'êtes pas un parent ?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notifications de rappels de travaux et d\'événement de calendrier"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Version de l\'OS"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observateur"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Une autre de nos applications conviendrait sans doute davantage. Appuyez une fois pour vous rendre sur le Play Store"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Ouvrir dans le navigateur"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Ouvrir avec une autre application"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Code d\'appariement"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Préparation..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Identifiants précédents"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Politique de confidentialité"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Vie privée, conditions d’utilisation, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR Code"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinataires"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Me le rappeler"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Rappels"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Répondre"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Répondre à tous"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Signaler un problème"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Demander une assistance pour la connexion"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Bouton « Demander une assistance pour la connexion »"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Redémarrer l’application"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Réessayer"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Retourner à la page de connexion"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ÉLÈVE"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Sélectionner des destinataires"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Envoyer un message à propos de ce travail"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Envoyer un message à propos de ce cours"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Envoyer un message"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Paramétrez une date et une heure à laquelle vous notifier de cet événement."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Paramétrez une date et une heure à laquelle être informé de ce travail spécifique."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Bouton rappel planifié"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Paramètres"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Partager votre engouement pour l\'application"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Quelque chose ne fonctionne plus mais je peux quand même réaliser ce que je dois faire."),
    "Student" : MessageLookupByLibrary.simpleMessage("Élève"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Sujet"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Soumis"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Soumission effectuée avec succès !"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Résumé"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Changer d\'utilisateurs"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programme"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("ENSEIGNANT"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Appuyez pour mettre en favoris les cours que vous souhaitez afficher sur le calendrier."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Appuyez pour apparier à un nouvel élève"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Appuyez pour sélectionner cet élève"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Appuyez pour afficher le sélecteur d\'élève"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Enseignant"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Dites-nous quelles fonctions de l\'application vous plaisent le plus"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Conditions d’utilisation"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Les informations suivantes nous aideront à mieux comprendre votre idée :"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Le serveur que vous avez saisi n\'est pas autorisé pour cette application."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Cet agent utilisateur pour cette application n’est pas autorisé."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Thème"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Aucune application installée ne sait ouvrir ce fichier"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Il n\'y a aucune information de page disponible."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Un problème est survenu lors du chargement des Conditions d’utilisation."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des destinataires de ce cours."),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement du résumé de ce cours."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de cette annonce."),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de cette conversation."),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue au chargement de ce fichier"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des messages de votre boîte de réception."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des alertes de votre élève."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement du calendrier d\'élève"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de vos élèves."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des cours de l’élève."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Nous n’avons rien à vous notifier."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Cette application n\'est pas autorisée à l\'utilisation."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Ce cours ne possède pas encore de travaux ni d\'événements de calendrier."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Ce fichier n’est pas pris en charge et ne peut être visualisé depuis l’application."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Note totale"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Euh... oups !"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Impossible de récupérer les cours. Veuillez vérifier votre et réessayer."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Impossible de charger cette image"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Impossible de lire ce fichier multimédia"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Impossible d’envoyer le message. Vérifiez votre connexion, puis réessayez."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("En construction"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Utilisateur inconnu"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Changements non enregistrés"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Fichier non pris en charge"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Envoyer fichier"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Utiliser l’appareil photo"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID d\'utilisateur"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Numéro de version"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Afficher les détails de l’erreur"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Nous sommes actuellement en train de travailler sur cette fonctionnalité, rien que pour vous."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Nous n’avons pas pu afficher ce lien. Il appartient peut-être à un établissement auquel vous n’êtes pas connecté."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nous n’avons pas trouvé d\'élève associé à ce compte"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Nous n’avons pas pu vérifier que ce serveur est autorisé à utiliser cette application."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("On ne sait pas trop ce qui s’est passé, mais ça a mal fini. Contactez-nous si le problème persiste."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Oui"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Vous n’observez aucun élève."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Vous serez prévenu de ce travail le..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Vous serez notifié de cet événement le..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Votre code n’est pas correct, ou bien il a expiré."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Il est possible que les cours de l\'élève n\'aient pas encore été publiés."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Vous êtes à jour !"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Agenda"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guides de Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Assistance Canvas"),
    "collapse" : MessageLookupByLibrary.simpleMessage("réduire"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("réduit"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cours"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("Ignorer"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Comment trouver mon école ou mon district ?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Saisissez le district ou le nom de l\'école..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("étendre"),
    "expanded" : MessageLookupByLibrary.simpleMessage("étendu"),
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
