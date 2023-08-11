// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_assignment_info.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateAssignmentInfo> _$createAssignmentInfoSerializer =
    new _$CreateAssignmentInfoSerializer();

class _$CreateAssignmentInfoSerializer
    implements StructuredSerializer<CreateAssignmentInfo> {
  @override
  final Iterable<Type> types = const [
    CreateAssignmentInfo,
    _$CreateAssignmentInfo
  ];
  @override
  final String wireName = 'CreateAssignmentInfo';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, CreateAssignmentInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'points_possible',
      serializers.serialize(object.pointsPossible,
          specifiedType: const FullType(double)),
      'course_id',
      serializers.serialize(object.courseId,
          specifiedType: const FullType(String)),
      'published',
      serializers.serialize(object.published,
          specifiedType: const FullType(bool)),
    ];
    Object? value;
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
    value = object.useRubricForGrading;

    result
      ..add('use_rubric_for_grading')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));
    value = object.assignmentGroupId;

    result
      ..add('assignment_group_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.position;

    result
      ..add('position')
      ..add(serializers.serialize(value, specifiedType: const FullType(int)));
    value = object.lockInfo;

    result
      ..add('lock_info')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(LockInfo)));
    value = object.lockedForUser;

    result
      ..add('locked_for_user')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));
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
    value = object.freeFormCriterionComments;

    result
      ..add('free_form_criterion_comments')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));
    value = object.muted;

    result
      ..add('muted')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));
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

    return result;
  }

  @override
  CreateAssignmentInfo deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateAssignmentInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
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
              specifiedType: const FullType(bool)) as bool?;
          break;
        case 'assignment_group_id':
          result.assignmentGroupId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'position':
          result.position = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int?;
          break;
        case 'lock_info':
          result.lockInfo.replace(serializers.deserialize(value,
              specifiedType: const FullType(LockInfo))! as LockInfo);
          break;
        case 'locked_for_user':
          result.lockedForUser = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
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
              specifiedType: const FullType(bool)) as bool?;
          break;
        case 'published':
          result.published = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'muted':
          result.muted = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
          break;
        case 'group_category_id':
          result.groupCategoryId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'submission_types':
          result.submissionTypes.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(SubmissionTypes)]))!
              as BuiltList<Object?>);
          break;
      }
    }

    return result.build();
  }
}

class _$CreateAssignmentInfo extends CreateAssignmentInfo {
  @override
  final String name;
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
  final bool? useRubricForGrading;
  @override
  final String? assignmentGroupId;
  @override
  final int? position;
  @override
  final LockInfo? lockInfo;
  @override
  final bool? lockedForUser;
  @override
  final DateTime? lockAt;
  @override
  final DateTime? unlockAt;
  @override
  final String? lockExplanation;
  @override
  final bool? freeFormCriterionComments;
  @override
  final bool published;
  @override
  final bool? muted;
  @override
  final String? groupCategoryId;
  @override
  final BuiltList<SubmissionTypes>? submissionTypes;

  factory _$CreateAssignmentInfo(
          [void Function(CreateAssignmentInfoBuilder)? updates]) =>
      (new CreateAssignmentInfoBuilder()..update(updates))._build();

  _$CreateAssignmentInfo._(
      {required this.name,
      this.description,
      this.dueAt,
      required this.pointsPossible,
      required this.courseId,
      this.gradingType,
      this.htmlUrl,
      this.url,
      this.quizId,
      this.useRubricForGrading,
      this.assignmentGroupId,
      this.position,
      this.lockInfo,
      this.lockedForUser,
      this.lockAt,
      this.unlockAt,
      this.lockExplanation,
      this.freeFormCriterionComments,
      required this.published,
      this.muted,
      this.groupCategoryId,
      this.submissionTypes})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        name, r'CreateAssignmentInfo', 'name');
    BuiltValueNullFieldError.checkNotNull(
        pointsPossible, r'CreateAssignmentInfo', 'pointsPossible');
    BuiltValueNullFieldError.checkNotNull(
        courseId, r'CreateAssignmentInfo', 'courseId');
    BuiltValueNullFieldError.checkNotNull(
        published, r'CreateAssignmentInfo', 'published');
  }

  @override
  CreateAssignmentInfo rebuild(
          void Function(CreateAssignmentInfoBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateAssignmentInfoBuilder toBuilder() =>
      new CreateAssignmentInfoBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateAssignmentInfo &&
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
        assignmentGroupId == other.assignmentGroupId &&
        position == other.position &&
        lockInfo == other.lockInfo &&
        lockedForUser == other.lockedForUser &&
        lockAt == other.lockAt &&
        unlockAt == other.unlockAt &&
        lockExplanation == other.lockExplanation &&
        freeFormCriterionComments == other.freeFormCriterionComments &&
        published == other.published &&
        muted == other.muted &&
        groupCategoryId == other.groupCategoryId &&
        submissionTypes == other.submissionTypes;
  }

  @override
  int get hashCode {
    var _$hash = 0;
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
    _$hash = $jc(_$hash, assignmentGroupId.hashCode);
    _$hash = $jc(_$hash, position.hashCode);
    _$hash = $jc(_$hash, lockInfo.hashCode);
    _$hash = $jc(_$hash, lockedForUser.hashCode);
    _$hash = $jc(_$hash, lockAt.hashCode);
    _$hash = $jc(_$hash, unlockAt.hashCode);
    _$hash = $jc(_$hash, lockExplanation.hashCode);
    _$hash = $jc(_$hash, freeFormCriterionComments.hashCode);
    _$hash = $jc(_$hash, published.hashCode);
    _$hash = $jc(_$hash, muted.hashCode);
    _$hash = $jc(_$hash, groupCategoryId.hashCode);
    _$hash = $jc(_$hash, submissionTypes.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateAssignmentInfo')
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
          ..add('assignmentGroupId', assignmentGroupId)
          ..add('position', position)
          ..add('lockInfo', lockInfo)
          ..add('lockedForUser', lockedForUser)
          ..add('lockAt', lockAt)
          ..add('unlockAt', unlockAt)
          ..add('lockExplanation', lockExplanation)
          ..add('freeFormCriterionComments', freeFormCriterionComments)
          ..add('published', published)
          ..add('muted', muted)
          ..add('groupCategoryId', groupCategoryId)
          ..add('submissionTypes', submissionTypes))
        .toString();
  }
}

class CreateAssignmentInfoBuilder
    implements Builder<CreateAssignmentInfo, CreateAssignmentInfoBuilder> {
  _$CreateAssignmentInfo? _$v;

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

  bool? _muted;
  bool? get muted => _$this._muted;
  set muted(bool? muted) => _$this._muted = muted;

  String? _groupCategoryId;
  String? get groupCategoryId => _$this._groupCategoryId;
  set groupCategoryId(String? groupCategoryId) =>
      _$this._groupCategoryId = groupCategoryId;

  ListBuilder<SubmissionTypes>? _submissionTypes;
  ListBuilder<SubmissionTypes> get submissionTypes =>
      _$this._submissionTypes ??= new ListBuilder<SubmissionTypes>();
  set submissionTypes(ListBuilder<SubmissionTypes>? submissionTypes) =>
      _$this._submissionTypes = submissionTypes;

  CreateAssignmentInfoBuilder() {
    CreateAssignmentInfo._initializeBuilder(this);
  }

  CreateAssignmentInfoBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
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
      _assignmentGroupId = $v.assignmentGroupId;
      _position = $v.position;
      _lockInfo = $v.lockInfo?.toBuilder();
      _lockedForUser = $v.lockedForUser;
      _lockAt = $v.lockAt;
      _unlockAt = $v.unlockAt;
      _lockExplanation = $v.lockExplanation;
      _freeFormCriterionComments = $v.freeFormCriterionComments;
      _published = $v.published;
      _muted = $v.muted;
      _groupCategoryId = $v.groupCategoryId;
      _submissionTypes = $v.submissionTypes?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateAssignmentInfo other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateAssignmentInfo;
  }

  @override
  void update(void Function(CreateAssignmentInfoBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateAssignmentInfo build() => _build();

  _$CreateAssignmentInfo _build() {
    _$CreateAssignmentInfo _$result;
    try {
      _$result = _$v ??
          new _$CreateAssignmentInfo._(
              name: BuiltValueNullFieldError.checkNotNull(
                  name, r'CreateAssignmentInfo', 'name'),
              description: description,
              dueAt: dueAt,
              pointsPossible: BuiltValueNullFieldError.checkNotNull(
                  pointsPossible, r'CreateAssignmentInfo', 'pointsPossible'),
              courseId: BuiltValueNullFieldError.checkNotNull(
                  courseId, r'CreateAssignmentInfo', 'courseId'),
              gradingType: gradingType,
              htmlUrl: htmlUrl,
              url: url,
              quizId: quizId,
              useRubricForGrading: useRubricForGrading,
              assignmentGroupId: assignmentGroupId,
              position: position,
              lockInfo: _lockInfo?.build(),
              lockedForUser: lockedForUser,
              lockAt: lockAt,
              unlockAt: unlockAt,
              lockExplanation: lockExplanation,
              freeFormCriterionComments: freeFormCriterionComments,
              published: BuiltValueNullFieldError.checkNotNull(
                  published, r'CreateAssignmentInfo', 'published'),
              muted: muted,
              groupCategoryId: groupCategoryId,
              submissionTypes: _submissionTypes?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'lockInfo';
        _lockInfo?.build();

        _$failedField = 'submissionTypes';
        _submissionTypes?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CreateAssignmentInfo', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
