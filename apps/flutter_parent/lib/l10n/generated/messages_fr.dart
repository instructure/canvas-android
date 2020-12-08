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

  static m0(userName) => "Vous agissez en tant que ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Note du devoir supérieure à ${threshold}";

  static m3(threshold) => "Note du devoir inférieure à ${threshold}";

  static m4(moduleName) => "Ce travail est verrouillé par le module « ${moduleName}. »";

  static m5(studentName, assignmentName) => "À propos : ${studentName}, Travaux - ${assignmentName}";

  static m6(points) => "${points} pts";

  static m7(points) => "${points} points";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} à 1 autre', other: '${authorName} à ${howMany} autres')}";

  static m9(authorName, recipientName) => "${authorName} à ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} à ${recipientName} et 1 autre', other: '${authorName} à ${recipientName} et ${howMany} autres')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Changer la couleur à ${studentName}";

  static m13(score, pointsPossible) => "${score} sur ${pointsPossible} points";

  static m14(studentShortName) => "pour ${studentShortName}";

  static m15(threshold) => "Note de cours supérieure à ${threshold}";

  static m16(threshold) => "Note de cours inférieure à ${threshold}";

  static m17(date, time) => "le ${date} à ${time}";

  static m18(alertTitle) => "Rejeter ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Essayez de rechercher le nom de l’école ou du district auquel vous essayez d\'accéder, par ex. « École privée Smith » ou « Écoles du comté de Smith ». Vous pouvez également saisir directement un domaine Canvas, par ex. « smith.instructure.com ».\n\nPour de plus amples informations sur la recherche du compte Canvas de votre établissement, vous pouvez consulter le ${canvasGuides}, contacter ${canvasSupport}, ou encore contacter votre école pour recevoir de l\'aide.";

  static m20(date, time) => "Dû le ${date} à ${time}";

  static m21(userName) => "Vous n’agirez plus en tant que ${userName} et serez déconnecté.";

  static m22(userName) => "Vous n’agirez plus en tant que ${userName} et reprendrez votre compte original.";

  static m23(studentName, eventTitle) => "À propos : ${studentName}, Événement - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Note finale : ${grade}";

  static m26(studentName) => "À propos : ${studentName}, Première page";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "À propos : ${studentName}, Notes";

  static m29(pointsLost) => "Pénalité de retard (-${pointsLost})";

  static m30(studentName, linkUrl) => "À propos : ${studentName}, ${linkUrl}";

  static m31(percentage) => "Doit être supérieur à ${percentage}";

  static m32(percentage) => "Doit être inférieur à ${percentage}";

  static m33(month) => "Mois suivant : ${month}";

  static m34(date) => "La semaine suivante démarre le ${date}";

  static m35(query) => "Impossible de trouver une école correspondant à « ${query} »";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Sur 1 point', other: 'Sur ${points} points')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} points possibles";

  static m39(month) => "Mois précédent : ${month}";

  static m40(date) => "La semaine précédente démarrait le ${date}";

  static m41(termsOfService, privacyPolicy) => "En cliquant sur \"Créer un compte\", vous acceptez les ${termsOfService} et confirmez la ${privacyPolicy}";

  static m42(version) => "Suggestions pour Android - Canvas Parent ${version}";

  static m43(month) => "Mois de ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} étoile', other: '${position} étoiles')}";

  static m45(date, time) => "Ce travail a été soumis le ${date} à ${time} et attend sa note.";

  static m46(studentName) => "À propos : ${studentName}, Programme";

  static m47(count) => "${count} non lu";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Agir comme\" permet essentiellement de se connecter en tant qu\'utilisateur sans mot de passe. Vous pourrez effectuer toutes les actions que vous voulez comme si vous étiez cet utilisateur et, vu des autres utilisateurs, tout paraîtra comme si c’était cet utilisateur qui avait agi. Cependant, les journaux d’audit enregistrent le fait que c’est vous qui avez effectué ces actions au nom de l’utilisateur."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Une description est requise."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Une erreur réseau est survenue lors de l\'ajout de l’élève. Vérifiez votre connexion, puis réessayez."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Un sujet est requis."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Agir en tant qu\'utilisateur"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Ajouter un élève"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Ajouter une pièce jointe"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Ajouter un nouvel élève"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Ajouter un élève avec..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Paramètres d\'alertes"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("M\'alerter quand..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Toutes les périodes de notation"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Vous possédez déjà un compte ? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Une adresse e-mail est requise."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors de la tentative d’affichage de ce lien"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue pendant l’enregistrement de votre sélection. Veuillez réessayer."),
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
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, Fuschia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendriers"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Autorisations d\'appareil photo"),
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
    "Create Account" : MessageLookupByLibrary.simpleMessage("Créer un compte"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Mode de couleur foncée"),
    "Date" : MessageLookupByLibrary.simpleMessage("Date"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Supprimer"),
    "Description" : MessageLookupByLibrary.simpleMessage("Description"),
    "Device" : MessageLookupByLibrary.simpleMessage("Appareil"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modèle d\'appareil"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domaine"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domaine :"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Ne plus montrer à nouveau"),
    "Done" : MessageLookupByLibrary.simpleMessage("Terminé"),
    "Download" : MessageLookupByLibrary.simpleMessage("Télécharger"),
    "Due" : MessageLookupByLibrary.simpleMessage("À rendre le"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("URGENCE CRITIQUE EXTRÊME !!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Électrique, bleu"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adresse électronique"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Courriel :"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Courriel..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Saisissez le code d’appariement élève qui vous a été transmis. Si le code ne fonctionne pas, il a peut-être expiré."),
    "Event" : MessageLookupByLibrary.simpleMessage("Événement"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Excusé"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Code QR expiré"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Échec. Appuyez pour les options"),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtre"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrer par"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Feu, Orange"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Première page"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Nom complet"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Nom complet..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Message d’erreur complet"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Aller à « Aujourd\'hui »"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Note"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Note en pourcentage"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Noté"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Notes"),
    "Help" : MessageLookupByLibrary.simpleMessage("Aide"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Cacher le mot de passe"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Mode contraste élevé"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Comment nous débrouillons-nous ?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Comment cela vous affecte-t-il ?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Je ne peux plus rien faire jusqu\'à ce que vous me contactiez."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Je n’ai pas de compte Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("J’ai un compte Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("J\'ai besoin d\'aide mais ce n\'est pas urgent."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("J\'ai du mal à me connecter"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idée pour l\'application Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Afin de vous fournir une meilleure expérience utilisateur, nous avons changé la façon dont les rappels fonctionnent : Vous pouvez ajouter de nouveaux rappels en affichant un travail ou un événement de calendrier, puis en appuyant sur l’interrupteur situé dans la section « Rappelez-moi » (Remind Me).\n\nAttention, tout rappel créé avec d\'anciennes versions de l’application sera incompatible avec les nouveaux changements. Vous devrez donc créer à nouveau ces rappels."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Boîte de réception"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Boîte de réception Zéro"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incomplet"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Domaine incorrect"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Annonce d\'institution"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Annonces de l\'institution"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instructions"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Les interactions sur cette page sont limités par votre institution."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Code QR non valide"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Voilà une bien belle journée pour se reposer, se relaxer et faire le plein d\'énergie."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Il semble qu\'aucun travail n’a été créé à cet endroit pour le moment."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Juste une question informelle, un commentaire, une idée, une suggestion..."),
    "Late" : MessageLookupByLibrary.simpleMessage("En retard"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lancer l\'outil externe"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Légal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Mode de couleur claire"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Erreur de lien"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Langue :"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Localiser un code QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Lieu"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Verrouillé"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Se déconnecter"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Flux d’identification : Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Flux d’identification : Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Flux d’identification : Administrateur du site"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Flux d’identification : Passer la vérification par mobile"),
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
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Ouvrir Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Ouvrir dans le navigateur"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Ouvrir avec une autre application"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Code d\'appariement"),
    "Password" : MessageLookupByLibrary.simpleMessage("Mot de passe"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Un mot de passe est requis."),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Le mot de passe doit contenir au moins 8 caractères."),
    "Password…" : MessageLookupByLibrary.simpleMessage("Mot de passe..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Note à l\'intention du planificateur"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Veuillez saisir une adresse électronique valide"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Veuillez saisir une adresse email"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Veuillez saisir un nom complet."),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Veuillez scanner un code QR généré par Canvas."),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Prune, violet"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Préparation..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Identifiants précédents"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Politique de confidentialité"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Lien vers la Politique de confidentialité"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Vie privée, conditions d’utilisation, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR Code"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Le scan de code QR nécessite l’accès à l\'appareil photo."),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Framboise, Rouge"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinataires"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Actualiser"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Me le rappeler"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Rappels"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Les rappels ont été modifiés !"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Répondre"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Répondre à tous"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Signaler un problème"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Demander une assistance pour la connexion"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Bouton « Demander une assistance pour la connexion »"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Redémarrer l’application"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Réessayer"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Retourner à la page de connexion"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ÉLÈVE"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Capture d\'écran montrant l’emplacement du générateur de code QR dans le navigateur."),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Capture d\'écran montrant l’emplacement où est généré le code QR de jumelage dans l’application Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Sélectionner"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Sélectionner la couleur de l’élève"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Sélectionner des destinataires"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Envoyer un avis"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Envoyer un message à propos de ce travail"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Envoyer un message à propos de ce cours"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Envoyer un message"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Paramétrez une date et une heure à laquelle vous notifier de cet événement."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Paramétrez une date et une heure à laquelle être informé de ce travail spécifique."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Paramètres"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Trèfle, vert"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Partager votre engouement pour l\'application"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Afficher le mot de passe"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Se connecter"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Un problème est survenu lors de la création de votre compte, veuillez contacter votre école pour obtenir de l\'aide."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Quelque chose ne fonctionne plus mais je peux quand même réaliser ce que je dois faire."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Cesser d\'agir en tant qu\'utilisateur"),
    "Student" : MessageLookupByLibrary.simpleMessage("Élève"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Jumelage élève"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Les élèves peuvent créer un code QR à l’aide de l’application Canvas Student sur leur appareil mobile."),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Les élèves peuvent obtenir un code de jumelage via le site Web de Canvas."),
    "Subject" : MessageLookupByLibrary.simpleMessage("Sujet"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Soumis"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Soumission effectuée avec succès !"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Résumé"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Changer d\'utilisateurs"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programme"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("ENSEIGNANT"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Appuyez pour mettre en favoris les cours que vous souhaitez afficher sur le calendrier. Sélectionnez-en jusqu\'à 10"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Appuyez pour apparier à un nouvel élève"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Appuyez pour sélectionner cet élève"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Appuyez pour afficher le sélecteur d\'élève"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Enseignant"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Dites-nous quelles fonctions de l\'application vous plaisent le plus"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Conditions d’utilisation"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Lien vers les Conditions d’utilisation"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Conditions d’utilisation"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Le code QR que vous avez scanné a probablement expiré. Veuillez réactualiser le code depuis l’appareil de l\'élève, puis réessayez."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Les informations suivantes nous aideront à mieux comprendre votre idée :"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Le serveur que vous avez saisi n\'est pas autorisé pour cette application."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("L\'élève que vous essayez d\'ajouter appartient à une autre école. Connectez-vous ou créez un compte auprès de cette école pour scanner ce code."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Cet agent utilisateur pour cette application n’est pas autorisé."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Thème"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Aucune application installée ne sait ouvrir ce fichier"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Il n\'y a aucune information de page disponible."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Un problème est survenu lors du chargement des Conditions d’utilisation."),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Un problème est survenu lors de la suppression de l\'élève de votre compte. Veuillez vérifier votre connexion, puis réessayez."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des destinataires de ce cours."),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement du résumé de ce cours."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de cette annonce."),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de cette conversation."),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue au chargement de ce fichier"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des messages de votre boîte de réception."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des alertes de votre élève."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement du calendrier d\'élève"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement de vos élèves."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors du chargement des cours de l’élève."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue pendant la connexion. Veuillez générer un autre code QR, puis réessayez."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Une erreur est survenue lors de la tentative d\'agir au nom de cet utilisateur. Veuillez vérifier le domaine et l’identifiant utilisateur, puis réessayez."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Nous n’avons rien à vous notifier."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Cette application n\'est pas autorisée à l\'utilisation."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Ce cours ne possède pas encore de travaux ni d\'événements de calendrier."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Ce fichier n’est pas pris en charge et ne peut être visualisé depuis l’application."),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Cette action annulera le jumelage et supprimera toutes les inscriptions pour cet élève de votre compte."),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Utiliser le thème sombre pour le contenu Web."),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID utilisateur"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID d\'utilisateur"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Numéro de version"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Afficher la description"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Afficher les détails de l’erreur"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Afficher la politique de confidentialité"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Nous sommes actuellement en train de travailler sur cette fonctionnalité, rien que pour vous."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Nous n’avons pas pu afficher ce lien. Il appartient peut-être à un établissement auquel vous n’êtes pas connecté."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nous n’avons pas trouvé d\'élève associé à ce compte"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Nous n’avons pas pu vérifier que ce serveur est autorisé à utiliser cette application."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("On ne sait pas trop ce qui s’est passé, mais ça a mal fini. Contactez-nous si le problème persiste."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("En quoi pouvons-nous nous améliorer ?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Oui"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Vous n’observez aucun élève."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Vous pouvez seulement choisir 10 calendriers à afficher"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Vous devez entrer un identifiant utilisateur."),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Vous devez entrer un domaine valide."),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Vous devez sélectionner au moins un calendrier à afficher."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Vous serez prévenu de ce travail le..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Vous serez notifié de cet événement le..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Vous trouverez le code QR sur le Web dans votre profil. Cliquez sur « QR pour connexion mobile » dans la liste."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Vous devrez ouvrir votre application pour élèves Canvas Student pour continuer. Rendez-vous dans Menu Principal > Paramètres > Jumelage avec un observateur et scannez le code QR qui y apparaît."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Votre code n’est pas correct, ou bien il a expiré."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Il est possible que les cours de l\'élève n\'aient pas encore été publiés."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Vous êtes à jour !"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Agenda"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guides de Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Assistance Canvas"),
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
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Comment trouver mon école ou mon district ?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Saisissez le district ou le nom de l\'école..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("étendre"),
    "expanded" : MessageLookupByLibrary.simpleMessage("étendu"),
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
