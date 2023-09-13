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

import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_permissions.dart';
import 'package:flutter_parent/models/course_settings.dart';
import 'package:flutter_parent/models/course_tab.dart';
import 'package:flutter_parent/models/grading_period_response.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class CourseApi {
  Future<List<Course>?> getObserveeCourses({bool forceRefresh = false}) async {
    final dio = canvasDio(forceRefresh: forceRefresh, pageSize: PageSize.canvasMax);
    final params = {
      'include[]': [
        'term',
        'syllabus_body',
        'total_scores',
        'license',
        'is_public',
        'needs_grading_count',
        'permissions',
        'favorites',
        'current_grading_period_scores',
        'course_image',
        'sections',
        'observed_users',
        'settings',
        'grading_scheme'
      ],
      'enrollment_state': 'active',
    };
    return fetchList(dio.get('courses', queryParameters: params), depaginateWith: dio);
  }

  Future<Course?> getCourse(String courseId, {bool forceRefresh = false}) async {
    final params = {
      'include[]': [
        'syllabus_body',
        'term',
        'permissions',
        'license',
        'is_public',
        'needs_grading_count',
        'total_scores',
        'current_grading_period_scores',
        'course_image',
        'observed_users',
        'settings',
        'grading_scheme'
      ]
    };
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetch(dio.get('courses/${courseId}', queryParameters: params));
  }

  // TODO: Set up pagination when API is fixed (no header link) and remove per_page query parameter
  Future<GradingPeriodResponse?> getGradingPeriods(String courseId, {bool forceRefresh = false}) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetch(dio.get('courses/$courseId/grading_periods?per_page=100'));
  }

  Future<List<CourseTab>?> getCourseTabs(String courseId, {bool forceRefresh = false}) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetchList(dio.get('courses/$courseId/tabs'));
  }

  Future<CourseSettings?> getCourseSettings(String courseId, {bool forceRefresh = false}) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetch(dio.get('courses/$courseId/settings'));
  }

  Future<CoursePermissions?> getCoursePermissions(String courseId, {bool forceRefresh = false}) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetch(dio.get('courses/$courseId/permissions'));
  }
}
