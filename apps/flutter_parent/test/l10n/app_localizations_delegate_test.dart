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

import 'dart:ui';

import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:test/test.dart';

void main() {
  test('shouldReload returns false', () {
    expect(AppLocalizations.delegate.shouldReload(AppLocalizations.delegate), false);
  });

  /// isSupported tests

  test('isSupported returns true for supported language', () {
    final locale = Locale('en', '');
    expect(AppLocalizations.delegate.isSupported(locale), true);
  });

  test('isSupported returns true for supported custom language pack', () {
    final locale = Locale.fromSubtags(languageCode: 'en', scriptCode: 'unimelb', countryCode: 'AU');
    expect(AppLocalizations.delegate.isSupported(locale), true);
  });

  test('isSupported returns true for supported custom language pack without a country code', () {
    final locale = Locale.fromSubtags(languageCode: 'sv', scriptCode: 'instk12');
    expect(AppLocalizations.delegate.isSupported(locale), true);
  });

  test('isSupported returns false for unsupported language', () {
    final locale = Locale('aa', '');
    expect(AppLocalizations.delegate.isSupported(locale), false);
  });

  test('isSupported returns false for unsupported custom language pack', () {
    final locale = Locale.fromSubtags(languageCode: 'en', scriptCode: 'bleminu', countryCode: 'AU');
    expect(AppLocalizations.delegate.isSupported(locale), true);
  });

  test('isSupported returns false for unsupported country', () {
    final locale = Locale('en', 'AA');
    expect(AppLocalizations.delegate.isSupported(locale), false);
  });

  /// resolution tests

  test('resolution callback returns locale when supported', () {
    final fallback = Locale('en', '');
    final locale = Locale('es', '');

    _testLocaleResolution(fallback, locale, locale);
  });

  test('resolution callback returns locale without country code when language with a country code is unsupported', () {
    final fallback = Locale('en', '');
    final locale = Locale('pl', 'AA');
    final expected = Locale('pl', '');

    _testLocaleResolution(fallback, locale, expected, matchCountry: false);
  });

  test('resolution callback returns fallback when language with a country code is unsupported', () {
    final fallback = Locale('en', '');
    final locale = Locale('pl', 'AA');

    _testLocaleResolution(fallback, locale, fallback);
  });

  test('resolution callback returns custom language locale when supported', () {
    final fallback = Locale('en', '');
    final locale = Locale.fromSubtags(languageCode: 'en', scriptCode: 'unimelb', countryCode: 'AU');

    _testLocaleResolution(fallback, locale, locale);
  });

  test('resolution callback returns custom language locale without a country code when supported', () {
    final fallback = Locale('en', '');
    final locale = Locale.fromSubtags(languageCode: 'sv', scriptCode: 'instk12');

    _testLocaleResolution(fallback, locale, locale);
  });

  test('resolution callback returns fallback locale when unsupported', () {
    final fallback = Locale('en', '');
    final locale = Locale('aa', '');

    _testLocaleResolution(fallback, locale, fallback);
  });

  test('resolution callback returns fallback locale when custom language locale is unsupported', () {
    final fallback = Locale('en', '');
    final locale = Locale.fromSubtags(languageCode: 'en', scriptCode: 'bleminu', countryCode: 'AU');

    _testLocaleResolution(fallback, locale, fallback);
  });

  test('resolution callback returns locale when custom language without a country code is unsupported', () {
    final fallback = Locale('en', '');
    final locale = Locale.fromSubtags(languageCode: 'sv', scriptCode: '12inst');
    final expected = Locale('sv', '');

    _testLocaleResolution(fallback, locale, expected);
  });

  test('resolution callback returns en locale when no fallback and locale is unsupported', () {
    final locale = Locale('aa', '');
    final expected = Locale('en', '');

    _testLocaleResolution(null, locale, expected);
  });
}

_testLocaleResolution(Locale fallback, Locale resolving, Locale expected, {bool matchCountry = true}) {
  final callback = AppLocalizations.delegate.resolution(fallback: fallback, matchCountry: matchCountry);
  final actual = callback(resolving, AppLocalizations.delegate.supportedLocales);

  expect(actual, expected);
}
