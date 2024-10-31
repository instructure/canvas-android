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
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_interactor.dart';
import 'package:flutter_parent/screens/announcements/announcement_view_state.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

final _studentId = '123';
void main() {
  final String domain = 'https://test.instructure.com';

  final interactor = MockAlertsInteractor();
  final announcementInteractor = MockAnnouncementDetailsInteractor();
  final alertNotifier = MockAlertCountNotifier();
  final mockNav = MockQuickNav();

  setupTestLocator((locator) {
    locator.registerFactory<AlertsInteractor>(() => interactor);
    locator.registerFactory<AnnouncementDetailsInteractor>(() => announcementInteractor);
    locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());
    locator.registerLazySingleton<AlertCountNotifier>(() => alertNotifier);
    locator.registerFactory<QuickNav>(() => mockNav);
  });

  setUp(() {
    reset(interactor);
    reset(announcementInteractor);
    reset(alertNotifier);
    reset(mockNav);
  });

  Future<void> _pumpAndTapAlert(WidgetTester tester, Alert alert) async {
    final alerts = List.of([alert]);

    when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
    when(interactor.markAlertRead(_studentId, alerts.first.id))
        .thenAnswer((_) => Future.value(alerts.first.rebuild((b) => b..workflowState = AlertWorkflowState.read)));

    final response = AnnouncementViewState('hodorTitle', 'hodor Subject', 'hodor Message', DateTime.now(), null);
    when(announcementInteractor.getAnnouncement(any, any, any, any, any)).thenAnswer((_) => Future.value(response));

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    await tester.tap(find.text(alert.title));
    await tester.pumpAndSettle();
  }

  group('Loading', () {
    testWidgetsWithAccessibilityChecks('Shows while waiting for future', (tester) async {
      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(null));

      await tester.pumpWidget(_testableWidget());
      await tester.pump();

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Does not show once loaded', (tester) async {
      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(null));

      await tester.pumpWidget(_testableWidget());
      await tester.pump();
      await tester.pump(); // One extra frame to finish loading

      expect(find.byType(CircularProgressIndicator), findsNothing);
    });
  });

  group('Empty message', () {
    testWidgetsWithAccessibilityChecks('Shows when response is null', (tester) async {
      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(null));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.byType(SvgPicture), findsOneWidget);
      expect(find.text(AppLocalizations().noAlertsTitle), findsOneWidget);
      expect(find.text(AppLocalizations().noAlertsMessage), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Shows when list is empty', (tester) async {
      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList([], null)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.byType(SvgPicture), findsOneWidget);
      expect(find.text(AppLocalizations().noAlertsTitle), findsOneWidget);
      expect(find.text(AppLocalizations().noAlertsMessage), findsOneWidget);
    });
  });

  testWidgetsWithAccessibilityChecks('Shows error', (tester) async {
    when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.error('ErRoR'));

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().unexpectedError), findsOneWidget);
  });

  group('With data', () {
    // TODO Fix test - Tested manually, and passed
    testWidgetsWithAccessibilityChecks('Can refresh', (tester) async {
      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value());

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final matchedWidget = find.byType(RefreshIndicator);
      expect(matchedWidget, findsOneWidget);

      await tester.drag(matchedWidget, const Offset(0, 200));

      expect(find.byType(CircularProgressIndicator), findsOneWidget);

      await tester.pumpAndSettle();
      expect(find.byType(RefreshIndicator), findsOneWidget);
    }, skip: true);

    testWidgetsWithAccessibilityChecks('refreshes when student changes', (tester) async {
      final notifier = SelectedStudentNotifier();
      when(interactor.getAlertsForStudent(any, any)).thenAnswer((_) => Future.value(null));

      await tester.pumpWidget(_testableWidget(notifier: notifier));
      await tester.pumpAndSettle();

      verify(interactor.getAlertsForStudent(_studentId, any)).called(1);

      final newStudentId = _studentId + 'new';
      final newStudent = notifier.value?.rebuild((b) => b..id = newStudentId);
      notifier.update(newStudent!);
      await tester.pump();

      verify(interactor.getAlertsForStudent(newStudentId, true)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Shows alert info for institution annoucnements', (tester) async {
      final alerts = _mockData(type: AlertType.institutionAnnouncement);

      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().globalAnnouncement);
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style!.color, ParentColors.ash);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
      expect(find.byIcon(CanvasIcons.info), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.info)) as Icon).color, ParentColors.ash);
    });

    testWidgetsWithAccessibilityChecks('Shows alert info for course annoucnements', (tester) async {
      final alerts = _mockData(type: AlertType.courseAnnouncement);

      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().courseAnnouncement);
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style!.color, ParentColors.ash);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
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

      when(interactor.getAlertsForStudent(_studentId, any))
          .thenAnswer((_) => Future.value(AlertsList(alerts, thresholds)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().courseGradeAboveThreshold(thresholdValue));
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style!.color, StudentColorSet.electric.light);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
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

      when(interactor.getAlertsForStudent(_studentId, any))
          .thenAnswer((_) => Future.value(AlertsList(alerts, thresholds)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().assignmentGradeAboveThreshold(thresholdValue));
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style!.color, StudentColorSet.electric.light);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
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

      when(interactor.getAlertsForStudent(_studentId, any))
          .thenAnswer((_) => Future.value(AlertsList(alerts, thresholds)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().courseGradeBelowThreshold(thresholdValue));
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style!.color, ParentColors.failure);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
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

      when(interactor.getAlertsForStudent(_studentId, any))
          .thenAnswer((_) => Future.value(AlertsList(alerts, thresholds)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().assignmentGradeBelowThreshold(thresholdValue));
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style!.color, ParentColors.failure);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
      expect(find.byIcon(CanvasIcons.warning), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.warning)) as Icon).color, ParentColors.failure);
    });

    testWidgetsWithAccessibilityChecks('Shows alert negative for missing assignment', (tester) async {
      final alerts = _mockData(type: AlertType.assignmentMissing);

      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final title = find.text(AppLocalizations().assignmentMissing);
      expect(title, findsOneWidget);
      expect((tester.widget(title) as Text).style!.color, ParentColors.failure);
      expect(find.text(alerts.first.title), findsOneWidget);
      expect(find.text(alerts.first.actionDate.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
      expect(find.byIcon(CanvasIcons.warning), findsOneWidget);
      expect((tester.widget(find.byIcon(CanvasIcons.warning)) as Icon).color, ParentColors.failure);
    });

    testWidgetsWithAccessibilityChecks('Shows alert badge when unread', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeLow, state: AlertWorkflowState.unread);

      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      expect(find.byType(IndicatorBadge), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Can tap alert to mark as read', (tester) async {
      final alerts =
          _mockData(type: AlertType.courseGradeLow, state: AlertWorkflowState.unread, htmlUrl: '$domain/courses/12345');

      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      when(interactor.markAlertRead(_studentId, alerts.first.id))
          .thenAnswer((_) => Future.value(alerts.first.rebuild((b) => b..workflowState = AlertWorkflowState.read)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      await tester.tap(find.text(alerts.first.title));
      await tester.pumpAndSettle();

      verify(interactor.markAlertRead(_studentId, alerts.first.id)).called(1);
      verify(alertNotifier.update(_studentId)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Tapping alert that is read does not call to mark as read', (tester) async {
      final alerts =
          _mockData(type: AlertType.courseGradeLow, state: AlertWorkflowState.read, htmlUrl: '$domain/courses/12345');

      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      await tester.tap(find.text(alerts.first.title));
      await tester.pumpAndSettle();

      verifyNever(interactor.markAlertRead(any, any));
      verifyNever(alertNotifier.update(any));
    });

    testWidgetsWithAccessibilityChecks('Can dismiss an unread alert', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeLow, state: AlertWorkflowState.unread);

      final alert = alerts.first;
      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      when(interactor.markAlertDismissed(_studentId, alert.id)).thenAnswer((_) => Future.value(null));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final icon = find.byIcon(Icons.clear);
      expect(icon, findsOneWidget);
      await tester.tap(icon);
      await tester.pumpAndSettle();

      expect(find.text(alert.title), findsNothing);
      expect(find.byType(EmptyPandaWidget), findsOneWidget);
      verify(interactor.markAlertDismissed(_studentId, alert.id)).called(1);
      verify(alertNotifier.update(_studentId)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Can dismiss a read alert', (tester) async {
      final alerts = _mockData(size: 2, type: AlertType.courseGradeLow, state: AlertWorkflowState.read);

      final alert = alerts.first;
      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));
      when(interactor.markAlertDismissed(_studentId, alert.id)).thenAnswer((_) => Future.value(null));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final icon = find.byIcon(Icons.clear).first;
      expect(icon, findsOneWidget);
      await tester.tap(icon);
      await tester.pumpAndSettle();

      expect(find.text(alert.title), findsNothing);
      expect(find.byType(EmptyPandaWidget), findsNothing);
      verify(interactor.markAlertDismissed(_studentId, alert.id)).called(1);
      verifyNever(alertNotifier.update(any));
    });

    testWidgetsWithAccessibilityChecks('Can tap course announcement alert to go to announcement', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..htmlUrl = '$domain/courses/1234/discussion_topics/1234'
        ..alertType = AlertType.courseAnnouncement
        ..lockedForUser = false);

      await _pumpAndTapAlert(tester, alert);

      verify(mockNav.routeInternally(any, alert.htmlUrl));
    });

    testWidgetsWithAccessibilityChecks('Can tap institution announcement alert to go to announcement', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..contextId = '12345'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.institutionAnnouncement
        ..lockedForUser = false);
      await _pumpAndTapAlert(tester, alert);

      verify(mockNav.pushRoute(any, PandaRouter.institutionAnnouncementDetails(alert.contextId)));
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment missing alert to show assignment details', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.assignmentMissing
        ..htmlUrl = '$domain/courses/1234/assignments/1234'
        ..lockedForUser = false);
      await _pumpAndTapAlert(tester, alert);

      verify(mockNav.routeInternally(any, alert.htmlUrl));
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment grade high alert to show assignment details',
        (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.assignmentGradeHigh
        ..htmlUrl = '$domain/courses/1234/assignments/1234'
        ..lockedForUser = false);
      await _pumpAndTapAlert(tester, alert);

      verify(mockNav.routeInternally(any, alert.htmlUrl));
    });

    testWidgetsWithAccessibilityChecks('Can tap assignment grade low alert to show assignment details', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.assignmentGradeLow
        ..htmlUrl = '$domain/courses/1234/assignments/1234'
        ..lockedForUser = false);
      await _pumpAndTapAlert(tester, alert);

      verify(mockNav.routeInternally(any, alert.htmlUrl));
    });

    testWidgetsWithAccessibilityChecks('Can tap course grade high alert to show course details', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.courseGradeHigh
        ..htmlUrl = '$domain/courses/1234'
        ..lockedForUser = false);
      await _pumpAndTapAlert(tester, alert);

      verify(mockNav.routeInternally(any, alert.htmlUrl));
    });

    testWidgetsWithAccessibilityChecks('Can tap course grade low alert to show course details', (tester) async {
      final alert = Alert((b) => b
        ..id = '123'
        ..title = 'Hodor'
        ..workflowState = AlertWorkflowState.unread
        ..alertType = AlertType.courseGradeLow
        ..htmlUrl = '$domain/courses/1234'
        ..lockedForUser = false);
      await _pumpAndTapAlert(tester, alert);

      verify(mockNav.routeInternally(any, alert.htmlUrl));
    });
  });

  group('Accessibility', () {
    testWidgetsWithAccessibilityChecks('Dismiss button semantic label includes alert title', (tester) async {
      final alerts = _mockData(type: AlertType.courseGradeLow, state: AlertWorkflowState.unread);

      final alert = alerts.first;
      when(interactor.getAlertsForStudent(_studentId, any)).thenAnswer((_) => Future.value(AlertsList(alerts, null)));

      await tester.pumpWidget(_testableWidget());
      await tester.pumpAndSettle();

      final semantics = find.byTooltip(AppLocalizations().dismissAlertLabel(alert.title));
      final icon = find.byIcon(Icons.clear);

      expect(find.descendant(of: semantics, matching: icon), findsOneWidget);
    });
  });
}

Widget _testableWidget({SelectedStudentNotifier? notifier}) {
  notifier = notifier ?? SelectedStudentNotifier();
  return TestApp(
    ChangeNotifierProvider(
      create: (context) => notifier?..value = CanvasModelTestUtils.mockUser(id: _studentId, name: 'Trevor'),
      child: Consumer<SelectedStudentNotifier>(builder: (context, model, _) {
        return Scaffold(body: AlertsScreen());
      }),
    ),
    platformConfig: PlatformConfig(initWebview: true),
  );
}

List<Alert> _mockData(
    {int size = 1, AlertType? type, AlertWorkflowState state = AlertWorkflowState.read, String htmlUrl = ''}) {
  return List.generate(
      size,
      (index) => Alert((b) => b
        ..id = index.toString()
        ..title = 'Alert $index'
        ..workflowState = state
        ..alertType = type ?? AlertType.institutionAnnouncement
        ..htmlUrl = htmlUrl
        ..lockedForUser = false));
}