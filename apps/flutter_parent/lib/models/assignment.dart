/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.
library assignment;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'submission.dart';

part 'assignment.g.dart';

abstract class Assignment implements Built<Assignment, AssignmentBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Assignment> get serializer => _$assignmentSerializer;

  Assignment._();

  factory Assignment([void Function(AssignmentBuilder) updates]) = _$Assignment;

  int get id;

  @nullable
  String get name;

  @nullable
  String get description;

  @nullable
  @BuiltValueField(wireName: 'due_at')
  DateTime get dueAt;

  @BuiltValueField(wireName: 'points_possible')
  double get pointsPossible;

  @BuiltValueField(wireName: 'course_id')
  int get courseId;

  @nullable
  @BuiltValueField(wireName: "grading_type")
  String get gradingType;

  @nullable
  @BuiltValueField(wireName: "html_url")
  String get htmlUrl;

  @nullable
  String get url;

  @nullable
  @BuiltValueField(wireName: "quiz_id")
  int get quizId; // (Optional) id of the associated quiz (applies only when submission_types is ["online_quiz"])

  @BuiltValueField(wireName: "use_rubric_for_grading")
  bool get useRubricForGrading;

  @nullable
  Submission get submission;

  @BuiltValueField(wireName: "assignment_group_id")
  int get assignmentGroupId;

  int get position;

  @BuiltValueField(wireName: "locked_for_user")
  bool get lockedForUser;

  @nullable
  @BuiltValueField(wireName: "lock_at")
  DateTime get lockAt; // Date the teacher no longer accepts submissions.

  @nullable
  @BuiltValueField(wireName: "unlock_at")
  DateTime get unlockAt;

  @nullable
  @BuiltValueField(wireName: "lock_explanation")
  String get lockExplanation;

  @BuiltValueField(wireName: "free_form_criterion_comments")
  bool get freeFormCriterionComments;

  bool get published;

  bool get muted;

  @nullable
  @BuiltValueField(wireName: "group_category_id")
  int get groupCategoryId;

  @BuiltValueField(wireName: "user_submitted")
  bool get userSubmitted;

  @BuiltValueField(wireName: "only_visible_to_overrides")
  bool get onlyVisibleToOverrides;

  @BuiltValueField(wireName: "anonymous_peer_reviews")
  bool get anonymousPeerReviews;

  @BuiltValueField(wireName: "moderated_grading")
  bool get moderatedGrading;

  @BuiltValueField(wireName: "anonymous_grading")
  bool get anonymousGrading;

  bool get isStudioEnabled;

  static void _initializeBuilder(AssignmentBuilder b) => b
    ..pointsPossible = 0.0
    ..useRubricForGrading = false
    ..lockedForUser = false
    ..freeFormCriterionComments = false
    ..published = false
    ..muted = false
    ..userSubmitted = false
    ..onlyVisibleToOverrides = false
    ..anonymousPeerReviews = false
    ..moderatedGrading = false
    ..anonymousGrading = false
    ..isStudioEnabled = false;
}
