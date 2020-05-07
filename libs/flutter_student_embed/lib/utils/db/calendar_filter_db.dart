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
import 'package:flutter_student_embed/models/calendar_filter.dart';
import 'package:sqflite/sqflite.dart';

import '../service_locator.dart';

class CalendarFilterDb {
  static const String tableName = 'calendar_filter';

  static const String columnId = '_id';
  static const String columnUserDomain = 'user_domain';
  static const String columnUserId = 'user_id';
  static const String columnFilters = 'filters';

  static const allColumns = [
    columnId,
    columnUserDomain,
    columnUserId,
    columnFilters,
  ];

  Database db = locator<Database>();

  static Map<String, dynamic> toMap(CalendarFilter data) => {
        columnId: data.id,
        columnUserDomain: data.userDomain,
        columnUserId: data.userId,
        columnFilters: joinFilters(data.filters.toSet()),
      };

  static CalendarFilter fromMap(Map<String, dynamic> map) => CalendarFilter((b) => b
    ..id = map[columnId]
    ..userDomain = map[columnUserDomain]
    ..userId = map[columnUserId]
    ..filters = SetBuilder(splitFilters(map[columnFilters])));

  static Future<void> createTable(Database db, int version) async {
    await db.execute('''
      create table $tableName ( 
        $columnId integer primary key autoincrement, 
        $columnUserDomain text not null,
        $columnUserId text not null,
        $columnFilters text not null)
      ''');
  }

  static Future<void> updateTable(Database db, int oldVersion, int newVersion) async {
    if (oldVersion < 2) {
      // This table was added in database version 2
      await db.execute('''
        create table $tableName ( 
          $columnId integer primary key autoincrement, 
          $columnUserDomain text not null,
          $columnUserId text not null,
          $columnFilters text not null)
        ''');
    }
  }

  static String joinFilters(Set<String> filters) {
    if (filters == null || filters.isEmpty) return '';
    return filters.join('|');
  }

  static Set<String> splitFilters(String joinedFilters) {
    if (joinedFilters == null || joinedFilters.isEmpty) return {};
    return joinedFilters.split('|').toSet();
  }

  Future<CalendarFilter> insertOrUpdate(CalendarFilter data) async {
    CalendarFilter existing = await getForUser(data.userDomain, data.userId);
    if (existing == null) {
      var id = await db.insert(tableName, toMap(data));
      return getById(id);
    } else {
      data = data.rebuild((b) => b..id = existing.id);
      var id = await db.update(tableName, toMap(data), where: '$columnId = ?', whereArgs: [existing.id]);
      return getById(id);
    }
  }

  Future<CalendarFilter> getById(int id) async {
    List<Map> maps = await db.query(tableName, columns: allColumns, where: '$columnId = ?', whereArgs: [id]);
    if (maps.isNotEmpty) return fromMap(maps.first);
    return null;
  }

  Future<CalendarFilter> getForUser(String userDomain, String userId) async {
    List<Map> maps = await db.query(
      tableName,
      columns: allColumns,
      where: '$columnUserDomain = ? AND $columnUserId = ?',
      whereArgs: [userDomain, userId],
    );
    if (maps.isNotEmpty) return fromMap(maps.first);
    return null;
  }

  Future<int> deleteById(int id) {
    return db.delete(tableName, where: '$columnId = ?', whereArgs: [id]);
  }

  Future<int> deleteAllForUser(String userDomain, String userId) {
    return db.delete(
      tableName,
      where: '$columnUserDomain = ? AND $columnUserId = ?',
      whereArgs: [userDomain, userId],
    );
  }
}
