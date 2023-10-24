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
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/service_locator.dart';

/// A simple observer that reports to analytics and crashlytics each time a screen changes
/// Only handles PageRoute transitions. Dialogs won't be covered as there is typically no useful information in it's route for logging.
class AnalyticsObserver extends NavigatorObserver {
  void _sendScreenView(PageRoute<dynamic> route) {
    if (route.settings.name == null) return; // No name means we can't match it, should be logged by QuickNav.push
    final match = PandaRouter.router.match(route.settings.name!);
    final String? screenName = match?.route.route;
    if (screenName != null) {
      final message =
          'Pushing widget: $screenName ${match!.parameters.isNotEmpty ? 'with params: ${match.parameters}' : ''}';
      locator<Analytics>().logMessage(message); // Log message for crashlytics debugging
      locator<Analytics>().setCurrentScreen(screenName); // Log current screen for analytics
    }
  }

  @override
  void didPush(Route<dynamic> route, Route<dynamic>? previousRoute) {
    super.didPush(route, previousRoute);
    if (route is PageRoute) {
      _sendScreenView(route);
    }
  }

  @override
  void didReplace({Route<dynamic>? newRoute, Route<dynamic>? oldRoute}) {
    super.didReplace(newRoute: newRoute, oldRoute: oldRoute);
    if (newRoute is PageRoute) {
      _sendScreenView(newRoute);
    }
  }

  @override
  void didPop(Route<dynamic> route, Route<dynamic>? previousRoute) {
    super.didPop(route, previousRoute);
    if (previousRoute is PageRoute && route is PageRoute) {
      _sendScreenView(previousRoute);
    }
  }
}
