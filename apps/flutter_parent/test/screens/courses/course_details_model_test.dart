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

import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

void main() {
  final studentId = 123;
  final courseId = 321;
  final course = Course((b) => b..id = courseId);

  _setupLocator({CourseDetailsInteractor interactor}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<CourseDetailsInteractor>(() => interactor ?? _MockCourseDetailsInteractor());
  }

  test('constructing with a course updates the course id', () {
    final model = CourseDetailsModel.withCourse(studentId, course);

    expect(model.courseId, courseId);
  });

  group('loadData', () {
    test('does not refresh course if it has data', () async {
      final interactor = _MockCourseDetailsInteractor();
      _setupLocator(interactor: interactor);
      final model = CourseDetailsModel.withCourse(studentId, course);

      await model.loadData();

      verifyNever(interactor.loadCourse(courseId));
      expect(model.course, course);
    });

    test('refreshes course if course refresh forced', () async {
      final expected = null;
      final interactor = _MockCourseDetailsInteractor();
      when(interactor.loadCourse(courseId)).thenAnswer((_) => Future.value(expected));
      _setupLocator(interactor: interactor);
      final model = CourseDetailsModel.withCourse(studentId, course);

      await model.loadData(refreshCourse: true);

      verify(interactor.loadCourse(courseId)).called(1);
      expect(model.course, expected);
    });

    test('refreshes course if course is null', () async {
      final expected = null;
      final interactor = _MockCourseDetailsInteractor();
      when(interactor.loadCourse(courseId)).thenAnswer((_) => Future.value(expected));
      _setupLocator(interactor: interactor);
      final model = CourseDetailsModel(studentId, courseId);

      await model.loadData();

      verify(interactor.loadCourse(courseId)).called(1);
      expect(model.course, expected);
    });

    test('does not refresh assignments if it has data', () async {
      final assignments = List<AssignmentGroup>();
      final interactor = _MockCourseDetailsInteractor();
      _setupLocator(interactor: interactor);
      final model = CourseDetailsModel.withCourse(studentId, course);
      model.assignmentGroups = assignments;

      await model.loadData();

      verifyNever(interactor.loadAssignmentGroups(courseId, studentId));
      expect(model.assignmentGroups, assignments);
    });

    test('refreshes assignments if assignment refresh forced', () async {
      final expected = null;
      final interactor = _MockCourseDetailsInteractor();
      when(interactor.loadAssignmentGroups(courseId, studentId)).thenAnswer((_) => Future.value(expected));
      _setupLocator(interactor: interactor);
      final model = CourseDetailsModel.withCourse(studentId, course);
      model.assignmentGroups = List();

      await model.loadData(refreshAssignments: true);

      verify(interactor.loadAssignmentGroups(courseId, studentId)).called(1);
      expect(model.assignmentGroups, expected);
    });

    test('refreshes assignments if assignments is null', () async {
      final expected = List<AssignmentGroup>();
      final interactor = _MockCourseDetailsInteractor();
      when(interactor.loadAssignmentGroups(courseId, studentId)).thenAnswer((_) => Future.value(expected));
      _setupLocator(interactor: interactor);
      final model = CourseDetailsModel.withCourse(studentId, course);

      expect(model.assignmentGroups, null);
      await model.loadData();

      verify(interactor.loadAssignmentGroups(courseId, studentId)).called(1);
      expect(model.assignmentGroups, expected);
    });
  });
}

class _MockCourseDetailsInteractor extends Mock implements CourseDetailsInteractor {}
