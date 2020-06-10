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

import 'package:flutter_student_embed/utils/db/db_util.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite/sqflite.dart';

import '../../testutils/mock_helpers.dart';

void main() {
  // For coverage
  test('init throws FlutterError in unit test', () async {
    expect(() => DbUtil.init(), throwsFlutterError);
  });

  test('instance throws StateError if not initialized', () async {
    expect(() => DbUtil.instance, throwsStateError);
  });

  test('onCreate calls creators', () async {
    Database database = MockDatabase();
    int databaseVersion = 123;

    int creatorCount = 10;
    int creatorCalls = 0;
    DbUtil.creators = List.generate(
      creatorCount,
      (index) => (db, dbVersion) {
        expect(db, database);
        expect(dbVersion, databaseVersion);
        creatorCalls++;
      },
    );

    await DbUtil.onCreate(database, databaseVersion);
    expect(creatorCalls, creatorCount);
  });

  test('onUpgrade calls updaters', () async {
    Database database = MockDatabase();
    int oldDatabaseVersion = 0;
    int newDatabaseVersion = 123;

    int updaterCount = 10;
    int updateCalls = 0;
    DbUtil.updaters = List.generate(
      updaterCount,
      (index) => (db, oldVersion, newVersion) {
        expect(db, database);
        expect(oldVersion, oldDatabaseVersion);
        expect(newVersion, newDatabaseVersion);
        updateCalls++;
      },
    );

    await DbUtil.onUpgrade(database, oldDatabaseVersion, newDatabaseVersion);
    expect(updateCalls, updaterCount);
  });
}
