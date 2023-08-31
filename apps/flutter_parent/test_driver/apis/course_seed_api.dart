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

import 'dart:convert';

import 'package:faker/faker.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/create_course_wrapper.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class CourseSeedApi {
  static Future<Course?> createCourse({bool forceRefresh = false}) async {
    final dio = seedingDio();
    final courseNumber = faker.randomGenerator.integer(500, min: 100).toString();
    final courseName = faker.sport.name() + " " + courseNumber;
    final courseCode = courseName.substring(0, 1) + courseNumber;
    final courseCreateWrapper = CreateCourseWrapper((b) => b
      ..offer = true
      ..course.name = courseName
      ..course.courseCode = courseCode
      ..course.role = "student"
      ..course.syllabusBody = "A Syllabus"
      ..build());

    var postBody = json.encode(serialize(courseCreateWrapper));

    return fetch(dio.post("accounts/self/courses", data: postBody));
  }
}
