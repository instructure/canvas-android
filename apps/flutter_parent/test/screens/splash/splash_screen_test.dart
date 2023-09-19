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

import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/aup/acceptable_use_policy_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/not_a_parent_screen.dart';
import 'package:flutter_parent/screens/splash/splash_screen.dart';
import 'package:flutter_parent/screens/splash/splash_screen_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/canvas_loading_indicator.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final login = Login((b) => b
    ..domain = 'domain'
    ..accessToken = 'token'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  setUp(() async {
    final mockRemoteConfig = setupMockRemoteConfig(valueSettings: {'qr_login_enabled_parent': 'true'});
    await setupPlatformChannels(config: PlatformConfig(initRemoteConfig: mockRemoteConfig));
  });

  tearDown(() {
    RemoteConfigUtils.clean();
  });

  testWidgetsWithAccessibilityChecks('Displays loadingIndicator', (tester) async {
    var interactor = MockSplashScreenInteractor();
    var nav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => nav);
    });

    final completer = Completer<SplashScreenData>();
    when(interactor.getData()).thenAnswer((_) => completer.future);
    when(interactor.isTermsAcceptanceRequired()).thenAnswer((_) async => false);

    await tester.pumpWidget(TestApp(SplashScreen()));
    await tester.pump();
    await tester.pump();

    expect(find.byType(CanvasLoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Routes to not-a-parent screen if not an observer and cannot masquerade',
      (tester) async {
    var interactor = MockSplashScreenInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
    });

    when(interactor.isTermsAcceptanceRequired()).thenAnswer((_) async => false);
    when(interactor.getData()).thenAnswer((_) async => SplashScreenData(false, false));

    await tester.pumpWidget(TestApp(
      SplashScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: login),
    ));
    await tester.pumpAndSettle();
    await tester.pump();
    await tester.pump();

    expect(find.byType(NotAParentScreen), findsOneWidget);
    await ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Routes to Acceptable Use Policy screen if needed', (tester) async {
    var interactor = MockSplashScreenInteractor();
    var mockNav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => mockNav);
    });
    
    when(interactor.getData()).thenAnswer((_) async => SplashScreenData(true, false));
    when(interactor.isTermsAcceptanceRequired()).thenAnswer((_) async => true);

    await tester.pumpWidget(TestApp(
      SplashScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: login),
    ));
    await tester.pump(); // Pump to get data
    await tester.pump(); // Pump to update with result
    await tester.pump(); // Pump to update with result
    await tester.pump(const Duration(milliseconds: 350)); // Pump for animation finish

    verify(mockNav.pushRouteWithCustomTransition(any, '/aup', any, any, any));
    await ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Routes to dashboard if not an observer but can masquerade', (tester) async {
    var interactor = MockSplashScreenInteractor();
    var mockNav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => mockNav);
    });

    when(interactor.getData()).thenAnswer((_) async => SplashScreenData(false, true));
    when(interactor.isTermsAcceptanceRequired()).thenAnswer((_) async => false);

    await tester.pumpWidget(TestApp(
      SplashScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: login),
    ));
    await tester.pump(); // Pump to get data
    await tester.pump(); // Pump to update with result
    await tester.pump(); // Pump to update with result
    await tester.pump(const Duration(milliseconds: 350)); // Pump for animation finish

    verify(mockNav.pushRouteWithCustomTransition(any, '/dashboard', any, any, any));
    await ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Routes to dashboard if an observer but cannot masquerade', (tester) async {
    var interactor = MockSplashScreenInteractor();
    var mockNav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => mockNav);
    });

    when(interactor.getData()).thenAnswer((_) async => SplashScreenData(true, false));
    when(interactor.isTermsAcceptanceRequired()).thenAnswer((_) async => false);

    await tester.pumpWidget(TestApp(
      SplashScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: login),
    ));
    await tester.pump(); // Pump to get data
    await tester.pump(); // Pump to update with result
    await tester.pump(); // Pump to update with result
    await tester.pump(const Duration(milliseconds: 350)); // Pump for animation finish

    verify(mockNav.pushRouteWithCustomTransition(any, '/dashboard', any, any, any));
    await ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Routes to login screen when the user is not logged in', (tester) async {
    var interactor = MockSplashScreenInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
    });

    when(interactor.getCameraCount()).thenAnswer((_) => Future.value(2));

    await tester.pumpWidget(TestApp(SplashScreen()));
    await tester.pumpAndSettle();

    expect(find.byType(LoginLandingScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks(
      'Routes to login screen when the user is not logged in and camera count throws error', (tester) async {
    var interactor = MockSplashScreenInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
    });

    when(interactor.getCameraCount()).thenAnswer((_) => Future.error('error'));

    await tester.pumpWidget(TestApp(SplashScreen()));
    await tester.pumpAndSettle();

    expect(find.byType(LoginLandingScreen), findsOneWidget);
  });

  /* - TODO - fix this up if this route starts to pre-fetch students again
  testWidgetsWithAccessibilityChecks('Routes to dashboard when there are students', (tester) async {
    var interactor = _MockInteractor();
    var nav = _MockNav();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => nav);
    });

    var students = [User()];
    final completer = Completer<List<User>>();
    when(interactor.getStudents(forceRefresh: true)).thenAnswer((_) => completer.future);

    await tester.pumpWidget(TestApp(SplashScreen()));
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
    var interactor = MockSplashScreenInteractor();
    var mockNav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => mockNav);
    });

    final completer = Completer<SplashScreenData>();
    when(interactor.getData()).thenAnswer((_) => completer.future);
    when(interactor.isTermsAcceptanceRequired()).thenAnswer((_) async => false);

    await tester.pumpWidget(TestApp(
      SplashScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: login),
    ));
    await tester.pump();

    completer.completeError('Fake error');
    await tester.pump();
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 350));

    verify(mockNav.pushRouteWithCustomTransition(any, '/dashboard', any, any, any));
    await ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Requests MasqueradeUI refresh', (tester) async {
    var masqueradeUser = CanvasModelTestUtils.mockUser(name: "Masked User");
    var maskedLogin = login.rebuild((b) => b
      ..masqueradeDomain = 'masqueradeDomain'
      ..masqueradeUser = masqueradeUser.toBuilder());
    var masqueradeInfo = AppLocalizations().actingAsUser(masqueradeUser.name);

    var interactor = MockSplashScreenInteractor();
    var mockNav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<SplashScreenInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => mockNav);
    });

    when(interactor.getData()).thenAnswer((_) async {
      ApiPrefs.switchLogins(maskedLogin);
      return SplashScreenData(true, false);
    });
    when(interactor.isTermsAcceptanceRequired()).thenAnswer((_) async => false);

    await tester.pumpWidget(TestApp(
      SplashScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: login),
    ));
    await tester.pump(); // Pump to get data

    // Should not show masquerade info at this point
    expect(find.text(masqueradeInfo), findsNothing);

    await tester.pump(); // Pump to update with result
    await tester.pump(); // Pump to update with result
    await tester.pump(const Duration(milliseconds: 350)); // Pump for animation finish

    // Should now show masquerade info
    expect(find.text(masqueradeInfo), findsOneWidget);
    await ApiPrefs.clean();
  });
}