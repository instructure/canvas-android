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
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:intl/intl.dart';
import 'package:test/test.dart';

class AssignmentDetailsPage {
  static Future<void> validateGradedAssignment(FlutterDriver? driver, Assignment assignment, String grade) async {
    await driver?.waitFor(find.text(assignment.name!)); // No key to use here

    var pointTotalText = await driver?.getText(find.byValueKey("assignment_details_total_points"));
    var pointTotalExpected = assignment.pointsPossible.toInt().toString();
    expect(pointTotalText?.contains(pointTotalExpected), true,
        reason: "Expected total points to include $pointTotalExpected");

    var statusText = await driver?.getText(find.byValueKey("assignment_details_status"));
    expect(statusText, "Graded", reason: "Expected status to be Graded");

    _validateDueDate(driver, assignment.dueAt!);

    await driver?.scrollIntoView(find.byValueKey('grade-cell-graded-container'));
    var gradeText = await driver?.getText(find.byValueKey('grade-cell-score'));
    expect(gradeText, grade, reason: "Expected grade to be $grade");
  }

  static Future<void> validateSubmittedAssignment(FlutterDriver? driver, Assignment assignment) async {
    await driver?.waitFor(find.text(assignment.name!)); // No key to use here

    var pointTotalText = await driver?.getText(find.byValueKey("assignment_details_total_points"));
    var pointTotalExpected = assignment.pointsPossible.toInt().toString();
    expect(pointTotalText?.contains(pointTotalExpected), true,
        reason: "Expected total points to include $pointTotalExpected");

    var statusText = await driver?.getText(find.byValueKey("assignment_details_status"));
    expect(statusText, "Submitted", reason: "Expected status to be Submitted");

    _validateDueDate(driver, assignment.dueAt!);

    await driver?.scrollIntoView(find.byValueKey('grade-cell-submitted-container'));
    var submittedStatus = await driver?.getText(find.byValueKey('grade-cell-submit-status'));
    expect(submittedStatus, "Successfully submitted!", reason: "Expected to see 'Successfully submitted!'");
  }

  static Future<void> validateUnsubmittedAssignment(FlutterDriver? driver, Assignment assignment) async {
    await driver?.waitFor(find.text(assignment.name!)); // No key to use here

    var pointTotalText = await driver?.getText(find.byValueKey("assignment_details_total_points"));
    var pointTotalExpected = assignment.pointsPossible.toInt().toString();
    expect(pointTotalText?.contains(pointTotalExpected), true,
        reason: "Expected total points to include $pointTotalExpected");

    var statusText = await driver?.getText(find.byValueKey("assignment_details_status"));
    expect(statusText, "Not Submitted", reason: "Expected status to be Not Submitted");

    _validateDueDate(driver, assignment.dueAt!);
  }

  static Future<void> validateUnsubmittedQuiz(FlutterDriver? driver, Quiz quiz) async {
    await driver?.waitFor(find.text(quiz.title)); // No key to use here

    var pointTotalText = await driver?.getText(find.byValueKey("assignment_details_total_points"));
    var pointTotalExpected = quiz.pointsPossible.toInt().toString();
    expect(pointTotalText?.contains(pointTotalExpected), true,
        reason: "Expected total points to include $pointTotalExpected");

    var statusText = await driver?.getText(find.byValueKey("assignment_details_status"));
    expect(statusText, "Not Submitted", reason: "Expected status to be Not Submitted");

    _validateDueDate(driver, quiz.dueAt);
  }

  static Future<void> _validateDueDate(FlutterDriver? driver, DateTime dueAt) async {
    var localDate = dueAt.toLocal();
    String date = (DateFormat.MMMd(supportedDateLocale)).format(localDate).replaceAll(RegExp('[^A-Za-z0-9]'), '');
    String time = (DateFormat.jm(supportedDateLocale)).format(localDate).replaceAll(RegExp('[^A-Za-z0-9]'), '');
    var dueDateText = (await driver?.getText(find.byValueKey("assignment_details_due_date")))!.replaceAll(RegExp('[^A-Za-z0-9]'), '');
    expect(dueDateText.contains(date), true, reason: "Expected due date to contain $date [$dueDateText]");
    expect(dueDateText.contains(time), true, reason: "Expected due date to contain $time [$dueDateText]");
  }

  static Future<void> initiateCreateEmail(FlutterDriver? driver) async {
    await driver?.tap(find.byType("FloatingActionButton"));
  }
}
