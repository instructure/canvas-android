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
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/serializers.dart';

import 'utils/paged_list.dart';

class CourseApi {
  static Future<List<Course>> getObserveeCourses() async {
    print('getting observee courses depaginated');
    var coursesResponse = await Dio().get('${ApiPrefs.getApiUrl()}courses}', queryParameters: {
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
        'observed_users'
      ],
      'state': ['completed', 'available']
    });

    if (coursesResponse.statusCode == 200 || coursesResponse.statusCode == 201) {
      final coursesPaged = PagedList<Course>(coursesResponse);
      print('finished success: ${coursesPaged.nextUrl}');
      return (coursesPaged.nextUrl == null)
          ? coursesPaged.data
          : _getObserveeCoursesDepaginated(coursesPaged);
    } else {
      return Future.error(coursesResponse.statusCode);
    }
  }

  static Future<List<Course>> _getObserveeCoursesDepaginated(PagedList<Course> prevResponse) async {
    print('getting assignments depaginated _recursive_ (${prevResponse.nextUrl}');
    // Query params already specified in url
    var coursesResponse =
        await Dio().get(prevResponse.nextUrl, options: Options(headers: ApiPrefs.getHeaderMap()));

    if (coursesResponse.statusCode == 200 || coursesResponse.statusCode == 201) {
      prevResponse.updateWithResponse(coursesResponse);
      print("finished success _recursive_: ${prevResponse.nextUrl}");
      return (prevResponse.nextUrl == null)
          ? prevResponse.data
          : _getObserveeCoursesDepaginated(prevResponse);
    } else {
      print("finished error _recursive_");
      return Future.error(coursesResponse.statusMessage);
    }
  }

  Future<Course> getCourse(int courseId) async {
    var response = await Dio().get('${ApiPrefs.getApiUrl()}courses/${courseId}',
        options: Options(headers: ApiPrefs.getHeaderMap()),
        queryParameters: {
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
          ],
        });

    if (response.statusCode == 200 || response.statusCode == 201) {
      return deserialize<Course>(response.data);
    } else {
      return null;
    }
  }
}
