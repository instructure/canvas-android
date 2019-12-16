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

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

const studentId = '123';
const courseId = '321';
final course = Course((b) => b..id = courseId);

void main() {
  _setupLocator({CourseDetailsInteractor interactor}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<CourseDetailsInteractor>(() => interactor ?? _MockCourseDetailsInteractor());
  }

  test('constructing with a course updates the course id', () {
    final model = CourseDetailsModel.withCourse(studentId, course);

    expect(model.courseId, courseId);
  });

  group('loadData for course', () {
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
  });

  group('loadData for asasignment groups', () {
    test('refreshAssignmentGroups does not load submissions for a failed assignments call', () async {
      final interactor = _MockCourseDetailsInteractor();
      when(interactor.loadAssignmentGroups(courseId, studentId)).thenAnswer((_) {
        // Catch the error here, as it will fail the test if it's uncaught
        return Future<List<AssignmentGroup>>.error('Fail to get groups').catchError((_) {});
      });
      _setupLocator(interactor: interactor);

      final model = CourseDetailsModel.withCourse(studentId, course);
      await model.loadData(refreshAssignmentGroups: true);
      await model.assignmentGroupFuture;

      verify(interactor.loadAssignmentGroups(courseId, studentId)).called(1);
      verifyNever(interactor.loadSubmissions(courseId, studentId, any));
    });

    test('does not refresh assignments if it has data', () async {
      final interactor = _MockCourseDetailsInteractor();
      _setupLocator(interactor: interactor);

      final model = CourseDetailsModel.withCourse(studentId, course);
      model.assignmentGroupFuture = Future.value();
      await model.loadData();

      verifyNever(interactor.loadAssignmentGroups(courseId, studentId));
    });

    test('refreshAssignmentGroups if assignment refresh forced', () async {
      final interactor = _MockCourseDetailsInteractor();
      _setupLocator(interactor: interactor);

      final model = CourseDetailsModel.withCourse(studentId, course);
      await model.loadData(refreshAssignmentGroups: true);

      verify(interactor.loadAssignmentGroups(courseId, studentId)).called(1);
    });

    test('refreshes assignments if assignments is null', () async {
      final expected = List<AssignmentGroup>();
      final interactor = _MockCourseDetailsInteractor();
      when(interactor.loadAssignmentGroups(courseId, studentId)).thenAnswer((_) async => expected);
      _setupLocator(interactor: interactor);

      final model = CourseDetailsModel.withCourse(studentId, course);
      expect(model.assignmentGroupFuture, null);
      await model.loadData();

      verify(interactor.loadAssignmentGroups(courseId, studentId)).called(1);
      expect(await model.assignmentGroupFuture, expected);
    });

    test('refreshes assignments and loads submissions', () async {
      // Setup the data
      final assignmentId = '101';
      final initial = _mockAssignmentGroups(assignmentId);
      final submissions = [Submission((b) => b.assignmentId = assignmentId)];

      // Mock the data
      final interactor = _MockCourseDetailsInteractor();
      when(interactor.loadAssignmentGroups(courseId, studentId)).thenAnswer((_) async => initial);
      when(interactor.loadSubmissions(courseId, studentId, [assignmentId])).thenAnswer((_) async => submissions);
      _setupLocator(interactor: interactor);

      // Use the model
      final model = CourseDetailsModel.withCourse(studentId, course);
      await model.loadData();

      // Test the model
      expect((await model.assignmentGroupFuture).first.assignments.first.submission, submissions.first);
      verify(interactor.loadAssignmentGroups(courseId, studentId)).called(1);
      verify(interactor.loadSubmissions(courseId, studentId, [assignmentId])).called(1);
    });

    test('refreshes assignments and does not load submissions for unpublished assignments', () async {
      // Setup the data
      final assignmentId = '101';
      final initial = _mockAssignmentGroups(assignmentId, published: false);

      // Mock the data
      final interactor = _MockCourseDetailsInteractor();
      when(interactor.loadAssignmentGroups(courseId, studentId)).thenAnswer((_) async => initial);
      _setupLocator(interactor: interactor);

      // Use the model
      final model = CourseDetailsModel.withCourse(studentId, course);
      await model.loadData();

      // Test the model
      expect((await model.assignmentGroupFuture).first.assignments, isEmpty);
      verify(interactor.loadAssignmentGroups(courseId, studentId)).called(1);
      verifyNever(interactor.loadSubmissions(courseId, studentId, any));
    });
  });
}

List<AssignmentGroup> _mockAssignmentGroups(String assignmentId, {bool published = true}) {
  return [
    AssignmentGroup((group) => group
      ..id = '202'
      ..name = 'assignments'
      ..groupWeight = 0
      ..position = 0
      ..assignments = BuiltList.of([
        Assignment((b) => b
          ..id = assignmentId
          ..courseId = courseId
          ..assignmentGroupId = group.id
          ..published = published
          ..position = 0)
      ]).toBuilder())
  ];
}

class _MockCourseDetailsInteractor extends Mock implements CourseDetailsInteractor {}
