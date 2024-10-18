// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assignment.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const GradingType _$gradingTypePassFail = const GradingType._('passFail');
const GradingType _$gradingTypePercent = const GradingType._('percent');
const GradingType _$gradingTypeLetterGrade = const GradingType._('letterGrade');
const GradingType _$gradingTypePoints = const GradingType._('points');
const GradingType _$gradingTypeGpaScale = const GradingType._('gpaScale');
const GradingType _$gradingTypeNotGraded = const GradingType._('notGraded');
const GradingType _$gradingTypeOther = const GradingType._('other');

GradingType _$gradingTypeValueOf(String name) {
  switch (name) {
    case 'passFail':
      return _$gradingTypePassFail;
    case 'percent':
      return _$gradingTypePercent;
    case 'letterGrade':
      return _$gradingTypeLetterGrade;
    case 'points':
      return _$gradingTypePoints;
    case 'gpaScale':
      return _$gradingTypeGpaScale;
    case 'notGraded':
      return _$gradingTypeNotGraded;
    case 'other':
      return _$gradingTypeOther;
    default:
      return _$gradingTypeOther;
  }
}

final BuiltSet<GradingType> _$gradingTypeValues =
    new BuiltSet<GradingType>(const <GradingType>[
  _$gradingTypePassFail,
  _$gradingTypePercent,
  _$gradingTypeLetterGrade,
  _$gradingTypePoints,
  _$gradingTypeGpaScale,
  _$gradingTypeNotGraded,
  _$gradingTypeOther,
]);

const SubmissionTypes _$submissionTypesDiscussionTopic =
    const SubmissionTypes._('discussionTopic');
const SubmissionTypes _$submissionTypesOnlineQuiz =
    const SubmissionTypes._('onlineQuiz');
const SubmissionTypes _$submissionTypesOnPaper =
    const SubmissionTypes._('onPaper');
const SubmissionTypes _$submissionTypesNone = const SubmissionTypes._('none');
const SubmissionTypes _$submissionTypesExternalTool =
    const SubmissionTypes._('externalTool');
const SubmissionTypes _$submissionTypesOnlineTextEntry =
    const SubmissionTypes._('onlineTextEntry');
const SubmissionTypes _$submissionTypesOnlineUrl =
    const SubmissionTypes._('onlineUrl');
const SubmissionTypes _$submissionTypesOnlineUpload =
    const SubmissionTypes._('onlineUpload');
const SubmissionTypes _$submissionTypesMediaRecording =
    const SubmissionTypes._('mediaRecording');

SubmissionTypes _$submissionTypesValueOf(String name) {
  switch (name) {
    case 'discussionTopic':
      return _$submissionTypesDiscussionTopic;
    case 'onlineQuiz':
      return _$submissionTypesOnlineQuiz;
    case 'onPaper':
      return _$submissionTypesOnPaper;
    case 'none':
      return _$submissionTypesNone;
    case 'externalTool':
      return _$submissionTypesExternalTool;
    case 'onlineTextEntry':
      return _$submissionTypesOnlineTextEntry;
    case 'onlineUrl':
      return _$submissionTypesOnlineUrl;
    case 'onlineUpload':
      return _$submissionTypesOnlineUpload;
    case 'mediaRecording':
      return _$submissionTypesMediaRecording;
    default:
      return _$submissionTypesNone;
  }
}

final BuiltSet<SubmissionTypes> _$submissionTypesValues =
    new BuiltSet<SubmissionTypes>(const <SubmissionTypes>[
  _$submissionTypesDiscussionTopic,
  _$submissionTypesOnlineQuiz,
  _$submissionTypesOnPaper,
  _$submissionTypesNone,
  _$submissionTypesExternalTool,
  _$submissionTypesOnlineTextEntry,
  _$submissionTypesOnlineUrl,
  _$submissionTypesOnlineUpload,
  _$submissionTypesMediaRecording,
]);

Serializer<Assignment> _$assignmentSerializer = new _$AssignmentSerializer();
Serializer<GradingType> _$gradingTypeSerializer = new _$GradingTypeSerializer();
Serializer<SubmissionTypes> _$submissionTypesSerializer =
    new _$SubmissionTypesSerializer();

class _$AssignmentSerializer implements StructuredSerializer<Assignment> {
  @override
  final Iterable<Type> types = const [Assignment, _$Assignment];
  @override
  final String wireName = 'Assignment';

  @override
  Iterable<Object?> serialize(Serializers serializers, Assignment object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'points_possible',
      serializers.serialize(object.pointsPossible,
          specifiedType: const FullType(double)),
      'course_id',
      serializers.serialize(object.courseId,
          specifiedType: const FullType(String)),
      'use_rubric_for_grading',
      serializers.serialize(object.useRubricForGrading,
          specifiedType: const FullType(bool)),
      'assignment_group_id',
      serializers.serialize(object.assignmentGroupId,
          specifiedType: const FullType(String)),
      'position',
      serializers.serialize(object.position,
          specifiedType: const FullType(int)),
      'locked_for_user',
      serializers.serialize(object.lockedForUser,
          specifiedType: const FullType(bool)),
      'free_form_criterion_comments',
      serializers.serialize(object.freeFormCriterionComments,
          specifiedType: const FullType(bool)),
      'published',
      serializers.serialize(object.published,
          specifiedType: const FullType(bool)),
      'user_submitted',
      serializers.serialize(object.userSubmitted,
          specifiedType: const FullType(bool)),
      'only_visible_to_overrides',
      serializers.serialize(object.onlyVisibleToOverrides,
          specifiedType: const FullType(bool)),
      'anonymous_peer_reviews',
      serializers.serialize(object.anonymousPeerReviews,
          specifiedType: const FullType(bool)),
      'moderated_grading',
      serializers.serialize(object.moderatedGrading,
          specifiedType: const FullType(bool)),
      'anonymous_grading',
      serializers.serialize(object.anonymousGrading,
          specifiedType: const FullType(bool)),
      'isStudioEnabled',
      serializers.serialize(object.isStudioEnabled,
          specifiedType: const FullType(bool)),
    ];
    Object? value;
    value = object.name;

    result
      ..add('name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.description;

    result
      ..add('description')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.dueAt;

    result
      ..add('due_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.gradingType;

    result
      ..add('grading_type')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(GradingType)));
    value = object.htmlUrl;

    result
      ..add('html_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.url;

    result
      ..add('url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.quizId;

    result
      ..add('quiz_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.submissionWrapper;

    result
      ..add('submission')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(SubmissionWrapper)));
    value = object.lockInfo;

    result
      ..add('lock_info')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(LockInfo)));
    value = object.lockAt;

    result
      ..add('lock_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.unlockAt;

    result
      ..add('unlock_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.lockExplanation;

    result
      ..add('lock_explanation')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.groupCategoryId;

    result
      ..add('group_category_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.submissionTypes;

    result
      ..add('submission_types')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(
              BuiltList, const [const FullType(SubmissionTypes)])));
    value = object.isHiddenInGradeBook;

    result
      ..add('hide_in_gradebook')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));

    return result;
  }

  @override
  Assignment deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AssignmentBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'description':
          result.description = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'due_at':
          result.dueAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'points_possible':
          result.pointsPossible = serializers.deserialize(value,
              specifiedType: const FullType(double))! as double;
          break;
        case 'course_id':
          result.courseId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'grading_type':
          result.gradingType = serializers.deserialize(value,
              specifiedType: const FullType(GradingType)) as GradingType?;
          break;
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'quiz_id':
          result.quizId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'use_rubric_for_grading':
          result.useRubricForGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'submission':
          result.submissionWrapper.replace(serializers.deserialize(value,
                  specifiedType: const FullType(SubmissionWrapper))!
              as SubmissionWrapper);
          break;
        case 'assignment_group_id':
          result.assignmentGroupId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'position':
          result.position = serializers.deserialize(value,
              specifiedType: const FullType(int))! as int;
          break;
        case 'lock_info':
          result.lockInfo.replace(serializers.deserialize(value,
              specifiedType: const FullType(LockInfo))! as LockInfo);
          break;
        case 'locked_for_user':
          result.lockedForUser = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'lock_at':
          result.lockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'unlock_at':
          result.unlockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'lock_explanation':
          result.lockExplanation = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'free_form_criterion_comments':
          result.freeFormCriterionComments = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'published':
          result.published = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'group_category_id':
          result.groupCategoryId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'user_submitted':
          result.userSubmitted = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'only_visible_to_overrides':
          result.onlyVisibleToOverrides = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'anonymous_peer_reviews':
          result.anonymousPeerReviews = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'moderated_grading':
          result.moderatedGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'anonymous_grading':
          result.anonymousGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'isStudioEnabled':
          result.isStudioEnabled = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'submission_types':
          result.submissionTypes.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(SubmissionTypes)]))!
              as BuiltList<Object?>);
          break;
        case 'hide_in_gradebook':
          result.isHiddenInGradeBook = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
          break;
      }
    }

    return result.build();
  }
}

class _$GradingTypeSerializer implements PrimitiveSerializer<GradingType> {
  static const Map<String, Object> _toWire = const <String, Object>{
    'passFail': 'pass_fail',
    'percent': 'percent',
    'letterGrade': 'letter_grade',
    'points': 'points',
    'gpaScale': 'gpa_scale',
    'notGraded': 'not_graded',
  };
  static const Map<Object, String> _fromWire = const <Object, String>{
    'pass_fail': 'passFail',
    'percent': 'percent',
    'letter_grade': 'letterGrade',
    'points': 'points',
    'gpa_scale': 'gpaScale',
    'not_graded': 'notGraded',
  };

  @override
  final Iterable<Type> types = const <Type>[GradingType];
  @override
  final String wireName = 'grading_type';

  @override
  Object serialize(Serializers serializers, GradingType object,
          {FullType specifiedType = FullType.unspecified}) =>
      _toWire[object.name] ?? object.name;

  @override
  GradingType deserialize(Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      GradingType.valueOf(
          _fromWire[serialized] ?? (serialized is String ? serialized : ''));
}

class _$SubmissionTypesSerializer
    implements PrimitiveSerializer<SubmissionTypes> {
  static const Map<String, Object> _toWire = const <String, Object>{
    'discussionTopic': 'discussion_topic',
    'onlineQuiz': 'online_quiz',
    'onPaper': 'on_paper',
    'externalTool': 'external_tool',
    'onlineTextEntry': 'online_text_entry',
    'onlineUrl': 'online_url',
    'onlineUpload': 'online_upload',
    'mediaRecording': 'media_recording',
  };
  static const Map<Object, String> _fromWire = const <Object, String>{
    'discussion_topic': 'discussionTopic',
    'online_quiz': 'onlineQuiz',
    'on_paper': 'onPaper',
    'external_tool': 'externalTool',
    'online_text_entry': 'onlineTextEntry',
    'online_url': 'onlineUrl',
    'online_upload': 'onlineUpload',
    'media_recording': 'mediaRecording',
  };

  @override
  final Iterable<Type> types = const <Type>[SubmissionTypes];
  @override
  final String wireName = 'submission_types';

  @override
  Object serialize(Serializers serializers, SubmissionTypes object,
          {FullType specifiedType = FullType.unspecified}) =>
      _toWire[object.name] ?? object.name;

  @override
  SubmissionTypes deserialize(Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      SubmissionTypes.valueOf(
          _fromWire[serialized] ?? (serialized is String ? serialized : ''));
}

class _$Assignment extends Assignment {
  @override
  final String id;
  @override
  final String? name;
  @override
  final String? description;
  @override
  final DateTime? dueAt;
  @override
  final double pointsPossible;
  @override
  final String courseId;
  @override
  final GradingType? gradingType;
  @override
  final String? htmlUrl;
  @override
  final String? url;
  @override
  final String? quizId;
  @override
  final bool useRubricForGrading;
  @override
  final SubmissionWrapper? submissionWrapper;
  @override
  final String assignmentGroupId;
  @override
  final int position;
  @override
  final LockInfo? lockInfo;
  @override
  final bool lockedForUser;
  @override
  final DateTime? lockAt;
  @override
  final DateTime? unlockAt;
  @override
  final String? lockExplanation;
  @override
  final bool freeFormCriterionComments;
  @override
  final bool published;
  @override
  final String? groupCategoryId;
  @override
  final bool userSubmitted;
  @override
  final bool onlyVisibleToOverrides;
  @override
  final bool anonymousPeerReviews;
  @override
  final bool moderatedGrading;
  @override
  final bool anonymousGrading;
  @override
  final bool isStudioEnabled;
  @override
  final BuiltList<SubmissionTypes>? submissionTypes;
  @override
  final bool? isHiddenInGradeBook;

  factory _$Assignment([void Function(AssignmentBuilder)? updates]) =>
      (new AssignmentBuilder()..update(updates))._build();

  _$Assignment._(
      {required this.id,
      this.name,
      this.description,
      this.dueAt,
      required this.pointsPossible,
      required this.courseId,
      this.gradingType,
      this.htmlUrl,
      this.url,
      this.quizId,
      required this.useRubricForGrading,
      this.submissionWrapper,
      required this.assignmentGroupId,
      required this.position,
      this.lockInfo,
      required this.lockedForUser,
      this.lockAt,
      this.unlockAt,
      this.lockExplanation,
      required this.freeFormCriterionComments,
      required this.published,
      this.groupCategoryId,
      required this.userSubmitted,
      required this.onlyVisibleToOverrides,
      required this.anonymousPeerReviews,
      required this.moderatedGrading,
      required this.anonymousGrading,
      required this.isStudioEnabled,
      this.submissionTypes,
      this.isHiddenInGradeBook})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Assignment', 'id');
    BuiltValueNullFieldError.checkNotNull(
        pointsPossible, r'Assignment', 'pointsPossible');
    BuiltValueNullFieldError.checkNotNull(courseId, r'Assignment', 'courseId');
    BuiltValueNullFieldError.checkNotNull(
        useRubricForGrading, r'Assignment', 'useRubricForGrading');
    BuiltValueNullFieldError.checkNotNull(
        assignmentGroupId, r'Assignment', 'assignmentGroupId');
    BuiltValueNullFieldError.checkNotNull(position, r'Assignment', 'position');
    BuiltValueNullFieldError.checkNotNull(
        lockedForUser, r'Assignment', 'lockedForUser');
    BuiltValueNullFieldError.checkNotNull(
        freeFormCriterionComments, r'Assignment', 'freeFormCriterionComments');
    BuiltValueNullFieldError.checkNotNull(
        published, r'Assignment', 'published');
    BuiltValueNullFieldError.checkNotNull(
        userSubmitted, r'Assignment', 'userSubmitted');
    BuiltValueNullFieldError.checkNotNull(
        onlyVisibleToOverrides, r'Assignment', 'onlyVisibleToOverrides');
    BuiltValueNullFieldError.checkNotNull(
        anonymousPeerReviews, r'Assignment', 'anonymousPeerReviews');
    BuiltValueNullFieldError.checkNotNull(
        moderatedGrading, r'Assignment', 'moderatedGrading');
    BuiltValueNullFieldError.checkNotNull(
        anonymousGrading, r'Assignment', 'anonymousGrading');
    BuiltValueNullFieldError.checkNotNull(
        isStudioEnabled, r'Assignment', 'isStudioEnabled');
  }

  @override
  Assignment rebuild(void Function(AssignmentBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AssignmentBuilder toBuilder() => new AssignmentBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Assignment &&
        id == other.id &&
        name == other.name &&
        description == other.description &&
        dueAt == other.dueAt &&
        pointsPossible == other.pointsPossible &&
        courseId == other.courseId &&
        gradingType == other.gradingType &&
        htmlUrl == other.htmlUrl &&
        url == other.url &&
        quizId == other.quizId &&
        useRubricForGrading == other.useRubricForGrading &&
        submissionWrapper == other.submissionWrapper &&
        assignmentGroupId == other.assignmentGroupId &&
        position == other.position &&
        lockInfo == other.lockInfo &&
        lockedForUser == other.lockedForUser &&
        lockAt == other.lockAt &&
        unlockAt == other.unlockAt &&
        lockExplanation == other.lockExplanation &&
        freeFormCriterionComments == other.freeFormCriterionComments &&
        published == other.published &&
        groupCategoryId == other.groupCategoryId &&
        userSubmitted == other.userSubmitted &&
        onlyVisibleToOverrides == other.onlyVisibleToOverrides &&
        anonymousPeerReviews == other.anonymousPeerReviews &&
        moderatedGrading == other.moderatedGrading &&
        anonymousGrading == other.anonymousGrading &&
        isStudioEnabled == other.isStudioEnabled &&
        submissionTypes == other.submissionTypes &&
        isHiddenInGradeBook == other.isHiddenInGradeBook;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, description.hashCode);
    _$hash = $jc(_$hash, dueAt.hashCode);
    _$hash = $jc(_$hash, pointsPossible.hashCode);
    _$hash = $jc(_$hash, courseId.hashCode);
    _$hash = $jc(_$hash, gradingType.hashCode);
    _$hash = $jc(_$hash, htmlUrl.hashCode);
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, quizId.hashCode);
    _$hash = $jc(_$hash, useRubricForGrading.hashCode);
    _$hash = $jc(_$hash, submissionWrapper.hashCode);
    _$hash = $jc(_$hash, assignmentGroupId.hashCode);
    _$hash = $jc(_$hash, position.hashCode);
    _$hash = $jc(_$hash, lockInfo.hashCode);
    _$hash = $jc(_$hash, lockedForUser.hashCode);
    _$hash = $jc(_$hash, lockAt.hashCode);
    _$hash = $jc(_$hash, unlockAt.hashCode);
    _$hash = $jc(_$hash, lockExplanation.hashCode);
    _$hash = $jc(_$hash, freeFormCriterionComments.hashCode);
    _$hash = $jc(_$hash, published.hashCode);
    _$hash = $jc(_$hash, groupCategoryId.hashCode);
    _$hash = $jc(_$hash, userSubmitted.hashCode);
    _$hash = $jc(_$hash, onlyVisibleToOverrides.hashCode);
    _$hash = $jc(_$hash, anonymousPeerReviews.hashCode);
    _$hash = $jc(_$hash, moderatedGrading.hashCode);
    _$hash = $jc(_$hash, anonymousGrading.hashCode);
    _$hash = $jc(_$hash, isStudioEnabled.hashCode);
    _$hash = $jc(_$hash, submissionTypes.hashCode);
    _$hash = $jc(_$hash, isHiddenInGradeBook.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Assignment')
          ..add('id', id)
          ..add('name', name)
          ..add('description', description)
          ..add('dueAt', dueAt)
          ..add('pointsPossible', pointsPossible)
          ..add('courseId', courseId)
          ..add('gradingType', gradingType)
          ..add('htmlUrl', htmlUrl)
          ..add('url', url)
          ..add('quizId', quizId)
          ..add('useRubricForGrading', useRubricForGrading)
          ..add('submissionWrapper', submissionWrapper)
          ..add('assignmentGroupId', assignmentGroupId)
          ..add('position', position)
          ..add('lockInfo', lockInfo)
          ..add('lockedForUser', lockedForUser)
          ..add('lockAt', lockAt)
          ..add('unlockAt', unlockAt)
          ..add('lockExplanation', lockExplanation)
          ..add('freeFormCriterionComments', freeFormCriterionComments)
          ..add('published', published)
          ..add('groupCategoryId', groupCategoryId)
          ..add('userSubmitted', userSubmitted)
          ..add('onlyVisibleToOverrides', onlyVisibleToOverrides)
          ..add('anonymousPeerReviews', anonymousPeerReviews)
          ..add('moderatedGrading', moderatedGrading)
          ..add('anonymousGrading', anonymousGrading)
          ..add('isStudioEnabled', isStudioEnabled)
          ..add('submissionTypes', submissionTypes)
          ..add('isHiddenInGradeBook', isHiddenInGradeBook))
        .toString();
  }
}

class AssignmentBuilder implements Builder<Assignment, AssignmentBuilder> {
  _$Assignment? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  DateTime? _dueAt;
  DateTime? get dueAt => _$this._dueAt;
  set dueAt(DateTime? dueAt) => _$this._dueAt = dueAt;

  double? _pointsPossible;
  double? get pointsPossible => _$this._pointsPossible;
  set pointsPossible(double? pointsPossible) =>
      _$this._pointsPossible = pointsPossible;

  String? _courseId;
  String? get courseId => _$this._courseId;
  set courseId(String? courseId) => _$this._courseId = courseId;

  GradingType? _gradingType;
  GradingType? get gradingType => _$this._gradingType;
  set gradingType(GradingType? gradingType) =>
      _$this._gradingType = gradingType;

  String? _htmlUrl;
  String? get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String? htmlUrl) => _$this._htmlUrl = htmlUrl;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  String? _quizId;
  String? get quizId => _$this._quizId;
  set quizId(String? quizId) => _$this._quizId = quizId;

  bool? _useRubricForGrading;
  bool? get useRubricForGrading => _$this._useRubricForGrading;
  set useRubricForGrading(bool? useRubricForGrading) =>
      _$this._useRubricForGrading = useRubricForGrading;

  SubmissionWrapperBuilder? _submissionWrapper;
  SubmissionWrapperBuilder get submissionWrapper =>
      _$this._submissionWrapper ??= new SubmissionWrapperBuilder();
  set submissionWrapper(SubmissionWrapperBuilder? submissionWrapper) =>
      _$this._submissionWrapper = submissionWrapper;

  String? _assignmentGroupId;
  String? get assignmentGroupId => _$this._assignmentGroupId;
  set assignmentGroupId(String? assignmentGroupId) =>
      _$this._assignmentGroupId = assignmentGroupId;

  int? _position;
  int? get position => _$this._position;
  set position(int? position) => _$this._position = position;

  LockInfoBuilder? _lockInfo;
  LockInfoBuilder get lockInfo => _$this._lockInfo ??= new LockInfoBuilder();
  set lockInfo(LockInfoBuilder? lockInfo) => _$this._lockInfo = lockInfo;

  bool? _lockedForUser;
  bool? get lockedForUser => _$this._lockedForUser;
  set lockedForUser(bool? lockedForUser) =>
      _$this._lockedForUser = lockedForUser;

  DateTime? _lockAt;
  DateTime? get lockAt => _$this._lockAt;
  set lockAt(DateTime? lockAt) => _$this._lockAt = lockAt;

  DateTime? _unlockAt;
  DateTime? get unlockAt => _$this._unlockAt;
  set unlockAt(DateTime? unlockAt) => _$this._unlockAt = unlockAt;

  String? _lockExplanation;
  String? get lockExplanation => _$this._lockExplanation;
  set lockExplanation(String? lockExplanation) =>
      _$this._lockExplanation = lockExplanation;

  bool? _freeFormCriterionComments;
  bool? get freeFormCriterionComments => _$this._freeFormCriterionComments;
  set freeFormCriterionComments(bool? freeFormCriterionComments) =>
      _$this._freeFormCriterionComments = freeFormCriterionComments;

  bool? _published;
  bool? get published => _$this._published;
  set published(bool? published) => _$this._published = published;

  String? _groupCategoryId;
  String? get groupCategoryId => _$this._groupCategoryId;
  set groupCategoryId(String? groupCategoryId) =>
      _$this._groupCategoryId = groupCategoryId;

  bool? _userSubmitted;
  bool? get userSubmitted => _$this._userSubmitted;
  set userSubmitted(bool? userSubmitted) =>
      _$this._userSubmitted = userSubmitted;

  bool? _onlyVisibleToOverrides;
  bool? get onlyVisibleToOverrides => _$this._onlyVisibleToOverrides;
  set onlyVisibleToOverrides(bool? onlyVisibleToOverrides) =>
      _$this._onlyVisibleToOverrides = onlyVisibleToOverrides;

  bool? _anonymousPeerReviews;
  bool? get anonymousPeerReviews => _$this._anonymousPeerReviews;
  set anonymousPeerReviews(bool? anonymousPeerReviews) =>
      _$this._anonymousPeerReviews = anonymousPeerReviews;

  bool? _moderatedGrading;
  bool? get moderatedGrading => _$this._moderatedGrading;
  set moderatedGrading(bool? moderatedGrading) =>
      _$this._moderatedGrading = moderatedGrading;

  bool? _anonymousGrading;
  bool? get anonymousGrading => _$this._anonymousGrading;
  set anonymousGrading(bool? anonymousGrading) =>
      _$this._anonymousGrading = anonymousGrading;

  bool? _isStudioEnabled;
  bool? get isStudioEnabled => _$this._isStudioEnabled;
  set isStudioEnabled(bool? isStudioEnabled) =>
      _$this._isStudioEnabled = isStudioEnabled;

  ListBuilder<SubmissionTypes>? _submissionTypes;
  ListBuilder<SubmissionTypes> get submissionTypes =>
      _$this._submissionTypes ??= new ListBuilder<SubmissionTypes>();
  set submissionTypes(ListBuilder<SubmissionTypes>? submissionTypes) =>
      _$this._submissionTypes = submissionTypes;

  bool? _isHiddenInGradeBook;
  bool? get isHiddenInGradeBook => _$this._isHiddenInGradeBook;
  set isHiddenInGradeBook(bool? isHiddenInGradeBook) =>
      _$this._isHiddenInGradeBook = isHiddenInGradeBook;

  AssignmentBuilder() {
    Assignment._initializeBuilder(this);
  }

  AssignmentBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _name = $v.name;
      _description = $v.description;
      _dueAt = $v.dueAt;
      _pointsPossible = $v.pointsPossible;
      _courseId = $v.courseId;
      _gradingType = $v.gradingType;
      _htmlUrl = $v.htmlUrl;
      _url = $v.url;
      _quizId = $v.quizId;
      _useRubricForGrading = $v.useRubricForGrading;
      _submissionWrapper = $v.submissionWrapper?.toBuilder();
      _assignmentGroupId = $v.assignmentGroupId;
      _position = $v.position;
      _lockInfo = $v.lockInfo?.toBuilder();
      _lockedForUser = $v.lockedForUser;
      _lockAt = $v.lockAt;
      _unlockAt = $v.unlockAt;
      _lockExplanation = $v.lockExplanation;
      _freeFormCriterionComments = $v.freeFormCriterionComments;
      _published = $v.published;
      _groupCategoryId = $v.groupCategoryId;
      _userSubmitted = $v.userSubmitted;
      _onlyVisibleToOverrides = $v.onlyVisibleToOverrides;
      _anonymousPeerReviews = $v.anonymousPeerReviews;
      _moderatedGrading = $v.moderatedGrading;
      _anonymousGrading = $v.anonymousGrading;
      _isStudioEnabled = $v.isStudioEnabled;
      _submissionTypes = $v.submissionTypes?.toBuilder();
      _isHiddenInGradeBook = $v.isHiddenInGradeBook;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Assignment other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Assignment;
  }

  @override
  void update(void Function(AssignmentBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Assignment build() => _build();

  _$Assignment _build() {
    _$Assignment _$result;
    try {
      _$result = _$v ??
          new _$Assignment._(
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'Assignment', 'id'),
              name: name,
              description: description,
              dueAt: dueAt,
              pointsPossible: BuiltValueNullFieldError.checkNotNull(
                  pointsPossible, r'Assignment', 'pointsPossible'),
              courseId: BuiltValueNullFieldError.checkNotNull(
                  courseId, r'Assignment', 'courseId'),
              gradingType: gradingType,
              htmlUrl: htmlUrl,
              url: url,
              quizId: quizId,
              useRubricForGrading: BuiltValueNullFieldError.checkNotNull(
                  useRubricForGrading, r'Assignment', 'useRubricForGrading'),
              submissionWrapper: _submissionWrapper?.build(),
              assignmentGroupId: BuiltValueNullFieldError.checkNotNull(
                  assignmentGroupId, r'Assignment', 'assignmentGroupId'),
              position: BuiltValueNullFieldError.checkNotNull(
                  position, r'Assignment', 'position'),
              lockInfo: _lockInfo?.build(),
              lockedForUser: BuiltValueNullFieldError.checkNotNull(
                  lockedForUser, r'Assignment', 'lockedForUser'),
              lockAt: lockAt,
              unlockAt: unlockAt,
              lockExplanation: lockExplanation,
              freeFormCriterionComments: BuiltValueNullFieldError.checkNotNull(
                  freeFormCriterionComments,
                  r'Assignment',
                  'freeFormCriterionComments'),
              published: BuiltValueNullFieldError.checkNotNull(published, r'Assignment', 'published'),
              groupCategoryId: groupCategoryId,
              userSubmitted: BuiltValueNullFieldError.checkNotNull(userSubmitted, r'Assignment', 'userSubmitted'),
              onlyVisibleToOverrides: BuiltValueNullFieldError.checkNotNull(onlyVisibleToOverrides, r'Assignment', 'onlyVisibleToOverrides'),
              anonymousPeerReviews: BuiltValueNullFieldError.checkNotNull(anonymousPeerReviews, r'Assignment', 'anonymousPeerReviews'),
              moderatedGrading: BuiltValueNullFieldError.checkNotNull(moderatedGrading, r'Assignment', 'moderatedGrading'),
              anonymousGrading: BuiltValueNullFieldError.checkNotNull(anonymousGrading, r'Assignment', 'anonymousGrading'),
              isStudioEnabled: BuiltValueNullFieldError.checkNotNull(isStudioEnabled, r'Assignment', 'isStudioEnabled'),
              submissionTypes: _submissionTypes?.build(),
              isHiddenInGradeBook: isHiddenInGradeBook);
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'submissionWrapper';
        _submissionWrapper?.build();

        _$failedField = 'lockInfo';
        _lockInfo?.build();

        _$failedField = 'submissionTypes';
        _submissionTypes?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'Assignment', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
