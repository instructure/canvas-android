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

import 'package:built_value/built_value.dart';
import 'package:flutter/material.dart' hide Builder;
import 'package:flutter/rendering.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:intl/intl.dart';

import 'assignment.dart';

part 'grade_cell_data.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build
abstract class GradeCellData implements Built<GradeCellData, GradeCellDataBuilder> {
  GradeCellState get state;
  String get submissionText;
  bool get showCompleteIcon;
  bool get showIncompleteIcon;
  bool get showPointsLabel;
  Color get accentColor;
  double get graphPercent;
  String get score;
  String get grade;
  String get gradeContentDescription;
  String get outOf;
  String get yourGrade;
  String get latePenalty;
  String get finalGrade;

  GradeCellData._();
  factory GradeCellData([void Function(GradeCellDataBuilder) updates]) = _$GradeCellData;

  static void _initializeBuilder(GradeCellDataBuilder b) => b
    ..state = GradeCellState.empty
    ..submissionText = ''
    ..showCompleteIcon = false
    ..showIncompleteIcon = false
    ..showPointsLabel = false
    ..accentColor = Colors.grey
    ..graphPercent = 0
    ..score = ''
    ..grade = ''
    ..gradeContentDescription = ''
    ..outOf = ''
    ..yourGrade = ''
    ..latePenalty = ''
    ..finalGrade = '';

  static GradeCellData forSubmission(
    Course? course,
    Assignment? assignment,
    Submission? submission,
    ThemeData theme,
    AppLocalizations l10n,
  ) {
    final excused = submission?.excused ?? false;
    final restrictQuantitativeData = course?.settings?.restrictQuantitativeData ?? false;

    // Return empty state if null, unsubmitted and ungraded, or has a 'not graded' or restricted grading type
    final restricted = restrictQuantitativeData &&
        assignment?.isGradingTypeQuantitative() == true &&
        (course?.gradingSchemeItems.isEmpty == true || assignment?.pointsPossible == 0) &&
        !excused;

    if (assignment == null ||
        submission == null ||
        (submission.submittedAt == null && !excused && submission.grade == null) ||
        assignment.gradingType == GradingType.notGraded ||
        restricted) {
      return GradeCellData();
    }

    // Return submitted state if the submission has not been graded
    if (submission.submittedAt != null && submission.grade == null && !excused) {
      return GradeCellData((b) => b
        ..state = GradeCellState.submitted
        ..submissionText = submission.submittedAt!.l10nFormat(
          l10n.submissionStatusSuccessSubtitle,
          dateFormat: DateFormat.MMMMd(supportedDateLocale),
        ));
    }

    var accentColor = theme.colorScheme.secondary;

    var pointsPossibleText = NumberFormat.decimalPattern().format(assignment.pointsPossible);

    var outOfText = restrictQuantitativeData ? '' : l10n.outOfPoints(pointsPossibleText, assignment.pointsPossible);

    // Excused
    if (submission.excused) {
      return GradeCellData((b) => b
        ..state = GradeCellState.graded
        ..graphPercent = 1.0
        ..showCompleteIcon = true
        ..accentColor = accentColor
        ..grade = l10n.excused
        ..outOf = outOfText);
    }

    // Complete/Incomplete
    if (assignment.gradingType == GradingType.passFail) {
      var isComplete = (submission.grade == 'complete');
      return GradeCellData((b) => b
        ..state = GradeCellState.graded
        ..showCompleteIcon = isComplete
        ..showIncompleteIcon = !isComplete
        ..grade = isComplete ? l10n.gradeComplete : l10n.gradeIncomplete
        ..accentColor = isComplete ? accentColor : ParentColors.ash
        ..outOf = outOfText
        ..graphPercent = 1.0);
    }

    var score = NumberFormat.decimalPattern().format(submission.score);
    var graphPercent = (submission.score / assignment.pointsPossible).clamp(0.0, 1.0);

    // If grading type is Points, don't show the grade since we're already showing it as the score
    var grade = assignment.gradingType != GradingType.points ? submission.grade ?? '' : '';

    if (restrictQuantitativeData && assignment.isGradingTypeQuantitative()) {
      grade = course?.convertScoreToLetterGrade(submission.score, assignment.pointsPossible) ?? '';
    }

    // Screen reader fails on letter grades with a minus (e.g. 'A-'), so we replace the dash with the word 'minus'
    var accessibleGradeString = grade.replaceAll('-', '. ${l10n.accessibilityMinus}');

    var yourGrade = '';
    var latePenalty = '';
    var finalGrade = '';
    var restrictedScore = grade;

    // Adjust for late penalty, if any
    if ((submission.pointsDeducted ?? 0.0) > 0.0) {
      grade = ''; // Grade will be shown in the 'final grade' text
      var pointsDeducted = NumberFormat.decimalPattern().format(submission.pointsDeducted ?? 0.0);
      var pointsAchieved = NumberFormat.decimalPattern().format(submission.enteredScore);
      yourGrade = l10n.yourGrade(pointsAchieved);
      latePenalty = l10n.latePenaltyUpdated(pointsDeducted);
      finalGrade = l10n.finalGrade(submission.grade ?? grade);
    }

    return restrictQuantitativeData
        ? GradeCellData((b) => b
          ..state = GradeCellState.gradedRestrictQuantitativeData
          ..graphPercent = 1.0
          ..accentColor = accentColor
          ..score = restrictedScore
          ..gradeContentDescription = accessibleGradeString)
        : GradeCellData((b) => b
          ..state = GradeCellState.graded
          ..graphPercent = graphPercent
          ..accentColor = accentColor
          ..score = score
          ..showPointsLabel = true
          ..outOf = outOfText
          ..grade = grade
          ..gradeContentDescription = accessibleGradeString
          ..yourGrade = yourGrade
          ..latePenalty = latePenalty
          ..finalGrade = finalGrade);
  }
}

enum GradeCellState { empty, submitted, graded, gradedRestrictQuantitativeData }
