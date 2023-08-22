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
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/models/submission_wrapper.dart';
import 'package:test/test.dart';

void main() {
  final id1 = '123';
  final submission1 = Submission((b) => b
    ..id = '$id1'
    ..assignmentId = '$id1'
    ..userId = '$id1'
    ..missing = true);
  final submissionString1 = '''{
  "id": "$id1",
  "assignment_id": "$id1",
  "user_id": "$id1",
  "missing": true
  }''';

  final submissionString1round2 =
      '''{id: 123, score: 0.0, attempt: 0, submission_history: [], grade_matches_current_submission: false, late: false, excused: false, missing: true, assignment_id: 123, user_id: 123, grader_id: , entered_score: 0.0, grade: null, submitted_at: null, commentCreated: null, mediaContentType: null, mediaCommentUrl: null, mediaCommentDisplay: null, body: null, workflow_state: null, submission_type: null, preview_url: null, url: null, assignment: null, user: null, points_deducted: null, entered_grade: null, posted_at: null}''';

  final id2 = '456';
  final submission2 = Submission((b) => b
    ..id = '$id2'
    ..assignmentId = '$id2'
    ..userId = '$id2'
    ..missing = true);
  final submissionString2 = '''{
  "id": "$id2",
  "assignment_id": "$id2",
  "user_id": "$id2",
  "missing": true
  }''';

  final submissionString2round2 =
      '''{id: 456, score: 0.0, attempt: 0, submission_history: [], grade_matches_current_submission: false, late: false, excused: false, missing: true, assignment_id: 456, user_id: 456, grader_id: , entered_score: 0.0, grade: null, submitted_at: null, commentCreated: null, mediaContentType: null, mediaCommentUrl: null, mediaCommentDisplay: null, body: null, workflow_state: null, submission_type: null, preview_url: null, url: null, assignment: null, user: null, points_deducted: null, entered_grade: null, posted_at: null}''';

  group('deserialize', () {
    test('Single submission', () {
      final encodedJson = jsonDecode(submissionString2);

      SubmissionWrapper? submissionWrapper = jsonSerializers.deserializeWith(SubmissionWrapper.serializer, encodedJson);

      expect(submissionWrapper?.submission, equals(submission2));
      expect(submissionWrapper?.submissionList, isNull);
    });

    test('List of submissions', () {
      final jsonArray = '[$submissionString1, $submissionString2]';
      final encodedJson = jsonDecode(jsonArray);
      print(encodedJson.toString());

      SubmissionWrapper? submissionWrapper = jsonSerializers.deserializeWith(SubmissionWrapper.serializer, encodedJson);

      expect(submissionWrapper?.submission, isNull);
      expect(submissionWrapper?.submissionList, equals([submission1, submission2]));
    });
  });

  group('serialize', () {
    test('Single submission', () {
      final submissionWrapper = SubmissionWrapper((b) => b..submission = submission1.toBuilder());
      final jsonValue = jsonSerializers.serializeWith(SubmissionWrapper.serializer, submissionWrapper);

      expect(jsonValue.toString(), equals('{submission: $submissionString1round2}'));
    });

    test('List of submissions', () {
      final submissionWrapper = SubmissionWrapper(
          (b) => b..submissionList = BuiltList<Submission>.from([submission1, submission2]).toBuilder());
      final jsonValue = jsonSerializers.serializeWith(SubmissionWrapper.serializer, submissionWrapper);

      expect(jsonValue.toString(), equals('{submission: [$submissionString1round2, $submissionString2round2]}'));
    });
  });
}
