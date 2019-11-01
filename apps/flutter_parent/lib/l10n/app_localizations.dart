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
}
