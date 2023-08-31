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

import 'package:built_collection/built_collection.dart';
import 'package:faker/faker.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/dataseeding/create_assignment_wrapper.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class AssignmentSeedApi {
  static Future<Assignment?> createAssignment(String courseId,
      {double pointsPossible = 20, DateTime? dueAt = null, bool published = true}) async {
    if (dueAt == null) dueAt = DateTime.now().add(Duration(days: 1)).toUtc();
    final dish = faker.food.dish();
    final assignmentName = dish + ' ' + faker.randomGenerator.integer(100, min: 1).toString();
    final assignmentDescription = "Let's have some $dish!";
    final assignmentCreateWrapper = CreateAssignmentWrapper((b) => b
      ..assignment.name = assignmentName
      ..assignment.description = assignmentDescription
      ..assignment.courseId = courseId
      ..assignment.dueAt = dueAt
      ..assignment.published = published
      ..assignment.pointsPossible = pointsPossible
      ..assignment.submissionTypes = ListBuilder([SubmissionTypes.onlineTextEntry])
      ..assignment.gradingType = GradingType.points);

    var postBody = json.encode(serialize(assignmentCreateWrapper));
    final dio = seedingDio();

    return fetch(dio.post("courses/$courseId/assignments", data: postBody));
  }
}
