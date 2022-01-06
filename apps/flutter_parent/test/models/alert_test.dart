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

import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Alert type is converted to correct api string', () {
    expect('course_announcement', AlertType.courseAnnouncement.toApiString());
    expect('institution_announcement', AlertType.institutionAnnouncement.toApiString());
    expect('assignment_grade_high', AlertType.assignmentGradeHigh.toApiString());
    expect('assignment_grade_low', AlertType.assignmentGradeLow.toApiString());
    expect('assignment_missing', AlertType.assignmentMissing.toApiString());
    expect('course_grade_high', AlertType.courseGradeHigh.toApiString());
    expect('course_grade_low', AlertType.courseGradeLow.toApiString());
  });

  test('isSwitch returns true when switch, false when not', () {
    expect(true, AlertType.assignmentMissing.isSwitch());
    expect(true, AlertType.courseAnnouncement.isSwitch());
    expect(true, AlertType.institutionAnnouncement.isSwitch());
    expect(true, AlertType.assignmentMissing.isSwitch());
    expect(false, AlertType.courseGradeLow.isSwitch());
    expect(false, AlertType.courseGradeHigh.isSwitch());
    expect(false, AlertType.assignmentGradeLow.isSwitch());
    expect(false, AlertType.assignmentGradeHigh.isSwitch());
  });

  test('returns valid courseId for course announcement alert', () {
    final courseId = '1234';
    final alert = Alert((b) => b
      ..id = '123'
      ..title = 'Hodor'
      ..workflowState = AlertWorkflowState.unread
      ..htmlUrl = 'https://instructure.com/api/v1/courses/$courseId/discussion_topics/1234'
      ..alertType = AlertType.courseAnnouncement
      ..lockedForUser = false);

    expect(alert.getCourseIdForAnnouncement(), courseId);
  });

  test('assertion fails for not a course announcement alert', () {
    final alert = Alert((b) => b
      ..id = '123'
      ..title = 'Hodor'
      ..workflowState = AlertWorkflowState.unread
      ..alertType = AlertType.institutionAnnouncement
      ..lockedForUser = false);

    expect(() {
      alert.getCourseIdForAnnouncement();
    }, throwsA(isA<AssertionError>()));
  });
}
