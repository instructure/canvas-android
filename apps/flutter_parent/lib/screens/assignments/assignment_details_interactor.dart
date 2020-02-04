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
import 'package:flutter_parent/models/alarm.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AssignmentDetailsInteractor {
  Future<AssignmentDetails> loadAssignmentDetails(
    bool forceRefresh,
    String courseId,
    String assignmentId,
    String studentId,
  ) async {
    final course = locator<CourseApi>().getCourse(courseId);
    final assignment = locator<AssignmentApi>().getAssignment(courseId, assignmentId, forceRefresh: forceRefresh);
    final alarm = null; // TODO: Load alarm from database

    return AssignmentDetails(
      assignment: (await assignment),
      course: (await course),
      alarm: alarm,
    );
  }
}

class AssignmentDetails {
  final Alarm alarm;
  final Course course;
  final Assignment assignment;

  AssignmentDetails({this.alarm, this.course, this.assignment});
}
