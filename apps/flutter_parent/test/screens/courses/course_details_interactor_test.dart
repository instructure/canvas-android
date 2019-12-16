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

import 'package:flutter_parent/api/assignment_api.dart';
import 'package:flutter_parent/api/course_api.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

void main() {
  _setupLocator({CourseApi courseApi, AssignmentApi assignmentApi}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerLazySingleton<CourseApi>(() => courseApi ?? _MockCourseApi());
    _locator.registerLazySingleton<AssignmentApi>(() => assignmentApi ?? _MockAssignmentApi());
  }

  test('load course calls the api', () async {
    final courseId = '123';
    final courseApi = _MockCourseApi();
    _setupLocator(courseApi: courseApi);

    CourseDetailsInteractor().loadCourse(courseId);

    verify(courseApi.getCourse(courseId)).called(1);
  });

  test('load assignments calls the api', () async {
    final courseId = '123';
    final studentId = '321';
    final assignmentApi = _MockAssignmentApi();
    _setupLocator(assignmentApi: assignmentApi);

    CourseDetailsInteractor().loadAssignmentGroups(courseId, studentId);

    verify(assignmentApi.getAssignmentGroupsWithSubmissionsDepaginated(courseId, studentId)).called(1);
  });

  test('load submissions calls the api', () async {
    final courseId = '123';
    final studentId = '321';
    final assignmentIds = ['1', '2', '3'];
    final assignmentApi = _MockAssignmentApi();
    _setupLocator(assignmentApi: assignmentApi);

    CourseDetailsInteractor().loadSubmissions(courseId, studentId, assignmentIds);

    verify(assignmentApi.getSubmissions(courseId, studentId, assignmentIds, forceRefresh: false)).called(1);
  });

  test('load submissions calls the api with force refresh', () async {
    final courseId = '123';
    final studentId = '321';
    final assignmentIds = ['1', '2', '3'];
    final assignmentApi = _MockAssignmentApi();
    _setupLocator(assignmentApi: assignmentApi);

    CourseDetailsInteractor().loadSubmissions(courseId, studentId, assignmentIds, forceRefresh: true);

    verify(assignmentApi.getSubmissions(courseId, studentId, assignmentIds, forceRefresh: true)).called(1);
  });
}

class _MockCourseApi extends Mock implements CourseApi {}

class _MockAssignmentApi extends Mock implements AssignmentApi {}
