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

part 'create_assignment_info.g.dart';

abstract class CreateAssignmentInfo implements Built<CreateAssignmentInfo, CreateAssignmentInfoBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CreateAssignmentInfo> get serializer => _$createAssignmentInfoSerializer;

  CreateAssignmentInfo._();

  factory CreateAssignmentInfo([void Function(CreateAssignmentInfoBuilder) updates]) = _$CreateAssignmentInfo;

  String get name;

  String? get description;

  @BuiltValueField(wireName: 'due_at')
  DateTime? get dueAt;

  @BuiltValueField(wireName: 'points_possible')
  double get pointsPossible;

  @BuiltValueField(wireName: 'course_id')
  String get courseId;

  @BuiltValueField(wireName: 'grading_type')
  GradingType? get gradingType;

  @BuiltValueField(wireName: 'html_url')
  String? get htmlUrl;

  String? get url;

  @BuiltValueField(wireName: 'quiz_id')
  String? get quizId; // (Optional) id of the associated quiz (applies only when submission_types is ["online_quiz"])

  @BuiltValueField(wireName: 'use_rubric_for_grading')
  bool? get useRubricForGrading;

  @BuiltValueField(wireName: 'assignment_group_id')
  String? get assignmentGroupId;

  int? get position;

  @BuiltValueField(wireName: 'lock_info')
  LockInfo? get lockInfo;

  @BuiltValueField(wireName: 'locked_for_user')
  bool? get lockedForUser;

  @BuiltValueField(wireName: 'lock_at')
  DateTime? get lockAt; // Date the teacher no longer accepts submissions.

  @BuiltValueField(wireName: 'unlock_at')
  DateTime? get unlockAt;

  @BuiltValueField(wireName: 'lock_explanation')
  String? get lockExplanation;

  @BuiltValueField(wireName: 'free_form_criterion_comments')
  bool? get freeFormCriterionComments;

  bool get published;

  bool? get muted;

  @BuiltValueField(wireName: 'group_category_id')
  String? get groupCategoryId;

  @BuiltValueField(wireName: 'submission_types')
  BuiltList<SubmissionTypes>? get submissionTypes;

  static void _initializeBuilder(CreateAssignmentInfoBuilder b) => b;

  bool get isDiscussion => submissionTypes?.contains(SubmissionTypes.discussionTopic) == true;

  bool get isQuiz => submissionTypes?.contains(SubmissionTypes.onlineQuiz) == true;
}
