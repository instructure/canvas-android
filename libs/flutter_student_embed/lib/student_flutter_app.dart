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
        builder: (context, widget) {
          // Workaround for scroll physics bug, see [_ParentlessScrollBehavior]
          return ScrollConfiguration(
            behavior: _ParentlessScrollBehavior(),
            child: widget,
          );
        },
        debugShowCheckedModeBanner: false,
        navigatorKey: _navKey,
        title: '',
        // No title since this will be embedded and will not represent a full-screen component
        locale: _locale,
        navigatorObservers: [NativeComm.routeTracker],
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

/// A modified version of [_MaterialScrollBehavior] that works around a very odd bug where the contents of scrollable
/// containers (most notably PageViews) will stop rendering once there are more than two or three concurrent instances
/// of our calendar fragment. This bug is likely specific to our particular method of embedding Flutter.
///
/// For future reference, the behavior causing this bug was tracked to the following pull request:
///     https://github.com/flutter/flutter/pull/56521 (Flutter commit 98bc176)
///
/// The behavior appears to be related to the use of [RangeMaintainingScrollPhysics] as the parent of the default
/// default scroll physics, which in the case of Material apps is [ClampingScrollPhysics]. As such, the workaround
/// for this bug is to globally change that default to a parent-less instance of [ClampingScrollPhysics] by wrapping
/// the app contents in a [ScrollConfiguration] whose scroll behavior (this class) provides said default.
///
/// This class was copied verbatim from flutter/packages/flutter/lib/src/material/app.dart at Flutter version 1.20.2,
/// with the only modification being the addition of the overridden `getScrollPhysics` method.
class _ParentlessScrollBehavior extends ScrollBehavior {
  @override
  TargetPlatform getPlatform(BuildContext context) {
    return Theme.of(context).platform;
  }

  @override
  ScrollPhysics getScrollPhysics(BuildContext context) {
    // Return ClampingScrollPhysics without the RangeMaintainingScrollPhysics parent
    return const ClampingScrollPhysics();
  }

  @override
  Widget buildViewportChrome(BuildContext context, Widget child, AxisDirection axisDirection) {
    // When modifying this function, consider modifying the implementation in
    // the base class as well.
    switch (getPlatform(context)) {
      case TargetPlatform.iOS:
      case TargetPlatform.linux:
      case TargetPlatform.macOS:
      case TargetPlatform.windows:
        return child;
      case TargetPlatform.android:
      case TargetPlatform.fuchsia:
        return GlowingOverscrollIndicator(
          child: child,
          axisDirection: axisDirection,
          color: Theme.of(context).accentColor,
        );
    }
    return null;
  }
}
