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

import 'package:flutter_student_embed/network/api/course_api.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../testutils/mock_helpers.dart';
import '../../testutils/test_app.dart';

void main() {
  MockCourseApi courseApi = MockCourseApi();
  MockPlannerApi plannerApi = MockPlannerApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<CourseApi>(() => courseApi);
    locator.registerLazySingleton<PlannerApi>(() => plannerApi);
  });

  tearDown(() {
    reset(courseApi);
    reset(plannerApi);
  });

  test('getCoursesForUser calls CourseApi with correct params', () {
    CreateUpdateToDoScreenInteractor().getCoursesForUser(isRefresh: true);
    verify(courseApi.getCourses(forceRefresh: true));
  });

  test('createToDo calls PlannerAPI with correct params', () {
    String title = 'Test Title';
    String description = 'Test Description';
    DateTime date = DateTime.now();
    String courseId = 'course_123';
    CreateUpdateToDoScreenInteractor().createToDo(title, description, date, courseId);
    verify(plannerApi.createPlannerNote(title, description, date, courseId));
  });

  test('updateToDo calls PlannerAPI with correct params', () {
    String id = 'planner_note_123';
    String title = 'Test Title';
    String description = 'Test Description';
    DateTime date = DateTime.now();
    String courseId = 'course_123';
    CreateUpdateToDoScreenInteractor().updateToDo(id, title, description, date, courseId);
    verify(plannerApi.updatePlannerNote(id, title, description, date, courseId));
  });
}
