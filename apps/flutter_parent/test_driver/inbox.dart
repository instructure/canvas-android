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
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

import 'apis/assignment_seed_api.dart';
import 'app_seed_utils.dart';

void main() async {
  enableFlutterDriverExtension(handler: AppSeedUtils.seedContextListener);

  // Initialize our ApiPrefs
  await ApiPrefs.init();

  // Seed our data
  var data = await AppSeedUtils.seed(nStudents: 1, nCourses: 1);
  var parent = data.parents.first;
  var teacher = data.teachers.first;
  var course = data.courses.first;
  var student = data.students.first;

  // Seed an assignment
  // Graded
  var assignment = (await AssignmentSeedApi.createAssignment(course.id, dueAt: DateTime.now().add(Duration(days: 3)).toUtc()))!;

  // Sign in the parent
  await AppSeedUtils.signIn(parent);

  // Seed a conversation
  await Future.delayed(const Duration(seconds: 5));
  var newConversation = (await InboxApi().createConversation(course.id, [teacher.id], "sUp?", "Let's talk", null))!;
  newConversation = newConversation.rebuild((b) => b..contextName = course.name); // Do this manually

  // Let the test driver know that seeding has completed
  AppSeedUtils.markSeedingComplete(MapBuilder({
    "parent": json.encode(serialize(parent)),
    "student": json.encode(serialize(student)),
    "teacher": json.encode(serialize(teacher)),
    "course": json.encode(serialize(course)),
    "conversation": json.encode(serialize(newConversation)),
    "assignment": json.encode(serialize(assignment)),
  }));

  // Call app.main(), which should bring up the dashboard.
  app.main();
}
