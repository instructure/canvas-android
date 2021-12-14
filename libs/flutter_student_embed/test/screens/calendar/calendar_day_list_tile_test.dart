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
import 'package:flutter_student_embed/utils/design/canvas_icons.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/canvas_model_utils.dart';
import '../../testutils/platform_config.dart';
import '../../testutils/test_app.dart';

void main() {
  final studentId = '1337';
  final studentName = 'Instructure Panda';
  _FakePlannerItemClickCallback itemClickCallHandler;

  final student = User((b) => b
    ..id = studentId
    ..name = studentName);

  setUp(() {
    itemClickCallHandler = _FakePlannerItemClickCallback();
  });

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows title', (tester) async {
      var title = 'The Title';
      var plannable = _createPlannable(title: title);

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: plannable), itemClickCallHandler),
      ));
      await tester.pump();

      expect(find.text(title), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows context name', (tester) async {
      var contextName = 'Instructure';

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(contextName: contextName), itemClickCallHandler),
      ));
      await tester.pump();

      expect(find.text(contextName), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows "Planner Note" as context name for planner notes', (tester) async {
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(contextName: null, plannableType: 'planner_note'), itemClickCallHandler),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().toDo), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows due date', (tester) async {
      var dueDate = 'Due Apr 8 at 11:59 AM';
      var date = DateTime.parse('2020-04-08 11:59:00');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: _createPlannable(dueAt: date)), itemClickCallHandler),
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
            itemClickCallHandler),
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
          itemClickCallHandler)));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentTotalPoints('10')), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not show points possible when no status and points possible is null',
        (tester) async {
      var pointsPossible = null;
      await tester.pumpWidget(TestApp(CalendarDayListTile(
          _createPlannerItem(contextName: 'blank', plannable: _createPlannable(pointsPossible: pointsPossible)),
          itemClickCallHandler)));
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
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus), itemClickCallHandler),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().excused), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'missing\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.missing = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus), itemClickCallHandler),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().missing), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'graded\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.graded = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus), itemClickCallHandler),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentGradedLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'submitted\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.needsGrading = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus), itemClickCallHandler),
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentSubmittedLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows assignment icon for assignments', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'assignment');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, itemClickCallHandler),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.assignment);
    });

    testWidgetsWithAccessibilityChecks('shows quiz icon for quizzes', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'quiz');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, itemClickCallHandler),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.quiz);
    });

    testWidgetsWithAccessibilityChecks('shows calendar event icon for calendar events', (tester) async {
      var plannerItem = _createPlannerItem(contextName: 'blank', plannableType: 'calendar_event');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, itemClickCallHandler),
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.calendar_day);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('tapping assignment plannable emits correct channel call', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'assignment', contextName: 'Tap me');

      await setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, itemClickCallHandler),
        platformConfig: PlatformConfig(),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      expect(itemClickCallHandler.callCount, 1, reason: "Expected item-click callback to be called once");
      expect(itemClickCallHandler.plannerItem, plannerItem, reason: "planner items don't match");
    });

    testWidgetsWithAccessibilityChecks('tapping quiz assignment plannable emits correct channel call', (tester) async {
      var plannerItem = _createPlannerItem(
        plannableType: 'quiz',
        contextName: 'Tap me',
        plannable: _createPlannable(assignmentId: '123'),
      );

      await setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, itemClickCallHandler),
        platformConfig: PlatformConfig(),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      expect(itemClickCallHandler.callCount, 1, reason: "Expected item-click callback to be called once");
      expect(itemClickCallHandler.plannerItem, plannerItem, reason: "planner items don't match");
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

      await setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, itemClickCallHandler),
        platformConfig: PlatformConfig(
          initLoggedInUser: login,
        ),
      ));
      await tester.pump();

      // Tap the quiz widget
      await tester.tap(find.text('Tap me'));
      await tester.pump();

      expect(itemClickCallHandler.callCount, 1, reason: "Expected item-click callback to be called once");
      expect(itemClickCallHandler.plannerItem, plannerItem, reason: "planner items don't match");
    });

    testWidgetsWithAccessibilityChecks('tapping discussion plannable emits correct channel call', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'discussion_topic', contextName: 'Tap me');

      await setupTestLocator((locator) => locator..registerFactory<QuickNav>(() => QuickNav()));

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem, itemClickCallHandler),
        platformConfig: PlatformConfig(),
      ));
      await tester.pump();

      await tester.tap(find.text('Tap me'));
      await tester.pump();
      await tester.pump();

      expect(itemClickCallHandler.callCount, 1, reason: "Expected item-click callback to be called once");
      expect(itemClickCallHandler.plannerItem, plannerItem, reason: "planner items don't match");
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

class _FakePlannerItemClickCallback {
  int callCount = 0;
  PlannerItem plannerItem;
  call(PlannerItem item) {
    callCount += 1;
    plannerItem = item;
  }
}
