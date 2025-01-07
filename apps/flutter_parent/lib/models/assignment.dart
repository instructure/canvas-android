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
import 'package:collection/collection.dart';
import 'package:flutter_parent/models/lock_info.dart';
import 'package:flutter_parent/models/submission_wrapper.dart';

import 'submission.dart';

part 'assignment.g.dart';

abstract class Assignment implements Built<Assignment, AssignmentBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Assignment> get serializer => _$assignmentSerializer;

  Assignment._();

  factory Assignment([void Function(AssignmentBuilder) updates]) = _$Assignment;

  String get id;

  String? get name;

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
  bool get useRubricForGrading;

  /// Wrapper object to handle observer and non-observer submission case
  /// See SubmissionWrapper for more details
  @BuiltValueField(wireName: 'submission')
  SubmissionWrapper? get submissionWrapper;

  /// This is used specifically for the observer -> assignment list case (all observee submissions are returned)
  /// If you are using the assignment/submission model for any other case use submissionWrapper.submission above.
  Submission? submission(String? studentId) =>
      submissionWrapper?.submissionList?.firstWhereOrNull((submission) => submission.userId == studentId);

  @BuiltValueField(wireName: 'assignment_group_id')
  String get assignmentGroupId;

  int get position;

  @BuiltValueField(wireName: 'lock_info')
  LockInfo? get lockInfo;

  @BuiltValueField(wireName: 'locked_for_user')
  bool get lockedForUser;

  @BuiltValueField(wireName: 'lock_at')
  DateTime? get lockAt; // Date the teacher no longer accepts submissions.

  @BuiltValueField(wireName: 'unlock_at')
  DateTime? get unlockAt;

  @BuiltValueField(wireName: 'lock_explanation')
  String? get lockExplanation;

  @BuiltValueField(wireName: 'free_form_criterion_comments')
  bool get freeFormCriterionComments;

  bool get published;

  @BuiltValueField(wireName: 'group_category_id')
  String? get groupCategoryId;

  @BuiltValueField(wireName: 'user_submitted')
  bool get userSubmitted;

  @BuiltValueField(wireName: 'only_visible_to_overrides')
  bool get onlyVisibleToOverrides;

  @BuiltValueField(wireName: 'anonymous_peer_reviews')
  bool get anonymousPeerReviews;

  @BuiltValueField(wireName: 'moderated_grading')
  bool get moderatedGrading;

  @BuiltValueField(wireName: 'anonymous_grading')
  bool get anonymousGrading;

  bool get isStudioEnabled;

  @BuiltValueField(wireName: 'submission_types')
  BuiltList<SubmissionTypes>? get submissionTypes;

  @BuiltValueField(wireName: 'hide_in_gradebook')
  bool? get isHiddenInGradeBook;

  static void _initializeBuilder(AssignmentBuilder b) => b
    ..pointsPossible = 0.0
    ..useRubricForGrading = false
    ..lockedForUser = false
    ..freeFormCriterionComments = false
    ..published = false
    ..userSubmitted = false
    ..onlyVisibleToOverrides = false
    ..anonymousPeerReviews = false
    ..moderatedGrading = false
    ..anonymousGrading = false
    ..isStudioEnabled = false;

  @BuiltValueField(serialize: false)
  bool get isFullyLocked {
    if (lockInfo == null || lockInfo?.isEmpty == true) return false;
    if (lockInfo!.hasModuleName) return true;
    if (lockInfo!.unlockAt != null && lockInfo!.unlockAt!.isAfter(DateTime.now())) return true;
    return false;
  }

  bool isSubmittable() =>
      submissionTypes?.every((type) => type == SubmissionTypes.onPaper || type == SubmissionTypes.none) != true;

  SubmissionStatus getStatus({required String? studentId}) {
    final submission = this.submission(studentId);
    if ((!isSubmittable() && submission == null) || (!isSubmittable() && submission?.isGraded() == false)) {
      return SubmissionStatus.NONE;
    } else if (submission?.isLate == true) {
      return SubmissionStatus.LATE;
    } else if (_isMissingSubmission(studentId)) {
      return SubmissionStatus.MISSING;
    } else if (submission?.submittedAt == null && (submission?.isGraded() ?? false) == false) {
      return SubmissionStatus.NOT_SUBMITTED;
    } else {
      return SubmissionStatus.SUBMITTED;
    }
  }

  // Returns true if the submission is marked as missing, or if it's pass due and either no submission or 'fake' submission
  bool _isMissingSubmission(String? studentId) {
    final submission = this.submission(studentId);
    if (submission?.missing == true) return true;

    // Don't mark LTI assignments as missing when overdue as they usually won't have a real submission for it
    if (submissionTypes?.contains(SubmissionTypes.externalTool) == true) return false;

    final isPastDue = dueAt?.isBefore(DateTime.now()) == true;
    return isPastDue && (submission == null || (submission.attempt == 0 && submission.grade == null));
  }

  bool get isDiscussion => submissionTypes?.contains(SubmissionTypes.discussionTopic) ?? false;

  bool get isQuiz => submissionTypes?.contains(SubmissionTypes.onlineQuiz) ?? false;

  bool isGradingTypeQuantitative() {
    return gradingType == GradingType.points || gradingType == GradingType.percent;
  }
}

@BuiltValueEnum(wireName: 'grading_type')
class GradingType extends EnumClass {
  const GradingType._(String name) : super(name);

  static BuiltSet<GradingType> get values => _$gradingTypeValues;

  static GradingType valueOf(String name) => _$gradingTypeValueOf(name);

  static Serializer<GradingType> get serializer => _$gradingTypeSerializer;

  @BuiltValueEnumConst(wireName: 'pass_fail')
  static const GradingType passFail = _$gradingTypePassFail;

  @BuiltValueEnumConst(wireName: 'percent')
  static const GradingType percent = _$gradingTypePercent;

  @BuiltValueEnumConst(wireName: 'letter_grade')
  static const GradingType letterGrade = _$gradingTypeLetterGrade;

  @BuiltValueEnumConst(wireName: 'points')
  static const GradingType points = _$gradingTypePoints;

  @BuiltValueEnumConst(wireName: 'gpa_scale')
  static const GradingType gpaScale = _$gradingTypeGpaScale;

  @BuiltValueEnumConst(wireName: 'not_graded')
  static const GradingType notGraded = _$gradingTypeNotGraded;

  @BuiltValueEnumConst(fallback: true)
  static const GradingType other = _$gradingTypeOther;
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
