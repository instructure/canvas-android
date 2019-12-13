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

import 'package:flutter_parent/api/utils/dio_config.dart';
import 'package:flutter_parent/api/utils/fetch.dart';
import 'package:flutter_parent/api/utils/paged_list.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/submission.dart';

class AssignmentApi {
  Future<List<Assignment>> getAssignmentsWithSubmissionsDepaginated(int courseId, int studentId) async {
    var dio = canvasDio();
    var params = {
      'include': ['all_dates', 'overrides', 'rubric_assessment', 'submission'],
      'order_by': 'due_at',
      'override_assignment_dates': 'true',
      'needs_grading_count_by_section': 'true',
    };
    return fetchList(dio.get('courses/$courseId/assignments', queryParameters: params), depaginateWith: dio);
  }

  Future<List<AssignmentGroup>> getAssignmentGroupsWithSubmissionsDepaginated(int courseId, int studentId) async {
    var dio = canvasDio();
    var params = {
      'include': [
        'assignments',
        'discussion_topic',
        'submission',
        'all_dates',
        'overrides',
        'observed_users',
      ],
      'override_assignment_dates': 'true',
    };
    return fetchList(dio.get('courses/$courseId/assignment_groups', queryParameters: params), depaginateWith: dio);
  }

  Future<PagedList<Assignment>> getAssignmentsWithSubmissionsPaged(int courseId, int studentId) async {
    var params = {
      'include': ['all_dates', 'overrides', 'rubric_assessment', 'submission'],
      'order_by': 'due_at',
      'override_assignment_dates': 'true',
      'needs_grading_count_by_section': 'true',
    };
    return fetchFirstPage(canvasDio().get('courses/$courseId/assignments', queryParameters: params));
  }

  Future<Assignment> getAssignment(int courseId, int assignmentId) async {
    var params = {
      'include': ['overrides', 'rubric_assessment', 'submission'],
      'all_dates': 'true',
      'override_assignment_dates': 'true',
      'needs_grading_count_by_section': 'true',
    };
    return fetch(canvasDio().get('courses/$courseId/assignments/$assignmentId', queryParameters: params));
  }

  // TODO: Remove once LA-274 is implemented, and submissions are given with assignment groups (for observers)
  Future<List<Submission>> getSubmissions(int courseId, int studentId, List<int> assignmentIds, {bool forceRefresh}) {
    final dio = canvasDio(forceRefresh: forceRefresh);
    final params = {
      'student_ids': [studentId],
      'assignment_ids': assignmentIds,
    };
    return fetchList(dio.get('courses/$courseId/students/submissions', queryParameters: params), depaginateWith: dio);
  }
}
