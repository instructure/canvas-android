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

import 'dart:core';

import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_screen.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_details_screen.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_screen.dart';
import 'package:flutter_parent/screens/events/event_details_screen.dart';
import 'package:flutter_parent/screens/help/help_screen.dart';
import 'package:flutter_parent/screens/help/legal_screen.dart';
import 'package:flutter_parent/screens/help/terms_of_use_screen.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/not_a_parent_screen.dart';
import 'package:flutter_parent/screens/settings/settings_screen.dart';
import 'package:flutter_parent/screens/splash/splash_screen.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';

class ParentRouter {
  static final Router router = Router();

  static bool _isInitialized = false;

  static final String _rootSplash = '/';
  static String rootSplash() => _rootSplash;

  static final String _rootWithUrl = '/external/:externalUrl';
  static String rootWithUrl(String url) => '/external/$url';

  static final String _dashboard = '/dashboard';
  static String dashboard() => _dashboard;

  static final String _login = '/login';
  static String login() => _login;

  static final String _loginWeb = '/loginWeb';
  static String loginWeb(String domain, String authenticationProvider) =>
      '/loginWeb?domain=$domain&authenticationProvider=$authenticationProvider';

  static final String _domainSearch = '/domainSearch';
  static String domainSearch() => _domainSearch;

  static final String _notParent = '/not_parent';
  static String notParent() => _notParent;

  static final String _conversations = '/conversations';
  static String conversations() => _conversations;

  static final String _manageStudents = '/manage_students';
  static String manageStudents() => _manageStudents;

  static final String _help = '/help';
  static String help() => _help;

  static final String _legal = '/legal';
  static String legal() => _legal;

  static final String _termsOfUse = '/terms_of_use';
  static String termsOfUse() => _termsOfUse;

  static final String _settings = '/settings';
  static String settings() => _settings;

  static final String _assignmentDetails = '/courses/:courseId/assignments/:assignmentId';
  static String assignmentDetails(String courseId, String assignmentId) =>
      '/courses/$courseId/assignments/$assignmentId';

  static final String _courseDetails = '/courses/:courseId';
  static String courseDetails(String courseId) => '/courses/$courseId';

  static final _eventDetails = '/courses/:courseId/calendar_events/:eventId';
  static String eventDetails(String courseId, String eventId) => 'courses/$courseId/calendar_events/$eventId';

  static final _courseAnnouncementDetails = '/courses/:courseId/discussion_topics/:announcementId';
  static String courseAnnouncementDetails(String courseId, String announcementId) =>
      '/courses/$courseId/discussion_topics/$announcementId';

  static final _institutionAnnouncementDetails = '/account_notifications/:accountNotificationId';
  static String institutionAnnouncementDetails(String accountNotificationId) =>
      '/account_notifications/$accountNotificationId';

  static void init() {
    if (!_isInitialized) {
      _isInitialized = true;
      router.define(_rootSplash, handler: _rootSplashHandler);
      router.define(_conversations, handler: _conversationsHandler);
      router.define(_dashboard, handler: _dashboardHandler);
      router.define(_login, handler: _loginHandler);
      router.define(_loginWeb, handler: _loginWebHandler);
      router.define(_domainSearch, handler: _domainSearchHandler);
      router.define(_notParent, handler: _notParentHandler);
      // EXTERNAL

      // INTERNAL
      router.define(_courseDetails, handler: _courseDetailsHandler);
      router.define(_assignmentDetails, handler: _assignmentDetailsHandler);
      router.define(_eventDetails, handler: _eventDetailsHandler);
      router.define(_help, handler: _helpHandler);
      router.define(_legal, handler: _legalHandler);
      router.define(_termsOfUse, handler: _termsOfUseHandler);
      router.define(_settings, handler: _settingsHandler);
      router.define(_courseAnnouncementDetails, handler: _courseAnnouncementDetailsHandler);
      router.define(_institutionAnnouncementDetails, handler: _institutionAnnouncementDetailsHandler);
    }
  }

  // Handlers
  static Handler _rootSplashHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return SplashScreen();
  });

  static Handler _conversationsHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return ConversationListScreen();
  });

  static Handler _dashboardHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return DashboardScreen();
  });

  static Handler _loginHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return LoginLandingScreen();
  });

  static Handler _loginWebHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return WebLoginScreen(params["domain"][0], authenticationProvider: params["authenticationProvider"][0]);
  });

  static Handler _domainSearchHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return DomainSearchScreen();
  });

  static Handler _notParentHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return NotAParentScreen();
  });

  // INTERNAL HANDLER
  static Handler _courseDetailsHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return CourseDetailsScreen(params["courseId"][0]);
  });

  static Handler _assignmentDetailsHandler =
      Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return AssignmentDetailsScreen(
      courseId: params["courseId"][0],
      assignmentId: params["assignmentId"][0],
    );
  });

  static Handler _eventDetailsHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return EventDetailsScreen.withId(eventId: params["eventId"][0], courseId: params["courseId"][0]);
  });

  static Handler _helpHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return HelpScreen();
  });

  static Handler _legalHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return LegalScreen();
  });

  static Handler _termsOfUseHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return TermsOfUseScreen();
  });

  static Handler _settingsHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return SettingsScreen();
  });

  static Handler _courseAnnouncementDetailsHandler =
      Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return AnnouncementDetailScreen(
        params['announcementId'][0], AnnouncementType.COURSE, params['courseId'][0], context);
  });

  static Handler _institutionAnnouncementDetailsHandler =
      Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return AnnouncementDetailScreen(params['accountNotificationId'][0], AnnouncementType.INSTITUTION, '', context);
  });

  // EXTERNAL HANDLER

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
