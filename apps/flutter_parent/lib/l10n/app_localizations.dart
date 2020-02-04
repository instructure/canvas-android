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
      Locale('en', ''), // First so it's our fallback

      // Supported languages
      Locale('ar', ''),
      Locale('cy', ''),
      Locale('da', ''),
      Locale('de', ''),
      Locale('en', 'AU'),
      Locale('en', 'CY'),
      Locale('en', 'GB'),
      Locale('es', ''),
      Locale('fi', ''),
      Locale('fr', ''),
      Locale('fr', 'CA'),
      Locale('ht', ''),
      Locale('ja', ''),
      Locale('mi', ''),
      Locale('nb', ''),
      Locale('nl', ''),
      Locale('pl', ''),
      Locale('pl', ''),
      Locale('pl', 'BR'),
      Locale('pl', 'PT'),
      Locale('ru', ''),
      Locale('sl', ''),
      Locale('sv', ''),
      Locale('zh', ''),
      Locale('zh', 'HK'),

      // Custom language packs
      Locale.fromSubtags(languageCode: 'da', scriptCode: 'instk12'),
      Locale.fromSubtags(languageCode: 'en', scriptCode: 'unimelb', countryCode: 'AU'),
      Locale.fromSubtags(languageCode: 'en', scriptCode: 'instukhe', countryCode: 'GB'),
      Locale.fromSubtags(languageCode: 'nb', scriptCode: 'instk12'),
      Locale.fromSubtags(languageCode: 'sv', scriptCode: 'instk12'),
    ];
  }

  @override
  bool isSupported(Locale locale) => _isSupported(locale, true);

  @override
  Future<AppLocalizations> load(Locale locale) => AppLocalizations._load(locale);

  @override
  bool shouldReload(LocalizationsDelegate<AppLocalizations> old) => false;

  LocaleResolutionCallback resolution({Locale fallback, bool matchCountry = true}) {
    return (Locale locale, Iterable<Locale> supported) {
      return _resolve(locale, fallback, supported, matchCountry);
    };
  }

  ///
  /// Returns true if the specified locale is supported, false otherwise.
  ///
  bool _isSupported(Locale locale, bool matchCountry) {
    if (locale == null) {
      return false;
    }

    // Must match language code, must match country code if specified
    return supportedLocales.any((Locale supportedLocale) =>
        (supportedLocale.languageCode == locale.languageCode) &&
        ((supportedLocale.countryCode == locale.countryCode) ||
            (true != matchCountry && (supportedLocale.countryCode == null || supportedLocale.countryCode.isEmpty))));
  }

  ///
  /// Internal method to resolve a locale from a list of locales.
  ///
  Locale _resolve(Locale locale, Locale fallback, Iterable<Locale> supported, bool matchCountry) {
    if (locale == null || !_isSupported(locale, matchCountry)) {
      return fallback ?? supported.first;
    }

    if (supported.contains(locale)) {
      return locale;
    } else {
      return Locale(locale.languageCode, '');
    }
  }
}

AppLocalizations L10n(BuildContext context) => Localizations.of<AppLocalizations>(context, AppLocalizations);

///
/// App Localization class.
///
/// This will hold all of the strings and reference the right resources depending on locale.
/// View the README for detailed instructions on usage.
///
class AppLocalizations {
  static Future<AppLocalizations> _load(Locale locale) {
    final String localeName =
        (locale.countryCode == null && locale.scriptCode == null) ? locale.languageCode : locale.toString();

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

  String get shareFeedback =>
      Intl.message('Send us feedback', desc: 'Label text for the nav drawer button to share feedback');

  String get signOut => Intl.message('Sign Out', desc: 'Label text for the Sign Out nav drawer button');

  String get switchUsers => Intl.message('Switch Users', desc: 'Label text for the Switch Users nav drawer button');

  String appVersion(String version) => Intl.message('v. $version',
      name: 'appVersion', args: [version], desc: 'App version shown in the navigation drawer');

  /// Login landing screen

  String get previousLogins => Intl.message('Previous Logins', desc: 'Label for the list of previous user logins');

  String get canvasLogoLabel {
    return Intl.message('Canvas logo', name: 'canvaslogoLabel', desc: 'The semantics label for the Canvas logo');
  }

  String get findSchoolOrDistrict => Intl.message(
        'Find School or District',
        name: 'findSchoolOrDistrict',
        desc: 'Text for the find-my-school button',
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
        args: [count],
        examples: const {'count': 5},
      );

  String get attachmentFailed => Intl.message('Failed. Tap for options.',
      desc: 'Short message shown on a message attachment when uploading has failed');

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
      desc: 'Author info for a single-recipient message; includes both the author name and the recipient name.',
    );
  }

  String authorToNOthers(String authorName, int howMany) {
    return Intl.plural(
      howMany,
      one: '$authorName to 1 other',
      other: '$authorName to $howMany others',
      args: [authorName, howMany],
      desc: 'Author info for a mutli-recipient message; includes the author name and the number of recipients',
    );
  }

  String authorToRecipientAndNOthers(String authorName, String recipientName, int howMany) {
    return Intl.plural(
      howMany,
      one: '$authorName to $recipientName & 1 other',
      other: '$authorName to $recipientName & $howMany others',
      args: [authorName, howMany],
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
        'There was an error loading your your student\’s courses.',
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

  String assignmentSubjectMessage(String studentName, String assignmentName) => Intl.message(
        'Regarding: $studentName, Assignment - $assignmentName',
        desc: 'The subject line for a message to a teacher regarding a student\'s assignment',
        name: 'assignmentSubjectMessage',
        args: [studentName, assignmentName],
      );

  /// Assignment Details Screen

  String get assignmentDetailsTitle =>
      Intl.message('Assignment Details', desc: 'Title for the page that shows details for an assignment');

  String assignmentTotalPoints(String points) => Intl.message('$points pts',
      name: 'assignmentTotalPoints', desc: 'Label used for the total points the assignment is worth');

  String assignmentTotalPointsAccessible(String points) => Intl.message('$points points',
      name: 'assignmentTotalPointsAccessible',
      desc: 'Screen reader label used for the total points the assignment is worth');

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

  String get assignmentRemindMeSwitch =>
      Intl.message('Set reminder switch', desc: 'Label for the switch to set a reminder');

  String get assignmentRemindMeDescription =>
      Intl.message('Set a date and time to be notified of this specific assignment.',
          desc: 'Description for row to set reminders');

  String get assignmentRemindMeSet =>
      Intl.message('You will be notified about this assignment on…', desc: 'Description for when a reminder is set');

  String get assignmentDescriptionLabel =>
      Intl.message('Description', desc: 'Label for the description of the assignment');

  String get assignmentInstructionsLabel =>
      Intl.message('Instructions', desc: 'Label for the description of the assignment when it has quiz instructions');

  String get assignmentNoDescriptionBody =>
      Intl.message('No description', desc: 'Message used when the assignment has no description');

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

  String get courseAnnouncement => Intl.message(
        'Course Announcement',
        desc: 'Title for alerts when there is a course announcement',
      );

  String get institutionAnnouncement => Intl.message(
        'Institution Announcement',
        desc: 'Title for alerts when there is an institution announcement',
      );

  String assignmentGradeAboveThreshold(String threshold) => Intl.message(
        'Assignment Grade Above $threshold',
        name: 'assignmentGradeAbove',
        args: [threshold],
        desc: 'Title for alerts when an assignment grade is above the threshold value',
      );

  String assignmentGradeBelowThreshold(String threshold) => Intl.message(
        'Assignment Grade Below $threshold',
        name: 'assignmentGradeBelow',
        args: [threshold],
        desc: 'Title for alerts when an assignment grade is below the threshold value',
      );

  String courseGradeAboveThreshold(String threshold) => Intl.message(
        'Course Grade Above $threshold',
        name: 'courseGradeAbove',
        args: [threshold],
        desc: 'Title for alerts when a course grade is above the threshold value',
      );

  String courseGradeBelowThreshold(String threshold) => Intl.message(
        'Course Grade Below $threshold',
        name: 'courseGradeBelow',
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

  String get appearance => Intl.message('Appearance', desc: 'Label for the appearance section in the settings page');

  /// Grade cell

  String get submissionStatusSuccessTitle => Intl.message('Successfully submitted!',
      desc: 'Title displayed in the grade cell for an assignment that has been submitted');

  String submissionStatusSuccessSubtitle(String date, String time) {
    return Intl.message(
      'This assignment was submitted on $date at $time and is waiting to be graded',
      desc: 'Subtitle displayed in the grade cell for an assignment that has been submitted and is awaiting a grade',
      args: [date, time],
    );
  }

  String outOfPoints(String points, num howMany) => Intl.plural(
        howMany,
        one: 'Out of 1 point',
        other: 'Out of $points points',
        desc: '',
        args: [points],
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

  String latePenalty(String pointsLost) => Intl.message(
        'Late penalty (-$pointsLost)',
        desc: 'Text displayed when a late penalty has been applied to the assignment',
        args: [pointsLost],
      );

  String finalGrade(String grade) => Intl.message(
        'Final Grade: $grade',
        desc: 'Text that displays the final grade of an assignment',
        args: [grade],
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

  String get institutionAnnouncements => Intl.message('Institution Announcements');

  String get never => Intl.message('Never',
      desc: 'Indication that tells the user they will not receive alert notifications of a specific kind');

  String get gradePercentage => Intl.message('Grade percentage');

  String get alertThresholdsLoadingError => Intl.message('There was an error loading your student\'s alerts.');

  String get mustBeBelow100 => Intl.message('Must be below 100');

  String mustBeBelowN(int percentage) => Intl.message(
        'Must be below $percentage',
        desc: 'Validation error to the user that they must choose a percentage below \'n\'',
        args: [percentage],
        examples: const {'percentage': 5},
      );

  String mustBeAboveN(int percentage) => Intl.message(
        'Must be above $percentage',
        desc: 'Validation error to the user that they must choose a percentage above \'n\'',
        args: [percentage],
        examples: const {'percentage': 5},
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

  String get pairingCodeEntryExplanation => Intl.message(
      'Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired');

  String get errorPairingFailed => Intl.message('Your code is incorrect or expired.');

  String get qrCode => Intl.message('QR Code');

  String get addNewStudent =>
      Intl.message('Add new student', desc: 'Semantics label for the FAB on the Manage Students Screen');

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

  /// Miscellaneous

  String get cancel => Intl.message('Cancel');

  String get next => Intl.message('Next', name: 'next');

  String get ok => Intl.message('OK', name: 'ok');

  String get yes => Intl.message('Yes');

  String get no => Intl.message('No');

  String get retry => Intl.message('Retry');

  String get delete => Intl.message('Delete', desc: 'Label used for general delete/remove actions');

  String get done => Intl.message('Done', desc: 'Label for general done/finished actions');

  String get unexpectedError => Intl.message('An unexpected error occurred');

  String get dateTimeFormat => Intl.message(
        "MMM d 'at' h:mma",
        desc:
            "The string to format dates, only the 'at' needs to be translated, as well as arranging the date/time components. MMM will show the month abbreviated as 'Oct', d shows the day, h:mma will show the time as '10:33PM'. Use a captial H for 24 hour times, in which case the 'a' can be omitted to remove the am/pm from the string.",
      );

  String get dueDateTimeFormat => Intl.message(
        "'Due' MMM d 'at' h:mma",
        desc:
            "The string to format dates, only the 'Due' and 'at' needs to be translated (not including apostrophes), as well as arranging the date/time components. MMM will show the month abbreviated as 'Oct', d shows the day, h:mma will show the time as '10:33PM'. Use a captial H for 24 hour times, in which case the 'a' can be omitted to remove the am/pm from the string.",
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

  String get institutionAnnouncementTitle =>
      Intl.message('Institution Announcement', desc: 'Title text shown for institution level announcements');

  String get genericNetworkError => Intl.message('Network error');

  String get underConstruction => Intl.message('Under Construction');
  String get currentlyBuildingThisFeature =>
      Intl.message('We are currently building this feature for your viewing pleasure.');
}
