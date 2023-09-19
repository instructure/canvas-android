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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_interactor.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/pairing/pairing_interactor.dart';
import 'package:flutter_parent/screens/pairing/qr_pairing_screen.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_util.dart';
import 'package:flutter_parent/screens/splash/splash_screen.dart';
import 'package:flutter_parent/screens/splash/splash_screen_interactor.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/common_widgets/two_finger_double_tap_gesture_detector.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
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

void main() async {
  final analytics = MockAnalytics();
  final interactor = MockDashboardInteractor();
  final authApi = MockAuthApi();
  final pairingInteractor = MockPairingInteractor();

  final login = Login((b) => b
    ..domain = 'domain'
    ..accessToken = 'token'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  await setupTestLocator((locator) {
    locator.registerLazySingleton<QuickNav>(() => QuickNav());
    locator.registerLazySingleton<Analytics>(() => analytics);
    locator.registerLazySingleton<AuthApi>(() => authApi);
    locator.registerLazySingleton<QRLoginUtil>(() => QRLoginUtil());

    locator.registerLazySingleton<PairingInteractor>(() => pairingInteractor);
    locator.registerFactory<DashboardInteractor>(() => interactor);
    locator.registerFactory<SplashScreenInteractor>(() => SplashScreenInteractor());
    locator.registerFactory<DomainSearchInteractor>(() => MockDomainSearchInteractor());
  });

  setUp(() async {
    reset(analytics);
    reset(interactor);
    reset(authApi);
    final mockRemoteConfig = setupMockRemoteConfig(
        valueSettings: {'qr_login_enabled_parent': 'true', 'qr_account_creation_enabled': 'true'});
    await setupPlatformChannels(config: PlatformConfig(initRemoteConfig: mockRemoteConfig));
    await ApiPrefs.init();
  });

  tearDown(() {
    RemoteConfigUtils.clean();
  });

  Future<void> twoFingerDoubleTap(WidgetTester tester) async {
    Offset center = tester.getCenter(find.byType(TwoFingerDoubleTapGestureDetector));

    // Perform first two-finger tap
    TestGesture pointer1 = await tester.startGesture(center.translate(-64, 64));
    TestGesture pointer2 = await tester.startGesture(center.translate(64, 64));
    await pointer1.up();
    await pointer2.up();

    // Perform second two-finger tap
    await tester.pump(Duration(milliseconds: 100));
    pointer1 = await tester.startGesture(center.translate(-64, 64));
    pointer2 = await tester.startGesture(center.translate(64, 64));
    await pointer1.up();
    await pointer2.up();
    await tester.pump();
  }

  testWidgetsWithAccessibilityChecks('Opens domain search screen', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().findSchool), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().findSchool));
    await tester.pumpAndSettle();

    expect(find.byType(DomainSearchScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays Snicker Doodles drawer', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    var size = tester.getSize(find.byType(LoginLandingScreen));
    await tester.flingFrom(Offset(size.width - 5, size.height / 2), Offset(-size.width / 2, 0), 1000);
    await tester.pumpAndSettle();

    expect(find.byType(Drawer), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not display login list if there are no previous logins', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().previousLogins), findsNothing);
    expect(find.byKey(Key('previous-logins')), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Displays login list if there are previous logins', (tester) async {

    List<Login> logins = [
      Login((b) => b
        ..domain = 'domain1'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 1').toBuilder()),
      Login((b) => b
        ..domain = 'domain2'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 2').toBuilder()),
    ];


    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await ApiPrefs.saveLogins(logins);
    await tester.pumpAndSettle();


    expect(find.text(AppLocalizations().previousLogins), findsOneWidget);
    expect(find.byKey(Key('previous-logins')), findsOneWidget);

    expect(find.text(logins[0].user.name), findsOneWidget);
    expect(find.text(logins[1].user.name), findsOneWidget);

    expect(find.text(logins[0].domain), findsOneWidget);
    expect(find.text(logins[1].domain), findsOneWidget);

    expect(find.byType(Avatar), findsNWidgets(2));
    expect(find.byIcon(Icons.clear), findsNWidgets(2));

  });

  testWidgetsWithAccessibilityChecks('Displays Previous Login correctly for masquerade', (tester) async {
    await tester.runAsync(() async {

      Login login = Login((b) => b
        ..domain = 'domain1'
        ..masqueradeDomain = 'masqueradeDomain'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 1').toBuilder()
        ..masqueradeUser = CanvasModelTestUtils.mockUser(name: 'masqueradeUser').toBuilder());

      await tester.pumpWidget(TestApp(LoginLandingScreen()));
      await ApiPrefs.saveLogins([login]);
      await tester.pumpAndSettle();

      expect(find.text(AppLocalizations().previousLogins), findsOneWidget);
      expect(find.byKey(Key('previous-logins')), findsOneWidget);

      expect(find.text(login.user.name), findsNothing);
      expect(find.text(login.masqueradeUser!.name), findsOneWidget);

      expect(find.text(login.domain), findsNothing);
      expect(find.text(login.masqueradeDomain!), findsOneWidget);

      expect(find.byType(Avatar), findsOneWidget);
      expect(find.byIcon(Icons.clear), findsOneWidget);
      expect(find.byIcon(CanvasIconsSolid.masquerade), findsOneWidget);
    });
  });

  testWidgetsWithAccessibilityChecks('Clearing previous login removes it from the list', (tester) async {
    await tester.runAsync(() async {

      List<Login> logins = [
        Login((b) => b
          ..domain = 'domain1'
          ..user = CanvasModelTestUtils.mockUser(name: 'user 1').toBuilder()),
        Login((b) => b
          ..domain = 'domain2'
          ..user = CanvasModelTestUtils.mockUser(name: 'user 2').toBuilder()),
      ];

      await tester.pumpWidget(TestApp(LoginLandingScreen()));
      await ApiPrefs.saveLogins(logins);
      await tester.pumpAndSettle();

      expect(find.byKey(Key('previous-logins')), findsOneWidget);
      expect(find.text(logins[0].user.name), findsOneWidget);
      expect(find.text(logins[1].user.name), findsOneWidget);

      // Remove second login
      await tester.tap(find.byIcon(Icons.clear).last);
      await tester.pumpAndSettle();

      expect(find.byKey(Key('previous-logins')), findsOneWidget);
      expect(find.text(logins[0].user.name), findsOneWidget);
      expect(find.text(logins[1].user.name), findsNothing);

      // Remove first login
      await tester.tap(find.byIcon(Icons.clear));
      await tester.pumpAndSettle();

      expect(find.byKey(Key('previous-logins')), findsNothing);
      expect(find.text(logins[0].user.name), findsNothing);
      expect(find.text(logins[1].user.name), findsNothing);

    });
  });

  testWidgetsWithAccessibilityChecks('Tapping a login sets the current login and loads splash screen', (tester) async {
    List<Login> logins = [
      Login((b) => b
        ..domain = 'domain1'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 1').toBuilder()),
    ];

    await tester.pumpWidget(TestApp(LoginLandingScreen(), platformConfig: PlatformConfig(initLoggedInUser: login)));
    await ApiPrefs.saveLogins(logins);
    await tester.pumpAndSettle();

    await tester.tap(find.text(logins[0].user.name));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 350));

    expect(find.byType(SplashScreen), findsOneWidget);
    expect(ApiPrefs.getCurrentLogin(), logins[0]);
    await ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Uses two-finger double-tap to cycle login flows', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    // First tap should move to the 'Canvas' login flow
    await twoFingerDoubleTap(tester);
    expect(find.text(AppLocalizations().loginFlowCanvas), findsOneWidget);

    // Second tap should move to the 'Site Admin' login flow
    await twoFingerDoubleTap(tester);
    expect(find.text(AppLocalizations().loginFlowSiteAdmin), findsOneWidget);

    // Third tap should move to the 'Skip mobile verify' login flow
    await twoFingerDoubleTap(tester);
    expect(find.text(AppLocalizations().loginFlowSkipMobileVerify), findsOneWidget);

    // Final tap should cycle back to the 'Normal' login flow
    await twoFingerDoubleTap(tester);
    expect(find.text(AppLocalizations().loginFlowNormal), findsOneWidget);

    await tester.pumpAndSettle(); // Wait for SnackBar to finish displaying
  });

  testWidgetsWithAccessibilityChecks('Passes selected LoginFlow to DomainSearchScreen', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    // Tap three times to move to the 'Skip mobile verify' login flow
    await twoFingerDoubleTap(tester);
    await twoFingerDoubleTap(tester);
    await twoFingerDoubleTap(tester);

    expect(find.text(AppLocalizations().findSchool), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().findSchool));
    await tester.pumpAndSettle();

    expect(find.byType(DomainSearchScreen), findsOneWidget);

    DomainSearchScreen domainSearch = tester.widget(find.byType(DomainSearchScreen));
    expect(domainSearch.loginFlow, LoginFlow.skipMobileVerify);

  });

  testWidgetsWithAccessibilityChecks('Tapping QR login shows QR Login picker', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await ApiPrefs.setCameraCount(2);
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().qrCode));
    await tester.pumpAndSettle();

    expect(find.byType(BottomSheet), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping have account in QR Login picker shows QR Login Tutorial screen',
      (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await ApiPrefs.setCameraCount(2);
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().qrCode));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().qrLoginHaveAccount));
    await tester.pumpAndSettle();

    expect(find.byType(QRLoginTutorialScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping new account in QR Login picker shows QR Pairing Tutorial screen',
      (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await ApiPrefs.setCameraCount(2);
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().qrCode));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().qrLoginNewAccount));
    await tester.pumpAndSettle();

    expect(find.byType(QRPairingScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('QR login does not display when camera count is 0', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await ApiPrefs.setCameraCount(0);
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().qrCode), findsNothing);
  });
}
