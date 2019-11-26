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

import 'package:flutter_parent/api/utils/dio_config.dart';
import 'package:flutter_parent/api/utils/fetch.dart';
import 'package:flutter_parent/models/course.dart';

class CourseApi {
  Future<List<Course>> getObserveeCourses({bool forceRefresh: false}) async {
    final dio = canvasDio(forceRefresh: forceRefresh, usePerPageParam: true);
    final params = {
      'include': [
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
      ],
      'state': ['completed', 'available']
    };
    return fetchList(dio.get('courses', queryParameters: params), depaginateWith: dio);
  }

  Future<Course> getCourse(int courseId) async {
    final params = {
      'include': [
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
      ]
    };
    return fetch(canvasDio().get('courses/${courseId}', queryParameters: params));
  }
}
