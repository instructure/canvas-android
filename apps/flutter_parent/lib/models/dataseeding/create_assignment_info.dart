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
import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/lock_info.dart';

import '../assignment.dart';
import '../submission.dart';

part 'create_assignment_info.g.dart';

abstract class CreateAssignmentInfo implements Built<CreateAssignmentInfo, CreateAssignmentInfoBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CreateAssignmentInfo> get serializer => _$createAssignmentInfoSerializer;

  CreateAssignmentInfo._();

  factory CreateAssignmentInfo([void Function(CreateAssignmentInfoBuilder) updates]) = _$CreateAssignmentInfo;

  String get name;

  @nullable
  String get description;

  @nullable
  @BuiltValueField(wireName: 'due_at')
  DateTime get dueAt;

  @BuiltValueField(wireName: 'points_possible')
  double get pointsPossible;

  @BuiltValueField(wireName: 'course_id')
  String get courseId;

  @nullable
  @BuiltValueField(wireName: 'grading_type')
  GradingType get gradingType;

  @nullable
  @BuiltValueField(wireName: 'html_url')
  String get htmlUrl;

  @nullable
  String get url;

  @nullable
  @BuiltValueField(wireName: 'quiz_id')
  String get quizId; // (Optional) id of the associated quiz (applies only when submission_types is ["online_quiz"])

  @nullable
  @BuiltValueField(wireName: 'use_rubric_for_grading')
  bool get useRubricForGrading;

  @nullable
  @BuiltValueField(wireName: 'assignment_group_id')
  String get assignmentGroupId;

  @nullable
  int get position;

  @nullable
  @BuiltValueField(wireName: 'lock_info')
  LockInfo get lockInfo;

  @nullable
  @BuiltValueField(wireName: 'locked_for_user')
  bool get lockedForUser;

  @nullable
  @BuiltValueField(wireName: 'lock_at')
  DateTime get lockAt; // Date the teacher no longer accepts submissions.

  @nullable
  @BuiltValueField(wireName: 'unlock_at')
  DateTime get unlockAt;

  @nullable
  @BuiltValueField(wireName: 'lock_explanation')
  String get lockExplanation;

  @nullable
  @BuiltValueField(wireName: 'free_form_criterion_comments')
  bool get freeFormCriterionComments;

  bool get published;

  @nullable
  bool get muted;

  @nullable
  @BuiltValueField(wireName: 'group_category_id')
  String get groupCategoryId;

  @nullable
  @BuiltValueField(wireName: 'submission_types')
  BuiltList<SubmissionTypes> get submissionTypes;

  static void _initializeBuilder(CreateAssignmentInfoBuilder b) => b;

  bool get isDiscussion => submissionTypes.contains(SubmissionTypes.discussionTopic);

  bool get isQuiz => submissionTypes.contains(SubmissionTypes.onlineQuiz);
}
