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

  int get id;

  @nullable
  String get grade;

  double get score;

  int get attempt;

  @nullable
  @BuiltValueField(wireName: 'submitted_at')
  DateTime get submittedAt;

  @nullable
  DateTime get commentCreated;

  @nullable
  String get mediaContentType;

  @nullable
  String get mediaCommentUrl;

  @nullable
  String get mediaCommentDisplay;

  @BuiltValueField(wireName: 'submission_history')
  BuiltList<Submission> get submissionHistory;

  @nullable
  String get body;

  @BuiltValueField(wireName: 'grade_matches_current_submission')
  bool get isGradeMatchesCurrentSubmission;

  @nullable
  @BuiltValueField(wireName: 'workflow_state')
  String get workflowState;

  @nullable
  @BuiltValueField(wireName: 'submission_type')
  String get submissionType;

  @nullable
  @BuiltValueField(wireName: 'preview_url')
  String get previewUrl;

  @nullable
  String get url;

  bool get late;

  bool get excused;

  bool get missing;

  // Conversation Stuff
  @BuiltValueField(wireName: "assignment_id")
  int get assignmentId;

  @nullable
  Assignment get assignment;

  @BuiltValueField(wireName: "user_id")
  int get userId;

  @BuiltValueField(wireName: "grader_id")
  int get graderId;

  @nullable
  User get user;

  @nullable
  @BuiltValueField(wireName: "points_deducted")
  double get pointsDeducted;

  @BuiltValueField(wireName: "entered_score")
  double get enteredScore;

  @nullable
  @BuiltValueField(wireName: "entered_grade")
  String get enteredGrade;

  static void _initializeBuilder(SubmissionBuilder b) => b
    ..id = 0
    ..score = 0.0
    ..attempt = 0
    ..enteredScore = 0.0
    ..graderId = 0
    ..userId = 0
    ..submissionHistory = ListBuilder<Submission>()
    ..isGradeMatchesCurrentSubmission = false
    ..late = false
    ..excused = false
    ..missing = false;
}
