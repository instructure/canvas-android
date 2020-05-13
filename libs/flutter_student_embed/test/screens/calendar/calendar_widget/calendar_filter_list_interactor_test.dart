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
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../../testutils/canvas_model_utils.dart';
import '../../../testutils/mock_helpers.dart';
import '../../../testutils/platform_config.dart';
import '../../../testutils/test_app.dart';

void main() {
  final _api = MockCourseApi();
  final _user = CanvasModelTestUtils.mockUser(id: '123');

  setupTestLocator((locator) {
    locator.registerLazySingleton<CourseApi>(() => _api);
  });

  setUp(() async {
    reset(_api);
    await setupPlatformChannels(
        //config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(_user))}));
        config: PlatformConfig());
  });

  test('getCourses calls API', () async {
    when(_api.getCourses(forceRefresh: true)).thenAnswer((_) async => []);

    await CalendarFilterListInteractor().getCoursesForUser(isRefresh: true);

    verify(_api.getCourses(forceRefresh: true));
  });

  // TODO: Figure out what the desired filtering behavior is for Student
//  test('getCourses filters by student enrollment', () async {
//    final apiCourses = [
//      Course(),
//      Course((c) => c
//        ..enrollments = ListBuilder([
//          Enrollment((e) => e
//            ..userId = _user.id
//            ..enrollmentState = '')
//        ])),
//      Course(),
//    ];
//    final expectedCourses = [apiCourses[1]];
//
//    when(_api.getCourses(forceRefresh: true)).thenAnswer((_) async => apiCourses);
//
//    final actualCourses = await CalendarFilterListInteractor().getCoursesForUser(isRefresh: true);
//
//    expect(actualCourses, expectedCourses);
//  });
//
//  test('getCourses filters by valid course dates', () async {
//    final apiCourses = [
//      Course((c) => c
//        ..accessRestrictedByDate = true
//        ..enrollments = ListBuilder([
//          Enrollment((e) => e
//            ..userId = _user.id
//            ..enrollmentState = '')
//        ])),
//      Course((c) => c
//        ..accessRestrictedByDate = false
//        ..endAt = DateTime.now().add(Duration(days: 10)).toIso8601String()
//        ..enrollments = ListBuilder([
//          Enrollment((e) => e
//            ..userId = _user.id
//            ..enrollmentState = '')
//        ]))
//    ];
//    final expectedCourses = [apiCourses[1]];
//
//    when(_api.getCourses(forceRefresh: true)).thenAnswer((_) async => apiCourses);
//
//    final actualCourses = await CalendarFilterListInteractor().getCoursesForUser(isRefresh: true);
//
//    expect(actualCourses, expectedCourses);
//  });
}
