/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

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
      Locale("en", ""), // First so it's our fallback

      // Supported languages
      Locale("ar", ""),
      Locale("cy", ""),
      Locale("da", ""),
      Locale("de", ""),
      Locale("en", "AU"),
      Locale("en", "CY"),
      Locale("en", "GB"),
      Locale("es", ""),
      Locale("fi", ""),
      Locale("fr", ""),
      Locale("fr", "CA"),
      Locale("ht", ""),
      Locale("ja", ""),
      Locale("mi", ""),
      Locale("nb", ""),
      Locale("nl", ""),
      Locale("pl", ""),
      Locale("pl", ""),
      Locale("pl", "BR"),
      Locale("pl", "PT"),
      Locale("ru", ""),
      Locale("sl", ""),
      Locale("sv", ""),
      Locale("zh", ""),
      Locale("zh", "HK"),

      // Custom language packs
      Locale.fromSubtags(languageCode: "da", scriptCode: "instk12"),
      Locale.fromSubtags(languageCode: "en", scriptCode: "unimelb", countryCode: "AU"),
      Locale.fromSubtags(languageCode: "en", scriptCode: "instukhe", countryCode: "GB"),
      Locale.fromSubtags(languageCode: "nb", scriptCode: "instk12"),
      Locale.fromSubtags(languageCode: "sv", scriptCode: "instk12"),
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

    final languageLocale = Locale(locale.languageCode, "");
    if (supported.contains(locale)) {
      return locale;
    } else if (supported.contains(languageLocale)) {
      return languageLocale;
    } else {
      return fallback ?? supported.first;
    }
  }
}

AppLocalizations L10n(BuildContext context) => AppLocalizations.of(context);

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

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
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

  /// Navigation Drawer
  String get manageStudents =>
      Intl.message('Manage Students', desc: 'Label text for the Manage Students nav drawer button');

  String get help => Intl.message('Help', desc: 'Label text for the help nav drawer button');

  String get signOut => Intl.message('Sign Out', desc: 'Label text for the Sign Out nav drawer button');

  String appVersion(String version) => Intl.message('v. $version',
      name: 'appVersion', args: [version], desc: 'App version shown in the navigation drawer');

  /// Login landing screen

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
        'Enter school name or district...',
        name: 'domainSearchInputHint',
        desc: "Input hint for the text box on the domain search screen",
      );

  String noDomainResults(String query) => Intl.message(
        "Unable to find schools matching '$query'",
        name: 'noDomainResults',
        args: [query],
        desc: 'Message shown to users when the domain search query did not return any results',
      );

  String get domainSearchHelpLabel => Intl.message(
        "How do I find my school or district?",
        name: 'domainSearchHelpLabel',
        desc: 'Label for the help button on the domain search screen',
      );

  String get canvasGuides => Intl.message(
        'Canvas Guides',
        name: 'canvasGuides',
        desc:
            "Proper name for the Canvas Guides. This will be used in the domainSearchHelpBody text and will be highlighted and clickable",
      );

  String get canvasSupport => Intl.message(
        'Canvas Support',
        name: 'canvasSupport',
        desc:
            "Proper name for Canvas Support. This will be used in the domainSearchHelpBody text and will be highlighted and clickable",
      );

  String domainSearchHelpBody(String canvasGuides, String canvasSupport) => Intl.message(
      """Try searching for the name of the school or district you’re attempting to access, like “Smith Private School” or “Smith County Schools.” You can also enter a Canvas domain directly, like “smith.instructure.com.”\n\nFor more information on finding your institution’s Canvas account, you can visit the $canvasGuides, reach out to $canvasSupport, or contact your school for assistance.""",
      name: 'domainSearchHelpBody',
      desc: 'The body text shown in the help dialog on the domain search screen',
      args: [canvasGuides, canvasSupport]);

  /// Inbox

  String get inbox => Intl.message('Inbox', desc: 'Title for the Inbox screen');

  String get errorLoadingMessages => Intl.message('There was an error loading your inbox messages',
      desc: 'Message shown when an error occured while loading inbox messages');

  String get noSubject => Intl.message('No Subject', desc: 'Title used for inbox messages that have no subject');

  String get errorFetchingCourses =>
      Intl.message("Unable to fetch courses. Please check your connection and try again.",
          desc: "Message shown when an error occured while loading courses");

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

  String get newMessageTitle => Intl.message('New message', desc: "Title of the new-message screen");

  String get addAttachment =>
      Intl.message("Add attachment", desc: 'Tooltip for the add-attachment button in the new-message screen');

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

  String get attachmentFailed => Intl.message('Failed. Tap for options.',
      desc: 'Short message shown on a message attachment when uploading has failed');

  /// Courses Screen

  String get noGrade => Intl.message(
        'No Grade',
        desc: 'Message shown when there is currently no grade available for a course',
      );

  /// Course Details Screen

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

  /// Alerts Screen

  String get noAlertsMessage => Intl.message(
        'This student has no alerts to view.',
        desc: 'The empty message to show to users when there are no alerts for the student.',
      );

  /// Enrollment types

  String get enrollmentTypeTeacher => Intl.message('Teacher', desc: 'Label for the Teacher enrollment type');

  String get enrollmentTypeStudent => Intl.message('Student', desc: 'Label for the Student enrollment type');

  String get enrollmentTypeTA => Intl.message('TA',
      desc:
          "Label for the Teaching Assistant enrollment type (also known as Teacher's Aid or Education Assistant), reduced to a short acronym/initialism if appropriate.");

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

  /// Miscellaneous

  String get next => Intl.message('Next', name: 'next');

  String get ok => Intl.message('OK', name: 'ok');

  String get yes => Intl.message('Yes');

  String get no => Intl.message('No');

  String get retry => Intl.message("Retry");

  String get delete => Intl.message("Delete", desc: 'Label used for general delete/remove actions');

  String get done => Intl.message('Done', desc: 'Label for general done/finished actions');

  String get unexpectedError => Intl.message('An unexpected error occurred');

  String get dateTimeFormat => Intl.message(
        "MMM d 'at' h:mma",
        desc:
            "The string to format dates, only the 'at' needs to be translated. MMM will show the month abbreviated as 'Oct', d shows the day, h:mma will show the time as '10:33PM'.",
      );
}
