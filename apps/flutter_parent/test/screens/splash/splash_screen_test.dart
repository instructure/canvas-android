// Copyright (C) 2019 - present Instructure, Inc.
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

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/not_a_parent_screen.dart';
import 'package:flutter_parent/screens/splash/splash_screen.dart';
import 'package:flutter_parent/utils/common_widgets/canvas_loading_indicator.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  final login = Login((b) => b
    ..domain = 'domain'
    ..accessToken = 'token'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  testWidgetsWithAccessibilityChecks('Displays loadingIndicator', (tester) async {
    var interactor = _MockInteractor();
    var nav = _MockNav();
    setupTestLocator((locator) {
      locator.registerFactory<DashboardInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => nav);
    });

    final completer = Completer<List<User>>();
    when(interactor.getStudents(forceRefresh: true)).thenAnswer((_) => completer.future);

    await tester.pumpWidget(TestApp(SplashScreen()));
    await tester.pump();

    expect(find.byType(CanvasLoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Routes to not-a-parent screen when there are no students and user is logged in',
      (tester) async {
    var interactor = _MockInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<DashboardInteractor>(() => interactor);
    });

    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));

    when(interactor.getStudents(forceRefresh: true)).thenAnswer((_) => Future.value([]));

    await tester.pumpWidget(TestApp(SplashScreen(), highContrast: true));
    await tester.pumpAndSettle();

    expect(find.byType(NotAParentScreen), findsOneWidget);
    await ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Routes to login screen when the user is not logged in', (tester) async {
    var interactor = _MockInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<DashboardInteractor>(() => interactor);
    });

    await ApiPrefs.clean();

    await tester.pumpWidget(TestApp(SplashScreen(), highContrast: true));
    await tester.pumpAndSettle();

    expect(find.byType(LoginLandingScreen), findsOneWidget);
  });

  /* - TODO - fix this up if this route starts to pre-fetch students again
  testWidgetsWithAccessibilityChecks('Routes to dashboard when there are students', (tester) async {
    var interactor = _MockInteractor();
    var nav = _MockNav();
    setupTestLocator((locator) {
      locator.registerFactory<DashboardInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => nav);
    });

    var students = [User()];
    final completer = Completer<List<User>>();
    when(interactor.getStudents(forceRefresh: true)).thenAnswer((_) => completer.future);

    await tester.pumpWidget(TestApp(SplashScreen(), highContrast: true));
    await tester.pump();

    completer.complete(students);
    await tester.pump();
    await tester.pump(Duration(milliseconds: 350));

    var route = verify(nav.replaceRoute(any, captureAny)).captured[0];
    expect(route.runtimeType, PageRouteBuilder);

    var screen = route.pageBuilder(null, null, null);
    expect(screen.runtimeType, DashboardScreen);
    expect((screen as DashboardScreen).students, students);
  });
   */

  testWidgetsWithAccessibilityChecks('Routes to dashboard without students on error', (tester) async {
    var interactor = _MockInteractor();
    var observer = _MockNavigatorObserver();
    setupTestLocator((locator) {
      locator.registerFactory<DashboardInteractor>(() => interactor);
    });

    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));

    final completer = Completer<List<User>>();
    when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) => completer.future);
    when(interactor.getSelf()).thenAnswer((_) => Future.value(login.user));
    when(interactor.getAlertCountNotifier()).thenReturn(_MockAlertCountNotifier());
    when(interactor.getInboxCountNotifier()).thenReturn(_MockInboxCountNotifier());

    await tester.pumpWidget(TestApp(
      SplashScreen(),
      highContrast: true,
      navigatorObservers: [observer],
    ));
    await tester.pump();

    completer.completeError('Fake error');
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 350));

    var route =
        verify(observer.didReplace(newRoute: captureAnyNamed('newRoute'), oldRoute: anyNamed('oldRoute'))).captured[0];
    expect(route.runtimeType, PageRouteBuilder);

    var screen = route.buildPage(null, null, null);
    expect(screen.runtimeType, DashboardScreen);
    expect((screen as DashboardScreen).students, isNull);
    ApiPrefs.clean();
  });
}

class _MockInteractor extends Mock implements DashboardInteractor {}

class _MockNav extends Mock implements QuickNav {}

class _MockNavigatorObserver extends Mock implements NavigatorObserver {}

class _MockAlertCountNotifier extends Mock implements AlertCountNotifier {}

class _MockInboxCountNotifier extends Mock implements InboxCountNotifier {}
