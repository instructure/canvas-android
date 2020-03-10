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
import 'package:flutter_parent/router/parent_router.dart';
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
import 'package:flutter_parent/utils/logger.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../utils/test_app.dart';

final _logger = _MockLogger();

void main() {
  setUpAll(() {
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
  });

  group('url handler', () {
    test('returns splash when the url does not match any routes', () {
      final url = 'https://test.instructure.com/not-supported';
      final widget = _getWidgetFromRoute(
        PandaRouter.rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for splash handler
      );

      expect(widget, isA<SplashScreen>());
    });

    test('returns dashboard for https scheme', () {
      final url = 'https://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        PandaRouter.rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for conversations handler
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for http scheme', () {
      final url = 'http://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        PandaRouter.rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for conversations handler
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for canvas-parent scheme', () {
      final url = 'canvas-parent://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        PandaRouter.rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for conversations handler
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns dashboard for canvas-courses scheme', () {
      final url = 'canvas-courses://test.instructure.com/conversations';
      final widget = _getWidgetFromRoute(
        PandaRouter.rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for conversations handler
      );

      expect(widget, isA<ConversationListScreen>());
    });

    test('returns course details ', () {
      final url = 'https://test.instructure.com/courses/123';
      final widget = _getWidgetFromRoute(
        PandaRouter.rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for course details handler
      );

      expect(widget, isA<CourseDetailsScreen>());
    });

    test('returns assignment details ', () {
      final courseId = '123';
      final assignmentId = '321';
      final url = 'https://test.instructure.com/courses/$courseId/assignments/$assignmentId';
      final widget = _getWidgetFromRoute(
        PandaRouter.rootWithUrl(url),
        logCount: 2, // Once for root url handling, another for assignment details handler
      ) as AssignmentDetailsScreen;

      expect(widget, isA<AssignmentDetailsScreen>());
      expect(widget.courseId, courseId);
      expect(widget.assignmentId, assignmentId);
    });
  });
}

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
