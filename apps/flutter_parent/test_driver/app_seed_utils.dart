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
import 'package:flutter_parent/network/utils/api_prefs.dart';

import 'apis/course_seed_api.dart';
import 'apis/enrollment_seed_api.dart';
import 'apis/user_seed_api.dart';

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
  static DataHandler seedContextListener = (String? message) async {
    if (message == "GetSeedContext") {
      return json.encode(serialize(_seedContext));
    }
    return '';
  };

  // Lets the test driver know that data seeding has completed.
  static void markSeedingComplete(MapBuilder<String, String> seedObjects) {
    _seedContext = SeedContext((b) => b
      ..seedingComplete = true
      ..seedObjects = seedObjects
      ..build());
  }

  /// Generic data seeding utility.
  /// Sets up a teacher, a specified number of parents, courses and students,
  /// and enrolls teacher+students in all courses.  It is assumed that
  /// all students are children / observees of the parent(s), and they will
  /// be paired accordingly.
  static Future<SeedResults> seed({int nParents = 1, int nStudents = 1, int nCourses = 1}) async {
    SeedResults result = SeedResults();

    // Create nParents parents
    for (int i = 0; i < nParents; i++) {
      result.parents.add((await UserSeedApi.createUser())!);
    }

    // Create a single teacher
    result.teachers.add((await UserSeedApi.createUser())!);

    // Create nStudents students
    for (int i = 0; i < nStudents; i++) {
      var newStudent = await UserSeedApi.createUser();
      result.students.add(newStudent!);
    }

    // Enroll all students and teachers in all courses.
    for (int i = 0; i < nCourses; i++) {
      var newCourse = (await CourseSeedApi.createCourse())!;
      result.courses.add(newCourse);

      await EnrollmentSeedApi.createEnrollment(result.teachers.first!.id, newCourse.id, "TeacherEnrollment", "");
      for (int i = 0; i < result.students.length; i++) {
        await EnrollmentSeedApi.createEnrollment(
            result.students.elementAt(i).id, newCourse.id, "StudentEnrollment", "");
      }
    }

    // Now that student users are enrolled as students, we can pair them up with parents via pairing codes.
    for (SeededUser parent in result.parents) {
      for (SeededUser student in result.students) {
        var pairingResult = await seedPairing(parent, student);
        print('internal pairingResult: $pairingResult');
      }
    }

    return result;
  }

  /// Create a course and enroll any indicated teachers, parents or students.
  /// Allows you a little more flexibility in setting up a course / enrollment than is allowed by
  /// seed() above.
  static Future<Course> seedCourseAndEnrollments(
      {SeededUser? parent = null, SeededUser? student = null, SeededUser? teacher = null}) async {
    var newCourse = (await CourseSeedApi.createCourse())!;

    if (parent != null && student != null) {
      await EnrollmentSeedApi.createEnrollment(parent.id, newCourse.id, "ObserverEnrollment", student.id);
    }

    if (teacher != null) {
      await EnrollmentSeedApi.createEnrollment(teacher.id, newCourse.id, "TeacherEnrollment", "");
    }

    if (student != null) {
      await EnrollmentSeedApi.createEnrollment(student.id, newCourse.id, "StudentEnrollment", "");
    }

    return newCourse;
  }

  /// Pair a parent and a student.  Will only work if student is enrolled as a student.
  static Future<bool> seedPairing(SeededUser parent, SeededUser student) async {
    var pairingCodeStructure = await UserSeedApi.createObserverPairingCode(student.id);
    var pairingCode = pairingCodeStructure?.code;
    var pairingResult = await UserSeedApi.addObservee(parent, student, pairingCode);
    return pairingResult;
  }

  // Signs in a user via ApiPrefs.  That user should then be the reference
  // user when the initial screen starts.
  static Future signIn(SeededUser user) async {
    var parentLogin = user.toLogin();
    await ApiPrefs.addLogin(parentLogin);
    await ApiPrefs.switchLogins(parentLogin);
  }
}
