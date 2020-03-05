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
import 'package:flutter_parent/models/plannable.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/planner_submission.dart';
import 'package:flutter_parent/screens/calendar/calendar_day_list_tile.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows title', (tester) async {
      var title = 'The Title';
      var plannable = _createPlannable(title: title);

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: plannable)),
        highContrast: true,
      ));
      await tester.pump();

      expect(find.text(title), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows context name', (tester) async {
      var contextName = 'Instructure';

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(contextName: contextName)),
        highContrast: true,
      ));
      await tester.pump();

      expect(find.text(contextName), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows due date', (tester) async {
      var dueDate = 'Due Apr 8 at 11:59 AM';
      var date = DateTime.parse('2020-04-08 11:59:00');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: _createPlannable(dueAt: date))),
        highContrast: true,
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
        highContrast: true,
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
        highContrast: true,
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentTotalPoints('10')), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not show points possible when no status and points possible is null',
        (tester) async {
      var pointsPossible = null;
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: _createPlannable(pointsPossible: pointsPossible))),
        highContrast: true,
      ));
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
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus)),
        highContrast: true,
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().excused), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'missing\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.missing = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus)),
        highContrast: true,
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().missing), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'graded\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.graded = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus)),
        highContrast: true,
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentGradedLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows \'submitted\' status', (tester) async {
      var submissionStatus = PlannerSubmission((b) => b.needsGrading = true);
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(submission: submissionStatus)),
        highContrast: true,
      ));
      await tester.pump();

      expect(find.text(AppLocalizations().assignmentSubmittedLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows assignment icon for assignments', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'assignment');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        highContrast: true,
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.assignment);
    });

    testWidgetsWithAccessibilityChecks('shows quiz icon for quizzes', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'quiz');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        highContrast: true,
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.quiz);
    });

    testWidgetsWithAccessibilityChecks('shows announcement icon for announcements', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'announcement');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        highContrast: true,
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.announcement);
    });

    testWidgetsWithAccessibilityChecks('shows calendar event icon for calendar events', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'calendar_event');

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        highContrast: true,
      ));
      await tester.pump();

      var icon = tester.widget<Icon>(find.byType(Icon));

      expect(icon.icon, CanvasIcons.calendar_day);
    });
  });

  group('Interaction', () {
    // TODO: Finish when routing is in place
//    testWidgetsWithAccessibilityChecks('tapping assignment plannable navigates to assignment details page',
//        (tester) async {
//      var plannerItem = _createPlannerItem(plannableType: 'assignment', contextName: 'Tap me');
//
//      await tester.pumpWidget(TestApp(CalendarDayListTile(plannerItem)));
//      await tester.pump();
//
//      await tester.tap(find.text('Tap me'));
//      await tester.pump();
//
//      expect(find.byType(AssignmentDetailsScreen), findsOneWidget);
//    });
  });
}

Plannable _createPlannable({String title, DateTime dueAt, double pointsPossible}) => Plannable((b) => b
  ..id = ''
  ..title = title ?? ''
  ..pointsPossible = pointsPossible
  ..dueAt = dueAt);

PlannerItem _createPlannerItem(
        {String contextName, Plannable plannable, String plannableType, PlannerSubmission submission}) =>
    PlannerItem((b) => b
      ..courseId = ''
      ..plannable = plannable != null ? plannable.toBuilder() : _createPlannable().toBuilder()
      ..contextType = ''
      ..contextName = contextName ?? ''
      ..plannableType = plannableType ?? 'assignment'
      ..submissionStatus = submission != null ? submission.toBuilder() : null);
