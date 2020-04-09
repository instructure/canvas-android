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
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {

  final interactor = _MockWebLoginInteractor();

  setUp(() {
    reset(interactor);
    setupTestLocator((locator) {
      locator.registerFactory<WebLoginInteractor>(() => interactor);
    });
  });

  testWidgetsWithAccessibilityChecks('Shows the domain in the toolbar', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value(MobileVerifyResult()));

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pump();

    expect(find.text(domain), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows loading while doing mobile verify', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value(MobileVerifyResult()));

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not show loading when mobile verify is finished', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value(MobileVerifyResult()));

    await tester.pumpWidget(TestApp(WebLoginScreen(domain), platformConfig: PlatformConfig(initWebview: true)));
    await tester.pumpAndSettle();

    expect(find.byType(CircularProgressIndicator), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows a web view when mobile verify is finished', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value(MobileVerifyResult()));

    await tester.pumpWidget(TestApp(WebLoginScreen(domain), platformConfig: PlatformConfig(initWebview: true)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows a dialog when mobile verify failed with general error', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain))
        .thenAnswer((_) => Future.value(MobileVerifyResult((b) => b..result = VerifyResultEnum.generalError)));

    await tester.pumpWidget(TestApp(
      WebLoginScreen(domain),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text(AppLocalizations().domainVerificationErrorGeneral), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows a dialog when mobile verify failed because of domain', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain))
        .thenAnswer((_) => Future.value(MobileVerifyResult((b) => b..result = VerifyResultEnum.domainNotAuthorized)));

    await tester.pumpWidget(TestApp(
      WebLoginScreen(domain),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text(AppLocalizations().domainVerificationErrorDomain), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows a dialog when mobile verify failed because of user agent', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain))
        .thenAnswer((_) => Future.value(MobileVerifyResult((b) => b..result = VerifyResultEnum.unknownUserAgent)));

    await tester.pumpWidget(TestApp(
      WebLoginScreen(domain),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text(AppLocalizations().domainVerificationErrorUserAgent), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows a dialog when mobile verify failed when unknown', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain))
        .thenAnswer((_) => Future.value(MobileVerifyResult((b) => b..result = VerifyResultEnum.unknownError)));

    await tester.pumpWidget(TestApp(
      WebLoginScreen(domain),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text(AppLocalizations().domainVerificationErrorUnknown), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows a dialog when mobile verify failed', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.error(null));

    await tester.pumpWidget(TestApp(
      WebLoginScreen(domain),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text(AppLocalizations().domainVerificationErrorUnknown), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows a dialog that can be closed', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.error(null));

    await tester.pumpWidget(TestApp(WebLoginScreen(domain), platformConfig: PlatformConfig(initWebview: true)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);

    final matchedWidget = find.text(AppLocalizations().ok);
    expect(matchedWidget, findsOneWidget);
    await tester.tap(matchedWidget);
    await tester.pumpAndSettle();

    expect(find.byType(Dialog), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows a skip verify dialog when that is the login type', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value());
    when(interactor.performLogin(any, any)).thenAnswer((_) => Future.value());

    await tester.pumpWidget(TestApp(
      WebLoginScreen(domain, loginFlow: LoginFlow.skipMobileVerify),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pump();
    await tester.pump();
    await tester.pump(Duration(milliseconds: 1000));

    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text(AppLocalizations().skipMobileVerifyTitle), findsOneWidget);
    await tester.enterText(find.byKey(Key(WebLoginScreen.PROTOCOL_SKIP_VERIFY_KEY)), '');

    // Tap ok, verify we still have a dialog when id/secret are empty
    await tester.tap(find.text(AppLocalizations().ok));
    await tester.pump();
    expect(find.byType(Dialog), findsOneWidget);

    await tester.enterText(find.byKey(Key(WebLoginScreen.PROTOCOL_SKIP_VERIFY_KEY)), 'https');
    await tester.enterText(find.byKey(Key(WebLoginScreen.ID_SKIP_VERIFY_KEY)), 'id');
    await tester.enterText(find.byKey(Key(WebLoginScreen.SECRET_SKIP_VERIFY_KEY)), 'secret');

    // Tap ok, verify the dialog is gone now
    await tester.tap(find.text(AppLocalizations().ok.toUpperCase()));
    await tester.pump();
    await tester.pump();
    expect(find.byType(Dialog), findsNothing);

    verifyNever(interactor.mobileVerify(any));
  });

  testWidgetsWithAccessibilityChecks('Can cancel a skip verify dialog when that is the login type', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value());
    when(interactor.performLogin(any, any)).thenAnswer((_) => Future.value());

    await tester.pumpWidget(TestApp(
      WebLoginScreen(domain, loginFlow: LoginFlow.skipMobileVerify),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pump();
    await tester.pump();
    await tester.pump(Duration(milliseconds: 1000));

    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text(AppLocalizations().skipMobileVerifyTitle), findsOneWidget);

    await tester.tap(find.text(AppLocalizations().cancel.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.byType(Dialog), findsNothing);
    verify(interactor.mobileVerify(any));
  });

  testWidgetsWithAccessibilityChecks('Can cancel a skip verify dialog when that is the login type', (tester) async {
    final domain = 'domain';
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value());
    when(interactor.performLogin(any, any)).thenAnswer((_) => Future.value());

    await tester.pumpWidget(TestApp(
      WebLoginScreen(domain, loginFlow: LoginFlow.skipMobileVerify),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pump();
    await tester.pump();
    await tester.pump(Duration(milliseconds: 1000));

    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text(AppLocalizations().skipMobileVerifyTitle), findsOneWidget);

    await tester.tap(find.text(AppLocalizations().cancel.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.byType(Dialog), findsNothing);
    verify(interactor.mobileVerify(any));
  });
}

class _MockWebLoginInteractor extends Mock implements WebLoginInteractor {}
