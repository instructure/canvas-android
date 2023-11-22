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

import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/plannable.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/planner_submission.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_day_list_tile.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/events/event_details_screen.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final studentId = '1337';
  final studentName = 'Instructure Panda';

  final student = User((b) => b
    ..id = studentId
    ..name = studentName);

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows title', (tester) async {
      var title = 'The Title';
      var plannable = _createPlannable(title: title);

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: plannable)),
      ));
      await tester.pump();

      expect(find.text(title), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows context name', (tester) async {
      var contextName = 'Instructure';

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(contextName: contextName)),
      ));
      await tester.pump();

      expect(find.text(contextName), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows due date', (tester) async {
      var dueDate = 'Due Apr 8 at 11:59 AM';
      var date = DateTime.parse('2020-04-08 11:59:00');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: _createPlannable(dueAt: date))),
      ));
      await tester.pump();

      expect(find.text(dueDate), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not show due date if there is none', (tester) async {
      var status = PlannerSubmission();
      var pointsPossible = 10.0;
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(
            plannable: _createPlannable(dueAt: null, pointsPossible: pointsPossible), submission: status)),
      ));
      await tester.pump();

      // Shows title, context title, and points possible
      var textFinder = find.ancestor(of: find.byType(Text), matching: find.byType(CalendarDayListTile));
      expect(textFinder, findsNWidgets(3));
    });

    testWidgetsWithAccessibilityChecks('shows points possible on no submission status and points possible is not null',
        (tester) async {
      var pointsPossible = 10.0;
      var submissionStatus = PlannerSubmission();
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(
            plannable: _createPlannable(pointsPossible: pointsPossible), submission: submissionStatus)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentTotalPoints('10')), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not show points possible when no status and points possible is null',
        (tester) async {
      var pointsPossible = null;
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(
            _createPlannerItem(contextName: 'blank', plannable: _createPlannable(pointsPossible: pointsPossible))),
      ));
      await tester.pump();

      // Check for a text widget that has the 'pts' substring
      expect(find.byWidgetPredicate((widget) {
        if (widget is Text) {
          final Text textWidget = widget;
          if (textWidget.data != null) return textWidget.data!.contains('pts');
          return textWidget.textSpan!.toPlainText().contains('pts');
        }
        return false;
      }), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('shows \'excused\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.excused = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().excused), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'missing\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.missing = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().missing), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'graded\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.graded = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentGradedLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'submitted\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.needsGrading = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentSubmittedLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows assignment icon for assignments', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'assignment');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.assignment);
    });

    testWidgetsWithAccessibilityChecks('shows quiz icon for quizzes', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'quiz');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.quiz);
    });

    testWidgetsWithAccessibilityChecks('shows calendar event icon for calendar events', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'calendar_event');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.calendar_day);
    });

    /*
    testWidgetsWithAccessibilityChecks('shows announcement icon for announcements', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'announcement');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.announcement);
    });

    testWidgetsWithAccessibilityChecks('shows "Planner Note" as context name for planner notes', (tester) async {
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(contextName: null, plannableType: 'planner_note')),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().plannerNote), findsOneWidget);
    });
     */
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('tapping assignment plannable navigates to assignment details screen',
        (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'assignment', contextName: 'Tap me');

      setupTestLocator((locator) => locator
        ..registerFactory<QuickNav>(() => QuickNav())
        ..registerFactory<AssignmentDetailsInteractor>(() => MockAssignmentDetailsInteractor()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      expect(find.byType(AssignmentDetailsScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping quiz assignment plannable navigates to assignment details screen',
        (tester) async {
      var plannerItem = _createPlannerItem(
        plannableType: 'quiz',
        contextName: 'Tap me',
        plannable: _createPlannable(assignmentId: '123'),
      );

      setupTestLocator((locator) => locator
        ..registerFactory<QuickNav>(() => QuickNav())
        ..registerFactory<AssignmentDetailsInteractor>(() => MockAssignmentDetailsInteractor()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      expect(find.byType(AssignmentDetailsScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping calendar event plannable navigates to event details screen',
        (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'calendar_event', contextName: 'Tap me');

      setupTestLocator((locator) => locator
        ..registerFactory<QuickNav>(() => QuickNav())
        ..registerFactory<EventDetailsInteractor>(() => MockEventDetailsInteractor()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      expect(find.byType(EventDetailsScreen), findsOneWidget);
    });

    /*
    testWidgetsWithAccessibilityChecks('tapping announcement plannable navigates to announcement details screen',
        (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'announcement', contextName: 'Tap me');

      setupTestLocator((locator) => locator
        ..registerFactory<QuickNav>(() => QuickNav())
        ..registerFactory<AnnouncementDetailsInteractor>(() => _MockAnnouncementDetailsInteractor()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      expect(find.byType(AnnouncementDetailScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping quiz plannable launches mobile browser', (tester) async {
      var login = Login((b) => b
        ..domain = 'https://test.instructure.com'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      final url = 'https://test.instructure.com/courses/1/quizzes/1';
      var plannerItem = _createPlannerItem(
        plannableType: 'quiz',
        contextName: 'Tap me',
        htmlUrl: '/courses/1/quizzes/1',
      );

      final _mockLauncher = _MockUrlLauncher();
      final _mockWebContentInteractor = _MockWebContentInteractor();
      final _analytics = _MockAnalytics();

      when(_mockLauncher.canLaunch(url)).thenAnswer((_) => Future.value(true));
      when(_mockLauncher.launch(url)).thenAnswer((_) => Future.value(true));
      when(_mockWebContentInteractor.getAuthUrl(url)).thenAnswer((_) => Future.value(url));

      setupTestLocator((locator) => locator
        ..registerFactory<QuickNav>(() => QuickNav())
        ..registerLazySingleton<Analytics>(() => _analytics)
        ..registerLazySingleton<UrlLauncher>(() => _mockLauncher)
        ..registerLazySingleton<WebContentInteractor>(() => _mockWebContentInteractor));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        platformConfig: PlatformConfig(
          initLoggedInUser: login,
          mockApiPrefs: {
            ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student)),
          },
        ),
      ));
      await tester.pump();

      // Tap the quiz widget
      await tester.tap(find.text('Tap me'));
      await tester.pump();

      verify(_analytics.logMessage('Attempting to route INTERNAL url: $url')).called(1);
      verify(_mockLauncher.launch(url)).called(1);
    });

    testWidgetsWithAccessibilityChecks('tapping discussion plannable navigates to course announcement details screen',
        (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'discussion_topic', contextName: 'Tap me');

      setupTestLocator((locator) => locator
        ..registerFactory<QuickNav>(() => QuickNav())
        ..registerFactory<AnnouncementDetailsInteractor>(() => _MockAnnouncementDetailsInteractor()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      expect(find.byType(AnnouncementDetailScreen), findsOneWidget);
    });
     */
  });
}

Plannable _createPlannable({String? title, DateTime? dueAt, double? pointsPossible, String? assignmentId}) =>
    Plannable((b) => b
      ..id = ''
      ..title = title ?? ''
      ..pointsPossible = pointsPossible
      ..dueAt = dueAt
      ..assignmentId = assignmentId ?? '');

PlannerItem _createPlannerItem(
        {String? contextName,
        Plannable? plannable,
        String? plannableType,
        PlannerSubmission? submission,
        String? htmlUrl}) =>
    PlannerItem((b) => b
      ..courseId = ''
      ..plannable = plannable != null ? plannable.toBuilder() : _createPlannable().toBuilder()
      ..contextType = ''
      ..contextName = contextName
      ..plannableType = plannableType ?? 'assignment'
      ..plannableDate = DateTime.now()
      ..htmlUrl = htmlUrl ?? ''
      ..submissionStatus = submission != null ? submission.toBuilder() : null
      ..submissionStatusRaw = submission != null ? JsonObject(serialize(submission)) : null);

/*
class _MockAnnouncementDetailsInteractor extends Mock implements AnnouncementDetailsInteractor {}
class _MockUrlLauncher extends Mock implements UrlLauncher {}
class _MockWebContentInteractor extends Mock implements WebContentInteractor {}
class _MockAnalytics extends Mock implements Analytics {}
 */
