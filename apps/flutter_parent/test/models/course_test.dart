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
import 'package:flutter_parent/models/section.dart';
import 'package:flutter_parent/models/term.dart';
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
      expect(grade, CourseGrade(course, course.enrollments.first, forceAllPeriods: false));
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

  group('isValidForParent', () {
    test('returns false for accessRestrictedByDate', () {
      final course = _course.rebuild((b) => b..accessRestrictedByDate = true);

      final isValid = course.isValidForDate();

      expect(isValid, isFalse);
    });

    test('returns false for workFlowState completed', () {
      final course = _course.rebuild((b) => b
        ..workflowState = 'completed'
        ..accessRestrictedByDate = false);

      final isValid = course.isValidForDate();

      expect(isValid, isFalse);
    });

    test('returns false for restrictEnrollmentsToCourseDates and !isWithinCourseDates', () {
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = true
        ..startAt = futureDate
        ..endAt = pastDate);

      final isValid = course.isValidForDate();

      expect(isValid, isFalse);
    });

    test('returns true for restrictEnrollmentsToCourseDates with isWithinCourseDates', () {
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = true
        ..startAt = pastDate
        ..endAt = futureDate);

      final isValid = course.isValidForDate();

      expect(isValid, isTrue);
    });

    test('returns false for !restrictEnrollmentsToCourseDates and !isWithinCourseDates & !isWithinTermDates', () {
      final term = Term((b) => b
        ..id = ''
        ..startAt = pastDate
        ..endAt = pastDate);
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = false
        ..startAt = pastDate
        ..endAt = pastDate
        ..term = term.toBuilder());

      final isValid = course.isValidForDate();

      expect(isValid, isFalse);
    });

    test(
        'returns false for !restrictEnrollmentsToCourseDates and !isWithinCourseDates & !isWithinTermDates & !isWithinAnySection',
        () {
      final section = Section((b) => b
        ..id = ''
        ..name = ''
        ..startAt = pastDate
        ..endAt = pastDate);
      final term = Term((b) => b
        ..id = ''
        ..startAt = pastDate
        ..endAt = pastDate);
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = false
        ..endAt = pastDate
        ..startAt = pastDate
        ..term = term.toBuilder()
        ..sections = ListBuilder([section]));

      final isValid = course.isValidForDate();

      expect(isValid, isFalse);
    });

    test('returns true for restrictEnrollmentsToCourseDates and null course dates', () {
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = true
        ..startAt = null
        ..endAt = null);

      final isValid = course.isValidForDate();

      expect(isValid, isTrue);
    });

    test(
        'returns true for !restrictEnrollmentsToCourseDates with isWithinCourseDates & isWithinTermDates & isWithinAnySection',
        () {
      final section = Section((b) => b
        ..id = ''
        ..name = ''
        ..startAt = pastDate
        ..endAt = futureDate);
      final term = Term((b) => b
        ..id = ''
        ..startAt = pastDate
        ..endAt = futureDate);
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = false
        ..endAt = futureDate
        ..startAt = pastDate
        ..term = term.toBuilder()
        ..sections = ListBuilder([section]));

      final isValid = course.isValidForDate();

      expect(isValid, isTrue);
    });

    test(
        'returns true for !restrictEnrollmentsToCourseDates with null dates for isWithinCourseDates & isWithinTermDates & isWithinAnySection',
        () {
      final section = Section((b) => b
        ..id = ''
        ..name = ''
        ..startAt = null
        ..endAt = null);
      final term = Term((b) => b
        ..id = ''
        ..startAt = null
        ..endAt = null);
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = false
        ..endAt = null
        ..startAt = null
        ..term = term.toBuilder()
        ..sections = ListBuilder([section]));

      final isValid = course.isValidForDate();

      expect(isValid, isTrue);
    });

    test('returns true for !restrictEnrollmentsToCourseDates with null course, section, and term dates', () {
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = false
        ..endAt = null
        ..startAt = null
        ..term = null
        ..sections = null);

      final isValid = course.isValidForDate();

      expect(isValid, isTrue);
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

    test('returns false for invalid date with valid enrollment', () {
      final course = _course.rebuild((b) => b
        ..accessRestrictedByDate = false
        ..restrictEnrollmentsToCourseDates = true
        ..endAt = pastDate
        ..enrollments = ListBuilder([_enrollment]));

      final isValid = course.isValidForCurrentStudent(_studentId);

      expect(isValid, isFalse);
    });
  });
}
