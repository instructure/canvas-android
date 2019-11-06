library course_assignment;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'submission.dart';

part 'course_assignment.g.dart';

abstract class CourseAssignment implements Built<CourseAssignment, CourseAssignmentBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CourseAssignment> get serializer => _$courseAssignmentSerializer;

  CourseAssignment._();
  factory CourseAssignment([void Function(CourseAssignmentBuilder) updates]) =_$CourseAssignment;


  int get id;

  @nullable
  String get name;

  @nullable
  String get description;

  //  @JsonKey(name: "submission_types")
  // List<String> submissionTypesRaw = [];

  @nullable
  @BuiltValueField(wireName: 'due_at')
  DateTime get dueAt;

  @BuiltValueField(wireName: 'points_possible')
  double get pointsPossible;

  @BuiltValueField(wireName: 'course_id')
  int get courseId;

  //  @JsonKey(name: "grade_group_students_individually")
  //  bool isGradeGroupsIndividually = false;

  @nullable
  @BuiltValueField(wireName: "grading_type")
  String get gradingType;

  //  @JsonKey(name: "needs_grading_count")
  //  int needsGradingCount;

  @nullable
  @BuiltValueField(wireName: "html_url")
  String get htmlUrl;

  @nullable
  String get url;

  @BuiltValueField(wireName: "quiz_id")
  int get quizId; // (Optional) id of the associated quiz (applies only when submission_types is ["online_quiz"])

//  List<RubricCriterion> rubric = [];

  @BuiltValueField(wireName: "use_rubric_for_grading")
  bool get useRubricForGrading;
//  @JsonKey(name: "rubric_settings")
//  RubricSettings rubricSettings;
//  @SerializedName("allowed_extensions")
//  List<String> allowedExtensions = [];
  Submission get submission;

  @BuiltValueField(wireName: "assignment_group_id")
  int get assignmentGroupId;

  int get position;

//  @JsonKey(name: "peer_reviews")
//  bool isPeerReviews = false;
//  @JsonKey(name: "lock_info") // Module lock info
//  LockInfo lockInfo;
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

//  @JsonKey(name: "discussion_topic")
//  DiscussionTopicHeader discussionTopicHeader;
//  @JsonKey(name: "needs_grading_count_by_section")
//  List<NeedsGradingCount> needsGradingCountBySection = [];
  @BuiltValueField(wireName: "free_form_criterion_comments")
  bool get freeFormCriterionComments;

  bool get published;

  bool get muted;

  @BuiltValueField(wireName: "group_category_id")
  int get groupCategoryId;

//  @JsonKey(name: "all_dates")
//  List<AssignmentDueDate> allDates = [];
  @BuiltValueField(wireName: "user_submitted")
  bool get userSubmitted;

//  bool unpublishable = false;
//  List<AssignmentOverride> overrides = [];
  @BuiltValueField(wireName: "only_visible_to_overrides")
  bool get onlyVisibleToOverrides;

  @BuiltValueField(wireName: "anonymous_peer_reviews")
  bool get anonymousPeerReviews;

  @BuiltValueField(wireName: "moderated_grading")
  bool get moderatedGrading;

  @BuiltValueField(wireName: "anonymous_grading")
  bool get anonymousGrading;

  bool get isArcEnabled;


  static void _initializeBuilder(CourseAssignmentBuilder b) => b
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
    ..isArcEnabled = false;

}
