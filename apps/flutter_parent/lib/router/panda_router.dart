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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/router_error_screen.dart';
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
import 'package:flutter_parent/utils/common_widgets/web_view/simple_web_view_screen.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/flutter_snackbar_veneer.dart';
import 'package:url_launcher/url_launcher.dart';

///
/// Debug note: Getting the deep link route from an external source (via the Android Activity's onCreate intent) can be
/// viewed by calling `WidgetsBinding.instance.window.defaultRouteName`, though we don't have to manually do this
/// as flutter sets this up for us.
class PandaRouter {
  static WebContentInteractor get _interactor => locator<WebContentInteractor>();

  static final Router router = Router();

  static bool _isInitialized = false;

  static String assignmentDetails(String courseId, String assignmentId) =>
      '/courses/$courseId/assignments/$assignmentId';

  static String conversations() => '/conversations';

  static String courseAnnouncementDetails(String courseId, String announcementId) =>
      '/courses/$courseId/discussion_topics/$announcementId';

  static String courseDetails(String courseId) => '/courses/$courseId';

  static String dashboard() => '/dashboard';

  static String discussionDetails(String courseId, String topicId) => courseAnnouncementDetails(courseId, topicId);

  static String domainSearch() => '/domainSearch';

  static String eventDetails(String courseId, String eventId) => 'courses/$courseId/calendar_events/$eventId';

  static String help() => '/help';

  static String institutionAnnouncementDetails(String accountNotificationId) =>
      '/account_notifications/$accountNotificationId';

  static String legal() => '/legal';

  static String login() => '/login';
  static final String _loginWeb = '/loginWeb';

  static String loginWeb(String domain, {String authenticationProvider = null}) =>
      '$_loginWeb?${_RouterKeys.domain}=$domain&${_RouterKeys.authenticationProvider}=$authenticationProvider';

  static String manageStudents() => '/manage_students';

  static String notParent() => '/not_parent';

  static String quizAssignmentDetails(String courseId, String quizId) => assignmentDetails(courseId, quizId);
  static final String _rootWithExternalUrl = '/external';
  static final String _routerError = '/error';

  static String _routerErrorRoute(String url) => '/error?${_RouterKeys.url}=${Uri.encodeQueryComponent(url)}';

  static String rootSplash() => '/';
  static final String _simpleWebView = '/internal';

  static String _simpleWebViewRoute(String url) => '/internal?${_RouterKeys.url}=${Uri.encodeQueryComponent(url)}';

  static String settings() => '/settings';

  static String termsOfUse() => '/terms_of_use';

  static void init() {
    if (!_isInitialized) {
      _isInitialized = true;
      router.define(rootSplash(), handler: _rootSplashHandler);
      router.define(conversations(), handler: _conversationsHandler);
      router.define(dashboard(), handler: _dashboardHandler);
      router.define(login(), handler: _loginHandler);
      router.define(_loginWeb, handler: _loginWebHandler);
      router.define(domainSearch(), handler: _domainSearchHandler);
      router.define(notParent(), handler: _notParentHandler);

      // INTERNAL
      router.define(courseDetails(':${_RouterKeys.courseId}'), handler: _courseDetailsHandler);
      router.define(assignmentDetails(':${_RouterKeys.courseId}', ':${_RouterKeys.assignmentId}'),
          handler: _assignmentDetailsHandler);
      router.define(quizAssignmentDetails(':${_RouterKeys.courseId}', ':${_RouterKeys.quizId}'),
          handler: _assignmentDetailsHandler);
      router.define(eventDetails(':${_RouterKeys.courseId}', ':${_RouterKeys.eventId}'), handler: _eventDetailsHandler);
      router.define(help(), handler: _helpHandler);
      router.define(legal(), handler: _legalHandler);
      router.define(termsOfUse(), handler: _termsOfUseHandler);
      router.define(settings(), handler: _settingsHandler);
      router.define(courseAnnouncementDetails(':${_RouterKeys.courseId}', ':${_RouterKeys.announcementId}'),
          handler: _courseAnnouncementDetailsHandler);
      router.define(institutionAnnouncementDetails(':${_RouterKeys.accountNotificationId}'),
          handler: _institutionAnnouncementDetailsHandler);
      router.define(_simpleWebView, handler: _simpleWebViewHandler);
      router.define(_routerError, handler: _routerErrorHandler);

      // EXTERNAL
      router.define(_rootWithExternalUrl, handler: _rootWithExternalUrlHandler);
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
    var authProvider = params[_RouterKeys.authenticationProvider]?.elementAt(0);
    return (authProvider == null || authProvider == 'null')
        ? WebLoginScreen(params[_RouterKeys.domain][0])
        : WebLoginScreen(
            params[_RouterKeys.domain][0],
            authenticationProvider: authProvider,
          );
  });

  static Handler _domainSearchHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return DomainSearchScreen();
  });

  static Handler _notParentHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return NotAParentScreen();
  });

  static Handler _courseDetailsHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return CourseDetailsScreen(params[_RouterKeys.courseId][0]);
  });

  static Handler _assignmentDetailsHandler =
      Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return AssignmentDetailsScreen(
      courseId: params[_RouterKeys.courseId][0],
      assignmentId: params[_RouterKeys.assignmentId][0],
    );
  });

  static Handler _eventDetailsHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return EventDetailsScreen.withId(
      eventId: params[_RouterKeys.eventId][0],
      courseId: params[_RouterKeys.courseId][0],
    );
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
      params[_RouterKeys.announcementId][0],
      AnnouncementType.COURSE,
      params[_RouterKeys.courseId][0],
      context,
    );
  });

  static Handler _institutionAnnouncementDetailsHandler =
      Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return AnnouncementDetailScreen(
      params[_RouterKeys.accountNotificationId][0],
      AnnouncementType.INSTITUTION,
      '',
      context,
    );
  });

  static Handler _routerErrorHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    final url = params[_RouterKeys.url][0];
    return RouterErrorScreen(url);
  });

  static Handler _simpleWebViewHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    final url = params[_RouterKeys.url][0];
    return SimpleWebViewScreen(url, url);
  });

  /// Used to handled external urls routed by the intent-filter -> MainActivity.kt
  static Handler _rootWithExternalUrlHandler =
      Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    final link = params[_RouterKeys.url][0];
    final urlRouteWrapper = getRouteWrapper(link);

    locator<Analytics>().logMessage('Attempting to route EXTERNAL url: $link');

    // We only care about valid app routes if they are already signed in
    if (urlRouteWrapper.appRouteMatch != null && ApiPrefs.isLoggedIn()) {
      if (urlRouteWrapper.validHost) {
        // If its a link we can handle natively and within our domain, route
        return (urlRouteWrapper.appRouteMatch.route.handler as Handler)
            .handlerFunc(context, urlRouteWrapper.appRouteMatch.parameters);
      } else {
        // Otherwise, we want to route to the error page if they are already logged in
        return _routerErrorHandler.handlerFunc(context, params);
      }
    }

    // We don't support the link or the user isn't signed in, default to the splash screen for now
    return _rootSplashHandler.handlerFunc(context, {});
  });

  /// Used to handle links clicked within web content
  static Future<void> routeInternally(BuildContext context, String link) async {
    final urlRouteWrapper = getRouteWrapper(link);

    locator<Analytics>().logMessage('Attempting to route INTERNAL url: $link');

    // Check to see if the route can be handled internally, isn't to root, and matches our current domain
    if (urlRouteWrapper.appRouteMatch != null) {
      if (urlRouteWrapper.validHost) {
        // Its a match, so we can route internally
        locator<QuickNav>().pushRoute(context, urlRouteWrapper.path);
      } else {
        // Show an error screen for non-matching domain
        locator<QuickNav>().pushRoute(context, _routerErrorRoute(link));
      }
    } else {
      // No native route found, let's launch the url if possible, or show an error toast
      if (await canLaunch(link)) {
        String authUrl = await _interactor.getAuthUrl(link);
        if (limitWebAccess) {
          // Special case for limit webview access flag (We don't want them to be able to navigate within the webview)
          locator<QuickNav>().pushRoute(context, _simpleWebViewRoute(link));
        } else {
          launch(authUrl);
        }
      } else {
        locator<FlutterSnackbarVeneer>().showSnackBar(context, L10n(context).routerLaunchErrorMessage);
      }
    }
  }

  static final bool limitWebAccess = false; // TODO - replace after MBL-13924 is implemented

  /// Simple helper method to determine if the router can handle a url
  /// returns a RouteWrapper
  /// _RouteWrapper.appRouteMatch will be null when there is no match or path is root
  static _UrlRouteWrapper getRouteWrapper(String link) {
    Uri uri = Uri.parse(link);

    // Determine if we can handle the url natively
    final path = '/${uri.pathSegments.join('/')}';
    final match = router.match(path);
    final currentDomain = ApiPrefs.getDomain();
    // Check to see if the route can be handled internally, isn't to root, and matches our current domain
    return _UrlRouteWrapper(
        path, currentDomain == null ? true : currentDomain.contains(uri.host), path == '/' ? null : match);
  }
}

/// Simple helper class to keep route keys/params consistently named
class _RouterKeys {
  static final accountNotificationId = 'accountNotificationId';
  static final announcementId = 'announcementId';
  static final assignmentId = 'assignmentId';
  static final authenticationProvider = 'authenticationProvider';
  static final courseId = 'courseId';
  static final domain = 'domain';
  static final eventId = 'eventId';
  static final quizId = 'quizId';
  static final topicId = 'topicId';
  static final url = 'url'; // NOTE: This has to match MainActivity.kt in the Android code
}

/// Simple helper class to manage the repeated data extracted from a link to be routed
class _UrlRouteWrapper {
  final String path;
  final bool validHost;
  final AppRouteMatch appRouteMatch;

  _UrlRouteWrapper(this.path, this.validHost, this.appRouteMatch);
}
