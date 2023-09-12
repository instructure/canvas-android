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

import 'apis/user_seed_api.dart';
import 'app_seed_utils.dart';

void main() async {
  enableFlutterDriverExtension(handler: AppSeedUtils.seedContextListener);

  // Initialize our ApiPrefs
  await ApiPrefs.init();

  // Create parent and two students, one of which is paired to the parent.
  var parent = (await UserSeedApi.createUser())!;
  var student1 = (await UserSeedApi.createUser())!;
  var student2 = (await UserSeedApi.createUser())!;
  var teacher = (await UserSeedApi.createUser())!;
  var course1 = await AppSeedUtils.seedCourseAndEnrollments(student: student1, teacher: teacher);
  var course2 = await AppSeedUtils.seedCourseAndEnrollments(student: student2, teacher: teacher);
  await AppSeedUtils.seedPairing(parent, student1);

  // Get a pairing code for student2
  var pairingCodeStructure = await UserSeedApi.createObserverPairingCode(student2.id);
  var pairingCode = pairingCodeStructure?.code;
  print("PAIRING CODE: $pairingCode");

  // Sign in the parent
  await AppSeedUtils.signIn(parent);

  // Let the test driver know that seeding has completed
  AppSeedUtils.markSeedingComplete(MapBuilder({
    "parent": json.encode(serialize(parent)),
    "student1": json.encode(serialize(student1)),
    "student2": json.encode(serialize(student2)),
    "course1": json.encode(serialize(course1)),
    "course2": json.encode(serialize(course2)),
    "pairingCode2": pairingCode,
  }));

  // Call app.main(), which should bring up the dashboard.
  app.main();
}
