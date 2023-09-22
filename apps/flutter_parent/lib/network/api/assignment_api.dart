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

import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';
import 'package:flutter_parent/network/utils/paged_list.dart';

class AssignmentApi {
  Future<List<Assignment>?> getAssignmentsWithSubmissionsDepaginated(int courseId, int studentId) async {
    var dio = canvasDio();
    var params = {
      'include[]': ['all_dates', 'overrides', 'rubric_assessment', 'submission'],
      'order_by': 'due_at',
      'override_assignment_dates': 'true',
      'needs_grading_count_by_section': 'true',
    };
    return fetchList(dio.get('courses/$courseId/assignments', queryParameters: params), depaginateWith: dio);
  }

  Future<List<AssignmentGroup>?> getAssignmentGroupsWithSubmissionsDepaginated(
      String courseId, String? studentId, String? gradingPeriodId,
      {bool forceRefresh = false}) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    var params = {
      'include[]': [
        'assignments',
        'discussion_topic',
        'submission',
        'all_dates',
        'overrides',
        'observed_users',
      ],
      'override_assignment_dates': 'true',
      if (gradingPeriodId?.isNotEmpty == true) 'grading_period_id': gradingPeriodId,
    };
    return fetchList(dio.get('courses/$courseId/assignment_groups', queryParameters: params), depaginateWith: dio);
  }

  Future<PagedList<Assignment>?> getAssignmentsWithSubmissionsPaged(String courseId, String studentId) async {
    var params = {
      'include[]': ['all_dates', 'overrides', 'rubric_assessment', 'submission'],
      'order_by': 'due_at',
      'override_assignment_dates': 'true',
      'needs_grading_count_by_section': 'true',
    };
    var dio = canvasDio();
    return fetchFirstPage(dio.get('courses/$courseId/assignments', queryParameters: params));
  }

  Future<Assignment?> getAssignment(String courseId, String assignmentId, {bool forceRefresh = false}) async {
    var params = {
      'include[]': ['overrides', 'rubric_assessment', 'submission', 'observed_users'],
      'all_dates': 'true',
      'override_assignment_dates': 'true',
      'needs_grading_count_by_section': 'true',
    };
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetch(dio.get('courses/$courseId/assignments/$assignmentId', queryParameters: params));
  }
}
