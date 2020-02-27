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

import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:flutter_driver/driver_extension.dart';
import 'package:flutter_parent/main.dart' as app;
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

import 'app_seed_utils.dart';

void main() async {
  enableFlutterDriverExtension(handler: AppSeedUtils.seedContextListener);

  // Initialize our ApiPrefs
  await ApiPrefs.init();

  // Seed our data
  var data = await AppSeedUtils.seed(nStudents: 2, nCourses: 2);

  // Sign in the parent
  await AppSeedUtils.signIn(data.parents.first);

  // Let the test driver know that seeding has completed
  AppSeedUtils.markSeedingComplete(MapBuilder({
    "parent": json.encode(serialize(data.parents.first)),
    "student1": json.encode(serialize(data.students[0])),
    "student2": json.encode(serialize(data.students[1])),
    "course1": json.encode(serialize(data.courses[0])),
    "course2": json.encode(serialize(data.courses[1]))
  }));

  // Call app.main(), which should bring up the dashboard.
  app.main();
}
