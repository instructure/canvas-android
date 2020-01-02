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
library assignment;

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'submission.dart';

part 'assignment.g.dart';

abstract class Assignment implements Built<Assignment, AssignmentBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Assignment> get serializer => _$assignmentSerializer;

  Assignment._();

  factory Assignment([void Function(AssignmentBuilder) updates]) = _$Assignment;

  String get id;

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
  String get courseId;

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
  String get quizId; // (Optional) id of the associated quiz (applies only when submission_types is ["online_quiz"])

  @BuiltValueField(wireName: "use_rubric_for_grading")
  bool get useRubricForGrading;

  @nullable
  Submission get submission;

  @BuiltValueField(wireName: "assignment_group_id")
  String get assignmentGroupId;

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
  String get groupCategoryId;

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

  @nullable
  @BuiltValueField(wireName: 'submission_types')
  BuiltList<SubmissionTypes> get submissionTypes;

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

  SubmissionStatus getStatus() {
    if (submissionTypes?.every((type) => type == SubmissionTypes.onPaper || type == SubmissionTypes.none) == true) {
      return SubmissionStatus.NONE;
    } else if (submission?.isLate == true) {
      return SubmissionStatus.LATE;
    } else if (_isMissingSubmission()) {
      return SubmissionStatus.MISSING;
    } else if (submission?.submittedAt == null) {
      return SubmissionStatus.NOT_SUBMITTED;
    } else {
      return SubmissionStatus.SUBMITTED;
    }
  }

  // Returns true if the submission is marked as missing, or if it's pass due and either no submission or 'fake' submission
  bool _isMissingSubmission() {
    if (submission?.missing == true) return true;

    final isPastDue = dueAt?.isBefore(DateTime.now()) == true;
    return isPastDue && (submission == null || (submission.attempt == 0 && submission.grade == null));
  }
}

@BuiltValueEnum(wireName: 'submission_types')
class SubmissionTypes extends EnumClass {
  const SubmissionTypes._(String name) : super(name);

  static BuiltSet<SubmissionTypes> get values => _$submissionTypesValues;

  static SubmissionTypes valueOf(String name) => _$submissionTypesValueOf(name);

  static Serializer<SubmissionTypes> get serializer => _$submissionTypesSerializer;

  @BuiltValueEnumConst(wireName: 'discussion_topic')
  static const SubmissionTypes discussionTopic = _$submissionTypesDiscussionTopic;

  @BuiltValueEnumConst(wireName: 'online_quiz')
  static const SubmissionTypes onlineQuiz = _$submissionTypesOnlineQuiz;

  @BuiltValueEnumConst(wireName: 'on_paper')
  static const SubmissionTypes onPaper = _$submissionTypesOnPaper;

  @BuiltValueEnumConst(fallback: true)
  static const SubmissionTypes none = _$submissionTypesNone;

  @BuiltValueEnumConst(wireName: 'external_tool')
  static const SubmissionTypes externalTool = _$submissionTypesExternalTool;

  @BuiltValueEnumConst(wireName: 'online_text_entry')
  static const SubmissionTypes onlineTextEntry = _$submissionTypesOnlineTextEntry;

  @BuiltValueEnumConst(wireName: 'online_url')
  static const SubmissionTypes onlineUrl = _$submissionTypesOnlineUrl;

  @BuiltValueEnumConst(wireName: 'online_upload')
  static const SubmissionTypes onlineUpload = _$submissionTypesOnlineUpload;

  @BuiltValueEnumConst(wireName: 'media_recording')
  static const SubmissionTypes mediaRecording = _$submissionTypesMediaRecording;
}

/// Internal enum for determining the assignment's submission status, NONE represents on_paper and none submission types
enum SubmissionStatus { LATE, MISSING, SUBMITTED, NOT_SUBMITTED, NONE }
