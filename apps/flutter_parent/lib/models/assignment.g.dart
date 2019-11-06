// GENERATED CODE - DO NOT MODIFY BY HAND

part of course_assignment;

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Assignment> _$assignmentSerializer = new _$AssignmentSerializer();

class _$AssignmentSerializer implements StructuredSerializer<Assignment> {
  @override
  final Iterable<Type> types = const [Assignment, _$Assignment];
  @override
  final String wireName = 'Assignment';

  @override
  Iterable<Object> serialize(Serializers serializers, Assignment object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(int)),
      'points_possible',
      serializers.serialize(object.pointsPossible,
          specifiedType: const FullType(double)),
      'course_id',
      serializers.serialize(object.courseId,
          specifiedType: const FullType(int)),
      'quiz_id',
      serializers.serialize(object.quizId, specifiedType: const FullType(int)),
      'use_rubric_for_grading',
      serializers.serialize(object.useRubricForGrading,
          specifiedType: const FullType(bool)),
      'submission',
      serializers.serialize(object.submission,
          specifiedType: const FullType(Submission)),
      'assignment_group_id',
      serializers.serialize(object.assignmentGroupId,
          specifiedType: const FullType(int)),
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
      'muted',
      serializers.serialize(object.muted, specifiedType: const FullType(bool)),
      'group_category_id',
      serializers.serialize(object.groupCategoryId,
          specifiedType: const FullType(int)),
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
      'isArcEnabled',
      serializers.serialize(object.isArcEnabled,
          specifiedType: const FullType(bool)),
    ];
    result.add('name');
    if (object.name == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.name,
          specifiedType: const FullType(String)));
    }
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
          specifiedType: const FullType(String)));
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
    return result;
  }

  @override
  Assignment deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AssignmentBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
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
              specifiedType: const FullType(int)) as int;
          break;
        case 'grading_type':
          result.gradingType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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
              specifiedType: const FullType(int)) as int;
          break;
        case 'use_rubric_for_grading':
          result.useRubricForGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'submission':
          result.submission.replace(serializers.deserialize(value,
              specifiedType: const FullType(Submission)) as Submission);
          break;
        case 'assignment_group_id':
          result.assignmentGroupId = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'position':
          result.position = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
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
              specifiedType: const FullType(int)) as int;
          break;
        case 'user_submitted':
          result.userSubmitted = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'only_visible_to_overrides':
          result.onlyVisibleToOverrides = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'anonymous_peer_reviews':
          result.anonymousPeerReviews = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'moderated_grading':
          result.moderatedGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'anonymous_grading':
          result.anonymousGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'isArcEnabled':
          result.isArcEnabled = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$Assignment extends Assignment {
  @override
  final int id;
  @override
  final String name;
  @override
  final String description;
  @override
  final DateTime dueAt;
  @override
  final double pointsPossible;
  @override
  final int courseId;
  @override
  final String gradingType;
  @override
  final String htmlUrl;
  @override
  final String url;
  @override
  final int quizId;
  @override
  final bool useRubricForGrading;
  @override
  final Submission submission;
  @override
  final int assignmentGroupId;
  @override
  final int position;
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
  final int groupCategoryId;
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
  final bool isArcEnabled;

  factory _$Assignment([void Function(AssignmentBuilder) updates]) =>
      (new AssignmentBuilder()..update(updates)).build();

  _$Assignment._(
      {this.id,
      this.name,
      this.description,
      this.dueAt,
      this.pointsPossible,
      this.courseId,
      this.gradingType,
      this.htmlUrl,
      this.url,
      this.quizId,
      this.useRubricForGrading,
      this.submission,
      this.assignmentGroupId,
      this.position,
      this.lockedForUser,
      this.lockAt,
      this.unlockAt,
      this.lockExplanation,
      this.freeFormCriterionComments,
      this.published,
      this.muted,
      this.groupCategoryId,
      this.userSubmitted,
      this.onlyVisibleToOverrides,
      this.anonymousPeerReviews,
      this.moderatedGrading,
      this.anonymousGrading,
      this.isArcEnabled})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Assignment', 'id');
    }
    if (pointsPossible == null) {
      throw new BuiltValueNullFieldError('Assignment', 'pointsPossible');
    }
    if (courseId == null) {
      throw new BuiltValueNullFieldError('Assignment', 'courseId');
    }
    if (quizId == null) {
      throw new BuiltValueNullFieldError('Assignment', 'quizId');
    }
    if (useRubricForGrading == null) {
      throw new BuiltValueNullFieldError('Assignment', 'useRubricForGrading');
    }
    if (submission == null) {
      throw new BuiltValueNullFieldError('Assignment', 'submission');
    }
    if (assignmentGroupId == null) {
      throw new BuiltValueNullFieldError('Assignment', 'assignmentGroupId');
    }
    if (position == null) {
      throw new BuiltValueNullFieldError('Assignment', 'position');
    }
    if (lockedForUser == null) {
      throw new BuiltValueNullFieldError('Assignment', 'lockedForUser');
    }
    if (freeFormCriterionComments == null) {
      throw new BuiltValueNullFieldError(
          'Assignment', 'freeFormCriterionComments');
    }
    if (published == null) {
      throw new BuiltValueNullFieldError('Assignment', 'published');
    }
    if (muted == null) {
      throw new BuiltValueNullFieldError('Assignment', 'muted');
    }
    if (groupCategoryId == null) {
      throw new BuiltValueNullFieldError('Assignment', 'groupCategoryId');
    }
    if (userSubmitted == null) {
      throw new BuiltValueNullFieldError('Assignment', 'userSubmitted');
    }
    if (onlyVisibleToOverrides == null) {
      throw new BuiltValueNullFieldError(
          'Assignment', 'onlyVisibleToOverrides');
    }
    if (anonymousPeerReviews == null) {
      throw new BuiltValueNullFieldError('Assignment', 'anonymousPeerReviews');
    }
    if (moderatedGrading == null) {
      throw new BuiltValueNullFieldError('Assignment', 'moderatedGrading');
    }
    if (anonymousGrading == null) {
      throw new BuiltValueNullFieldError('Assignment', 'anonymousGrading');
    }
    if (isArcEnabled == null) {
      throw new BuiltValueNullFieldError('Assignment', 'isArcEnabled');
    }
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
        submission == other.submission &&
        assignmentGroupId == other.assignmentGroupId &&
        position == other.position &&
        lockedForUser == other.lockedForUser &&
        lockAt == other.lockAt &&
        unlockAt == other.unlockAt &&
        lockExplanation == other.lockExplanation &&
        freeFormCriterionComments == other.freeFormCriterionComments &&
        published == other.published &&
        muted == other.muted &&
        groupCategoryId == other.groupCategoryId &&
        userSubmitted == other.userSubmitted &&
        onlyVisibleToOverrides == other.onlyVisibleToOverrides &&
        anonymousPeerReviews == other.anonymousPeerReviews &&
        moderatedGrading == other.moderatedGrading &&
        anonymousGrading == other.anonymousGrading &&
        isArcEnabled == other.isArcEnabled;
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
                                                                            $jc($jc($jc($jc($jc($jc($jc($jc($jc($jc(0, id.hashCode), name.hashCode), description.hashCode), dueAt.hashCode), pointsPossible.hashCode), courseId.hashCode), gradingType.hashCode), htmlUrl.hashCode), url.hashCode),
                                                                                quizId.hashCode),
                                                                            useRubricForGrading.hashCode),
                                                                        submission.hashCode),
                                                                    assignmentGroupId.hashCode),
                                                                position.hashCode),
                                                            lockedForUser.hashCode),
                                                        lockAt.hashCode),
                                                    unlockAt.hashCode),
                                                lockExplanation.hashCode),
                                            freeFormCriterionComments.hashCode),
                                        published.hashCode),
                                    muted.hashCode),
                                groupCategoryId.hashCode),
                            userSubmitted.hashCode),
                        onlyVisibleToOverrides.hashCode),
                    anonymousPeerReviews.hashCode),
                moderatedGrading.hashCode),
            anonymousGrading.hashCode),
        isArcEnabled.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Assignment')
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
          ..add('submission', submission)
          ..add('assignmentGroupId', assignmentGroupId)
          ..add('position', position)
          ..add('lockedForUser', lockedForUser)
          ..add('lockAt', lockAt)
          ..add('unlockAt', unlockAt)
          ..add('lockExplanation', lockExplanation)
          ..add('freeFormCriterionComments', freeFormCriterionComments)
          ..add('published', published)
          ..add('muted', muted)
          ..add('groupCategoryId', groupCategoryId)
          ..add('userSubmitted', userSubmitted)
          ..add('onlyVisibleToOverrides', onlyVisibleToOverrides)
          ..add('anonymousPeerReviews', anonymousPeerReviews)
          ..add('moderatedGrading', moderatedGrading)
          ..add('anonymousGrading', anonymousGrading)
          ..add('isArcEnabled', isArcEnabled))
        .toString();
  }
}

class AssignmentBuilder implements Builder<Assignment, AssignmentBuilder> {
  _$Assignment _$v;

  int _id;
  int get id => _$this._id;
  set id(int id) => _$this._id = id;

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

  int _courseId;
  int get courseId => _$this._courseId;
  set courseId(int courseId) => _$this._courseId = courseId;

  String _gradingType;
  String get gradingType => _$this._gradingType;
  set gradingType(String gradingType) => _$this._gradingType = gradingType;

  String _htmlUrl;
  String get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String htmlUrl) => _$this._htmlUrl = htmlUrl;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  int _quizId;
  int get quizId => _$this._quizId;
  set quizId(int quizId) => _$this._quizId = quizId;

  bool _useRubricForGrading;
  bool get useRubricForGrading => _$this._useRubricForGrading;
  set useRubricForGrading(bool useRubricForGrading) =>
      _$this._useRubricForGrading = useRubricForGrading;

  SubmissionBuilder _submission;
  SubmissionBuilder get submission =>
      _$this._submission ??= new SubmissionBuilder();
  set submission(SubmissionBuilder submission) =>
      _$this._submission = submission;

  int _assignmentGroupId;
  int get assignmentGroupId => _$this._assignmentGroupId;
  set assignmentGroupId(int assignmentGroupId) =>
      _$this._assignmentGroupId = assignmentGroupId;

  int _position;
  int get position => _$this._position;
  set position(int position) => _$this._position = position;

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

  int _groupCategoryId;
  int get groupCategoryId => _$this._groupCategoryId;
  set groupCategoryId(int groupCategoryId) =>
      _$this._groupCategoryId = groupCategoryId;

  bool _userSubmitted;
  bool get userSubmitted => _$this._userSubmitted;
  set userSubmitted(bool userSubmitted) =>
      _$this._userSubmitted = userSubmitted;

  bool _onlyVisibleToOverrides;
  bool get onlyVisibleToOverrides => _$this._onlyVisibleToOverrides;
  set onlyVisibleToOverrides(bool onlyVisibleToOverrides) =>
      _$this._onlyVisibleToOverrides = onlyVisibleToOverrides;

  bool _anonymousPeerReviews;
  bool get anonymousPeerReviews => _$this._anonymousPeerReviews;
  set anonymousPeerReviews(bool anonymousPeerReviews) =>
      _$this._anonymousPeerReviews = anonymousPeerReviews;

  bool _moderatedGrading;
  bool get moderatedGrading => _$this._moderatedGrading;
  set moderatedGrading(bool moderatedGrading) =>
      _$this._moderatedGrading = moderatedGrading;

  bool _anonymousGrading;
  bool get anonymousGrading => _$this._anonymousGrading;
  set anonymousGrading(bool anonymousGrading) =>
      _$this._anonymousGrading = anonymousGrading;

  bool _isArcEnabled;
  bool get isArcEnabled => _$this._isArcEnabled;
  set isArcEnabled(bool isArcEnabled) => _$this._isArcEnabled = isArcEnabled;

  AssignmentBuilder() {
    Assignment._initializeBuilder(this);
  }

  AssignmentBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
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
      _submission = _$v.submission?.toBuilder();
      _assignmentGroupId = _$v.assignmentGroupId;
      _position = _$v.position;
      _lockedForUser = _$v.lockedForUser;
      _lockAt = _$v.lockAt;
      _unlockAt = _$v.unlockAt;
      _lockExplanation = _$v.lockExplanation;
      _freeFormCriterionComments = _$v.freeFormCriterionComments;
      _published = _$v.published;
      _muted = _$v.muted;
      _groupCategoryId = _$v.groupCategoryId;
      _userSubmitted = _$v.userSubmitted;
      _onlyVisibleToOverrides = _$v.onlyVisibleToOverrides;
      _anonymousPeerReviews = _$v.anonymousPeerReviews;
      _moderatedGrading = _$v.moderatedGrading;
      _anonymousGrading = _$v.anonymousGrading;
      _isArcEnabled = _$v.isArcEnabled;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Assignment other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Assignment;
  }

  @override
  void update(void Function(AssignmentBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Assignment build() {
    _$Assignment _$result;
    try {
      _$result = _$v ??
          new _$Assignment._(
              id: id,
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
              submission: submission.build(),
              assignmentGroupId: assignmentGroupId,
              position: position,
              lockedForUser: lockedForUser,
              lockAt: lockAt,
              unlockAt: unlockAt,
              lockExplanation: lockExplanation,
              freeFormCriterionComments: freeFormCriterionComments,
              published: published,
              muted: muted,
              groupCategoryId: groupCategoryId,
              userSubmitted: userSubmitted,
              onlyVisibleToOverrides: onlyVisibleToOverrides,
              anonymousPeerReviews: anonymousPeerReviews,
              moderatedGrading: moderatedGrading,
              anonymousGrading: anonymousGrading,
              isArcEnabled: isArcEnabled);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'submission';
        submission.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Assignment', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
