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

import 'package:dio/dio.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/api/utils/paged_list.dart';
import 'package:flutter_parent/models/serializers.dart';

class AssignmentApi {

  static Future<List<Assignment>> getAssignmentsWithSubmissionsDepaginated(int courseId, int studentId) async {
    var assignmentResponse = await Dio().get(ApiPrefs.getApiUrl() + 'courses/$courseId/assignments',
        queryParameters: {
          'as_user_id': studentId,
          'include': ['all_dates', 'overrides', 'rubric_assessment', 'submission'],
          'order_by': 'due_at',
          'override_assignment_dates': 'true',
          'needs_grading_count_by_section': 'true',
        },
        options: Options(headers: ApiPrefs.getHeaderMap()));

    if (assignmentResponse.statusCode == 200 || assignmentResponse.statusCode == 201) {
      final response = PagedList<Assignment>(assignmentResponse);
      return (response.nextUrl == null) ? response.data : _getAssignmentsWithSubmissionsDepaginated(response);
    } else {
      return Future.error(assignmentResponse.statusMessage);
    }
  }

  static Future<List<Assignment>> _getAssignmentsWithSubmissionsDepaginated(PagedList<Assignment> prevResponse) async {
    // Query params already specified in url
    var assignmentResponse = await Dio().get(prevResponse.nextUrl, options: Options(headers: ApiPrefs.getHeaderMap()));

    if (assignmentResponse.statusCode == 200 || assignmentResponse.statusCode == 201) {
      prevResponse.updateWithResponse(assignmentResponse);
      return (prevResponse.nextUrl == null) ? prevResponse.data : _getAssignmentsWithSubmissionsDepaginated(prevResponse);
    } else {
      return Future.error(assignmentResponse.statusMessage);
    }
  }

  static Future<PagedList<Assignment>> getAssignmentsWithSubmissionsPaged(int courseId, int studentId) async {
    var assignmentResponse = await Dio().get(ApiPrefs.getApiUrl() + 'courses/$courseId/assignments',
        queryParameters: {
          'as_user_id': studentId,
          'include': ['all_dates', 'overrides', 'rubric_assessment', 'submission'],
          'order_by': 'due_at',
          'override_assignment_dates': 'true',
          'needs_grading_count_by_section': 'true',
        },
        options: Options(headers: ApiPrefs.getHeaderMap()));

    if (assignmentResponse.statusCode == 200 || assignmentResponse.statusCode == 201) {
      return PagedList<Assignment>(assignmentResponse);
    } else {
      return Future.error(assignmentResponse.statusMessage);
    }
  }

  static Future<PagedList<Assignment>> getAssignmentsWithSubmissionsPagedNext(String nextUrl) async {
    // Query params already specified in url
    var assignmentResponse = await Dio().get(nextUrl, options: Options(headers: ApiPrefs.getHeaderMap()));

    if (assignmentResponse.statusCode == 200 || assignmentResponse.statusCode == 201) {
      return PagedList<Assignment>(assignmentResponse);
    } else {
      return Future.error(assignmentResponse.statusMessage);
    }
  }

  static Future<Assignment> getAssignment(int courseId, int assignmentId) async {
    var assignmentResponse = await Dio().get(ApiPrefs.getApiUrl() + 'courses/$courseId/assignments/$assignmentId',
        queryParameters: {
          'include': ['overrides', 'rubric_assessment', 'submission'],
          'all_dates': 'true',
          'override_assignment_dates': 'true',
          'needs_grading_count_by_section': 'true',
        },
        options: Options(headers: ApiPrefs.getHeaderMap()));

    if (assignmentResponse.statusCode == 200 || assignmentResponse.statusCode == 201) {
      return deserialize<Assignment>(assignmentResponse.data);
    } else {
      return Future.error(assignmentResponse.statusMessage);
    }
  }
}
