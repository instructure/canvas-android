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
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/page_api.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final courseId = '123';
  final studentId = '1337';
  final gradingPeriodId = '321';

  final MockCourseApi courseApi = MockCourseApi();
  final MockAssignmentApi assignmentApi = MockAssignmentApi();
  final enrollmentApi = MockEnrollmentsApi();
  final MockCalendarEventsApi calendarApi = MockCalendarEventsApi();
  final MockPageApi pageApi = MockPageApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<CourseApi>(() => courseApi);
    locator.registerLazySingleton<AssignmentApi>(() => assignmentApi);
    locator.registerLazySingleton<EnrollmentsApi>(() => enrollmentApi);
    locator.registerLazySingleton<CalendarEventsApi>(() => calendarApi);
    locator.registerLazySingleton<PageApi>(() => pageApi);
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

  test('load schedule items calls the API', () async {
    CourseDetailsInteractor().loadScheduleItems('123', 'type', true);

    verify(
      calendarApi.getAllCalendarEvents(
        allEvents: true,
        type: 'type',
        startDate: null,
        endDate: null,
        contexts: ['course_123'],
        forceRefresh: true,
      ),
    );
  });

  test('load home page calls the api', () {
    final courseId = '123';
    CourseDetailsInteractor().loadFrontPage(courseId, forceRefresh: true);
    verify(pageApi.getCourseFrontPage(courseId, forceRefresh: true)).called(1);
  });

  test('load course tabs calls the api', () {
    final courseId = '123';
    CourseDetailsInteractor().loadCourseTabs(courseId, forceRefresh: true);
    verify(courseApi.getCourseTabs(courseId, forceRefresh: true)).called(1);
  });

  test('load course settings calls the api', () {
    final courseId = '123';
    CourseDetailsInteractor().loadCourseSettings(courseId, forceRefresh: true);
    verify(courseApi.getCourseSettings(courseId, forceRefresh: true)).called(1);
  });
}
