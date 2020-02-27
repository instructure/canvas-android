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
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/seed_context.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/api/dataseeding/course_seed_api.dart';
import 'package:flutter_parent/network/api/dataseeding/enrollment_seed_api.dart';
import 'package:flutter_parent/network/api/dataseeding/user_seed_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

// A data class to encapsulate all information created by the seed() call.
class SeedResults {
  List<SeededUser> students = [];
  List<SeededUser> teachers = [];
  List<SeededUser> parents = [];
  List<Course> courses = [];
}

// Some app-side helper logic to abstract the "GetSeedContext" app call logic and
// mark data seeding as complete.
class AppSeedUtils {
  // Initial seeding context: Not yet completed
  static SeedContext _seedContext = SeedContext((b) => b
    ..seedingComplete = false
    ..build());

  // The listener/handler to pass to enableFlutterDriverExtension()
  static DataHandler seedContextListener = (String message) async {
    if (message == "GetSeedContext") {
      return json.encode(serialize(_seedContext));
    }
  };

  // Lets the test driver know that data seeding has completed.
  static void markSeedingComplete(MapBuilder<String, String> seedObjects) {
    _seedContext = SeedContext((b) => b
      ..seedingComplete = true
      ..seedObjects = seedObjects
      ..build());
  }

  // Generic data seeding utility.
  // Sets up a parent, a teacher, a specified number of courses and students,
  // and enrolls parent+teacher+students in all courses.  It is assumed that
  // all students are children / observees of the parent.
  static Future<SeedResults> seed({int nStudents = 1, int nCourses = 1}) async {
    SeedResults result = SeedResults();
    result.parents.add(await UserSeedApi.createUser());
    result.teachers.add(await UserSeedApi.createUser());

    for (int i = 0; i < nStudents; i++) {
      var newStudent = await UserSeedApi.createUser();
      result.students.add(newStudent);
    }

    // TODO: A different path where courses could be exclusive to students.
    // I.e., create nCourses courses for each student.
    for (int i = 0; i < nCourses; i++) {
      var newCourse = await CourseSeedApi.createCourse();
      result.courses.add(newCourse);

      await EnrollmentSeedApi.createEnrollment(
          result.teachers.first.id, newCourse.id, "TeacherEnrollment", "");
      for (int i = 0; i < result.students.length; i++) {
        await EnrollmentSeedApi.createEnrollment(
            result.students.elementAt(i).id,
            newCourse.id,
            "StudentEnrollment",
            "");
        await EnrollmentSeedApi.createEnrollment(
            result.parents.first.id,
            newCourse.id,
            "ObserverEnrollment",
            result.students.elementAt(i).id);
      }
    }

    return result;
  }

  // Signs in a user via ApiPrefs.  That user should then be the reference
  // user when the initial screen starts.
  static Future signIn(SeededUser user) async {
    var parentLogin = user.toLogin();
    await ApiPrefs.addLogin(parentLogin);
    await ApiPrefs.switchLogins(parentLogin);
  }
}
