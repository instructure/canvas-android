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
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_grade.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grading_scheme_item.dart';
import 'package:test/test.dart';

void main() {
  final _studentId = '123';
  final _gradingPeriodId = '321';
  final _course = Course((b) => b..id = 'course_123');
  final futureDate = DateTime.now().add(Duration(days: 10));
  final pastDate = DateTime.now().subtract(Duration(days: 10));
  final _enrollment = Enrollment((b) => b
    ..enrollmentState = 'active'
    ..userId = _studentId);

  group('getCourseGrade', () {
    test('returns a course grade with the course and enrollment', () {
      final grade = _course.getCourseGrade(_studentId, enrollment: _enrollment);
      expect(grade, CourseGrade(_course, _enrollment, forceAllPeriods: false));
    });

    test('returns a course grade with the course and student enrollment', () {
      final course = _course.rebuild((b) => b..enrollments = ListBuilder([_enrollment]));

      final grade = course.getCourseGrade(_studentId);
      expect(grade, CourseGrade(course, course.enrollments!.first, forceAllPeriods: false));
    });

    test('returns a course grade with the course and student gradinig period enrollment', () {
      final enrollmentInGradingPeriod = _enrollment.rebuild((b) => b..currentGradingPeriodId = _gradingPeriodId);
      final course = _course.rebuild((b) => b..enrollments = ListBuilder([_enrollment, enrollmentInGradingPeriod]));

      final grade = course.getCourseGrade(_studentId, gradingPeriodId: _gradingPeriodId);
      expect(grade, CourseGrade(course, enrollmentInGradingPeriod, forceAllPeriods: false));
    });

    test('returns a course grade with the course and student enrollment without grading period', () {
      final enrollmentInGradingPeriod = _enrollment.rebuild((b) => b..currentGradingPeriodId = _gradingPeriodId);
      final course = _course.rebuild((b) => b..enrollments = ListBuilder([_enrollment, enrollmentInGradingPeriod]));

      // Test the various ways that grading period can not be set
      CourseGrade grade = course.getCourseGrade(_studentId);
      expect(grade, CourseGrade(course, _enrollment, forceAllPeriods: false));

      grade = course.getCourseGrade(_studentId, gradingPeriodId: null);
      expect(grade, CourseGrade(course, _enrollment, forceAllPeriods: false));

      grade = course.getCourseGrade(_studentId, gradingPeriodId: '');
      expect(grade, CourseGrade(course, _enrollment, forceAllPeriods: false));
    });

    test('returns a course grade with the course and no enrollment without matchinig grading period', () {
      final course = _course.rebuild((b) => b..enrollments = ListBuilder([_enrollment]));

      // Test the various ways that grading period can not be set
      final grade = course.getCourseGrade(_studentId, gradingPeriodId: _gradingPeriodId);
      expect(grade, CourseGrade(course, null, forceAllPeriods: false));
    });

    test('returns a course grade with the course and passed in enrollment when course has matching enrollment', () {
      final enrollment = _enrollment.rebuild((b) => b..currentGradingPeriodId = _gradingPeriodId);
      final course = _course.rebuild((b) => b..enrollments = ListBuilder([_enrollment]));

      // Test the various ways that grading period can not be set
      final grade = course.getCourseGrade(_studentId, enrollment: enrollment);
      expect(grade, CourseGrade(course, enrollment, forceAllPeriods: false));
    });
  });

  group('isValidForCurrentStudent', () {
    test('returns true for valid date with valid enrollment', () {
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = true
        ..endAt = futureDate
        ..enrollments = ListBuilder([_enrollment]));

      final isValid = course.isValidForCurrentStudent(_studentId);

      expect(isValid, isTrue);
    });

    test('returns false for valid date with invalid enrollment', () {
      final invalidEnrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..userId = '789');
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = true
        ..endAt = futureDate
        ..enrollments = ListBuilder([invalidEnrollment]));

      final isValid = course.isValidForCurrentStudent(_studentId);

      expect(isValid, isFalse);
    });
  });

  group('gradingScheme', () {
    final gradingSchemeBuilder = ListBuilder<JsonObject>()
      ..add(JsonObject(["A", 0.9]))
      ..add(JsonObject(["F", 0.0]));

    final course = _course.rebuild((b) => b..gradingScheme = gradingSchemeBuilder);

    test('returns "F" for 70 percent', () {
      expect(course.convertScoreToLetterGrade(70, 100), 'F');
    });

    test('returns "A" for 90 percent', () {
      expect(course.convertScoreToLetterGrade(90, 100), 'A');
    });

    test('returns empty if max score is 0', () {
      expect(course.convertScoreToLetterGrade(10, 0), '');
    });

    test('returns empty if grading scheme is null or empty', () {
      final course = _course.rebuild((b) => b..gradingScheme = null);
      expect(course.convertScoreToLetterGrade(10, 0), '');
    });

    test('grading scheme mapping filters out incorrect items', () {
      gradingSchemeBuilder.add(JsonObject(""));
      gradingSchemeBuilder.add(JsonObject([1, 0.9]));
      gradingSchemeBuilder.add(JsonObject(["C", "3"]));
      final course = _course.rebuild((b) => b..gradingScheme = gradingSchemeBuilder);
      expect(course.gradingSchemeItems, [
        GradingSchemeItem((b) => b
          ..grade = "A"
          ..value = 0.9),
        GradingSchemeItem((b) => b
          ..grade = "F"
          ..value = 0.0),
      ]);
    });
  });
}
