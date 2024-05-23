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

import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/grade_cell_data.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:intl/intl.dart';

void main() {
  late Assignment baseAssignment;
  late Submission baseSubmission;
  late GradeCellData baseGradedState;
  late Course baseCourse;

  Color secondaryColor = Colors.pinkAccent;

  ThemeData theme = ThemeData().copyWith(colorScheme: ThemeData().colorScheme.copyWith(secondary: secondaryColor));
  AppLocalizations l10n = AppLocalizations();

  setUp(() {
    baseAssignment = Assignment((a) => a
      ..id = '1'
      ..courseId = '123'
      ..assignmentGroupId = ''
      ..position = 0
      ..pointsPossible = 100.0
      ..gradingType = GradingType.points);

    baseSubmission = Submission((s) => s
      ..attempt = 1
      ..assignmentId = '1'
      ..submittedAt = DateTime(2017, 6, 27, 18, 47, 0)
      ..workflowState = 'graded'
      ..enteredGrade = '85'
      ..enteredScore = 85.0
      ..grade = '85'
      ..score = 85.0);

    baseGradedState = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..accentColor = secondaryColor
      ..outOf = 'Out of 100 points');

    final gradingSchemeBuilder = ListBuilder<JsonObject>()
      ..add(JsonObject(["A", 0.9]))
      ..add(JsonObject(["F", 0.0]));

    baseCourse = Course((b) => b
      ..id = '123'
      ..settings.restrictQuantitativeData = false
      ..gradingScheme = gradingSchemeBuilder);
  });

  test('Returns empty for null submission', () {
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(baseCourse, baseAssignment, null, theme, l10n);
    expect(actual, expected);
  });

  test('Returns Graded state if graded but not submitted', () {
    var submission = baseSubmission.rebuild((b) => b..submittedAt = null);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true);
    var actual = GradeCellData.forSubmission(baseCourse, baseAssignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns Empty state if graded, not submitted, and grade is hidden', () {
    var submission = baseSubmission.rebuild((b) => b
      ..submittedAt = null
      ..enteredGrade = null
      ..enteredScore = 0.0
      ..grade = null
      ..score = 0.0);
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(baseCourse, baseAssignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns Submitted state if submitted but not graded', () {
    var submission = Submission((s) => s
      ..attempt = 1
      ..assignmentId = '1'
      ..submittedAt = DateTime(2017, 6, 27, 18, 47, 0)
      ..workflowState = 'submitted');
    var expected = GradeCellData((b) => b
      ..state = GradeCellState.submitted
      ..submissionText = submission.submittedAt.l10nFormat(
        l10n.submissionStatusSuccessSubtitle,
        dateFormat: DateFormat.MMMMd(),
      ));
    var actual = GradeCellData.forSubmission(baseCourse, baseAssignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns Empty state when not submitted and ungraded', () {
    var submission = Submission((b) => b..assignmentId = '1');
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(baseCourse, baseAssignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct graded state for excused', () {
    var submission = baseSubmission.rebuild((b) => b..excused = true);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 1.0
      ..showCompleteIcon = true
      ..grade = 'Excused');
    var actual = GradeCellData.forSubmission(baseCourse, baseAssignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Percentage\' grading type', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.percent);
    var submission = baseSubmission.rebuild((b) => b..grade = '85%');
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true
      ..grade = '85%'
      ..gradeContentDescription = '85%');
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Complete-Incomplete\' grading type if Complete', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.passFail);
    var submission = baseSubmission.rebuild((b) => b..grade = 'complete');
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 1.0
      ..showCompleteIcon = true
      ..grade = 'Complete');
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Complete-Incomplete\' grading type if Incomplete', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.passFail);
    var submission = baseSubmission.rebuild((b) => b
      ..grade = 'incomplete'
      ..score = 0.0);
    var expected = baseGradedState.rebuild((b) => b
      ..accentColor = ParentColors.ash
      ..graphPercent = 1.0
      ..showIncompleteIcon = true
      ..grade = 'Incomplete');
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Complete-Incomplete\' grading type if not graded', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.passFail);
    var submission = baseSubmission.rebuild((b) => b
      ..grade = null
      ..score = 0.0);
    var expected = GradeCellData((b) => b
      ..state = GradeCellState.submitted
      ..submissionText = submission.submittedAt.l10nFormat(
        l10n.submissionStatusSuccessSubtitle,
        dateFormat: DateFormat.MMMMd(),
      ));
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Points\' grading type', () {
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true);
    var actual = GradeCellData.forSubmission(baseCourse, baseAssignment, baseSubmission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Letter Grade\' grading type', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.letterGrade);
    var submission = baseSubmission.rebuild((b) => b..grade = 'B+');
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true
      ..grade = 'B+'
      ..gradeContentDescription = 'B+');
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Letter Grade\' grading type with minus', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.letterGrade);
    var submission = baseSubmission.rebuild((b) => b
      ..grade = 'A-'
      ..enteredGrade = 'A-'
      ..enteredScore = 91.0
      ..score = 91.0);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.91
      ..score = '91'
      ..showPointsLabel = true
      ..grade = 'A-'
      ..gradeContentDescription = 'A. minus');
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'GPA Scale\' grading type', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.gpaScale);
    var submission = baseSubmission.rebuild((b) => b..grade = '3.8 GPA');
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true
      ..grade = '3.8 GPA'
      ..gradeContentDescription = '3.8 GPA');
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns empty state for \'Not Graded\' grading type', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.notGraded);
    var submission = Submission((b) => b..assignmentId = '1');
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for late penalty', () {
    var submission = baseSubmission.rebuild((b) => b
      ..pointsDeducted = 6.0
      ..grade = '79'
      ..score = 79.0);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.79
      ..score = '79'
      ..showPointsLabel = true
      ..yourGrade = 'Your grade: 85'
      ..latePenalty = 'Late Penalty: -6 pts'
      ..finalGrade = 'Final Grade: 79');
    var actual = GradeCellData.forSubmission(baseCourse, baseAssignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Includes content descriptions for letter grade with minus', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.letterGrade);
    var submission = baseSubmission.rebuild((b) => b..grade = 'B-');
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true
      ..grade = 'B-'
      ..gradeContentDescription = 'B. minus');
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Includes content descriptions for grades', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.letterGrade);
    var submission = baseSubmission.rebuild((b) => b
      ..grade = 'B'
      ..enteredGrade = '88'
      ..enteredScore = 88.0
      ..score = 88.0);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.88
      ..score = '88'
      ..showPointsLabel = true
      ..grade = 'B'
      ..gradeContentDescription = 'B');
    var actual = GradeCellData.forSubmission(baseCourse, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns Empty state when quantitative data is restricted, grading type is points, grading scheme is null and not excused', () {
    var course = baseCourse.rebuild((b) => b
      ..settings.restrictQuantitativeData = true
      ..gradingScheme = null);
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.points);
    var submission = Submission((b) => b
      ..assignmentId = '1'
      ..score = 10.0
      ..grade = 'A');
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(course, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns Empty state when quantitative data is restricted, grading type is percent, grading scheme is null and not excused', () {
    var course = baseCourse.rebuild((b) => b
      ..settings.restrictQuantitativeData = true
      ..gradingScheme = null);
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.percent);
    var submission = Submission((b) => b
      ..assignmentId = '1'
      ..score = 10.0
      ..grade = 'A');
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(course, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state when quantitative data is restricted and grading type is percent and excused', () {
    var course = baseCourse.rebuild((b) => b..settings.restrictQuantitativeData = true);
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.percent);
    var submission = Submission((b) => b
      ..assignmentId = '1'
      ..score = 10.0
      ..grade = 'A'
      ..excused = true);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 1.0
      ..grade = l10n.excused
      ..outOf = ''
      ..showCompleteIcon = true);
    var actual = GradeCellData.forSubmission(course, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state when quantitative data is restricted and graded', () {
    var course = baseCourse.rebuild((b) => b..settings.restrictQuantitativeData = true);
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.letterGrade);
    var submission = Submission((b) => b
      ..assignmentId = '1'
      ..score = 10.0
      ..grade = 'A');
    var expected = baseGradedState.rebuild((b) => b
      ..state = GradeCellState.gradedRestrictQuantitativeData
      ..graphPercent = 1.0
      ..score = submission.grade
      ..gradeContentDescription = submission.grade
      ..outOf = '');
    var actual = GradeCellData.forSubmission(course, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state when quantitative data is restricted, grading type is percent and grading scheme is given', () {
    var course = baseCourse.rebuild((b) => b..settings.restrictQuantitativeData = true);
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.percent);
    var submission = Submission((b) => b
      ..assignmentId = '1'
      ..score = 10.0
      ..grade = 'A');
    var expected = baseGradedState.rebuild((b) => b
      ..state = GradeCellState.gradedRestrictQuantitativeData
      ..graphPercent = 1.0
      ..score = 'F'
      ..outOf = ''
      ..gradeContentDescription = 'F');
    var actual = GradeCellData.forSubmission(course, assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state when quantitative data is restricted, grading type is point and grading scheme is given', () {
    var course = baseCourse.rebuild((b) => b..settings.restrictQuantitativeData = true);
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.points);
    var submission = Submission((b) => b
      ..assignmentId = '1'
      ..score = 90.0
      ..grade = 'A');
    var expected = baseGradedState.rebuild((b) => b
      ..state = GradeCellState.gradedRestrictQuantitativeData
      ..graphPercent = 1.0
      ..score = 'A'
      ..outOf = ''
      ..gradeContentDescription = 'A');
    var actual = GradeCellData.forSubmission(course, assignment, submission, theme, l10n);
    expect(actual, expected);
  });
}
