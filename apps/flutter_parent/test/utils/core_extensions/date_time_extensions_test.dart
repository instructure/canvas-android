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
import 'package:intl/intl.dart';
import 'package:test/test.dart';

void main() {
  String localize(String date, String time) => 'Due $date at $time';

  DateTime now = DateTime.now();
  DateTime nowUtc = now.toUtc();

  String expectedDefaultDate = DateFormat.MMMd().format(now);
  String expectedDefaultTime = DateFormat.jm().format(now);
  String expectedDefaultOutput = localize(expectedDefaultDate, expectedDefaultTime);

  test('returns null if DateTime is null', () {
    DateTime date = null;
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
    String actual = now.l10nFormat(localize, dateFormat: DateFormat.MMMMEEEEd());
    expect(actual, expected);
  });

  test('applies specified time format', () {
    String expectedTime = DateFormat.MMMMEEEEd().format(now);
    String expected = localize(expectedDefaultDate, expectedTime);
    String actual = now.l10nFormat(localize, timeFormat: DateFormat.MMMMEEEEd());
    expect(actual, expected);
  });
}
