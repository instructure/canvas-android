/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter_parent/models/canvas_page.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/page_api.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_interactor.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_screen.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final mockCourseApi = MockCourseApi();
  final mockPageApi = MockPageApi();
  final interactor = CourseRoutingShellInteractor();

  final course = Course((b) => b
    ..id = '123'
    ..name = 'course name'
    ..syllabusBody = 'hodor');

  final page = CanvasPage((b) => b
    ..id = '123'
    ..body = 'hodor'
    ..hideFromStudents = false
    ..frontPage = false
    ..published = false);

  setupTestLocator((locator) {
    locator.registerLazySingleton<CourseApi>(() => mockCourseApi);
    locator.registerLazySingleton<PageApi>(() => mockPageApi);
  });

  setUp(() {
    reset(mockCourseApi);
    reset(mockPageApi);
  });

  test('returns valid course and null front page for syllabus type', () async {
    when(mockCourseApi.getCourse(any)).thenAnswer((_) => Future.value(course));

    final result = await interactor.loadCourseShell(CourseShellType.syllabus, course.id);

    expect(result?.frontPage, isNull);
    expect(result?.course, isNotNull);
  });

  test('returns error when course syllabus is null for syllabus type', () async {
    final courseNullSyllabus = Course((b) => b
      ..id = '123'
      ..name = 'course name'
      ..syllabusBody = null);

    when(mockCourseApi.getCourse(any)).thenAnswer((_) => Future.value(courseNullSyllabus));

    bool fail = false;
    await interactor.loadCourseShell(CourseShellType.syllabus, courseNullSyllabus.id).catchError((_) {
      fail = true;
    });

    expect(fail, isTrue);
  });

  test('returns valid course and front page for frontPage type', () async {
    when(mockCourseApi.getCourse(any)).thenAnswer((_) => Future.value(course));
    when(mockPageApi.getCourseFrontPage(course.id)).thenAnswer((_) => Future.value(page));

    final result = await interactor.loadCourseShell(CourseShellType.frontPage, course.id);

    expect(result?.frontPage, isNotNull);
    expect(result?.course, isNotNull);
  });

  test('returns error when course front page hass null body for frontPage type', () async {
    when(mockCourseApi.getCourse(any)).thenAnswer((_) => Future.value(course));
    when(mockPageApi.getCourseFrontPage(course.id)).thenAnswer((_) => Future.value(null));

    bool fail = false;
    await interactor.loadCourseShell(CourseShellType.frontPage, course.id).catchError((_) {
      fail = true;
    });

    expect(fail, isTrue);
  });
}
