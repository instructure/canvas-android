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
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../../utils/canvas_model_utils.dart';
import '../../../utils/platform_config.dart';
import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final _api = MockCourseApi();
  final _user = CanvasModelTestUtils.mockUser(id: '123');

  setupTestLocator((locator) {
    locator.registerLazySingleton<CourseApi>(() => _api);
  });

  setUp(() async {
    reset(_api);
    await setupPlatformChannels(
        config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(_user))}));
  });

  test('getCourses calls API', () async {
    when(_api.getObserveeCourses(forceRefresh: true)).thenAnswer((_) async => []);

    await CalendarFilterListInteractor().getCoursesForSelectedStudent(isRefresh: true);

    verify(_api.getObserveeCourses(forceRefresh: true));
  });

  test('getCourses filters by student enrollment', () async {
    final apiCourses = [
      Course(),
      Course((c) => c
        ..enrollments = ListBuilder([
          Enrollment((e) => e
            ..userId = _user.id
            ..enrollmentState = '')
        ])),
      Course(),
    ];
    final expectedCourses = [apiCourses[1]];

    when(_api.getObserveeCourses(forceRefresh: true)).thenAnswer((_) async => apiCourses);

    final actualCourses = await CalendarFilterListInteractor().getCoursesForSelectedStudent(isRefresh: true);

    expect(actualCourses, expectedCourses);
  });
}
