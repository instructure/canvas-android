// GENERATED CODE - DO NOT MODIFY BY HAND

part of submission;

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Submission> _$submissionSerializer = new _$SubmissionSerializer();

class _$SubmissionSerializer implements StructuredSerializer<Submission> {
  @override
  final Iterable<Type> types = const [Submission, _$Submission];
  @override
  final String wireName = 'Submission';

  @override
  Iterable<Object> serialize(Serializers serializers, Submission object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(int)),
      'score',
      serializers.serialize(object.score,
          specifiedType: const FullType(double)),
      'attempt',
      serializers.serialize(object.attempt, specifiedType: const FullType(int)),
      'submission_history',
      serializers.serialize(object.submissionHistory,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Submission)])),
      'grade_matches_current_submission',
      serializers.serialize(object.isGradeMatchesCurrentSubmission,
          specifiedType: const FullType(bool)),
      'late',
      serializers.serialize(object.late, specifiedType: const FullType(bool)),
      'excused',
      serializers.serialize(object.excused,
          specifiedType: const FullType(bool)),
      'missing',
      serializers.serialize(object.missing,
          specifiedType: const FullType(bool)),
      'assignment_id',
      serializers.serialize(object.assignmentId,
          specifiedType: const FullType(int)),
      'assignment',
      serializers.serialize(object.assignment,
          specifiedType: const FullType(Assignment)),
      'user_id',
      serializers.serialize(object.userId, specifiedType: const FullType(int)),
      'grader_id',
      serializers.serialize(object.graderId,
          specifiedType: const FullType(int)),
      'entered_score',
      serializers.serialize(object.enteredScore,
          specifiedType: const FullType(double)),
    ];
    result.add('grade');
    if (object.grade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.grade,
          specifiedType: const FullType(String)));
    }
    result.add('submitted_at');
    if (object.submittedAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.submittedAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('commentCreated');
    if (object.commentCreated == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.commentCreated,
          specifiedType: const FullType(DateTime)));
    }
    result.add('mediaContentType');
    if (object.mediaContentType == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.mediaContentType,
          specifiedType: const FullType(String)));
    }
    result.add('mediaCommentUrl');
    if (object.mediaCommentUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.mediaCommentUrl,
          specifiedType: const FullType(String)));
    }
    result.add('mediaCommentDisplay');
    if (object.mediaCommentDisplay == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.mediaCommentDisplay,
          specifiedType: const FullType(String)));
    }
    result.add('body');
    if (object.body == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.body,
          specifiedType: const FullType(String)));
    }
    result.add('workflow_state');
    if (object.workflowState == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.workflowState,
          specifiedType: const FullType(String)));
    }
    result.add('submission_type');
    if (object.submissionType == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.submissionType,
          specifiedType: const FullType(String)));
    }
    result.add('preview_url');
    if (object.previewUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.previewUrl,
          specifiedType: const FullType(String)));
    }
    result.add('url');
    if (object.url == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.url,
          specifiedType: const FullType(String)));
    }
    result.add('user');
    if (object.user == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.user,
          specifiedType: const FullType(User)));
    }
    result.add('points_deducted');
    if (object.pointsDeducted == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.pointsDeducted,
          specifiedType: const FullType(double)));
    }
    result.add('entered_grade');
    if (object.enteredGrade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.enteredGrade,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  Submission deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SubmissionBuilder();

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
        case 'grade':
          result.grade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'score':
          result.score = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'attempt':
          result.attempt = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'submitted_at':
          result.submittedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'commentCreated':
          result.commentCreated = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'mediaContentType':
          result.mediaContentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'mediaCommentUrl':
          result.mediaCommentUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'mediaCommentDisplay':
          result.mediaCommentDisplay = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'submission_history':
          result.submissionHistory.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Submission)]))
              as BuiltList<dynamic>);
          break;
        case 'body':
          result.body = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'grade_matches_current_submission':
          result.isGradeMatchesCurrentSubmission = serializers
              .deserialize(value, specifiedType: const FullType(bool)) as bool;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'submission_type':
          result.submissionType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'preview_url':
          result.previewUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'late':
          result.late = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'excused':
          result.excused = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'missing':
          result.missing = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'assignment_id':
          result.assignmentId = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'assignment':
          result.assignment.replace(serializers.deserialize(value,
              specifiedType: const FullType(Assignment)) as Assignment);
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'grader_id':
          result.graderId = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User)) as User);
          break;
        case 'points_deducted':
          result.pointsDeducted = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'entered_score':
          result.enteredScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'entered_grade':
          result.enteredGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$Submission extends Submission {
  @override
  final int id;
  @override
  final String grade;
  @override
  final double score;
  @override
  final int attempt;
  @override
  final DateTime submittedAt;
  @override
  final DateTime commentCreated;
  @override
  final String mediaContentType;
  @override
  final String mediaCommentUrl;
  @override
  final String mediaCommentDisplay;
  @override
  final BuiltList<Submission> submissionHistory;
  @override
  final String body;
  @override
  final bool isGradeMatchesCurrentSubmission;
  @override
  final String workflowState;
  @override
  final String submissionType;
  @override
  final String previewUrl;
  @override
  final String url;
  @override
  final bool late;
  @override
  final bool excused;
  @override
  final bool missing;
  @override
  final int assignmentId;
  @override
  final Assignment assignment;
  @override
  final int userId;
  @override
  final int graderId;
  @override
  final User user;
  @override
  final double pointsDeducted;
  @override
  final double enteredScore;
  @override
  final String enteredGrade;

  factory _$Submission([void Function(SubmissionBuilder) updates]) =>
      (new SubmissionBuilder()..update(updates)).build();

  _$Submission._(
      {this.id,
      this.grade,
      this.score,
      this.attempt,
      this.submittedAt,
      this.commentCreated,
      this.mediaContentType,
      this.mediaCommentUrl,
      this.mediaCommentDisplay,
      this.submissionHistory,
      this.body,
      this.isGradeMatchesCurrentSubmission,
      this.workflowState,
      this.submissionType,
      this.previewUrl,
      this.url,
      this.late,
      this.excused,
      this.missing,
      this.assignmentId,
      this.assignment,
      this.userId,
      this.graderId,
      this.user,
      this.pointsDeducted,
      this.enteredScore,
      this.enteredGrade})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Submission', 'id');
    }
    if (score == null) {
      throw new BuiltValueNullFieldError('Submission', 'score');
    }
    if (attempt == null) {
      throw new BuiltValueNullFieldError('Submission', 'attempt');
    }
    if (submissionHistory == null) {
      throw new BuiltValueNullFieldError('Submission', 'submissionHistory');
    }
    if (isGradeMatchesCurrentSubmission == null) {
      throw new BuiltValueNullFieldError(
          'Submission', 'isGradeMatchesCurrentSubmission');
    }
    if (late == null) {
      throw new BuiltValueNullFieldError('Submission', 'late');
    }
    if (excused == null) {
      throw new BuiltValueNullFieldError('Submission', 'excused');
    }
    if (missing == null) {
      throw new BuiltValueNullFieldError('Submission', 'missing');
    }
    if (assignmentId == null) {
      throw new BuiltValueNullFieldError('Submission', 'assignmentId');
    }
    if (assignment == null) {
      throw new BuiltValueNullFieldError('Submission', 'assignment');
    }
    if (userId == null) {
      throw new BuiltValueNullFieldError('Submission', 'userId');
    }
    if (graderId == null) {
      throw new BuiltValueNullFieldError('Submission', 'graderId');
    }
    if (enteredScore == null) {
      throw new BuiltValueNullFieldError('Submission', 'enteredScore');
    }
  }

  @override
  Submission rebuild(void Function(SubmissionBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SubmissionBuilder toBuilder() => new SubmissionBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Submission &&
        id == other.id &&
        grade == other.grade &&
        score == other.score &&
        attempt == other.attempt &&
        submittedAt == other.submittedAt &&
        commentCreated == other.commentCreated &&
        mediaContentType == other.mediaContentType &&
        mediaCommentUrl == other.mediaCommentUrl &&
        mediaCommentDisplay == other.mediaCommentDisplay &&
        submissionHistory == other.submissionHistory &&
        body == other.body &&
        isGradeMatchesCurrentSubmission ==
            other.isGradeMatchesCurrentSubmission &&
        workflowState == other.workflowState &&
        submissionType == other.submissionType &&
        previewUrl == other.previewUrl &&
        url == other.url &&
        late == other.late &&
        excused == other.excused &&
        missing == other.missing &&
        assignmentId == other.assignmentId &&
        assignment == other.assignment &&
        userId == other.userId &&
        graderId == other.graderId &&
        user == other.user &&
        pointsDeducted == other.pointsDeducted &&
        enteredScore == other.enteredScore &&
        enteredGrade == other.enteredGrade;
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
                                                                            $jc($jc($jc($jc($jc($jc($jc($jc($jc(0, id.hashCode), grade.hashCode), score.hashCode), attempt.hashCode), submittedAt.hashCode), commentCreated.hashCode), mediaContentType.hashCode), mediaCommentUrl.hashCode),
                                                                                mediaCommentDisplay.hashCode),
                                                                            submissionHistory.hashCode),
                                                                        body.hashCode),
                                                                    isGradeMatchesCurrentSubmission.hashCode),
                                                                workflowState.hashCode),
                                                            submissionType.hashCode),
                                                        previewUrl.hashCode),
                                                    url.hashCode),
                                                late.hashCode),
                                            excused.hashCode),
                                        missing.hashCode),
                                    assignmentId.hashCode),
                                assignment.hashCode),
                            userId.hashCode),
                        graderId.hashCode),
                    user.hashCode),
                pointsDeducted.hashCode),
            enteredScore.hashCode),
        enteredGrade.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Submission')
          ..add('id', id)
          ..add('grade', grade)
          ..add('score', score)
          ..add('attempt', attempt)
          ..add('submittedAt', submittedAt)
          ..add('commentCreated', commentCreated)
          ..add('mediaContentType', mediaContentType)
          ..add('mediaCommentUrl', mediaCommentUrl)
          ..add('mediaCommentDisplay', mediaCommentDisplay)
          ..add('submissionHistory', submissionHistory)
          ..add('body', body)
          ..add('isGradeMatchesCurrentSubmission',
              isGradeMatchesCurrentSubmission)
          ..add('workflowState', workflowState)
          ..add('submissionType', submissionType)
          ..add('previewUrl', previewUrl)
          ..add('url', url)
          ..add('late', late)
          ..add('excused', excused)
          ..add('missing', missing)
          ..add('assignmentId', assignmentId)
          ..add('assignment', assignment)
          ..add('userId', userId)
          ..add('graderId', graderId)
          ..add('user', user)
          ..add('pointsDeducted', pointsDeducted)
          ..add('enteredScore', enteredScore)
          ..add('enteredGrade', enteredGrade))
        .toString();
  }
}

class SubmissionBuilder implements Builder<Submission, SubmissionBuilder> {
  _$Submission _$v;

  int _id;
  int get id => _$this._id;
  set id(int id) => _$this._id = id;

  String _grade;
  String get grade => _$this._grade;
  set grade(String grade) => _$this._grade = grade;

  double _score;
  double get score => _$this._score;
  set score(double score) => _$this._score = score;

  int _attempt;
  int get attempt => _$this._attempt;
  set attempt(int attempt) => _$this._attempt = attempt;

  DateTime _submittedAt;
  DateTime get submittedAt => _$this._submittedAt;
  set submittedAt(DateTime submittedAt) => _$this._submittedAt = submittedAt;

  DateTime _commentCreated;
  DateTime get commentCreated => _$this._commentCreated;
  set commentCreated(DateTime commentCreated) =>
      _$this._commentCreated = commentCreated;

  String _mediaContentType;
  String get mediaContentType => _$this._mediaContentType;
  set mediaContentType(String mediaContentType) =>
      _$this._mediaContentType = mediaContentType;

  String _mediaCommentUrl;
  String get mediaCommentUrl => _$this._mediaCommentUrl;
  set mediaCommentUrl(String mediaCommentUrl) =>
      _$this._mediaCommentUrl = mediaCommentUrl;

  String _mediaCommentDisplay;
  String get mediaCommentDisplay => _$this._mediaCommentDisplay;
  set mediaCommentDisplay(String mediaCommentDisplay) =>
      _$this._mediaCommentDisplay = mediaCommentDisplay;

  ListBuilder<Submission> _submissionHistory;
  ListBuilder<Submission> get submissionHistory =>
      _$this._submissionHistory ??= new ListBuilder<Submission>();
  set submissionHistory(ListBuilder<Submission> submissionHistory) =>
      _$this._submissionHistory = submissionHistory;

  String _body;
  String get body => _$this._body;
  set body(String body) => _$this._body = body;

  bool _isGradeMatchesCurrentSubmission;
  bool get isGradeMatchesCurrentSubmission =>
      _$this._isGradeMatchesCurrentSubmission;
  set isGradeMatchesCurrentSubmission(bool isGradeMatchesCurrentSubmission) =>
      _$this._isGradeMatchesCurrentSubmission = isGradeMatchesCurrentSubmission;

  String _workflowState;
  String get workflowState => _$this._workflowState;
  set workflowState(String workflowState) =>
      _$this._workflowState = workflowState;

  String _submissionType;
  String get submissionType => _$this._submissionType;
  set submissionType(String submissionType) =>
      _$this._submissionType = submissionType;

  String _previewUrl;
  String get previewUrl => _$this._previewUrl;
  set previewUrl(String previewUrl) => _$this._previewUrl = previewUrl;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  bool _late;
  bool get late => _$this._late;
  set late(bool late) => _$this._late = late;

  bool _excused;
  bool get excused => _$this._excused;
  set excused(bool excused) => _$this._excused = excused;

  bool _missing;
  bool get missing => _$this._missing;
  set missing(bool missing) => _$this._missing = missing;

  int _assignmentId;
  int get assignmentId => _$this._assignmentId;
  set assignmentId(int assignmentId) => _$this._assignmentId = assignmentId;

  AssignmentBuilder _assignment;
  AssignmentBuilder get assignment =>
      _$this._assignment ??= new AssignmentBuilder();
  set assignment(AssignmentBuilder assignment) =>
      _$this._assignment = assignment;

  int _userId;
  int get userId => _$this._userId;
  set userId(int userId) => _$this._userId = userId;

  int _graderId;
  int get graderId => _$this._graderId;
  set graderId(int graderId) => _$this._graderId = graderId;

  UserBuilder _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder user) => _$this._user = user;

  double _pointsDeducted;
  double get pointsDeducted => _$this._pointsDeducted;
  set pointsDeducted(double pointsDeducted) =>
      _$this._pointsDeducted = pointsDeducted;

  double _enteredScore;
  double get enteredScore => _$this._enteredScore;
  set enteredScore(double enteredScore) => _$this._enteredScore = enteredScore;

  String _enteredGrade;
  String get enteredGrade => _$this._enteredGrade;
  set enteredGrade(String enteredGrade) => _$this._enteredGrade = enteredGrade;

  SubmissionBuilder() {
    Submission._initializeBuilder(this);
  }

  SubmissionBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _grade = _$v.grade;
      _score = _$v.score;
      _attempt = _$v.attempt;
      _submittedAt = _$v.submittedAt;
      _commentCreated = _$v.commentCreated;
      _mediaContentType = _$v.mediaContentType;
      _mediaCommentUrl = _$v.mediaCommentUrl;
      _mediaCommentDisplay = _$v.mediaCommentDisplay;
      _submissionHistory = _$v.submissionHistory?.toBuilder();
      _body = _$v.body;
      _isGradeMatchesCurrentSubmission = _$v.isGradeMatchesCurrentSubmission;
      _workflowState = _$v.workflowState;
      _submissionType = _$v.submissionType;
      _previewUrl = _$v.previewUrl;
      _url = _$v.url;
      _late = _$v.late;
      _excused = _$v.excused;
      _missing = _$v.missing;
      _assignmentId = _$v.assignmentId;
      _assignment = _$v.assignment?.toBuilder();
      _userId = _$v.userId;
      _graderId = _$v.graderId;
      _user = _$v.user?.toBuilder();
      _pointsDeducted = _$v.pointsDeducted;
      _enteredScore = _$v.enteredScore;
      _enteredGrade = _$v.enteredGrade;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Submission other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Submission;
  }

  @override
  void update(void Function(SubmissionBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Submission build() {
    _$Submission _$result;
    try {
      _$result = _$v ??
          new _$Submission._(
              id: id,
              grade: grade,
              score: score,
              attempt: attempt,
              submittedAt: submittedAt,
              commentCreated: commentCreated,
              mediaContentType: mediaContentType,
              mediaCommentUrl: mediaCommentUrl,
              mediaCommentDisplay: mediaCommentDisplay,
              submissionHistory: submissionHistory.build(),
              body: body,
              isGradeMatchesCurrentSubmission: isGradeMatchesCurrentSubmission,
              workflowState: workflowState,
              submissionType: submissionType,
              previewUrl: previewUrl,
              url: url,
              late: late,
              excused: excused,
              missing: missing,
              assignmentId: assignmentId,
              assignment: assignment.build(),
              userId: userId,
              graderId: graderId,
              user: _user?.build(),
              pointsDeducted: pointsDeducted,
              enteredScore: enteredScore,
              enteredGrade: enteredGrade);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'submissionHistory';
        submissionHistory.build();

        _$failedField = 'assignment';
        assignment.build();

        _$failedField = 'user';
        _user?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Submission', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
