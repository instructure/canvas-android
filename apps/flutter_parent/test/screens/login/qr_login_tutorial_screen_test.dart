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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen_interactor.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final barcodeResultUrl = 'https://${QRUtils.QR_HOST}/canvas/login?${QRUtils.QR_AUTH_CODE}=1234'
      '&${QRUtils.QR_DOMAIN}=mobiledev.instructure.com';
  final interactor = MockQRLoginTutorialScreenInteractor();
  final mockNav = MockQuickNav();

  setupTestLocator((locator) {
    locator.registerFactory<QRLoginTutorialScreenInteractor>(() => interactor);
    locator.registerLazySingleton<QuickNav>(() => mockNav);
  });

  setUp(() {
    reset(interactor);
    reset(mockNav);
  });

  testWidgetsWithAccessibilityChecks('Displays page objects and text correctly', (tester) async {
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.runAsync(() async {
      Element element = tester.element(find.byType(FractionallySizedBox));
      final FractionallySizedBox widget = element.widget as FractionallySizedBox;
      final Image image = widget.child as Image;
      final ImageProvider imageProvider = image.image;
      await precacheImage(imageProvider, element);
      await tester.pumpAndSettle();
    });

    await expectLater(find.text(AppLocalizations().locateQRCode), findsOneWidget);
    await expectLater(find.text(AppLocalizations().qrCodeExplanation), findsOneWidget);
    await expectLater(find.text(AppLocalizations().next.toUpperCase()), findsOneWidget);
    await expectLater(find.bySemanticsLabel(AppLocalizations().qrCodeScreenshotContentDescription, skipOffstage: false),
        findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking next scans, returns valid result, and pushes route', (tester) async {
    when(interactor.scan()).thenAnswer((_) => Future.value(BarcodeScanResult(true, result: barcodeResultUrl)));
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().next.toUpperCase()));
    await tester.pumpAndSettle();
    verify(mockNav.pushRoute(any, '/qr_login?qrLoginUrl=${Uri.encodeQueryComponent(barcodeResultUrl)}'));
  });

  testWidgetsWithAccessibilityChecks('Clicking next scans, returns invalid result, and displays camera error',
      (tester) async {
    when(interactor.scan()).thenAnswer((_) => Future.value(BarcodeScanResult(false, errorType: QRError.cameraError)));
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().next.toUpperCase()));
    await tester.pump();
    expect(find.text(AppLocalizations().qrCodeNoCameraError), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking next scans, returns invalid result, and displays qr error',
      (tester) async {
    when(interactor.scan()).thenAnswer((_) => Future.value(BarcodeScanResult(false, errorType: QRError.invalidQR)));
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().next.toUpperCase()));
    await tester.pump();
    expect(find.text(AppLocalizations().invalidQRCodeError), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking next scans, user cancels, and displays no error', (tester) async {
    when(interactor.scan()).thenAnswer((_) => Future.value(BarcodeScanResult(false, errorType: QRError.cancelled)));
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().next.toUpperCase()));
    await tester.pump();
    expect(find.byType(SnackBar), findsNothing);
  });

  testWidgetsWithAccessibilityChecks(
      'Clicking next scans, returns valid result, but then displays qr login error on login failure', (tester) async {
    when(interactor.scan()).thenAnswer((_) => Future.value(BarcodeScanResult(true, result: barcodeResultUrl)));
    when(mockNav.pushRoute(any, any)).thenAnswer((_) => Future.value(AppLocalizations().loginWithQRCodeError));
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().next.toUpperCase()));
    await tester.pumpAndSettle();
    expect(find.text(AppLocalizations().loginWithQRCodeError), findsOneWidget);
  });
}