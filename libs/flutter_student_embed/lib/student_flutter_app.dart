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
import 'package:flutter/scheduler.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_screen.dart';
import 'package:flutter_student_embed/utils/design/student_theme.dart';
import 'package:flutter_student_embed/utils/native_comm.dart';

class StudentFlutterApp extends StatefulWidget {
  @override
  _StudentFlutterAppState createState() => _StudentFlutterAppState();
}

class _StudentFlutterAppState extends State<StudentFlutterApp> {
  Locale _locale = Locale('en'); // will be updated by the localeResolutionCallback

  GlobalKey<NavigatorState> _navKey = GlobalKey();

  rebuild(locale) {
    setState(() => _locale = locale);
  }

  @override
  void initState() {
    super.initState();

    // Set up routeToCalendar callback, called when the app should route to a new CalendarScreen
    NativeComm.routeToCalendar = (channelId) {
      _navKey.currentState.push(
        // Use a route with a non-animated 'pop' transition
        PageRouteBuilder(
          pageBuilder: (_, __, ___) => CalendarScreen(channelId: channelId),
          settings: RouteSettings(name: CalendarScreen.routeName), // Set route name for the benefit of ShouldPopTracker
          transitionsBuilder: (_, __, ___, child) => child,
          transitionDuration: Duration.zero,
        ),
      );
    };

    // Set up resetRoute callback to clear the back stack, called on logout or user switch
    NativeComm.resetRoute = () {
      _navKey.currentState.pushAndRemoveUntil(MaterialPageRoute(builder: (context) => Material()), (_) => false);
    };
  }

  @override
  Widget build(BuildContext context) {
    return StudentTheme(
      builder: (context, themeData) => MaterialApp(
        debugShowCheckedModeBanner: false,
        navigatorKey: _navKey,
        title: '',
        // No title since this will be embedded and will not represent a full-screen component
        locale: _locale,
        //navigatorObservers: [NativeComm.routeTracker],
        localizationsDelegates: const [
          AppLocalizations.delegate,
          // Material components use these delegates to provide default localization
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
        ],
        supportedLocales: AppLocalizations.delegate.supportedLocales,
        localeResolutionCallback: _localeCallback(),
        theme: themeData,
        home: Material(), // Home should be an empty screen
      ),
    );
  }

  // Get notified when there's a new system locale so we can rebuild the app with the new language
  LocaleResolutionCallback _localeCallback() => (locale, supportedLocales) {
        // If there is no user locale, they want the system locale. If there is a user locale, we should use it over the system locale
        Locale newLocale = ApiPrefs.getUser()?.locale == null ? locale : ApiPrefs.effectiveLocale();

        const fallback = Locale('en');
        Locale resolvedLocale =
            AppLocalizations.delegate.resolution(fallback: fallback, matchCountry: false)(newLocale, supportedLocales);

        // Update the state if the locale changed
        if (_locale != resolvedLocale) {
          SchedulerBinding.instance.addPostFrameCallback((_) {
            setState(() => _locale = resolvedLocale);
          });
        }

        return resolvedLocale;
      };
}
