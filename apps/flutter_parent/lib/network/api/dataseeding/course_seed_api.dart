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

  static Future<Course> createCourse({bool forceRefresh: false}) async {
    final dio = seedingDio();
    final courseCreateWrapper = CreateCourseWrapper( (b) => b
        ..offer = true
        ..course.name = faker.sport.name()
        ..course.courseCode = faker.randomGenerator.string(2)
        ..course.role = "student"
        ..build()
    );

    var postBody = json.encode(serialize(courseCreateWrapper));

    print("createCourse: courseCreateWrapper: $courseCreateWrapper, postBody: $postBody");

    return fetch(dio.post("accounts/self/courses", data: postBody));
  }
}