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

import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:intl/intl.dart';
import 'package:test/test.dart';

void main() {
  group('l10nFormat', () {
    String localize(String date, String time) => 'Due $date at $time';

    DateTime now = DateTime.now();
    DateTime nowUtc = now.toUtc();

    String expectedDefaultDate = DateFormat.MMMd().format(now);
    String expectedDefaultTime = DateFormat.jm().format(now);
    String expectedDefaultOutput = localize(expectedDefaultDate, expectedDefaultTime);

    test('returns null if DateTime is null', () {
      DateTime? date = null;
      expect(date.l10nFormat(localize), isNull);
    });

    test('returns null if localizer is null', () {
      expect(now.l10nFormat(null), isNull);
    });

    test('correctly formats DateTime', () {
      expect(now.l10nFormat(localize), expectedDefaultOutput);
    });

    test('converts to local DateTime', () {
      expect(nowUtc.l10nFormat(localize), expectedDefaultOutput);
    });

    test('uses correct default formats', () {
      expect(now.l10nFormat(localize, dateFormat: null, timeFormat: null), expectedDefaultOutput);
    });

    test('applies specified date format', () {
      String expectedDate = DateFormat.MMMMEEEEd().format(now);
      String expected = localize(expectedDate, expectedDefaultTime);
      String? actual = now.l10nFormat(localize, dateFormat: DateFormat.MMMMEEEEd());
      expect(actual, expected);
    });

    test('applies specified time format', () {
      String expectedTime = DateFormat.MMMMEEEEd().format(now);
      String expected = localize(expectedDefaultDate, expectedTime);
      String? actual = now.l10nFormat(localize, timeFormat: DateFormat.MMMMEEEEd());
      expect(actual, expected);
    });
  });

  group('isSameDayAs', () {
    test('returns true for the same DateTime', () {
      DateTime date = DateTime.now();
      expect(date.isSameDayAs(date), isTrue);
    });

    test('returns false if this date is null', () {
      DateTime? date1 = null;
      DateTime date2 = DateTime.now();
      expect(date1.isSameDayAs(date2), isFalse);
    });

    test('returns false if other date is null', () {
      DateTime date1 = DateTime.now();
      DateTime? date2 = null;
      expect(date1.isSameDayAs(date2), isFalse);
    });

    test('returns false if both dates are null', () {
      DateTime? date1 = null;
      DateTime? date2 = null;
      expect(date1.isSameDayAs(date2), isFalse);
    });

    test('returns true if dates are identical but times are not', () {
      DateTime date1 = DateTime(2000, 1, 1, 0, 0);
      DateTime date2 = DateTime(2000, 1, 1, 23, 59);
      expect(date1.isSameDayAs(date2), isTrue);
    });

    test('returns false if dates are not the same', () {
      DateTime date1 = DateTime(2000, 1, 1);
      DateTime date2 = DateTime(2000, 1, 2);
      expect(date1.isSameDayAs(date2), isFalse);
    });
  });

  group('withFirstDayOfWeek', () {
    initializeDateFormatting();

    tearDownAll(() {
      Intl.defaultLocale = Intl.systemLocale;
    });

    test('returns null if date is null', () {
      DateTime? date = null;
      expect(date.withFirstDayOfWeek(), isNull);
    });

    test('returns correct date for every day of week for US locale', () {
      DateTime weekStart = DateTime(1999, 12, 26); // Week starting December 26, 2000
      DateTime nextWeekStart = DateTime(2000, 1, 2); // Week starting January 2, 2000

      // First day of week is Sunday, December 26, 1999
      expect(DateTime(1999, 12, 26).withFirstDayOfWeek(), weekStart);

      // Last day of week is Saturday, January 1, 2000
      expect(DateTime(2000, 1, 1).withFirstDayOfWeek(), weekStart);

      // First day of next week is Sunday, January 2, 2000
      expect(DateTime(2000, 1, 2).withFirstDayOfWeek(), nextWeekStart);
    });

    test('returns correct date for every day of week for German locale', () {
      DateTime weekStart = DateTime(1999, 12, 27); // Week starting December 27, 1999
      DateTime nextWeekStart = DateTime(2000, 1, 3); // Week starting January 3, 2000

      // Set locale to German
      Intl.defaultLocale = 'de';

      // First day of week is Monday, December 27, 1999
      expect(DateTime(1999, 12, 27).withFirstDayOfWeek(), weekStart);

      // Last day of week is Sunday, January 2, 2000
      expect(DateTime(2000, 1, 2).withFirstDayOfWeek(), weekStart);

      // First day of next week is Monday, January 3, 2000
      expect(DateTime(2000, 1, 3).withFirstDayOfWeek(), nextWeekStart);
    });

    test('returns correct date for every day of week for Arabic locale', () {
      DateTime weekStart = DateTime(2000, 1, 1); // Week starting January 1, 2000
      DateTime nextWeekStart = DateTime(2000, 1, 8); // Week starting January 8, 2000

      // Set locale to Arabic
      Intl.defaultLocale = 'ar';

      // First day of week is Saturday, January 1, 2000
      expect(DateTime(2000, 1, 1).withFirstDayOfWeek(), weekStart);

      // Last day of week is Sunday, January
      expect(DateTime(2000, 1, 7).withFirstDayOfWeek(), weekStart);

      // First day of next week is Monday, January 3, 2000
      expect(DateTime(2000, 1, 8).withFirstDayOfWeek(), nextWeekStart);
    });
  });

  group('isWeekend', () {
    initializeDateFormatting();

    tearDownAll(() {
      Intl.defaultLocale = Intl.systemLocale;
    });

    test('returns false if date is null', () {
      DateTime? date = null;
      expect(date.isWeekend(), isFalse);
    });

    test('returns correct values for US locale', () {
      expect(DateTime(2000, 1, 3).isWeekend(), isFalse); // Monday
      expect(DateTime(2000, 1, 4).isWeekend(), isFalse); // Tuesday
      expect(DateTime(2000, 1, 5).isWeekend(), isFalse); // Wednesday
      expect(DateTime(2000, 1, 6).isWeekend(), isFalse); // Thursday
      expect(DateTime(2000, 1, 7).isWeekend(), isFalse); // Friday
      expect(DateTime(2000, 1, 8).isWeekend(), isTrue); // Saturday
      expect(DateTime(2000, 1, 9).isWeekend(), isTrue); // Sunday
    });

    test('returns correct values for German locale', () {
      // Set locale to German
      Intl.defaultLocale = 'de';

      expect(DateTime(2000, 1, 3).isWeekend(), isFalse); // Monday
      expect(DateTime(2000, 1, 4).isWeekend(), isFalse); // Tuesday
      expect(DateTime(2000, 1, 5).isWeekend(), isFalse); // Wednesday
      expect(DateTime(2000, 1, 6).isWeekend(), isFalse); // Thursday
      expect(DateTime(2000, 1, 7).isWeekend(), isFalse); // Friday
      expect(DateTime(2000, 1, 8).isWeekend(), isTrue); // Saturday
      expect(DateTime(2000, 1, 9).isWeekend(), isTrue); // Sunday
    });

    test('returns correct values for Arabic locale', () {
      // Set locale to Arabic
      Intl.defaultLocale = 'ar';

      expect(DateTime(2000, 1, 3).isWeekend(), isFalse); // Monday
      expect(DateTime(2000, 1, 4).isWeekend(), isFalse); // Tuesday
      expect(DateTime(2000, 1, 5).isWeekend(), isFalse); // Wednesday
      expect(DateTime(2000, 1, 6).isWeekend(), isFalse); // Thursday
      expect(DateTime(2000, 1, 7).isWeekend(), isTrue); // Friday
      expect(DateTime(2000, 1, 8).isWeekend(), isTrue); // Saturday
      expect(DateTime(2000, 1, 9).isWeekend(), isFalse); // Sunday
    });

    test('returns correct values for Pashto locale', () {
      // Set locale to Pashto
      Intl.defaultLocale = 'ps';

      expect(DateTime(2000, 1, 3).isWeekend(), isFalse); // Monday
      expect(DateTime(2000, 1, 4).isWeekend(), isFalse); // Tuesday
      expect(DateTime(2000, 1, 5).isWeekend(), isFalse); // Wednesday
      expect(DateTime(2000, 1, 6).isWeekend(), isTrue); // Thursday
      expect(DateTime(2000, 1, 7).isWeekend(), isTrue); // Friday
      expect(DateTime(2000, 1, 8).isWeekend(), isFalse); // Saturday
      expect(DateTime(2000, 1, 9).isWeekend(), isFalse); // Sunday
    });
  });

  group('roundToMidnight', () {
    test('returns same date if already midnight', () {
      final date = DateTime(2000, 1, 12);
      final actual = date.roundToMidnight();
      expect(actual, date);
    });

    test('returns same date if hour is less than 12', () {
      final date = DateTime(2000, 1, 12, 7);
      final expected = DateTime(2000, 1, 12);
      final actual = date.roundToMidnight();
      expect(actual, expected);
    });

    test('returns next day if hour is 12', () {
      final date = DateTime(2000, 1, 12, 12);
      final expected = DateTime(2000, 1, 13);
      final actual = date.roundToMidnight();
      expect(actual, expected);
    });

    test('returns next day if hour is greater than 12', () {
      final date = DateTime(2000, 1, 12, 23);
      final expected = DateTime(2000, 1, 13);
      final actual = date.roundToMidnight();
      expect(actual, expected);
    });
  });
}
