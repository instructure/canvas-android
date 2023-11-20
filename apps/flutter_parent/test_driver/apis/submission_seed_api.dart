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
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/dataseeding/create_submission_wrapper.dart';
import 'package:flutter_parent/models/dataseeding/grade_submission_wrapper.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class SubmissionSeedApi {
  static Future<Submission?> createSubmission(String courseId, Assignment? assignment, String asUserId) async {
    SubmissionTypes? submissionType = assignment?.submissionTypes?.first;
    String submissionTypeString = "";
    switch (submissionType) {
      case SubmissionTypes.onlineTextEntry:
        submissionTypeString = "online_text_entry";
        break;
      case SubmissionTypes.onlineUrl:
        submissionTypeString = "online_url";
        break;
      default:
        "unknown";
        break;
    }
    String? url = (submissionType == SubmissionTypes.onlineUrl) ? faker.internet.httpsUrl() : null;
    String? textBody = (submissionType == SubmissionTypes.onlineTextEntry) ? faker.lorem.sentence() : null;
    final submissionWrapper = CreateSubmissionWrapper((b) => b
      ..submission.body = textBody
      ..submission.url = url
      ..submission.submissionType = submissionTypeString
      ..submission.userId = int.parse(asUserId));

    var postBody = json.encode(serialize(submissionWrapper));
    final dio = seedingDio();

    print("submission postBody =  $postBody");
    return fetch(dio.post("courses/$courseId/assignments/${assignment?.id}/submissions", data: postBody));
  }

  static Future<Submission?> gradeSubmission(String courseId, Assignment? assignment, String studentId, String grade) async {
    final gradeWrapper = GradeSubmissionWrapper((b) => b..submission.postedGrade = grade);

    final postBody = json.encode(serialize(gradeWrapper));
    final dio = seedingDio();

    print("Grade submission postBody: $postBody");
    return fetch(dio.put("courses/$courseId/assignments/${assignment?.id}/submissions/$studentId", data: postBody));
  }
}
