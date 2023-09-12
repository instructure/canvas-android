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
import 'pages/dashboard_page.dart';
import 'pages/manage_students_page.dart';

void main() {
  FlutterDriver? driver;

  // Connect to the Flutter driver before running any tests.
  setUpAll(() async {
    driver = await FlutterDriver.connect();
  });

  // Close the connection to the driver after the tests have completed.
  tearDownAll(() async {
    if (driver != null) {
      driver?.close();
    }
  });

  // Tests that the "Manage Students" page shows students and allows for the addition of a new student.
  // Does NOT test anything related to alerts or alert settings; that will be the subject of another test.
  test('Manage Students E2E', () async {
    // Wait for seeding to complete
    var seedContext = (await DriverSeedUtils.waitForSeedingToComplete(driver))!;

    print("driver: Seeding complete!");

    // Read in our seeded data
    var students = [
      seedContext.getNamedObject<SeededUser>("student1")!,
      seedContext.getNamedObject<SeededUser>("student2")!
    ];
    var courses = [
      seedContext.getNamedObject<Course>("course1")!,
      seedContext.getNamedObject<Course>("course2")!,
    ];
    var parent = seedContext.getNamedObject<SeededUser>("parent")!;
    var pairingCode = seedContext.seedObjects["pairingCode2"]!; // Direct string fetch

    // Verify that student[0] and course[0] show up on the main dashboard page
    await DashboardPage.waitForRender(driver);
    await DashboardPage.verifyStudentDisplayed(driver, students[0]);
    await DashboardPage.verifyCourse(driver, courses[0]);

    // Open the "Manage Students" pagee
    await DashboardPage.openManageStudents(driver);

    // Verify that the first student is showing already
    await ManageStudentsPage.verifyStudentDisplayed(driver, students[0]);

    // Add the second
    await ManageStudentsPage.addStudent(driver, pairingCode);

    // Verify that both are showing now
    await ManageStudentsPage.verifyStudentDisplayed(driver, students[0]);
    await ManageStudentsPage.verifyStudentDisplayed(driver, students[1]);

    // Back to main dashboard
    await driver?.tap(find.pageBack());

    // Switch students and verify that new student's course is showing
    await DashboardPage.changeStudent(driver, students[1]);
    await DashboardPage.verifyCourse(driver, courses[1]);
  }, timeout: Timeout(Duration(seconds: 90))); // Change timeout from 30 sec default to 90 secs
}
