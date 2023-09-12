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
import 'package:flutter_parent/models/dataseeding/quiz.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:test/test.dart';

import 'driver_seed_utils.dart';
import 'pages/course_details_page.dart';
import 'pages/course_summary_page.dart';
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

  // An end-to-end test for the course summary page.
  // Seeds an assignment, a quiz and a calendar event, and
  // verifies that they show up correctly on the summary page.
  test('Summary E2E', () async {
    // Wait for seeding to complete
    var seedContext = (await DriverSeedUtils.waitForSeedingToComplete(driver))!;

    print("driver: Seeding complete!");
    var parent = seedContext.getNamedObject<SeededUser>("parent")!;
    var course = seedContext.getNamedObject<Course>("course")!;
    var assignment = seedContext.getNamedObject<Assignment>("assignment")!;
    var quiz = seedContext.getNamedObject<Quiz>("quiz")!;
    var event = seedContext.getNamedObject<ScheduleItem>("event")!;

    // Let's check that all of our assignments, quizzes and announcements are displayed
    await DashboardPage.waitForRender(driver);
    await DashboardPage.selectCourse(driver, course);
    await CourseDetailsPage.selectSummary(driver);

    // Check that our various items are present
    // (Will also click-through to verify that the details pages are shown correctly.)
    await CourseSummaryPage.verifyAssignmentPresent(driver, assignment);
    await CourseSummaryPage.verifyQuizPresent(driver, quiz);
    await CourseSummaryPage.verifyEventPresent(driver, event);
  }, timeout: Timeout(Duration(seconds: 90))); // Change timeout from 30 sec default to 90 secs
}
