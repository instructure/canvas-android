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
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:intl/intl.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  _setupLocator({AlertsInteractor interactor}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<AlertsInteractor>(() => interactor ?? _MockAlertsInteractor());
  }

  void _pumpAndTapAlert(WidgetTester tester, AlertType type) async {
    final alerts = _mockData(type: type);

    final interactor = _MockAlertsInteractor();
    when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value(alerts));
    when(interactor.markAlertRead(alerts.first.id))
        .thenAnswer((_) => Future.value(alerts.first.rebuild((b) => b..workflowState = AlertWorkflowState.read)));
    _setupLocator(interactor: interactor);

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    await tester.tap(find.text(alerts.first.title));
    await tester.pumpAndSettle();
  }

  group('Loading', () {
    testWidgetsWithAccessibilityChecks('Shows while waiting for future', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value());
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pump();

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Does not show once loaded', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value());
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pump();
      await tester.pump(); // One extra frame to finish loading

      expect(find.byType(CircularProgressIndicator), findsNothing);
    });
  });

  group('Empty message', () {
    testWidgetsWithAccessibilityChecks('Shows when response is null', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => null);
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.text(AppLocalizations().noAlertsMessage), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Shows when list is empty', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value(List()));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.text(AppLocalizations().noAlertsMessage), findsOneWidget);
    });
  });

  testWidgetsWithAccessibilityChecks('Shows error', (tester) async {
    final interactor = _MockAlertsInteractor();
    when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.error("ErRoR"));
    _setupLocator(interactor: interactor);

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().unexpectedError), findsOneWidget);
  });

  group('With data', () {
    testWidgetsWithAccessibilityChecks('Can refresh', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value());
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final matchedWidget = find.byType(RefreshIndicator);
      expect(matchedWidget, findsOneWidget);

      await tester.drag(matchedWidget, const Offset(0, 200));
      await tester.pump();

      expect(find.byType(CircularProgressIndicator), findsOneWidget);

      await tester.pumpAndSettle();
      expect(find.byType(RefreshIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Shows alert info', (tester) async {
      final alerts = _mockData(type: AlertType.institutionAnnouncement);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value(alerts));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(DateFormat(AppLocalizations().dateTimeFormat).format(alerts.first.actionDate.toLocal())),
          findsOneWidget);
      expect(find.byIcon(CanvasIcons.info), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.info)) as Icon).color, ParentColors.ash);
    });

    testWidgetsWithAccessibilityChecks('Shows alert positive', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeHigh);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value(alerts));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(DateFormat(AppLocalizations().dateTimeFormat).format(alerts.first.actionDate.toLocal())),
          findsOneWidget);
      expect(find.byIcon(CanvasIcons.info), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.info)) as Icon).color, StudentColorSet.electric.light);
    });

    testWidgetsWithAccessibilityChecks('Shows alert negative', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeLow);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value(alerts));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(DateFormat(AppLocalizations().dateTimeFormat).format(alerts.first.actionDate.toLocal())),
          findsOneWidget);
      expect(find.byIcon(CanvasIcons.warning), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.warning)) as Icon).color, ParentColors.failure);
    });

    testWidgetsWithAccessibilityChecks('Can tap alert to mark as read', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeLow);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any)).thenAnswer((_) => Future.value(alerts));
      when(interactor.markAlertRead(alerts.first.id))
          .thenAnswer((_) => Future.value(alerts.first.rebuild((b) => b..workflowState = AlertWorkflowState.read)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      await tester.tap(find.text(alerts.first.title));
      await tester.pumpAndSettle();

      verify(interactor.markAlertRead(alerts.first.id)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Can tap course announcement alert to go to announcement', (tester) async {
      await _pumpAndTapAlert(tester, AlertType.courseAnnouncement);

      // TODO: Test that course announcement shows
    });

    testWidgetsWithAccessibilityChecks('Can tap institution announcement alert to go to announcement', (tester) async {
      await _pumpAndTapAlert(tester, AlertType.institutionAnnouncement);

      // TODO: Test that institution announcement shows
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment missing alert to go to announcement', (tester) async {
      await _pumpAndTapAlert(tester, AlertType.assignmentMissing);

      // TODO: Test that assignment shows
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment grade high alert to go to announcement', (tester) async {
      await _pumpAndTapAlert(tester, AlertType.assignmentGradeHigh);

      // TODO: Test that assignment shows
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment grade low alert to go to announcement', (tester) async {
      await _pumpAndTapAlert(tester, AlertType.assignmentGradeLow);

      // TODO: Test that assignment shows
    });
  });
}

Widget _testableWidget({User student}) {
  return TestApp(Scaffold(body: AlertsScreen(student ?? _mockUser())));
}

User _mockUser({String id = ''}) {
  return User((b) => b..id = id);
}

List<Alert> _mockData({int size = 1, AlertType type}) {
  return List.generate(
      size,
      (index) => Alert((b) => b
        ..id = index.toString()
        ..title = 'Alert $index'
        ..alertType = type ?? AlertType.institutionAnnouncement));
}

class _MockAlertsInteractor extends Mock implements AlertsInteractor {}
