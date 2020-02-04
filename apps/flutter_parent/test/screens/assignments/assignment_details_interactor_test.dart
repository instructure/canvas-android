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
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';

void main() {
  final assignmentId = '123';
  final courseId = '321';
  final studentId = '1337';

  final assignmentApi = _MockAssignmentApi();
  final courseApi = _MockCourseApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<AssignmentApi>(() => assignmentApi);
    locator.registerLazySingleton<CourseApi>(() => courseApi);
  });

  // Reset the interactions for the shared mocks
  setUp(() {
    reset(assignmentApi);
    reset(courseApi);
  });

  group('loadAssignmentDetails', () {
    test('returns the course name', () async {
      final course = Course((b) => b..name = 'course name');
      when(courseApi.getCourse(courseId)).thenAnswer((_) async => course);
      final details =
          await AssignmentDetailsInteractor().loadAssignmentDetails(false, courseId, assignmentId, studentId);

      expect(details.course, course);
    });

    test('returns a null alarm if none exist for the assignment', () async {
      when(courseApi.getCourse(courseId)).thenAnswer((_) async => Course((b) => b..name = ''));
      final details =
          await AssignmentDetailsInteractor().loadAssignmentDetails(false, courseId, assignmentId, studentId);

      expect(details.alarm, null);
    });

    // TODO: Test an alarm
//    test('returns an alarm for the assignment', () async {
//      final details =
//          await AssignmentDetailsInteractor().loadAssignmentDetails(false, courseId, assignmentId, studentId);
//
//      expect(details.alarm, Alarm());
//    });

    test('returns an assignment', () async {
      final assignment = Assignment((b) => b
        ..id = assignmentId
        ..courseId = courseId
        ..assignmentGroupId = ''
        ..position = 0);
      when(courseApi.getCourse(courseId)).thenAnswer((_) async => Course((b) => b..name = ''));
      when(assignmentApi.getAssignment(courseId, assignmentId, forceRefresh: false))
          .thenAnswer((_) async => assignment);
      final details =
          await AssignmentDetailsInteractor().loadAssignmentDetails(false, courseId, assignmentId, studentId);

      expect(details.assignment, assignment);
    });
  });
}

class _MockAssignmentApi extends Mock implements AssignmentApi {}

class _MockCourseApi extends Mock implements CourseApi {}
