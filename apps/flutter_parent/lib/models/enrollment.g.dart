// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'enrollment.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Enrollment> _$enrollmentSerializer = new _$EnrollmentSerializer();

class _$EnrollmentSerializer implements StructuredSerializer<Enrollment> {
  @override
  final Iterable<Type> types = const [Enrollment, _$Enrollment];
  @override
  final String wireName = 'Enrollment';

  @override
  Iterable<Object?> serialize(Serializers serializers, Enrollment object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'enrollment_state',
      serializers.serialize(object.enrollmentState,
          specifiedType: const FullType(String)),
      'user_id',
      serializers.serialize(object.userId,
          specifiedType: const FullType(String)),
      'multiple_grading_periods_enabled',
      serializers.serialize(object.multipleGradingPeriodsEnabled,
          specifiedType: const FullType(bool)),
      'totals_for_all_grading_periods_option',
      serializers.serialize(object.totalsForAllGradingPeriodsOption,
          specifiedType: const FullType(bool)),
      'associated_user_id',
      serializers.serialize(object.associatedUserId,
          specifiedType: const FullType(String)),
      'limit_privileges_to_course_section',
      serializers.serialize(object.limitPrivilegesToCourseSection,
          specifiedType: const FullType(bool)),
    ];
    Object? value;
    value = object.role;

    result
      ..add('role')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.type;

    result
      ..add('type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.courseId;

    result
      ..add('course_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.courseSectionId;

    result
      ..add('course_section_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.grades;

    result
      ..add('grades')
      ..add(serializers.serialize(value, specifiedType: const FullType(Grade)));
    value = object.computedCurrentScore;

    result
      ..add('computed_current_score')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));
    value = object.computedFinalScore;

    result
      ..add('computed_final_score')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));
    value = object.computedCurrentGrade;

    result
      ..add('computed_current_grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.computedFinalGrade;

    result
      ..add('computed_final_grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.computedCurrentLetterGrade;

    result
      ..add('computed_current_letter_grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.currentPeriodComputedCurrentScore;

    result
      ..add('current_period_computed_current_score')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));
    value = object.currentPeriodComputedFinalScore;

    result
      ..add('current_period_computed_final_score')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));
    value = object.currentPeriodComputedCurrentGrade;

    result
      ..add('current_period_computed_current_grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.currentPeriodComputedFinalGrade;

    result
      ..add('current_period_computed_final_grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.currentGradingPeriodId;

    result
      ..add('current_grading_period_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.currentGradingPeriodTitle;

    result
      ..add('current_grading_period_title')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.lastActivityAt;

    result
      ..add('last_activity_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.observedUser;

    result
      ..add('observed_user')
      ..add(serializers.serialize(value, specifiedType: const FullType(User)));
    value = object.user;

    result
      ..add('user')
      ..add(serializers.serialize(value, specifiedType: const FullType(User)));

    return result;
  }

  @override
  Enrollment deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new EnrollmentBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'role':
          result.role = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'type':
          result.type = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'course_id':
          result.courseId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'course_section_id':
          result.courseSectionId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'enrollment_state':
          result.enrollmentState = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'grades':
          result.grades.replace(serializers.deserialize(value,
              specifiedType: const FullType(Grade))! as Grade);
          break;
        case 'computed_current_score':
          result.computedCurrentScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double?;
          break;
        case 'computed_final_score':
          result.computedFinalScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double?;
          break;
        case 'computed_current_grade':
          result.computedCurrentGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'computed_final_grade':
          result.computedFinalGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'computed_current_letter_grade':
          result.computedCurrentLetterGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'multiple_grading_periods_enabled':
          result.multipleGradingPeriodsEnabled = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'totals_for_all_grading_periods_option':
          result.totalsForAllGradingPeriodsOption = serializers
              .deserialize(value, specifiedType: const FullType(bool))! as bool;
          break;
        case 'current_period_computed_current_score':
          result.currentPeriodComputedCurrentScore = serializers.deserialize(
              value,
              specifiedType: const FullType(double)) as double?;
          break;
        case 'current_period_computed_final_score':
          result.currentPeriodComputedFinalScore = serializers.deserialize(
              value,
              specifiedType: const FullType(double)) as double?;
          break;
        case 'current_period_computed_current_grade':
          result.currentPeriodComputedCurrentGrade = serializers.deserialize(
              value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'current_period_computed_final_grade':
          result.currentPeriodComputedFinalGrade = serializers.deserialize(
              value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'current_grading_period_id':
          result.currentGradingPeriodId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'current_grading_period_title':
          result.currentGradingPeriodTitle = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'associated_user_id':
          result.associatedUserId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'last_activity_at':
          result.lastActivityAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'limit_privileges_to_course_section':
          result.limitPrivilegesToCourseSection = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'observed_user':
          result.observedUser.replace(serializers.deserialize(value,
              specifiedType: const FullType(User))! as User);
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User))! as User);
          break;
      }
    }

    return result.build();
  }
}

class _$Enrollment extends Enrollment {
  @override
  final String? role;
  @override
  final String? type;
  @override
  final String id;
  @override
  final String? courseId;
  @override
  final String? courseSectionId;
  @override
  final String enrollmentState;
  @override
  final String userId;
  @override
  final Grade? grades;
  @override
  final double? computedCurrentScore;
  @override
  final double? computedFinalScore;
  @override
  final String? computedCurrentGrade;
  @override
  final String? computedFinalGrade;
  @override
  final String? computedCurrentLetterGrade;
  @override
  final bool multipleGradingPeriodsEnabled;
  @override
  final bool totalsForAllGradingPeriodsOption;
  @override
  final double? currentPeriodComputedCurrentScore;
  @override
  final double? currentPeriodComputedFinalScore;
  @override
  final String? currentPeriodComputedCurrentGrade;
  @override
  final String? currentPeriodComputedFinalGrade;
  @override
  final String? currentGradingPeriodId;
  @override
  final String? currentGradingPeriodTitle;
  @override
  final String associatedUserId;
  @override
  final DateTime? lastActivityAt;
  @override
  final bool limitPrivilegesToCourseSection;
  @override
  final User? observedUser;
  @override
  final User? user;

  factory _$Enrollment([void Function(EnrollmentBuilder)? updates]) =>
      (new EnrollmentBuilder()..update(updates))._build();

  _$Enrollment._(
      {this.role,
      this.type,
      required this.id,
      this.courseId,
      this.courseSectionId,
      required this.enrollmentState,
      required this.userId,
      this.grades,
      this.computedCurrentScore,
      this.computedFinalScore,
      this.computedCurrentGrade,
      this.computedFinalGrade,
      this.computedCurrentLetterGrade,
      required this.multipleGradingPeriodsEnabled,
      required this.totalsForAllGradingPeriodsOption,
      this.currentPeriodComputedCurrentScore,
      this.currentPeriodComputedFinalScore,
      this.currentPeriodComputedCurrentGrade,
      this.currentPeriodComputedFinalGrade,
      this.currentGradingPeriodId,
      this.currentGradingPeriodTitle,
      required this.associatedUserId,
      this.lastActivityAt,
      required this.limitPrivilegesToCourseSection,
      this.observedUser,
      this.user})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Enrollment', 'id');
    BuiltValueNullFieldError.checkNotNull(
        enrollmentState, r'Enrollment', 'enrollmentState');
    BuiltValueNullFieldError.checkNotNull(userId, r'Enrollment', 'userId');
    BuiltValueNullFieldError.checkNotNull(multipleGradingPeriodsEnabled,
        r'Enrollment', 'multipleGradingPeriodsEnabled');
    BuiltValueNullFieldError.checkNotNull(totalsForAllGradingPeriodsOption,
        r'Enrollment', 'totalsForAllGradingPeriodsOption');
    BuiltValueNullFieldError.checkNotNull(
        associatedUserId, r'Enrollment', 'associatedUserId');
    BuiltValueNullFieldError.checkNotNull(limitPrivilegesToCourseSection,
        r'Enrollment', 'limitPrivilegesToCourseSection');
  }

  @override
  Enrollment rebuild(void Function(EnrollmentBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  EnrollmentBuilder toBuilder() => new EnrollmentBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Enrollment &&
        role == other.role &&
        type == other.type &&
        id == other.id &&
        courseId == other.courseId &&
        courseSectionId == other.courseSectionId &&
        enrollmentState == other.enrollmentState &&
        userId == other.userId &&
        grades == other.grades &&
        computedCurrentScore == other.computedCurrentScore &&
        computedFinalScore == other.computedFinalScore &&
        computedCurrentGrade == other.computedCurrentGrade &&
        computedFinalGrade == other.computedFinalGrade &&
        computedCurrentLetterGrade == other.computedCurrentLetterGrade &&
        multipleGradingPeriodsEnabled == other.multipleGradingPeriodsEnabled &&
        totalsForAllGradingPeriodsOption ==
            other.totalsForAllGradingPeriodsOption &&
        currentPeriodComputedCurrentScore ==
            other.currentPeriodComputedCurrentScore &&
        currentPeriodComputedFinalScore ==
            other.currentPeriodComputedFinalScore &&
        currentPeriodComputedCurrentGrade ==
            other.currentPeriodComputedCurrentGrade &&
        currentPeriodComputedFinalGrade ==
            other.currentPeriodComputedFinalGrade &&
        currentGradingPeriodId == other.currentGradingPeriodId &&
        currentGradingPeriodTitle == other.currentGradingPeriodTitle &&
        associatedUserId == other.associatedUserId &&
        lastActivityAt == other.lastActivityAt &&
        limitPrivilegesToCourseSection ==
            other.limitPrivilegesToCourseSection &&
        observedUser == other.observedUser &&
        user == other.user;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, role.hashCode);
    _$hash = $jc(_$hash, type.hashCode);
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, courseId.hashCode);
    _$hash = $jc(_$hash, courseSectionId.hashCode);
    _$hash = $jc(_$hash, enrollmentState.hashCode);
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, grades.hashCode);
    _$hash = $jc(_$hash, computedCurrentScore.hashCode);
    _$hash = $jc(_$hash, computedFinalScore.hashCode);
    _$hash = $jc(_$hash, computedCurrentGrade.hashCode);
    _$hash = $jc(_$hash, computedFinalGrade.hashCode);
    _$hash = $jc(_$hash, computedCurrentLetterGrade.hashCode);
    _$hash = $jc(_$hash, multipleGradingPeriodsEnabled.hashCode);
    _$hash = $jc(_$hash, totalsForAllGradingPeriodsOption.hashCode);
    _$hash = $jc(_$hash, currentPeriodComputedCurrentScore.hashCode);
    _$hash = $jc(_$hash, currentPeriodComputedFinalScore.hashCode);
    _$hash = $jc(_$hash, currentPeriodComputedCurrentGrade.hashCode);
    _$hash = $jc(_$hash, currentPeriodComputedFinalGrade.hashCode);
    _$hash = $jc(_$hash, currentGradingPeriodId.hashCode);
    _$hash = $jc(_$hash, currentGradingPeriodTitle.hashCode);
    _$hash = $jc(_$hash, associatedUserId.hashCode);
    _$hash = $jc(_$hash, lastActivityAt.hashCode);
    _$hash = $jc(_$hash, limitPrivilegesToCourseSection.hashCode);
    _$hash = $jc(_$hash, observedUser.hashCode);
    _$hash = $jc(_$hash, user.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Enrollment')
          ..add('role', role)
          ..add('type', type)
          ..add('id', id)
          ..add('courseId', courseId)
          ..add('courseSectionId', courseSectionId)
          ..add('enrollmentState', enrollmentState)
          ..add('userId', userId)
          ..add('grades', grades)
          ..add('computedCurrentScore', computedCurrentScore)
          ..add('computedFinalScore', computedFinalScore)
          ..add('computedCurrentGrade', computedCurrentGrade)
          ..add('computedFinalGrade', computedFinalGrade)
          ..add('computedCurrentLetterGrade', computedCurrentLetterGrade)
          ..add('multipleGradingPeriodsEnabled', multipleGradingPeriodsEnabled)
          ..add('totalsForAllGradingPeriodsOption',
              totalsForAllGradingPeriodsOption)
          ..add('currentPeriodComputedCurrentScore',
              currentPeriodComputedCurrentScore)
          ..add('currentPeriodComputedFinalScore',
              currentPeriodComputedFinalScore)
          ..add('currentPeriodComputedCurrentGrade',
              currentPeriodComputedCurrentGrade)
          ..add('currentPeriodComputedFinalGrade',
              currentPeriodComputedFinalGrade)
          ..add('currentGradingPeriodId', currentGradingPeriodId)
          ..add('currentGradingPeriodTitle', currentGradingPeriodTitle)
          ..add('associatedUserId', associatedUserId)
          ..add('lastActivityAt', lastActivityAt)
          ..add(
              'limitPrivilegesToCourseSection', limitPrivilegesToCourseSection)
          ..add('observedUser', observedUser)
          ..add('user', user))
        .toString();
  }
}

class EnrollmentBuilder implements Builder<Enrollment, EnrollmentBuilder> {
  _$Enrollment? _$v;

  String? _role;
  String? get role => _$this._role;
  set role(String? role) => _$this._role = role;

  String? _type;
  String? get type => _$this._type;
  set type(String? type) => _$this._type = type;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _courseId;
  String? get courseId => _$this._courseId;
  set courseId(String? courseId) => _$this._courseId = courseId;

  String? _courseSectionId;
  String? get courseSectionId => _$this._courseSectionId;
  set courseSectionId(String? courseSectionId) =>
      _$this._courseSectionId = courseSectionId;

  String? _enrollmentState;
  String? get enrollmentState => _$this._enrollmentState;
  set enrollmentState(String? enrollmentState) =>
      _$this._enrollmentState = enrollmentState;

  String? _userId;
  String? get userId => _$this._userId;
  set userId(String? userId) => _$this._userId = userId;

  GradeBuilder? _grades;
  GradeBuilder get grades => _$this._grades ??= new GradeBuilder();
  set grades(GradeBuilder? grades) => _$this._grades = grades;

  double? _computedCurrentScore;
  double? get computedCurrentScore => _$this._computedCurrentScore;
  set computedCurrentScore(double? computedCurrentScore) =>
      _$this._computedCurrentScore = computedCurrentScore;

  double? _computedFinalScore;
  double? get computedFinalScore => _$this._computedFinalScore;
  set computedFinalScore(double? computedFinalScore) =>
      _$this._computedFinalScore = computedFinalScore;

  String? _computedCurrentGrade;
  String? get computedCurrentGrade => _$this._computedCurrentGrade;
  set computedCurrentGrade(String? computedCurrentGrade) =>
      _$this._computedCurrentGrade = computedCurrentGrade;

  String? _computedFinalGrade;
  String? get computedFinalGrade => _$this._computedFinalGrade;
  set computedFinalGrade(String? computedFinalGrade) =>
      _$this._computedFinalGrade = computedFinalGrade;

  String? _computedCurrentLetterGrade;
  String? get computedCurrentLetterGrade => _$this._computedCurrentLetterGrade;
  set computedCurrentLetterGrade(String? computedCurrentLetterGrade) =>
      _$this._computedCurrentLetterGrade = computedCurrentLetterGrade;

  bool? _multipleGradingPeriodsEnabled;
  bool? get multipleGradingPeriodsEnabled =>
      _$this._multipleGradingPeriodsEnabled;
  set multipleGradingPeriodsEnabled(bool? multipleGradingPeriodsEnabled) =>
      _$this._multipleGradingPeriodsEnabled = multipleGradingPeriodsEnabled;

  bool? _totalsForAllGradingPeriodsOption;
  bool? get totalsForAllGradingPeriodsOption =>
      _$this._totalsForAllGradingPeriodsOption;
  set totalsForAllGradingPeriodsOption(
          bool? totalsForAllGradingPeriodsOption) =>
      _$this._totalsForAllGradingPeriodsOption =
          totalsForAllGradingPeriodsOption;

  double? _currentPeriodComputedCurrentScore;
  double? get currentPeriodComputedCurrentScore =>
      _$this._currentPeriodComputedCurrentScore;
  set currentPeriodComputedCurrentScore(
          double? currentPeriodComputedCurrentScore) =>
      _$this._currentPeriodComputedCurrentScore =
          currentPeriodComputedCurrentScore;

  double? _currentPeriodComputedFinalScore;
  double? get currentPeriodComputedFinalScore =>
      _$this._currentPeriodComputedFinalScore;
  set currentPeriodComputedFinalScore(
          double? currentPeriodComputedFinalScore) =>
      _$this._currentPeriodComputedFinalScore = currentPeriodComputedFinalScore;

  String? _currentPeriodComputedCurrentGrade;
  String? get currentPeriodComputedCurrentGrade =>
      _$this._currentPeriodComputedCurrentGrade;
  set currentPeriodComputedCurrentGrade(
          String? currentPeriodComputedCurrentGrade) =>
      _$this._currentPeriodComputedCurrentGrade =
          currentPeriodComputedCurrentGrade;

  String? _currentPeriodComputedFinalGrade;
  String? get currentPeriodComputedFinalGrade =>
      _$this._currentPeriodComputedFinalGrade;
  set currentPeriodComputedFinalGrade(
          String? currentPeriodComputedFinalGrade) =>
      _$this._currentPeriodComputedFinalGrade = currentPeriodComputedFinalGrade;

  String? _currentGradingPeriodId;
  String? get currentGradingPeriodId => _$this._currentGradingPeriodId;
  set currentGradingPeriodId(String? currentGradingPeriodId) =>
      _$this._currentGradingPeriodId = currentGradingPeriodId;

  String? _currentGradingPeriodTitle;
  String? get currentGradingPeriodTitle => _$this._currentGradingPeriodTitle;
  set currentGradingPeriodTitle(String? currentGradingPeriodTitle) =>
      _$this._currentGradingPeriodTitle = currentGradingPeriodTitle;

  String? _associatedUserId;
  String? get associatedUserId => _$this._associatedUserId;
  set associatedUserId(String? associatedUserId) =>
      _$this._associatedUserId = associatedUserId;

  DateTime? _lastActivityAt;
  DateTime? get lastActivityAt => _$this._lastActivityAt;
  set lastActivityAt(DateTime? lastActivityAt) =>
      _$this._lastActivityAt = lastActivityAt;

  bool? _limitPrivilegesToCourseSection;
  bool? get limitPrivilegesToCourseSection =>
      _$this._limitPrivilegesToCourseSection;
  set limitPrivilegesToCourseSection(bool? limitPrivilegesToCourseSection) =>
      _$this._limitPrivilegesToCourseSection = limitPrivilegesToCourseSection;

  UserBuilder? _observedUser;
  UserBuilder get observedUser => _$this._observedUser ??= new UserBuilder();
  set observedUser(UserBuilder? observedUser) =>
      _$this._observedUser = observedUser;

  UserBuilder? _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder? user) => _$this._user = user;

  EnrollmentBuilder() {
    Enrollment._initializeBuilder(this);
  }

  EnrollmentBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _role = $v.role;
      _type = $v.type;
      _id = $v.id;
      _courseId = $v.courseId;
      _courseSectionId = $v.courseSectionId;
      _enrollmentState = $v.enrollmentState;
      _userId = $v.userId;
      _grades = $v.grades?.toBuilder();
      _computedCurrentScore = $v.computedCurrentScore;
      _computedFinalScore = $v.computedFinalScore;
      _computedCurrentGrade = $v.computedCurrentGrade;
      _computedFinalGrade = $v.computedFinalGrade;
      _computedCurrentLetterGrade = $v.computedCurrentLetterGrade;
      _multipleGradingPeriodsEnabled = $v.multipleGradingPeriodsEnabled;
      _totalsForAllGradingPeriodsOption = $v.totalsForAllGradingPeriodsOption;
      _currentPeriodComputedCurrentScore = $v.currentPeriodComputedCurrentScore;
      _currentPeriodComputedFinalScore = $v.currentPeriodComputedFinalScore;
      _currentPeriodComputedCurrentGrade = $v.currentPeriodComputedCurrentGrade;
      _currentPeriodComputedFinalGrade = $v.currentPeriodComputedFinalGrade;
      _currentGradingPeriodId = $v.currentGradingPeriodId;
      _currentGradingPeriodTitle = $v.currentGradingPeriodTitle;
      _associatedUserId = $v.associatedUserId;
      _lastActivityAt = $v.lastActivityAt;
      _limitPrivilegesToCourseSection = $v.limitPrivilegesToCourseSection;
      _observedUser = $v.observedUser?.toBuilder();
      _user = $v.user?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Enrollment other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Enrollment;
  }

  @override
  void update(void Function(EnrollmentBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Enrollment build() => _build();

  _$Enrollment _build() {
    _$Enrollment _$result;
    try {
      _$result = _$v ??
          new _$Enrollment._(
              role: role,
              type: type,
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'Enrollment', 'id'),
              courseId: courseId,
              courseSectionId: courseSectionId,
              enrollmentState: BuiltValueNullFieldError.checkNotNull(
                  enrollmentState, r'Enrollment', 'enrollmentState'),
              userId: BuiltValueNullFieldError.checkNotNull(
                  userId, r'Enrollment', 'userId'),
              grades: _grades?.build(),
              computedCurrentScore: computedCurrentScore,
              computedFinalScore: computedFinalScore,
              computedCurrentGrade: computedCurrentGrade,
              computedFinalGrade: computedFinalGrade,
              computedCurrentLetterGrade: computedCurrentLetterGrade,
              multipleGradingPeriodsEnabled: BuiltValueNullFieldError.checkNotNull(
                  multipleGradingPeriodsEnabled, r'Enrollment', 'multipleGradingPeriodsEnabled'),
              totalsForAllGradingPeriodsOption: BuiltValueNullFieldError.checkNotNull(
                  totalsForAllGradingPeriodsOption,
                  r'Enrollment',
                  'totalsForAllGradingPeriodsOption'),
              currentPeriodComputedCurrentScore:
                  currentPeriodComputedCurrentScore,
              currentPeriodComputedFinalScore: currentPeriodComputedFinalScore,
              currentPeriodComputedCurrentGrade:
                  currentPeriodComputedCurrentGrade,
              currentPeriodComputedFinalGrade: currentPeriodComputedFinalGrade,
              currentGradingPeriodId: currentGradingPeriodId,
              currentGradingPeriodTitle: currentGradingPeriodTitle,
              associatedUserId: BuiltValueNullFieldError.checkNotNull(
                  associatedUserId, r'Enrollment', 'associatedUserId'),
              lastActivityAt: lastActivityAt,
              limitPrivilegesToCourseSection: BuiltValueNullFieldError.checkNotNull(
                  limitPrivilegesToCourseSection, r'Enrollment', 'limitPrivilegesToCourseSection'),
              observedUser: _observedUser?.build(),
              user: _user?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'grades';
        _grades?.build();

        _$failedField = 'observedUser';
        _observedUser?.build();
        _$failedField = 'user';
        _user?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'Enrollment', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
