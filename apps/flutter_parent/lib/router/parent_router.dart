/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/not_a_parent_screen.dart';
import 'package:flutter_parent/screens/splash/splash_screen.dart';

class ParentRouter {
  static final Router router = Router();

  static bool _isInitialized = false;

  static final String _root = '/';
  static String root() {
    return _root;
  }

  static final String _rootWithUrl = '/external/:externalUrl';
  static String rootWithUrl(String url) {
    return '/external/$url';
  }

  static final String _dashboard = '/dashboard';
  static String dashboard() {
    return _dashboard;
  }

  static final String _login = '/login';
  static String login() {
    return _login;
  }

  static final String _notParent = '/not_parent';
  static String notParent() {
    return _notParent;
  }

  static final String _conversations = '/conversations';
  static String conversations() {
    return _conversations;
  }

  static final String _assignmentDetails = '/courses/:courseId/assignments/:assignmentId';
  static String assignmentDetails(String courseId, String assignmentId) {
    return '/courses/$courseId/assignments/$assignmentId';
  }

  static void init() {
    if (!_isInitialized) {
      _isInitialized = true;
      router.notFoundHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
        print('default sup dog');
        return Container(
          child: Text('WAHTS UP IM THE DEFAULT'),
        );
      });
      router.define(_root, handler: _rootHandler);
      router.define(_conversations, handler: _conversationsHandler);
      router.define(_dashboard, handler: _dashboardHandler);
      router.define(_login, handler: _loginHandler);
      router.define(_notParent, handler: _notParentHandler);
    }
  }

  // Handlers
  static Handler _rootHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    print('splash sup dog');
    return SplashScreen();
  });

  static Handler _conversationsHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    print('conv sup dog');
    return ConversationListScreen();
  });

  static Handler _dashboardHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    print('dashboard sup dog');
    return DashboardScreen();
  });

  static Handler _loginHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    print('login sup dog');
    return LoginLandingScreen();
  });

  static Handler _notParentHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    print('not a parent sup dog');
    return NotAParentScreen();
  });

  /**
   *  Handles all links, preps them to be appropriate for the router
   *
   *  Should handle external / internal / download
   */
  static String prepLink(BuildContext context, String link) {
    // Validate the url
    Uri uri = Uri.parse(link);
    // - validating the host, check if it matches currently logged in user

    // Determine if we can handle the url natively
    // -formatting the url, router.matchRoute returns RouteMatchType.noMatch or not
    RouteMatch match = router.matchRoute(context, uri.pathSegments.join('/'));
    if (match.matchType == RouteMatchType.noMatch) {
      // route to SimpleWebScreen

    } else {
      // If supported, route

    }
  }
}
