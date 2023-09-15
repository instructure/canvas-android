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
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:test/test.dart';

import 'driver_seed_utils.dart';
import 'pages/assignment_details_page.dart';
import 'pages/course_grades_page.dart';
import 'pages/dashboard_page.dart';

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

  test('Grades+Assignments E2E', () async {
    // Wait for seeding to complete
    var seedContext = (await DriverSeedUtils.waitForSeedingToComplete(driver))!;

    print("driver: Seeding complete!");
    var course = seedContext.getNamedObject<Course>("course")!;
    var assignments = [
      seedContext.getNamedObject<Assignment>("assignment1")!,
      seedContext.getNamedObject<Assignment>("assignment2")!,
      seedContext.getNamedObject<Assignment>("assignment3")!,
      seedContext.getNamedObject<Assignment>("assignment4")!
    ];

    // Assignment-specific data
    var expectedStatuses = ["missing", "not submitted", "submitted", "submitted"];
    var expectedGrades = ["-", "-", "-", "19"];

    // apparently this is important
    await DashboardPage.waitForRender(driver);

    // Verify that we are showing the correct grade for the course
    await DashboardPage.verifyCourse(driver, course, grade: "95%");

    // Select our course
    await DashboardPage.selectCourse(driver, course); // Why wouldn't swipe-to-refresh work instead of the above?

    // We're now on the assignments/grades list.

    // Make sure that our total grade is correct
    await CourseGradesPage.verifyTotalGradeContains(driver, "95");

    // Verify that each assignment is present.
    for (int i = 0; i < assignments.length; i++) {
      var a = assignments[i];
      await CourseGradesPage.verifyAssignment(driver, a, grade: expectedGrades[i], status: expectedStatuses[i]);
    }

    // For each assignment, open the assignment details page and verify its correctness
    await CourseGradesPage.selectAssignment(driver, assignments[0]);
    await AssignmentDetailsPage.validateUnsubmittedAssignment(driver, assignments[0]);
    await driver?.tap(find.pageBack());

    await CourseGradesPage.selectAssignment(driver, assignments[1]);
    await AssignmentDetailsPage.validateUnsubmittedAssignment(driver, assignments[1]);
    await driver?.tap(find.pageBack());

    await CourseGradesPage.selectAssignment(driver, assignments[2]);
    await AssignmentDetailsPage.validateSubmittedAssignment(driver, assignments[2]);
    await driver?.tap(find.pageBack());

    await CourseGradesPage.selectAssignment(driver, assignments[3]);
    await AssignmentDetailsPage.validateGradedAssignment(driver, assignments[3], "19");
    await driver?.tap(find.pageBack());
  }, timeout: Timeout(Duration(minutes: 2)));
}
