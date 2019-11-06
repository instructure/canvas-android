/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.
import 'enrollment.dart';

class CourseGrade {
  Enrollment _enrollment;

  CourseGrade(this._enrollment);

  double currentScore() => _hasActiveGradingPeriod()
      ? _getCurrentPeriodComputedCurrentScore()
      : _getCurrentScore();

  String currentGrade() => _hasActiveGradingPeriod()
      ? _getCurrentPeriodComputedCurrentGrade()
      : _getCurrentGrade();

  bool noCurrentGrade() =>
      currentScore() == null &&
          (currentGrade() == null ||
              currentGrade().contains("N/A") ||
              currentGrade().isEmpty);

  bool _hasActiveGradingPeriod() =>
      _enrollment.multipleGradingPeriodsEnabled &&
          _enrollment.currentGradingPeriodId != null &&
          _enrollment.currentGradingPeriodId != 0;

  double _getCurrentScore() =>
      _enrollment.grade?.currentScore ?? _enrollment.computedCurrentScore;

//  double _getFinalScore() =>
//      _enrollment.grade?.finalScore ?? _enrollment.computedFinalScore;

  String _getCurrentGrade() =>
      _enrollment.grade?.currentGrade ?? _enrollment.computedCurrentGrade;

//  String _getFinalGrade() =>
//      _enrollment.grade?.finalGrade ?? _enrollment.computedFinalGrade;

  double _getCurrentPeriodComputedCurrentScore() =>
      _enrollment.grade?.currentScore ??
          _enrollment.currentPeriodComputedCurrentScore;

  String _getCurrentPeriodComputedCurrentGrade() =>
      _enrollment.grade?.currentGrade ??
          _enrollment.currentPeriodComputedCurrentGrade;

//  double _getCurrentPeriodComputedFinalScore() =>
//      _enrollment.grade?.finalScore ??
//      _enrollment.currentPeriodComputedFinalScore;

//  String _getCurrentPeriodComputedFinalGrade() =>
//      _enrollment.grade?.finalGrade ??
//      _enrollment.currentPeriodComputedFinalGrade;
}
