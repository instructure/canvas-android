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

import 'package:sqflite/sqflite.dart';

import 'calendar_filter_db.dart';

List<OnDatabaseCreateFn> _creators = [
  CalendarFilterDb.createTable,
];

List<OnDatabaseVersionChangeFn> _updaters = [
  CalendarFilterDb.updateTable,
];

class DbUtil {
  static const dbVersion = 2;
  static const dbName = 'canvas_student_flutter.db';

  static Database _db;

  static Database get instance {
    if (_db == null) throw StateError('DbUtil has not been initialized!');
    return _db;
  }

  static Future<void> init() async {
    _db = await openDatabase(dbName, version: dbVersion, onCreate: _onCreate, onUpgrade: _onUpgrade);
  }

  static Future<void> _onCreate(Database db, int version) async {
    _creators.forEach((creator) => creator(db, version));
  }

  static Future<void> _onUpgrade(Database db, int oldVersion, int newVersion) async {
    _updaters.forEach((updater) => updater(db, oldVersion, newVersion));
  }
}
