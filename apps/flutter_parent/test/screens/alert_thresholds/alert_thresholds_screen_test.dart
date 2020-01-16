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
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_percentage_dialog.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:intl/intl.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/network_image_response.dart';
import '../../utils/test_app.dart';

void main() {
  // For user images
  mockNetworkImageResponse();

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows student', (tester) async {
      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([]));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();

      expect(find.text('Panda'), findsOneWidget);
      expect(find.byType(Avatar), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'alert me\' header', (tester) async {
      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([]));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();

      expect(find.text(AppLocalizations().alertMeWhen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows loading', (tester) async {
      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([]));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows error', (tester) async {
      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.error('Error'));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();

      expect(find.byType(ErrorPandaWidget), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows correct thresholds and their types', (tester) async {
      _setupBasicInteractor();

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();
      await tester.pump();

      expect(_percentageThresholdFinder(AppLocalizations().courseGradeBelow), findsOneWidget);
      expect(_percentageThresholdFinder(AppLocalizations().courseGradeAbove), findsOneWidget);
      expect(_switchThresholdFinder(AppLocalizations().assignmentMissing), findsOneWidget);
      expect(_percentageThresholdFinder(AppLocalizations().assignmentGradeBelow), findsOneWidget);
      expect(_percentageThresholdFinder(AppLocalizations().assignmentGradeAbove), findsOneWidget);
      expect(_switchThresholdFinder(AppLocalizations().courseAnnouncements), findsOneWidget);
      expect(_switchThresholdFinder(AppLocalizations().institutionAnnouncements), findsOneWidget);
    });
  });

  group('Loading', () {
    testWidgetsWithAccessibilityChecks('student thresholds', (tester) async {
      String id = '1234';

      var interactor = _setupBasicInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([]));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(id: id, name: 'Panda'));
      await tester.pump();

      verify(interactor.getAlertThresholdsForStudent(id)).called(1);
    });

    testWidgetsWithAccessibilityChecks('percent - non-null', (tester) async {
      String threshold = '23';
      AlertType type = AlertType.courseGradeLow;

      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any))
          .thenAnswer((_) => Future.value([_mockThreshold(type: type, value: threshold)]));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();
      await tester.pump();

      expect(_percentageThresholdFinder(AppLocalizations().courseGradeBelow, value: threshold), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('percent - null', (tester) async {
      AlertType type = AlertType.courseGradeLow;

      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([_mockThreshold(type: type)]));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();
      await tester.pump();

      expect(_percentageThresholdFinder(AppLocalizations().courseGradeBelow, value: null), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('switch - non-null', (tester) async {
      AlertType type = AlertType.assignmentMissing;

      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([_mockThreshold(type: type)]));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();
      await tester.pump();

      expect(_switchThresholdFinder(AppLocalizations().assignmentMissing, switchedOn: true), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('switch - null', (tester) async {
      _setupBasicInteractor();

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();
      await tester.pump();

      expect(_switchThresholdFinder(AppLocalizations().assignmentMissing, switchedOn: false), findsOneWidget);
    });
  });

  group('Interactions', () {
    testWidgetsWithAccessibilityChecks('percent - tap shows dialog', (tester) async {
      _setupBasicInteractor();

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();
      await tester.pump();

      // Tap on a percent threshold
      await tester.tap(find.text(AppLocalizations().courseGradeBelow));
      await tester.pump();

      expect(find.byType(AlertThresholdsPercentageDialog), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('switch - tap changes value', (tester) async {
      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([]));
      when(interactor.updateAlertThreshold(any, any, any))
          .thenAnswer((_) => Future.value(_mockThreshold(type: AlertType.assignmentMissing)));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();
      await tester.pump();

      // Switch is off initially
      expect(_switchThresholdFinder(AppLocalizations().assignmentMissing, switchedOn: false), findsOneWidget);

      // Tap on a percent threshold
      await tester.tap(find.text(AppLocalizations().assignmentMissing));
      await tester.pumpAndSettle();

      // Make sure switched is on
      expect(_switchThresholdFinder(AppLocalizations().assignmentMissing, switchedOn: true), findsOneWidget);
    });
  });

  group('Interaction Results', () {
    testWidgetsWithAccessibilityChecks('percent threshold changed from dialog', (tester) async {
      var initialValue = '42';
      var updatedValue = '24';
      var initialThreshold = _mockThreshold(value: initialValue);

      var interactor = MockAlertThresholdsInteractor();
      when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([initialThreshold]));
      when(interactor.updateAlertThreshold(any, any, any, value: updatedValue))
          .thenAnswer((_) => Future.value(initialThreshold.rebuild((b) => b.threshold = updatedValue)));
      _setupLocator(interactor);

      await _setupScreen(tester, CanvasModelTestUtils.mockUser(name: 'Panda'));
      await tester.pumpAndSettle();
      await tester.pump();

      // Check to make sure we have the initial value
      expect(find.text(NumberFormat.percentPattern().format(int.tryParse(initialValue) / 100)), findsOneWidget);

      // Tap on a percent threshold
      await tester.tap(find.text(AppLocalizations().courseGradeBelow));
      await tester.pump();

      // Check for dialog
      expect(find.byType(AlertThresholdsPercentageDialog), findsOneWidget);

      // Enter text in the dialog
      await tester.enterText(find.byType(TextFormField), updatedValue);

      // Submit text
      await tester.tap(find.text(AppLocalizations().ok));
      await tester.pumpAndSettle();

      // Check for the update
      expect(find.text(NumberFormat.percentPattern().format(int.tryParse(updatedValue) / 100)), findsOneWidget);
    });
  });
}

Finder _percentageThresholdFinder(String title, {String value}) => find.byWidgetPredicate((widget) {
      return widget is ListTile &&
              widget.title is Text &&
              (widget.title as Text).data == title &&
              widget.trailing is Text &&
              (widget.trailing as Text).data ==
                  (value != null
                      ? NumberFormat.percentPattern().format(int.tryParse(value) / 100)
                      : AppLocalizations().never)
          ? true
          : false;
    });

Finder _switchThresholdFinder(String title, {bool switchedOn}) => find.byWidgetPredicate((widget) {
      return widget is SwitchListTile &&
              widget.title is Text &&
              (widget.title as Text).data == title &&
              (widget.value == (switchedOn != null ? switchedOn : widget.value))
          ? true
          : false;
    });

void _setupScreen(WidgetTester tester, [User student]) async {
  var user = student ?? CanvasModelTestUtils.mockUser();
  var screen = TestApp(
    AlertThresholdsScreen(user),
    highContrast: true,
    darkMode: true,
  );

  await tester.pumpWidget(screen);
}

AlertThreshold _mockThreshold({AlertType type, String value}) => AlertThreshold((b) => b
  ..alertType = type ?? AlertType.courseGradeLow
  ..threshold = value ?? null
  ..build());

void _setupLocator([AlertThresholdsInteractor interactor]) {
  final _locator = GetIt.instance;
  _locator.reset();

  _locator.registerFactory<AlertThresholdsInteractor>(() => interactor ?? MockAlertThresholdsInteractor());
}

AlertThresholdsInteractor _setupBasicInteractor() {
  var interactor = MockAlertThresholdsInteractor();
  when(interactor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([]));
  _setupLocator(interactor);
  return interactor;
}

class MockAlertThresholdsInteractor extends Mock implements AlertThresholdsInteractor {}
