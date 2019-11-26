/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter_parent/api/assignment_api.dart';
import 'package:flutter_parent/api/course_api.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CourseDetailsInteractor {
  Future<Course> loadCourse(int courseId) {
    return locator<CourseApi>().getCourse(courseId);
  }

  Future<List<AssignmentGroup>> loadAssignmentGroups(int courseId, int studentId) {
    return locator<AssignmentApi>().getAssignmentGroupsWithSubmissionsDepaginated(courseId, studentId);
  }

  // TODO: Remove once LA-274 is implemented, and submissions are given with assignment groups (for observers)
  Future<List<Submission>> loadSubmissions(
    int courseId,
    int studentId,
    List<int> assignmentIds, {
    bool forceRefresh = false,
  }) {
    return locator<AssignmentApi>().getSubmissions(courseId, studentId, assignmentIds, forceRefresh: forceRefresh);
  }
}
