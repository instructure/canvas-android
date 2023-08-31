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

import 'package:flutter_parent/models/dataseeding/create_enrollment_wrapper.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class EnrollmentSeedApi {
  static Future<Enrollment?> createEnrollment(
      String userId, String courseId, String role, String associatedUserId) async {
    final dio = seedingDio();
    final enrollmentWrapper = CreateEnrollmentWrapper((b) => b
      ..enrollment.userId = userId
      ..enrollment.associatedUserId = (associatedUserId.isEmpty ? null : associatedUserId)
      ..enrollment.role = role
      ..enrollment.type = role
      ..enrollment.enrollmentState = "active"
      ..build());

    var postBody = json.encode(serialize(enrollmentWrapper));

    return fetch(dio.post("courses/${courseId}/enrollments", data: postBody));
  }
}
