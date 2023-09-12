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
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:test/test.dart';

import 'driver_seed_utils.dart';
import 'pages/assignment_details_page.dart';
import 'pages/calendar_page.dart';
import 'pages/dashboard_page.dart';

// Run test with command: flutter drive --target=test_driver/calendar.dart
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

  // An end-to-end test for the calendar/planner.
  // Seeds a couple of assignments, a quiz and an announcement and
  // verifies that they show up on the calendar.  Also tests course
  // filtering.
  //
  // I have added some logic to allow the test to succeed on weekends --
  // it can scroll to next week or last week to find tomorrow's/yesterday's
  // assignments if need be.  But normally this should not be necessary,
  // as we usually run the test M-F.
  test('Calendar E2E', () async {
    // Wait for seeding to complete
    var seedContext = (await DriverSeedUtils.waitForSeedingToComplete(driver))!;

    print("driver: Seeding complete!");
    var parent = seedContext.getNamedObject<SeededUser>("parent");
    var student = seedContext.getNamedObject<SeededUser>("student");
    var courses = [seedContext.getNamedObject<Course>("course1")!, seedContext.getNamedObject<Course>("course2")!];
    var assignment1 = seedContext.getNamedObject<Assignment>("assignment1")!; // From first course
    var assignment2 = seedContext.getNamedObject<Assignment>("assignment2")!; // From second course
    var event2 = seedContext.getNamedObject<ScheduleItem>("event2")!; // From second course

    // Let's check that all of our assignments, quizzes and announcements are displayed
    await DashboardPage.waitForRender(driver);
    await DashboardPage.goToCalendar(driver);
    await CalendarPage.waitForRender(driver);
    await CalendarPage.verifyAssignmentDisplayed(driver, assignment1);
    await CalendarPage.verifyAssignmentDisplayed(driver, assignment2);
    await CalendarPage.verifyEventDisplayed(driver, event2);

    // Let's try opening an assignment
    await CalendarPage.openAssignment(driver, assignment1);
    await AssignmentDetailsPage.validateUnsubmittedAssignment(driver, assignment1);
    await driver?.tap(find.pageBack());

    // Let's filter out the first course and try again
    await CalendarPage.toggleFilter(driver, courses[0]);
    await CalendarPage.verifyAssignmentNotDisplayed(driver, assignment1);
    await CalendarPage.verifyAssignmentDisplayed(driver, assignment2);
    await CalendarPage.verifyEventDisplayed(driver, event2);

    // Let's re-enable the first course and filter out the second, and try again
    await CalendarPage.toggleFilter(driver, courses[0]);
    await CalendarPage.toggleFilter(driver, courses[1]);
    await CalendarPage.verifyAssignmentDisplayed(driver, assignment1);
    await CalendarPage.verifyAssignmentNotDisplayed(driver, assignment2);
    await CalendarPage.verifyEventNotDisplayed(driver, event2);
  }, timeout: Timeout(Duration(seconds: 90))); // Change timeout from 30 sec default to 90 secs
}
