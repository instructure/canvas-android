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

import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';

void main() {
  final courseId = '123';
  final studentId = '1337';
  final gradingPeriodId = '321';

  final _MockCourseApi courseApi = _MockCourseApi();
  final _MockAssignmentApi assignmentApi = _MockAssignmentApi();
  final _MockEnrollmentApi enrollmentApi = _MockEnrollmentApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<CourseApi>(() => courseApi);
    locator.registerLazySingleton<AssignmentApi>(() => assignmentApi);
    locator.registerLazySingleton<EnrollmentsApi>(() => enrollmentApi);
  });

  test('load course calls the api', () async {
    CourseDetailsInteractor().loadCourse(courseId);

    verify(courseApi.getCourse(courseId)).called(1);
  });

  test('load assignments calls the api', () async {
    CourseDetailsInteractor().loadAssignmentGroups(courseId, studentId, gradingPeriodId, forceRefresh: true);

    verify(assignmentApi.getAssignmentGroupsWithSubmissionsDepaginated(courseId, studentId, gradingPeriodId,
            forceRefresh: true))
        .called(1);
  });

  test('load grading periods calls the api', () async {
    CourseDetailsInteractor().loadGradingPeriods(courseId, forceRefresh: true);

    verify(courseApi.getGradingPeriods(courseId, forceRefresh: true)).called(1);
  });

  test('load enrollments calls the api', () async {
    CourseDetailsInteractor().loadEnrollmentsForGradingPeriod(courseId, studentId, gradingPeriodId, forceRefresh: true);

    verify(enrollmentApi.getEnrollmentsByGradingPeriod(courseId, studentId, gradingPeriodId, forceRefresh: true))
        .called(1);
  });
}

class _MockCourseApi extends Mock implements CourseApi {}

class _MockAssignmentApi extends Mock implements AssignmentApi {}

class _MockEnrollmentApi extends Mock implements EnrollmentsApi {}
