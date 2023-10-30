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

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/calendar_filter.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:mockito/mockito.dart';
import 'package:sqflite/sqflite.dart';
import 'package:test/test.dart';

import '../test_app.dart';
import '../test_helpers/mock_helpers.dart';
import '../test_helpers/mock_helpers.mocks.dart';

void main() {
  final database = MockDatabase();

  setupTestLocator((locator) {
    locator.registerLazySingleton<Database>(() => database);
  });

  setUp(() {
    reset(database);
  });

  test('createTable executes a create table statement', () async {
    CalendarFilterDb.createTable(database, 0);
    verify(database.execute(argThat(contains('create table ${CalendarFilterDb.tableName}'))));
  });

  test('updateTable executes a create table statement if last db version was less than 2', () async {
    final lastVersion = 1;
    final thisVersion = 100;
    CalendarFilterDb.updateTable(database, lastVersion, thisVersion);
    verify(database.execute(argThat(contains('create table ${CalendarFilterDb.tableName}'))));
  });

  test('updateTable does not create table if last db version was 2 or higher', () async {
    final lastVersion = 2;
    final thisVersion = 3;
    CalendarFilterDb.updateTable(database, lastVersion, thisVersion);
    verifyNever(database.execute(argThat(contains('create table ${CalendarFilterDb.tableName}'))));
  });

  test('insertOrUpdate performs insert if there is no existing item, and returns inserted item with id', () async {
    final calendarFilter = CalendarFilter((b) => b
      ..userDomain = 'domain'
      ..userId = 'user-id'
      ..observeeId = 'observee-id'
      ..filters = SetBuilder());

    final expected = calendarFilter.rebuild((b) => b..id = 123);

    // Mock no existing item, i.e. return nothing for 'getByObserveeId'
    when(database.query(
      any,
      columns: CalendarFilterDb.allColumns,
      where:
          '${CalendarFilterDb.columnUserDomain} = ? AND ${CalendarFilterDb.columnUserId} = ? AND ${CalendarFilterDb.columnObserveeId} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    // Mock inserted item
    when(database.query(
      any,
      columns: CalendarFilterDb.allColumns,
      where: '${CalendarFilterDb.columnId} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => [CalendarFilterDb.toMap(expected)]);

    var actual = await CalendarFilterDb().insertOrUpdate(calendarFilter);

    expect(actual, expected);

    verify(database.insert(CalendarFilterDb.tableName, CalendarFilterDb.toMap(calendarFilter)));
  });

  test('insertOrUpdate performs update if there is an existing item, and returns updated item', () async {
    final calendarFilter = CalendarFilter((b) => b
      ..userDomain = 'domain'
      ..userId = 'user-id'
      ..observeeId = 'observee-id'
      ..filters = SetBuilder({'course_123'}));

    final existing = calendarFilter.rebuild((b) => b
      ..id = 123
      ..filters = SetBuilder());

    final expected = calendarFilter.rebuild((b) => b..id = 123);

    // Mock existing item
    when(database.query(
      any,
      columns: CalendarFilterDb.allColumns,
      where:
          '${CalendarFilterDb.columnUserDomain} = ? AND ${CalendarFilterDb.columnUserId} = ? AND ${CalendarFilterDb.columnObserveeId} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => [CalendarFilterDb.toMap(existing)]);

    // Mock updated item
    when(database.query(
      any,
      columns: CalendarFilterDb.allColumns,
      where: '${CalendarFilterDb.columnId} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => [CalendarFilterDb.toMap(expected)]);

    var actual = await CalendarFilterDb().insertOrUpdate(calendarFilter);

    expect(actual, expected);

    verify(database.update(
      CalendarFilterDb.tableName,
      CalendarFilterDb.toMap(expected),
      where: '${CalendarFilterDb.columnId} = ?',
      whereArgs: [123],
    ));
  });

  test('getById queries the database', () async {
    final calendarFilterId = 123;

    when(database.query(
      any,
      columns: anyNamed('columns'),
      where: anyNamed('where'),
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    await CalendarFilterDb().getById(calendarFilterId);

    verify(database.query(
      CalendarFilterDb.tableName,
      columns: CalendarFilterDb.allColumns,
      where: '${CalendarFilterDb.columnId} = ?',
      whereArgs: [calendarFilterId],
    ));
  });

  test('getByObserveeId queries the database', () async {
    final domain = 'domain';
    final userId = 'userId';
    final observeeId = 'observeeId';

    when(database.query(
      any,
      columns: anyNamed('columns'),
      where: anyNamed('where'),
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    await CalendarFilterDb().getByObserveeId(domain, userId, observeeId);

    verify(database.query(
      CalendarFilterDb.tableName,
      columns: CalendarFilterDb.allColumns,
      where:
          '${CalendarFilterDb.columnUserDomain} = ? AND ${CalendarFilterDb.columnUserId} = ? AND ${CalendarFilterDb.columnObserveeId} = ?',
      whereArgs: [domain, userId, observeeId],
    ));
  });

  test('deleteById deletes from the database', () async {
    final calendarFilterId = 123;

    await CalendarFilterDb().deleteById(calendarFilterId);

    verify(database.delete(
      CalendarFilterDb.tableName,
      where: '${CalendarFilterDb.columnId} = ?',
      whereArgs: [calendarFilterId],
    ));
  });

  test('deleteAllForUser deletes from the database', () async {
    final domain = 'domain';
    final userId = 'userId';

    await CalendarFilterDb().deleteAllForUser(domain, userId);

    verify(database.delete(
      CalendarFilterDb.tableName,
      where: '${CalendarFilterDb.columnUserDomain} = ? AND ${CalendarFilterDb.columnUserId} = ?',
      whereArgs: [domain, userId],
    ));
  });

  group('joinFilters', () {
    test('joinfilters returns empty string for empty filter list', () {
      Set<String> filters = {};
      final expected = '';
      final actual = CalendarFilterDb.joinFilters(filters);
      expect(actual, expected);
    });

    test('joinfilters returns empty string for null filter list', () {
      Set<String>? filters = null;
      final expected = '';
      final actual = CalendarFilterDb.joinFilters(filters);
      expect(actual, expected);
    });

    test('joinfilters returns correct string for single-item filter list', () {
      Set<String> filters = {'ABC'};
      final expected = 'ABC';
      final actual = CalendarFilterDb.joinFilters(filters);
      expect(actual, expected);
    });

    test('joinfilters returns correct string for multi-item filter list', () {
      Set<String> filters = {'ABC', '123', 'DEF', '456'};
      final expected = 'ABC|123|DEF|456';
      final actual = CalendarFilterDb.joinFilters(filters);
      expect(actual, expected);
    });
  });

  group('splitFilters', () {
    test('splitFilters returns empty list for empty string', () {
      String input = '';
      List<String> expected = [];
      final actual = CalendarFilterDb.splitFilters(input);
      expect(actual, expected);
    });

    test('splitFilters returns empty list for null string', () {
      String? input = null;
      List<String> expected = [];
      final actual = CalendarFilterDb.splitFilters(input);
      expect(actual, expected);
    });

    test('splitFilters returns single-item list for single-item string', () {
      String input = 'ABC';
      List<String> expected = ['ABC'];
      final actual = CalendarFilterDb.splitFilters(input);
      expect(actual, expected);
    });

    test('splitFilters returns multi-item list for multi-item string', () {
      String input = 'ABC|123|DEF|456';
      List<String> expected = ['ABC', '123', 'DEF', '456'];
      final actual = CalendarFilterDb.splitFilters(input);
      expect(actual, expected);
    });
  });
}
