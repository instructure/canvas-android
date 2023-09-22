// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'submission.dart';

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
  Iterable<Object?> serialize(Serializers serializers, Submission object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
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
      serializers.serialize(object.isLate, specifiedType: const FullType(bool)),
      'excused',
      serializers.serialize(object.excused,
          specifiedType: const FullType(bool)),
      'missing',
      serializers.serialize(object.missing,
          specifiedType: const FullType(bool)),
      'assignment_id',
      serializers.serialize(object.assignmentId,
          specifiedType: const FullType(String)),
      'user_id',
      serializers.serialize(object.userId,
          specifiedType: const FullType(String)),
      'grader_id',
      serializers.serialize(object.graderId,
          specifiedType: const FullType(String)),
      'entered_score',
      serializers.serialize(object.enteredScore,
          specifiedType: const FullType(double)),
    ];
    Object? value;
    value = object.grade;

    result
      ..add('grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.submittedAt;

    result
      ..add('submitted_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.commentCreated;

    result
      ..add('commentCreated')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.mediaContentType;

    result
      ..add('mediaContentType')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.mediaCommentUrl;

    result
      ..add('mediaCommentUrl')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.mediaCommentDisplay;

    result
      ..add('mediaCommentDisplay')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.body;

    result
      ..add('body')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.workflowState;

    result
      ..add('workflow_state')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.submissionType;

    result
      ..add('submission_type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.previewUrl;

    result
      ..add('preview_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.url;

    result
      ..add('url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.assignment;

    result
      ..add('assignment')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(Assignment)));
    value = object.user;

    result
      ..add('user')
      ..add(serializers.serialize(value, specifiedType: const FullType(User)));
    value = object.pointsDeducted;

    result
      ..add('points_deducted')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));
    value = object.enteredGrade;

    result
      ..add('entered_grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.postedAt;

    result
      ..add('posted_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));

    return result;
  }

  @override
  Submission deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SubmissionBuilder();

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
        case 'grade':
          result.grade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'score':
          result.score = serializers.deserialize(value,
              specifiedType: const FullType(double))! as double;
          break;
        case 'attempt':
          result.attempt = serializers.deserialize(value,
              specifiedType: const FullType(int))! as int;
          break;
        case 'submitted_at':
          result.submittedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'commentCreated':
          result.commentCreated = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'mediaContentType':
          result.mediaContentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'mediaCommentUrl':
          result.mediaCommentUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'mediaCommentDisplay':
          result.mediaCommentDisplay = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'submission_history':
          result.submissionHistory.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Submission)]))!
              as BuiltList<Object?>);
          break;
        case 'body':
          result.body = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'grade_matches_current_submission':
          result.isGradeMatchesCurrentSubmission = serializers
              .deserialize(value, specifiedType: const FullType(bool))! as bool;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'submission_type':
          result.submissionType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'preview_url':
          result.previewUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'late':
          result.isLate = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'excused':
          result.excused = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'missing':
          result.missing = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'assignment_id':
          result.assignmentId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'assignment':
          result.assignment.replace(serializers.deserialize(value,
              specifiedType: const FullType(Assignment))! as Assignment);
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'grader_id':
          result.graderId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User))! as User);
          break;
        case 'points_deducted':
          result.pointsDeducted = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double?;
          break;
        case 'entered_score':
          result.enteredScore = serializers.deserialize(value,
              specifiedType: const FullType(double))! as double;
          break;
        case 'entered_grade':
          result.enteredGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'posted_at':
          result.postedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
      }
    }

    return result.build();
  }
}

class _$Submission extends Submission {
  @override
  final String id;
  @override
  final String? grade;
  @override
  final double score;
  @override
  final int attempt;
  @override
  final DateTime? submittedAt;
  @override
  final DateTime? commentCreated;
  @override
  final String? mediaContentType;
  @override
  final String? mediaCommentUrl;
  @override
  final String? mediaCommentDisplay;
  @override
  final BuiltList<Submission> submissionHistory;
  @override
  final String? body;
  @override
  final bool isGradeMatchesCurrentSubmission;
  @override
  final String? workflowState;
  @override
  final String? submissionType;
  @override
  final String? previewUrl;
  @override
  final String? url;
  @override
  final bool isLate;
  @override
  final bool excused;
  @override
  final bool missing;
  @override
  final String assignmentId;
  @override
  final Assignment? assignment;
  @override
  final String userId;
  @override
  final String graderId;
  @override
  final User? user;
  @override
  final double? pointsDeducted;
  @override
  final double enteredScore;
  @override
  final String? enteredGrade;
  @override
  final DateTime? postedAt;

  factory _$Submission([void Function(SubmissionBuilder)? updates]) =>
      (new SubmissionBuilder()..update(updates))._build();

  _$Submission._(
      {required this.id,
      this.grade,
      required this.score,
      required this.attempt,
      this.submittedAt,
      this.commentCreated,
      this.mediaContentType,
      this.mediaCommentUrl,
      this.mediaCommentDisplay,
      required this.submissionHistory,
      this.body,
      required this.isGradeMatchesCurrentSubmission,
      this.workflowState,
      this.submissionType,
      this.previewUrl,
      this.url,
      required this.isLate,
      required this.excused,
      required this.missing,
      required this.assignmentId,
      this.assignment,
      required this.userId,
      required this.graderId,
      this.user,
      this.pointsDeducted,
      required this.enteredScore,
      this.enteredGrade,
      this.postedAt})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Submission', 'id');
    BuiltValueNullFieldError.checkNotNull(score, r'Submission', 'score');
    BuiltValueNullFieldError.checkNotNull(attempt, r'Submission', 'attempt');
    BuiltValueNullFieldError.checkNotNull(
        submissionHistory, r'Submission', 'submissionHistory');
    BuiltValueNullFieldError.checkNotNull(isGradeMatchesCurrentSubmission,
        r'Submission', 'isGradeMatchesCurrentSubmission');
    BuiltValueNullFieldError.checkNotNull(isLate, r'Submission', 'isLate');
    BuiltValueNullFieldError.checkNotNull(excused, r'Submission', 'excused');
    BuiltValueNullFieldError.checkNotNull(missing, r'Submission', 'missing');
    BuiltValueNullFieldError.checkNotNull(
        assignmentId, r'Submission', 'assignmentId');
    BuiltValueNullFieldError.checkNotNull(userId, r'Submission', 'userId');
    BuiltValueNullFieldError.checkNotNull(graderId, r'Submission', 'graderId');
    BuiltValueNullFieldError.checkNotNull(
        enteredScore, r'Submission', 'enteredScore');
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
        isLate == other.isLate &&
        excused == other.excused &&
        missing == other.missing &&
        assignmentId == other.assignmentId &&
        assignment == other.assignment &&
        userId == other.userId &&
        graderId == other.graderId &&
        user == other.user &&
        pointsDeducted == other.pointsDeducted &&
        enteredScore == other.enteredScore &&
        enteredGrade == other.enteredGrade &&
        postedAt == other.postedAt;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, grade.hashCode);
    _$hash = $jc(_$hash, score.hashCode);
    _$hash = $jc(_$hash, attempt.hashCode);
    _$hash = $jc(_$hash, submittedAt.hashCode);
    _$hash = $jc(_$hash, commentCreated.hashCode);
    _$hash = $jc(_$hash, mediaContentType.hashCode);
    _$hash = $jc(_$hash, mediaCommentUrl.hashCode);
    _$hash = $jc(_$hash, mediaCommentDisplay.hashCode);
    _$hash = $jc(_$hash, submissionHistory.hashCode);
    _$hash = $jc(_$hash, body.hashCode);
    _$hash = $jc(_$hash, isGradeMatchesCurrentSubmission.hashCode);
    _$hash = $jc(_$hash, workflowState.hashCode);
    _$hash = $jc(_$hash, submissionType.hashCode);
    _$hash = $jc(_$hash, previewUrl.hashCode);
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, isLate.hashCode);
    _$hash = $jc(_$hash, excused.hashCode);
    _$hash = $jc(_$hash, missing.hashCode);
    _$hash = $jc(_$hash, assignmentId.hashCode);
    _$hash = $jc(_$hash, assignment.hashCode);
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, graderId.hashCode);
    _$hash = $jc(_$hash, user.hashCode);
    _$hash = $jc(_$hash, pointsDeducted.hashCode);
    _$hash = $jc(_$hash, enteredScore.hashCode);
    _$hash = $jc(_$hash, enteredGrade.hashCode);
    _$hash = $jc(_$hash, postedAt.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Submission')
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
          ..add('isLate', isLate)
          ..add('excused', excused)
          ..add('missing', missing)
          ..add('assignmentId', assignmentId)
          ..add('assignment', assignment)
          ..add('userId', userId)
          ..add('graderId', graderId)
          ..add('user', user)
          ..add('pointsDeducted', pointsDeducted)
          ..add('enteredScore', enteredScore)
          ..add('enteredGrade', enteredGrade)
          ..add('postedAt', postedAt))
        .toString();
  }
}

class SubmissionBuilder implements Builder<Submission, SubmissionBuilder> {
  _$Submission? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _grade;
  String? get grade => _$this._grade;
  set grade(String? grade) => _$this._grade = grade;

  double? _score;
  double? get score => _$this._score;
  set score(double? score) => _$this._score = score;

  int? _attempt;
  int? get attempt => _$this._attempt;
  set attempt(int? attempt) => _$this._attempt = attempt;

  DateTime? _submittedAt;
  DateTime? get submittedAt => _$this._submittedAt;
  set submittedAt(DateTime? submittedAt) => _$this._submittedAt = submittedAt;

  DateTime? _commentCreated;
  DateTime? get commentCreated => _$this._commentCreated;
  set commentCreated(DateTime? commentCreated) =>
      _$this._commentCreated = commentCreated;

  String? _mediaContentType;
  String? get mediaContentType => _$this._mediaContentType;
  set mediaContentType(String? mediaContentType) =>
      _$this._mediaContentType = mediaContentType;

  String? _mediaCommentUrl;
  String? get mediaCommentUrl => _$this._mediaCommentUrl;
  set mediaCommentUrl(String? mediaCommentUrl) =>
      _$this._mediaCommentUrl = mediaCommentUrl;

  String? _mediaCommentDisplay;
  String? get mediaCommentDisplay => _$this._mediaCommentDisplay;
  set mediaCommentDisplay(String? mediaCommentDisplay) =>
      _$this._mediaCommentDisplay = mediaCommentDisplay;

  ListBuilder<Submission>? _submissionHistory;
  ListBuilder<Submission> get submissionHistory =>
      _$this._submissionHistory ??= new ListBuilder<Submission>();
  set submissionHistory(ListBuilder<Submission>? submissionHistory) =>
      _$this._submissionHistory = submissionHistory;

  String? _body;
  String? get body => _$this._body;
  set body(String? body) => _$this._body = body;

  bool? _isGradeMatchesCurrentSubmission;
  bool? get isGradeMatchesCurrentSubmission =>
      _$this._isGradeMatchesCurrentSubmission;
  set isGradeMatchesCurrentSubmission(bool? isGradeMatchesCurrentSubmission) =>
      _$this._isGradeMatchesCurrentSubmission = isGradeMatchesCurrentSubmission;

  String? _workflowState;
  String? get workflowState => _$this._workflowState;
  set workflowState(String? workflowState) =>
      _$this._workflowState = workflowState;

  String? _submissionType;
  String? get submissionType => _$this._submissionType;
  set submissionType(String? submissionType) =>
      _$this._submissionType = submissionType;

  String? _previewUrl;
  String? get previewUrl => _$this._previewUrl;
  set previewUrl(String? previewUrl) => _$this._previewUrl = previewUrl;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  bool? _isLate;
  bool? get isLate => _$this._isLate;
  set isLate(bool? isLate) => _$this._isLate = isLate;

  bool? _excused;
  bool? get excused => _$this._excused;
  set excused(bool? excused) => _$this._excused = excused;

  bool? _missing;
  bool? get missing => _$this._missing;
  set missing(bool? missing) => _$this._missing = missing;

  String? _assignmentId;
  String? get assignmentId => _$this._assignmentId;
  set assignmentId(String? assignmentId) => _$this._assignmentId = assignmentId;

  AssignmentBuilder? _assignment;
  AssignmentBuilder get assignment =>
      _$this._assignment ??= new AssignmentBuilder();
  set assignment(AssignmentBuilder? assignment) =>
      _$this._assignment = assignment;

  String? _userId;
  String? get userId => _$this._userId;
  set userId(String? userId) => _$this._userId = userId;

  String? _graderId;
  String? get graderId => _$this._graderId;
  set graderId(String? graderId) => _$this._graderId = graderId;

  UserBuilder? _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder? user) => _$this._user = user;

  double? _pointsDeducted;
  double? get pointsDeducted => _$this._pointsDeducted;
  set pointsDeducted(double? pointsDeducted) =>
      _$this._pointsDeducted = pointsDeducted;

  double? _enteredScore;
  double? get enteredScore => _$this._enteredScore;
  set enteredScore(double? enteredScore) => _$this._enteredScore = enteredScore;

  String? _enteredGrade;
  String? get enteredGrade => _$this._enteredGrade;
  set enteredGrade(String? enteredGrade) => _$this._enteredGrade = enteredGrade;

  DateTime? _postedAt;
  DateTime? get postedAt => _$this._postedAt;
  set postedAt(DateTime? postedAt) => _$this._postedAt = postedAt;

  SubmissionBuilder() {
    Submission._initializeBuilder(this);
  }

  SubmissionBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _grade = $v.grade;
      _score = $v.score;
      _attempt = $v.attempt;
      _submittedAt = $v.submittedAt;
      _commentCreated = $v.commentCreated;
      _mediaContentType = $v.mediaContentType;
      _mediaCommentUrl = $v.mediaCommentUrl;
      _mediaCommentDisplay = $v.mediaCommentDisplay;
      _submissionHistory = $v.submissionHistory.toBuilder();
      _body = $v.body;
      _isGradeMatchesCurrentSubmission = $v.isGradeMatchesCurrentSubmission;
      _workflowState = $v.workflowState;
      _submissionType = $v.submissionType;
      _previewUrl = $v.previewUrl;
      _url = $v.url;
      _isLate = $v.isLate;
      _excused = $v.excused;
      _missing = $v.missing;
      _assignmentId = $v.assignmentId;
      _assignment = $v.assignment?.toBuilder();
      _userId = $v.userId;
      _graderId = $v.graderId;
      _user = $v.user?.toBuilder();
      _pointsDeducted = $v.pointsDeducted;
      _enteredScore = $v.enteredScore;
      _enteredGrade = $v.enteredGrade;
      _postedAt = $v.postedAt;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Submission other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Submission;
  }

  @override
  void update(void Function(SubmissionBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Submission build() => _build();

  _$Submission _build() {
    _$Submission _$result;
    try {
      _$result = _$v ??
          new _$Submission._(
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'Submission', 'id'),
              grade: grade,
              score: BuiltValueNullFieldError.checkNotNull(
                  score, r'Submission', 'score'),
              attempt: BuiltValueNullFieldError.checkNotNull(
                  attempt, r'Submission', 'attempt'),
              submittedAt: submittedAt,
              commentCreated: commentCreated,
              mediaContentType: mediaContentType,
              mediaCommentUrl: mediaCommentUrl,
              mediaCommentDisplay: mediaCommentDisplay,
              submissionHistory: submissionHistory.build(),
              body: body,
              isGradeMatchesCurrentSubmission:
                  BuiltValueNullFieldError.checkNotNull(
                      isGradeMatchesCurrentSubmission,
                      r'Submission',
                      'isGradeMatchesCurrentSubmission'),
              workflowState: workflowState,
              submissionType: submissionType,
              previewUrl: previewUrl,
              url: url,
              isLate: BuiltValueNullFieldError.checkNotNull(
                  isLate, r'Submission', 'isLate'),
              excused: BuiltValueNullFieldError.checkNotNull(
                  excused, r'Submission', 'excused'),
              missing: BuiltValueNullFieldError.checkNotNull(
                  missing, r'Submission', 'missing'),
              assignmentId: BuiltValueNullFieldError.checkNotNull(
                  assignmentId, r'Submission', 'assignmentId'),
              assignment: _assignment?.build(),
              userId:
                  BuiltValueNullFieldError.checkNotNull(userId, r'Submission', 'userId'),
              graderId: BuiltValueNullFieldError.checkNotNull(graderId, r'Submission', 'graderId'),
              user: _user?.build(),
              pointsDeducted: pointsDeducted,
              enteredScore: BuiltValueNullFieldError.checkNotNull(enteredScore, r'Submission', 'enteredScore'),
              enteredGrade: enteredGrade,
              postedAt: postedAt);
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'submissionHistory';
        submissionHistory.build();

        _$failedField = 'assignment';
        _assignment?.build();

        _$failedField = 'user';
        _user?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'Submission', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
