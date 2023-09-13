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

import 'package:flutter/cupertino.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_day.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_day_of_week_headers.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_month.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_week.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/test_app.dart';

void main() {
  DateTime selectedDate = DateTime(2020, 1, 15); // Jan 15 2020

  testWidgetsWithAccessibilityChecks('Displays six weeks for a 6-week month', (tester) async {
    await tester.pumpWidget(
      _appWithFetcher(
        CalendarMonth(
          year: 2020,
          month: 5, // May 2020, which spans six weeks in the en_US locale
          onDaySelected: (_) {},
          selectedDay: selectedDate,
          monthExpansionListener: MonthExpansionNotifier(1.0),
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(CalendarWeek), findsNWidgets(6));
  });

  testWidgetsWithAccessibilityChecks('Displays five weeks for a 5-week month', (tester) async {
    await tester.pumpWidget(
      _appWithFetcher(
        CalendarMonth(
          year: 2020,
          month: 1, // Jan 2020, which spans five weeks in the en_US locale
          onDaySelected: (_) {},
          selectedDay: selectedDate,
          monthExpansionListener: MonthExpansionNotifier(1.0),
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(CalendarWeek), findsNWidgets(5));
  });

  testWidgetsWithAccessibilityChecks('Displays day-of-week headers', (tester) async {
    await tester.pumpWidget(
      _appWithFetcher(
        CalendarMonth(
          year: 2020,
          month: 1,
          onDaySelected: (_) {},
          selectedDay: selectedDate,
          monthExpansionListener: MonthExpansionNotifier(1.0),
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(DayOfWeekHeaders), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays all days in all weeks spanned by the month', (tester) async {
    await tester.pumpWidget(
      _appWithFetcher(
        CalendarMonth(
          year: 2020,
          month: 1,
          onDaySelected: (_) {},
          selectedDay: selectedDate,
          monthExpansionListener: MonthExpansionNotifier(1.0),
        ),
      ),
    );
    await tester.pump();

    /* Displayed days for January 2020:
       29  30  31   1   2   3   4
        5   6   7   8   9  10  11
       12  13  14  15  16  17  18
       19  20  21  22  23  24  25
       26  27  28  29  30  31   1 */

    // Should show 35 days total
    expect(find.byType(CalendarDay), findsNWidgets(35));

    // Should show '1' twice - once for January, once for February
    expect(find.text('1'), findsNWidgets(2));

    // Should show '2' through '28' only once
    for (int i = 2; i <= 28; i++) {
      expect(find.text(i.toString()), findsOneWidget);
    }

    // Should show '29' through '31' twice - once for December, once for January
    for (int i = 29; i <= 31; i++) {
      expect(find.text(i.toString()), findsNWidgets(2));
    }
  });

  testWidgetsWithAccessibilityChecks('Invokes onDaySelected callback', (tester) async {
    DateTime? selected = null;

    await tester.pumpWidget(
      _appWithFetcher(
        CalendarMonth(
          year: 2020,
          month: 1,
          onDaySelected: (day) {
            selected = day;
          },
          selectedDay: selectedDate,
          monthExpansionListener: MonthExpansionNotifier(1.0),
        ),
      ),
    );
    await tester.pump();

    await tester.tap(find.text('13'));
    await tester.pump();

    expect(selected, DateTime(2020, 1, 13));
  });

  testWidgetsWithAccessibilityChecks('Displays only selected week when collapsed', (tester) async {
    await tester.pumpWidget(
      _appWithFetcher(
        CalendarMonth(
          year: 2020,
          month: 1,
          onDaySelected: (_) {},
          selectedDay: selectedDate,
          monthExpansionListener: MonthExpansionNotifier(0.0),
        ),
      ),
    );
    await tester.pump();

    for (int i = 0; i < 5; i++) {
      final week = find.byType(CalendarWeek).at(i);
      Opacity weekOpacity = tester.widget(find.ancestor(of: week, matching: find.byType(Opacity)));
      if (tester.any(find.descendant(of: week, matching: find.text(selectedDate.day.toString())))) {
        // If the week contains the selected day, it should be full opaque
        expect(weekOpacity.opacity, 1.0);
      } else {
        // If the week does not contain the selected day, it should be fully transparent
        expect(weekOpacity.opacity, 0.0);
      }
    }
  });

  testWidgetsWithAccessibilityChecks('Positions weeks according to expansion value', (tester) async {
    final expansionValue = 0.12345;
    await tester.pumpWidget(
      _appWithFetcher(
        CalendarMonth(
          year: 2020,
          month: 1,
          onDaySelected: (_) {},
          selectedDay: selectedDate,
          monthExpansionListener: MonthExpansionNotifier(expansionValue),
        ),
      ),
    );
    await tester.pump();

    for (int i = 0; i < 5; i++) {
      final week = find.byType(CalendarWeek).at(i);
      final position = tester.getTopLeft(week);
      final expectedY = DayOfWeekHeaders.headerHeight + (i * CalendarDay.dayHeight * expansionValue);
      expect(position.dy, moreOrLessEquals(expectedY));
      expect(position.dx, 0.0);
    }
  });

  testWidgetsWithAccessibilityChecks('Updates week positions on expansion value change', (tester) async {
    final expansionNotifier = MonthExpansionNotifier(0.0);
    await tester.pumpWidget(
      _appWithFetcher(
        CalendarMonth(
          year: 2020,
          month: 1,
          onDaySelected: (_) {},
          selectedDay: selectedDate,
          monthExpansionListener: expansionNotifier,
        ),
      ),
    );
    await tester.pump();

    for (int i = 0; i < 5; i++) {
      final week = find.byType(CalendarWeek).at(i);
      final position = tester.getTopLeft(week);
      expect(position.dy, DayOfWeekHeaders.headerHeight);
      expect(position.dx, 0.0);
    }

    final newExpansionValue = 0.5678;
    expansionNotifier.value = newExpansionValue;
    expansionNotifier.notify();
    await tester.pumpAndSettle();

    for (int i = 0; i < 5; i++) {
      final week = find.byType(CalendarWeek).at(i);
      final position = tester.getTopLeft(week);
      final expectedY = DayOfWeekHeaders.headerHeight + (i * CalendarDay.dayHeight * newExpansionValue);
      expect(position.dy, moreOrLessEquals(expectedY));
      expect(position.dx, 0.0);
    }
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
