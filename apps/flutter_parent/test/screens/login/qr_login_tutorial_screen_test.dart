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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen_interactor.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  final barcodeResultUrl =
      'https://${QRUtils.QR_HOST}/canvas/login?${QRUtils.QR_AUTH_CODE}=1234'
      '&${QRUtils.QR_DOMAIN}=mobiledev.instructure.com';
  final interactor = _MockInteractor();
  final mockNav = _MockNav();

  setupTestLocator((locator) {
    locator.registerFactory<QRLoginTutorialScreenInteractor>(() => interactor);
    locator.registerLazySingleton<QuickNav>(() => mockNav);
  });

  testWidgetsWithAccessibilityChecks('Displays page objects and text correctly', (tester) async {
    await tester.runAsync(() async {
      await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
      await tester.pumpAndSettle();
      Element element = tester.element(find.byType(FractionallySizedBox));
      final FractionallySizedBox widget = element.widget;
      final Image image = widget.child;
      final ImageProvider imageProvider = image.image;
      await precacheImage(imageProvider, element);
      await tester.pumpAndSettle();
    });

    await expectLater(find.text(AppLocalizations().locateQRCode), findsOneWidget);
    await expectLater(find.text(AppLocalizations().qrCodeExplanation), findsOneWidget);
    await expectLater(find.text(AppLocalizations().next.toUpperCase()), findsOneWidget);
    await expectLater(find.bySemanticsLabel(AppLocalizations().qrCodeScreenshotContentDescription, skipOffstage: false), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking next scans, returns valid result, and pushes route', (tester) async {
    when(interactor.scan()).thenAnswer((_) => Future.value(BarcodeScanResult(
          true, result: barcodeResultUrl
        )));
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().next.toUpperCase()));
    await tester.pump();
    verify(mockNav.pushRoute(any, '/qr_login?qrLoginUrl=${Uri.encodeQueryComponent(barcodeResultUrl)}'));
  });

  testWidgetsWithAccessibilityChecks('Clicking next scans, returns invalid result, and displays camera error', (tester) async {
    when(interactor.scan()).thenAnswer((_) => Future.value(BarcodeScanResult(
        false, errorType: QRError.cameraError
    )));
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().next.toUpperCase()));
    await tester.pump();
    expect(find.text(AppLocalizations().qrCodeNoCameraError), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking next scans, returns invalid result, and displays qr error', (tester) async {
    when(interactor.scan()).thenAnswer((_) => Future.value(BarcodeScanResult(
        false, errorType: QRError.invalidQR
    )));
    await tester.pumpWidget(TestApp(QRLoginTutorialScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(AppLocalizations().next.toUpperCase()));
    await tester.pump();
    expect(find.text(AppLocalizations().invalidQRCodeError), findsOneWidget);
  });
}

class _MockInteractor extends Mock implements QRLoginTutorialScreenInteractor {}

class _MockNav extends Mock implements QuickNav {}