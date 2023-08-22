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
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/user_color.dart';
import 'package:flutter_parent/models/user_colors.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
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
    UserColorsDb.createTable(database, 0);
    verify(database.execute(argThat(contains('create table ${UserColorsDb.tableName}'))));
  });

  test('updateTable executes a create table statement if last db version was less than 4', () async {
    final lastVersion = 3;
    final thisVersion = 100;
    UserColorsDb.updateTable(database, lastVersion, thisVersion);
    verify(database.execute(argThat(contains('create table ${UserColorsDb.tableName}'))));
  });

  test('updateTable does not create table if last db version was 4 or higher', () async {
    final lastVersion = 4;
    final thisVersion = 5;
    UserColorsDb.updateTable(database, lastVersion, thisVersion);
    verifyNever(database.execute(argThat(contains('create table ${UserColorsDb.tableName}'))));
  });

  test('insertOrUpdate performs insert if there is no existing item, and returns inserted item with id', () async {
    final userColor = UserColor((b) => b
      ..userDomain = 'domain'
      ..userId = 'user-id'
      ..canvasContext = 'canvas-context'
      ..color = Color(0xFF00ACEC));

    final expected = userColor.rebuild((b) => b..id = 123);

    // Mock no existing item, i.e. return nothing for 'getByContext'
    when(database.query(
      any,
      columns: UserColorsDb.allColumns,
      where:
          '${UserColorsDb.columnUserDomain} = ? AND ${UserColorsDb.columnUserId} = ? AND ${UserColorsDb.columnCanvasContext} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    // Mock inserted item
    when(database.query(
      any,
      columns: UserColorsDb.allColumns,
      where: '${UserColorsDb.columnId} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => [UserColorsDb.toMap(expected)]);

    var actual = await UserColorsDb().insertOrUpdate(userColor);

    expect(actual, expected);

    verify(database.insert(UserColorsDb.tableName, UserColorsDb.toMap(userColor)));
  });

  test('insertOrUpdate performs update if there is an existing item, and returns updated item', () async {
    Color oldColor = Color(0xFF00ACEC);
    Color newColor = Color(0xFFACEC00);

    final userColor = UserColor((b) => b
      ..userDomain = 'domain'
      ..userId = 'user-id'
      ..canvasContext = 'canvas-context'
      ..color = newColor);

    final existing = userColor.rebuild((b) => b
      ..id = 123
      ..color = oldColor);

    final expected = userColor.rebuild((b) => b..id = 123);

    // Mock existing item
    when(database.query(
      any,
      columns: UserColorsDb.allColumns,
      where:
          '${UserColorsDb.columnUserDomain} = ? AND ${UserColorsDb.columnUserId} = ? AND ${UserColorsDb.columnCanvasContext} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => [UserColorsDb.toMap(existing)]);

    // Mock updated item
    when(database.query(
      any,
      columns: UserColorsDb.allColumns,
      where: '${UserColorsDb.columnId} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => [UserColorsDb.toMap(expected)]);

    var actual = await UserColorsDb().insertOrUpdate(userColor);

    expect(actual, expected);

    verify(database.update(
      UserColorsDb.tableName,
      UserColorsDb.toMap(expected),
      where: '${UserColorsDb.columnId} = ?',
      whereArgs: [123],
    ));
  });

  test('getById queries the database', () async {
    final userColorId = 123;

    when(database.query(
      any,
      columns: anyNamed('columns'),
      where: anyNamed('where'),
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    await UserColorsDb().getById(userColorId);

    verify(database.query(
      UserColorsDb.tableName,
      columns: UserColorsDb.allColumns,
      where: '${UserColorsDb.columnId} = ?',
      whereArgs: [userColorId],
    ));
  });

  test('getByContext queries the database', () async {
    final domain = 'domain';
    final userId = 'userId';
    final canvasContext = 'user_123';

    when(database.query(
      any,
      columns: anyNamed('columns'),
      where: anyNamed('where'),
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    await UserColorsDb().getByContext(domain, userId, canvasContext);

    verify(database.query(
      UserColorsDb.tableName,
      columns: UserColorsDb.allColumns,
      where:
          '${UserColorsDb.columnUserDomain} = ? AND ${UserColorsDb.columnUserId} = ? AND ${UserColorsDb.columnCanvasContext} = ?',
      whereArgs: [domain, userId, canvasContext],
    ));
  });

  test('deleteById deletes from the database', () async {
    final userColorId = 123;

    await UserColorsDb().deleteById(userColorId);

    verify(database.delete(
      UserColorsDb.tableName,
      where: '${UserColorsDb.columnId} = ?',
      whereArgs: [userColorId],
    ));
  });

  test('deleteAllForUser deletes from the database', () async {
    final domain = 'domain';
    final userId = 'userId';

    await UserColorsDb().deleteAllForUser(domain, userId);

    verify(database.delete(
      UserColorsDb.tableName,
      where: '${UserColorsDb.columnUserDomain} = ? AND ${UserColorsDb.columnUserId} = ?',
      whereArgs: [domain, userId],
    ));
  });

  test('parseColor parses 6 digit CSS colors', () {
    String colorString = '#00ACEC';
    Color expectedColor = Color(0xFF00ACEC);

    expect(UserColorsDb.parseColor(colorString), expectedColor);
  });

  test('insertOrUpdateAll inserts all colors', () async {
    String domain = 'domain';
    String userId = 'user-id';

    UserColors colors = UserColors((b) => b
      ..customColors = MapBuilder({
        'student_1': '#00ACEC',
        'student_2': '#ACEC00',
        'student_3': '#EC00AC',
      }));

    // Mock no existing items, i.e. return nothing for 'getByContext'
    when(database.query(
      any,
      columns: UserColorsDb.allColumns,
      where:
          '${UserColorsDb.columnUserDomain} = ? AND ${UserColorsDb.columnUserId} = ? AND ${UserColorsDb.columnCanvasContext} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    // Mock inserted items
    when(database.query(
      any,
      columns: UserColorsDb.allColumns,
      where: '${UserColorsDb.columnId} = ?',
      whereArgs: anyNamed('whereArgs'),
    )).thenAnswer((_) async => []);

    await UserColorsDb().insertOrUpdateAll(domain, userId, colors);

    for (var entry in colors.customColors.entries) {
      final userColor = UserColor((b) => b
        ..userDomain = 'domain'
        ..userId = 'user-id'
        ..canvasContext = entry.key
        ..color = UserColorsDb.parseColor(entry.value));

      verify(database.insert(UserColorsDb.tableName, UserColorsDb.toMap(userColor)));
    }
  });
}
