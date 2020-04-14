/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/page.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/page_api.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_screen.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CourseRoutingShellInteractor {
  Future<CourseShellData> loadCourseShell(CourseShellType type, String courseId, {bool forceRefresh = false}) async {
    var course = await _loadCourse(courseId);
    var frontPage = null;

    if (type == CourseShellType.frontPage) {
      frontPage = await _loadHomePage(courseId, forceRefresh: forceRefresh);
    }

    return CourseShellData(course, frontPage: frontPage);
  }

  Future<Course> _loadCourse(String courseId, {bool forceRefresh = false}) {
    return locator<CourseApi>().getCourse(courseId, forceRefresh: forceRefresh);
  }

  Future<Page> _loadHomePage(String courseId, {bool forceRefresh = false}) {
    return locator<PageApi>().getCourseFrontPage(courseId, forceRefresh: forceRefresh);
  }
}

class CourseShellData {
  final Course course;
  final Page frontPage;

  CourseShellData(this.course, {this.frontPage});
}
