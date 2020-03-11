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
  Iterable<Object> serialize(
      Serializers serializers, CreateAssignmentInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
    result.add('description');
    if (object.description == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.description,
          specifiedType: const FullType(String)));
    }
    result.add('due_at');
    if (object.dueAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.dueAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('grading_type');
    if (object.gradingType == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.gradingType,
          specifiedType: const FullType(GradingType)));
    }
    result.add('html_url');
    if (object.htmlUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.htmlUrl,
          specifiedType: const FullType(String)));
    }
    result.add('url');
    if (object.url == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.url,
          specifiedType: const FullType(String)));
    }
    result.add('quiz_id');
    if (object.quizId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.quizId,
          specifiedType: const FullType(String)));
    }
    result.add('use_rubric_for_grading');
    if (object.useRubricForGrading == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.useRubricForGrading,
          specifiedType: const FullType(bool)));
    }
    result.add('assignment_group_id');
    if (object.assignmentGroupId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.assignmentGroupId,
          specifiedType: const FullType(String)));
    }
    result.add('position');
    if (object.position == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.position,
          specifiedType: const FullType(int)));
    }
    result.add('lock_info');
    if (object.lockInfo == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.lockInfo,
          specifiedType: const FullType(LockInfo)));
    }
    result.add('locked_for_user');
    if (object.lockedForUser == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.lockedForUser,
          specifiedType: const FullType(bool)));
    }
    result.add('lock_at');
    if (object.lockAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.lockAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('unlock_at');
    if (object.unlockAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.unlockAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('lock_explanation');
    if (object.lockExplanation == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.lockExplanation,
          specifiedType: const FullType(String)));
    }
    result.add('free_form_criterion_comments');
    if (object.freeFormCriterionComments == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.freeFormCriterionComments,
          specifiedType: const FullType(bool)));
    }
    result.add('muted');
    if (object.muted == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.muted,
          specifiedType: const FullType(bool)));
    }
    result.add('group_category_id');
    if (object.groupCategoryId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.groupCategoryId,
          specifiedType: const FullType(String)));
    }
    result.add('submission_types');
    if (object.submissionTypes == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.submissionTypes,
          specifiedType: const FullType(
              BuiltList, const [const FullType(SubmissionTypes)])));
    }
    return result;
  }

  @override
  CreateAssignmentInfo deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateAssignmentInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'description':
          result.description = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'due_at':
          result.dueAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'points_possible':
          result.pointsPossible = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'course_id':
          result.courseId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'grading_type':
          result.gradingType = serializers.deserialize(value,
              specifiedType: const FullType(GradingType)) as GradingType;
          break;
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'quiz_id':
          result.quizId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'use_rubric_for_grading':
          result.useRubricForGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'assignment_group_id':
          result.assignmentGroupId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'position':
          result.position = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'lock_info':
          result.lockInfo.replace(serializers.deserialize(value,
              specifiedType: const FullType(LockInfo)) as LockInfo);
          break;
        case 'locked_for_user':
          result.lockedForUser = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'lock_at':
          result.lockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'unlock_at':
          result.unlockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'lock_explanation':
          result.lockExplanation = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'free_form_criterion_comments':
          result.freeFormCriterionComments = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'published':
          result.published = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'muted':
          result.muted = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'group_category_id':
          result.groupCategoryId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'submission_types':
          result.submissionTypes.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(SubmissionTypes)]))
              as BuiltList<Object>);
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
  final String description;
  @override
  final DateTime dueAt;
  @override
  final double pointsPossible;
  @override
  final String courseId;
  @override
  final GradingType gradingType;
  @override
  final String htmlUrl;
  @override
  final String url;
  @override
  final String quizId;
  @override
  final bool useRubricForGrading;
  @override
  final String assignmentGroupId;
  @override
  final int position;
  @override
  final LockInfo lockInfo;
  @override
  final bool lockedForUser;
  @override
  final DateTime lockAt;
  @override
  final DateTime unlockAt;
  @override
  final String lockExplanation;
  @override
  final bool freeFormCriterionComments;
  @override
  final bool published;
  @override
  final bool muted;
  @override
  final String groupCategoryId;
  @override
  final BuiltList<SubmissionTypes> submissionTypes;

  factory _$CreateAssignmentInfo(
          [void Function(CreateAssignmentInfoBuilder) updates]) =>
      (new CreateAssignmentInfoBuilder()..update(updates)).build();

  _$CreateAssignmentInfo._(
      {this.name,
      this.description,
      this.dueAt,
      this.pointsPossible,
      this.courseId,
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
      this.published,
      this.muted,
      this.groupCategoryId,
      this.submissionTypes})
      : super._() {
    if (name == null) {
      throw new BuiltValueNullFieldError('CreateAssignmentInfo', 'name');
    }
    if (pointsPossible == null) {
      throw new BuiltValueNullFieldError(
          'CreateAssignmentInfo', 'pointsPossible');
    }
    if (courseId == null) {
      throw new BuiltValueNullFieldError('CreateAssignmentInfo', 'courseId');
    }
    if (published == null) {
      throw new BuiltValueNullFieldError('CreateAssignmentInfo', 'published');
    }
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
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc(
                                $jc(
                                    $jc(
                                        $jc(
                                            $jc(
                                                $jc(
                                                    $jc(
                                                        $jc(
                                                            $jc(
                                                                $jc(
                                                                    $jc(
                                                                        $jc(
                                                                            $jc($jc($jc($jc(0, name.hashCode), description.hashCode), dueAt.hashCode),
                                                                                pointsPossible.hashCode),
                                                                            courseId.hashCode),
                                                                        gradingType.hashCode),
                                                                    htmlUrl.hashCode),
                                                                url.hashCode),
                                                            quizId.hashCode),
                                                        useRubricForGrading.hashCode),
                                                    assignmentGroupId.hashCode),
                                                position.hashCode),
                                            lockInfo.hashCode),
                                        lockedForUser.hashCode),
                                    lockAt.hashCode),
                                unlockAt.hashCode),
                            lockExplanation.hashCode),
                        freeFormCriterionComments.hashCode),
                    published.hashCode),
                muted.hashCode),
            groupCategoryId.hashCode),
        submissionTypes.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateAssignmentInfo')
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
  _$CreateAssignmentInfo _$v;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _description;
  String get description => _$this._description;
  set description(String description) => _$this._description = description;

  DateTime _dueAt;
  DateTime get dueAt => _$this._dueAt;
  set dueAt(DateTime dueAt) => _$this._dueAt = dueAt;

  double _pointsPossible;
  double get pointsPossible => _$this._pointsPossible;
  set pointsPossible(double pointsPossible) =>
      _$this._pointsPossible = pointsPossible;

  String _courseId;
  String get courseId => _$this._courseId;
  set courseId(String courseId) => _$this._courseId = courseId;

  GradingType _gradingType;
  GradingType get gradingType => _$this._gradingType;
  set gradingType(GradingType gradingType) => _$this._gradingType = gradingType;

  String _htmlUrl;
  String get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String htmlUrl) => _$this._htmlUrl = htmlUrl;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  String _quizId;
  String get quizId => _$this._quizId;
  set quizId(String quizId) => _$this._quizId = quizId;

  bool _useRubricForGrading;
  bool get useRubricForGrading => _$this._useRubricForGrading;
  set useRubricForGrading(bool useRubricForGrading) =>
      _$this._useRubricForGrading = useRubricForGrading;

  String _assignmentGroupId;
  String get assignmentGroupId => _$this._assignmentGroupId;
  set assignmentGroupId(String assignmentGroupId) =>
      _$this._assignmentGroupId = assignmentGroupId;

  int _position;
  int get position => _$this._position;
  set position(int position) => _$this._position = position;

  LockInfoBuilder _lockInfo;
  LockInfoBuilder get lockInfo => _$this._lockInfo ??= new LockInfoBuilder();
  set lockInfo(LockInfoBuilder lockInfo) => _$this._lockInfo = lockInfo;

  bool _lockedForUser;
  bool get lockedForUser => _$this._lockedForUser;
  set lockedForUser(bool lockedForUser) =>
      _$this._lockedForUser = lockedForUser;

  DateTime _lockAt;
  DateTime get lockAt => _$this._lockAt;
  set lockAt(DateTime lockAt) => _$this._lockAt = lockAt;

  DateTime _unlockAt;
  DateTime get unlockAt => _$this._unlockAt;
  set unlockAt(DateTime unlockAt) => _$this._unlockAt = unlockAt;

  String _lockExplanation;
  String get lockExplanation => _$this._lockExplanation;
  set lockExplanation(String lockExplanation) =>
      _$this._lockExplanation = lockExplanation;

  bool _freeFormCriterionComments;
  bool get freeFormCriterionComments => _$this._freeFormCriterionComments;
  set freeFormCriterionComments(bool freeFormCriterionComments) =>
      _$this._freeFormCriterionComments = freeFormCriterionComments;

  bool _published;
  bool get published => _$this._published;
  set published(bool published) => _$this._published = published;

  bool _muted;
  bool get muted => _$this._muted;
  set muted(bool muted) => _$this._muted = muted;

  String _groupCategoryId;
  String get groupCategoryId => _$this._groupCategoryId;
  set groupCategoryId(String groupCategoryId) =>
      _$this._groupCategoryId = groupCategoryId;

  ListBuilder<SubmissionTypes> _submissionTypes;
  ListBuilder<SubmissionTypes> get submissionTypes =>
      _$this._submissionTypes ??= new ListBuilder<SubmissionTypes>();
  set submissionTypes(ListBuilder<SubmissionTypes> submissionTypes) =>
      _$this._submissionTypes = submissionTypes;

  CreateAssignmentInfoBuilder() {
    CreateAssignmentInfo._initializeBuilder(this);
  }

  CreateAssignmentInfoBuilder get _$this {
    if (_$v != null) {
      _name = _$v.name;
      _description = _$v.description;
      _dueAt = _$v.dueAt;
      _pointsPossible = _$v.pointsPossible;
      _courseId = _$v.courseId;
      _gradingType = _$v.gradingType;
      _htmlUrl = _$v.htmlUrl;
      _url = _$v.url;
      _quizId = _$v.quizId;
      _useRubricForGrading = _$v.useRubricForGrading;
      _assignmentGroupId = _$v.assignmentGroupId;
      _position = _$v.position;
      _lockInfo = _$v.lockInfo?.toBuilder();
      _lockedForUser = _$v.lockedForUser;
      _lockAt = _$v.lockAt;
      _unlockAt = _$v.unlockAt;
      _lockExplanation = _$v.lockExplanation;
      _freeFormCriterionComments = _$v.freeFormCriterionComments;
      _published = _$v.published;
      _muted = _$v.muted;
      _groupCategoryId = _$v.groupCategoryId;
      _submissionTypes = _$v.submissionTypes?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateAssignmentInfo other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateAssignmentInfo;
  }

  @override
  void update(void Function(CreateAssignmentInfoBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateAssignmentInfo build() {
    _$CreateAssignmentInfo _$result;
    try {
      _$result = _$v ??
          new _$CreateAssignmentInfo._(
              name: name,
              description: description,
              dueAt: dueAt,
              pointsPossible: pointsPossible,
              courseId: courseId,
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
              published: published,
              muted: muted,
              groupCategoryId: groupCategoryId,
              submissionTypes: _submissionTypes?.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'lockInfo';
        _lockInfo?.build();

        _$failedField = 'submissionTypes';
        _submissionTypes?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CreateAssignmentInfo', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
