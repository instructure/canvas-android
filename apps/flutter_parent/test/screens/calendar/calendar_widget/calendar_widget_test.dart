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
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_event_count.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_month.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_week.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/test_app.dart';

void main() {
  Future<CalendarWidgetState> goToDate(WidgetTester tester, DateTime date) async {
    CalendarWidgetState state = tester.state(find.byType(CalendarWidget));
    state.selectDay(
      date,
      dayPagerBehavior: CalendarPageChangeBehavior.jump,
      weekPagerBehavior: CalendarPageChangeBehavior.jump,
      monthPagerBehavior: CalendarPageChangeBehavior.jump,
    );
    await tester.pumpAndSettle();
    return state;
  }

  testWidgetsWithAccessibilityChecks('Displays week view by default', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, __) => Container(),
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    expect(find.byType(CalendarWeek), findsOneWidget);
    expect(find.byType(CalendarMonth), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Expand button expands and collapses month', (tester) async {
    final calendar = CalendarWidget(
      dayBuilder: (_, __) => Container(),
      eventCount: CalendarEventCount(),
    );
    await tester.pumpWidget(
      TestApp(
        calendar,
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    // Tap expand button
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Should show month w/ multiple weeks
    expect(find.byType(CalendarMonth), findsOneWidget);
    expect(find.byType(CalendarWeek).evaluate().length, greaterThan(1));

    // Tap expand button again
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Should should one week view and no month
    expect(find.byType(CalendarWeek), findsOneWidget);
    expect(find.byType(CalendarMonth), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Can drag to expand and collapse month', (tester) async {
    final calendar = CalendarWidget(
      dayBuilder: (_, __) => Container(),
      eventCount: CalendarEventCount(),
    );
    await tester.pumpWidget(
      TestApp(
        calendar,
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    // Drag down on week to expand
    await tester.drag(find.byType(CalendarWeek), Offset(0, 400));
    await tester.pumpAndSettle();

    // Should show month w/ multiple weeks
    expect(find.byType(CalendarMonth), findsOneWidget);
    expect(find.byType(CalendarWeek).evaluate().length, greaterThan(1));

    // Drag up on month to collapse
    await tester.drag(find.byType(CalendarMonth), Offset(0, -400));
    await tester.pumpAndSettle();

    // Should should one week view and no month
    expect(find.byType(CalendarWeek), findsOneWidget);
    expect(find.byType(CalendarMonth), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Can fling to expand and collapse month', (tester) async {
    final calendar = CalendarWidget(
      dayBuilder: (_, __) => Container(),
      eventCount: CalendarEventCount(),
    );
    await tester.pumpWidget(
      TestApp(
        calendar,
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    // Fling down on week to expand
    await tester.fling(find.byType(CalendarWeek), Offset(0, 50), 300);
    await tester.pumpAndSettle();

    // Should show month w/ multiple weeks
    expect(find.byType(CalendarMonth), findsOneWidget);
    expect(find.byType(CalendarWeek).evaluate().length, greaterThan(1));

    // Fling up on month to collapse
    await tester.fling(find.byType(CalendarMonth), Offset(0, -50), 300);
    await tester.pumpAndSettle();

    // Should should one week view and no month
    expect(find.byType(CalendarWeek), findsOneWidget);
    expect(find.byType(CalendarMonth), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Can fling day content to collapse month', (tester) async {
    final dayContentKey = Key('day-content');
    final calendar = CalendarWidget(
      dayBuilder: (_, __) => Container(key: dayContentKey),
      eventCount: CalendarEventCount(),
    );
    await tester.pumpWidget(
      TestApp(
        calendar,
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    // Tap expand button
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Should show month now
    expect(find.byType(CalendarMonth), findsOneWidget);

    // Fling up on day content to collapse
    await tester.fling(find.byKey(dayContentKey), Offset(0, -50), 300);
    await tester.pumpAndSettle();

    // Should should one week view and no month
    expect(find.byType(CalendarWeek), findsOneWidget);
    expect(find.byType(CalendarMonth), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Jumps to selected date', (tester) async {
    DateTime dateForDayBuilder = null;
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) {
            dateForDayBuilder = day;
            return Container();
          },
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    await goToDate(tester, targetDate);

    // Day should have built using target date
    expect(dateForDayBuilder, targetDate);

    // Week should start on Dec 26, 1999
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(1999, 12, 26));

    // Month should be Jan 2000
    final monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth, skipOffstage: false));
    expect(monthWidget.year, 2000);
    expect(monthWidget.month, 1);
  });

  testWidgetsWithAccessibilityChecks('Animates to selected date', (tester) async {
    DateTime dateForDayBuilder = null;
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) {
            dateForDayBuilder = day;
            return Container();
          },
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    CalendarWidgetState state = await goToDate(tester, DateTime(2000, 1, 1));

    DateTime targetDate = DateTime(1999, 12, 23);
    state.selectDay(
      targetDate,
      dayPagerBehavior: CalendarPageChangeBehavior.animate,
      weekPagerBehavior: CalendarPageChangeBehavior.animate,
      monthPagerBehavior: CalendarPageChangeBehavior.animate,
    );
    await tester.pumpAndSettle();

    // Day should have built using target date
    expect(dateForDayBuilder, targetDate);

    // Week should start on Dec 19, 1999
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(1999, 12, 19));

    // Month should be Dec 1999
    final monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth, skipOffstage: false));
    expect(monthWidget.year, 1999);
    expect(monthWidget.month, 12);
  });

  testWidgetsWithAccessibilityChecks('Swipes to previous week', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) => Container(),
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    CalendarWidgetState state = await goToDate(tester, targetDate);

    // Week should start on Dec 26, 1999
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(1999, 12, 26));

    // Selected day should be Saturday (Jan 1 2000)
    expect(state.selectedDay, targetDate);

    // Fling to previous week
    await tester.fling(find.byType(CalendarWeek), Offset(50, 0), 300);
    await tester.pumpAndSettle();

    // Week should now start on Dec 19, 1999
    weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(1999, 12, 19));

    // Selected day should be the same day of the week, Saturday (Dec 25, 1999)
    expect(state.selectedDay, DateTime(1999, 12, 25));
  });

  testWidgetsWithAccessibilityChecks('Swipes to next week', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) => Container(),
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    CalendarWidgetState state = await goToDate(tester, targetDate);

    // Week should start on Dec 26, 1999
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(1999, 12, 26));

    // Selected day should be Saturday (Jan 1 2000)
    expect(state.selectedDay, targetDate);

    // Fling to previous month
    await tester.fling(find.byType(CalendarWeek), Offset(-50, 0), 300);
    await tester.pumpAndSettle();

    // Week should now start on Jan 2, 2000
    weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(2000, 1, 2));

    // Selected day should be the same day of the week, Saturday (Jan 8, 2000)
    expect(state.selectedDay, DateTime(2000, 1, 8));
  });

  testWidgetsWithAccessibilityChecks('Swipes to previous month', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) => Container(),
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    CalendarWidgetState state = await goToDate(tester, targetDate);

    // Tap expand button
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Month should be January 2000
    CalendarMonth monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
    expect(monthWidget.year, 2000);
    expect(monthWidget.month, 1);

    // Selected day should be Jan 1 2000
    expect(state.selectedDay, targetDate);

    // Fling to previous month
    await tester.fling(find.byType(CalendarMonth), Offset(50, 0), 300);
    await tester.pumpAndSettle();

    // Month should be December 1999
    monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
    expect(monthWidget.year, 1999);
    expect(monthWidget.month, 12);

    // Selected day should be Same day of the month (Dec 1, 1999)
    expect(state.selectedDay, DateTime(1999, 12, 1));
  });

  testWidgetsWithAccessibilityChecks('Swipes to next month', (tester) async {
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) => Container(),
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    CalendarWidgetState state = await goToDate(tester, targetDate);

    // Tap expand button
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Month should be January 2000
    CalendarMonth monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
    expect(monthWidget.year, 2000);
    expect(monthWidget.month, 1);

    // Selected day should be Jan 1 2000
    expect(state.selectedDay, targetDate);

    // Fling to next month
    await tester.fling(find.byType(CalendarMonth), Offset(-50, 0), 300);
    await tester.pumpAndSettle();

    // Month should be February 2000
    monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
    expect(monthWidget.year, 2000);
    expect(monthWidget.month, 2);

    // Selected day should be same day of the month (Feb 1, 2000)
    expect(state.selectedDay, DateTime(2000, 2, 1));
  });

  testWidgetsWithAccessibilityChecks('Displays a11y arrows for week view', (tester) async {
    await tester.pumpWidget(
      TestApp(
        MediaQuery(
          child: CalendarWidget(
            dayBuilder: (_, __) => Container(),
            eventCount: CalendarEventCount(),
          ),
          data: MediaQueryData(accessibleNavigation: true),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    // Jump to Jan 1 2000
    await goToDate(tester, DateTime(2000, 1, 1));

    // Should show week a11y arrows
    expect(find.byKey(Key('calendar-a11y-previous-week')), findsOneWidget);
    expect(find.byKey(Key('calendar-a11y-next-week')), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('A11y arrow moves to previous week', (tester) async {
    await tester.pumpWidget(
      TestApp(
        MediaQuery(
          child: CalendarWidget(
            dayBuilder: (_, __) => Container(),
            eventCount: CalendarEventCount(),
          ),
          data: MediaQueryData(accessibleNavigation: true),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    CalendarWidgetState state = await goToDate(tester, targetDate);

    // Tap previous arrow
    await tester.tap(find.byKey(Key('calendar-a11y-previous-week')));
    await tester.pumpAndSettle();

    // Week should start on Dec 19, 1999
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(1999, 12, 19));

    // Selected day should be the same day of the week, Saturday (Dec 25, 1999)
    expect(state.selectedDay, DateTime(1999, 12, 25));
  });

  testWidgetsWithAccessibilityChecks('A11y arrow moves to next week', (tester) async {
    await tester.pumpWidget(
      TestApp(
        MediaQuery(
          child: CalendarWidget(
            dayBuilder: (_, __) => Container(),
            eventCount: CalendarEventCount(),
          ),
          data: MediaQueryData(accessibleNavigation: true),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    CalendarWidgetState state = await goToDate(tester, targetDate);

    // Tap next arrow
    await tester.tap(find.byKey(Key('calendar-a11y-next-week')));
    await tester.pumpAndSettle();

    // Week should now start on Jan 2, 2000
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(2000, 1, 2));

    // Selected day should be the same day of the week, Saturday (Jan 8, 2000)
    expect(state.selectedDay, DateTime(2000, 1, 8));
  });

  testWidgetsWithAccessibilityChecks('Displays a11y arrows for month view', (tester) async {
    await tester.pumpWidget(
      TestApp(
        MediaQuery(
          child: CalendarWidget(
            dayBuilder: (_, __) => Container(),
            eventCount: CalendarEventCount(),
          ),
          data: MediaQueryData(accessibleNavigation: true),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    // Jump to Jan 1 2000
    await goToDate(tester, DateTime(2000, 1, 1));

    // Tap to expand month view
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Should show week a11y arrows
    expect(find.byKey(Key('calendar-a11y-previous-month')), findsOneWidget);
    expect(find.byKey(Key('calendar-a11y-next-month')), findsOneWidget);
  });
  testWidgetsWithAccessibilityChecks('A11y arrow moves to previous month', (tester) async {
    await tester.pumpWidget(
      TestApp(
        MediaQuery(
          child: CalendarWidget(
            dayBuilder: (_, __) => Container(),
            eventCount: CalendarEventCount(),
          ),
          data: MediaQueryData(accessibleNavigation: true),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    CalendarWidgetState state = await goToDate(tester, targetDate);

    // Tap to expand month view
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Tap previous arrow
    await tester.tap(find.byKey(Key('calendar-a11y-previous-month')));
    await tester.pumpAndSettle();

    // Month should be December 1999
    var monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
    expect(monthWidget.year, 1999);
    expect(monthWidget.month, 12);

    // Selected day should be Same day of the month (Dec 1, 1999)
    expect(state.selectedDay, DateTime(1999, 12, 1));
  });

  testWidgetsWithAccessibilityChecks('A11y arrow moves to next month', (tester) async {
    await tester.pumpWidget(
      TestApp(
        MediaQuery(
          child: CalendarWidget(
            dayBuilder: (_, __) => Container(),
            eventCount: CalendarEventCount(),
          ),
          data: MediaQueryData(accessibleNavigation: true),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    DateTime targetDate = DateTime(2000, 1, 1);
    CalendarWidgetState state = await goToDate(tester, targetDate);

    // Tap to expand month view
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Tap next arrow
    await tester.tap(find.byKey(Key('calendar-a11y-next-month')));
    await tester.pumpAndSettle();

    // Month should be February 2000
    var monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
    expect(monthWidget.year, 2000);
    expect(monthWidget.month, 2);

    // Selected day should be same day of the month (Feb 1, 2000)
    expect(state.selectedDay, DateTime(2000, 2, 1));
  });

  testWidgetsWithAccessibilityChecks('Selects date from week view', (tester) async {
    DateTime dateForDayBuilder = null;
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) {
            dateForDayBuilder = day;
            return Container();
          },
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    await goToDate(tester, DateTime(2000, 1, 1));

    await tester.tap(find.text('31'));
    await tester.pumpAndSettle();

    // Day should have built Dec 31 1999
    expect(dateForDayBuilder, DateTime(1999, 12, 31));

    // Week should start Dec 26, 1999
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(1999, 12, 26));

    // Month should be December 1999
    final monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth, skipOffstage: false));
    expect(monthWidget.year, 1999);
    expect(monthWidget.month, 12);
  });

  testWidgetsWithAccessibilityChecks('Selects date from month view', (tester) async {
    DateTime dateForDayBuilder = null;
    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) {
            dateForDayBuilder = day;
            return Container();
          },
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    await goToDate(tester, DateTime(2000, 1, 1));
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Tap the last '1', which should be February 1 2000
    await tester.tap(find.text('1').last);
    await tester.pumpAndSettle();

    // Day should have built Feb 1 2000
    expect(dateForDayBuilder, DateTime(2000, 2, 1));

    // Week should start Jan 30 2000
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek, skipOffstage: false).first).firstDay;
    expect(weekStart, DateTime(2000, 1, 30));

    // Month should be Feb 2000
    final monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
    expect(monthWidget.year, 2000);
    expect(monthWidget.month, 2);
  });

  testWidgetsWithAccessibilityChecks('Swipes to select adjacent day', (tester) async {
    DateTime dateForDayBuilder = null;
    final dayContentKey = Key('day-content');

    await tester.pumpWidget(
      TestApp(
        CalendarWidget(
          dayBuilder: (_, day) {
            dateForDayBuilder = day;
            return Container(key: dayContentKey);
          },
          eventCount: CalendarEventCount(),
        ),
        highContrast: true,
      ),
    );
    await tester.pumpAndSettle();

    await goToDate(tester, DateTime(2000, 1, 1));

    // Swipe to next day, which should be Jan 2 2000
    await tester.fling(find.byKey(dayContentKey), Offset(-50, 0), 300);
    await tester.pumpAndSettle();

    // Day should have built Jan 2 2000
    expect(dateForDayBuilder, DateTime(2000, 1, 2));

    // Week should start Jan 2 2000
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(2000, 1, 2));

    // Month should be Jan 2000
    final monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth, skipOffstage: false));
    expect(monthWidget.year, 2000);
    expect(monthWidget.month, 1);
  });
}
