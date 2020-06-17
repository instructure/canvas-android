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

import 'dart:convert';
import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_student_embed/models/login.dart';
import 'package:flutter_student_embed/models/serializers.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_screen.dart';
import 'package:flutter_student_embed/screens/calendar/planner_fetcher.dart';
import 'package:flutter_student_embed/utils/common_widgets/colored_status_bar.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';

import 'design/student_colors.dart';

class NativeComm {
  static const channelName = 'com.instructure.student/flutterComm';

  static const methodReset = 'reset';
  static const methodRouteToCalendar = 'routeToCalendar';
  static const methodSetStatusBarColor = 'setStatusBarColor';
  static const methodUpdateCalendarDates = 'updateCalendarDates';
  static const methodUpdateLoginData = 'updateLoginData';
  static const methodUpdateShouldPop = 'updateShouldPop';
  static const methodUpdateThemeData = 'updateThemeData';

  static const channel = const MethodChannel(channelName);

  static final ShouldPopTracker routeTracker = ShouldPopTracker((shouldPop, statusBarColor) {
    channel.invokeMethod(methodUpdateShouldPop, shouldPop);
    setStatusBarColor(statusBarColor);
  });

  /// Called when theme values have been updated and the widget tree should be rebuilt
  static Function() onThemeUpdated;

  /// Called when the native app has requested to show a calendar screen
  static Function(String channelId) routeToCalendar;

  /// Called when the native app is clearing its navigation back stack (e.g. logout) and requests that Flutter do the same
  static Function() resetRoute;

  @visibleForTesting
  static Future<dynamic> Function(MethodCall) methodCallHandler = (methodCall) async {
    switch (methodCall.method) {
      case methodUpdateLoginData:
        _updateLogin(methodCall.arguments);
        break;
      case methodUpdateThemeData:
        _updateTheme(methodCall.arguments);
        break;
      case methodRouteToCalendar:
        if (routeToCalendar != null) routeToCalendar(methodCall.arguments as String);
        break;
      case methodReset:
        _performReset();
        break;
      case methodUpdateCalendarDates:
        _updateCalendarDates(methodCall.arguments);
        break;
      default:
        throw 'Channel method not implemented: ${methodCall.method}';
    }
    return null;
  };

  static void init() {
    channel.setMethodCallHandler(methodCallHandler);
  }

  static void _updateLogin(dynamic loginData) {
    if (loginData == null) {
      ApiPrefs.setLogin(null);
      return;
    }
    try {
      Login login = deserialize<Login>(json.decode(loginData));
      ApiPrefs.setLogin(login);
    } catch (e) {
      print('Error updating login!');
    }
  }

  static void _updateTheme(dynamic themeData) {
    try {
      StudentColors.primaryColor = Color(int.parse(themeData['primaryColor'], radix: 16));
      StudentColors.accentColor = Color(int.parse(themeData['accentColor'], radix: 16));
      StudentColors.buttonColor = Color(int.parse(themeData['buttonColor'], radix: 16));
      StudentColors.primaryTextColor = Color(int.parse(themeData['primaryTextColor'], radix: 16));

      Map<dynamic, dynamic> contextColors = themeData['contextColors'];
      StudentColors.contextColors = contextColors.map((contextCode, hexCode) {
        return MapEntry(contextCode as String, Color(int.parse(hexCode, radix: 16)));
      });

      if (onThemeUpdated != null) onThemeUpdated();
    } catch (e) {
      print('Error updating theme data!');
    }
  }

  static void _updateCalendarDates(dynamic rawDates) {
    List<DateTime> dates = (rawDates as List<dynamic>).map((it) => DateTime.parse(it as String).toLocal()).toList();
    PlannerFetcher.notifyDatesChanged(dates);
  }

  static void _performReset() {
    // Reset the current route to an empty screen
    if (resetRoute != null) resetRoute();

    // Reset ApiPrefs
    ApiPrefs.reset();

    // Reset theme
    StudentColors.reset();
    if (onThemeUpdated != null) onThemeUpdated();
  }

  static void setStatusBarColor(Color color) {
    channel.invokeMethod(methodSetStatusBarColor, color.value.toRadixString(16));
  }
}

/// Tracks whether the host app should pop it's current fragment on back press. Currently set up to only be
/// true if the current route is a CalendarScreen.
class ShouldPopTracker extends NavigatorObserver {
  final Function(bool shouldPop, Color statusBarColor) onUpdate;

  ShouldPopTracker(this.onUpdate);

  void update(Route route) {
    var color = StudentColors.primaryColor;
    if (route is QuickFadeRoute) {
      Widget child = route.child;
      if (child is ColoredStatusBar) {
        color = child.getStatusBarColor();
      }
    }
    onUpdate(route?.settings?.name == CalendarScreen.routeName, color);
  }

  @override
  void didPush(Route route, Route previousRoute) {
    update(route);
  }

  @override
  void didPop(Route route, Route previousRoute) {
    update(previousRoute);
  }

  @override
  void didRemove(Route route, Route previousRoute) {
    update(previousRoute);
  }

  @override
  void didReplace({Route newRoute, Route oldRoute}) {
    update(newRoute);
  }
}
