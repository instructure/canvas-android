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
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_day.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/test_app.dart';

void main() {
  DateTime dayDate = DateTime(2000); // Jan 1 2000
  DateTime selectedDate = dayDate.add(Duration(days: 1)); // Jan 2 2000

  testWidgetsWithAccessibilityChecks('Displays day of month', (tester) async {
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: dayDate,
      selectedDay: selectedDate,
      onDaySelected: (_) {},
    )));
    await tester.pumpAndSettle();

    expect(find.text('1'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Sets a11y description', (tester) async {
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: dayDate,
      selectedDay: selectedDate,
      onDaySelected: (_) {},
    )));
    await tester.pump();

    expect(find.bySemanticsLabel('Saturday, January 1, 0 events'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Uses dark text color for week days', (tester) async {
    final date = DateTime(2000, 1, 3); // Jan 3 2000, a Monday
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: date, // Jan 3
      selectedDay: selectedDate,
      onDaySelected: (_) {},
    )));
    await tester.pump();

    final textStyle = tester.widget<AnimatedDefaultTextStyle>(find.byType(AnimatedDefaultTextStyle).last).style;
    expect(textStyle.color, ParentColors.licorice);
  });

  testWidgetsWithAccessibilityChecks('Uses faded text color for weekends', (tester) async {
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: dayDate, // Jan 1 2000, a Saturday
      selectedDay: selectedDate,
      onDaySelected: (_) {},
    )));
    await tester.pump();

    final textStyle = tester.widget<AnimatedDefaultTextStyle>(find.byType(AnimatedDefaultTextStyle).last).style;
    expect(textStyle.color, ParentColors.ash);
  });

  testWidgetsWithAccessibilityChecks('Uses faded text color for week days if month is not selected', (tester) async {
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: DateTime(1999, 12, 31), // Dec 31 1999, a Friday
      selectedDay: selectedDate, // Jan 2 2000
      onDaySelected: (_) {},
    )));
    await tester.pump();

    final textStyle = tester.widget<AnimatedDefaultTextStyle>(find.byType(AnimatedDefaultTextStyle).last).style;
    expect(textStyle.color, ParentColors.ash);
  });

  testWidgetsWithAccessibilityChecks('Uses no decoration if day is not selected and is not today', (tester) async {
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: dayDate,
      selectedDay: selectedDate,
      onDaySelected: (_) {},
    )));
    await tester.pump();

    final container = find.ancestor(of: find.text('1'), matching: find.byType(Container)).first;
    expect(tester.widget<Container>(container).decoration, isNull);
  });

  testWidgetsWithAccessibilityChecks('Uses accent text color and accent border for selected day', (tester) async {
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: selectedDate,
      selectedDay: selectedDate,
      onDaySelected: (_) {},
    )));
    await tester.pump();

    final theme = Theme.of(tester.element(find.byType(CalendarDay)));
    final textStyle = tester.widget<AnimatedDefaultTextStyle>(find.byType(AnimatedDefaultTextStyle).last).style;
    expect(textStyle.color, theme.colorScheme.secondary);

    final container = find.ancestor(of: find.text('2'), matching: find.byType(Container)).first;
    final decoration = tester.widget<Container>(container).decoration as BoxDecoration;
    expect(decoration.borderRadius, BorderRadius.circular(16));
    expect(decoration.border, Border.all(color: theme.colorScheme.secondary, width: 2));
  });

  testWidgetsWithAccessibilityChecks('Uses white text color and accent background for today', (tester) async {
    final date = DateTime.now();
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: date,
      selectedDay: selectedDate,
      onDaySelected: (_) {},
    )));
    await tester.pump();

    final theme = Theme.of(tester.element(find.byType(CalendarDay)));
    final textStyle = tester.widget<AnimatedDefaultTextStyle>(find.byType(AnimatedDefaultTextStyle).last).style;
    expect(textStyle.color, Colors.white);

    final container = find.ancestor(of: find.text(date.day.toString()), matching: find.byType(Container)).first;
    final decoration = tester.widget<Container>(container).decoration;
    expect(decoration, BoxDecoration(color: theme.colorScheme.secondary, shape: BoxShape.circle));
  });

  testWidgetsWithAccessibilityChecks('Displays activity dots', (tester) async {
    final fetcher = _FakeFetcher(
      observeeId: '',
      userDomain: '',
      userId: '',
    );
    fetcher.nextSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, [
      _createPlannerItem(contextName: 'blank'),
      _createPlannerItem(contextName: 'blank'),
      _createPlannerItem(contextName: 'blank'),
    ]);
    await tester.pumpWidget(
      _appWithFetcher(
        CalendarDay(
          date: dayDate,
          selectedDay: selectedDate,
          onDaySelected: (_) {},
        ),
        fetcher: fetcher,
      ),
    );
    await tester.pump();

    // Should show three dots
    var activityDots = find.descendant(of: find.byType(Row), matching: find.byType(Container)).evaluate();
    expect(activityDots.length, 3);

    // Reset count and rebuild
    fetcher.nextSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);
    fetcher.reset();
    await tester.pumpAndSettle();

    // Should not show any dots
    activityDots = find.descendant(of: find.byType(Row), matching: find.byType(Container)).evaluate();
    expect(activityDots.length, 0);
  });

  testWidgetsWithAccessibilityChecks('Invokes onDaySelectedCallback', (tester) async {
    DateTime? selection = null;
    await tester.pumpWidget(_appWithFetcher(CalendarDay(
      date: dayDate,
      selectedDay: selectedDate,
      onDaySelected: (day) {
        selection = day;
      },
    )));
    await tester.pump();

    await tester.tap(find.byType(CalendarDay));
    await tester.pump();

    expect(selection, dayDate);
  });
}

Widget _appWithFetcher(Widget child, {PlannerFetcher? fetcher}) {
  return TestApp(
    ChangeNotifierProvider<PlannerFetcher>(
      create: (BuildContext context) => fetcher ?? _FakeFetcher(
        observeeId: '',
        userDomain: '',
        userId: '',
      ),
      child: child,
    ),
  );
}

class _FakeFetcher extends PlannerFetcher {
  AsyncSnapshot<List<PlannerItem>> nextSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);

  _FakeFetcher({required super.observeeId, required super.userDomain, required super.userId});

  @override
  AsyncSnapshot<List<PlannerItem>> getSnapshotForDate(DateTime date) => nextSnapshot;
}

Plannable _createPlannable() => Plannable((b) => b
  ..id = ''
  ..title = '');

PlannerItem _createPlannerItem({String? contextName}) => PlannerItem((b) => b
  ..courseId = ''
  ..plannable = _createPlannable().toBuilder()
  ..contextType = ''
  ..plannableDate = DateTime.now()
  ..contextName = contextName ?? ''
  ..htmlUrl = ''
  ..plannableType = 'assignment');
