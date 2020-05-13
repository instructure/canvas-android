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

import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/login.dart';
import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/models/planner_submission.dart';
import 'package:flutter_student_embed/models/serializers.dart';
import 'package:flutter_student_embed/models/user.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_day_list_tile.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_screen.dart';
import 'package:flutter_student_embed/screens/to_do/to_do_details_screen.dart';
import 'package:flutter_student_embed/utils/design/canvas_icons.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/canvas_model_utils.dart';
import '../../testutils/mock_helpers.dart';
import '../../testutils/platform_config.dart';
import '../../testutils/test_app.dart';

void main() {
  final studentId = '1337';
  final studentName = 'Instructure Panda';
  MockCalendarScreenChannel mockChannel;

  final student = User((b) => b
    ..id = studentId
    ..name = studentName);

  setUp(() {
    mockChannel = MockCalendarScreenChannel();
  });

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows title', (tester) async {
      var title = 'The Title';
      var plannable = _createPlannable(title: title);

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: plannable), _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      expect(find.text(title), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows context name', (tester) async {
      var contextName = 'Instructure';

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(contextName: contextName), _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      expect(find.text(contextName), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows "Planner Note" as context name for planner notes', (tester) async {
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(contextName: null, plannableType: 'planner_note'),
            _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().toDo), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows due date', (tester) async {
      var dueDate = 'Due Apr 8 at 11:59 AM';
      var date = DateTime.parse('2020-04-08 11:59:00');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(
            _createPlannerItem(plannable: _createPlannable(dueAt: date)), _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      expect(find.text(dueDate), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not show due date if there is none', (tester) async {
      var status = PlannerSubmission();
      var pointsPossible = 10.0;
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(
            _createPlannerItem(
                plannable: _createPlannable(dueAt: null, pointsPossible: pointsPossible), submission: status),
            _getItemClickHandler(tester, mockChannel)),
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
      await tester.pumpWidget(TestApp(CalendarDayListTile(
          _createPlannerItem(plannable: _createPlannable(pointsPossible: pointsPossible), submission: submissionStatus),
          _getItemClickHandler(tester, mockChannel))));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentTotalPoints('10')), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not show points possible when no status and points possible is null',
        (tester) async {
      var pointsPossible = null;
      await tester.pumpWidget(TestApp(CalendarDayListTile(
          _createPlannerItem(contextName: 'blank', plannable: _createPlannable(pointsPossible: pointsPossible)),
          _getItemClickHandler(tester, mockChannel))));
      await tester.pump();

      // Check for a text widget that has the 'pts' substring
      expect(find.byWidgetPredicate((widget) {
        if (widget is Text) {
          final Text textWidget = widget;
          if (textWidget.data != null) return textWidget.data.contains('pts');
          return textWidget.textSpan.toPlainText().contains('pts');
        }
        return false;
      }), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('shows \'excused\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.excused = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(
            _createPlannerItem(submission: submissionStatus), _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().excused), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'missing\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.missing = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(
            _createPlannerItem(submission: submissionStatus), _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().missing), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'graded\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.graded = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(
            _createPlannerItem(submission: submissionStatus), _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentGradedLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'submitted\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.needsGrading = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(
            _createPlannerItem(submission: submissionStatus), _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentSubmittedLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows assignment icon for assignments', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'assignment');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.assignment);
    });

    testWidgetsWithAccessibilityChecks('shows quiz icon for quizzes', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'quiz');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.quiz);
    });

    testWidgetsWithAccessibilityChecks('shows announcement icon for announcements', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'announcement');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.announcement);
    });

    testWidgetsWithAccessibilityChecks('shows calendar event icon for calendar events', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'calendar_event');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.calendar_day);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('tapping assignment plannable emits correct channel call', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'assignment', contextName: 'Tap me');

      setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
        platformConfig: PlatformConfig(),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      verify(mockChannel.nativeRouteToItem(plannerItem)).called(1);
    });

    testWidgetsWithAccessibilityChecks('tapping announcement plannable emits correct channel call', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'announcement', contextName: 'Tap me');

      setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
        platformConfig: PlatformConfig(),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      verify(mockChannel.nativeRouteToItem(plannerItem)).called(1);
    });

    testWidgetsWithAccessibilityChecks('tapping quiz assignment plannable emits correct channel call', (tester) async {
      var plannerItem = _createPlannerItem(
        plannableType: 'quiz',
        contextName: 'Tap me',
        plannable: _createPlannable(assignmentId: '123'),
      );

      setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
        platformConfig: PlatformConfig(),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      verify(mockChannel.nativeRouteToItem(plannerItem)).called(1);
    });

    testWidgetsWithAccessibilityChecks('tapping quiz plannable emits correct channel call', (tester) async {
      var login = Login((b) => b
        ..domain = 'https://test.instructure.com'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      var plannerItem = _createPlannerItem(
        plannableType: 'quiz',
        contextName: 'Tap me',
        htmlUrl: '/courses/1/quizzes/1',
      );

      setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
        platformConfig: PlatformConfig(
          initLoggedInUser: login,
        ),
      ));
      await tester.pump();

      // Tap the quiz widget
      await tester.tap(find.text('Tap me'));
      await tester.pump();

      verify(mockChannel.nativeRouteToItem(plannerItem)).called(1);
    });

    testWidgetsWithAccessibilityChecks('tapping discussion plannable emits correct channel call', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'discussion_topic', contextName: 'Tap me');

      setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
        platformConfig: PlatformConfig(),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      verify(mockChannel.nativeRouteToItem(plannerItem)).called(1);
    });

    testWidgetsWithAccessibilityChecks('tapping calendar event plannable navigates to ToDo details screen',
        (tester) async {
      var plannerItem = _createPlannerItem(
          plannableType: 'planner_note', contextName: 'Tap me', plannable: _createPlannable(dueAt: DateTime.now()));

      setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, _getItemClickHandler(tester, mockChannel)),
        platformConfig: PlatformConfig(),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me To Do')); // 'Tap me' won't do the trick
      await tester.pump();
      await tester.pump();

      expect(find.byType(ToDoDetailsScreen), findsOneWidget);
    });
  });
}

Plannable _createPlannable({String title, DateTime dueAt, double pointsPossible, String assignmentId}) =>
    Plannable((b) => b
      ..id = ''
      ..title = title ?? ''
      ..pointsPossible = pointsPossible
      ..dueAt = dueAt
      ..toDoDate = dueAt // JEH
      ..assignmentId = assignmentId);

PlannerItem _createPlannerItem(
        {String contextName,
        Plannable plannable,
        String plannableType,
        PlannerSubmission submission,
        String htmlUrl}) =>
    PlannerItem((b) => b
      ..courseId = ''
      ..plannable = plannable != null ? plannable.toBuilder() : _createPlannable().toBuilder()
      ..contextType = ''
      ..contextName = contextName
      ..plannableType = plannableType ?? 'assignment'
      ..plannableDate = DateTime.now().toUtc()
      ..htmlUrl = htmlUrl ?? ''
      ..submissionStatusRaw = submission != null ? JsonObject(serialize(submission)) : null);

// Logic borrowed from the onItemSelected definition set forth in CalendarScreenState.
Function(PlannerItem) _getItemClickHandler(WidgetTester tester, CalendarScreenChannel channel) {
  return (PlannerItem item) async {
    var widget = tester.element(find.byType(CalendarDayListTile)).widget as CalendarDayListTile;
    var context = widget.global_key_for_testing.currentContext;
    if (item.plannableType == 'planner_note') {
      // Display planner to-do details in flutter, refreshing changed dates if necessary
      var updatedDates = await locator<QuickNav>().push(context, ToDoDetailsScreen(item));
      //_refreshDates(updatedDates); // Not necessary for tests
    } else {
      channel.nativeRouteToItem(item);
    }
  };
}
