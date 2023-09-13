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
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_grade.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:test/test.dart';

void main() {
  final _course = Course((b) => b..id = 'course_123');
  final _enrollment = Enrollment((b) => b..enrollmentState = 'active');

  group('isCourseGradeLocked', () {
    test('returns true if hideFinalGrades is true', () {
      final course = Course((b) => b..hideFinalGrades = true);
      final grade = CourseGrade(course, null);

      expect(grade.isCourseGradeLocked(), true);
    });

    test('returns false if hideFinalGrades and hasGradingPeriods are false', () {
      final course = Course((b) => b
        ..hideFinalGrades = false
        ..hasGradingPeriods = false);
      final grade = CourseGrade(course, null);

      expect(grade.isCourseGradeLocked(), false);
    });

    test('returns true if hasGradingPeriods is true and there are no course enrollments', () {
      final course = Course((b) => b
        ..hasGradingPeriods = true
        ..enrollments = BuiltList.of(<Enrollment>[]).toBuilder());
      final grade = CourseGrade(course, null);

      expect(grade.isCourseGradeLocked(), true);
    });

    test('returns false if hasGradingPeriods is true and an active grading period on the course enrollment is set', () {
      final enrollment = _enrollment.rebuild((b) => b
        ..currentGradingPeriodId = '101'
        ..multipleGradingPeriodsEnabled = true);

      final course = Course((b) => b
        ..hasGradingPeriods = true
        ..enrollments = BuiltList.of([enrollment]).toBuilder());

      final grade = CourseGrade(course, null);

      expect(grade.isCourseGradeLocked(), false);
    });

    test(
        'returns true if hasGradingPeriods is true and an active grading period on the course enrollment is set but forcing all periods',
        () {
      final enrollment = _enrollment.rebuild((b) => b
        ..currentGradingPeriodId = '101'
        ..multipleGradingPeriodsEnabled = true);

      final course = Course((b) => b
        ..hasGradingPeriods = true
        ..enrollments = BuiltList.of([enrollment]).toBuilder());

      final grade = CourseGrade(course, null, forceAllPeriods: true);

      expect(grade.isCourseGradeLocked(), true);
    });

    test('returns false if hasGradingPeriods is true and NO active grading period on the course enrollment is set', () {
      final enrollment = _enrollment.rebuild((b) => b..multipleGradingPeriodsEnabled = true);

      final course = Course((b) => b
        ..hasGradingPeriods = true
        ..enrollments = BuiltList.of([enrollment]).toBuilder());

      final grade = CourseGrade(course, null);

      expect(grade.isCourseGradeLocked(), true);
    });

    test(
        'returns false if hasGradingPeriods is true and totalsForAllGradingPeriodsOption on the course enrollment is true',
        () {
      final enrollment = _enrollment.rebuild((b) => b
        ..role = 'student'
        ..totalsForAllGradingPeriodsOption = true
        ..multipleGradingPeriodsEnabled = true);

      final course = Course((b) => b
        ..hasGradingPeriods = true
        ..enrollments = BuiltList.of([enrollment]).toBuilder());

      final grade = CourseGrade(course, null);

      expect(grade.isCourseGradeLocked(), false);
    });

    test(
        'returns false if hasGradingPeriods is true and totalsForAllGradingPeriodsOption on the course enrollment is false',
        () {
      final enrollment = _enrollment.rebuild((b) => b
        ..role = 'student'
        ..totalsForAllGradingPeriodsOption = false
        ..multipleGradingPeriodsEnabled = true);

      final course = Course((b) => b
        ..hasGradingPeriods = true
        ..enrollments = BuiltList.of([enrollment]).toBuilder());

      final grade = CourseGrade(course, null);

      expect(grade.isCourseGradeLocked(), true);
    });
  });

  group('equals', () {
    test('returns true if the course is the same', () {
      final grade = CourseGrade(_course, null);
      expect(true, grade == CourseGrade(_course, null));
    });

    test('returns false if the courses are different', () {
      final grade = CourseGrade(_course, null);
      expect(false, grade == CourseGrade(_course.rebuild((b) => b..id = 'copy'), null));
    });

    test('returns true if the enrollment is the same', () {
      final grade = CourseGrade(null, _enrollment);
      expect(true, grade == CourseGrade(null, _enrollment));
    });

    test('returns false if the enrollments are different', () {
      final grade = CourseGrade(null, _enrollment);
      expect(false, grade == CourseGrade(null, _enrollment.rebuild((b) => b..enrollmentState = 'completed')));
    });

    test('returns true if the course and enrollment are the same', () {
      final grade = CourseGrade(_course, _enrollment);
      expect(true, grade == CourseGrade(_course, _enrollment));
    });

    test('returns true if the course and enrollment are the same and forceAllPeriod is true', () {
      final grade = CourseGrade(_course, _enrollment, forceAllPeriods: true);
      expect(true, grade == CourseGrade(_course, _enrollment, forceAllPeriods: true));
    });

    test('returns true if the course and enrollment are the same and forceAllPeriod is false', () {
      final grade = CourseGrade(_course, _enrollment, forceAllPeriods: true);
      expect(true, grade == CourseGrade(_course, _enrollment, forceAllPeriods: true));
    });

    test('returns false if the course and enrollment are the same but have different forceAllPeriod', () {
      final grade = CourseGrade(_course, _enrollment, forceAllPeriods: true);
      expect(false, grade == CourseGrade(_course, _enrollment));
    });

    test('returns false if the other side is not a course grade', () {
      final grade = CourseGrade(null, null);
      expect(false, grade == _course);
    });
  });
}
