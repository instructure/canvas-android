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

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_click_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_month.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_week.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_parent/utils/common_widgets/dropdown_arrow.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/test_app.dart';

void main() {
  setupTestLocator((locator) {
    locator.registerLazySingleton<CalendarTodayClickNotifier>(() => CalendarTodayClickNotifier());
  });
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

  Widget calendarTestApp(Widget child, {Locale? locale}) {
    return TestApp(ChangeNotifierProvider(create: (_) => CalendarTodayNotifier(), child: child), locale: locale);
  }

  testWidgetsWithAccessibilityChecks('Displays week view by default', (tester) async {
    await tester.pumpWidget(
      calendarTestApp(
        CalendarWidget(
          dayBuilder: (_, __) => Container(),
          fetcher: _FakeFetcher(),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.byType(CalendarWeek), findsOneWidget);
    expect(find.byType(CalendarMonth), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Displays month view when passed in', (tester) async {
    await tester.pumpWidget(
      calendarTestApp(
        CalendarWidget(
          dayBuilder: (_, __) => Container(),
          fetcher: _FakeFetcher(),
          startingView: CalendarView.Month,
        ),
      ),
    );
    await tester.pumpAndSettle();

    // Weeks can be 4, 5 or 6 in a month view, we just have to match greater than 4
    expect(find.byType(CalendarWeek).evaluate().length, greaterThanOrEqualTo(4),
        reason: 'Not showing all weeks for the month');
    expect(find.byType(CalendarMonth), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Switches to specified starting date', (tester) async {
    DateTime startingDate = DateTime(2000, 1, 1);
    DateTime? dateForDayBuilder = null;
    await tester.pumpWidget(
      calendarTestApp(
        CalendarWidget(
          startingDate: startingDate,
          dayBuilder: (_, day) {
            dateForDayBuilder = day;
            return Container();
          },
          fetcher: _FakeFetcher(),
        ),
      ),
    );
    await tester.pumpAndSettle();

    // Day should have built using target date
    expect(dateForDayBuilder, startingDate);

    // Week should start on Dec 26, 1999
    DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
    expect(weekStart, DateTime(1999, 12, 26));

    // Month should be Jan 2000
    final monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth, skipOffstage: false));
    expect(monthWidget.year, 2000);
    expect(monthWidget.month, 1);
  });

  testWidgetsWithAccessibilityChecks('Can click events in day content while month is visible', (tester) async {
    var pressed = false;

    final calendar = CalendarWidget(
      dayBuilder: (_, __) => TextButton(
        onPressed: () => pressed = true,
        child: Text('Press me!'),
      ),
      fetcher: _FakeFetcher(),
    );

    await tester.pumpWidget(
      calendarTestApp(
        calendar,
      ),
    );
    await tester.pumpAndSettle();

    // Tap expand button
    await tester.tap(find.byKey(Key('expand-button')));
    await tester.pumpAndSettle();

    // Should show month now
    expect(find.byType(CalendarMonth), findsOneWidget);

    // Tap on the 'event'
    await tester.tap(find.byType(TextButton));
    await tester.pumpAndSettle();

    // Should have our pressed value true
    expect(pressed, true);
  });

  testWidgetsWithAccessibilityChecks('Can click events in day content while week is visible', (tester) async {
    var pressed = false;

    final calendar = CalendarWidget(
      dayBuilder: (_, __) => TextButton(
        onPressed: () => pressed = true,
        child: Text('Press me!'),
      ),
      fetcher: _FakeFetcher(),
    );

    await tester.pumpWidget(
      calendarTestApp(
        calendar,
      ),
    );
    await tester.pumpAndSettle();

    // Should show month now
    expect(find.byType(CalendarMonth), findsNothing);
    expect(find.byType(CalendarWeek), findsOneWidget);

    // Tap on the 'event'
    await tester.tap(find.byType(TextButton));
    await tester.pumpAndSettle();

    // Should have our pressed value true
    expect(pressed, true);
  });

  group('Month Expand/Collapse', () {
    testWidgetsWithAccessibilityChecks('Expand button expands and collapses month', (tester) async {
      final calendar = CalendarWidget(
        dayBuilder: (_, __) => Container(),
        fetcher: _FakeFetcher(),
      );
      await tester.pumpWidget(
        calendarTestApp(
          calendar,
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
        fetcher: _FakeFetcher(),
      );
      await tester.pumpWidget(
        calendarTestApp(
          calendar,
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
        fetcher: _FakeFetcher(),
      );
      await tester.pumpWidget(
        calendarTestApp(
          calendar,
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
        fetcher: _FakeFetcher(),
      );
      await tester.pumpWidget(
        calendarTestApp(
          calendar,
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
  });

  group('Set day/week/month', () {
    testWidgetsWithAccessibilityChecks('Jumps to selected date', (tester) async {
      DateTime? dateForDayBuilder = null;
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) {
              dateForDayBuilder = day;
              return Container();
            },
            fetcher: _FakeFetcher(),
          ),
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
      DateTime? dateForDayBuilder = null;
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) {
              dateForDayBuilder = day;
              return Container();
            },
            fetcher: _FakeFetcher(),
          ),
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
  });

  group('Week and Month Swipe', () {
    testWidgetsWithAccessibilityChecks('Swipes to previous week', (tester) async {
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) => Container(),
            fetcher: _FakeFetcher(),
          ),
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
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) => Container(),
            fetcher: _FakeFetcher(),
          ),
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
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) => Container(),
            fetcher: _FakeFetcher(),
          ),
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
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) => Container(),
            fetcher: _FakeFetcher(),
          ),
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
  });

  group('Accessibility', () {
    testWidgetsWithAccessibilityChecks('Displays a11y arrows for week view', (tester) async {
      await tester.pumpWidget(
        calendarTestApp(
          MediaQuery(
            child: CalendarWidget(
              dayBuilder: (_, __) => Container(),
              fetcher: _FakeFetcher(),
            ),
            data: MediaQueryData(accessibleNavigation: true),
          ),
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
        calendarTestApp(
          MediaQuery(
            child: CalendarWidget(
              dayBuilder: (_, __) => Container(),
              fetcher: _FakeFetcher(),
            ),
            data: MediaQueryData(accessibleNavigation: true),
          ),
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
        calendarTestApp(
          MediaQuery(
            child: CalendarWidget(
              dayBuilder: (_, __) => Container(),
              fetcher: _FakeFetcher(),
            ),
            data: MediaQueryData(accessibleNavigation: true),
          ),
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
        calendarTestApp(
          MediaQuery(
            child: CalendarWidget(
              dayBuilder: (_, __) => Container(),
              fetcher: _FakeFetcher(),
            ),
            data: MediaQueryData(accessibleNavigation: true),
          ),
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
        calendarTestApp(
          MediaQuery(
            child: CalendarWidget(
              dayBuilder: (_, __) => Container(),
              fetcher: _FakeFetcher(),
            ),
            data: MediaQueryData(accessibleNavigation: true),
          ),
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
        calendarTestApp(
          MediaQuery(
            child: CalendarWidget(
              dayBuilder: (_, __) => Container(),
              fetcher: _FakeFetcher(),
            ),
            data: MediaQueryData(accessibleNavigation: true),
          ),
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
  });

  group('Date Selection', () {
    testWidgetsWithAccessibilityChecks('Selects date from week view', (tester) async {
      DateTime? dateForDayBuilder = null;
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) {
              dateForDayBuilder = day;
              return Container();
            },
            fetcher: _FakeFetcher(),
          ),
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
      DateTime? dateForDayBuilder = null;
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) {
              dateForDayBuilder = day;
              return Container();
            },
            fetcher: _FakeFetcher(),
          ),
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
      DateTime? dateForDayBuilder = null;
      final dayContentKey = Key('day-content');

      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) {
              dateForDayBuilder = day;
              return Container(key: dayContentKey);
            },
            fetcher: _FakeFetcher(),
          ),
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
  });

  group('Insufficient Height', () {
    testWidgetsWithAccessibilityChecks('disables expand/collapse button for insufficient height', (tester) async {
      final calendarHeight = 200;
      final screenHeight = tester.binding.window.physicalSize.height / tester.binding.window.devicePixelRatio;
      final calendar = CalendarWidget(
        dayBuilder: (_, __) => Container(),
        fetcher: _FakeFetcher(),
      );
      await tester.pumpWidget(
        calendarTestApp(
          Column(
            children: <Widget>[
              SizedBox(height: screenHeight - calendarHeight),
              Expanded(child: calendar),
            ],
          ),
        ),
      );
      await tester.pumpAndSettle();

      // Should not show dropdown arrow
      expect(find.byType(DropdownArrow), findsNothing);

      // Tap expand button
      await tester.tap(find.byKey(Key('expand-button')));
      await tester.pumpAndSettle();

      // Should not show month
      expect(find.byType(CalendarMonth), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('disables swipe-to-expand for insufficient height', (tester) async {
      final calendarHeight = 200;
      final screenHeight = tester.binding.window.physicalSize.height / tester.binding.window.devicePixelRatio;
      final calendar = CalendarWidget(
        dayBuilder: (_, __) => Container(),
        fetcher: _FakeFetcher(),
      );
      await tester.pumpWidget(
        calendarTestApp(
          Column(
            children: <Widget>[
              SizedBox(height: screenHeight - calendarHeight),
              Expanded(child: calendar),
            ],
          ),
        ),
      );
      await tester.pumpAndSettle();

      // Fling down on week to try expand
      await tester.fling(find.byType(CalendarWeek), Offset(0, 50), 300);
      await tester.pumpAndSettle();

      // Should not show month
      expect(find.byType(CalendarMonth), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('collapses month if height becomes insufficient', (tester) async {
      // Due to how we must capture height changes, when the month view is open and the height becomes insufficient
      // there will be at least one build pass where the month layout overflows its parent. This is acceptable while the
      // month view is animating its collapse into the week view. However, because such overflows will fail the test,
      // we must intercept and ignore those specific errors
      FlutterExceptionHandler? onError = FlutterError.onError;
      FlutterError.onError = (details) {
        var exception = details.exception;
        if (exception is FlutterError && exception.message.startsWith('A RenderFlex overflowed')) {
          // Intentionally left blank
        } else {
          onError!(details);
        }
      };

      try {
        double calendarHeight = 600;
        final screenHeight = tester.binding.window.physicalSize.height / tester.binding.window.devicePixelRatio;

        final calendar = CalendarWidget(
          dayBuilder: (_, __) => Container(),
          fetcher: _FakeFetcher(),
        );

        late StateSetter stateSetter;

        await tester.pumpWidget(
          calendarTestApp(
            StatefulBuilder(
              builder: (context, setState) {
                stateSetter = setState;
                return Column(
                  children: <Widget>[
                    SizedBox(height: screenHeight - calendarHeight),
                    Expanded(child: OverflowBox(maxHeight: calendarHeight, child: calendar)),
                  ],
                );
              },
            ),
          ),
        );
        await tester.pumpAndSettle();

        // Tap expand button
        await tester.tap(find.byKey(Key('expand-button')));
        await tester.pumpAndSettle();

        // Should show month
        expect(find.byType(CalendarMonth), findsOneWidget);

        // Shrink calendar height and rebuild
        calendarHeight = 200;
        stateSetter(() {});
        await tester.pumpAndSettle();

        // Should no longer show month
        expect(find.byType(CalendarMonth), findsNothing);
      } finally {
        // Restore exception handler
        FlutterError.onError = onError;
      }
    });
  });

  group('Right-to-Left', () {
    testWidgetsWithAccessibilityChecks('Swipes to previous week in RTL', (tester) async {
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) => Container(),
            fetcher: _FakeFetcher(),
          ),
          locale: Locale('ar', 'AR'),
        ),
      );
      await tester.pumpAndSettle();

      DateTime targetDate = DateTime(2000, 1, 1);
      CalendarWidgetState state = await goToDate(tester, targetDate);

      // 'ar' week should start on Jan 1, 2000
      DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
      expect(weekStart, DateTime(2000, 1, 1));

      // Selected day should be Saturday (Jan 1 2000)
      expect(state.selectedDay, targetDate);

      // Fling to previous week
      await tester.fling(find.byType(CalendarWeek), Offset(-50, 0), 300);
      await tester.pumpAndSettle();

      // Week should now start on Dec 25, 1999
      weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
      expect(weekStart, DateTime(1999, 12, 25));

      // Selected day should be the same day of the week, Saturday (Dec 25, 1999)
      expect(state.selectedDay, DateTime(1999, 12, 25));
    });

    testWidgetsWithAccessibilityChecks('Swipes to next week in RTL', (tester) async {
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) => Container(),
            fetcher: _FakeFetcher(),
          ),
          locale: Locale('ar', 'AR'),
        ),
      );
      await tester.pumpAndSettle();

      DateTime targetDate = DateTime(2000, 1, 1);
      CalendarWidgetState state = await goToDate(tester, targetDate);

      // 'ar' week should start on Jan 1, 2000
      DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
      expect(weekStart, DateTime(2000, 1, 1));

      // Selected day should be Saturday (Jan 1 2000)
      expect(state.selectedDay, targetDate);

      // Fling to next week
      await tester.fling(find.byType(CalendarWeek), Offset(50, 0), 300);
      await tester.pumpAndSettle();

      // Week should now start on Jan 8, 2000
      weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
      expect(weekStart, DateTime(2000, 1, 8));

      // Selected day should be the same day of the week, Saturday (Jan 8, 2000)
      expect(state.selectedDay, DateTime(2000, 1, 8));
    });

    testWidgetsWithAccessibilityChecks('Swipes to previous month in RTL', (tester) async {
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) => Container(),
            fetcher: _FakeFetcher(),
          ),
          locale: Locale('ar', 'AR'),
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
      await tester.fling(find.byType(CalendarMonth), Offset(-50, 0), 300);
      await tester.pumpAndSettle();

      // Month should be December 1999
      monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
      expect(monthWidget.year, 1999);
      expect(monthWidget.month, 12);

      // Selected day should be Same day of the month (Dec 1, 1999)
      expect(state.selectedDay, DateTime(1999, 12, 1));
    });

    testWidgetsWithAccessibilityChecks('Swipes to next month in RTL', (tester) async {
      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) => Container(),
            fetcher: _FakeFetcher(),
          ),
          locale: Locale('ar', 'AR'),
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
      await tester.fling(find.byType(CalendarMonth), Offset(50, 0), 300);
      await tester.pumpAndSettle();

      // Month should be February 2000
      monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth));
      expect(monthWidget.year, 2000);
      expect(monthWidget.month, 2);

      // Selected day should be same day of the month (Feb 1, 2000)
      expect(state.selectedDay, DateTime(2000, 2, 1));
    });

    testWidgetsWithAccessibilityChecks('Swipes to select adjacent day in RTL', (tester) async {
      DateTime? dateForDayBuilder = null;
      final dayContentKey = Key('day-content');

      await tester.pumpWidget(
        calendarTestApp(
          CalendarWidget(
            dayBuilder: (_, day) {
              dateForDayBuilder = day;
              return Container(key: dayContentKey);
            },
            fetcher: _FakeFetcher(),
          ),
          locale: Locale('ar', 'AR'),
        ),
      );
      await tester.pumpAndSettle();

      await goToDate(tester, DateTime(2000, 1, 1));

      // Swipe to next day, which should be Jan 2 2000
      await tester.fling(find.byKey(dayContentKey), Offset(50, 0), 300);
      await tester.pumpAndSettle();

      // Day should have built Jan 2 2000
      expect(dateForDayBuilder, DateTime(2000, 1, 2));

      // Week should start Jan 1 2000
      DateTime weekStart = tester.widget<CalendarWeek>(find.byType(CalendarWeek)).firstDay;
      expect(weekStart, DateTime(2000, 1, 1));

      // Month should be Jan 2000
      final monthWidget = tester.widget<CalendarMonth>(find.byType(CalendarMonth, skipOffstage: false));
      expect(monthWidget.year, 2000);
      expect(monthWidget.month, 1);
    });
  });
}

class _FakeFetcher extends PlannerFetcher {
  AsyncSnapshot<List<PlannerItem>> nextSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);

  _FakeFetcher({super.observeeId = '', super.userDomain = '', super.userId = ''});

  @override
  AsyncSnapshot<List<PlannerItem>> getSnapshotForDate(DateTime date) => nextSnapshot;
}
