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

// Imports the Flutter Driver API.

import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:test/test.dart';

import 'driver_seed_utils.dart';

void main() {
  FlutterDriver driver;

  // Connect to the Flutter driver before running any tests.
  setUpAll(() async {
    driver = await FlutterDriver.connect();
  });

  // Close the connection to the driver after the tests have completed.
  tearDownAll(() async {
    if (driver != null) {
      driver.close();
    }
  });

  test('Dashboard E2E', () async {
    // Wait for seeding to complete
    var seedContext = await DriverSeedUtils.waitForSeedingToComplete(driver);

    print("driver: Seeding complete!");
    var students = [
      seedContext.getNamedObject<SeededUser>("student1"),
      seedContext.getNamedObject<SeededUser>("student2")
    ];
    var courses = [
      seedContext.getNamedObject<Course>("course1"),
      seedContext.getNamedObject<Course>("course2")
    ];
    var parent = seedContext.getNamedObject<SeededUser>("parent");

    await driver.waitFor(
        find.byType("DashboardScreen"), timeout: Duration(seconds: 5));

    // Verify that our course names, codes and grades are listed
    await courses.forEach((course) async {
      var actualName = await driver.getText(
          find.byValueKey("${course.courseCode}_name"));
      expect(actualName, course.name);
      var actualCode = await driver.getText(
          find.byValueKey("${course.courseCode}_code"));
      expect(actualCode, course.courseCode);
      var actualGrade = await driver.getText(
          find.byValueKey("${course.courseCode}_grade"));
      expect(actualGrade,
          "No Grade"); // AppLocalizations().noGrade would pull in dart:ui
    });

    // Verify that first student is showing
    await driver.waitFor(find.text(students[0].shortName));

    // Let's open the student list expansion
    await driver.tap(find.byValueKey('student_expansion_touch_target'));

    // Verify that each of our students are on the student list
    await students.forEach((student) async {
      var actualName = await driver.getText(
          find.byValueKey("${student.shortName}_text"));
      expect(actualName, student.shortName);
    });

    // Now select the second student (which should close the student list expansion)
    await driver.tap(find.byValueKey("${students[1].shortName}_text"));
    await Future.delayed(
        Duration(seconds: 2)); // Wait for animation to complete.

    // And make sure that the second student is now displayed
    await driver.waitFor(find.text(students[1].shortName));

    // Now let's make sure that the drawer opens
    await driver.tap(find.byValueKey("drawer_menu"));

    // And the name of our parent is displayed
    await driver.waitFor(find.text(parent.name));
  }, timeout: Timeout(
      Duration(minutes: 1))); // Change timeout from 30 sec default to 1 min

}


