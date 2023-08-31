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
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_day_of_week_headers.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/test_app.dart';

void main() {
  testWidgetsWithAccessibilityChecks('Displays correctly for US locale', (tester) async {
    // Day names, in expected display order
    final allDays = [
      find.text('Sun'),
      find.text('Mon'),
      find.text('Tue'),
      find.text('Wed'),
      find.text('Thu'),
      find.text('Fri'),
      find.text('Sat'),
    ];

    // Weekdays
    final weekdays = [
      find.text('Mon'),
      find.text('Tue'),
      find.text('Wed'),
      find.text('Thu'),
      find.text('Fri'),
    ];

    // Weekend days
    final weekends = [
      find.text('Sun'),
      find.text('Sat'),
    ];

    await tester.pumpWidget(TestApp(DayOfWeekHeaders()));
    await tester.pump();

    // Confirm all weekday names are displayed
    allDays.forEach((it) => expect(it, findsOneWidget));

    // Confirm names are displayed in order
    for (int i = 1; i < allDays.length; i++) {
      expect(tester.getTopLeft(allDays[i - 1]).dx, lessThan(tester.getTopLeft(allDays[i]).dx));
    }

    // Week days should use dark text
    weekdays.forEach((day) {
      final textColor = tester.widget<Text>(day).style!.color;
      expect(textColor, ParentColors.licorice);
    });

    // weekends should use faded text
    weekends.forEach((day) {
      final textColor = tester.widget<Text>(day).style!.color;
      expect(textColor, ParentColors.oxford);
    });
  });

  testWidgetsWithAccessibilityChecks('Displays correctly for German locale', (tester) async {
    // Weekday names in expected display order
    final allDays = [
      find.text('Mo'),
      find.text('Di'),
      find.text('Mi'),
      find.text('Do'),
      find.text('Fr'),
      find.text('Sa'),
      find.text('So'),
    ];

    // Weekdays
    final weekdays = [
      find.text('Mo'),
      find.text('Di'),
      find.text('Mi'),
      find.text('Do'),
      find.text('Fr'),
    ];

    // Weekend days
    final weekends = [
      find.text('Sa'),
      find.text('So'),
    ];

    await tester.pumpWidget(TestApp(DayOfWeekHeaders(), locale: Locale('de')));
    await tester.pump();

    // Confirm all weekday names are displayed
    allDays.forEach((it) => expect(it, findsOneWidget));

    // Confirm names are displayed in order
    for (int i = 1; i < allDays.length; i++) {
      expect(tester.getTopLeft(allDays[i - 1]).dx, lessThan(tester.getTopLeft(allDays[i]).dx));
    }

    // Week days should use dark text
    weekdays.forEach((day) {
      final textColor = tester.widget<Text>(day).style!.color;
      expect(textColor, ParentColors.licorice);
    });

    // weekends should use faded text
    weekends.forEach((day) {
      final textColor = tester.widget<Text>(day).style!.color;
      expect(textColor, ParentColors.oxford);
    });
  });

  testWidgetsWithAccessibilityChecks('Displays correctly for Arabic locale - RTL', (tester) async {
    // Weekday names in expected display order (RTL)
    final allDays = [
      find.text('الجمعة'),
      find.text('الخميس'),
      find.text('الأربعاء'),
      find.text('الثلاثاء'),
      find.text('الاثنين'),
      find.text('الأحد'),
      find.text('السبت'),
    ];

    // Weekdays
    final weekdays = [
      find.text('الخميس'),
      find.text('الأربعاء'),
      find.text('الثلاثاء'),
      find.text('الاثنين'),
      find.text('الأحد'),
    ];

    // Weekend days
    final weekends = [
      find.text('الجمعة'),
      find.text('السبت'),
    ];

    await tester.pumpWidget(TestApp(DayOfWeekHeaders(), locale: Locale('ar')));
    await tester.pump();

    // Confirm all weekday names are displayed
    allDays.forEach((it) => expect(it, findsOneWidget));

    // Confirm names are displayed in order
    for (int i = 1; i < allDays.length; i++) {
      expect(tester.getTopLeft(allDays[i - 1]).dx, lessThan(tester.getTopLeft(allDays[i]).dx));
    }

    // Week days should use dark text
    weekdays.forEach((day) {
      final textColor = tester.widget<Text>(day).style!.color;
      expect(textColor, ParentColors.licorice);
    });

    // weekends should use faded text
    weekends.forEach((day) {
      final textColor = tester.widget<Text>(day).style!.color;
      expect(textColor, ParentColors.oxford);
    });
  });
}
