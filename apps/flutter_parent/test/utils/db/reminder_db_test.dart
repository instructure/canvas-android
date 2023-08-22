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

import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
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
    ReminderDb.createTable(database, 0);
    verify(database.execute(argThat(contains('create table ${ReminderDb.tableName}'))));
  });

  test('insert performs insert on database and returns inserted item with id', () async {
    final reminder = Reminder((b) => b
      ..userDomain = 'domain'
      ..userId = 'user-id'
      ..type = 'type'
      ..itemId = 'item-id'
      ..date = DateTime.now());

    final expected = reminder.rebuild((b) => b..id = 123);

    when(database.query(
      any,
      columns: anyNamed('columns'),
      where: anyNamed('where'),
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => [ReminderDb.toMap(expected)]);

    var actual = await ReminderDb().insert(reminder);

    expect(actual, expected);
    verify(database.insert(ReminderDb.tableName, ReminderDb.toMap(reminder)));
  });

  test('getById queries the database', () async {
    final reminderId = 123;

    when(database.query(
      any,
      columns: anyNamed('columns'),
      where: anyNamed('where'),
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    await ReminderDb().getById(reminderId);

    verify(database.query(
      ReminderDb.tableName,
      columns: ReminderDb.allColumns,
      where: '${ReminderDb.columnId} = ?',
      whereArgs: [reminderId],
    ));
  });

  test('getByItem queries the database', () async {
    final domain = 'domain';
    final userId = 'userId';
    final type = 'type';
    final itemId = 'itemId';

    when(database.query(
      any,
      columns: anyNamed('columns'),
      where: anyNamed('where'),
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    await ReminderDb().getByItem(domain, userId, type, itemId);

    verify(database.query(
      ReminderDb.tableName,
      columns: ReminderDb.allColumns,
      where:
          '${ReminderDb.columnUserDomain} = ? AND ${ReminderDb.columnUserId} = ? AND ${ReminderDb.columnType} = ? AND ${ReminderDb.columnItemId} = ?',
      whereArgs: [domain, userId, type, itemId],
    ));
  });

  test('getAllForUser queries the database', () async {
    final domain = 'domain';
    final userId = 'userId';

    when(database.query(
      any,
      columns: anyNamed('columns'),
      where: anyNamed('where'),
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    await ReminderDb().getAllForUser(domain, userId);

    verify(database.query(
      ReminderDb.tableName,
      columns: ReminderDb.allColumns,
      where: '${ReminderDb.columnUserDomain} = ? AND ${ReminderDb.columnUserId} = ?',
      whereArgs: [domain, userId],
    ));
  });

  test('deleteById deletes from the database', () async {
    final reminderId = 123;

    await ReminderDb().deleteById(reminderId);

    verify(database.delete(
      ReminderDb.tableName,
      where: '${ReminderDb.columnId} = ?',
      whereArgs: [reminderId],
    ));
  });

  test('deleteAllForUser deletes from the database', () async {
    final domain = 'domain';
    final userId = 'userId';

    await ReminderDb().deleteAllForUser(domain, userId);

    verify(database.delete(
      ReminderDb.tableName,
      where: '${ReminderDb.columnUserDomain} = ? AND ${ReminderDb.columnUserId} = ?',
      whereArgs: [domain, userId],
    ));
  });
}
