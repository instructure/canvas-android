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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/grade_cell_data.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:intl/intl.dart';

void main() {
  Assignment baseAssignment;
  Submission baseSubmission;
  GradeCellData baseGradedState;

  Color accentColor = Colors.pinkAccent;

  ThemeData theme = ThemeData(accentColor: accentColor);
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
      ..accentColor = accentColor
      ..outOf = 'Out of 100 points');
  });

  test('Returns empty for null submission', () {
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(baseAssignment, null, theme, l10n);
    expect(actual, expected);
  });

  test('Returns Graded state if graded but not submitted', () {
    var submission = baseSubmission.rebuild((b) => b..submittedAt = null);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true);
    var actual = GradeCellData.forSubmission(baseAssignment, submission, theme, l10n);
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
    var actual = GradeCellData.forSubmission(baseAssignment, submission, theme, l10n);
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
      ..submissionText = l10n.submissionStatusSuccessSubtitle(
        DateFormat.MMMMd().format(submission.submittedAt),
        DateFormat.jm().format(submission.submittedAt),
      ));
    var actual = GradeCellData.forSubmission(baseAssignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns Empty state when not submitted and ungraded', () {
    var submission = Submission((b) => b..assignmentId = '1');
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(baseAssignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct graded state for excused', () {
    var submission = baseSubmission.rebuild((b) => b..excused = true);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 1.0
      ..showCompleteIcon = true
      ..grade = 'Excused');
    var actual = GradeCellData.forSubmission(baseAssignment, submission, theme, l10n);
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
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Complete-Incomplete\' grading type if Complete', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.passFail);
    var submission = baseSubmission.rebuild((b) => b..grade = 'complete');
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 1.0
      ..showCompleteIcon = true
      ..grade = 'Complete');
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Complete-Incomplete\' grading type if Incomplete', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.passFail);
    var submission = baseSubmission.rebuild((b) => b
      ..grade = 'incomplete'
      ..score = 0.0);
    var expected = baseGradedState.rebuild((b) => b
      ..accentColor = Color(0xFF8B969E)
      ..graphPercent = 1.0
      ..showIncompleteIcon = true
      ..grade = 'Incomplete');
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Complete-Incomplete\' grading type if not graded', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.passFail);
    var submission = baseSubmission.rebuild((b) => b
      ..grade = null
      ..score = 0.0);
    var expected = GradeCellData((b) => b
      ..state = GradeCellState.submitted
      ..submissionText = l10n.submissionStatusSuccessSubtitle(
        DateFormat.MMMMd().format(submission.submittedAt),
        DateFormat.jm().format(submission.submittedAt),
      ));
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for \'Points\' grading type', () {
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true);
    var actual = GradeCellData.forSubmission(baseAssignment, baseSubmission, theme, l10n);
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
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
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
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
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
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns empty state for \'Not Graded\' grading type', () {
    var assignment = baseAssignment.rebuild((b) => b..gradingType = GradingType.notGraded);
    var submission = Submission((b) => b..assignmentId = '1');
    var expected = GradeCellData();
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
    expect(actual, expected);
  });

  test('Returns correct state for late penalty', () {
    var submission = baseSubmission.rebuild((b) => b
      ..pointsDeducted = 6.0
      ..grade = '79'
      ..score = 79.0);
    var expected = baseGradedState.rebuild((b) => b
      ..graphPercent = 0.85
      ..score = '85'
      ..showPointsLabel = true
      ..latePenalty = 'Late penalty (-6)'
      ..finalGrade = 'Final Grade: 79');
    var actual = GradeCellData.forSubmission(baseAssignment, submission, theme, l10n);
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
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
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
    var actual = GradeCellData.forSubmission(assignment, submission, theme, l10n);
    expect(actual, expected);
  });
}
