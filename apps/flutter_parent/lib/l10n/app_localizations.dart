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

  /// Miscellaneous

  String get next => Intl.message('Next', name: 'next');

  String get ok => Intl.message('OK', name: 'ok');

  String get unexpectedError => Intl.message('An unexpected error occurred');
}
