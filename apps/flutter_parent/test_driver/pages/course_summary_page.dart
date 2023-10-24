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
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/dataseeding/quiz.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:intl/intl.dart';
import 'package:test/test.dart';

import 'assignment_details_page.dart';
import 'event_details_page.dart';

class CourseSummaryPage {
  static Future<void> verifyAssignmentPresent(FlutterDriver? driver, Assignment assignment) async {
    var titleFinder = find.byValueKey('summary_item_title_${assignment.id}');
    await driver?.scrollIntoView(titleFinder);
    var title = await driver?.getText(titleFinder);
    expect(title, assignment.name, reason: "assignment title");
    await _validateDueDate(driver, assignment.id, assignment.dueAt);

    // Lets click through to the assignment details, validate them, and come back
    await driver?.tap(titleFinder);
    await AssignmentDetailsPage.validateUnsubmittedAssignment(driver, assignment);
    await driver?.tap(find.pageBack());
  }

  static Future<void> verifyQuizPresent(FlutterDriver? driver, Quiz quiz) async {
    var titleFinder = find.byValueKey('summary_item_title_${quiz.id}');
    await driver?.scrollIntoView(titleFinder);
    var title = await driver?.getText(titleFinder);
    expect(title, quiz.title, reason: "quiz title");
    await _validateDueDate(driver, quiz.id, quiz.dueAt);

    // Lets click through to the quiz/assignment details, validate them, and come back
    await driver?.tap(titleFinder);
    await AssignmentDetailsPage.validateUnsubmittedQuiz(driver, quiz);
    await driver?.tap(find.pageBack());
  }

  static Future<void> verifyEventPresent(FlutterDriver? driver, ScheduleItem event) async {
    var titleFinder = find.byValueKey('summary_item_title_${event.id}');
    await driver?.scrollIntoView(titleFinder);
    var title = await driver?.getText(titleFinder);
    expect(title, event.title, reason: "calendar event title");

    await _validateDueDate(driver, event.id, event.isAllDay ? event.allDayDate : event.startAt);

    // Let's click through to the event details, validate them, and come back
    await driver?.tap(titleFinder);
    await EventDetailsPage.verifyEventDisplayed(driver, event);
    await driver?.tap(find.pageBack());
  }

  static Future<void> _validateDueDate(FlutterDriver? driver, String itemId, DateTime? dueDate) async {
    var dateFinder = find.byValueKey('summary_item_subtitle_${itemId}');
    await driver?.scrollIntoView(dateFinder);
    var text = await driver?.getText(dateFinder);
    if (dueDate == null) {
      expect(text, "No Due Date", reason: "Due date");
    } else {
      var localDate = dueDate.toLocal();
      String date = (DateFormat.MMMd(supportedDateLocale)).format(localDate);
      String time = (DateFormat.jm(supportedDateLocale)).format(localDate);
      expect(text?.contains(date), true, reason: "Expected due date ($text) to contain $date");
      expect(text?.contains(time), true, reason: "Expected due date ($text) to contain $time");
    }
  }
}
