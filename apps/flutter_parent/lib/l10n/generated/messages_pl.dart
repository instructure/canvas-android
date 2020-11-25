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

  static m0(userName) => "Podszywasz się jako ${userName}";

  static m1(version) => "wer. ${version}";

  static m2(threshold) => "Ocena za zadanie powyżej ${threshold}";

  static m3(threshold) => "Ocena za zadanie poniżej ${threshold}";

  static m4(moduleName) => "To zadanie jest zablokowane przez moduł \"${moduleName}\".";

  static m5(studentName, assignmentName) => "W odniesieniu do: ${studentName}, zadanie - ${assignmentName}";

  static m6(points) => "${points} pkt";

  static m7(points) => "${points} punkty";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} do 1 innego', other: '${authorName} do ${howMany} innych')}";

  static m9(authorName, recipientName) => "${authorName} do ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} do ${recipientName} oraz 1 innego', other: '${authorName} do ${recipientName} oraz ${howMany} innych')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Zmień kolor na ${studentName}";

  static m13(score, pointsPossible) => "${score} z ${pointsPossible} pkt";

  static m14(studentShortName) => "dla ${studentShortName}";

  static m15(threshold) => "Ocena z kursu powyżej ${threshold}";

  static m16(threshold) => "Ocena z kursu poniżej ${threshold}";

  static m17(date, time) => "${date} o ${time}";

  static m18(alertTitle) => "Odrzuć ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Spróbuj wyszukać nazwę szkoły lub okręg, do których chcesz uzyskać dostęp, np. Prywatna Szkoła im. Adama Smitha albo szkoły z okręgu mazowieckiego. Można także wpisać bezpośrednio domenę Canvas, np. smith.instructure.com.\n\nAby uzyskać więcej informacji o wyszukiwaniu konta Canvas instytucji, odwiedź ${canvasGuides} lub skontaktuj się z ${canvasSupport} bądź ze swoją szkołą.";

  static m20(date, time) => "Termin ${date} o ${time}";

  static m21(userName) => "Zatrzymasz podszywanie się jako ${userName} i nastąpi wylogowanie.";

  static m22(userName) => "Zatrzymasz podszywanie się jako ${userName} i powrócisz do swojego konta.";

  static m23(studentName, eventTitle) => "W odniesieniu do: ${studentName}, wydarzenie - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Ocena końcowa: ${grade}";

  static m26(studentName) => "W odniesieniu do: ${studentName}, pierwsza strona";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "W odniesieniu do: ${studentName}, oceny";

  static m29(pointsLost) => "Kara za spóźnienie (-${pointsLost})";

  static m30(studentName, linkUrl) => "W odniesieniu do: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Wymagane powyżej ${percentage}";

  static m32(percentage) => "Wymagane poniżej ${percentage}";

  static m33(month) => "Następny miesiąc: ${month}";

  static m34(date) => "Następny tydzień od ${date}";

  static m35(query) => "Nie można znaleźć szkół spełniających kryterium \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Z 1 pkt', other: 'Z ${points} pkt')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} pkt do zdobycia";

  static m39(month) => "Poprzedni miesiąc: ${month}";

  static m40(date) => "Poprzedni tydzień od ${date}";

  static m41(termsOfService, privacyPolicy) => "Stukając opcję Utwórz konto, zgadzasz się na ${termsOfService} i potwierdzasz ${privacyPolicy}.";

  static m42(version) => "Sugestie dla systemu Android - Canvas Parent ${version}";

  static m43(month) => "Miesiąc: ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} gwiazdka', other: '${position} gwiazdki')}";

  static m45(date, time) => "To zadanie zostało przesłane w dniu ${date} o godz. ${time} i oczekuje na ocenę";

  static m46(studentName) => "W odniesieniu do: ${studentName}, program";

  static m47(count) => "${count} nieprzeczytane";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Podszywanie\" polega na logowaniu się jako inny użytkownik bez użycia hasła. Będziesz mieć możliwość podejmowania wszelkich działań jako ten użytkownik, a z perspektywy innych użytkowników będzie to wyglądać, jakby właśnie dany użytkownik przeprowadzał wszystkie czynności. W dzienniku audytu zostanie jednak zapisana informacja, że to Ty wykonywałeś(aś) działania w imieniu tego użytkownika."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Wymagany jest opis."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd sieciowy podczas dodawania uczestnika. Sprawdź połączenie sieciowe i spróbuj ponownie."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Wymagany jest temat."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Działaj jako użytkownik"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Dodaj uczestnika"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Dodaj załącznik"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Dodaj nowego uczestnika"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Dodaj uczestnika z..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Ustawienia alertów"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Powiadom mnie, gdy..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Wszystkie okresy oceniania"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Masz już konto? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Wymagany jest adres e-mail."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wyświetlania tego łącza"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas zapisywania kolekcji. Spróbuj ponownie."),
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
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, fuksjowy"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Kalendarze"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Uprawnienia kamery"),
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
    "Create Account" : MessageLookupByLibrary.simpleMessage("Utwórz konto"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Tryb ciemnego tła"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Usuń"),
    "Description" : MessageLookupByLibrary.simpleMessage("Opis"),
    "Device" : MessageLookupByLibrary.simpleMessage("Urządzenie"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Model urządzenia"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domena"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domena:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Nie pokazuj więcej"),
    "Done" : MessageLookupByLibrary.simpleMessage("Gotowe"),
    "Download" : MessageLookupByLibrary.simpleMessage("Pobierz"),
    "Due" : MessageLookupByLibrary.simpleMessage("Termin"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("KRYTYCZNA SYTUACJA AWARYJNA!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Elektryczny, niebieski"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Adres e-mail"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Adres e-mail:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Adres e-mail..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Wpisz otrzymany kod parowania uczestnika. Jeśli kod parowania nie działa, mógł wygasnąć"),
    "Event" : MessageLookupByLibrary.simpleMessage("Wydarzenie"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Usprawiedliwiony"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Nieważny kod QR"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Nie powiodło się. Stuknij, aby wyświetlić opcje."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtruj"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtruj wg"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Ogień, pomarańczowy"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Pierwsza strona"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Imię i nazwisko"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Imię i nazwisko..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Pełny komunikat o błędzie"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Przejdź do dziś"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Ocena"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Procent oceny"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Oceniono"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Oceny"),
    "Help" : MessageLookupByLibrary.simpleMessage("Pomoc"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Ukryj hasło"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Tryb wysokiego kontrastu"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Jak się czujemy?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("W jaki sposób to na Ciebie wpływa?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Nie mogę nie zrobić dopóki się ze mną nie skontaktujesz."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Nie mam konta Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Posiadam konto Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Potrzebuję pomocy ale nie jest to pilne."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Mam problem z logowaniem"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Pomysł na aplikację Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Aby zapewnić lepszą obsługę, zaktualizowaliśmy sposób działania przypomnień. Można dodawać nowe przypomnienia, wyświetlając zadanie lub wydarzenie w kalendarzu i stukając przełącznik pod sekcją \"Przypomnij mi\".\n\nNależy pamiętać, że przypomnienia utworzone w starszej wersji tej aplikacji nie będą zgodne z nowymi zmianami i będzie trzeba utworzyć je ponownie."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Skrzynka odbiorcza"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Skrzynka odbiorcza zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Nie ukończono"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Niepoprawna domena"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Ogłoszenie instytucji"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Ogłoszenia instytucji"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instrukcje"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interakcje na tej stronie zostały ograniczone przez Twoją instytucje."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Niepoprawny kod QR"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Wygląda to na świetny dzień do odpoczynku, relaksu i regeneracji."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Wygląda na to, że w tej przestrzeni nie utworzono zadań."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Jedynie przypadkowe pytanie, komentarz, pomysł, sugestię..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Późno"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Uruchom narzędzie zewnętrzne"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Informacje prawne"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Tryb jasny"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Błąd łącza"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Lokalizacja:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Znajdź kod QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Lokalizacja"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Zablokowany"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Wyloguj"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Proces logowania: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Proces logowania: Normalny"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Proces logowania: Administrator strony"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Proces logowania: Pomiń weryfikację mobilną"),
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
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Otwórz aplikację Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Otwórz w przeglądarce"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Otwórz w innej aplikacji"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Kod parowania"),
    "Password" : MessageLookupByLibrary.simpleMessage("Hasło"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Wymagane jest hasło"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Hasło musi zawierać co najmniej 8 znaków."),
    "Password…" : MessageLookupByLibrary.simpleMessage("Hasło..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Notatka planera"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Wprowadź prawidłowy adres e-mail"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Podaj adres e-mail"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Podaj pełną nazwę"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Zeskanuj kod QR wygenerowany przez Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Śliwka, fioletowy"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Przygotowywanie..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Poprzednie logowania"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Polityka prywatności"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Łącze do Polityki prywatności"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Polityka prywatności, Warunki korzystania, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Kod QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Skanowanie kodu QR wymaga dostępu kamery"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Poziomka, czerwony"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Odbiorcy"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Odśwież"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Przypomnij mi"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Przypomnienia"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Przypomnienia uległy zmianie!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Odpowiedz"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Odpowiedz wszystkim"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Zgłoś problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Poproś o pomoc w logowaniu"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Przycisk prośby o pomoc w logowaniu"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Ponownie uruchom aplikację"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Ponów próbę"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Powrót do logowania"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("UCZESTNIK"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Zrzut ekranu z lokalizacją wygenerowanego kodu QR w przeglądarce"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Zrzut ekranu przedstawiający miejsce generowania kodu QR w aplikacji Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Wybierz"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Wybierz kolor uczestnika"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Wybierz odbiorców"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Wyślij informację zwrotną"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Wyślij wiadomość o tym zadaniu"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Wyślij wiadomość o tym kursie"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Wyślij wiadomość"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Ustaw datę i godzinę powiadomienia o tym wydarzeniu."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Ustaw datę i godzinę powiadomienia dla tego zadania."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Ustawienia"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Koniczynka, zielony"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Podziel się miłością do Aplikacji"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Pokaż hasło"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Zaloguj się"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Coś się zepsuło ale jestem w stanie to obejść aby wykonać to czego potrzebuję."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Przestań działać jako Użytkownik"),
    "Student" : MessageLookupByLibrary.simpleMessage("Uczestnik"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Parowanie uczestnika"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Uczestnicy mogą utworzyć kod QR za pomocą aplikacji Canvas Student na smartfonie"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Uczestnicy mogą otrzymać kod parowania za pomocą witryny Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Temat"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Przesłano"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Przesłano pomyślnie!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Podsumowanie"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Przełącz użytkownika"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Program"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("NAUCZYCIEL"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Stuknij, aby dodać do ulubionych kursy, które chcesz wyświetlić w Kalendarzu. Wybierz maksymalnie 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Stuknij, aby sparować z nowym uczestnikiem"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Stuknij, aby wybrać tego uczestnika"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Stuknij, aby wyświetlić opcję wyboru uczestników"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Nauczyciel"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Powiedz nam o twoich ulubionych częściach aplikacji"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Warunki korzystania z usługi"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Łącze do Warunków korzystania z usługi"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Warunki korzystania"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Kod QR, który zeskanowano, mógł już wygasnąć. Odśwież kod na urządzeniu uczestnika i spróbuj ponownie."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Poniższe informacje pozwolą nam lepiej zrozumieć twój pomysł:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Wybrany serwer nie ma uprawnień dla tej aplikacji."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Uczestnik, którego próbujesz dodać, należy do innej szkoły. Aby zeskanować ten kod, zaloguj się lub utwórz konto powiązane z tą szkołą."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Pośrednik użytkownika dla tej aplikacji nie ma uprawnień."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Motyw"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Brak zainstalowanych aplikacji, które mogłyby otworzyć ten plik"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Brak informacji o stronie."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytywaniem Warunków korzystania"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas usuwania tego uczestnika z konta. Sprawdź połączenie i spróbuj ponownie."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem odbiorców dla tego kursu"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem szczegółów podsumowania dla tego kursu."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem tego ogłoszenia"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem tej rozmowy"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Wystąpił problem z wczytaniem tego pliku"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania wiadomości ze skrzynki odbiorczej."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania alertów dla uczestników."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania kalendarza dla uczestników"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania uczestników."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas wczytywania kursów uczestnika."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas logowania. Wygeneruj kolejny kod QR i spróbuj ponownie."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Wystąpił błąd podczas podszywania się jako ten użytkownik. Sprawdź domenę i ID użytkownika oraz spróbuj ponownie."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Brak informacji do przekazania."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Ta aplikacja nie jest dopuszczona do użytku."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Ten kurs nie ma żadnych zadań ani wydarzeń w kalendarzu."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Ten plik nie jest obsługiwany i nie można go wyświetlić w aplikacji"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Spowoduje to usunięcie parowania i wszystkich zapisów uczestnika z konta."),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Użyj ciemnego motywu w zawartości sieciowej"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ID użytkownika"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Identyfikator użytkownika:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Numer wersji"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Wyświetl opis"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Wyświetl szczegóły błędu"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Pokaż politykę prywatności"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Tworzymy tę funkcję dla przyjemności użytkowników."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Nie udało się wyświetlić tego łącza, może należeć do instytucji, do której nie jesteś zalogowany."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Nie udało się odnaleźć uczestnika powiązanego z tym kontem"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Nie udało się zweryfikować serwera do użycia z tą aplikacją."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nie mamy pewności, co się wydarzyło, ale nie było to dobre. Jeśli to będzie się powtarzać, skontaktuj się z nami."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Co możemy ulepszyć?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Tak"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Nie obserwujesz żadnych uczestników."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Możesz wybrać tylko 10 kalendarzy do wyświetlenia"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Musisz wpisać ID użytkownika"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Musisz wpisać prawidłową domenę"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Musisz wybrać co najmniej jeden kalendarz do wyświetlenia"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Otrzymasz powiadomienie o tym zadaniu w dniu..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Otrzymasz powiadomienie o tym wydarzeniu w dniu..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Kod QR można znaleźć w witrynie, na profilu konta. Kliknij \'Kod QR do logowania mobilnego\' na liście."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Aby kontynuować, należy otworzyć aplikację Canvas Student. Przejdź do Menu głównego > Ustawienia > Paruj z obserwującym i zeskanuj podany kod QR."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Twój kod jest nieprawidłowy lub wygasł."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Być może kursy uczestnika nie zostały jeszcze opublikowane."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("To już wszystko!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alerty"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Kalendarz"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Przewodniki Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Wsparcie Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("zwinąć"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("zwinięte"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Kursy"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Jak mogę odnaleźć swoją szkołę lub okrąg?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Wpisz nazwę szkoły lub okręg..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("rozwinąć"),
    "expanded" : MessageLookupByLibrary.simpleMessage("rozwinięte"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Znajdź szkołę"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("ja"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Następny"),
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
    "send" : MessageLookupByLibrary.simpleMessage("wyślij"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("nieprzeczytane"),
    "unreadCount" : m47
  };
}
