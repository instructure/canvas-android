library submission;

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'course_assignment.dart';
import 'user.dart';

part 'submission.g.dart';

abstract class Submission implements Built<Submission, SubmissionBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Submission> get serializer => _$submissionSerializer;

  Submission._();
  factory Submission([updates(SubmissionBuilder b)]) = _$Submission;

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

  //  val attachments: ArrayList<Attachment> = arrayListOf(),
  @nullable
  String get body;

  //  @JsonKey(name: "rubric_assessment")
  //  var rubricAssessment: HashMap<String, RubricCriterionAssessment> = hashMapOf(),
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

  //  @JsonKey(name: "media_comment")
  //  val mediaComment: MediaComment? = null,
  // Conversation Stuff
  @BuiltValueField(wireName: "assignment_id")
  int get assignmentId;

  CourseAssignment get assignment;

  @BuiltValueField(wireName: "user_id")
  int get userId;

  @BuiltValueField(wireName: "grader_id")
  int get graderId;

  @nullable
  User get user;

  // This value could be null. Currently will only be returned when getting the submission for
  // a user when the submission_type is discussion_topic
//  @JsonKey(name: "discussion_entries")
//  val discussionEntries: ArrayList<DiscussionEntry> = arrayListOf(),
  // Group Info only available when including groups in the Submissions#index endpoint
//  val group: Group? = null,
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
