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
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../../utils/canvas_model_utils.dart';
import '../../../utils/test_app.dart';

void main() {
  test('getCourses calls API', () async {
    final api = _MockCourseApi();
    setupTestLocator((locator) => locator.registerLazySingleton<CourseApi>(() => api));
    when(api.getObserveeCourses(forceRefresh: true)).thenAnswer((_) async => []);

    await CalendarFilterListInteractor().getCoursesForSelectedStudent(isRefresh: true);

    verify(api.getObserveeCourses(forceRefresh: true));
  });

  test('getCourses filters by student enrollment', () async {
    final studentId = 'student123';
    await setupPlatformChannels();
    User user = CanvasModelTestUtils.mockUser(id: studentId);
    await ApiPrefs.setCurrentStudent(user);

    final apiCourses = [
      Course(),
      Course((c) => c
        ..enrollments = ListBuilder([
          Enrollment((e) => e
            ..userId = studentId
            ..enrollmentState = '')
        ])),
      Course(),
    ];
    final expectedCourses = [apiCourses[1]];

    final api = _MockCourseApi();
    setupTestLocator((locator) => locator.registerLazySingleton<CourseApi>(() => api));

    when(api.getObserveeCourses(forceRefresh: true)).thenAnswer((_) async => apiCourses);

    final actualCourses = await CalendarFilterListInteractor().getCoursesForSelectedStudent(isRefresh: true);

    expect(actualCourses, expectedCourses);
  });
}

class _MockCourseApi extends Mock implements CourseApi {}
