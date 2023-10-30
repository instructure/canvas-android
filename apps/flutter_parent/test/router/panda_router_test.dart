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

import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/router/router_error_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_screen.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
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
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_parent/utils/veneers/flutter_snackbar_veneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../utils/accessibility_utils.dart';
import '../utils/canvas_model_utils.dart';
import '../utils/platform_config.dart';
import '../utils/test_app.dart';
import '../utils/test_helpers/mock_helpers.dart';
import '../utils/test_helpers/mock_helpers.mocks.dart';

final _analytics = MockAnalytics();

void main() {
  final String _domain = 'https://test.instructure.com';
  final user = CanvasModelTestUtils.mockUser();
  final login = Login((b) => b
    ..domain = _domain
    ..accessToken = 'token'
    ..user = user.toBuilder());

  final _mockNav = MockQuickNav();
  final _mockWebContentInteractor = MockWebContentInteractor();
  final _mockSnackbar = MockFlutterSnackbarVeneer();
  final _mockLauncher = MockUrlLauncher();

  setUpAll(() async {
    PandaRouter.init();
    setupTestLocator((locator) {
      locator.registerLazySingleton<Analytics>(() => _analytics);
      locator.registerLazySingleton<WebContentInteractor>(() => _mockWebContentInteractor);
      locator.registerLazySingleton<QuickNav>(() => _mockNav);
      locator.registerLazySingleton<FlutterSnackbarVeneer>(() => _mockSnackbar);
      locator.registerLazySingleton<UrlLauncher>(() => _mockLauncher);
    });
  });

  setUp(() async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));
    ApiPrefs.setCurrentStudent(user);
    reset(_analytics);
    reset(_mockLauncher);
    reset(_mockNav);
    reset(_mockSnackbar);
  });

  group('route generators', () {
    test('loginWeb returns null by default', () {});
  });

  group('handlers', () {
    test('rootSplash returns splash screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.rootSplash());
      expect(widget, isA<SplashScreen>());
    });

    test('conversations returns conversations list screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.conversations());
      expect(widget, isA<ConversationListScreen>());
    });

    test('dashboard returns dashboard screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.dashboard());
      expect(widget, isA<DashboardScreen>());
    });

    test('login returns login landing screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.login());
      expect(widget, isA<LoginLandingScreen>());
    });

    test('domainSearch returns DomainSearchScreen', () {
      final widget = _getWidgetFromRoute(PandaRouter.domainSearch());
      expect(widget, isA<DomainSearchScreen>());
    });

    test('domainSearch returns DomainSearchScreen with LoginFlow', () {
      final flow = LoginFlow.canvas;
      final widget = _getWidgetFromRoute(PandaRouter.domainSearch(loginFlow: flow));

      expect(widget, isA<DomainSearchScreen>());
      expect((widget as DomainSearchScreen).loginFlow, flow);
    });

    test('loginWeb returns web login screen', () {
      final domain = 'domain';
      final widget = _getWidgetFromRoute(
        PandaRouter.loginWeb(domain),
      ) as WebLoginScreen;

      expect(widget, isA<WebLoginScreen>());
      expect(widget.domain, domain);
    });

    test('loginWeb returns web login screen with non-uri safe characters ', () {
      final domain = 'domain%';
      final widget = _getWidgetFromRoute(
        PandaRouter.loginWeb(domain),
      ) as WebLoginScreen;

      expect(widget, isA<WebLoginScreen>());
      expect(widget.domain, domain);
    });

    test('loginWeb returns web login screen with LoginFlow', () {
      final domain = 'domain';
      final flow = LoginFlow.siteAdmin;
      final widget = _getWidgetFromRoute(
        PandaRouter.loginWeb(domain, loginFlow: flow),
      ) as WebLoginScreen;

      expect(widget, isA<WebLoginScreen>());
      expect(widget.domain, domain);
      expect(widget.loginFlow, flow);
    });

    test('loginWeb returns web login screen with auth provider', () {
      final domain = 'domain';
      final authProvider = 'auth';
      final widget = _getWidgetFromRoute(
        PandaRouter.loginWeb(domain, authenticationProvider: authProvider),
      ) as WebLoginScreen;

      expect(widget, isA<WebLoginScreen>());
      expect(widget.domain, domain);
      expect(widget.authenticationProvider, authProvider);
    });

    test('notParent returns not a parent screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.notParent());
      expect(widget, isA<NotAParentScreen>());
    });

    test('courseDetails returns course details screen', () async {
      await setupPlatformChannels();
      final widget = _getWidgetFromRoute(
        PandaRouter.courseDetails('123'),
      );
      expect(widget, isA<CourseDetailsScreen>());
    });

    test('assignmentDetails returns assignment details screen', () {
      final courseId = '123';
      final assignmentId = '321';
      final widget = _getWidgetFromRoute(
        PandaRouter.assignmentDetails(courseId, assignmentId),
      ) as AssignmentDetailsScreen;

      expect(widget, isA<AssignmentDetailsScreen>());
      expect(widget.courseId, courseId);
      expect(widget.assignmentId, assignmentId);
    });

    test('eventDetails returns event details screen', () {
      final courseId = '123';
      final eventId = '321';
      final widget = _getWidgetFromRoute(
        PandaRouter.eventDetails(courseId, eventId),
      ) as EventDetailsScreen;

      expect(widget, isA<EventDetailsScreen>());
      expect(widget.courseId, courseId);
      expect(widget.eventId, eventId);
    });

    test('help returns help screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.help());
      expect(widget, isA<HelpScreen>());
    });

    test('legal returns legal screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.legal());
      expect(widget, isA<LegalScreen>());
    });

    test('termsOfUse returns terms of use screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.termsOfUse());
      expect(widget, isA<TermsOfUseScreen>());
    });

    test('settings returns settings screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.settings());
      expect(widget, isA<SettingsScreen>());
    });

    test('courseAnnouncementDetails returns announcement details screen', () {
      final courseId = '123';
      final announcementId = '321';
      final widget = _getWidgetFromRoute(
        PandaRouter.courseAnnouncementDetails(courseId, announcementId),
      ) as AnnouncementDetailScreen;

      expect(widget, isA<AnnouncementDetailScreen>());
      expect(widget.courseId, courseId);
      expect(widget.announcementId, announcementId);
      expect(widget.announcementType, AnnouncementType.COURSE);
    });

    test('institutionAnnouncementDetails returns announcement details screen', () {
      final notificationId = '321';
      final widget = _getWidgetFromRoute(
        PandaRouter.institutionAnnouncementDetails(notificationId),
      ) as AnnouncementDetailScreen;

      expect(widget, isA<AnnouncementDetailScreen>());
      expect(widget.courseId, '');
      expect(widget.announcementId, notificationId);
      expect(widget.announcementType, AnnouncementType.INSTITUTION);
    });

    test('discussionDetails returns announcement details screen', () {
      final courseId = '123';
      final assignmentId = '321';
      final widget = _getWidgetFromRoute(
        PandaRouter.discussionDetails(courseId, assignmentId),
      ) as AssignmentDetailsScreen;

      expect(widget, isA<AssignmentDetailsScreen>());
      expect(widget.courseId, courseId);
      expect(widget.assignmentId, assignmentId);
    });

    test('RouterErrorScreen returns RouterErrorScreen', () {
      final url = 'https://test.instructure.com/blah-blah-blah';
      final widget = _getWidgetFromRoute(
        _routerErrorRoute(url),
      ) as RouterErrorScreen;

      expect(widget, isA<RouterErrorScreen>());
    });

    test('RouterErrorScreen returns RouterErrorScreen', () {
      final url = 'https://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _simpleWebViewRoute(url),
      ) as SimpleWebViewScreen;

      expect(widget, isA<SimpleWebViewScreen>());
    });

    test('calendar returns Dashboard screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.calendar, extraParams: {
        'view_start': ['2018-12-15'],
        'view_name': ['month']
      });

      expect(widget, isA<DashboardScreen>());

      DashboardScreen dashboard = widget as DashboardScreen;
      expect(dashboard.startingPage, DashboardContentScreens.Calendar);
      expect(dashboard.deepLinkParams?[CalendarScreen.startDateKey], DateTime(2018, 12, 15));
      expect(dashboard.deepLinkParams?[CalendarScreen.startViewKey], CalendarView.Month);
    });

    test('courses returns Dashboard screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.courses());
      expect((widget as DashboardScreen).startingPage, DashboardContentScreens.Courses);
      expect(widget, isA<DashboardScreen>());
    });

    test('alerts returns Dashboard screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.alerts);
      expect((widget as DashboardScreen).startingPage, DashboardContentScreens.Alerts);
      expect(widget, isA<DashboardScreen>());
    });

    test('qrTutorial returns QRTutorial screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.qrTutorial());
      expect(widget, isA<QRLoginTutorialScreen>());
    });

    test('qrLogin returns splash screen', () {
      final barcodeResultUrl = 'https://${QRUtils.QR_HOST}/canvas/login?${QRUtils.QR_AUTH_CODE}=1234'
          '&${QRUtils.QR_DOMAIN}=mobiledev.instructure.com';
      final widget = _getWidgetFromRoute(PandaRouter.qrLogin(barcodeResultUrl));
      expect(widget, isA<SplashScreen>());
    });

    test('qrPairing returns qrPairingScreen screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.qrPairing());
      expect(widget, isA<QRPairingScreen>());
    });

    test('qrPairing returns qrPairingScreen screen', () {
      final widget = _getWidgetFromRoute(PandaRouter.qrPairing(isCreatingAccount: true));
      expect(widget, isA<QRPairingScreen>());
      expect((widget as QRPairingScreen).isCreatingAccount, true);
    });

    test('qrPairing returns qrPairingScreen screen', () {
      final uri = 'canvas-parent://mobiledev.instructure.com/pair?code=123abc&account_id=1234';
      final pairingInfo = QRUtils.parsePairingInfo(uri) as QRPairingInfo;
      final widget = _getWidgetFromRoute(PandaRouter.qrPairing(pairingUri: uri));
      expect(widget, isA<QRPairingScreen>());
      expect((widget as QRPairingScreen).pairingInfo?.code, pairingInfo.code);
      expect((widget).pairingInfo?.domain, pairingInfo.domain);
      expect((widget).pairingInfo?.accountId, pairingInfo.accountId);
    });

    test('syllabus returns CourseRoutingShellScreen', () {
      final courseId = '123';
      final widget = _getWidgetFromRoute(PandaRouter.syllabus(courseId));
      expect(widget, isA<CourseRoutingShellScreen>());
      expect((widget as CourseRoutingShellScreen).courseId, courseId);
      expect((widget as CourseRoutingShellScreen).type, CourseShellType.syllabus);
    });

    test('frontPage returns CourseRoutingShellScreen', () {
      final courseId = '123';
      final widget = _getWidgetFromRoute(PandaRouter.frontPage(courseId));
      expect(widget, isA<CourseRoutingShellScreen>());
      expect((widget as CourseRoutingShellScreen).courseId, courseId);
      expect((widget).type, CourseShellType.frontPage);
    });

    test('frontPageWiki returns CourseRoutingShellScreen', () {
      final courseId = '123';
      final widget = _getWidgetFromRoute(PandaRouter.frontPageWiki(courseId));
      expect(widget, isA<CourseRoutingShellScreen>());
      expect((widget as CourseRoutingShellScreen).courseId, courseId);
      expect((widget).type, CourseShellType.frontPage);
    });
  });

  group('external url handler', () {
    test('returns splash when the url does not match any routes', () {
      final url = 'https://test.instructure.com/not-supported';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      );

      expect(widget, isA<SplashScreen>());
    });

    test('returns dashboard for https scheme', () {
      final url = 'https://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for http scheme', () {
      final url = 'http://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for canvas-parent scheme', () {
      final url = 'canvas-parent://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for canvas-courses scheme', () {
      final url = 'canvas-courses://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns course details ', () {
      final url = 'https://test.instructure.com/courses/123';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      );

      expect(widget, isA<CourseDetailsScreen>());
    });

    test('returns course details for grades', () {
      final url = 'https://test.instructure.com/courses/123/grades';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      );

      expect(widget, isA<CourseDetailsScreen>());
    });

    test('returns assignment details ', () {
      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://test.instructure.com/courses/$courseId/assignments/$assignmentId';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      ) as AssignmentDetailsScreen;

      expect(widget, isA<AssignmentDetailsScreen>());
      expect(widget.courseId, courseId);
      expect(widget.assignmentId, assignmentId);
    });

    // This route conflicts with assignment details, so having a specific test for it will ensure they aren't broken
    test('returns CourseRoutingShellScreen for syllabus', () {
      final courseId = '123';
      final url = 'https://test.instructure.com/courses/$courseId/assignments/syllabus';
      final widget = _getWidgetFromRoute(_rootWithUrl(url)) as CourseRoutingShellScreen;

      expect(widget, isA<CourseRoutingShellScreen>());
      expect(widget.courseId, courseId);
      expect(widget.type, CourseShellType.syllabus);
    });

    test('returns router error screen for mismatched domain with valid route', () {
      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://fakedomain.instructure.com/courses/$courseId/assignments/$assignmentId';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      ) as RouterErrorScreen;

      expect(widget, isA<RouterErrorScreen>());
    });

    test('returns Splash screen for qr login', () {
      final barcodeResultUrl = 'https://${QRUtils.QR_HOST}/canvas/login?${QRUtils.QR_AUTH_CODE}=1234'
          '&${QRUtils.QR_DOMAIN}=mobiledev.instructure.com';
      final widget = _getWidgetFromRoute(_rootWithUrl(barcodeResultUrl)) as SplashScreen;
      expect(widget, isA<SplashScreen>());
      expect(widget.qrLoginUrl, barcodeResultUrl);
    });

    test('returns Splash screen for qr login with no user signed in', () async {
      await setupPlatformChannels(config: PlatformConfig());
      final barcodeResultUrl = 'https://${QRUtils.QR_HOST}/canvas/login?${QRUtils.QR_AUTH_CODE}=1234'
          '&${QRUtils.QR_DOMAIN}=mobiledev.instructure.com';
      final widget = _getWidgetFromRoute(_rootWithUrl(barcodeResultUrl)) as SplashScreen;
      expect(widget, isA<SplashScreen>());
      expect(widget.qrLoginUrl, barcodeResultUrl);
    });

    // Added the following below tests because they are new cases for the router, two routes, one handler
    test('returns CourseRoutingShellScreen for frontPage', () {
      final courseId = '123';
      final url = 'https://test.instructure.com/courses/$courseId/pages/first-page';
      final widget = _getWidgetFromRoute(_rootWithUrl(url)) as CourseRoutingShellScreen;

      expect(widget, isA<CourseRoutingShellScreen>());
      expect(widget.courseId, courseId);
      expect(widget.type, CourseShellType.frontPage);
    });

    test('returns CourseRoutingShellScreen for frontPageWiki', () {
      final courseId = '123';
      final url = 'https://test.instructure.com/courses/$courseId/wiki';
      final widget = _getWidgetFromRoute(_rootWithUrl(url)) as CourseRoutingShellScreen;

      expect(widget, isA<CourseRoutingShellScreen>());
      expect(widget.courseId, courseId);
      expect(widget.type, CourseShellType.frontPage);
    });

    test('returns Dashboard for any route with no current user or QR', () async {
      ApiPrefs.setCurrentStudent(null);

      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://test.instructure.com/courses/$courseId/assignments/$assignmentId';

      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
      ) as DashboardScreen;

      expect(widget, isA<DashboardScreen>());
    });
  });

  group('internal url handler', () {
    testWidgetsWithAccessibilityChecks('returns assignment details', (tester) async {
      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://test.instructure.com/courses/$courseId/assignments/$assignmentId';
      await TestApp.showWidgetFromTap(tester, (context) => PandaRouter.routeInternally(context, url));

      verify(_analytics.logMessage('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockNav.pushRoute(any, PandaRouter.assignmentDetails(courseId, assignmentId)));
    });

    testWidgetsWithAccessibilityChecks('returns router error screen for mismatched domain with valid route',
        (tester) async {
      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://fakedomain.instructure.com/courses/$courseId/assignments/$assignmentId';
      await TestApp.showWidgetFromTap(tester, (context) => PandaRouter.routeInternally(context, url),
          config: PlatformConfig(initLoggedInUser: login));

      verify(_analytics.logMessage('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockNav.pushRoute(any, _routerErrorRoute(url)));
    });

    testWidgetsWithAccessibilityChecks('launches url for route without match', (tester) async {
      final url = 'https://test.instructure.com/courses/1567973/pages/key-test';

      when(_mockLauncher.canLaunch(url)).thenAnswer((_) => Future.value(true));
      when(_mockLauncher.launch(url)).thenAnswer((_) => Future.value(true));
      when(_mockWebContentInteractor.getAuthUrl(url)).thenAnswer((_) => Future.value(url));

      await TestApp.showWidgetFromTap(tester, (context) => PandaRouter.routeInternally(context, url));

      verify(_analytics.logMessage('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockLauncher.launch(url)).called(1);
    });

    testWidgetsWithAccessibilityChecks('shows snackbar error for canLaunch false', (tester) async {
      final url = 'https://test.instructure.com/brokenurl';
      when(_mockLauncher.canLaunch(url)).thenAnswer((_) => Future.value(false));

      await TestApp.showWidgetFromTap(tester, (context) => PandaRouter.routeInternally(context, url));

      verify(_analytics.logMessage('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockSnackbar.showSnackBar(any, 'An error occurred when trying to display this link'));
    });

    testWidgetsWithAccessibilityChecks('launches simpleWebView for limitAccessFlag without match', (tester) async {
      final newLogin = login.rebuild((b) => b.user = login.user
          .rebuild((user) => user.permissions = UserPermissionBuilder()..limitParentAppWebAccess = true)
          .toBuilder());

      final url = 'https://test.instructure.com/commons/1234';
      when(_mockWebContentInteractor.getAuthUrl(url)).thenAnswer((_) async => url);

      await TestApp.showWidgetFromTap(
        tester,
        (context) => PandaRouter.routeInternally(context, url),
        config: PlatformConfig(initLoggedInUser: newLogin),
      );

      verify(_analytics.logMessage('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockNav.pushRoute(any, PandaRouter.simpleWebViewRoute(url, AppLocalizations().webAccessLimitedMessage)));
    });
  });

  group('url route wrapper', () {
    test('returns valid UrlRouteWrapper', () {
      final path = '/courses/1567973';
      final url = '$_domain$path';
      final routeWrapper = PandaRouter.getRouteWrapper(url);

      assert(routeWrapper.path == path);
      assert(routeWrapper.validHost);
      assert(routeWrapper.appRouteMatch != null);
    });

    test('returns UrlRouteWrapper with validHost false and null routematch', () {
      final path = '/courses/12347/modules/12345';
      final url = 'https://fakedomain.instructure.com$path';
      final routeWrapper = PandaRouter.getRouteWrapper(url);

      assert(routeWrapper.path == path);
      assert(routeWrapper.validHost == false);
      assert(routeWrapper.appRouteMatch == null);
    });

    test('root path returns with validHost and null routematch', () {
      final path = '/';
      final url = '$_domain$path';
      final routeWrapper = PandaRouter.getRouteWrapper(url);

      assert(routeWrapper.path == path);
      assert(routeWrapper.validHost == true);
      assert(routeWrapper.appRouteMatch == null);
    });
  });
}

String _rootWithUrl(String url) => 'external?url=${Uri.encodeQueryComponent(url)}';

String _routerErrorRoute(String url) => '/error?url=${Uri.encodeQueryComponent(url)}';

String _simpleWebViewRoute(String url) => '/internal?url=${Uri.encodeQueryComponent(url)}';

Widget _getWidgetFromRoute(String route, {Map<String, List<String>>? extraParams}) {
  final match = PandaRouter.router.match(route);

  if (extraParams != null) match?.parameters.addAll(extraParams);
  final widget = (match!.route.handler as Handler).handlerFunc(null, match.parameters);

  return widget!;
}
