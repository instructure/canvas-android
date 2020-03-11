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

  test('Grades E2E', () async {
    // Wait for seeding to complete
    var seedContext = await DriverSeedUtils.waitForSeedingToComplete(driver);

    print("driver: Seeding complete!");
    var course = seedContext.getNamedObject<Course>("course");
    var assignments = [
      seedContext.getNamedObject<Assignment>("assignment1"),
      seedContext.getNamedObject<Assignment>("assignment2"),
      seedContext.getNamedObject<Assignment>("assignment3"),
      seedContext.getNamedObject<Assignment>("assignment4")
    ];

    await driver.waitFor(find.byType("DashboardScreen"), timeout: Duration(seconds: 5));

    await driver.tap(find.text(course.name));

    // Assignment-specific data
    var expectedStatuses = ["missing", "not submitted", "submitted", "submitted"];
    var expectedGrades = ["-", "-", "-", "19"];

    // We're now on the assignments/grades list.  Verify that each assignment is present.
    for (int i = 0; i < assignments.length; i++) {
      var a = assignments[i];
      //print("assignment: $a");
      await driver.scrollIntoView(find.byValueKey("assignment_${a.id}_row"));

      // Verify assignment name
      var assignmentName = await driver.getText(find.byValueKey("assignment_${a.id}_name"));
      //print("assignment name text = $assignmentName");
      expect(assignmentName, a.name, reason: "Expected assignment name to be ${a.name}");

      // Verify assignment status
      var assignmentStatus = await driver.getText(find.byValueKey("assignment_${a.id}_status"));
      expect(assignmentStatus.toLowerCase(), expectedStatuses[i].toLowerCase(),
          reason: "Expected status to be ${expectedStatuses[i]}");

      // Verify assignment grade
      var assignmentGrade = await driver.getText(find.byValueKey("assignment_${a.id}_grade"));
      //print("assignment grade text = $assignmentGrade");
      expect(assignmentGrade.contains(a.pointsPossible.toInt().toString()), true,
          reason: "Expected grade to contain ${a.pointsPossible.toInt()}");
      expect(assignmentGrade.contains(expectedGrades[i]), true,
          reason: "Expected grade to contain ${expectedGrades[i]}");
    }

    // Make sure that our total grade is correct
    var totalGradeFinder = find.byValueKey("total_grade");
    await driver.scrollIntoView(totalGradeFinder);
    var totalGrade = await driver.getText(totalGradeFinder);
    expect(totalGrade.contains("95"), true, reason: "Total grade should be 95");

    await Future.delayed(Duration(seconds: 3));
  }, timeout: Timeout(Duration(minutes: 1)));
}
