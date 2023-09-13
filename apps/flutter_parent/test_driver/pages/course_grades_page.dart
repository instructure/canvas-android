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
import 'package:test/test.dart';

import '../flutter_driver_extensions.dart';

class CourseGradesPage {
  static Future<void> verifyTotalGradeContains(FlutterDriver? driver, String text) async {
    var totalGradeText = await driver?.getTextWithRefreshes(_totalGradeFinder);
    expect(totalGradeText?.toLowerCase().contains(text.toLowerCase()), true,
        reason: "Expected total grade to contain $text");
  }

  static Future<void> verifyAssignment(FlutterDriver? driver, Assignment assignment,
      {String? grade = null, String? status = null}) async {
    var rowFinder = _assignmentRowFinder(assignment);
    await driver?.scrollIntoView(rowFinder);

    var nameFinder = _assignmentNameFinder(assignment);
    var nameText = await driver?.getTextWithRefreshes(nameFinder);
    expect(nameText, assignment.name, reason: "Expected assignment name of ${assignment.name}");

    var gradeFinder = _assignmentGradeFinder(assignment);
    var gradeText = await driver?.getTextWithRefreshes(gradeFinder);
    expect(gradeText?.contains(assignment.pointsPossible.toInt().toString()), true,
        reason: "Expected grade to contain ${assignment.pointsPossible.toInt()}");
    if (grade != null) {
      expect(gradeText?.contains(grade), true, reason: "Expected grade to contain $grade");
    }

    if (status != null) {
      var statusFinder = _assignmentStatusFinder(assignment);
      var statusText = await driver?.getTextWithRefreshes(statusFinder);
      expect(statusText?.toLowerCase().contains(status.toLowerCase()), true,
          reason: "Expected status to contain $status");
    }
  }

  static Future<void> selectAssignment(FlutterDriver? driver, Assignment assignment) async {
    var rowFinder = _assignmentRowFinder(assignment);
    await driver?.scrollIntoView(rowFinder);
    await driver?.tap(_assignmentNameFinder(assignment));
  }

  static final _totalGradeFinder = find.byValueKey("total_grade");

  static SerializableFinder _assignmentRowFinder(Assignment assignment) {
    return find.byValueKey("assignment_${assignment.id}_row");
  }

  static SerializableFinder _assignmentNameFinder(Assignment assignment) {
    return find.byValueKey("assignment_${assignment.id}_name");
  }

  static SerializableFinder _assignmentStatusFinder(Assignment assignment) {
    return find.byValueKey("assignment_${assignment.id}_status");
  }

  static SerializableFinder _assignmentGradeFinder(Assignment assignment) {
    return find.byValueKey("assignment_${assignment.id}_grade");
  }

  static Future<void> initiateCreateEmail(FlutterDriver? driver) async {
    await driver?.tap(find.byType('FloatingActionButton'));
  }
}
