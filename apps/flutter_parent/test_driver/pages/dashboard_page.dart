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

import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:test/test.dart';

import '..//flutter_driver_extensions.dart';

class DashboardPage {
  static Future<void> verifyCourse(FlutterDriver? driver, Course course, {String? grade = null}) async {
    var actualName = await driver?.getTextWithRefreshes(find.byValueKey("${course.courseCode}_name"));
    expect(actualName, course.name);
    var actualCode = await driver?.getText(find.byValueKey("${course.courseCode}_code"));
    expect(actualCode, course.courseCode);
    if (grade != null) {
      var actualGrade =
          await driver?.getTextWithRefreshes(find.byValueKey("${course.courseCode}_grade"), expectedText: grade);
      expect(actualGrade, grade);
    }
  }

  static Future<void> verifyCourses(FlutterDriver? driver, List<Course> courses) async {
    courses.forEach((course) async {
      await verifyCourse(driver, course);
    });
  }

  static Future<void> selectCourse(FlutterDriver? driver, Course course) async {
    await driver?.tapWithRefreshes(find.text(course.name));
  }

  static Future<void> waitForRender(FlutterDriver? driver) async {
    print("Waiting for DashboardScreen to appear");
    await driver?.waitFor(find.byType("DashboardScreen"),
        timeout: Duration(seconds: 30)); // It can take a while sometimes...
  }

  static Future<void> verifyStudentDisplayed(FlutterDriver? driver, SeededUser student) async {
    await driver?.waitFor(find.text(student.shortName));
  }

  static Future<void> changeStudent(FlutterDriver? driver, SeededUser newStudent) async {
    // Open the student list expansion
    await driver?.tap(find.byValueKey('student_expansion_touch_target'));

    // Select the new student
    await driver?.tap(find.byValueKey("${newStudent.shortName}_text"));
    await Future.delayed(Duration(seconds: 1)); // Wait for animation to complete.
  }

  static Future<void> openNavDrawer(FlutterDriver? driver) async {
    await driver?.tap(find.byValueKey("drawer_menu"));
  }

  static Future<void> openInbox(FlutterDriver? driver) async {
    await openNavDrawer(driver);
    await driver?.tap(find.text("Inbox"));
  }

  static Future<void> openManageStudents(FlutterDriver? driver) async {
    await openNavDrawer(driver);
    await driver?.tap(find.text("Manage Students"));
  }

  static Future<void> goToCalendar(FlutterDriver? driver) async {
    await driver?.tap(find.text("Calendar"));
  }
}
