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

import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/account_creation/account_creation_interactor.dart';
import 'package:flutter_parent/screens/account_creation/account_creation_screen.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/finders.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';
import '../courses/course_summary_screen_test.dart';

/**
 * NOTE FOR TEST FILE: A lot of the below tests have minTapSize disabled, this is due to containing inline links,
 * which are not required to meet the min tap target size
 */
void main() {
  final interactor = MockAccountCreationInteractor();
  final mockNav = MockQuickNav();
  final analytics = MockAnalytics();

  final tos = TermsOfService((b) => b
    ..accountId = '123'
    ..id = '123'
    ..passive = false);

  final tosPassive = TermsOfService((b) => b
    ..accountId = '123'
    ..id = '123'
    ..passive = true);

  final pairingInfo = QRPairingScanResult.success('123', 'hodor.com', '123') as QRPairingInfo;
  final tosString =
      'By tapping \'Create Account\', you agree to the Terms of Service and acknowledge the Privacy Policy';

  setupTestLocator((locator) {
    locator.registerFactory<AccountCreationInteractor>(() => interactor);
    locator.registerLazySingleton<QuickNav>(() => mockNav);
    locator.registerLazySingleton<Analytics>(() => analytics);
  });

  setUp(() {
    reset(interactor);
  });

  group('rendering and loading', () {
    testWidgetsWithAccessibilityChecks('forms and labels', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();
      expect(find.text('Full Name'), findsOneWidget);
      expect(find.text('Full Name…'), findsOneWidget);

      expect(find.text('Email Address'), findsOneWidget);
      expect(find.text('Email…'), findsOneWidget);

      expect(find.text('Password'), findsOneWidget);
      expect(find.text('Password…'), findsOneWidget);

      expect(find.byIcon(CanvasIcons.eye), findsOneWidget);

      expect(find.text('Create Account', skipOffstage: false), findsOneWidget);
      expect(find.byType(ElevatedButton, skipOffstage: false), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('tos and privacy text visible when passive false', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      expect(find.richText(tosString, skipOffstage: false), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('only privacy text visible when passive false', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tosPassive);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      expect(find.richText('View the Privacy Policy', skipOffstage: false), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('tos and privacy text visible when passive false', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      expect(find.richText(tosString, skipOffstage: false), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('already have an account text present', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      expect(find.richText('Already have an account? Sign In', skipOffstage: false), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('tos loading indicator', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pump();

      expect(find.byType(LoadingIndicator, skipOffstage: false), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});
  });

  group('form validation and errors', () {
    testWidgetsWithAccessibilityChecks('correct forms show no errors', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();
      await tester.enterText(find.widgetWithText(TextFormField, 'Full Name…'), 'hodor');
      await tester.enterText(find.widgetWithText(TextFormField, 'Email…'), 'hodor@hodor.com');
      await tester.enterText(find.widgetWithText(TextFormField, 'Password…'), '12345678');
      await tester.drag(find.byType(Scaffold), Offset(0, -500));
      await tester.pumpAndSettle();

      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      expect(find.text('Please enter full name'), findsNothing);
      expect(find.text('Please enter an email address'), findsNothing);
      expect(find.text('Please enter a valid email address'), findsNothing);
      expect(find.text('Password is required'), findsNothing);
      expect(find.text('Password must contain at least 8 characters'), findsNothing);
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('empty forms show errors', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();
      await tester.drag(find.byType(Scaffold), Offset(0, -500));
      await tester.pumpAndSettle();

      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      expect(find.text('Please enter full name'), findsOneWidget);
      expect(find.text('Please enter an email address'), findsOneWidget);
      expect(find.text('Password is required'), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('password too short error', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.enterText(find.widgetWithText(TextFormField, 'Password…'), '1234567');

      await tester.drag(find.byType(Scaffold), Offset(0, -500));
      await tester.pumpAndSettle();

      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      expect(find.text('Password must contain at least 8 characters'), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('invalid email error', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.enterText(find.widgetWithText(TextFormField, 'Email…'), 'hodor');

      await tester.drag(find.byType(Scaffold), Offset(0, -500));
      await tester.pumpAndSettle();

      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      expect(find.text('Please enter a valid email address'), findsOneWidget);
    }, a11yExclusions: {A11yExclusion.minTapSize});
  });

  group('inline links', () {
    testWidgetsWithAccessibilityChecks('sign in link pushes login route', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();
      await tester.drag(find.byType(Scaffold), Offset(0, -500));
      await tester.pumpAndSettle();

      await tester.tap(find.richText('Already have an account? Sign In'));

      verify(mockNav.pushRoute(any, PandaRouter.loginWeb('hodor.com', loginFlow: LoginFlow.normal)));
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('Tos link pushes tos route', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.drag(find.byType(Scaffold), Offset(0, -1000));
      await tester.pumpAndSettle();

      // Get text selection for 'Canvas Support' span
      var targetText = l10n.qrCreateAccountTermsOfService;
      var bodyWidget = tester.widget<Text>(find.byKey(AccountCreationScreen.accountCreationTextSpanKey));
      var bodyText = bodyWidget.textSpan?.toPlainText() ?? '';
      var index = bodyText.indexOf(targetText);
      var selection = TextSelection(baseOffset: index, extentOffset: index + targetText.length);

      // Get clickable area
      RenderParagraph box = AccountCreationScreen.accountCreationTextSpanKey.currentContext?.findRenderObject() as RenderParagraph;
      var bodyOffset = box.localToGlobal(Offset.zero);
      var textOffset = box.getBoxesForSelection(selection)[0].toRect().center;

      await tester.tapAt(bodyOffset + textOffset);

      verify(mockNav.pushRoute(any, PandaRouter.termsOfUse(accountId: '123', domain: 'hodor.com')));
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('failed tos load launches default tos url', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => null);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.drag(find.byType(Scaffold), Offset(0, -1000));
      await tester.pumpAndSettle();

      // Get text selection for 'Canvas Support' span
      var targetText = l10n.qrCreateAccountTermsOfService;
      var bodyWidget = tester.widget<Text>(find.byKey(AccountCreationScreen.accountCreationTextSpanKey));
      var bodyText = bodyWidget.textSpan?.toPlainText() ?? '';
      var index = bodyText.indexOf(targetText);
      var selection = TextSelection(baseOffset: index, extentOffset: index + targetText.length);

      // Get clickable area
      RenderParagraph box = AccountCreationScreen.accountCreationTextSpanKey.currentContext?.findRenderObject() as RenderParagraph;
      var bodyOffset = box.localToGlobal(Offset.zero);
      var textOffset = box.getBoxesForSelection(selection)[0].toRect().center;

      await tester.tapAt(bodyOffset + textOffset);

      verify(interactor.launchDefaultToS());
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('privacy policy link launches privacy policy url', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => null);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.drag(find.byType(Scaffold), Offset(0, -1000));
      await tester.pumpAndSettle();

      // Get text selection for 'Canvas Support' span
      var targetText = l10n.qrCreateAccountPrivacyPolicy;
      var bodyWidget = tester.widget<Text>(find.byKey(AccountCreationScreen.accountCreationTextSpanKey));
      var bodyText = bodyWidget.textSpan?.toPlainText() ?? '';
      var index = bodyText.indexOf(targetText);
      var selection = TextSelection(baseOffset: index, extentOffset: index + targetText.length);

      // Get clickable area
      RenderParagraph box = AccountCreationScreen.accountCreationTextSpanKey.currentContext?.findRenderObject() as RenderParagraph;
      var bodyOffset = box.localToGlobal(Offset.zero);
      var textOffset = box.getBoxesForSelection(selection)[0].toRect().center;

      await tester.tapAt(bodyOffset + textOffset);

      verify(interactor.launchPrivacyPolicy());
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('passive tos privacy policy link launches privacy policy url', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tosPassive);

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.drag(find.byType(Scaffold), Offset(0, -1000));
      await tester.pumpAndSettle();

      await tester.tap(find.richText(l10n.qrCreateAccountViewPrivacy));

      verify(interactor.launchPrivacyPolicy());
    }, a11yExclusions: {A11yExclusion.minTapSize});
  });

  group('account creation', () {
    testWidgetsWithAccessibilityChecks('valid form account creation pushes login route', (tester) async {
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);
      when(interactor.createNewAccount('123', '123', 'hodor', 'hodor@hodor.com', '12345678', 'hodor.com'))
          .thenAnswer((_) async => Response(statusCode: 200, requestOptions: RequestOptions(path: '')));

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.enterText(find.widgetWithText(TextFormField, 'Full Name…'), 'hodor');
      await tester.enterText(find.widgetWithText(TextFormField, 'Email…'), 'hodor@hodor.com');
      await tester.enterText(find.widgetWithText(TextFormField, 'Password…'), '12345678');

      await tester.drag(find.byType(Scaffold), Offset(0, -500));

      await tester.pumpAndSettle();

      await tester.tap(find.byType(ElevatedButton));

      verify(mockNav.pushRoute(any, PandaRouter.loginWeb('hodor.com', loginFlow: LoginFlow.normal)));
      verify(analytics.logEvent(
        AnalyticsEventConstants.QR_ACCOUNT_SUCCESS,
        extras: {AnalyticsParamConstants.DOMAIN_PARAM: 'hodor.com'},
      ));
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('account creation with invalid pairing code shows error', (tester) async {
      final jsonData = json.decode(
          '{\"errors\":{\"user\":{},\"pseudonym\":{},\"observee\":{},\"pairing_code\":{\"code\":[{\"attribute\":\"code\",\"type\":\"invalid\",\"message\":\"invalid\"}]},\"recaptcha\":null}}');
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);
      when(interactor.createNewAccount('123', '123', 'hodor', 'hodor@hodor.com', '12345678', 'hodor.com'))
          .thenThrow(DioError(response: Response(data: jsonData, requestOptions: RequestOptions(path: '')), requestOptions: RequestOptions(path: '')));

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.enterText(find.widgetWithText(TextFormField, 'Full Name…'), 'hodor');
      await tester.enterText(find.widgetWithText(TextFormField, 'Email…'), 'hodor@hodor.com');
      await tester.enterText(find.widgetWithText(TextFormField, 'Password…'), '12345678');

      await tester.drag(find.byType(Scaffold), Offset(0, -500));

      await tester.pumpAndSettle();

      await tester.tap(find.byType(ElevatedButton));

      await tester.pump();
      await tester.pump();
      await tester.pump();

      expect(find.text('Your code is incorrect or expired.', skipOffstage: false), findsOneWidget);
      verify(analytics.logEvent(
        AnalyticsEventConstants.QR_ACCOUNT_FAILURE,
        extras: {AnalyticsParamConstants.DOMAIN_PARAM: 'hodor.com'},
      ));
    }, a11yExclusions: {A11yExclusion.minTapSize});

    testWidgetsWithAccessibilityChecks('account creation with invalid email shows error', (tester) async {
      final jsonData = json.decode(
          '{\"errors\":{\"user\":{\"pseudonyms\":[{\"message\":\"invalid\"}]},\"pseudonym\":{},\"observee\":{},\"pairing_code\":{},\"recaptcha\":null}}');
      when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);
      when(interactor.createNewAccount('123', '123', 'hodor', 'hodor@hodor.com', '12345678', 'hodor.com'))
          .thenThrow(DioError(response: Response(data: jsonData, requestOptions: RequestOptions(path: '')), requestOptions: RequestOptions(path: '')));

      await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
      await tester.pumpAndSettle();

      await tester.enterText(find.widgetWithText(TextFormField, 'Full Name…'), 'hodor');
      await tester.enterText(find.widgetWithText(TextFormField, 'Email…'), 'hodor@hodor.com');
      await tester.enterText(find.widgetWithText(TextFormField, 'Password…'), '12345678');

      await tester.drag(find.byType(Scaffold), Offset(0, -500));

      await tester.pumpAndSettle();

      await tester.tap(find.byType(ElevatedButton));

      await tester.pump();
      await tester.pump();
      await tester.pump();

      expect(find.text('Please enter a valid email address'), findsOneWidget);
      verify(analytics.logEvent(
        AnalyticsEventConstants.QR_ACCOUNT_FAILURE,
        extras: {AnalyticsParamConstants.DOMAIN_PARAM: 'hodor.com'},
      ));
    }, a11yExclusions: {A11yExclusion.minTapSize});
  });

  testWidgetsWithAccessibilityChecks('account creation with error shows generic error message', (tester) async {
    when(interactor.getToSForAccount('123', 'hodor.com')).thenAnswer((_) async => tos);
    when(interactor.createNewAccount('123', '123', 'hodor', 'hodor@hodor.com', '12345678', 'hodor.com'))
        .thenThrow(DioError(response: Response(requestOptions: RequestOptions(path: '')), requestOptions: RequestOptions(path: '')));

    await tester.pumpWidget(TestApp(AccountCreationScreen(pairingInfo)));
    await tester.pumpAndSettle();

    await tester.enterText(find.widgetWithText(TextFormField, 'Full Name…'), 'hodor');
    await tester.enterText(find.widgetWithText(TextFormField, 'Email…'), 'hodor@hodor.com');
    await tester.enterText(find.widgetWithText(TextFormField, 'Password…'), '12345678');

    await tester.drag(find.byType(Scaffold), Offset(0, -500));

    await tester.pumpAndSettle();

    await tester.tap(find.byType(ElevatedButton));

    await tester.pump();
    await tester.pump();
    await tester.pump();

    expect(
        find.text(
            'Something went wrong trying to create your account, please reach out to your school for assistance.'),
        findsOneWidget);
    verify(analytics.logEvent(
      AnalyticsEventConstants.QR_ACCOUNT_FAILURE,
      extras: {AnalyticsParamConstants.DOMAIN_PARAM: 'hodor.com'},
    ));
  }, a11yExclusions: {A11yExclusion.minTapSize});
}
