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

import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/pairing/pairing_interactor.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/screens/pairing/qr_pairing_screen.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';
import 'pairing_util_test.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();

  MockPairingInteractor interactor = MockPairingInteractor();
  MockQuickNav nav = MockQuickNav();
  MockStudentAddedNotifier studentAddedNotifier = MockStudentAddedNotifier();

  setupTestLocator((locator) {
    locator.registerLazySingleton<PairingInteractor>(() => interactor);
    locator.registerLazySingleton<QuickNav>(() => nav);
    locator.registerLazySingleton<StudentAddedNotifier>(() => studentAddedNotifier);
  });

  setUp(() {
    reset(interactor);
    reset(nav);
    reset(studentAddedNotifier);
  });

  testWidgetsWithAccessibilityChecks('Displays tutorial', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    expect(find.text(l10n.qrPairingTutorialTitle), findsOneWidget);
    expect(find.text(l10n.qrPairingTutorialMessage), findsOneWidget);
    expect(find.byType(Image), findsOneWidget);
    expect(find.text(l10n.next.toUpperCase()), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays pairing state', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode()).thenAnswer((_) async => QRPairingScanResult.success('', '', ''));

    Completer<bool> completer = Completer();
    when(interactor.pairWithStudent(any)).thenAnswer((_) async => completer.future);

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Pops with true value on success', (tester) async {
    when(interactor.scanQRCode()).thenAnswer((_) async => QRPairingScanResult.success('', '', ''));
    when(interactor.pairWithStudent(any)).thenAnswer((_) async => true);

    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    var pairingFuture = QuickNav().push(context, QRPairingScreen());
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.byType(QRPairingScreen), findsNothing);
    expect(await pairingFuture, isTrue);
    verify(studentAddedNotifier.notify());
  });

  testWidgetsWithAccessibilityChecks('Navigates to splash screen on success if first route', (tester) async {
    when(interactor.pairWithStudent(any)).thenAnswer((_) async => true);

    await tester.pumpWidget(TestApp(QRPairingScreen(pairingInfo: QRPairingScanResult.success('', '', '') as QRPairingInfo)));
    await tester.pump();

    verify(nav.replaceRoute(any, PandaRouter.rootSplash()));
  });

  testWidgetsWithAccessibilityChecks('Navigates to account creation screen on success if is account creation',
      (tester) async {
    var code = '123';
    var domain = 'hodor.com';
    var accountId = '12345';
    when(interactor.scanQRCode()).thenAnswer((_) async => QRPairingScanResult.success(code, domain, accountId));

    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    QuickNav().push(context, QRPairingScreen(isCreatingAccount: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pumpAndSettle();

    verify(nav.pushRoute(any, PandaRouter.accountCreation(code, domain, accountId)));
  });

  testWidgetsWithAccessibilityChecks('Displays camera permission error', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode())
        .thenAnswer((_) async => QRPairingScanResult.error(QRPairingScanErrorType.cameraError));

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pump();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.qrPairingCameraPermissionTitle), findsOneWidget);
    expect(find.text(l10n.qrCodeNoCameraError), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays invalid QR error', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode())
        .thenAnswer((_) async => QRPairingScanResult.error(QRPairingScanErrorType.invalidCode));

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pump();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.qrPairingInvalidCodeTitle), findsOneWidget);
    expect(find.text(l10n.invalidQRCodeError), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays unknown error', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode()).thenAnswer((_) async => QRPairingScanResult.error(QRPairingScanErrorType.unknown));

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pump();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.qrPairingFailedTitle), findsOneWidget);
    expect(find.text(l10n.qrPairingFailedSubtitle), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not show error if scan is canceled', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode()).thenAnswer((_) async => QRPairingScanResult.error(QRPairingScanErrorType.canceled));

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pump();

    expect(find.byType(EmptyPandaWidget), findsNothing);
    expect(find.text(l10n.qrPairingTutorialMessage), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays network error', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode()).thenAnswer((_) async => QRPairingScanResult.success('', '', ''));
    when(interactor.pairWithStudent(any)).thenAnswer((_) async => null); // null represents network error

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.genericNetworkError), findsOneWidget);
    expect(find.text(l10n.qrPairingNetworkError), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays pairing failure error', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode()).thenAnswer((_) async => QRPairingScanResult.success('', '', ''));
    when(interactor.pairWithStudent(any)).thenAnswer((_) async => false); // false represents pairing failure

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.qrPairingFailedTitle), findsOneWidget);
    expect(find.text(l10n.qrPairingFailedSubtitle), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays incorrect domain error', (tester) async {
    Login login = Login((b) => b
      ..user = CanvasModelTestUtils.mockUser().toBuilder()
      ..accessToken = 'token'
      ..domain = 'test.instructure.com');

    await tester.pumpWidget(TestApp(
      QRPairingScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: login),
    ));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode())
        .thenAnswer((_) async => QRPairingScanResult.success('123abc', 'other.instructure.com', '123'));
    when(interactor.pairWithStudent(any)).thenAnswer((_) async => false);

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.qrPairingWrongDomainTitle), findsOneWidget);
    expect(find.text(l10n.qrPairingWrongDomainSubtitle), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Error state allows retry', (tester) async {
    await tester.pumpWidget(TestApp(QRPairingScreen()));
    await tester.pumpAndSettle();

    when(interactor.scanQRCode()).thenAnswer((_) async => QRPairingScanResult.success('', '', ''));
    when(interactor.pairWithStudent(any)).thenAnswer((_) async => null);

    await tester.tap(find.text(l10n.next.toUpperCase()));
    await tester.pumpAndSettle();

    // Should show network error
    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.genericNetworkError), findsOneWidget);
    expect(find.text(l10n.qrPairingNetworkError), findsOneWidget);

    when(interactor.scanQRCode())
        .thenAnswer((_) async => QRPairingScanResult.error(QRPairingScanErrorType.invalidCode));

    // Tap retry
    await tester.tap(find.text(l10n.retry));
    await tester.pumpAndSettle();

    // Should now show invalid code error
    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.qrPairingInvalidCodeTitle), findsOneWidget);
    expect(find.text(l10n.invalidQRCodeError), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Initiates pairing on launch if pairing info is provided', (tester) async {
    QRPairingInfo pairingInfo = QRPairingScanResult.success('123acb', '', '') as QRPairingInfo;
    await tester.pumpWidget(TestApp(QRPairingScreen(pairingInfo: pairingInfo)));
    await tester.pump();

    verify(interactor.pairWithStudent(pairingInfo.code));
  });
}
