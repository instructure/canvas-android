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

import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_day.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_day_of_week_headers.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_event_count.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_week.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/test_app.dart';

void main() {
  DateTime weekStart = DateTime(1999, 12, 26); // Dec 26, 1999 - week of 2000 new year
  DateTime selectedDate = weekStart.add(Duration(days: 1)); // Dec 27, 1999

  test('Generates week dates from week start', () {
    final week = CalendarWeek(
      firstDay: weekStart,
      selectedDay: selectedDate,
      eventCount: CalendarEventCount(),
      onDaySelected: (_) {},
      displayDayOfWeekHeader: false,
    );

    expect(week.days.length, 7);
    expect(week.days[0], DateTime(1999, 12, 26));
    expect(week.days[1], DateTime(1999, 12, 27));
    expect(week.days[2], DateTime(1999, 12, 28));
    expect(week.days[3], DateTime(1999, 12, 29));
    expect(week.days[4], DateTime(1999, 12, 30));
    expect(week.days[5], DateTime(1999, 12, 31));
    expect(week.days[6], DateTime(2000, 1, 1));
  });

  testWidgetsWithAccessibilityChecks('Displays all days in week', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWeek(
          firstDay: weekStart,
          selectedDay: selectedDate,
          eventCount: CalendarEventCount(),
          onDaySelected: (_) {},
          displayDayOfWeekHeader: false,
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(CalendarDay), findsNWidgets(7));
    expect(find.text('26'), findsOneWidget);
    expect(find.text('27'), findsOneWidget);
    expect(find.text('28'), findsOneWidget);
    expect(find.text('29'), findsOneWidget);
    expect(find.text('30'), findsOneWidget);
    expect(find.text('31'), findsOneWidget);
    expect(find.text('1'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Invokes onDaySelected callback', (tester) async {
    DateTime selected = null;
    await tester.pumpWidget(
      TestApp(
        CalendarWeek(
          firstDay: weekStart,
          selectedDay: selectedDate,
          eventCount: CalendarEventCount(),
          onDaySelected: (day) {
            selected = day;
          },
          displayDayOfWeekHeader: false,
        ),
      ),
    );
    await tester.pump();

    await tester.tap(find.text('30'));
    await tester.pump();

    expect(selected, DateTime(1999, 12, 30));
  });

  testWidgetsWithAccessibilityChecks('Displays day-of-week headers if specified', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWeek(
          firstDay: weekStart,
          selectedDay: selectedDate,
          eventCount: CalendarEventCount(),
          onDaySelected: (_) {},
          displayDayOfWeekHeader: true,
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(DayOfWeekHeaders), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not display day-of-week headers if not specified', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWeek(
          firstDay: weekStart,
          selectedDay: selectedDate,
          eventCount: CalendarEventCount(),
          onDaySelected: (_) {},
          displayDayOfWeekHeader: false,
        ),
      ),
    );
    await tester.pump();

    expect(find.byType(DayOfWeekHeaders), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Expands CalendarDay widgets across screen', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWeek(
          firstDay: weekStart,
          selectedDay: selectedDate,
          eventCount: CalendarEventCount(),
          onDaySelected: (_) {},
          displayDayOfWeekHeader: false,
        ),
      ),
    );
    await tester.pump();

    final window = tester.binding.window;
    final windowWidth = window.physicalSize.width / window.devicePixelRatio;
    final expectedWidgetWidth = windowWidth / 7;

    for (int i = 0; i < 7; i++) {
      var widgetSize = tester.getSize(find.byType(CalendarDay).at(i));
      expect(widgetSize.width, moreOrLessEquals(expectedWidgetWidth));
    }
  });
}
