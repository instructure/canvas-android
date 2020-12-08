// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a ru locale. All the
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
  String get localeName => 'ru';

  static m0(userName) => "Вы действуете как ${userName}";

  static m1(version) => "в. ${version}";

  static m2(threshold) => "Оценка за задание выше ${threshold}";

  static m3(threshold) => "Оценка за задание ниже ${threshold}";

  static m4(moduleName) => "Это задание заблокировано модулем \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Касательно: ${studentName}, задание - ${assignmentName}";

  static m6(points) => "${points} баллов";

  static m7(points) => "${points} баллов";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} до 1 другое', other: '${authorName} до ${howMany} другие')}";

  static m9(authorName, recipientName) => "${authorName} до ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} до ${recipientName} и 1 другой', other: '${authorName} до ${recipientName} и ${howMany} другие')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Изменить цвет для ${studentName}";

  static m13(score, pointsPossible) => "${score} из ${pointsPossible} баллов";

  static m14(studentShortName) => "для ${studentShortName}";

  static m15(threshold) => "Оценка за курс выше ${threshold}";

  static m16(threshold) => "Оценка за курс ниже ${threshold}";

  static m17(date, time) => "${date} в ${time}";

  static m18(alertTitle) => "Исключить ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Попытайтесь выполнить поиск названия школы или округа, к которым вы пытаетесь получить доступ, например, «частная школа Смита» или «школа графства Смит». Также вы можете ввести название домена Canvas напрямую, например, «smith.instructure.com.»\n\nДля получения более подробной информации по поиску учетной записи учреждения Canvas вы можете посетить ${canvasGuides}, обратиться в ${canvasSupport} или в свою школу за поддержкой.";

  static m20(date, time) => "Срок выполнения ${date} в ${time}";

  static m21(userName) => "Функция «Действовать как ${userName}» будет отключена, и сеанс будет завершен.";

  static m22(userName) => "Функция «Действовать как ${userName}» будет отключена и вы вернетесь в свою исходную учетную запись.";

  static m23(studentName, eventTitle) => "Касательно: ${studentName}, событие - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Итоговая оценка: ${grade}";

  static m26(studentName) => "Касательно: ${studentName}, первая страница";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Касательно: ${studentName}, оценки";

  static m29(pointsLost) => "Снижение оценки за опоздание (-${pointsLost})";

  static m30(studentName, linkUrl) => "Касательно: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Должно быть больше ${percentage}";

  static m32(percentage) => "Должно быть меньше ${percentage}";

  static m33(month) => "Следующий месяц: ${month}";

  static m34(date) => "Начало на следующей неделе ${date}";

  static m35(query) => "Невозможно найти школу, соответствующую \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Из 1 балла', other: 'Из ${points} баллов')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} балла(-ов) возможно";

  static m39(month) => "Предыдущий месяц: ${month}";

  static m40(date) => "Начало на предыдущей неделе ${date}";

  static m41(termsOfService, privacyPolicy) => "Нажимая «Создать учетную запись», вы соглашаетесь с ${termsOfService} и признаете ${privacyPolicy}";

  static m42(version) => "Предложения для Android - Canvas Parent ${version}";

  static m43(month) => "Месяц из ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} звезда', other: '${position} звезд(-ы)')}";

  static m45(date, time) => "Это задание было отправлено ${date} в ${time} и ожидает оценки";

  static m46(studentName) => "Касательно: ${studentName}, содержание курса";

  static m47(count) => "${count} непрочитанных";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("«Действовать как пользователь» является по существу входом в систему в качестве этого пользователя без пароля. Вы сможете совершать любые действия, как если бы вы были этим пользователем, а для других пользователей это будет выглядеть так, как будто эти действия совершил этот пользователь. Тем не менее, в контрольных журналах записывается информация от том, что именно вы выполняли указанные действия от имени этого пользователя."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Требуется описание."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("При добавлении студента произошла ошибка. Проверьте подключение и попробуйте еще раз."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("Необходима тема."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Действовать как пользователь"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Добавить студента"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Добавить вложение"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Добавить нового студента"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Добавить студента с…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Настройки оповещения"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Оповестить меня, когда…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Все академические периоды"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Уже есть учетная запись? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("Необходим адрес электронной почты."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Произошла ошибка при попытке открытия данной ссылки"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Произошла ошибка при сохранении вашего выбора. Попробуйте еще раз."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Произошла непредвиденная ошибка"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Версия ОС Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Представление"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Версия приложения"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Вы студент или преподаватель?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Вы уверены, что вы хотите выйти?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Вы действительно хотите закрыть эту страницу? Ваше неотправленное сообщение будет потеряно."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Сведения о задании"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Оценка за задание выше"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Оценка за задание ниже"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Задание отсутствует"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Барни, фуксия"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Календари"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Разрешение для камеры"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Отменить"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Студент Canvas"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Преподаватель Canvas"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas на GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Выбрать курс для сообщения"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Выбрать из галереи"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Завершить"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Связаться со службой поддержки"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Объявление о курсе"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Объявления курса"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Оценка за курс выше"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Оценка за курс ниже"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Создать учетную запись"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Темный режим"),
    "Date" : MessageLookupByLibrary.simpleMessage("Дата"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Удалить"),
    "Description" : MessageLookupByLibrary.simpleMessage("Описание"),
    "Device" : MessageLookupByLibrary.simpleMessage("Устройство"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Модель устройства"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Домен"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Домен:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Не показывать снова"),
    "Done" : MessageLookupByLibrary.simpleMessage("Готово"),
    "Download" : MessageLookupByLibrary.simpleMessage("Скачать"),
    "Due" : MessageLookupByLibrary.simpleMessage("Срок"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("ЧРЕЗВЫЧАЙНО КРИТИЧЕСКАЯ СИТУАЦИЯ!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Электрик, синий"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Адрес электронной почты"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Эл. почта:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Электронная почта..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Введите предоставленный вам код сопряжения студентов. Если сопряжение не работает, срок его действия может закончиться"),
    "Event" : MessageLookupByLibrary.simpleMessage("Событие"),
    "Excused" : MessageLookupByLibrary.simpleMessage("По уважительной причине"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("QR-код с истекшим сроком действия"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Сбой. Коснитесь для просмотра опций."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Фильтровать"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Фильтровать по"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Огонь, оранжевый"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Первая страница"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Полное имя"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Полное имя..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Полное сообщение об ошибке"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Перейти к сегодня"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Оценка"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Процент оценок"),
    "Graded" : MessageLookupByLibrary.simpleMessage("С оценкой"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Оценки"),
    "Help" : MessageLookupByLibrary.simpleMessage("Справка"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Скрыть пароль"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Высококонтрастный режим"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("Как мы работаем?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Как это влияет на вас?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Я не могу добиться своей цели, пока не услышу от вас ответа."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("У меня нет учетной записи Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("У меня есть учетная запись Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Мне нужна помощь, но это не срочно."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("У меня проблема со входом в систему"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Идея для приложения Canvas Родители [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Для обеспечения высокого качества обслуживания мы обновили порядок использования напоминаний. Добавить новые напоминания можно, просмотрев задание или событие календаря и нажав переключатель в разделе «Напоминание».\n\nСледует иметь в виду, что напоминания, созданные с помощью старых версий данного приложения, не будут совместимы с новыми изменениями, и их нужно будет создавать заново."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Входящие"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Входящие Ноль"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Не завершено"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Неверный домен"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Объявление об учебном заведении"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Объявления заведения"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Инструкции"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Взаимодействие на этой странице ограничено вашей организацией."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Неверный QR-код"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Похоже, что сегодня можно отдохнуть, расслабиться и набраться сил."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Похоже, что в этом разделе пока что не было создано ни одного задания."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Просто обычный вопрос, комментарий, идея, предложение..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Поздно"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Запуск внешнего инструмента"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Юридические вопросы"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Светлый режим"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Ошибка ссылки"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Язык:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Определить местонахождение кода QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Местоположение"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Заблокировано"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Выйти"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Процесс авторизации: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Процесс авторизации: Обычный"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Процесс авторизации: Администратор сайта"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Процесс авторизации: Пропустить проверку мобильного устройства"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Управление студентами"),
    "Message" : MessageLookupByLibrary.simpleMessage("Сообщение"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Тема сообщения"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Отсутствует"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Должно быть меньше 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Ошибка сети"),
    "Never" : MessageLookupByLibrary.simpleMessage("Никогда"),
    "New message" : MessageLookupByLibrary.simpleMessage("Новое сообщение"),
    "No" : MessageLookupByLibrary.simpleMessage("Нет"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Нет оповещений"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Задания отсутствуют"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Курсы отсутствуют"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Срок выполнения не задан"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("На сегодня события отсутствуют!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Нет оценки"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Местоположение не указано"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Студенты отсутствуют"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Нет темы"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Нет сводных данных"),
    "No description" : MessageLookupByLibrary.simpleMessage("Описание отсутствует"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Получатели не выбраны"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Оценка не выставлена"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Не отправлено"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Не являетесь родителем?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Уведомления по напоминаниям о заданиях и календарных событиях"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Версия ОС"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Наблюдатель"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Одно из наших других приложений может подойти лучше. Прикоснитесь один раз для входа в Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Откройте Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Открыть в браузере"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Открыть с помощью другого приложения"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Код сопряжения"),
    "Password" : MessageLookupByLibrary.simpleMessage("Пароль"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Требуется пароль"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Пароль должен содержать не менее 8 символов"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Пароль..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Примечание планировщика"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Введите действительный адрес эл. почты"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Введите адрес эл. почты"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Введите полное имя"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Отсканируйте QR-код, сгенерированный Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Слива, фиолетовый"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Подготовка..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Предыдущие идентификаторы"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Политика конфиденциальности"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Ссылка на политику конфиденциальности"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Политика конфиденциальности, условия использования, открытый источник"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR-код"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Для сканирования QR-кода необходим доступ к камере"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Малина, красный"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Получатели"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Обновить"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Напомнить"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Оповещения"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Напоминания изменились!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Ответить"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Ответить на все"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Сообщить о проблеме"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Запрос помощи со входом в систему"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Кнопка запроса помощи по входу в систему"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Перезапуск приложения"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Повторить"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Вернуться к авторизации"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("СТУДЕНТ"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Снимок экрана с отображением места генерирования QR-кода в браузере"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Снимок экрана, на котором показано расположение генерирования QR-кода для подключения в приложение Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Выбрать"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Выбрать цвет студента"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Выбрать получателей"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Отправить оценку"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Отправить сообщение об этом задании"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Отправить сообщение об этом курсе"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Отправить сообщение"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Задать дату и время уведомления об этом событии."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Задать дату и время уведомления об этом конкретном задании."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Настройки"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Трилистник, зеленый"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Поделитесь своей любовью к приложению"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Показать пароль"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Войти в систему"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Что-то пошло не так при попытке создания вашей учетной записи. Обратитесь в свою школу за помощью."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Что-то не работает, но я могу обойтись без этого, чтобы получить то, что мне нужно сделать."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Перестать действовать как пользователь"),
    "Student" : MessageLookupByLibrary.simpleMessage("Студент"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Подключение студентов"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Студенты могут создать QR-код с помощью приложения Canvas Student на своем мобильном устройстве"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Студенты могут получить код подключения на веб-сайте Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Тема"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Отправлено"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Успешно отправлено!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Сводные данные"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Сменить пользователя"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Содержание курса"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("ПРЕПОДАВАТЕЛЬ"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Коснитесь, чтобы отправить в избранное курсы, которые вам хотелось бы видеть в календаре. Выберите до 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Коснитесь для связывания с новыми студентами"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Коснитесь для выбора этого студента"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Коснитесь для отображения селектора студентов"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Преподаватель"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Расскажите нам о своих любимых моментах приложения"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Условия предоставления услуг"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Ссылка на условия обслуживания"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Условия использования"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("У отсканированного вами QR-кода мог истечь срок действия. Обновите код на устройстве студента и попробуйте еще раз."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("Следующая информация поможет нам лучше понять вашу идею:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("Введенный сервер не авторизован для этого приложения."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("Студент, которого вы пытаетесь добавить, относится к другому факультету. Выполните вход или создайте учетную запись на этом факультете, чтобы отсканировать этот код."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("Пользовательский агент для этого приложения не авторизован."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Тема"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Нет установленных приложений, которые могут открыть этот файл"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Нет доступной информации о странице."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Возникала проблема при загрузке условий использования"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Возникла проблема при удалении этого студента из вашей учетной записи. Проверьте подключение и попробуйте еще раз."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке получателей для этого курса"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке подробных сводных данных для этого курса."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке этого объявления"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке этого обсуждения"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке этого файла"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке ваших сообщений из папки «Входящие»."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке оповещений ваших студентов."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке календаря ваших учащихся."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке ваших студентов."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Произошла ошибка при загрузке курсов ваших студентов."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Ошибка входа в систему. Сгенерируйте другой QR-код и попробуйте еще раз."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Произошла ошибка при попытке использования функции «Действовать как пользователь». Проверьте домен и идентификатор пользователя и повторите попытку."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("На данный момент оповещать не о чем."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Это приложение не авторизовано для использования."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Этот курс пока что не имеет никаких заданий или календарных событий."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Этот файл не поддерживается и не может быть просмотрен в приложении"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Это приведет к отключению и удалению всех зачислений для этого студента из вашей учетной записи."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Общая оценка"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Ой-ой!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Невозможно сделать выборку курсов. Проверьте подключение и попробуйте еще раз."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Невозможно скачать это изображение"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Невозможно воспроизвести этот медиафайл"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Невозможно отправить сообщение. Проверьте подключение и попробуйте еще раз."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("В разработке"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Неизвестный пользователь"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Несохраненные изменения"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Неподдерживаемый файл"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Загрузить файл"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Использование камеры"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Использовать темную тему в веб-контенте"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Идентификатор пользователя"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID пользователя:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Номер версии"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Просмотреть описание"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Просмотр подробностей ошибки"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Посмотреть политику конфиденциальности"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("В настоящее время мы разрабатываем эту функцию для более комфортного просмотра."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Мы не можем открыть данную ссылку, она может принадлежать учреждению, в систему которого вы в настоящее время не вошли."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Мы не смогли найти студентов, ассоциированных с этой учетной записью"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Мы не смогли проверить сервер для использования с этим приложением."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Мы не знаем точно, что произошло, но это нехорошо. Обратитесь к нам, если это происходит дальше."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("Что мы можем сделать лучше?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Да"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Вы не наблюдаете ни за одни студентом."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Вы можете выбрать только 10 календарей для отображения"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Необходимо ввести идентификатор пользователя"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Необходимо указать действительный домен"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Вы должны выбрать как минимум один календарь для отображения"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Вы будете уведомлены об этом задании…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Вы будете уведомлены об этом событии…"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("В профиле своей учетной записи вы найдете QR-код. Нажмите «QR для входа с мобильного устройства» в списке."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Вам понадобится открыть приложение Canvas Student, чтобы продолжить. Перейдите в Главное меню > Настройки > Подключить к Observer и отсканируйте QR-код, который вы видите здесь."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Ваш код неверен, или истек срок его действия."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Курсы вашего студента пока еще не могут быть опубликованы."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Вы все нагнали!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Предупреждения"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Календарь"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Руководства Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Логотип Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Поддержка Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("свернуть"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("в свернутом виде"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Курсы"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Как найти мою школу или округ?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Введите название школы или округа…"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("развернуть"),
    "expanded" : MessageLookupByLibrary.simpleMessage("в развернутом виде"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Найти школу"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("меня"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("минус"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Далее"),
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
    "send" : MessageLookupByLibrary.simpleMessage("отправить"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("не прочитано"),
    "unreadCount" : m47
  };
}
