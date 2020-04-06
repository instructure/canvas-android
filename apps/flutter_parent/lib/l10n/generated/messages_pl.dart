// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a pl locale. All the
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
  String get localeName => 'pl';

  static m0(version) => "wer. ${version}";

  static m1(threshold) => "Ocena za zadanie powyżej ${threshold}";

  static m2(threshold) => "Ocena za zadanie poniżej ${threshold}";

  static m3(moduleName) => "To zadanie jest zablokowane przez moduł \"${moduleName}\".";

  static m4(studentName, assignmentName) => "W odniesieniu do: ${studentName}, zadanie - ${assignmentName}";

  static m5(points) => "${points} pkt";

  static m6(points) => "${points} punkty";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} do 1 innego', other: '${authorName} do ${howMany} innych')}";

  static m8(authorName, recipientName) => "${authorName} do ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} do ${recipientName} oraz 1 innego', other: '${authorName} do ${recipientName} oraz ${howMany} innych')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} z ${pointsPossible} pkt";

  static m12(studentShortName) => "dla ${studentShortName}";

  static m13(threshold) => "Ocena z kursu powyżej ${threshold}";

  static m14(threshold) => "Ocena z kursu poniżej ${threshold}";

  static m15(date, time) => "${date} o ${time}";

  static m16(canvasGuides, canvasSupport) => "Spróbuj wyszukać nazwę szkoły lub okręg, do których chcesz uzyskać dostęp, np. Prywatna Szkoła im. Adama Smitha albo szkoły z okręgu mazowieckiego. Można także wpisać bezpośrednio domenę Canvas, np. smith.instructure.com.\n\nAby uzyskać więcej informacji o wyszukiwaniu konta Canvas instytucji, odwiedź ${canvasGuides} lub skontaktuj się z ${canvasSupport} bądź ze swoją szkołą.";

  static m17(date, time) => "Termin ${date} o ${time}";

  static m18(studentName, eventTitle) => "W odniesieniu do: ${studentName}, wydarzenie - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Ocena końcowa: ${grade}";

  static m21(studentName) => "W odniesieniu do: ${studentName}, pierwsza strona";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "W odniesieniu do: ${studentName}, oceny";

  static m24(pointsLost) => "Kara za spóźnienie (-${pointsLost})";

  static m25(studentName, linkUrl) => "W odniesieniu do: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Wymagane powyżej ${percentage}";

  static m27(percentage) => "Wymagane poniżej ${percentage}";

  static m28(month) => "Następny miesiąc: ${month}";

  static m29(date) => "Następny tydzień od ${date}";

  static m30(query) => "Nie można znaleźć szkół spełniających kryterium \"${query}\"";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Z 1 pkt', other: 'Z ${points} pkt')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} pkt do zdobycia";

  static m34(month) => "Poprzedni miesiąc: ${month}";

  static m35(date) => "Poprzedni tydzień od ${date}";

  static m36(month) => "Miesiąc: ${month}";

  static m37(date, time) => "To zadanie zostało przesłane w dniu ${date} o godz. ${time} i oczekuje na ocenę";

  static m38(studentName) => "W odniesieniu do: ${studentName}, program";

  static m39(count) => "${count} nieprzeczytane";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Wymagany jest opis."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Wymagany jest temat."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Dodaj ucznia"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Dodaj załącznik"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Dodaj nowego uczestnika"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Dodaj uczestnika z..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Ustawienia alertów"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Powiadom mnie, gdy..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Wszystkie okresy oceniania"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Wymagany jest adres e-mail."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wyświetlania tego łącza"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Wystąpił nieoczekiwany błąd"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Wersja systemu Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Wygląd"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Wersja aplikacji"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Jesteś uczestnikiem czy nauczycielem?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Czy na pewno chcesz się wylogować?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Czy na pewno chcesz zamknąć tę stronę? Niewysłana wiadomość zostanie utracona."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Szczegóły zadania"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Ocena za zadanie powyżej"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Ocena za zadanie poniżej"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Brak zadania"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendarze"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Anuluj"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Uczestnik Canvas"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas na GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Wybierz kurs do przesłania"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Wybierz z galerii"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Ukończony"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Skontaktuj się z działem wsparcia"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Ogłoszenie kursu"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Ogłoszenia dotyczące kursu"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Ocena z kursu powyżej"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Ocena z kursu poniżej"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Tryb ciemnego tła"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Usuń"),
    "Description" : MessageLookupByLibrary.simpleMessage("Opis"),
    "Device" : MessageLookupByLibrary.simpleMessage("Urządzenie"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Model urządzenia"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domena:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Gotowe"),
    "Download" : MessageLookupByLibrary.simpleMessage("Pobierz"),
    "Due" : MessageLookupByLibrary.simpleMessage("Termin"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("KRYTYCZNA SYTUACJA AWARYJNA!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adres e-mail"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Adres e-mail:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Wpisz otrzymany kod parowania uczestnika. Jeśli kod parowania nie działa, mógł wygasnąć"),
    "Event" : MessageLookupByLibrary.simpleMessage("Wydarzenie"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Usprawiedliwiony"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Nie powiodło się. Stuknij, aby wyświetlić opcje."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtruj"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtruj wg"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Pierwsza strona"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Pełny komunikat o błędzie"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Ocena"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Procent oceny"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Oceniono"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Oceny"),
    "Help" : MessageLookupByLibrary.simpleMessage("Pomoc"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Tryb wysokiego kontrastu"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("W jaki sposób to na Ciebie wpływa?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Nie mogę nie zrobić dopóki się ze mną nie skontaktujesz."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Potrzebuję pomocy ale nie jest to pilne."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Mam problem z logowaniem"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Pomysł na aplikację Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Skrzynka odbiorcza"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Skrzynka odbiorcza zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Nie ukończono"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Ogłoszenie instytucji"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Ogłoszenia instytucji"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instrukcje"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Wygląda to na świetny dzień do odpoczynku, relaksu i regeneracji."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Wygląda na to, że w tej przestrzeni nie utworzono zadań."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Jedynie przypadkowe pytanie, komentarz, pomysł, sugestię..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Późno"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Uruchom narzędzie zewnętrzne"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Informacje prawne"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Tryb jasny"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Błąd łącza"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Lokalizacja:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Lokalizacja"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Zablokowany"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Wyloguj"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Zarządzaj uczestnikami"),
    "Message" : MessageLookupByLibrary.simpleMessage("Wiadomość"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Temat wiadomości"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Brak"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Wymagane poniżej 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Błąd sieciowy"),
    "Never" : MessageLookupByLibrary.simpleMessage("Nigdy"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nowa wiadomość"),
    "No" : MessageLookupByLibrary.simpleMessage("Nie"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Brak alertów"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Brak zadań"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Brak kursów"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Brak terminu"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Brak wydarzeń na dziś!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Brak oceny"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Brak określonej lokalizacji"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Brak uczestników"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Brak tematu"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Brak podsumowania"),
    "No description" : MessageLookupByLibrary.simpleMessage("Brak opisu"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Nie wybrano odbiorców"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Nie oceniono"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Nie wysłano"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Nie jesteś rodzicem?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Powiadomienia dla przypomnień o zadaniach i wydarzeniach w kalendarzu"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Wersja OS"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Obserwujący"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Jedna z innych naszych aplikacji może być bardziej przydatna. Stuknij jedną z nich, aby odwiedzić sklep Play Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Otwórz w przeglądarce"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Otwórz w innej aplikacji"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Kod parowania"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Przygotowywanie..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Poprzednie logowania"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Polityka prywatności"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Polityka prywatności, Warunki korzystania, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Kod QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Odbiorcy"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Przypomnij mi"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Przypomnienia"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Odpowiedz"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Odpowiedz wszystkim"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Zgłoś problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Poproś o pomoc w logowaniu"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Przycisk prośby o pomoc w logowaniu"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Ponownie uruchom aplikację"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Ponów próbę"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Powrót do logowania"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("UCZESTNIK"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Wybierz odbiorców"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Wyślij wiadomość o tym zadaniu"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Wyślij wiadomość o tym kursie"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Wyślij wiadomość"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Ustaw datę i godzinę powiadomienia o tym wydarzeniu."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Ustaw datę i godzinę powiadomienia dla tego zadania."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Ustaw przełącznik przypomnienia"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Ustawienia"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Podziel się miłością do Aplikacji"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Coś się zepsuło ale jestem w stanie to obejść aby wykonać to czego potrzebuję."),
    "Student" : MessageLookupByLibrary.simpleMessage("Uczestnik"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Temat"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Przesłano"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Przesłano pomyślnie!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Podsumowanie"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Przełącz użytkownika"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Program"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("NAUCZYCIEL"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Stuknij, aby dodać do ulubionych kursy, które chcesz wyświetlić w Kalendarzu."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Stuknij, aby sparować z nowym uczestnikiem"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Stuknij, aby wybrać tego uczestnika"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Stuknij, aby wyświetlić opcję wyboru uczestników"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Nauczyciel"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Powiedz nam o twoich ulubionych częściach aplikacji"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Warunki korzystania"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Poniższe informacje pozwolą nam lepiej zrozumieć twój pomysł:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Wybrany serwer nie ma uprawnień dla tej aplikacji."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Pośrednik użytkownika dla tej aplikacji nie ma uprawnień."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Motyw"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Brak zainstalowanych aplikacji, które mogłyby otworzyć ten plik"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Brak informacji o stronie."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytywaniem Warunków korzystania"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem odbiorców dla tego kursu"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem szczegółów podsumowania dla tego kursu."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem tego ogłoszenia"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem tej rozmowy"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem tego pliku"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania wiadomości ze skrzynki odbiorczej."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania alertów dla uczestników."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania kalendarza dla uczestników"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania uczestników."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania kursów uczestnika."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Brak informacji do przekazania."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Ta aplikacja nie jest dopuszczona do użytku."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Ten kurs nie ma żadnych zadań ani wydarzeń w kalendarzu."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Ten plik nie jest obsługiwany i nie można go wyświetlić w aplikacji"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Łączna ocena"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("O, nie!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Nie udało się pobrać kursów. Sprawdź połączenie i spróbuj ponownie."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Nie można wczytać tego obrazu"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Nie można odtworzyć pliku multimediów"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Nie udało się wysłać wiadomości. Sprawdź połączenie sieciowe i spróbuj ponownie."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Strona w budowie"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Nieznany użytkownik"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Niezapisane zmiany"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Nieobsługiwany plik"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Prześlij plik"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Użyj kamery"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Identyfikator użytkownika:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Numer wersji"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Wyświetl szczegóły błędu"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Tworzymy tę funkcję dla przyjemności użytkowników."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Nie udało się wyświetlić tego łącza, może należeć do instytucji, do której nie jesteś zalogowany."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nie udało się odnaleźć uczestnika powiązanego z tym kontem"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Nie udało się zweryfikować serwera do użycia z tą aplikacją."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nie mamy pewności, co się wydarzyło, ale nie było to dobre. Jeśli to będzie się powtarzać, skontaktuj się z nami."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Tak"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Nie obserwujesz żadnych uczestników."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Otrzymasz powiadomienie o tym zadaniu w dniu..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Otrzymasz powiadomienie o tym wydarzeniu w dniu..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Twój kod jest nieprawidłowy lub wygasł."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Być może kursy uczestnika nie zostały jeszcze opublikowane."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("To już wszystko!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alerty"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Kalendarz"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Przewodniki Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Wsparcie Canvas"),
    "collapse" : MessageLookupByLibrary.simpleMessage("zwinąć"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("zwinięte"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kursy"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("odrzuć"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Jak mogę odnaleźć swoją szkołę lub okrąg?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Wpisz nazwę szkoły lub okręg..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("rozwinąć"),
    "expanded" : MessageLookupByLibrary.simpleMessage("rozwinięte"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Znajdź szkołę"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("ja"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Następny"),
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
    "send" : MessageLookupByLibrary.simpleMessage("wyślij"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("nieprzeczytane"),
    "unreadCount" : m39
  };
}
