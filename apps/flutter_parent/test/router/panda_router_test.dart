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
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/router/router_error_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_screen.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_details_screen.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
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
import 'package:flutter_parent/utils/logger.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/veneers/flutter_snackbar_veneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:url_launcher_platform_interface/url_launcher_platform_interface.dart';

import '../utils/accessibility_utils.dart';
import '../utils/canvas_model_utils.dart';
import '../utils/platform_config.dart';
import '../utils/test_app.dart';

final _logger = _MockLogger();

void main() {
  final String _domain = 'https://test.instructure.com';
  final login = Login((b) => b
    ..domain = _domain
    ..accessToken = 'token'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  final _mockNav = _MockNav();
  final _mockWebContentInteractor = _MockWebContentInteractor();
  final _mockSnackbar = _MockSnackbar();

  setUpAll(() async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));
    PandaRouter.init();
    setupTestLocator((locator) {
      locator.registerLazySingleton<Logger>(() => _logger);
    });
  });

  setUp(() {
    reset(_logger);
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

    test('loginWeb returns web login screen', () {
      final domain = 'domain';
      final widget = _getWidgetFromRoute(
        PandaRouter.loginWeb(domain),
      ) as WebLoginScreen;

      expect(widget, isA<WebLoginScreen>());
      expect(widget.domain, domain);
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

    test('courseAnnouncementDetails returns announcemnt details screen', () {
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

    test('institutionAnnouncementDetails returns announcemnt details screen', () {
      final notificationId = '321';
      final widget = _getWidgetFromRoute(
        PandaRouter.institutionAnnouncementDetails(notificationId),
      ) as AnnouncementDetailScreen;

      expect(widget, isA<AnnouncementDetailScreen>());
      expect(widget.courseId, '');
      expect(widget.announcementId, notificationId);
      expect(widget.announcementType, AnnouncementType.INSTITUTION);
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
  });

  group('external url handler', () {
    test('returns splash when the url does not match any routes', () {
      final url = 'https://test.instructure.com/not-supported';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for splash handler
      );

      expect(widget, isA<SplashScreen>());
    });

    test('returns dashboard for https scheme', () {
      final url = 'https://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for conversations handler
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for http scheme', () {
      final url = 'http://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for conversations handler
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for canvas-parent scheme', () {
      final url = 'canvas-parent://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for conversations handler
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for canvas-courses scheme', () {
      final url = 'canvas-courses://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for conversations handler
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns course details ', () {
      final url = 'https://test.instructure.com/courses/123';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for course details handler
      );

      expect(widget, isA<CourseDetailsScreen>());
    });

    test('returns assignment details ', () {
      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://test.instructure.com/courses/$courseId/assignments/$assignmentId';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for assignment details handler
      ) as AssignmentDetailsScreen;

      expect(widget, isA<AssignmentDetailsScreen>());
      expect(widget.courseId, courseId);
      expect(widget.assignmentId, assignmentId);
    });

    test('returns router error screen for mismatched domain with valid route', () {
      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://fakedomain.instructure.com/courses/$courseId/assignments/$assignmentId';
      final widget = _getWidgetFromRoute(
        _rootWithUrl(url),
        logCount: 2,
      ) as RouterErrorScreen;

      expect(widget, isA<RouterErrorScreen>());
    });
  });

  group('internal url handler', () {
    testWidgetsWithAccessibilityChecks('returns assignment details', (tester) async {
      setupTestLocator((locator) {
        locator.registerLazySingleton<Logger>(() => _logger);
        locator.registerLazySingleton<QuickNav>(() => _mockNav);
        locator.registerLazySingleton<WebContentInteractor>(() => _mockWebContentInteractor);
      });

      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://test.instructure.com/courses/$courseId/assignments/$assignmentId';
      await TestApp.showWidgetFromTap(tester, (context) => PandaRouter.routeInternally(context, url));

      verify(_logger.log('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockNav.pushRoute(any, PandaRouter.assignmentDetails(courseId, assignmentId)));
    });

    testWidgetsWithAccessibilityChecks('returns router error screen for mismatched domain with valid route',
        (tester) async {
      setupTestLocator((locator) {
        locator.registerLazySingleton<Logger>(() => _logger);
        locator.registerLazySingleton<QuickNav>(() => _mockNav);
        locator.registerLazySingleton<WebContentInteractor>(() => _mockWebContentInteractor);
      });
      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://fakedomain.instructure.com/courses/$courseId/assignments/$assignmentId';
      await TestApp.showWidgetFromTap(tester, (context) => PandaRouter.routeInternally(context, url));

      verify(_logger.log('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockNav.pushRoute(any, _routerErrorRoute(url)));
    });

    testWidgetsWithAccessibilityChecks('launches url for route without match', (tester) async {
      var mockLauncher = _MockUrlLauncherPlatform();
      UrlLauncherPlatform.instance = mockLauncher;
      setupTestLocator((locator) {
        locator.registerLazySingleton<Logger>(() => _logger);
        locator.registerLazySingleton<WebContentInteractor>(() => _mockWebContentInteractor);
      });
      final url = 'https://test.instructure.com/courses/1567973/pages/key-test';

      when(mockLauncher.canLaunch(url)).thenAnswer((_) => Future.value(true));
      when(mockLauncher.launch(
        url,
        useSafariVC: anyNamed('useSafariVC'),
        useWebView: anyNamed('useWebView'),
        enableJavaScript: anyNamed('enableJavaScript'),
        enableDomStorage: anyNamed('enableDomStorage'),
        universalLinksOnly: anyNamed('universalLinksOnly'),
        headers: anyNamed('headers'),
      )).thenAnswer((_) => Future.value(true));
      when(_mockWebContentInteractor.getAuthUrl(url)).thenAnswer((_) => Future.value(url));

      await TestApp.showWidgetFromTap(tester, (context) => PandaRouter.routeInternally(context, url));

      verify(_logger.log('Attempting to route INTERNAL url: $url')).called(1);
      verify(mockLauncher.launch(
        url,
        useSafariVC: anyNamed('useSafariVC'),
        useWebView: anyNamed('useWebView'),
        enableJavaScript: anyNamed('enableJavaScript'),
        enableDomStorage: anyNamed('enableDomStorage'),
        universalLinksOnly: anyNamed('universalLinksOnly'),
        headers: anyNamed('headers'),
      )).called(1);
    });

    testWidgetsWithAccessibilityChecks('shows snackbar error for canLaunch false', (tester) async {
      var mockLauncher = _MockUrlLauncherPlatform();
      UrlLauncherPlatform.instance = mockLauncher;
      setupTestLocator((locator) {
        locator.registerLazySingleton<Logger>(() => _logger);
        locator.registerLazySingleton<WebContentInteractor>(() => _mockWebContentInteractor);
        locator.registerLazySingleton<FlutterSnackbarVeneer>(() => _mockSnackbar);
      });
      final url = 'https://test.instructure.com/brokenurl';
      when(mockLauncher.canLaunch(url)).thenAnswer((_) => Future.value(false));

      await TestApp.showWidgetFromTap(tester, (context) => PandaRouter.routeInternally(context, url));

      verify(_logger.log('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockSnackbar.showSnackBar(any, 'An error occurred when trying to display this link'));
    });

//     Todo once MBL-13924 is done
//    testWidgetsWithAccessibilityChecks('launches simpleWebView for limitAccessFlag without match', (tester) async {
//    });
  });

  group('internal url handler', () {
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

String _rootWithUrl(String url) => '/external?url=${Uri.encodeQueryComponent(url)}';
String _routerErrorRoute(String url) => '/error?url=${Uri.encodeQueryComponent(url)}';
String _simpleWebViewRoute(String url) => '/internal?url=${Uri.encodeQueryComponent(url)}';

Widget _getWidgetFromRoute(String route, {int logCount = 1}) {
  final match = PandaRouter.router.match(route);
  final widget = (match.route.handler as Handler).handlerFunc(null, match.parameters);

  if (logCount > 0) {
    verify(_logger.log(any)).called(logCount);
  } else {
    verifyNever(_logger.log(any));
  }

  return widget;
}

class _MockLogger extends Mock implements Logger {}

class _MockNav extends Mock implements QuickNav {}

class _MockWebContentInteractor extends Mock implements WebContentInteractor {}

class _MockSnackbar extends Mock implements FlutterSnackbarVeneer {}

class _MockUrlLauncherPlatform extends Mock with MockPlatformInterfaceMixin implements UrlLauncherPlatform {}
