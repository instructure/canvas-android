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

import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final _api = MockCourseApi();

  final _studentId = '123';
  final _invalidStudentId = '789';

  final _futureDate = DateTime.now().add(Duration(days: 10));
  final _pastDate = DateTime.now().subtract(Duration(days: 10));

  var _student = User((b) => b
    ..id = _studentId
    ..name = 'UserName'
    ..sortableName = 'Sortable Name'
    ..build());

  final _enrollment = Enrollment((b) => b
    ..enrollmentState = 'active'
    ..userId = _studentId);

  final _invalidEnrollment = Enrollment((b) => b
    ..enrollmentState = 'active'
    ..userId = _invalidStudentId);

  final _course = Course((b) => b
    ..id = 'course_123'
    ..accessRestrictedByDate = false
    ..restrictEnrollmentsToCourseDates = true
    ..endAt = _futureDate
    ..enrollments = ListBuilder([_enrollment]));

  final _invalidCourse = Course((b) => b
    ..id = 'course_456'
    ..accessRestrictedByDate = false
    ..restrictEnrollmentsToCourseDates = true
    ..endAt = _futureDate
    ..enrollments = ListBuilder([_invalidEnrollment]));

  setupTestLocator((locator) {
    locator.registerLazySingleton<CourseApi>(() => _api);
  });

  setUp(() async {
    reset(_api);
    await setupPlatformChannels(
        config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(_student))}));
  });

  test('getCourses calls API', () async {
    when(_api.getObserveeCourses(forceRefresh: true)).thenAnswer((_) async => []);

    await CoursesInteractor().getCourses(isRefresh: true, studentId: null);

    verify(_api.getObserveeCourses(forceRefresh: true));
  });

  test('getCourses returns only courses for ApiPrefs.currentStudent.id and is valid', () async {
    when(_api.getObserveeCourses(forceRefresh: true)).thenAnswer((_) async => [_course, _invalidCourse]);

    final result = await CoursesInteractor().getCourses(isRefresh: true, studentId: null);

    verify(_api.getObserveeCourses(forceRefresh: true));
    expect(result?.length, 1);
    expect(result?.first.id, _course.id);
  });

  test('getCourses returns only courses for studentId parameter and is valid', () async {
    when(_api.getObserveeCourses(forceRefresh: true)).thenAnswer((_) async => [_course, _invalidCourse]);

    final result = await CoursesInteractor().getCourses(isRefresh: true, studentId: _invalidStudentId);

    verify(_api.getObserveeCourses(forceRefresh: true));
    expect(result?.length, 1);
    expect(result?.first.id, _invalidCourse.id);
  });

  test('getCourses returns no courses for invalid studentId but valid dates', () async {
    when(_api.getObserveeCourses(forceRefresh: true)).thenAnswer((_) async => [_invalidCourse]);

    final result = await CoursesInteractor().getCourses(isRefresh: true, studentId: _studentId);

    verify(_api.getObserveeCourses(forceRefresh: true));
    expect(result, isEmpty);
  });
}
