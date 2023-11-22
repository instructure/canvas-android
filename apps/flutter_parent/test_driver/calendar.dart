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
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

import 'apis/assignment_seed_api.dart';
import 'apis/calendar_seed_api.dart';
import 'app_seed_utils.dart';

void main() async {
  enableFlutterDriverExtension(handler: AppSeedUtils.seedContextListener);

  // Initialize our ApiPrefs
  await ApiPrefs.init();

  // Create a parent, a student and 2 courses.
  var data = await AppSeedUtils.seed(nParents: 1, nStudents: 1, nCourses: 2);
  var parent = data.parents[0];
  var student = data.students[0];
  var course1 = data.courses[0];
  var course2 = data.courses[1];
  Assignment assignment1 = (await AssignmentSeedApi.createAssignment(course1.id, dueAt: DateTime.now().add(Duration(days: 1)).toUtc()))!;
  Assignment assignment2 = (await AssignmentSeedApi.createAssignment(course2.id, dueAt: DateTime.now().subtract(Duration(days: 1)).toUtc()))!;
  ScheduleItem event2 = (await CalendarSeedApi.createCalendarEvent(course2.id, "Calendar Event", DateTime.now().toUtc(), allDay: true, locationName: "Location Name", locationAddress: "Location Address"))!;
  // TODO: Add graded quiz

  // Sign in the parent
  await AppSeedUtils.signIn(parent);

  // Let the test driver know that seeding has completed
  AppSeedUtils.markSeedingComplete(MapBuilder({
    "parent": json.encode(serialize(parent)),
    "student": json.encode(serialize(student)),
    "course1": json.encode(serialize(course1)),
    "course2": json.encode(serialize(course2)),
    "assignment1": json.encode(serialize(assignment1)),
    "assignment2": json.encode(serialize(assignment2)),
    "event2": json.encode(serialize(event2)),
  }));

  // Call app.main(), which should bring up the dashboard.
  app.main();
}
