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
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grading_period.dart';
import 'package:flutter_parent/models/grading_period_response.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';

const _studentId = '123';
const _courseId = '321';
final _course = Course((b) => b..id = _courseId);

void main() {
  final _MockCourseDetailsInteractor interactor = _MockCourseDetailsInteractor();

  setupTestLocator((locator) {
    locator.registerFactory<CourseDetailsInteractor>(() => interactor);
  });

  setUp(() {
    reset(interactor);
  });

  test('constructing with a course updates the course id', () {
    final model = CourseDetailsModel.withCourse(_studentId, '', _course);

    expect(model.courseId, _courseId);
  });

  group('loadData for course', () {
    test('does not refresh course if it has data', () async {
      final model = CourseDetailsModel.withCourse(_studentId, '', _course);

      await model.loadData();

      verifyNever(interactor.loadCourse(_courseId));
      expect(model.course, _course);
    });

    test('refreshes course if course refresh forced', () async {
      final expected = null;
      when(interactor.loadCourse(_courseId)).thenAnswer((_) => Future.value(expected));
      final model = CourseDetailsModel.withCourse(_studentId, '', _course);

      await model.loadData(refreshCourse: true);

      verify(interactor.loadCourse(_courseId)).called(1);
      expect(model.course, expected);
    });

    test('refreshes course if course is null', () async {
      final expected = null;
      when(interactor.loadCourse(_courseId)).thenAnswer((_) => Future.value(expected));
      final model = CourseDetailsModel(_studentId, '', _courseId);

      await model.loadData();

      verify(interactor.loadCourse(_courseId)).called(1);
      expect(model.course, expected);
    });
  });

  group('loadAssignments', () {
    test('returns grade details', () async {
      // Initial setup
      final termEnrollment = Enrollment((b) => b
        ..id = '10'
        ..enrollmentState = 'active');
      final gradingPeriods = [
        GradingPeriod((b) => b
          ..id = '123'
          ..title = 'Grade Period 1')
      ];
      final assignmentGroups = [
        AssignmentGroup((b) => b
          ..id = '111'
          ..name = 'Assignment Group 1')
      ];

      // Mock the data
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => [termEnrollment, termEnrollment.rebuild((b) => b..id = '20')]);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
          (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(gradingPeriods).toBuilder()));
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => assignmentGroups);

      // Make the call to test
      final model = CourseDetailsModel.withCourse(_studentId, '', _course);
      final gradeDetails = await model.loadAssignments();

      expect(gradeDetails.termEnrollment, termEnrollment); // Should match only the first enrollment
      expect(gradeDetails.gradingPeriods, gradingPeriods);
      expect(gradeDetails.assignmentGroups, assignmentGroups);
    });

    test('Does not fail with an empty group response', () async {
      // Mock the data with null response
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => null);

      // Make the call to test
      final model = CourseDetailsModel.withCourse(_studentId, '', _course);
      var gradeDetails = await model.loadAssignments();

      expect(gradeDetails.assignmentGroups, null);

      // Test again with empty array
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => []);
      gradeDetails = await model.loadAssignments();

      expect(gradeDetails.assignmentGroups, []);
    });

    test('Removes unpublished assignments from assignment groups', () async {
      // Init setup
      final publishedAssignments = [
        Assignment((b) => b
          ..id = '101'
          ..courseId = _courseId
          ..assignmentGroupId = '111'
          ..position = 0
          ..published = true)
      ];
      final unpublishedAssignments = [
        Assignment((b) => b
          ..id = '102'
          ..courseId = _courseId
          ..assignmentGroupId = '222'
          ..position = 0)
      ];
      final publishedGroup = AssignmentGroup((b) => b
        ..id = '111'
        ..name = 'Group 1'
        ..assignments = BuiltList.of(publishedAssignments).toBuilder());
      final unpublishedGroup = AssignmentGroup((b) => b
        ..id = '222'
        ..name = 'Group 2'
        ..assignments = BuiltList.of(unpublishedAssignments).toBuilder());

      final assignmentGroups = [
        publishedGroup,
        unpublishedGroup,
      ];

      // Mock the data with null response
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => assignmentGroups);

      // Make the call to test
      final model = CourseDetailsModel.withCourse(_studentId, '', _course);
      final gradeDetails = await model.loadAssignments();

      expect(gradeDetails.assignmentGroups, [
        publishedGroup,
        unpublishedGroup.rebuild((b) => b..assignments = BuiltList.of(List<Assignment>()).toBuilder())
      ]);
    });

    test('Updates currentGradingPeriod when load finishes', () async {
      // Init setup
      final gradingPeriod = GradingPeriod((b) => b
        ..id = '1'
        ..title = 'Period 1');

      // Create the model
      final model = CourseDetailsModel.withCourse(_studentId, '', _course);

      // Update the grading period, but it shouldn't percolate until a load is called
      model.updateGradingPeriod(gradingPeriod);
      expect(model.currentGradingPeriod(), null);

      await model.loadAssignments();
      expect(model.currentGradingPeriod(), gradingPeriod);

      // Verify the updated grading period was used in api calls
      verify(interactor.loadAssignmentGroups(_courseId, _studentId, gradingPeriod.id, forceRefresh: false)).called(1);
      verify(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, gradingPeriod.id, forceRefresh: false))
          .called(1);
    });
  });
}

List<AssignmentGroup> _mockAssignmentGroups(String assignmentId,
    {bool published = true, List<Submission> submissions}) {
  return [
    AssignmentGroup((group) => group
      ..id = '202'
      ..name = 'assignments'
      ..groupWeight = 0
      ..position = 0
      ..assignments = BuiltList.of([
        Assignment((b) => b
          ..id = assignmentId
          ..courseId = _courseId
          ..assignmentGroupId = group.id
          ..published = published
          ..position = 0
          ..submissionList = BuiltList.of(submissions ?? <Submission>[]).toBuilder())
      ]).toBuilder())
  ];
}

class _MockCourseDetailsInteractor extends Mock implements CourseDetailsInteractor {}
