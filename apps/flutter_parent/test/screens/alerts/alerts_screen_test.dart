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
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_interactor.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_view_state.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/screens/under_construction_screen.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_view_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/logger.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  _setupLocator({AlertsInteractor interactor, AnnouncementDetailsInteractor announcementDetailsInteractor}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<AlertsInteractor>(() => interactor ?? _MockAlertsInteractor());
    _locator.registerFactory<AnnouncementDetailsInteractor>(
        () => announcementDetailsInteractor ?? _MockAnnouncementDetailsInteractor());
    _locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());
    _locator.registerLazySingleton<AlertCountNotifier>(() => _MockAlertCountNotifier());
    _locator.registerFactory<QuickNav>(() => QuickNav());
    _locator.registerLazySingleton<Logger>(() => Logger());
  }

  AnnouncementDetailsInteractor _setupAnnouncementInteractor() {
    final announcementInteractor = _MockAnnouncementDetailsInteractor();
    final response = AnnouncementViewState('hodorTitle', 'hodor Subject', 'hodor Message', DateTime.now(), null);
    when(announcementInteractor.getAnnouncement(any, any, any, any)).thenAnswer((_) => Future.value(response));
    return announcementInteractor;
  }

  void _pumpAndTapAlert(WidgetTester tester, Alert alert) async {
    final alerts = List.of([alert]);

    final interactor = _MockAlertsInteractor();
    when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
    when(interactor.markAlertRead(alerts.first.id))
        .thenAnswer((_) => Future.value(alerts.first.rebuild((b) => b..workflowState = AlertWorkflowState.read)));
    _setupLocator(interactor: interactor, announcementDetailsInteractor: _setupAnnouncementInteractor());

    await tester.pumpWidget(_testableWidget(highContrastMode: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(alerts.first.title));
    await tester.pumpAndSettle();
  }

  group('Loading', () {
    testWidgetsWithAccessibilityChecks('Shows while waiting for future', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value());

      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pump();

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Does not show once loaded', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value());
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
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => null);
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.byType(SvgPicture), findsOneWidget);
      expect(find.text(AppLocalizations().noAlertsTitle), findsOneWidget);
      expect(find.text(AppLocalizations().noAlertsMessage), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Shows when list is empty', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(List(), null)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.byType(SvgPicture), findsOneWidget);
      expect(find.text(AppLocalizations().noAlertsTitle), findsOneWidget);
      expect(find.text(AppLocalizations().noAlertsMessage), findsOneWidget);
    });
  });

  testWidgetsWithAccessibilityChecks('Shows error', (tester) async {
    final interactor = _MockAlertsInteractor();
    when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.error('ErRoR'));
    _setupLocator(interactor: interactor);

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().unexpectedError), findsOneWidget);
  });

  group('With data', () {
    testWidgetsWithAccessibilityChecks('Can refresh', (tester) async {
      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value());
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

    testWidgetsWithAccessibilityChecks('Shows alert info for institution annoucnements', (tester) async {
      final alerts = _mockData(type: AlertType.institutionAnnouncement);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().institutionAnnouncement);
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style.color, ParentColors.ash);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byIcon(CanvasIcons.info), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.info)) as Icon).color, ParentColors.ash);
    });

    testWidgetsWithAccessibilityChecks('Shows alert info for course annoucnements', (tester) async {
      final alerts = _mockData(type: AlertType.courseAnnouncement);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().courseAnnouncement);
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style.color, ParentColors.ash);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byIcon(CanvasIcons.info), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.info)) as Icon).color, ParentColors.ash);
    });

    testWidgetsWithAccessibilityChecks('Shows alert positive for course grade high', (tester) async {
      final thresholdValue = '80';
      final alerts = _mockData(type: AlertType.courseGradeHigh);
      final thresholds = [
        AlertThreshold((b) => b
          ..alertType = AlertType.courseGradeHigh
          ..threshold = thresholdValue),
      ];

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, thresholds)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().courseGradeAboveThreshold(thresholdValue));
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style.color, StudentColorSet.electric.light);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byIcon(CanvasIcons.info), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.info)) as Icon).color, StudentColorSet.electric.light);
    });

    testWidgetsWithAccessibilityChecks('Shows alert positive for assignment grade high', (tester) async {
      final thresholdValue = '80';
      final alerts = _mockData(type: AlertType.assignmentGradeHigh);
      final thresholds = [
        AlertThreshold((b) => b
          ..alertType = AlertType.assignmentGradeHigh
          ..threshold = thresholdValue),
      ];

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, thresholds)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().assignmentGradeAboveThreshold(thresholdValue));
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style.color, StudentColorSet.electric.light);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byIcon(CanvasIcons.info), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.info)) as Icon).color, StudentColorSet.electric.light);
    });

    testWidgetsWithAccessibilityChecks('Shows alert negative for course grade low', (tester) async {
      final thresholdValue = '10';
      final alerts = _mockData(type: AlertType.courseGradeLow);
      final thresholds = [
        AlertThreshold((b) => b
          ..alertType = AlertType.courseGradeLow
          ..threshold = thresholdValue),
      ];

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, thresholds)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().courseGradeBelowThreshold(thresholdValue));
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style.color, ParentColors.failure);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byIcon(CanvasIcons.warning), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.warning)) as Icon).color, ParentColors.failure);
    });

    testWidgetsWithAccessibilityChecks('Shows alert negative for assignment grade low', (tester) async {
      final thresholdValue = '10';
      final alerts = _mockData(type: AlertType.assignmentGradeLow);
      final thresholds = [
        AlertThreshold((b) => b
          ..alertType = AlertType.assignmentGradeLow
          ..threshold = thresholdValue),
      ];

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, thresholds)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().assignmentGradeBelowThreshold(thresholdValue));
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style.color, ParentColors.failure);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byIcon(CanvasIcons.warning), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.warning)) as Icon).color, ParentColors.failure);
    });

    testWidgetsWithAccessibilityChecks('Shows alert negative for missing assignment', (tester) async {
      final alerts = _mockData(type: AlertType.assignmentMissing);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().assignmentMissing);
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style.color, ParentColors.failure);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byIcon(CanvasIcons.warning), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.warning)) as Icon).color, ParentColors.failure);
    });

    testWidgetsWithAccessibilityChecks('Shows alert badge when unread', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeLow, state: AlertWorkflowState.unread);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.byType(IndicatorBadge), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Can tap alert to mark as read', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeLow, state: AlertWorkflowState.unread);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      when(interactor.markAlertRead(alerts.first.id))
          .thenAnswer((_) => Future.value(alerts.first.rebuild((b) => b..workflowState = AlertWorkflowState.read)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      await tester.tap(find.text(alerts.first.title));
      await tester.pumpAndSettle();

      verify(interactor.markAlertRead(alerts.first.id)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Tapping alert that is read does not call to mark as read', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeLow, state: AlertWorkflowState.read);

      final interactor = _MockAlertsInteractor();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      await tester.tap(find.text(alerts.first.title));
      await tester.pumpAndSettle();

      verifyNever(interactor.markAlertRead(any));
    });

    testWidgetsWithAccessibilityChecks('Can tap course announcement alert to go to announcement', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..htmlUrl = 'https://instructure.com/api/v1/courses/1234/discussion_topics/1234'
        ..alertType = AlertType.courseAnnouncement);

      await _pumpAndTapAlert(tester, alert);

      expect(find.byType(AnnouncementDetailScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Can tap institution announcement alert to go to announcement', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.institutionAnnouncement);
      await _pumpAndTapAlert(tester, alert);

      expect(find.byType(AnnouncementDetailScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment missing alert to show under construction', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.assignmentMissing);
      await _pumpAndTapAlert(tester, alert);

      // TODO: Test that assignment shows
      expect(find.byType(UnderConstructionScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment grade high alert to show under construction',
        (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.assignmentGradeHigh);
      await _pumpAndTapAlert(tester, alert);

      // TODO: Test that assignment shows
      expect(find.byType(UnderConstructionScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment grade low alert to show under construction', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.assignmentGradeLow);
      await _pumpAndTapAlert(tester, alert);

      // TODO: Test that assignment shows
      expect(find.byType(UnderConstructionScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Can tap course grade high alert to show under construction', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.courseGradeHigh);
      await _pumpAndTapAlert(tester, alert);

      // TODO: Test that assignment shows
      expect(find.byType(UnderConstructionScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Can tap course grade low alert to show under construction', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.courseGradeLow);
      await _pumpAndTapAlert(tester, alert);

      // TODO: Test that assignment shows
      expect(find.byType(UnderConstructionScreen), findsOneWidget);
    });
  });
}

Widget _testableWidget({User student, bool highContrastMode = false}) {
  return TestApp(
    ChangeNotifierProvider(
      create: (context) => SelectedStudentNotifier()..update(CanvasModelTestUtils.mockUser(name: 'Trevor')),
      child: Consumer<SelectedStudentNotifier>(builder: (context, model, _) {
        return Scaffold(body: AlertsScreen());
      }),
    ),
    platformConfig: PlatformConfig(initWebview: true),
    highContrast: highContrastMode,
  );
}

List<Alert> _mockData({int size = 1, AlertType type, AlertWorkflowState state = AlertWorkflowState.read}) {
  return List.generate(
      size,
      (index) => Alert((b) => b
        ..id = index.toString()
        ..title = 'Alert $index'
        ..workflowState = state
        ..alertType = type ?? AlertType.institutionAnnouncement));
}

class _MockAlertsInteractor extends Mock implements AlertsInteractor {}

class _MockAnnouncementDetailsInteractor extends Mock implements AnnouncementDetailsInteractor {}

class _MockAlertCountNotifier extends Mock implements AlertCountNotifier {}

class _MockSelectedStudentNotifier extends Mock implements SelectedStudentNotifier {}
