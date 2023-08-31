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
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:sqflite/sqflite.dart';

class ReminderDb {
  static const String tableName = 'reminders';

  static const String columnId = '_id';
  static const String columnUserDomain = 'user_domain';
  static const String columnUserId = 'user_id';
  static const String columnType = 'type';
  static const String columnItemId = 'item_id';
  static const String columnCourseId = 'course_id';
  static const String columnDate = 'date';

  static const allColumns = [
    columnId,
    columnUserDomain,
    columnUserId,
    columnType,
    columnItemId,
    columnCourseId,
    columnDate,
  ];

  Database db = locator<Database>();

  static Map<String, dynamic> toMap(Reminder data) => {
        columnId: data.id,
        columnUserDomain: data.userDomain,
        columnUserId: data.userId,
        columnType: data.type,
        columnItemId: data.itemId,
        columnCourseId: data.courseId,
        columnDate: data.date?.toIso8601String(),
      };

  static Reminder fromMap(Map<dynamic, dynamic> map) => Reminder((b) => b
    ..id = map[columnId]
    ..userDomain = map[columnUserDomain]
    ..userId = map[columnUserId]
    ..type = map[columnType]
    ..itemId = map[columnItemId]
    ..courseId = map[columnCourseId]
    ..date = DateTime.parse(map[columnDate]));

  static Future<void> createTable(Database db, int version) async {
    await db.execute('''
      create table $tableName ( 
        $columnId integer primary key autoincrement, 
        $columnUserDomain text not null,
        $columnUserId text not null,
        $columnType text not null,
        $columnItemId text not null,
        $columnCourseId text not null,
        $columnDate text not null )
      ''');
  }

  static Future<void> updateTable(Database db, int oldVersion, int newVersion) async {
    if (oldVersion < 3) {
      // Course ID column added in database version 3
      await db.execute('alter table $tableName add column $columnCourseId text not null default \'\'');
    }
  }

  Future<Reminder?> insert(Reminder data) async {
    var id = await db.insert(tableName, toMap(data));
    return getById(id);
  }

  Future<Reminder?> getById(int id) async {
    List<Map> maps = await db.query(tableName, columns: allColumns, where: '$columnId = ?', whereArgs: [id]);
    if (maps.isNotEmpty) return fromMap(maps.first);
    return null;
  }

  Future<Reminder?> getByItem(String? userDomain, String? userId, String? type, String? itemId) async {
    List<Map> maps = await db.query(
      tableName,
      columns: allColumns,
      where: '$columnUserDomain = ? AND $columnUserId = ? AND $columnType = ? AND $columnItemId = ?',
      whereArgs: [userDomain, userId, type, itemId],
    );
    if (maps.isNotEmpty) return fromMap(maps.first);
    return null;
  }

  Future<List<Reminder>>? getAllForUser(String? userDomain, String? userId) async {
    List<Map> maps = await db.query(
      tableName,
      columns: allColumns,
      where: '$columnUserDomain = ? AND $columnUserId = ?',
      whereArgs: [userDomain, userId],
    );
    return maps.map((it) => fromMap(it)).toList();
  }

  Future<int> deleteById(int? id) {
    return db.delete(tableName, where: '$columnId = ?', whereArgs: [id]);
  }

  Future<int> deleteAllForUser(String? userDomain, String? userId) {
    return db.delete(
      tableName,
      where: '$columnUserDomain = ? AND $columnUserId = ?',
      whereArgs: [userDomain, userId],
    );
  }
}
