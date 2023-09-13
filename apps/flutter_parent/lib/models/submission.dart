// Copyright (C) 2019 - present Instructure, Inc.
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
library submission;

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'assignment.dart';
import 'user.dart';

part 'submission.g.dart';

abstract class Submission implements Built<Submission, SubmissionBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Submission> get serializer => _$submissionSerializer;

  Submission._();

  factory Submission([void Function(SubmissionBuilder) updates]) = _$Submission;

  String get id;

  String? get grade;

  double get score;

  int get attempt;

  @BuiltValueField(wireName: 'submitted_at')
  DateTime? get submittedAt;

  DateTime? get commentCreated;

  String? get mediaContentType;

  String? get mediaCommentUrl;

  String? get mediaCommentDisplay;

  @BuiltValueField(wireName: 'submission_history')
  BuiltList<Submission> get submissionHistory;

  String? get body;

  @BuiltValueField(wireName: 'grade_matches_current_submission')
  bool get isGradeMatchesCurrentSubmission;

  @BuiltValueField(wireName: 'workflow_state')
  String? get workflowState;

  @BuiltValueField(wireName: 'submission_type')
  String? get submissionType;

  @BuiltValueField(wireName: 'preview_url')
  String? get previewUrl;

  String? get url;

  // Not sure why, but build_runner fails when this field is named 'late'
  @BuiltValueField(wireName: 'late')
  bool get isLate;

  bool get excused;

  bool get missing;

  // Conversation Stuff
  @BuiltValueField(wireName: 'assignment_id')
  String get assignmentId;

  Assignment? get assignment;

  @BuiltValueField(wireName: 'user_id')
  String get userId;

  @BuiltValueField(wireName: 'grader_id')
  String get graderId;

  User? get user;

  @BuiltValueField(wireName: 'points_deducted')
  double? get pointsDeducted;

  @BuiltValueField(wireName: 'entered_score')
  double get enteredScore;

  @BuiltValueField(wireName: 'entered_grade')
  String? get enteredGrade;

  @BuiltValueField(wireName: 'posted_at')
  DateTime? get postedAt;

  bool isGraded() {
    return grade != null && workflowState != 'pending_review' && postedAt != null;
  }

  static void _initializeBuilder(SubmissionBuilder b) => b
    ..id = ''
    ..score = 0.0
    ..attempt = 0
    ..enteredScore = 0.0
    ..graderId = ''
    ..userId = ''
    ..submissionHistory = ListBuilder<Submission>()
    ..isGradeMatchesCurrentSubmission = false
    ..isLate = false
    ..excused = false
    ..missing = false;
}
