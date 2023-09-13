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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_percentage_dialog.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  group('Render', () {
    testWidgetsWithAccessibilityChecks('header', (tester) async {
      var widget = TestApp(
        AlertThresholdsPercentageDialog([], AlertType.courseGradeLow, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      expect(find.text(AppLocalizations().courseGradeBelow), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('text input', (tester) async {
      var widget = TestApp(
        AlertThresholdsPercentageDialog([], AlertType.courseGradeLow, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      expect(find.byType(TextFormField), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('action buttons', (tester) async {
      var widget = TestApp(
        AlertThresholdsPercentageDialog([], AlertType.courseGradeLow, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      expect(find.byType(TextButton), findsNWidgets(3));
      expect(find.text(AppLocalizations().cancel.toUpperCase()), findsOneWidget);
      expect(find.text(AppLocalizations().never.toUpperCase()), findsOneWidget);
      expect(find.text(AppLocalizations().ok), findsOneWidget);
    });

    testWidgets('initial value', (tester) async {
      var initialValue = '42';
      var initialThreshold = _mockThreshold(type: AlertType.courseGradeLow, value: initialValue);
      var alertType = AlertType.courseGradeLow;
      var studentId = '';

      // Setup the dialog
      var widget = TestApp(
        AlertThresholdsPercentageDialog([initialThreshold], alertType, studentId),
      );

      // Launch the dialog
      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Check for the initial value
      expect(find.text(initialValue), findsOneWidget);
    });
  });

  group('Validation', () {
    testWidgetsWithAccessibilityChecks('only numbers allowed', (tester) async {
      String alphaText = 'Instructure';
      String numText = '99';

      var widget = TestApp(
        AlertThresholdsPercentageDialog([], AlertType.courseGradeLow, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      await tester.enterText(find.byType(TextFormField), alphaText);
      expect(find.text(alphaText), findsNothing);

      await tester.enterText(find.byType(TextFormField), numText);
      expect(find.text(numText), findsOneWidget);
    });

    testWidgets('error message when setting low >= high', (tester) async {
      String lowInput = '99';
      int highInput = 80;
      AlertThreshold highThreshold = _mockThreshold(type: AlertType.courseGradeHigh, value: highInput.toString());

      var widget = TestApp(
        AlertThresholdsPercentageDialog([highThreshold], AlertType.courseGradeLow, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Enter invalid input
      await tester.enterText(find.byType(TextFormField), lowInput);
      expect(find.text(lowInput), findsOneWidget);
      await tester.pumpAndSettle();

      // Check for error message
      expect(find.text(AppLocalizations().mustBeBelowN(highInput)), findsOneWidget);
    });

    testWidgets('error message when setting high <= low', (tester) async {
      int lowInput = 99;
      String highInput = '80';
      AlertThreshold lowThreshold = _mockThreshold(type: AlertType.courseGradeLow, value: lowInput.toString());

      var widget = TestApp(
        AlertThresholdsPercentageDialog([lowThreshold], AlertType.courseGradeHigh, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Enter invalid input
      await tester.enterText(find.byType(TextFormField), highInput);
      expect(find.text(highInput), findsOneWidget);
      await tester.pumpAndSettle();

      // Check for error message
      expect(find.text(AppLocalizations().mustBeAboveN(lowInput)), findsOneWidget);
    });

    testWidgets('error message when input > 100', (tester) async {
      String input = '999';

      var widget = TestApp(
        AlertThresholdsPercentageDialog([], AlertType.courseGradeLow, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Enter invalid input
      await tester.enterText(find.byType(TextFormField), input);
      expect(find.text(input), findsOneWidget);
      await tester.pumpAndSettle();

      // Check for error message
      expect(find.text(AppLocalizations().mustBeBelow100), findsOneWidget);
    });

    testWidgets('disable ok button when input > 100', (tester) async {
      String input = '999';

      var widget = TestApp(
        AlertThresholdsPercentageDialog([], AlertType.courseGradeLow, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Enter invalid input
      await tester.enterText(find.byType(TextFormField), input);
      expect(find.text(input), findsOneWidget);
      await tester.pumpAndSettle();

      // Check for error message
      expect(tester.widget<TextButton>(find.byKey(AlertThresholdsPercentageDialogState.okButtonKey)).enabled, isFalse);
    });

    testWidgets('disable ok button when setting low >= high', (tester) async {
      String input = '70';

      AlertThreshold highThreshold = _mockThreshold(type: AlertType.courseGradeHigh, value: '50');

      var widget = TestApp(
        AlertThresholdsPercentageDialog([highThreshold], AlertType.courseGradeLow, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Enter invalid input
      await tester.enterText(find.byType(TextFormField), input);
      expect(find.text(input), findsOneWidget);
      await tester.pumpAndSettle();

      // Check for error message
      expect(tester.widget<TextButton>(find.byKey(AlertThresholdsPercentageDialogState.okButtonKey)).enabled, isFalse);
    });

    testWidgets('disable ok button when setting high <= low', (tester) async {
      String input = '40';

      AlertThreshold lowThreshold = _mockThreshold(type: AlertType.courseGradeLow, value: '50');

      var widget = TestApp(
        AlertThresholdsPercentageDialog([lowThreshold], AlertType.courseGradeHigh, ''),
      );

      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Enter invalid input
      await tester.enterText(find.byType(TextFormField), input);
      expect(find.text(input), findsOneWidget);
      await tester.pumpAndSettle();

      // Check for error message
      expect(tester.widget<TextButton>(find.byKey(AlertThresholdsPercentageDialogState.okButtonKey)).enabled, isFalse);
    });
  });

  group('Actions', () {
    // See Alert Thresholds Screen test for testing the result of tapping 'never'
    testWidgets('never - closes dialog, returns threshold with value of -1', (tester) async {
      AlertThreshold initial = _mockThreshold(type: AlertType.courseGradeLow, value: '42');
      AlertThreshold response = initial.rebuild((b) => b.threshold = '-1');
      AlertThreshold? result;

      var interactor = MockAlertThresholdsInteractor();
      when(interactor.updateAlertThreshold(any, any, any, value: anyNamed('value')))
          .thenAnswer((_) => Future.value(response));

      _setupLocator(thresholdsInteractor: interactor);

      var widget = TestApp(Builder(
          builder: (context) => Container(
                child: ElevatedButton(onPressed: () async {
                  result = await showDialog(
                      context: context,
                      builder:(_) => AlertThresholdsPercentageDialog([initial], AlertType.courseGradeLow, ''));
                },
                child: Container(),
              ))));

      // Show the dialog
      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();
      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      // Check to see if our initial value is there
      expect(find.text(initial.threshold!), findsOneWidget);

      // Tap on 'never'
      await tester.tap(find.text(AppLocalizations().never.toUpperCase()));
      await tester.pumpAndSettle();

      // Check if the dialog has been dismissed
      expect(find.byType(AlertThresholdsPercentageDialog), findsNothing);
      expect(result, initial.rebuild((b) => b.threshold = '-1'));
    });

    testWidgets('cancel - dismisses dialog', (tester) async {
      // The dialog won't dismiss if it is the only child in the TestApp widget
      var widget = TestApp(Builder(
          builder: (context) => Container(
                child: ElevatedButton(
                  child: Container(),
                  onPressed: () async {
                    showDialog(
                        context: context, builder:(_) => AlertThresholdsPercentageDialog([], AlertType.courseGradeLow, ''));
                  }),
              )));

      // Show the dialog
      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();
      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      // Tap cancel
      await tester.tap(find.text(AppLocalizations().cancel.toUpperCase()));
      await tester.pumpAndSettle();

      expect(find.byType(AlertThresholdsPercentageDialog), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('OK - calls interactor update method', (tester) async {
      var initialValue = '42';
      var initialThreshold = _mockThreshold(type: AlertType.courseGradeLow, value: initialValue);
      var updatedValue = '24';
      var updatedThreshold = _mockThreshold(type: AlertType.courseGradeLow, value: updatedValue);
      var alertType = AlertType.courseGradeLow;
      var studentId = '23';

      var interactor = MockAlertThresholdsInteractor();
      when(interactor.updateAlertThreshold(any, any, any, value: anyNamed('value')))
          .thenAnswer((_) => Future.value(updatedThreshold));

      _setupLocator(thresholdsInteractor: interactor);

      // Setup the dialog
      var widget = TestApp(
        AlertThresholdsPercentageDialog([initialThreshold], alertType, studentId),
      );

      // Launch the dialog
      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Enter value
      await tester.enterText(find.byType(TextFormField), updatedValue);
      expect(find.text(updatedValue), findsOneWidget);

      // Tap 'OK'
      await tester.tap(find.text(AppLocalizations().ok));
      await tester.pump();
      await tester.pump();

      await tester.pumpAndSettle();

      // Verify we called into the interactor
      verify(interactor.updateAlertThreshold(any, any, any, value: updatedValue)).called(1);
    });

    testWidgets('on submit - network failure shows error message', (tester) async {
      var interactor = MockAlertThresholdsInteractor();
      when(interactor.updateAlertThreshold(any, any, any, value: anyNamed('value')))
          .thenAnswer((_) => Future.error('error'));

      _setupLocator(thresholdsInteractor: interactor);

      var widget = TestApp(
        AlertThresholdsPercentageDialog(
            [_mockThreshold(type: AlertType.courseGradeLow, value: '42')], AlertType.courseGradeLow, ''),
      );

      // Show the dialog
      await tester.pumpWidget(widget);
      await tester.pumpAndSettle();

      // Tap on 'never'
      await tester.tap(find.text(AppLocalizations().never.toUpperCase()));
      await tester.pumpAndSettle();

      // Check for error message
      expect(find.text(AppLocalizations().genericNetworkError), findsOneWidget);
    });
  });
}

void _setupLocator({AlertThresholdsInteractor? thresholdsInteractor}) async {
  var locator = GetIt.instance;
  await locator.reset();

  locator.registerFactory<AlertThresholdsInteractor>(() => thresholdsInteractor ?? MockAlertThresholdsInteractor());
}

AlertThreshold _mockThreshold({AlertType? type, String? value}) => AlertThreshold((b) => b
  ..alertType = type ?? AlertType.courseGradeLow
  ..threshold = value ?? null
  ..build());
