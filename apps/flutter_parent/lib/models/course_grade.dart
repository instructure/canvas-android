// Copyright (C) 2019 - present Instructure, Inc.
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
import 'package:flutter_parent/models/course.dart';

import 'enrollment.dart';

/**
 * CourseGrade object for displaying course grade totals.
 *
 * Note: Current vs Final
 * As a general rule, current represents a grade calculated only from graded assignments, where as
 * final grades use all assignments, regardless of grading status, in their calculation.
 *
 * @finalGrade - Final grade string value, for the current grading period or the current term
 * (see Course.getCourseGrade, ignoreMGP).
 *
 * @finalScore - Final score value, a double representation of a percentage grade, for the current
 * grading period or the current term (see Course.getCourseGrade, ignoreMGP). Needs formatting
 * prior to use.
 *
 * @noFinalGrade - If the course contains no valid final grade or score, this flag will be true. This is usually
 * represented in the UI with "N/A". See Course.noFinalGrade for logic.
 */
class CourseGrade {
  Course? _course;
  Enrollment? _enrollment;
  bool _forceAllPeriods;

  CourseGrade(this._course, this._enrollment, {bool forceAllPeriods = false}) : _forceAllPeriods = forceAllPeriods;

  operator ==(Object other) {
    if (!(other is CourseGrade)) {
      return false;
    }
    final grade = other;
    return _course == grade._course && _enrollment == grade._enrollment && _forceAllPeriods == grade._forceAllPeriods;
  }

  /// Represents the lock status of a course, this is different from hideFinalGrades, as it takes both that value, and
  /// totalsForAllGradingPeriodsOption into account. The latter is only used when relevant.
  bool isCourseGradeLocked({bool forAllGradingPeriods = true}) {
    if (_course?.hideFinalGrades == true) {
      return true;
    } else if (_course?.hasGradingPeriods == true) {
      return forAllGradingPeriods && !_hasActiveGradingPeriod() && !_isTotalsForAllGradingPeriodsEnabled();
    } else {
      return false;
    }
  }

  /// Current score value, a double representation of a percentage grade, for the current grading period or the current
  /// term (see Course.getCourseGrade, ignoreMGP). Needs formatting prior to use.
  double? currentScore() => _hasActiveGradingPeriod() ? _getCurrentPeriodComputedCurrentScore() : _getCurrentScore();

  /// Current grade string value, for the current grading period or the current term. (see Course.getCourseGrade)
  String? currentGrade() => _hasActiveGradingPeriod() ? _getCurrentPeriodComputedCurrentGrade() : _getCurrentGrade();

  /// If the course contains no valid current grade or score, this flag will be true. This is usually represented in the
  /// UI with "N/A".
  bool noCurrentGrade() =>
      currentScore() == null && (currentGrade() == null || currentGrade()!.contains('N/A') || currentGrade()!.isEmpty);

  bool _hasActiveGradingPeriod() =>
      !_forceAllPeriods &&
      (_course?.enrollments?.toList().any((enrollment) => enrollment.hasActiveGradingPeriod()) ?? false);

  bool _isTotalsForAllGradingPeriodsEnabled() =>
      _course?.enrollments?.toList().any((enrollment) => enrollment.isTotalsForAllGradingPeriodsEnabled()) ?? false;

  double? _getCurrentScore() => _enrollment?.grades?.currentScore ?? _enrollment?.computedCurrentScore;

  String? _getCurrentGrade() => _enrollment?.grades?.currentGrade ?? _enrollment?.computedCurrentGrade ?? _enrollment?.computedCurrentLetterGrade;

  double? _getCurrentPeriodComputedCurrentScore() =>
      _enrollment?.grades?.currentScore ?? _enrollment?.currentPeriodComputedCurrentScore;

  String? _getCurrentPeriodComputedCurrentGrade() =>
      _enrollment?.grades?.currentGrade ?? _enrollment?.currentPeriodComputedCurrentGrade;

}
