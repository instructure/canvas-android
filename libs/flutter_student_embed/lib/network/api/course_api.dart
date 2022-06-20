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

import 'package:flutter_student_embed/models/course.dart';
import 'package:flutter_student_embed/network/utils/dio_config.dart';
import 'package:flutter_student_embed/network/utils/fetch.dart';

class CourseApi {
  Future<List<Course>> getCourses({bool forceRefresh: false}) async {
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
      ],
      'enrollment_state': ['active'],
    };
    var courses = await fetchList<Course>(dio.get('courses', queryParameters: params), depaginateWith: dio);
    courses.retainWhere((it) => it.accessRestrictedByDate == false);
    return courses;
  }
}
