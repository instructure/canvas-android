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

import 'dart:ui';

import 'package:flutter_parent/models/user_color.dart';
import 'package:flutter_parent/models/user_colors.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:sqflite/sqflite.dart';

class UserColorsDb {
  static const String tableName = 'user_colors';

  static const String columnId = '_id';
  static const String columnUserDomain = 'user_domain';
  static const String columnUserId = 'user_id';
  static const String columnCanvasContext = 'canvas_context';
  static const String columnColor = 'color';

  static const allColumns = [
    columnId,
    columnUserDomain,
    columnUserId,
    columnCanvasContext,
    columnColor,
  ];

  Database db = locator<Database>();

  static Map<String, dynamic> toMap(UserColor userColor) => {
        columnId: userColor.id,
        columnUserDomain: userColor.userDomain,
        columnUserId: userColor.userId,
        columnCanvasContext: userColor.canvasContext,
        columnColor: userColor.color.value,
      };

  static UserColor fromMap(Map<dynamic, dynamic> map) => UserColor((b) => b
    ..id = map[columnId]
    ..userDomain = map[columnUserDomain]
    ..userId = map[columnUserId]
    ..canvasContext = map[columnCanvasContext]
    ..color = Color(map[columnColor]));

  static Future<void> createTable(Database db, int version) async {
    await db.execute('''
      create table $tableName ( 
        $columnId integer primary key autoincrement, 
        $columnUserDomain text not null,
        $columnUserId text not null,
        $columnCanvasContext text not null,
        $columnColor integer not null)
      ''');
  }

  static Future<void> updateTable(Database db, int oldVersion, int newVersion) async {
    if (oldVersion < 4) {
      // This table was added in database version 4
      await createTable(db, newVersion);
    }
  }

  Future<UserColor?> getById(int id) async {
    List<Map> maps = await db.query(tableName, columns: allColumns, where: '$columnId = ?', whereArgs: [id]);
    if (maps.isNotEmpty) return fromMap(maps.first);
    return null;
  }

  Future<void> insertOrUpdateAll(String? domain, String? userId, UserColors colors) async {
    for (var entry in colors.customColors.entries) {
      await insertOrUpdate(UserColor((b) => b
        ..userDomain = domain
        ..userId = userId
        ..canvasContext = entry.key
        ..color = parseColor(entry.value)));
    }
  }

  static Color parseColor(String hexCode) => Color(int.parse('FF${hexCode.substring(1)}', radix: 16));

  Future<UserColor?> insertOrUpdate(UserColor data) async {
    UserColor? existing = await getByContext(data.userDomain, data.userId, data.canvasContext);
    if (existing == null) {
      var id = await db.insert(tableName, toMap(data));
      return getById(id);
    } else {
      data = data.rebuild((b) => b..id = existing.id);
      var id = await db.update(tableName, toMap(data), where: '$columnId = ?', whereArgs: [existing.id]);
      return getById(id);
    }
  }

  Future<UserColor?> getByContext(String? userDomain, String? userId, String canvasContext) async {
    List<Map> maps = await db.query(
      tableName,
      columns: allColumns,
      where: '$columnUserDomain = ? AND $columnUserId = ? AND $columnCanvasContext = ?',
      whereArgs: [userDomain, userId, canvasContext],
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
