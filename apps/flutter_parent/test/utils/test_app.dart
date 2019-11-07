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

import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:shared_preferences/shared_preferences.dart';

class TestApp extends StatefulWidget {
  TestApp(this.home, {Map<String, dynamic> mockPrefs = const {}}) : this.mockPrefs = mockPrefs;

  final Widget home;
  final Map<String, dynamic> mockPrefs;

  @override
  _TestAppState createState() => _TestAppState();
}

class _TestAppState extends State<TestApp> {
  Locale _locale;

  rebuild(locale) {
    setState(() => _locale = locale);
  }

  @override
  void initState() {
    super.initState();

    // So that widget tests don't fail when a screen uses shared preferences. Provide values in the constructor
    SharedPreferences.setMockInitialValues(widget.mockPrefs);

    // Probably don't want to do an async set state here, but it's better than calling ApiPrefs.init in _every_ test
    ApiPrefs.init().then((_) {
      SchedulerBinding.instance.addPostFrameCallback((_) {
        // If we're not mounted, can't set state
        if (mounted) setState(() => _locale = ApiPrefs.effectiveLocale());
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Canvas Parent',
      locale: _locale,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        // Material components use these delegate to provide default localization
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ],
      supportedLocales: AppLocalizations.delegate.supportedLocales,
      localeResolutionCallback: _localeCallback(),
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: widget.home,
    );
  }

  // Get notified when there's a new system locale so we can rebuild the app with the new language
  LocaleResolutionCallback _localeCallback() => (locale, supportedLocales) {
        const fallback = Locale("en", "");
        Locale resolvedLocale =
            AppLocalizations.delegate.resolution(fallback: fallback, matchCountry: false)(locale, supportedLocales);

        // Update the state if the locale changed
        if (_locale != resolvedLocale) {
          SchedulerBinding.instance.addPostFrameCallback((_) {
            setState(() => _locale = resolvedLocale);
          });
        }

        return resolvedLocale;
      };
}
