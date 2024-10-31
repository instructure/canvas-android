// Copyright (C) 2019 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:collection/collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/generated/messages_all.dart';
import 'package:intl/intl.dart';

///
/// Delegate for setting up locales.
///
class _AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  List<Locale> get supportedLocales {
    return const <Locale>[
      // First so it's our fallback
      Locale('en'),

      // Supported languages
      Locale('ar'),
      Locale('ca'),
      Locale('cy'), // Not yet supported by flutter_localizations; 'en' fallback will be used for Material localizations
      Locale('da'),
      Locale('de'),
      Locale('en', 'AU'),
      Locale('en', 'CA'),
      Locale('en', 'CY'),
      Locale('en', 'GB'),
      Locale('es'),
      Locale('fi'),
      // Country has to be first so it can be matched before the general language
      Locale('fr', 'CA'),
      Locale('fr'),
      Locale('ht'), // Not yet supported by flutter_localizations; 'en' fallback will be used for Material localizations
      Locale('is'),
      Locale('it'),
      Locale('ja'),
      Locale('mi'), // Not yet supported by flutter_localizations; 'en' fallback will be used for Material localizations
      Locale('nb'),
      Locale('nl'),
      Locale('pl'),
      // Has to match translators naming (pt-PT instead of just pt)
      Locale('pt', 'PT'),
      Locale('pt', 'BR'),
      Locale('ru'),
      Locale('sl'),
      Locale('sv'),
      // Country has to be first so it can be matched before the general language. Also has to match translators naming (zh-HK instead of zh-Hant)
      Locale('zh', 'HK'),
      Locale('zh'),

      // Custom language packs
      Locale.fromSubtags(languageCode: 'da', scriptCode: 'instk12'),
      Locale.fromSubtags(languageCode: 'en', scriptCode: 'unimelb', countryCode: 'AU'),
      Locale.fromSubtags(languageCode: 'en', scriptCode: 'instukhe', countryCode: 'GB'),
      Locale.fromSubtags(languageCode: 'nb', scriptCode: 'instk12'),
      Locale.fromSubtags(languageCode: 'sv', scriptCode: 'instk12'),
    ];
  }

  @override
  bool isSupported(Locale locale) => _isSupported(locale, true) != null;

  @override
  Future<AppLocalizations> load(Locale locale) => AppLocalizations._load(locale);

  @override
  bool shouldReload(LocalizationsDelegate<AppLocalizations> old) => false;

  LocaleResolutionCallback resolution({Locale? fallback, bool matchCountry = true}) {
    return (Locale? locale, Iterable<Locale> supported) {
      return _resolve(locale, fallback, supported, matchCountry);
    };
  }

  ///
  /// Returns true if the specified locale is supported, false otherwise.
  ///
  Locale? _isSupported(Locale? locale, bool shouldMatchCountry) {
    // Must match language code and script code.
    // Must match country code if specified or will fall back to the generic language pack if we don't match country code.
    // i.e., match a passed in 'zh-Hans' to the supported 'zh' if [matchCountry] is false
    return supportedLocales.firstWhereOrNull((Locale supportedLocale) {
      final matchLanguage = (supportedLocale.languageCode == locale?.languageCode);
      final matchScript = (locale?.scriptCode == supportedLocale.scriptCode);
      final matchCountry = (supportedLocale.countryCode == locale?.countryCode);
      final matchCountryFallback =
          (true != shouldMatchCountry && (supportedLocale.countryCode == null || supportedLocale.countryCode?.isEmpty == true));

      return matchLanguage && matchScript && (matchCountry || matchCountryFallback);
    });
  }

  ///
  /// Internal method to resolve a locale from a list of locales.
  ///
  Locale? _resolve(Locale? locale, Locale? fallback, Iterable<Locale> supported, bool matchCountry) {
    if (locale == Locale('zh', 'Hant')) {
      // Special case Traditional Chinese (server sends us zh-Hant but translators give us zh-HK)
      locale = Locale('zh', 'HK');
    } else if (locale == Locale('pt')) {
      // Special case base Portuguese (server sends us pt but translators give us pt-PT)
      locale = Locale('pt', 'PT');
    }

    return _isSupported(locale, matchCountry) ?? fallback ?? supported.first;
  }
}

AppLocalizations L10n(BuildContext context) => Localizations.of<AppLocalizations>(context, AppLocalizations) ?? AppLocalizations();

///
/// App Localization class.
///
/// This will hold all of the strings and reference the right resources depending on locale.
/// View the README for detailed instructions on usage.
///
class AppLocalizations {
  static Future<AppLocalizations> _load(Locale locale) {
    String localeName;
    if (locale.countryCode == null && locale.scriptCode == null) {
      localeName = locale.languageCode;
    } else if (locale.scriptCode != null) {
      final countryCode = locale.countryCode == null ? '' : '_${locale.countryCode}';
      localeName = '${locale.languageCode}${countryCode}_${locale.scriptCode}';
    } else {
      localeName = locale.toString();
    }

    return initializeMessages(localeName).then((_) {
      Intl.defaultLocale = localeName;
      return new AppLocalizations();
    });
  }

  static const _AppLocalizationsDelegate delegate = _AppLocalizationsDelegate();

  String get alertsLabel {
    return Intl.message('Alerts', name: 'alertsLabel', desc: 'The label for the Alerts tab');
  }

  String get calendarLabel {
    return Intl.message('Calendar', name: 'calendarLabel', desc: 'The label for the Calendar tab');
  }

  String get coursesLabel {
    return Intl.message('Courses', name: 'coursesLabel', desc: 'The label for the Courses tab');
  }

  /// Dashboard
  String get noStudents =>
      Intl.message('No Students', desc: 'Text for when an observer has no students they are observing');

  String get tapToShowStudentSelector => Intl.message('Tap to show student selector',
      desc: 'Semantics label for the area that will show the student selector when tapped');

  String get tapToPairNewStudent => Intl.message('Tap to pair with a new student',
      desc: 'Semantics label for the add student button in the student selector');

  String get tapToSelectStudent => Intl.message('Tap to select this student',
      desc: 'Semantics label on individual students in the student switcher');

  /// Navigation Drawer
  String get manageStudents => Intl.message('Manage Students',
      desc: 'Label text for the Manage Students nav drawer button as well as the title for the Manage Students screen');

  String get help => Intl.message('Help', desc: 'Label text for the help nav drawer button');

  String get logOut => Intl.message('Log Out', desc: 'Label text for the Log Out nav drawer button');

  String get switchUsers => Intl.message('Switch Users', desc: 'Label text for the Switch Users nav drawer button');

  String appVersion(String version) => Intl.message('v. $version',
      name: 'appVersion', args: [version], desc: 'App version shown in the navigation drawer');

  String get logoutConfirmation => Intl.message(
        'Are you sure you want to log out?',
        desc: 'Confirmation message displayed when the user tries to log out',
      );

  /// Plalendar

  String get calendars => Intl.message(
        'Calendars',
        desc: 'Label for button that lets users select which calendars to display',
      );

  String nextMonth(String month) => Intl.message(
        'Next month: $month',
        name: 'nextMonth',
        args: [month],
        desc: 'Label for the button that switches the calendar to the next month',
      );

  String previousMonth(String month) => Intl.message(
        'Previous month: $month',
        name: 'previousMonth',
        args: [month],
        desc: 'Label for the button that switches the calendar to the previous month',
      );

  String nextWeek(String date) => Intl.message(
        'Next week starting $date',
        name: 'nextWeek',
        args: [date],
        desc: 'Label for the button that switches the calendar to the next week',
      );

  String previousWeek(String date) => Intl.message(
        'Previous week starting $date',
        name: 'previousWeek',
        args: [date],
        desc: 'Label for the button that switches the calendar to the previous week',
      );

  String selectedMonthLabel(String month) => Intl.message(
        'Month of $month',
        name: 'selectedMonthLabel',
        args: [month],
        desc: 'Accessibility label for the button that expands/collapses the month view',
      );

  String get monthTapExpandHint => Intl.message(
        'expand',
        desc: 'Accessibility label for the on-tap hint for the button that expands/collapses the month view',
      );

  String get monthTapCollapseHint => Intl.message(
        'collapse',
        desc: 'Accessibility label for the on-tap hint for the button that expands/collapses the month view',
      );

  String pointsPossible(String points) => Intl.message(
        '$points points possible',
        name: 'pointsPossible',
        args: [points],
        desc: 'Screen reader label used for the points possible for an assignment, quiz, etc.',
      );

  String calendarDaySemanticsLabel(String date, int eventCount) => Intl.plural(
        eventCount,
        one: '$date, $eventCount event',
        other: '$date, $eventCount events',
        name: 'calendarDaySemanticsLabel',
        args: [date, eventCount],
        desc: 'Screen reader label used for calendar day, reads the date and count of events',
      );

  String get noEventsTitle => Intl.message(
        'No Events Today!',
        desc: 'Title displayed when there are no calendar events for the current day',
      );

  String get noEventsMessage => Intl.message(
        'It looks like a great day to rest, relax, and recharge.',
        desc: 'Message displayed when there are no calendar events for the current day',
      );

  String get errorLoadingEvents => Intl.message(
        'There was an error loading your student\'s calendar',
        desc: 'Message displayed when calendar events could not be loaded for the current student',
      );

  String get calendarTapToFavoriteDesc =>
      Intl.message('Tap to favorite the courses you want to see on the Calendar. Select up to 10.',
          desc: 'Description text on calendar filter screen.');

  String get tooManyCalendarsError => Intl.message('You may only choose 10 calendars to display',
      desc: 'Error text when trying to select more than 10 calendars');

  String get minimumCalendarsError => Intl.message('You must select at least one calendar to display',
      desc: 'Error text when trying to de-select all calendars');

  String get plannerNote => Intl.message('Planner Note', desc: 'Label used for notes in the planner');

  String get gotoTodayButtonLabel =>
      Intl.message('Go to today', desc: 'Accessibility label used for the today button in the planner');

  /// Login landing screen

  String get previousLogins => Intl.message('Previous Logins', desc: 'Label for the list of previous user logins');

  String get canvasLogoLabel {
    return Intl.message('Canvas logo', name: 'canvasLogoLabel', desc: 'The semantics label for the Canvas logo');
  }

  String get findSchool => Intl.message(
        'Find School',
        name: 'findSchool',
        desc: 'Text for the find-my-school button',
      );

  String get findAnotherSchool => Intl.message(
    'Find another school',
    name: 'findAnotherSchool',
    desc: 'Text for the find-another-school button',
  );

  /// Domain search screen

  String get domainSearchInputHint => Intl.message(
        'Enter school name or district…',
        name: 'domainSearchInputHint',
        desc: 'Input hint for the text box on the domain search screen',
      );

  String noDomainResults(String query) => Intl.message(
        'Unable to find schools matching "$query"',
        name: 'noDomainResults',
        args: [query],
        desc: 'Message shown to users when the domain search query did not return any results',
      );

  String get domainSearchHelpLabel => Intl.message(
        'How do I find my school or district?',
        name: 'domainSearchHelpLabel',
        desc: 'Label for the help button on the domain search screen',
      );

  String get canvasGuides => Intl.message(
        'Canvas Guides',
        name: 'canvasGuides',
        desc:
            'Proper name for the Canvas Guides. This will be used in the domainSearchHelpBody text and will be highlighted and clickable',
      );

  String get canvasSupport => Intl.message(
        'Canvas Support',
        name: 'canvasSupport',
        desc:
            'Proper name for Canvas Support. This will be used in the domainSearchHelpBody text and will be highlighted and clickable',
      );

  String domainSearchHelpBody(String canvasGuides, String canvasSupport) => Intl.message(
      """Try searching for the name of the school or district you’re attempting to access, like “Smith Private School” or “Smith County Schools.” You can also enter a Canvas domain directly, like “smith.instructure.com.”\n\nFor more information on finding your institution’s Canvas account, you can visit the $canvasGuides, reach out to $canvasSupport, or contact your school for assistance.""",
      name: 'domainSearchHelpBody',
      desc: 'The body text shown in the help dialog on the domain search screen',
      args: [canvasGuides, canvasSupport]);

  /// Crash screen

  String get crashScreenTitle =>
      Intl.message('Uh oh!', desc: 'Title of the screen that shows when a crash has occurred');

  String get crashScreenMessage =>
      Intl.message('We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening.',
          desc: 'Message shown when a crash has occurred');

  String get crashScreenContact => Intl.message('Contact Support',
      desc: 'Label for the button that allows users to contact support after a crash has occurred');

  String get crashScreenViewDetails =>
      Intl.message('View error details', desc: 'Label for the button that allowed users to view crash details');

  String get crashScreenRestart =>
      Intl.message('Restart app', desc: 'Label for the button that will restart the entire application');

  String get crashDetailsAppVersion =>
      Intl.message('Application version', desc: 'Label for the application version displayed in the crash details');

  String get crashDetailsDeviceModel =>
      Intl.message('Device model', desc: 'Label for the device model displayed in the crash details');

  String get crashDetailsAndroidVersion => Intl.message('Android OS version',
      desc: 'Label for the Android operating system version displayed in the crash details');

  String get crashDetailsFullMessage =>
      Intl.message('Full error message', desc: 'Label for the full error message displayed in the crash details');

  /// Inbox

  String get inbox => Intl.message('Inbox', desc: 'Title for the Inbox screen');

  String get errorLoadingMessages => Intl.message('There was an error loading your inbox messages.');

  String get noSubject => Intl.message('No Subject', desc: 'Title used for inbox messages that have no subject');

  String get errorFetchingCourses =>
      Intl.message('Unable to fetch courses. Please check your connection and try again.',
          desc: 'Message shown when an error occured while loading courses');

  String get messageChooseCourse => Intl.message('Choose a course to message',
      desc: 'Header in the course list shown when the user is choosing which course to associate with a new message');

  String get emptyInboxTitle =>
      Intl.message('Inbox Zero', desc: 'Title of the message shown when there are no inbox messages');

  String get emptyInboxSubtitle =>
      Intl.message('You’re all caught up!', desc: 'Subtitle of the message shown when there are no inbox messages');

  /// Create Conversation

  String get errorLoadingRecipients => Intl.message('There was an error loading recipients for this course',
      desc: 'Message shown when attempting to create a new message but the recipients list failed to load');

  String get errorSendingMessage => Intl.message('Unable to send message. Check your connection and try again.',
      desc: 'Message show when there was an error creating or sending a new message');

  String get unsavedChangesDialogTitle => Intl.message('Unsaved changes',
      desc: 'Title of the dialog shown when the user tries to leave with unsaved changes');

  String get unsavedChangesDialogBody =>
      Intl.message('Are you sure you wish to close this page? Your unsent message will be lost.',
          desc: 'Body text of the dialog shown when the user tries leave with unsaved changes');

  String get newMessageTitle => Intl.message('New message', desc: 'Title of the new-message screen');

  String get addAttachment =>
      Intl.message('Add attachment', desc: 'Tooltip for the add-attachment button in the new-message screen');

  String get sendMessage =>
      Intl.message('Send message', desc: 'Tooltip for the send-message button in the new-message screen');

  String get selectRecipients =>
      Intl.message('Select recipients', desc: 'Tooltip for the button that allows users to select message recipients');

  String get noRecipientsSelected => Intl.message('No recipients selected',
      desc: 'Hint displayed when the user has not selected any message recipients');

  String get messageSubjectInputHint =>
      Intl.message('Message subject', desc: 'Hint text displayed in the input field for the message subject');

  String get messageBodyInputHint =>
      Intl.message('Message', desc: 'Hint text displayed in the input field for the message body');

  String get recipients => Intl.message('Recipients', desc: 'Label for message recipients');

  String plusRecipientCount(int count) => Intl.message(
        '+$count',
        desc: 'Shows the number of recipients that are selected but not displayed on screen.',
        name: 'plusRecipientCount',
        args: [count],
        examples: const {'count': 5},
      );

  String get attachmentFailed => Intl.message('Failed. Tap for options.',
      desc: 'Short message shown on a message attachment when uploading has failed');

  String courseForWhom(String studentShortName) => Intl.message(
        'for $studentShortName',
        desc: 'Describes for whom a course is for (i.e. for Bill)',
        args: [studentShortName],
        name: 'courseForWhom',
      );

  String messageLinkPostscript(String studentName, String linkUrl) => Intl.message(
        'Regarding: $studentName, $linkUrl',
        desc:
            'A postscript appended to new messages that clarifies which student is the subject of the message and also includes a URL for the related Canvas component (course, assignment, event, etc).',
        args: [studentName, linkUrl],
        name: 'messageLinkPostscript',
      );

  /// View conversation

  String get errorLoadingConversation => Intl.message('There was an error loading this conversation',
      desc: 'Message shown when a conversation fails to load');

  String get reply => Intl.message('Reply', desc: 'Button label for replying to a conversation');

  String get replyAll => Intl.message('Reply All', desc: 'Button label for replying to all conversation participants');

  String get unknownUser => Intl.message('Unknown User', desc: 'Label used where the user name is not known');

  String get userNameMe => Intl.message(
        'me',
        desc:
            'First-person pronoun (i.e. \'me\') that will be used in message author info, e.g. \'Me to 4 others\' or \'Jon Snow to me\'',
      );

  String authorToRecipient(String authorName, String recipientName) {
    return Intl.message(
      '$authorName to $recipientName',
      args: [authorName, recipientName],
      name: 'authorToRecipient',
      desc: 'Author info for a single-recipient message; includes both the author name and the recipient name.',
    );
  }

  String authorToNOthers(String authorName, int howMany) {
    return Intl.plural(
      howMany,
      one: '$authorName to 1 other',
      other: '$authorName to $howMany others',
      args: [authorName, howMany],
      name: 'authorToNOthers',
      desc: 'Author info for a mutli-recipient message; includes the author name and the number of recipients',
    );
  }

  String authorToRecipientAndNOthers(String authorName, String recipientName, int howMany) {
    return Intl.plural(
      howMany,
      one: '$authorName to $recipientName & 1 other',
      other: '$authorName to $recipientName & $howMany others',
      args: [authorName, recipientName, howMany],
      name: 'authorToRecipientAndNOthers',
      desc:
          'Author info for a multi-recipient message; includes the author name, one recipient name, and the number of other recipients',
    );
  }

  /// Viewing attachments

  String get download => Intl.message('Download', desc: 'Label for the button that will begin downloading a file');

  String get openFileExternally => Intl.message('Open with another app',
      desc: 'Label for the button that will allow users to open a file with another app');

  String get noApplicationsToHandleFile => Intl.message('There are no installed applications that can open this file');

  String get unsupportedFileTitle => Intl.message('Unsupported File');

  String get unsupportedFileMessage => Intl.message('This file is unsupported and can’t be viewed through the app');

  String get errorPlayingMedia => Intl.message(
        'Unable to play this media file',
        desc: 'Message shown when audio or video media could not be played',
      );

  String get errorLoadingImage => Intl.message(
        'Unable to load this image',
        desc: 'Message shown when an image file could not be loaded or displayed',
      );

  String get errorLoadingFile => Intl.message(
        'There was an error loading this file',
        desc: 'Message shown when a file could not be loaded or displayed',
      );

  /// Courses Screen

  String get noCoursesTitle => Intl.message('No Courses', desc: 'Title for having no courses');

  String get noCoursesMessage =>
      Intl.message('Your student\’s courses might not be published yet.', desc: 'Message for having no courses');

  String get errorLoadingCourses => Intl.message(
        'There was an error loading your student\’s courses.',
        desc: 'Message displayed when the list of student courses could not be loaded',
      );

  String get noGrade => Intl.message(
        'No Grade',
        desc: 'Message shown when there is currently no grade available for a course',
      );

  /// Course Details Screen

  String get filterBy => Intl.message('Filter by', desc: 'Title for list of terms to filter grades by');

  String get courseGradesLabel => Intl.message(
        'Grades',
        desc: 'Label for the "Grades" tab in course details',
      );

  String get courseSyllabusLabel => Intl.message(
        'Syllabus',
        desc: 'Label for the "Syllabus" tab in course details',
      );

  String get courseFrontPageLabel => Intl.message(
        'Front Page',
        desc: 'Label for the "Front Page" tab in course details',
      );

  String get courseSummaryLabel => Intl.message(
        'Summary',
        desc: 'Label for the "Summary" tab in course details',
      );

  String get courseMessageHint => Intl.message(
        'Send a message about this course',
        desc: 'Accessibility hint for the course messaage floating action button',
      );

  String get courseTotalGradeLabel => Intl.message(
        'Total Grade',
        desc: 'Label for the total grade in the course',
      );

  String get assignmentGradedLabel => Intl.message(
        'Graded',
        desc: 'Label for assignments that have been graded',
      );

  String get assignmentSubmittedLabel => Intl.message(
        'Submitted',
        desc: 'Label for assignments that have been submitted',
      );

  String get assignmentNotSubmittedLabel => Intl.message(
        'Not Submitted',
        desc: 'Label for assignments that have not been submitted',
      );

  String get assignmentLateSubmittedLabel => Intl.message(
        'Late',
        desc: 'Label for assignments that have been marked late or submitted late',
      );

  String get assignmentMissingSubmittedLabel => Intl.message(
        'Missing',
        desc: 'Label for assignments that have been marked missing or are not submitted and past the due date',
      );

  String get assignmentNoScore => Intl.message(
        '-',
        desc: 'Value representing no score for student submission',
      );

  String get allGradingPeriods => Intl.message(
        'All Grading Periods',
        desc: 'Label for selecting all grading periods',
      );

  String get noAssignmentsTitle => Intl.message(
        'No Assignments',
        desc: 'Title for the no assignments message',
      );

  String get noAssignmentsMessage => Intl.message(
        'It looks like assignments haven\'t been created in this space yet.',
        desc: 'Message for no assignments',
      );

  String get errorLoadingCourseSummary => Intl.message(
        'There was an error loading the summary details for this course.',
        desc: 'Message shown when the course summary could not be loaded',
      );

  String get noCourseSummaryTitle => Intl.message(
        'No Summary',
        desc: 'Title displayed when there are no items in the course summary',
      );

  String get noCourseSummaryMessage => Intl.message(
        'This course does not have any assignments or calendar events yet.',
        desc: 'Message displayed when there are no items in the course summary',
      );

  String gradeFormatScoreOutOfPointsPossible(String score, String pointsPossible) => Intl.message(
        '${score} / ${pointsPossible}',
        desc: 'Formatted string for a student score out of the points possible',
        name: 'gradeFormatScoreOutOfPointsPossible',
        args: [score, pointsPossible],
      );

  String contentDescriptionScoreOutOfPointsPossible(String score, String pointsPossible) => Intl.message(
        '${score} out of ${pointsPossible} points',
        desc: 'Formatted string for a student score out of the points possible',
        name: 'contentDescriptionScoreOutOfPointsPossible',
        args: [score, pointsPossible],
      );

  String gradesSubjectMessage(String studentName) => Intl.message(
        'Regarding: $studentName, Grades',
        desc: 'The subject line for a message to a teacher regarding a student\'s grades',
        name: 'gradesSubjectMessage',
        args: [studentName],
      );

  String syllabusSubjectMessage(String studentName) => Intl.message(
        'Regarding: $studentName, Syllabus',
        desc: 'The subject line for a message to a teacher regarding a course syllabus',
        name: 'syllabusSubjectMessage',
        args: [studentName],
      );

  String frontPageSubjectMessage(String studentName) => Intl.message(
        'Regarding: $studentName, Front Page',
        desc: 'The subject line for a message to a teacher regarding a course front page',
        name: 'frontPageSubjectMessage',
        args: [studentName],
      );

  String assignmentSubjectMessage(String studentName, String assignmentName) => Intl.message(
        'Regarding: $studentName, Assignment - $assignmentName',
        desc: 'The subject line for a message to a teacher regarding a student\'s assignment',
        name: 'assignmentSubjectMessage',
        args: [studentName, assignmentName],
      );

  String eventSubjectMessage(String studentName, String eventTitle) => Intl.message(
        'Regarding: $studentName, Event - $eventTitle',
        desc: 'The subject line for a message to a teacher regarding a calendar event',
        name: 'eventSubjectMessage',
        args: [studentName, eventTitle],
      );

  String get noPageFound => Intl.message(
        'There is no page information available.',
        desc: 'Description for when no page information is available',
      );

  /// Assignment Details Screen

  String get assignmentDetailsTitle =>
      Intl.message('Assignment Details', desc: 'Title for the page that shows details for an assignment');

  String assignmentTotalPoints(String points) => Intl.message(
        '$points pts',
        name: 'assignmentTotalPoints',
        args: [points],
        desc: 'Label used for the total points the assignment is worth',
      );

  String assignmentTotalPointsAccessible(String points) => Intl.message(
        '$points points',
        name: 'assignmentTotalPointsAccessible',
        args: [points],
        desc: 'Screen reader label used for the total points the assignment is worth',
      );

  String get assignmentDueLabel => Intl.message('Due', desc: 'Label for an assignment due date');

  String get assignmentGradeLabel => Intl.message(
        'Grade',
        desc: 'Label for the section that displays an assignment\'s grade',
      );

  String get assignmentLockLabel => Intl.message('Locked', desc: 'Label for when an assignment is locked');

  String assignmentLockedModule(String moduleName) => Intl.message(
        'This assignment is locked by the module \"$moduleName\".',
        name: 'assignmentLockedModule',
        args: [moduleName],
        desc: 'The locked description when an assignment is locked by a module',
      );

  String get assignmentRemindMeLabel => Intl.message('Remind Me', desc: 'Label for the row to set reminders');

  String get assignmentRemindMeDescription =>
      Intl.message('Set a date and time to be notified of this specific assignment.',
          desc: 'Description for row to set reminders');

  String get assignmentRemindMeSet =>
      Intl.message('You will be notified about this assignment on…', desc: 'Description for when a reminder is set');

  String get assignmentInstructionsLabel =>
      Intl.message('Instructions', desc: 'Label for the description of the assignment when it has quiz instructions');

  String get assignmentMessageHint => Intl.message(
        'Send a message about this assignment',
        desc: 'Accessibility hint for the assignment messaage floating action button',
      );

  /// Web Login Screen

  String get domainVerificationErrorGeneral => Intl.message(
        'This app is not authorized for use.',
        desc: 'The error shown when the app being used is not verified by Canvas',
      );

  String get domainVerificationErrorDomain => Intl.message(
        'The server you entered is not authorized for this app.',
        desc: 'The error shown when the desired login domain is not verified by Canvas',
      );

  String get domainVerificationErrorUserAgent => Intl.message(
        'The user agent for this app is not authorized.',
        desc: 'The error shown when the user agent during verification is not verified by Canvas',
      );

  String get domainVerificationErrorUnknown => Intl.message(
        'We were unable to verify the server for use with this app.',
        desc: 'The generic error shown when we are unable to verify with Canvas',
      );

  // Skip non translatable (support only)
  String get skipMobileVerifyTitle => Intl.message(
        'Skipping Mobile Verify…',
        skip: true,
      );

  // Skip non translatable (support only)
  String get skipMobileVerifyProtocol => Intl.message(
        'https',
        skip: true,
      );

  // Skip non translatable (support only)
  String get skipMobileVerifyProtocolMissing => Intl.message(
        'Must provide a protocol',
        skip: true,
      );

  // Skip non translatable (support only)
  String get skipMobileVerifyClientId => Intl.message(
        'Client Id',
        skip: true,
      );

  // Skip non translatable (support only)
  String get skipMobileVerifyClientIdMissing => Intl.message(
        'Must provide a client id',
        skip: true,
      );

  // Skip non translatable (support only)
  String get skipMobileVerifyClientSecret => Intl.message(
        'Client Secret',
        skip: true,
      );

  // Skip non translatable (support only)
  String get skipMobileVerifyClientSecretMissing => Intl.message(
        'Must provide a client secret',
        skip: true,
      );

  /// Reminders

  String get remindersNotificationChannelName => Intl.message(
        'Reminders',
        desc: 'Name of the system notification channel for assignment and event reminders',
      );

  String get remindersNotificationChannelDescription => Intl.message(
        'Notifications for reminders about assignments and calendar events',
        desc: 'Description of the system notification channel for assignment and event reminders',
      );

  String get oldReminderMessageTitle => Intl.message(
        'Reminders have changed!',
        desc: 'Title of the dialog shown when the user needs to update their reminders',
      );

  String get oldReminderMessage => Intl.message(
        'In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the "Remind Me" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again.',
      );

  /// Not-A-Parent screen

  String get notAParentTitle => Intl.message('Not a parent?',
      desc: 'Title for the screen that shows when the user is not observing any students');

  String get notAParentSubtitle => Intl.message('We couldn\'t find any students associated with this account',
      desc: 'Subtitle for the screen that shows when the user is not observing any students');

  String get studentOrTeacherTitle => Intl.message('Are you a student or teacher?',
      desc: 'Label for button that will show users the option to view other Canvas apps in the Play Store');

  String get studentOrTeacherSubtitle =>
      Intl.message('One of our other apps might be a better fit. Tap one to visit the Play Store.',
          desc: 'Description of options to view other Canvas apps in the Play Store');

  String get returnToLogin =>
      Intl.message('Return to Login', desc: 'Label for the button that returns the user to the login screen');

  String get studentApp => Intl.message('STUDENT',
      desc:
          'The "student" portion of the "Canvas Student" app name, in all caps. "Canvas" is excluded in this context as it will be displayed to the user as a wordmark image');

  String get teacherApp => Intl.message('TEACHER',
      desc:
          'The "teacher" portion of the "Canvas Teacher" app name, in all caps. "Canvas" is excluded in this context as it will be displayed to the user as a wordmark image');

  String get canvasStudentApp => Intl.message('Canvas Student',
      desc:
          'The name of the Canvas Student app. Only "Student" should be translated as "Canvas" is a brand name in this context and should not be translated.');

  String get canvasTeacherApp => Intl.message('Canvas Teacher',
      desc:
          'The name of the Canvas Teacher app. Only "Teacher" should be translated as "Canvas" is a brand name in this context and should not be translated.');

  /// Alerts Screen

  String get noAlertsTitle => Intl.message(
        'No Alerts',
        desc: 'The title for the empty message to show to users when there are no alerts for the student.',
      );

  String get noAlertsMessage => Intl.message(
        'There’s nothing to be notified of yet.',
        desc: 'The empty message to show to users when there are no alerts for the student.',
      );

  String dismissAlertLabel(String alertTitle) => Intl.message(
        'Dismiss $alertTitle',
        name: 'dismissAlertLabel',
        args: [alertTitle],
        desc: 'Accessibility label to dismiss an alert',
      );

  String get courseAnnouncement => Intl.message(
        'Course Announcement',
        desc: 'Title for alerts when there is a course announcement',
      );

  String get globalAnnouncement => Intl.message(
    'Global Announcement',
    desc: 'Title for alerts when there is a global announcement',
  );

  String assignmentGradeAboveThreshold(String threshold) => Intl.message(
        'Assignment Grade Above $threshold',
        name: 'assignmentGradeAboveThreshold',
        args: [threshold],
        desc: 'Title for alerts when an assignment grade is above the threshold value',
      );

  String assignmentGradeBelowThreshold(String threshold) => Intl.message(
        'Assignment Grade Below $threshold',
        name: 'assignmentGradeBelowThreshold',
        args: [threshold],
        desc: 'Title for alerts when an assignment grade is below the threshold value',
      );

  String courseGradeAboveThreshold(String threshold) => Intl.message(
        'Course Grade Above $threshold',
        name: 'courseGradeAboveThreshold',
        args: [threshold],
        desc: 'Title for alerts when a course grade is above the threshold value',
      );

  String courseGradeBelowThreshold(String threshold) => Intl.message(
        'Course Grade Below $threshold',
        name: 'courseGradeBelowThreshold',
        args: [threshold],
        desc: 'Title for alerts when a course grade is below the threshold value',
      );

  /// Settings screen

  String get settings => Intl.message('Settings', desc: 'Title for the settings screen');

  String get theme => Intl.message('Theme', desc: 'Label for the light/dark theme section in the settings page');

  String get darkModeLabel => Intl.message('Dark Mode', desc: 'Label for the button that enables dark mode');

  String get lightModeLabel => Intl.message('Light Mode', desc: 'Label for the button that enables light mode');

  String get highContrastLabel =>
      Intl.message('High Contrast Mode', desc: 'Label for the switch that toggles high contrast mode');

  String get webViewDarkModeLabel =>
      Intl.message('Use Dark Theme in Web Content', desc: 'Label for the switch that toggles dark mode for webviews');

  String get appearance => Intl.message('Appearance', desc: 'Label for the appearance section in the settings page');

  /// Grade cell

  String get submissionStatusSuccessTitle => Intl.message('Successfully submitted!',
      desc: 'Title displayed in the grade cell for an assignment that has been submitted');

  String submissionStatusSuccessSubtitle(String date, String time) {
    return Intl.message(
      'This assignment was submitted on $date at $time and is waiting to be graded',
      desc: 'Subtitle displayed in the grade cell for an assignment that has been submitted and is awaiting a grade',
      args: [date, time],
      name: 'submissionStatusSuccessSubtitle',
    );
  }

  String outOfPoints(String points, num howMany) => Intl.plural(
        howMany,
        one: 'Out of 1 point',
        other: 'Out of $points points',
        desc: 'Description for an assignment grade that has points without a current scoroe',
        args: [points, howMany],
        name: 'outOfPoints',
        precision: 2,
      );

  String get excused => Intl.message('Excused', desc: 'Grading status for an assignment marked as excused');

  String get gradeComplete => Intl.message('Complete', desc: 'Grading status for an assignment marked as complete');

  String get gradeIncomplete => Intl.message(
        'Incomplete',
        desc: 'Grading status for an assignment marked as incomplete',
      );

  String get accessibilityMinus => Intl.message(
        'minus',
        desc: 'Screen reader-friendly replacement for the "-" character in letter grades like "A-"',
      );

  String yourGrade(String pointsAchieved) => Intl.message(
    'Your grade: $pointsAchieved',
    desc: 'Text displayed when a late penalty has been applied to the assignment, this is the achieved score without the penalty',
    args: [pointsAchieved],
    name: 'yourGrade',
  );

  String latePenaltyUpdated(String pointsLost) => Intl.message(
        'Late Penalty: -$pointsLost pts',
        desc: 'Text displayed when a late penalty has been applied to the assignment',
        args: [pointsLost],
        name: 'latePenaltyUpdated',
      );

  String finalGrade(String grade) => Intl.message(
        'Final Grade: $grade',
        desc: 'Text that displays the final grade of an assignment',
        args: [grade],
        name: 'finalGrade',
      );

  /// Alert Thresholds Screen
  String get alertSettings => Intl.message('Alert Settings');

  String get alertMeWhen => Intl.message('Alert me when…',
      desc:
          'Header for the screen where the observer chooses the thresholds that will determine when they receive alerts (e.g. when an assignment is graded below 70%)');

  String get courseGradeBelow => Intl.message('Course grade below',
      desc: 'Label describing the threshold for when the course grade is below a certain percentage');

  String get courseGradeAbove => Intl.message('Course grade above',
      desc: 'Label describing the threshold for when the course grade is above a certain percentage');

  String get assignmentMissing => Intl.message('Assignment missing');

  String get assignmentGradeBelow => Intl.message('Assignment grade below',
      desc: 'Label describing the threshold for when an assignment is graded below a certain percentage');

  String get assignmentGradeAbove => Intl.message('Assignment grade above',
      desc: 'Label describing the threshold for when an assignment is graded above a certain percentage');

  String get courseAnnouncements => Intl.message('Course Announcements');

  String get globalAnnouncements => Intl.message('Global Announcements');

  String get never => Intl.message('Never',
      desc: 'Indication that tells the user they will not receive alert notifications of a specific kind');

  String get gradePercentage => Intl.message('Grade percentage');

  String get alertThresholdsLoadingError => Intl.message('There was an error loading your student\'s alerts.');

  String get mustBeBelow100 => Intl.message('Must be below 100');

  String mustBeBelowN(int percentage) => Intl.message(
        'Must be below $percentage',
        desc: 'Validation error to the user that they must choose a percentage below \'n\'',
        args: [percentage],
        name: 'mustBeBelowN',
        examples: const {'percentage': 5},
      );

  String mustBeAboveN(int percentage) => Intl.message(
        'Must be above $percentage',
        desc: 'Validation error to the user that they must choose a percentage above \'n\'',
        args: [percentage],
        name: 'mustBeAboveN',
        examples: const {'percentage': 5},
      );

  /// Student color picker

  String get selectStudentColor => Intl.message(
        'Select Student Color',
        desc: 'Title for screen that allows users to assign a color to a specific student',
      );

  String get colorElectric => Intl.message('Electric, blue', desc: 'Name of the Electric (blue) color');

  String get colorPlum => Intl.message('Plum, Purple', desc: 'Name of the Plum (purple) color');

  String get colorBarney => Intl.message('Barney, Fuschia', desc: 'Name of the Barney (fuschia) color');

  String get colorRaspberry => Intl.message('Raspberry, Red', desc: 'Name of the Raspberry (red) color');

  String get colorFire => Intl.message('Fire, Orange', desc: 'Name of the Fire (orange) color');

  String get colorShamrock => Intl.message('Shamrock, Green', desc: 'Name of the Shamrock (green) color');

  String get errorSavingColor => Intl.message('An error occurred while saving your selection. Please try again.');

  String changeStudentColorLabel(String studentName) => Intl.message(
        'Change color for $studentName',
        name: 'changeStudentColorLabel',
        args: [studentName],
        desc: 'Accessibility label for the button that lets users change the color associated with a specific student',
      );

  /// Enrollment types

  String get enrollmentTypeTeacher => Intl.message('Teacher', desc: 'Label for the Teacher enrollment type');

  String get enrollmentTypeStudent => Intl.message('Student', desc: 'Label for the Student enrollment type');

  String get enrollmentTypeTA => Intl.message('TA',
      desc:
          'Label for the Teaching Assistant enrollment type (also known as Teacher Aid or Education Assistant), reduced to a short acronym/initialism if appropriate.');

  String get enrollmentTypeObserver => Intl.message('Observer', desc: 'Label for the Observer enrollment type');

  // Attachment picker

  String get useCamera => Intl.message('Use Camera',
      desc: 'Label for the action item that lets the user capture a photo using the device camera');

  String get uploadFile =>
      Intl.message('Upload File', desc: 'Label for the action item that lets the user upload a file from their device');

  String get chooseFromGallery => Intl.message('Choose from Gallery',
      desc: 'Label for the action item that lets the user select a photo from their device gallery');

  String get attachmentPreparing =>
      Intl.message('Preparing…', desc: 'Message shown while a file is being prepared to attach to a message');

  /// Manage Students
  String get addStudentWith => Intl.message('Add student with…');

  String get addStudent => Intl.message('Add Student');

  String get emptyStudentList => Intl.message('You are not observing any students.');

  String get errorLoadingStudents => Intl.message('There was an error loading your students.');

  String get pairingCode => Intl.message('Pairing Code');

  String get pairingCodeDescription => Intl.message('Students can obtain a pairing code through the Canvas website');

  String get pairingCodeEntryExplanation => Intl.message(
      'Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired');

  String get errorPairingFailed => Intl.message('Your code is incorrect or expired.');

  String get errorGenericPairingFailed => Intl.message(
      'Something went wrong trying to create your account, please reach out to your school for assistance.');

  String get qrCode => Intl.message('QR Code');

  String get qrCodeDescription =>
      Intl.message('Students can create a QR code using the Canvas Student app on their mobile device');

  String get addNewStudent =>
      Intl.message('Add new student', desc: 'Semantics label for the FAB on the Manage Students Screen');

  String get qrLoginSelect => Intl.message('Select', desc: 'Hint text to tell the user to choose one of two options');

  String get qrLoginHaveAccount =>
      Intl.message('I have a Canvas account', desc: 'Option to select for users that have a canvas account');

  String get qrLoginNewAccount => Intl.message('I don\'t have a Canvas account',
      desc: 'Option to select for users that don\'t have a canvas account');

  String get qrCreateAccount => Intl.message('Create Account', desc: 'Button text for account creation confirmation');

  String get qrCreateAccountLabelName => Intl.message('Full Name');

  String get qrCreateAccountLabelEmail => Intl.message('Email Address');

  String get qrCreateAccountLabelPassword => Intl.message('Password');

  String get qrCreateAccountHintName => Intl.message('Full Name…', desc: 'hint label for inside form field');

  String get qrCreateAccountHintEmail => Intl.message('Email…', desc: 'hint label for inside form field');

  String get qrCreateAccountHintPassword => Intl.message('Password…', desc: 'hint label for inside form field');

  String get qrCreateAccountNameError => Intl.message('Please enter full name', desc: 'Error message for form field');

  String get qrCreateAccountEmailError =>
      Intl.message('Please enter an email address', desc: 'Error message for form field');

  String get qrCreateAccountInvalidEmailError =>
      Intl.message('Please enter a valid email address', desc: 'Error message for form field');

  String get qrCreateAccountPasswordError => Intl.message('Password is required', desc: 'Error message for form field');

  String get qrCreateAccountPasswordLengthError =>
      Intl.message('Password must contain at least 8 characters', desc: 'Error message for form field');

  String qrCreateAccountTos(String termsOfService, String privacyPolicy) => Intl.message(
      """By tapping 'Create Account', you agree to the $termsOfService and acknowledge the $privacyPolicy""",
      name: 'qrCreateAccountTos',
      desc: 'The text show on the account creation screen',
      args: [termsOfService, privacyPolicy]);

  String get qrCreateAccountTermsOfService => Intl.message(
        'Terms of Service',
        desc:
            'Label for the Canvas Terms of Service agreement. This will be used in the qrCreateAccountTos text and will be highlighted and clickable',
      );

  String get qrCreateAccountPrivacyPolicy => Intl.message(
        'Privacy Policy',
        desc:
            'Label for the Canvas Privacy Policy agreement. This will be used in the qrCreateAccountTos text and will be highlighted and clickable',
      );

  String get qrCreateAccountViewPrivacy => Intl.message('View the Privacy Policy');

  String get qrCreateAccountSignIn1 => Intl.message('Already have an account? ',
      desc: 'Part of multiline text span, includes AccountSignIn1-2, in that order');

  String get qrCreateAccountSignIn2 =>
      Intl.message('Sign In', desc: 'Part of multiline text span, includes AccountSignIn1-2, in that order');

  String get qrCreateAccountEyeOffSemantics =>
      Intl.message('Hide Password', desc: 'content description for password hide button');

  String get qrCreateAccountEyeSemantics =>
      Intl.message('Show Password', desc: 'content description for password show button');

  String get qrCreateAccountTosSemantics =>
      Intl.message('Terms of Service Link', desc: 'content description for terms of service link');

  String get qrCreateAccountPrivacySemantics =>
      Intl.message('Privacy Policy Link', desc: 'content description for privacy policy link');

  /// Event details

  String get eventDetailsTitle => Intl.message('Event', desc: 'Title for the event details screen');

  String get eventDateLabel => Intl.message('Date', desc: 'Label for the event date');

  String get eventLocationLabel => Intl.message('Location', desc: 'Label for the location information');

  String get eventNoLocation =>
      Intl.message('No Location Specified', desc: 'Description for events that do not have a location');

  String eventTime(String startAt, String endAt) => Intl.message(
        '$startAt - $endAt',
        name: 'eventTime',
        args: [startAt, endAt],
        desc: 'The time the event is happening, example: "2:00 pm - 4:00 pm"',
      );

  String get eventRemindMeDescription => Intl.message('Set a date and time to be notified of this event.',
      desc: 'Description for row to set event reminders');

  String get eventRemindMeSet =>
      Intl.message('You will be notified about this event on…', desc: 'Description for when an event reminder is set');

  /// Help Screen
  String get helpShareLoveLabel => Intl.message(
        'Share Your Love for the App',
        desc: 'Label for option to open the app store',
      );

  String get helpShareLoveDescription => Intl.message(
        'Tell us about your favorite parts of the app',
        desc: 'Description for option to open the app store',
      );

  String get helpLegalLabel => Intl.message(
        'Legal',
        desc: 'Label for legal information option',
      );

  String get helpLegalDescription => Intl.message(
        'Privacy policy, terms of use, open source',
        desc: 'Description for legal information option',
      );

  String get featureRequestSubject => Intl.message(
        'Idea for Canvas Parent App [Android]',
        desc: 'The subject for the email to request a feature',
      );

  String get featureRequestHeader => Intl.message(
        'The following information will help us better understand your idea:',
        desc: 'The header for the users information that is attached to a feature request',
      );

  String get helpDomain => Intl.message('Domain:', desc: 'The label for the Canvas domain of the logged in user');

  String get helpUserId => Intl.message('User ID:', desc: 'The label for the Canvas user ID of the logged in user');

  String get helpEmail => Intl.message('Email:', desc: 'The label for the eamil of the logged in user');

  String get helpLocale => Intl.message('Locale:', desc: 'The label for the locale of the logged in user');

  /// Legal Screen

  String get privacyPolicy => Intl.message('Privacy Policy', desc: 'Label for the privacy policy');

  String get termsOfUse => Intl.message('Terms of Use', desc: 'Label for the terms of use');

  String get canvasOnGithub => Intl.message(
        'Canvas on GitHub',
        desc: 'Label for the button that opens the Canvas project on GitHub\'s website',
      );

  String get errorLoadingTermsOfUse => Intl.message('There was a problem loading the Terms of Use');

  /// Error Report Dialog

  String get device => Intl.message('Device', desc: 'Label used for device manufacturer/model in the error report');

  String get osVersion =>
      Intl.message('OS Version', desc: 'Label used for device operating system version in the error report');

  String get versionNumber =>
      Intl.message('Version Number', desc: 'Label used for the app version number in the error report');

  String get reportProblemTitle =>
      Intl.message('Report A Problem', desc: 'Title used for generic dialog to report problems');

  String get reportProblemSubject => Intl.message('Subject', desc: 'Label used for Subject text field');

  String get reportProblemSubjectEmpty =>
      Intl.message('A subject is required.', desc: 'Error shown when the subject field is empty');

  String get reportProblemEmail => Intl.message('Email Address', desc: 'Label used for Email Address text field');

  String get reportProblemEmailEmpty =>
      Intl.message('An email address is required.', desc: 'Error shown when the email field is empty');

  String get reportProblemDescription => Intl.message('Description', desc: 'Label used for Description text field');

  String get reportProblemDescriptionEmpty =>
      Intl.message('A description is required.', desc: 'Error shown when the description field is empty');

  String get reportProblemSeverity =>
      Intl.message('How is this affecting you?', desc: 'Label used for the dropdown to select how severe the issue is');

  String get sendReport => Intl.message('send', desc: 'Label used for send button when reporting a problem');

  String get errorSeverityComment => Intl.message('Just a casual question, comment, idea, suggestion…');

  String get errorSeverityNotUrgent => Intl.message('I need some help but it\'s not urgent.');

  String get errorSeverityWorkaroundPossible =>
      Intl.message('Something\'s broken but I can work around it to get what I need done.');

  String get errorSeverityBlocking => Intl.message('I can\'t get things done until I hear back from you.');

  String get errorSeverityCritical => Intl.message('EXTREME CRITICAL EMERGENCY!!');

  /// Calendar Screen
  String get missing => Intl.message(
        'Missing',
        desc: 'Description for when a student has not turned anything in for an assignment',
      );

  String get notGraded => Intl.message(
        'Not Graded',
        desc: 'Description for an assignment has not been graded.',
      );

  /// Masquerading

  String get loginFlowNormal => Intl.message(
        'Login flow: Normal',
        desc: 'Description for the normal login flow',
      );

  String get loginFlowCanvas => Intl.message(
        'Login flow: Canvas',
        desc: 'Description for the Canvas login flow',
      );

  String get loginFlowSiteAdmin => Intl.message(
        'Login flow: Site Admin',
        desc: 'Description for the Site Admin login flow',
      );

  String get loginFlowSkipMobileVerify => Intl.message(
        'Login flow: Skip mobile verify',
        desc: 'Description for the login flow that skips domain verification for mobile',
      );

  String get actAsUser => Intl.message(
        'Act As User',
        desc: 'Label for the button that allows the user to act (masquerade) as another user',
      );

  String get stopActAsUser => Intl.message(
        'Stop Acting as User',
        desc: 'Label for the button that allows the user to stop acting (masquerading) as another user',
      );

  String actingAsUser(String userName) => Intl.message(
        'You are acting as $userName',
        name: 'actingAsUser',
        args: [userName],
        desc: 'Message shown while acting (masquerading) as another user',
      );

  String get actAsDescription => Intl.message(
      '"Act as" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user.');

  String get domainInputHint => Intl.message('Domain', desc: 'Text field hint for domain url input');

  String get domainInputError => Intl.message(
        'You must enter a valid domain',
        desc: 'Message displayed for domain input error',
      );

  String get userIdInputHint => Intl.message('User ID', desc: 'Text field hint for user ID input');

  String get userIdInputError => Intl.message(
        'You must enter a user id',
        desc: 'Message displayed for user Id input error',
      );

  String get actAsUserError => Intl.message(
        'There was an error trying to act as this user. Please check the Domain and User ID and try again.',
      );

  String endMasqueradeMessage(String userName) => Intl.message(
        'You will stop acting as $userName and return to your original account.',
        name: 'endMasqueradeMessage',
        args: [userName],
        desc: 'Confirmation message displayed when the user wants to stop acting (masquerading) as another user',
      );

  String endMasqueradeLogoutMessage(String userName) => Intl.message(
        'You will stop acting as $userName and will be logged out.',
        name: 'endMasqueradeLogoutMessage',
        args: [userName],
        desc:
            'Confirmation message displayed when the user wants to stop acting (masquerading) as another user and will be logged out.',
      );

  /// Rating dialog

  String get ratingDialogTitle => Intl.message(
        'How are we doing?',
        desc: 'Title for dialog asking user to rate the app out of 5 stars.',
      );

  String get ratingDialogDontShowAgain => Intl.message(
        'Don\'t show again',
        desc: 'Button to prevent the rating dialog from showing again.',
      );

  String get ratingDialogCommentDescription => Intl.message(
        'What can we do better?',
        desc: 'Hint text for providing a comment with the rating.',
      );

  String get ratingDialogSendFeedback => Intl.message('Send Feedback', desc: 'Button to send rating with feedback');

  String ratingDialogEmailSubject(String version) => Intl.message(
        'Suggestions for Android - Canvas Parent $version',
        desc: 'The subject for an email to provide feedback for CanvasParent.',
        name: 'ratingDialogEmailSubject',
        args: [version],
      );

  String starRating(int position) => Intl.plural(
        position,
        one: '$position star',
        other: '$position stars',
        args: [position],
        name: 'starRating',
        desc: 'Accessibility label for the 1 stars to 5 stars rating',
        examples: const {'position': 1},
      );

  /// QR Pairing

  String get qrPairingTitle => Intl.message(
        'Student Pairing',
        desc: 'Title for the screen where users can pair to students using a QR code',
      );

  String get qrPairingTutorialTitle => Intl.message(
        'Open Canvas Student',
        desc: 'Title for QR pairing tutorial screen instructing users to open the Canvas Student app',
      );

  String get qrPairingTutorialMessage => Intl.message(
        'You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there.',
        desc: 'Message explaining how QR code pairing works',
      );

  String get qrPairingScreenshotContentDescription => Intl.message(
        'Screenshot showing location of pairing QR code generation in the Canvas Student app',
        desc: 'Content Description for qr pairing tutorial screenshot',
      );

  String get qrPairingFailedTitle => Intl.message(
        'Expired QR Code',
        desc: 'Error title shown when the users scans a QR code that has expired',
      );

  String get qrPairingFailedSubtitle => Intl.message(
        'The QR code you scanned may have expired. Refresh the code on the student\'s device and try again.',
      );

  String get qrPairingNetworkError => Intl.message(
        'A network error occurred when adding this student. Check your connection and try again.',
      );

  String get qrPairingInvalidCodeTitle => Intl.message(
        'Invalid QR Code',
        desc: 'Error title shown when the user scans an invalid QR code',
      );

  String get qrPairingWrongDomainTitle => Intl.message(
        'Incorrect Domain',
        desc: 'Error title shown when the users scane a QR code for a student that belongs to a different domain',
      );

  String get qrPairingWrongDomainSubtitle => Intl.message(
        'The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code.',
      );

  String get qrPairingCameraPermissionTitle => Intl.message(
        'Camera Permission',
        desc: 'Error title shown when the user wans to scan a QR code but has denied the camera permission',
      );

  String get confirmDeleteStudentMessage => Intl.message(
        'This will unpair and remove all enrollments for this student from your account.',
        desc: 'Confirmation message shown when the user tries to delete a student from their account',
      );

  String get deleteStudentFailure => Intl.message(
        'There was a problem removing this student from your account. Please check your connection and try again.',
      );

  /// Miscellaneous

  String get cancel => Intl.message('Cancel');

  String get next => Intl.message('Next', name: 'next');

  String get ok => Intl.message('OK', name: 'ok');

  String get yes => Intl.message('Yes');

  String get no => Intl.message('No');

  String get retry => Intl.message('Retry');

  String get delete => Intl.message('Delete', desc: 'Label used for general delete/remove actions');

  String get done => Intl.message('Done', desc: 'Label for general done/finished actions');

  String get refresh => Intl.message('Refresh', desc: 'Label for button to refresh data from the web');

  String get viewDescription => Intl.message(
        'View Description',
        desc: 'Button to view the description for an event or assignment',
      );

  String get allyExpanded => Intl.message(
        'expanded',
        desc: 'Description for the accessibility reader for list groups that are expanded',
      );

  String get allyCollapsed => Intl.message(
        'collapsed',
        desc: 'Description for the accessibility reader for list groups that are expanded',
      );

  String get unexpectedError => Intl.message('An unexpected error occurred');

  String get descriptionTitle => Intl.message(
        'Description',
        desc: 'Title for screens that contain only a description from Canavs',
      );

  String get noDescriptionBody =>
      Intl.message('No description', desc: 'Message used when the assignment has no description');

  String get launchExternalTool => Intl.message(
        'Launch External Tool',
        desc: 'Button text added to webviews to let users open external tools in their browser',
      );

  String get webAccessLimitedMessage => Intl.message(
        'Interactions on this page are limited by your institution.',
        desc: 'Message describing how the webview has limited access due to an instution setting',
      );

  String dateAtTime(String date, String time) => Intl.message(
        '$date at $time',
        args: [date, time],
        name: 'dateAtTime',
        desc: 'The string to format dates',
      );

  String dueDateAtTime(String date, String time) => Intl.message(
        'Due $date at $time',
        args: [date, time],
        name: 'dueDateAtTime',
        desc: 'The string to format due dates',
      );

  String get noDueDate => Intl.message(
        'No Due Date',
        desc: 'Label for assignments that do not have a due date',
      );

  String get filter => Intl.message(
        'Filter',
        desc: 'Label for buttons to filter what items are visible',
      );

  String get unread => Intl.message('unread', desc: 'Label for things that are marked as unread');

  String unreadCount(int count) => Intl.message(
        '${count} unread',
        args: [count],
        name: 'unreadCount',
        desc: 'Formatted string for when there are a number of unread items',
      );

  String badgeNumberPlus(int count) => Intl.message(
        '${count}+',
        args: [count],
        name: 'badgeNumberPlus',
        desc: 'Formatted string for when too many items are being notified in a badge, generally something like: 99+',
      );

  String get errorLoadingAnnouncement => Intl.message('There was an error loading this announcement',
      desc: 'Message shown when an announcement detail screen fails to load');

  String get globalAnnouncementTitle =>
      Intl.message('Global Announcement', desc: 'Title text shown for institution level announcements');

  String get genericNetworkError => Intl.message('Network error');

  String get underConstruction => Intl.message('Under Construction');

  String get currentlyBuildingThisFeature =>
      Intl.message('We are currently building this feature for your viewing pleasure.');

  String get loginHelpHint => Intl.message('Request Login Help Button',
      desc: 'Accessibility hint for button that opens help dialog for a login help request');

  String get loginHelpTitle =>
      Intl.message('Request Login Help', desc: 'Title of help dialog for a login help request');

  String get loginHelpSubject =>
      Intl.message('I\'m having trouble logging in', desc: 'Subject of help dialog for a login help request');

  String get routerLaunchErrorMessage => Intl.message('An error occurred when trying to display this link',
      desc: 'Error message shown when a link can\'t be opened');

  String get routerErrorMessage => Intl.message(
      'We are unable to display this link, it may belong to an institution you currently aren\'t logged in to.',
      desc: 'Description for error page shown when clicking a link');

  String get routerErrorTitle => Intl.message('Link Error', desc: 'Title for error page shown when clicking a link');

  String get openInBrowser => Intl.message('Open In Browser', desc: 'Text for button to open a link in the browswer');

  String get qrCodeExplanation => Intl.message(
      'You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list.',
      desc: 'Text for qr login tutorial screen');

  String get loginWithQRCode => Intl.message('QR Code', desc: 'Text for qr login button');

  String get locateQRCode => Intl.message('Locate QR Code', desc: 'Text for qr login button');

  String get invalidQRCodeError =>
      Intl.message('Please scan a QR code generated by Canvas', desc: 'Text for qr login error with incorrect qr code');

  String get loginWithQRCodeError =>
      Intl.message('There was an error logging in. Please generate another QR Code and try again.',
          desc: 'Text for qr login error');

  String get qrCodeScreenshotContentDescription =>
      Intl.message('Screenshot showing location of QR code generation in browser',
          desc: 'Content Description for qr login tutorial screenshot');

  String get qrCodeNoCameraError =>
      Intl.message('QR scanning requires camera access', desc: 'placeholder for camera error for QR code scan');

  String get lockedForUserError =>
      Intl.message('The linked item is no longer available', desc: 'error message when the alert could no be opened');

  String get lockedForUserTitle =>
      Intl.message('Locked', desc: 'title for locked alerts');

  String get messageSent =>
      Intl.message('Message sent', desc: 'confirmation message on the screen when the user succesfully sends a message');

  String get acceptableUsePolicyTitle =>
      Intl.message('Acceptable Use Policy', desc: 'title for the acceptable use policy screen');

  String get acceptableUsePolicyConfirm =>
      Intl.message('Submit', desc: 'submit button title for acceptable use policy screen');

  String get acceptableUsePolicyDescription =>
      Intl.message('Either you\'re a new user or the Acceptable Use Policy has changed since you last agreed to it. Please agree to the Acceptable Use Policy before you continue.', desc: 'acceptable use policy screen description');

  String get acceptableUsePolicyAgree =>
      Intl.message('I agree to the Acceptable Use Policy.', desc: 'acceptable use policy switch title');

  String get about =>
      Intl.message('About', desc: 'Title for about menu item in settings');

  String get aboutAppTitle =>
      Intl.message('App', desc: 'Title for App field on about page');

  String get aboutDomainTitle =>
      Intl.message('Domain', desc: 'Title for Domain field on about page');

  String get aboutLoginIdTitle =>
      Intl.message('Login ID', desc: 'Title for Login ID field on about page');

  String get aboutEmailTitle =>
      Intl.message('Email', desc: 'Title for Email field on about page');

  String get aboutVersionTitle =>
      Intl.message('Version', desc: 'Title for Version field on about page');

  String get aboutLogoSemanticsLabel =>
      Intl.message('Instructure logo', desc: 'Semantics label for the Instructure logo on the about page');

  String get needToEnablePermission =>
      Intl.message('You need to enable exact alarm permission for this action', desc: 'Error message when the user tries to set a reminder without the permission');

  String get submissionAndRubric => Intl.message(
      'Submission & Rubric',
      desc: 'Button text for Submission and Rubric on Assignment Details Screen'
  );

  String get submission => Intl.message(
      'Submission',
      desc: 'Title for WebView screen when opening submission'
  );
}
