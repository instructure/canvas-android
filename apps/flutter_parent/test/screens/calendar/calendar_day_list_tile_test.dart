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
import 'package:flutter_parent/models/plannable.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/screens/calendar/calendar_day_list_tile.dart';
import 'package:flutter_svg/flutter_svg.dart';
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

//    testWidgetsWithAccessibilityChecks('shows correct context icon', (tester) async {});

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
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: _createPlannable(dueAt: null))),
        highContrast: true,
      ));
      await tester.pump();

      // Shows title, context title, and points possible
      var textFinder = find.ancestor(of: find.byType(Text), matching: find.byType(CalendarDayListTile));
      expect(textFinder, findsNWidgets(3));
    });

    testWidgetsWithAccessibilityChecks('shows points possible', (tester) async {
      var pointsPossible = 10.0;
      await tester.pumpWidget(TestApp(
        CalendarDayListTile(_createPlannerItem(plannable: _createPlannable(pointsPossible: pointsPossible))),
        highContrast: true,
      ));
      await tester.pump();

      expect(find.text(pointsPossible.toString()), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows assignment icon for assignments', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'assignment');
      var assetPath = 'assets/svg/assignment.svg';

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        highContrast: true,
      ));
      await tester.pump();

      var svg = tester.widget<SvgPicture>(find.byType(SvgPicture));

      expect((svg.pictureProvider as ExactAssetPicture).assetName, assetPath);
    });

    testWidgetsWithAccessibilityChecks('shows quiz icon for quizzes', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'quiz');
      var assetPath = 'assets/svg/quiz.svg';

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        highContrast: true,
      ));
      await tester.pump();

      var svg = tester.widget<SvgPicture>(find.byType(SvgPicture));

      expect((svg.pictureProvider as ExactAssetPicture).assetName, assetPath);
    });

    testWidgetsWithAccessibilityChecks('shows announcement icon for announcements', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'announcement');
      var assetPath = 'assets/svg/announcement.svg';

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        highContrast: true,
      ));
      await tester.pump();

      var svg = tester.widget<SvgPicture>(find.byType(SvgPicture));

      expect((svg.pictureProvider as ExactAssetPicture).assetName, assetPath);
    });

    testWidgetsWithAccessibilityChecks('shows calendar event icon for calendar events', (tester) async {
      var plannerItem = _createPlannerItem(plannableType: 'calendar_event');
      var assetPath = 'assets/svg/calendar-event.svg';

      await tester.pumpWidget(TestApp(
        CalendarDayListTile(plannerItem),
        highContrast: true,
      ));
      await tester.pump();

      var svg = tester.widget<SvgPicture>(find.byType(SvgPicture));

      expect((svg.pictureProvider as ExactAssetPicture).assetName, assetPath);
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

PlannerItem _createPlannerItem({String contextName, Plannable plannable, String plannableType}) => PlannerItem((b) => b
  ..courseId = ''
  ..plannable = plannable != null ? plannable.toBuilder() : _createPlannable().toBuilder()
  ..contextType = ''
  ..contextName = contextName ?? ''
  ..plannableType = plannableType ?? 'assignment');
