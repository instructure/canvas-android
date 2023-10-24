// Copyright (C) 2020 - present Instructure, Inc.
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
import 'package:flutter_student_embed/l10n/generated/messages_all.dart';
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
//      Locale('cy'), // Not supported by material localizations
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
//      Locale('ht'), // Not supported by material localizations
      Locale('is'),
      Locale('it'),
      Locale('ja'),
//      Locale('mi'), // Not supported by material localizations
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

  LocaleResolutionCallback resolution({Locale fallback, bool matchCountry = true}) {
    return (Locale locale, Iterable<Locale> supported) {
      return _resolve(locale, fallback, supported, matchCountry);
    };
  }

  ///
  /// Returns true if the specified locale is supported, false otherwise.
  ///
  Locale _isSupported(Locale locale, bool shouldMatchCountry) {
    if (locale == null) {
      return null;
    }

    // Must match language code and script code.
    // Must match country code if specified or will fall back to the generic language pack if we don't match country code.
    // i.e., match a passed in 'zh-Hans' to the supported 'zh' if [matchCountry] is false
    return supportedLocales.firstWhere((Locale supportedLocale) {
      final matchLanguage = (supportedLocale.languageCode == locale.languageCode);
      final matchScript = (locale.scriptCode == supportedLocale.scriptCode);
      final matchCountry = (supportedLocale.countryCode == locale.countryCode);
      final matchCountryFallback =
          (true != shouldMatchCountry && (supportedLocale.countryCode == null || supportedLocale.countryCode.isEmpty));

      return matchLanguage && matchScript && (matchCountry || matchCountryFallback);
    }, orElse: () => null);
  }

  ///
  /// Internal method to resolve a locale from a list of locales.
  ///
  Locale _resolve(Locale locale, Locale fallback, Iterable<Locale> supported, bool matchCountry) {
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

AppLocalizations L10n(BuildContext context) => Localizations.of<AppLocalizations>(context, AppLocalizations);

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

  String get coursesLabel {
    return Intl.message('Courses', name: 'coursesLabel', desc: 'The label for the Courses tab');
  }

  /// Plalendar

  String get calendar => Intl.message('Calendar', desc: 'Title of the calendar screen');

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
        'There was an error loading your calendar',
        desc: 'Message displayed when calendar events could not be loaded for the current student',
      );

  String get calendarSelectFavoriteCalendars => Intl.message('Select elements to display on the calendar.',
      desc: 'Select calendars description');

  String get gotoTodayButtonLabel =>
      Intl.message('Go to today', desc: 'Accessibility label used for the today button in the planner');

  /// To Do (Planner Note)

  String get toDo => Intl.message('To Do', desc: 'Label used for To-Do items in the planner');

  String get noToDoDescription => Intl.message(
        'There\'s no description yet',
        desc: 'Message used when an item has no description',
      );

  String get date => Intl.message('Date', desc: 'Label used for the date/time section');

  String get edit => Intl.message('Edit', desc: 'Label for "edit" actions');

  String get newToDo => Intl.message('New To Do', desc: 'Title of the screen for creating new To Do items');

  String get editToDo => Intl.message('Edit To Do', desc: 'Title of the screen for editing a To Do item');

  String get toDoTitleHint => Intl.message('Title', desc: 'Hint shown for the title input when creating To Do items');

  String get toDoCourseLabel => Intl.message(
        'Course (optional)',
        desc: 'Label for optional course selection when creating To Do items',
      );

  String get toDoCourseNone => Intl.message(
        'None',
        desc: 'Label used when no course is selected when creating To Do items',
      );

  String get toDoDescriptionHint => Intl.message(
        'Description',
        desc: 'Hint shown for the description input when creating To Do items',
      );

  String get save => Intl.message('Save', desc: 'Label for the "save" action');

  String get areYouSure => Intl.message(
        'Are You Sure?',
        desc: 'Title of the dialog shown when the user tries to perform an action that requires confirmation',
      );

  String get deleteToDoConfirmationMessage => Intl.message(
        'Do you want to delete this To Do item?',
        desc: 'Message of the dialog shown when the user tries to delete a To Do item',
      );

  String get unsavedChangesDialogTitle => Intl.message('Unsaved changes',
      desc: 'Title of the dialog shown when the user tries to leave with unsaved changes');

  String get unsavedChangesDialogBody =>
      Intl.message('Are you sure you wish to close this page? Your unsaved changes will be lost.',
          desc: 'Body text of the dialog shown when the user tries leave with unsaved changes');

  String get titleEmptyErrorMessage => Intl.message(
        'Title must not be empty',
        desc: 'Error message shown when the users attempts to save with an empty title',
      );

  String get errorSavingToDo =>
      Intl.message('There was an error saving this To Do. Please check your connection and try again.');

  String get errorDeletingToDo =>
      Intl.message('There was an error deleting this To Do. Please check your connection and try again.');

  String get descriptionLabel => Intl.message('Description', desc: 'Label for the description of an item');

  /// Crash screen

  String get crashScreenTitle =>
      Intl.message('Uh oh!', desc: 'Title of the screen that shows when a crash has occurred');

  String get crashScreenMessage =>
      Intl.message('We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening.',
          desc: 'Message shown when a crash has occurred');

  String get crashScreenViewDetails =>
      Intl.message('View error details', desc: 'Label for the button that allowed users to view crash details');

  String get crashDetailsAppVersion =>
      Intl.message('Application version', desc: 'Label for the application version displayed in the crash details');

  String get crashDetailsDeviceModel =>
      Intl.message('Device model', desc: 'Label for the device model displayed in the crash details');

  String get crashDetailsAndroidVersion => Intl.message('Android OS version',
      desc: 'Label for the Android operating system version displayed in the crash details');

  String get crashDetailsFullMessage =>
      Intl.message('Full error message', desc: 'Label for the full error message displayed in the crash details');

  /// Calendar filter list screen

  String get noCoursesTitle => Intl.message('No Courses', desc: 'Title for having no courses');

  String get noCoursesMessage =>
      Intl.message('Your student\’s courses might not be published yet.', desc: 'Message for having no courses');

  String get errorLoadingCourses => Intl.message(
        'There was an error loading your your student\’s courses.',
        desc: 'Message displayed when the list of student courses could not be loaded',
      );

  /// Calendar day list tile

  String courseToDo(String courseName) => Intl.message(
        '$courseName To Do',
        name: 'courseToDo',
        args: [courseName],
        desc:
            'Label used for course-specific To-Do items in the planner, where the course name is used as an adjective to describe the type of To Do',
      );

  String get assignmentGradedLabel => Intl.message(
        'Graded',
        desc: 'Label for assignments that have been graded',
      );

  String get assignmentSubmittedLabel => Intl.message(
        'Submitted',
        desc: 'Label for assignments that have been submitted',
      );

  String assignmentTotalPoints(String points) => Intl.message(
        '$points pts',
        name: 'assignmentTotalPoints',
        args: [points],
        desc: 'Label used for the total points the assignment is worth',
      );

  String get excused => Intl.message('Excused', desc: 'Grading status for an assignment marked as excused');

  String get missing => Intl.message(
        'Missing',
        desc: 'Description for when a student has not turned anything in for an assignment',
      );

  /// Miscellaneous

  String get cancel => Intl.message('Cancel');

  String get yes => Intl.message('Yes');

  String get no => Intl.message('No');

  String get retry => Intl.message('Retry');

  String get delete => Intl.message('Delete', desc: 'Label used for general delete/remove actions');

  String get done => Intl.message('Done', desc: 'Label for general done/finished actions');

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
}
