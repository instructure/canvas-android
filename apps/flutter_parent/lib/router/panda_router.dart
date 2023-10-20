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
import 'package:flutter_parent/screens/account_creation/account_creation_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_screen.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/aup/acceptable_use_policy_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_parent/screens/courses/details/course_details_screen.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_screen.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_screen.dart';
import 'package:flutter_parent/screens/events/event_details_screen.dart';
import 'package:flutter_parent/screens/help/help_screen.dart';
import 'package:flutter_parent/screens/settings/legal_screen.dart';
import 'package:flutter_parent/screens/help/terms_of_use_screen.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/not_a_parent_screen.dart';
import 'package:flutter_parent/screens/pairing/qr_pairing_screen.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen.dart';
import 'package:flutter_parent/screens/settings/settings_screen.dart';
import 'package:flutter_parent/screens/splash/splash_screen.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/simple_web_view_screen.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_parent/utils/veneers/flutter_snackbar_veneer.dart';

///
/// Debug note: Getting the deep link route from an external source (via the Android Activity's onCreate intent) can be
/// viewed by calling `WidgetsBinding.instance.window.defaultRouteName`, though we don't have to manually do this
/// as flutter sets this up for us.
class PandaRouter {
  static WebContentInteractor get _interactor => locator<WebContentInteractor>();

  static final FluroRouter router = FluroRouter();

  static bool _isInitialized = false;

  static final _accountCreation = '/account_creation';

  static String accountCreation(String code, String domain, String accountId) {
    return '$_accountCreation?${_RouterKeys.pairingCode}=${Uri.encodeQueryComponent(code)}&${_RouterKeys.domain}=${Uri.encodeQueryComponent(domain)}&${_RouterKeys.accountId}=${Uri.encodeQueryComponent(accountId)}';
  }

  static final String alerts = '/alerts';

  static String assignmentDetails(String courseId, String assignmentId) =>
      '/courses/$courseId/assignments/$assignmentId';

  static final String calendar = '/calendar';

  static String conversations() => '/conversations';

  static String courseAnnouncementDetails(String courseId, String announcementId) =>
      '/courses/$courseId/discussion_topics/$announcementId';

  static String courseDetails(String courseId) => '/courses/$courseId';

  static String courses() => '/courses';

  static String dashboard() => '/dashboard';

  static String discussionDetails(String courseId, String assignmentId) => assignmentDetails(courseId, assignmentId);

  static String _domainSearch = '/domainSearch';

  static String domainSearch({LoginFlow loginFlow = LoginFlow.normal}) =>
      '$_domainSearch?${_RouterKeys.loginFlow}=${loginFlow.toString()}';

  static String eventDetails(String courseId, String eventId) => 'courses/$courseId/calendar_events/$eventId';

  static String frontPage(String courseId) => '/courses/$courseId/pages/first-page';

  static String frontPageWiki(String courseId) => '/courses/$courseId/wiki';

  static String gradesPage(String courseId) => '/courses/$courseId/grades';

  static String help() => '/help';

  static String institutionAnnouncementDetails(String accountNotificationId) =>
      '/account_notifications/$accountNotificationId';

  static String legal() => '/legal';

  static String login() => '/login';

  static final String _loginWeb = '/loginWeb';

  static String loginWeb(
    String domain, {
    String accountName = '',
    String? authenticationProvider = null,
    LoginFlow loginFlow = LoginFlow.normal,
  }) =>
      '$_loginWeb?${_RouterKeys.domain}=${Uri.encodeQueryComponent(domain)}&${_RouterKeys.accountName}=${Uri.encodeQueryComponent(accountName)}&${_RouterKeys.authenticationProvider}=$authenticationProvider&${_RouterKeys.loginFlow}=${loginFlow.toString()}';

  static String notParent() => '/not_parent';

  static String quizAssignmentDetails(String courseId, String quizId) => assignmentDetails(courseId, quizId);

  static String _qrLogin = '/qr_login';

  static String qrLogin(String qrLoginUrl) =>
      '/qr_login?${_RouterKeys.qrLoginUrl}=${Uri.encodeQueryComponent(qrLoginUrl)}';

  static String qrTutorial() => '/qr_tutorial';

  static String _qrPairing = '/qr_pairing';

  static String qrPairing({String? pairingUri, bool isCreatingAccount = false}) {
    if (isCreatingAccount) return '$_qrPairing?${_RouterKeys.isCreatingAccount}=${isCreatingAccount}';
    if (pairingUri == null) return _qrPairing;
    return '$_qrPairing?${_RouterKeys.qrPairingInfo}=${Uri.encodeQueryComponent(pairingUri)}';
  }

  static final String _rootWithExternalUrl = 'external';

  static final String _routerError = '/error';

  static String _routerErrorRoute(String url) => '/error?${_RouterKeys.url}=${Uri.encodeQueryComponent(url)}';

  static String rootSplash() => '/';

  static String aup() => '/aup';

  static final String _simpleWebView = '/internal';

  static String simpleWebViewRoute(String url, String infoText) =>
      '/internal?${_RouterKeys.url}=${Uri.encodeQueryComponent(url)}&${_RouterKeys.infoText}=${Uri.encodeQueryComponent(infoText)}';

  static String settings() => '/settings';

  static String syllabus(String courseId) => '/courses/$courseId/assignments/syllabus';

  static String termsOfUse({String? accountId, String? domain}) {
    if (accountId != null && domain != null) {
      return '/terms_of_use?${_RouterKeys.accountId}=${Uri.encodeQueryComponent(accountId)}&${_RouterKeys.url}=${Uri.encodeQueryComponent(domain)}';
    } else {
      return '/terms_of_use';
    }
  }

  static void init() {
    if (!_isInitialized) {
      _isInitialized = true;
      router.define(conversations(), handler: _conversationsHandler);
      router.define(dashboard(), handler: _dashboardHandler);
      router.define(_domainSearch, handler: _domainSearchHandler);
      router.define(_loginWeb, handler: _loginWebHandler);
      router.define(login(), handler: _loginHandler);
      router.define(notParent(), handler: _notParentHandler);
      router.define(rootSplash(), handler: _rootSplashHandler);

      // INTERNAL
      router.define(_accountCreation, handler: _accountCreationHandler);
      router.define(alerts, handler: _alertHandler);
      // RIP Alphabetical Order, syllabus needs to appear before assignment details, otherwise they conflict
      router.define(syllabus(':${_RouterKeys.courseId}'), handler: _syllabusHandler);
      router.define(assignmentDetails(':${_RouterKeys.courseId}', ':${_RouterKeys.assignmentId}'),
          handler: _assignmentDetailsHandler);
      router.define(calendar, handler: _calendarHandler);
      router.define(courseAnnouncementDetails(':${_RouterKeys.courseId}', ':${_RouterKeys.announcementId}'),
          handler: _courseAnnouncementDetailsHandler);
      router.define(courseDetails(':${_RouterKeys.courseId}'), handler: _courseDetailsHandler);
      router.define(courses(), handler: _coursesHandler);
      router.define(eventDetails(':${_RouterKeys.courseId}', ':${_RouterKeys.eventId}'), handler: _eventDetailsHandler);
      router.define(frontPage(':${_RouterKeys.courseId}'), handler: _frontPageHandler);
      router.define(frontPageWiki(':${_RouterKeys.courseId}'), handler: _frontPageHandler);
      router.define(gradesPage(':${_RouterKeys.courseId}'), handler: _gradesPageHandler);
      router.define(help(), handler: _helpHandler);
      router.define(institutionAnnouncementDetails(':${_RouterKeys.accountNotificationId}'),
          handler: _institutionAnnouncementDetailsHandler);
      router.define(legal(), handler: _legalHandler);
      router.define(quizAssignmentDetails(':${_RouterKeys.courseId}', ':${_RouterKeys.quizId}'),
          handler: _assignmentDetailsHandler);
      router.define(_qrLogin, handler: _qrLoginHandler);
      router.define(qrTutorial(), handler: _qrTutorialHandler);
      router.define(_qrPairing, handler: _qrPairingHandler);
      router.define(_routerError, handler: _routerErrorHandler);
      router.define(settings(), handler: _settingsHandler);
      router.define(_simpleWebView, handler: _simpleWebViewHandler);
      router.define(termsOfUse(), handler: _termsOfUseHandler);
      router.define(aup(), handler: _aupHandler);

      // EXTERNAL
      router.define(_rootWithExternalUrl, handler: _rootWithExternalUrlHandler);
    }
  }

  // Handlers
  static Handler _accountCreationHandler =
      Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    var pairingInfo = QRPairingScanResult.success(
        params[_RouterKeys.pairingCode]![0], params[_RouterKeys.domain]![0], params[_RouterKeys.accountId]![0]);
    return AccountCreationScreen(pairingInfo as QRPairingInfo);
  });

  static Handler _alertHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return DashboardScreen(
      startingPage: DashboardContentScreens.Alerts,
    );
  });

  static Handler _assignmentDetailsHandler =
      Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return AssignmentDetailsScreen(
      courseId: params[_RouterKeys.courseId]![0],
      assignmentId: params[_RouterKeys.assignmentId]![0],
    );
  });

  static Handler _calendarHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    var calendarParams = {
      CalendarScreen.startDateKey:
          DateTime.tryParse(params[_RouterKeys.calendarStart]?.elementAt(0) ?? '') ?? DateTime.now(),
      CalendarScreen.startViewKey:
          params[_RouterKeys.calendarView]?.elementAt(0) == 'month' ? CalendarView.Month : CalendarView.Week,
    };

    var widget = DashboardScreen(
      startingPage: DashboardContentScreens.Calendar,
      deepLinkParams: calendarParams,
    );

    return widget;
  });

  static Handler _conversationsHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return ConversationListScreen();
  });

  static Handler _courseAnnouncementDetailsHandler =
      Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return AnnouncementDetailScreen(
        params[_RouterKeys.announcementId]![0], AnnouncementType.COURSE, params[_RouterKeys.courseId]![0], context);
  });

  static Handler _courseDetailsHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return CourseDetailsScreen(params[_RouterKeys.courseId]![0]);
  });

  static Handler _coursesHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return DashboardScreen(
      startingPage: DashboardContentScreens.Courses,
    );
  });

  static Handler _dashboardHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return DashboardScreen();
  });

  static Handler _domainSearchHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    // Login flow
    String loginFlowString = params[_RouterKeys.loginFlow]?.elementAt(0) ?? LoginFlow.normal.toString();
    LoginFlow loginFlow = LoginFlow.values.firstWhere((e) => e.toString() == loginFlowString);

    return DomainSearchScreen(loginFlow: loginFlow);
  });

  static Handler _eventDetailsHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return EventDetailsScreen.withId(
      eventId: params[_RouterKeys.eventId]![0],
      courseId: params[_RouterKeys.courseId]![0],
    );
  });

  static Handler _frontPageHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return CourseRoutingShellScreen(params[_RouterKeys.courseId]![0], CourseShellType.frontPage);
  });

  static Handler _gradesPageHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return CourseDetailsScreen(params[_RouterKeys.courseId]![0]);
  });

  static Handler _helpHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return HelpScreen();
  });

  static Handler _institutionAnnouncementDetailsHandler =
      Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return AnnouncementDetailScreen(
        params[_RouterKeys.accountNotificationId]![0], AnnouncementType.INSTITUTION, '', context);
  });

  static Handler _legalHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return LegalScreen();
  });

  static Handler _loginHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return LoginLandingScreen();
  });

  static Handler _loginWebHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    // Auth provider
    String? authProvider = params[_RouterKeys.authenticationProvider]?.elementAt(0);
    if ('null' == authProvider) authProvider = null;

    // Login flow
    String? loginFlowString = params[_RouterKeys.loginFlow]?.elementAt(0);
    if (loginFlowString == null || loginFlowString == 'null') loginFlowString = LoginFlow.normal.toString();
    LoginFlow loginFlow = LoginFlow.values.firstWhere((e) => e.toString() == loginFlowString);

    return WebLoginScreen(
      params[_RouterKeys.domain]![0],
      accountName: params[_RouterKeys.accountName]![0],
      authenticationProvider: authProvider,
      loginFlow: loginFlow,
    );
  });

  static Handler _notParentHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return NotAParentScreen();
  });

  static Handler _qrLoginHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    String? qrLoginUrl = params[_RouterKeys.qrLoginUrl]?.elementAt(0);
    return SplashScreen(qrLoginUrl: qrLoginUrl);
  });

  static Handler _qrTutorialHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return QRLoginTutorialScreen();
  });

  static Handler _qrPairingHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    var pairingInfo = QRUtils.parsePairingInfo(params[_RouterKeys.qrPairingInfo]?.elementAt(0));
    var isCreatingAccount = params[_RouterKeys.isCreatingAccount]?.elementAt(0) == 'true';
    if (pairingInfo is QRPairingInfo) {
      return QRPairingScreen(pairingInfo: pairingInfo);
    } else if (isCreatingAccount) {
      return QRPairingScreen(isCreatingAccount: isCreatingAccount);
    } else {
      return QRPairingScreen();
    }
  });

  static Handler _rootSplashHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return SplashScreen();
  });

  static Handler _routerErrorHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    final url = params[_RouterKeys.url]![0];
    return RouterErrorScreen(url);
  });

  static Handler _settingsHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return SettingsScreen();
  });

  static Handler _simpleWebViewHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    final url = params[_RouterKeys.url]![0];
    final infoText = params[_RouterKeys.infoText]?.elementAt(0);
    return SimpleWebViewScreen(url, url, infoText: infoText == null || infoText == 'null' ? null : infoText);
  });

  static Handler _syllabusHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return CourseRoutingShellScreen(params[_RouterKeys.courseId]![0], CourseShellType.syllabus);
  });

  static Handler _termsOfUseHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    final domain = params[_RouterKeys.url]?.elementAt(0);
    final accountId = params[_RouterKeys.accountId]?.elementAt(0);

    if ((domain != null && domain.isNotEmpty) && (accountId != null && accountId.isNotEmpty)) {
      return TermsOfUseScreen(accountId: accountId, domain: domain);
    } else {
      return TermsOfUseScreen();
    }
  });

  static Handler _aupHandler = Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    return AcceptableUsePolicyScreen();
  });

  /// Used to handled external urls routed by the intent-filter -> MainActivity.kt
  static Handler _rootWithExternalUrlHandler =
      Handler(handlerFunc: (BuildContext? context, Map<String, List<String>> params) {
    var link = params[_RouterKeys.url]![0];

    // QR Login: we need to modify the url slightly
    var qrUri = QRUtils.verifySSOLogin(link);
    if (qrUri != null) {
      link = qrLogin(link);
    }

    // QR Pairing
    var pairingParseResult = QRUtils.parsePairingInfo(link);
    QRPairingInfo? pairingInfo = pairingParseResult is QRPairingInfo ? pairingParseResult : null;
    if (pairingInfo != null) {
      link = qrPairing(pairingUri: link);
    }

    final urlRouteWrapper = getRouteWrapper(link);

    locator<Analytics>().logMessage('Attempting to route EXTERNAL url: $link');

    // We only care about valid app routes if they are already signed in or performing a qr login
    if (urlRouteWrapper.appRouteMatch != null && (ApiPrefs.isLoggedIn() || qrUri != null)) {
      if (urlRouteWrapper.validHost) {
        // Before deep linking, we need to make sure a current student is set
        if (ApiPrefs.getCurrentStudent() != null || qrUri != null) {
          // If its a link we can handle natively and within our domain, route
          return (urlRouteWrapper.appRouteMatch?.route.handler as Handler)
              .handlerFunc(context, urlRouteWrapper.appRouteMatch!.parameters);
        } else {
          // This might be a migrated user or an error case, let's route them to the dashboard
          return _dashboardHandler.handlerFunc(context, {});
        }
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
      final url = await _interactor.getAuthUrl(link);
      if (limitWebAccess) {
        // Special case for limit webview access flag (We don't want them to be able to navigate within the webview)
        locator<QuickNav>().pushRoute(context, simpleWebViewRoute(url, L10n(context).webAccessLimitedMessage));
      } else if (await locator<UrlLauncher>().canLaunch(link) ?? false) {
        // No native route found, let's launch the url if possible, or show an error toast
        locator<UrlLauncher>().launch(url);
      } else {
        locator<FlutterSnackbarVeneer>().showSnackBar(context, L10n(context).routerLaunchErrorMessage);
      }
    }
  }

  static bool get limitWebAccess => ApiPrefs.getUser()?.permissions?.limitParentAppWebAccess ?? false;

  /// Simple helper method to determine if the router can handle a url
  /// returns a RouteWrapper
  /// _RouteWrapper.appRouteMatch will be null when there is no match or path is root
  static _UrlRouteWrapper getRouteWrapper(String link) {
    Uri uri;

    try {
      uri = Uri.parse(link);
    } catch (e) {
      return _UrlRouteWrapper(link, false, null);
    }

    // Add any fragment parameters, e.g. '/...#view_name=month&view_start=12-12-2020' as query params to the uri
    var frags = Uri.parse(link).fragment;

    uri = uri.replace(
      queryParameters: Uri.splitQueryString(frags)..addAll(uri.queryParameters),
      fragment: '', // Remove the fragment
    );

    // Determine if we can handle the url natively
    final path = '/${uri.pathSegments.join('/')}${uri.query.isNotEmpty ? '?${uri.query}' : ''}';
    final match = router.match(path);
    final currentDomain = ApiPrefs.getDomain();

    // Check to see if the route can be handled internally, isn't to root, and matches our current domain
    return _UrlRouteWrapper(
        path, currentDomain == null ? true : currentDomain.contains(uri.host), path == '/' ? null : match);
  }
}

/// Simple helper class to keep route keys/params consistently named
class _RouterKeys {
  static final accountId = 'accountId';
  static final accountNotificationId = 'accountNotificationId';
  static final announcementId = 'announcementId';
  static final assignmentId = 'assignmentId';
  static final authenticationProvider = 'authenticationProvider';
  static final pairingCode = 'pairing_code';
  static final calendarStart = 'view_start';
  static final calendarView = 'view_name';
  static final courseId = 'courseId';
  static final domain = 'domain';
  static final accountName = 'accountName';
  static final eventId = 'eventId';
  static final infoText = 'infoText';
  static final isCreatingAccount = 'isCreatingAccount';
  static final loginFlow = 'loginFlow';
  static final qrLoginUrl = 'qrLoginUrl';
  static final qrPairingInfo = 'qrPairingInfo';
  static final quizId = 'quizId';
  static final topicId = 'topicId';
  static final url = 'url'; // NOTE: This has to match MainActivity.kt in the Android code

}

/// Simple helper class to manage the repeated data extracted from a link to be routed
class _UrlRouteWrapper {
  final String path;
  final bool validHost;
  final AppRouteMatch? appRouteMatch;

  _UrlRouteWrapper(this.path, this.validHost, this.appRouteMatch);
}
