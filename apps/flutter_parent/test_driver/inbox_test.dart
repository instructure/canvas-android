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
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:test/test.dart';

import 'driver_seed_utils.dart';
import 'flutter_driver_extensions.dart';
import 'pages/assignment_details_page.dart';
import 'pages/conversation_create_page.dart';
import 'pages/conversation_details_page.dart';
import 'pages/conversation_list_page.dart';
import 'pages/course_details_page.dart';
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

  test('Inbox E2E', () async {
    // Wait for seeding to complete
    var seedContext = (await DriverSeedUtils.waitForSeedingToComplete(driver))!;

    print("driver: Seeding complete!");
    var parent = seedContext.getNamedObject<SeededUser>("parent")!;
    var student = seedContext.getNamedObject<SeededUser>("student")!;
    var course = seedContext.getNamedObject<Course>("course")!;
    var teacher = seedContext.getNamedObject<SeededUser>("teacher")!;
    var conversation = seedContext.getNamedObject<Conversation>("conversation")!;
    var assignment = seedContext.getNamedObject<Assignment>("assignment")!;

    // Verify that the pre-seeded conversation shows up
    await DashboardPage.waitForRender(driver);
    await DashboardPage.openInbox(driver);
    await ConversationListPage.verifyConversationDataDisplayed(driver, /*index*/ 0,
        partialSubjects: [conversation.subject],
        partialContexts: [conversation.contextName!],
        partialBodies: [conversation.lastMessage ?? conversation.lastAuthoredMessage!]);

    // Create a conversation from the Inbox
    await ConversationListPage.initiateCreateEmail(driver, course); // Will this work with only one course?
    await ConversationCreatePage.verifyRecipientListed(driver, teacher);
    await ConversationCreatePage.verifySubject(driver, course.name);
    await ConversationCreatePage.populateBody(driver, "Message 1 Body");
    await ConversationCreatePage.sendMail(driver); // Should send us back to conversation list

    // Verify that our new conversation shows up the conversation list
    await ConversationListPage.verifyConversationDataDisplayed(driver, /*index*/ 0,
        partialBodies: ['Message 1 Body'], partialSubjects: [course.name]);

    // Back to the dashboard
    await driver?.tap(find.pageBack());

    // Select a course and send a grades-related email
    await DashboardPage.selectCourse(driver, course);
    await CourseGradesPage.initiateCreateEmail(driver);
    await ConversationCreatePage.verifyRecipientListed(driver, teacher);
    await ConversationCreatePage.verifySubject(driver, 'Regarding: ${student.name}, Grades');
    await ConversationCreatePage.populateBody(driver, 'Grades Body');
    await ConversationCreatePage.sendMail(driver);

    // Go to the syllabus page and send an email
    await CourseDetailsPage.selectSyllabus(driver);
    await CourseDetailsPage.initiateCreateEmail(driver);
    await ConversationCreatePage.verifySubject(driver, 'Regarding: ${student.name}, Syllabus');
    await ConversationCreatePage.populateBody(driver, 'Syllabus Body');
    await ConversationCreatePage.sendMail(driver);

    // Back to grades/assignments tab
    CourseDetailsPage.selectGrades(driver);

    // Select an assignment and send an assignment-related email
    await CourseGradesPage.selectAssignment(driver, assignment);
    await AssignmentDetailsPage.initiateCreateEmail(driver);
    await ConversationCreatePage.verifyRecipientListed(driver, teacher);
    await ConversationCreatePage.verifySubject(driver, 'Regarding: ${student.name}, Assignment - ${assignment.name}');
    await ConversationCreatePage.populateBody(driver, 'Assignment Body');
    await ConversationCreatePage.sendMail(driver);

    await driver?.tap(find.pageBack()); // assignment details -> grades list
    await driver?.tap(find.pageBack()); // grades list -> dashboard

    await DashboardPage.openInbox(driver);
    await driver?.refresh(); // To make sure and load the latest emails
    await Future.delayed(const Duration(seconds: 2)); // Give ourselves a moment to load

    // Let's make sure that the three most recent emails that we created show up in our
    // conversation list.

    // The most recent email -- the assignment email -- should be on top (index 0)
    await ConversationListPage.verifyConversationDataDisplayed(driver, 0,
        partialSubjects: [student.name, assignment.name!],
        partialContexts: [course.name],
        partialBodies: ['Assignment Body', student.name]);

    // The syllabus email should be next (index 1)
    await ConversationListPage.verifyConversationDataDisplayed(driver, 1,
        partialSubjects: [student.name, "Syllabus"],
        partialContexts: [course.name],
        partialBodies: ['Syllabus Body', student.name]);

    // The grades email should be next (index 2)
    await ConversationListPage.verifyConversationDataDisplayed(driver, 2,
        partialSubjects: [student.name, "Grades"],
        partialContexts: [course.name],
        partialBodies: ['Grades Body', student.name]);

    // Let's open the most recent conversation -- should be the assignment conversation
    await ConversationListPage.selectMessage(driver, 0);

    // Then run some tests on it
    await ConversationDetailsPage.tapParticipants(driver); // To show individual participants
    await ConversationDetailsPage.verifyRecipientListed(driver, 0, teacher);
    await ConversationDetailsPage.verifySubject(driver, [student.name]);
    await ConversationDetailsPage.verifyCourse(driver, course.name);

    // Now let's reply to the email
    await ConversationDetailsPage.initiateEmailReplyAll(driver);
    await ConversationCreatePage.populateBody(driver, "reply body");
    await ConversationCreatePage.sendMail(driver);

    // And make sure that our new message shows up on the conversation list page...
    await driver?.tap(find.pageBack()); // From conversation detail page to conversation list page
    await driver?.refresh();
    await ConversationListPage.verifyConversationDataDisplayed(driver, 0, partialBodies: ['reply body']);
  }, timeout: Timeout(Duration(minutes: 2)));
}
