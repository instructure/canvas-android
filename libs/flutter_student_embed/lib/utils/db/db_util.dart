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

import 'package:flutter/cupertino.dart';
import 'package:sqflite/sqflite.dart';

import 'calendar_filter_db.dart';

class DbUtil {
  static const dbVersion = 2;
  static const dbName = 'canvas_student_flutter.db';

  @visibleForTesting
  static List<OnDatabaseCreateFn> creators = [
    CalendarFilterDb.createTable,
  ];

  @visibleForTesting
  static List<OnDatabaseVersionChangeFn> updaters = [
    CalendarFilterDb.updateTable,
  ];

  static Database _db;

  static Database get instance {
    if (_db == null) throw StateError('DbUtil has not been initialized!');
    return _db;
  }

  static Future<void> init() async {
    _db = await openDatabase(dbName, version: dbVersion, onCreate: onCreate, onUpgrade: onUpgrade);
  }

  @visibleForTesting
  static Future<void> onCreate(Database db, int version) async {
    creators.forEach((creator) => creator(db, version));
  }

  @visibleForTesting
  static Future<void> onUpgrade(Database db, int oldVersion, int newVersion) async {
    updaters.forEach((updater) => updater(db, oldVersion, newVersion));
  }
}
