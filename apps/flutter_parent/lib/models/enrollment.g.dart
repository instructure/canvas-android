// GENERATED CODE - DO NOT MODIFY BY HAND

part of enrollment;

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
  Iterable<Object> serialize(Serializers serializers, Enrollment object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
    result.add('role');
    if (object.role == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.role,
          specifiedType: const FullType(String)));
    }
    result.add('type');
    if (object.type == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.type,
          specifiedType: const FullType(String)));
    }
    result.add('course_id');
    if (object.courseId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.courseId,
          specifiedType: const FullType(String)));
    }
    result.add('course_section_id');
    if (object.courseSectionId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.courseSectionId,
          specifiedType: const FullType(String)));
    }
    result.add('grades');
    if (object.grades == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.grades,
          specifiedType: const FullType(Grade)));
    }
    result.add('computed_current_score');
    if (object.computedCurrentScore == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.computedCurrentScore,
          specifiedType: const FullType(double)));
    }
    result.add('computed_final_score');
    if (object.computedFinalScore == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.computedFinalScore,
          specifiedType: const FullType(double)));
    }
    result.add('computed_current_grade');
    if (object.computedCurrentGrade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.computedCurrentGrade,
          specifiedType: const FullType(String)));
    }
    result.add('computed_final_grade');
    if (object.computedFinalGrade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.computedFinalGrade,
          specifiedType: const FullType(String)));
    }
    result.add('current_period_computed_current_score');
    if (object.currentPeriodComputedCurrentScore == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.currentPeriodComputedCurrentScore,
          specifiedType: const FullType(double)));
    }
    result.add('current_period_computed_final_score');
    if (object.currentPeriodComputedFinalScore == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.currentPeriodComputedFinalScore,
          specifiedType: const FullType(double)));
    }
    result.add('current_period_computed_current_grade');
    if (object.currentPeriodComputedCurrentGrade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.currentPeriodComputedCurrentGrade,
          specifiedType: const FullType(String)));
    }
    result.add('current_period_computed_final_grade');
    if (object.currentPeriodComputedFinalGrade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.currentPeriodComputedFinalGrade,
          specifiedType: const FullType(String)));
    }
    result.add('current_grading_period_id');
    if (object.currentGradingPeriodId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.currentGradingPeriodId,
          specifiedType: const FullType(String)));
    }
    result.add('current_grading_period_title');
    if (object.currentGradingPeriodTitle == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.currentGradingPeriodTitle,
          specifiedType: const FullType(String)));
    }
    result.add('last_activity_at');
    if (object.lastActivityAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.lastActivityAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('observed_user');
    if (object.observedUser == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.observedUser,
          specifiedType: const FullType(User)));
    }
    result.add('user');
    if (object.user == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.user,
          specifiedType: const FullType(User)));
    }
    return result;
  }

  @override
  Enrollment deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new EnrollmentBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'role':
          result.role = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'type':
          result.type = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'course_id':
          result.courseId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'course_section_id':
          result.courseSectionId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'enrollment_state':
          result.enrollmentState = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'grades':
          result.grades.replace(serializers.deserialize(value,
              specifiedType: const FullType(Grade)) as Grade);
          break;
        case 'computed_current_score':
          result.computedCurrentScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'computed_final_score':
          result.computedFinalScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'computed_current_grade':
          result.computedCurrentGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'computed_final_grade':
          result.computedFinalGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'multiple_grading_periods_enabled':
          result.multipleGradingPeriodsEnabled = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'totals_for_all_grading_periods_option':
          result.totalsForAllGradingPeriodsOption = serializers
              .deserialize(value, specifiedType: const FullType(bool)) as bool;
          break;
        case 'current_period_computed_current_score':
          result.currentPeriodComputedCurrentScore = serializers.deserialize(
              value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'current_period_computed_final_score':
          result.currentPeriodComputedFinalScore = serializers.deserialize(
              value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'current_period_computed_current_grade':
          result.currentPeriodComputedCurrentGrade = serializers.deserialize(
              value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'current_period_computed_final_grade':
          result.currentPeriodComputedFinalGrade = serializers.deserialize(
              value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'current_grading_period_id':
          result.currentGradingPeriodId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'current_grading_period_title':
          result.currentGradingPeriodTitle = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'associated_user_id':
          result.associatedUserId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'last_activity_at':
          result.lastActivityAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'limit_privileges_to_course_section':
          result.limitPrivilegesToCourseSection = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'observed_user':
          result.observedUser.replace(serializers.deserialize(value,
              specifiedType: const FullType(User)) as User);
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User)) as User);
          break;
      }
    }

    return result.build();
  }
}

class _$Enrollment extends Enrollment {
  @override
  final String role;
  @override
  final String type;
  @override
  final String id;
  @override
  final String courseId;
  @override
  final String courseSectionId;
  @override
  final String enrollmentState;
  @override
  final String userId;
  @override
  final Grade grades;
  @override
  final double computedCurrentScore;
  @override
  final double computedFinalScore;
  @override
  final String computedCurrentGrade;
  @override
  final String computedFinalGrade;
  @override
  final bool multipleGradingPeriodsEnabled;
  @override
  final bool totalsForAllGradingPeriodsOption;
  @override
  final double currentPeriodComputedCurrentScore;
  @override
  final double currentPeriodComputedFinalScore;
  @override
  final String currentPeriodComputedCurrentGrade;
  @override
  final String currentPeriodComputedFinalGrade;
  @override
  final String currentGradingPeriodId;
  @override
  final String currentGradingPeriodTitle;
  @override
  final String associatedUserId;
  @override
  final DateTime lastActivityAt;
  @override
  final bool limitPrivilegesToCourseSection;
  @override
  final User observedUser;
  @override
  final User user;

  factory _$Enrollment([void Function(EnrollmentBuilder) updates]) =>
      (new EnrollmentBuilder()..update(updates)).build();

  _$Enrollment._(
      {this.role,
      this.type,
      this.id,
      this.courseId,
      this.courseSectionId,
      this.enrollmentState,
      this.userId,
      this.grades,
      this.computedCurrentScore,
      this.computedFinalScore,
      this.computedCurrentGrade,
      this.computedFinalGrade,
      this.multipleGradingPeriodsEnabled,
      this.totalsForAllGradingPeriodsOption,
      this.currentPeriodComputedCurrentScore,
      this.currentPeriodComputedFinalScore,
      this.currentPeriodComputedCurrentGrade,
      this.currentPeriodComputedFinalGrade,
      this.currentGradingPeriodId,
      this.currentGradingPeriodTitle,
      this.associatedUserId,
      this.lastActivityAt,
      this.limitPrivilegesToCourseSection,
      this.observedUser,
      this.user})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Enrollment', 'id');
    }
    if (enrollmentState == null) {
      throw new BuiltValueNullFieldError('Enrollment', 'enrollmentState');
    }
    if (userId == null) {
      throw new BuiltValueNullFieldError('Enrollment', 'userId');
    }
    if (multipleGradingPeriodsEnabled == null) {
      throw new BuiltValueNullFieldError(
          'Enrollment', 'multipleGradingPeriodsEnabled');
    }
    if (totalsForAllGradingPeriodsOption == null) {
      throw new BuiltValueNullFieldError(
          'Enrollment', 'totalsForAllGradingPeriodsOption');
    }
    if (associatedUserId == null) {
      throw new BuiltValueNullFieldError('Enrollment', 'associatedUserId');
    }
    if (limitPrivilegesToCourseSection == null) {
      throw new BuiltValueNullFieldError(
          'Enrollment', 'limitPrivilegesToCourseSection');
    }
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
                                                                            $jc($jc($jc($jc($jc($jc($jc(0, role.hashCode), type.hashCode), id.hashCode), courseId.hashCode), courseSectionId.hashCode), enrollmentState.hashCode),
                                                                                userId.hashCode),
                                                                            grades.hashCode),
                                                                        computedCurrentScore.hashCode),
                                                                    computedFinalScore.hashCode),
                                                                computedCurrentGrade.hashCode),
                                                            computedFinalGrade.hashCode),
                                                        multipleGradingPeriodsEnabled.hashCode),
                                                    totalsForAllGradingPeriodsOption.hashCode),
                                                currentPeriodComputedCurrentScore.hashCode),
                                            currentPeriodComputedFinalScore.hashCode),
                                        currentPeriodComputedCurrentGrade.hashCode),
                                    currentPeriodComputedFinalGrade.hashCode),
                                currentGradingPeriodId.hashCode),
                            currentGradingPeriodTitle.hashCode),
                        associatedUserId.hashCode),
                    lastActivityAt.hashCode),
                limitPrivilegesToCourseSection.hashCode),
            observedUser.hashCode),
        user.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Enrollment')
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
  _$Enrollment _$v;

  String _role;
  String get role => _$this._role;
  set role(String role) => _$this._role = role;

  String _type;
  String get type => _$this._type;
  set type(String type) => _$this._type = type;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _courseId;
  String get courseId => _$this._courseId;
  set courseId(String courseId) => _$this._courseId = courseId;

  String _courseSectionId;
  String get courseSectionId => _$this._courseSectionId;
  set courseSectionId(String courseSectionId) =>
      _$this._courseSectionId = courseSectionId;

  String _enrollmentState;
  String get enrollmentState => _$this._enrollmentState;
  set enrollmentState(String enrollmentState) =>
      _$this._enrollmentState = enrollmentState;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  GradeBuilder _grades;
  GradeBuilder get grades => _$this._grades ??= new GradeBuilder();
  set grades(GradeBuilder grades) => _$this._grades = grades;

  double _computedCurrentScore;
  double get computedCurrentScore => _$this._computedCurrentScore;
  set computedCurrentScore(double computedCurrentScore) =>
      _$this._computedCurrentScore = computedCurrentScore;

  double _computedFinalScore;
  double get computedFinalScore => _$this._computedFinalScore;
  set computedFinalScore(double computedFinalScore) =>
      _$this._computedFinalScore = computedFinalScore;

  String _computedCurrentGrade;
  String get computedCurrentGrade => _$this._computedCurrentGrade;
  set computedCurrentGrade(String computedCurrentGrade) =>
      _$this._computedCurrentGrade = computedCurrentGrade;

  String _computedFinalGrade;
  String get computedFinalGrade => _$this._computedFinalGrade;
  set computedFinalGrade(String computedFinalGrade) =>
      _$this._computedFinalGrade = computedFinalGrade;

  bool _multipleGradingPeriodsEnabled;
  bool get multipleGradingPeriodsEnabled =>
      _$this._multipleGradingPeriodsEnabled;
  set multipleGradingPeriodsEnabled(bool multipleGradingPeriodsEnabled) =>
      _$this._multipleGradingPeriodsEnabled = multipleGradingPeriodsEnabled;

  bool _totalsForAllGradingPeriodsOption;
  bool get totalsForAllGradingPeriodsOption =>
      _$this._totalsForAllGradingPeriodsOption;
  set totalsForAllGradingPeriodsOption(bool totalsForAllGradingPeriodsOption) =>
      _$this._totalsForAllGradingPeriodsOption =
          totalsForAllGradingPeriodsOption;

  double _currentPeriodComputedCurrentScore;
  double get currentPeriodComputedCurrentScore =>
      _$this._currentPeriodComputedCurrentScore;
  set currentPeriodComputedCurrentScore(
          double currentPeriodComputedCurrentScore) =>
      _$this._currentPeriodComputedCurrentScore =
          currentPeriodComputedCurrentScore;

  double _currentPeriodComputedFinalScore;
  double get currentPeriodComputedFinalScore =>
      _$this._currentPeriodComputedFinalScore;
  set currentPeriodComputedFinalScore(double currentPeriodComputedFinalScore) =>
      _$this._currentPeriodComputedFinalScore = currentPeriodComputedFinalScore;

  String _currentPeriodComputedCurrentGrade;
  String get currentPeriodComputedCurrentGrade =>
      _$this._currentPeriodComputedCurrentGrade;
  set currentPeriodComputedCurrentGrade(
          String currentPeriodComputedCurrentGrade) =>
      _$this._currentPeriodComputedCurrentGrade =
          currentPeriodComputedCurrentGrade;

  String _currentPeriodComputedFinalGrade;
  String get currentPeriodComputedFinalGrade =>
      _$this._currentPeriodComputedFinalGrade;
  set currentPeriodComputedFinalGrade(String currentPeriodComputedFinalGrade) =>
      _$this._currentPeriodComputedFinalGrade = currentPeriodComputedFinalGrade;

  String _currentGradingPeriodId;
  String get currentGradingPeriodId => _$this._currentGradingPeriodId;
  set currentGradingPeriodId(String currentGradingPeriodId) =>
      _$this._currentGradingPeriodId = currentGradingPeriodId;

  String _currentGradingPeriodTitle;
  String get currentGradingPeriodTitle => _$this._currentGradingPeriodTitle;
  set currentGradingPeriodTitle(String currentGradingPeriodTitle) =>
      _$this._currentGradingPeriodTitle = currentGradingPeriodTitle;

  String _associatedUserId;
  String get associatedUserId => _$this._associatedUserId;
  set associatedUserId(String associatedUserId) =>
      _$this._associatedUserId = associatedUserId;

  DateTime _lastActivityAt;
  DateTime get lastActivityAt => _$this._lastActivityAt;
  set lastActivityAt(DateTime lastActivityAt) =>
      _$this._lastActivityAt = lastActivityAt;

  bool _limitPrivilegesToCourseSection;
  bool get limitPrivilegesToCourseSection =>
      _$this._limitPrivilegesToCourseSection;
  set limitPrivilegesToCourseSection(bool limitPrivilegesToCourseSection) =>
      _$this._limitPrivilegesToCourseSection = limitPrivilegesToCourseSection;

  UserBuilder _observedUser;
  UserBuilder get observedUser => _$this._observedUser ??= new UserBuilder();
  set observedUser(UserBuilder observedUser) =>
      _$this._observedUser = observedUser;

  UserBuilder _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder user) => _$this._user = user;

  EnrollmentBuilder() {
    Enrollment._initializeBuilder(this);
  }

  EnrollmentBuilder get _$this {
    if (_$v != null) {
      _role = _$v.role;
      _type = _$v.type;
      _id = _$v.id;
      _courseId = _$v.courseId;
      _courseSectionId = _$v.courseSectionId;
      _enrollmentState = _$v.enrollmentState;
      _userId = _$v.userId;
      _grades = _$v.grades?.toBuilder();
      _computedCurrentScore = _$v.computedCurrentScore;
      _computedFinalScore = _$v.computedFinalScore;
      _computedCurrentGrade = _$v.computedCurrentGrade;
      _computedFinalGrade = _$v.computedFinalGrade;
      _multipleGradingPeriodsEnabled = _$v.multipleGradingPeriodsEnabled;
      _totalsForAllGradingPeriodsOption = _$v.totalsForAllGradingPeriodsOption;
      _currentPeriodComputedCurrentScore =
          _$v.currentPeriodComputedCurrentScore;
      _currentPeriodComputedFinalScore = _$v.currentPeriodComputedFinalScore;
      _currentPeriodComputedCurrentGrade =
          _$v.currentPeriodComputedCurrentGrade;
      _currentPeriodComputedFinalGrade = _$v.currentPeriodComputedFinalGrade;
      _currentGradingPeriodId = _$v.currentGradingPeriodId;
      _currentGradingPeriodTitle = _$v.currentGradingPeriodTitle;
      _associatedUserId = _$v.associatedUserId;
      _lastActivityAt = _$v.lastActivityAt;
      _limitPrivilegesToCourseSection = _$v.limitPrivilegesToCourseSection;
      _observedUser = _$v.observedUser?.toBuilder();
      _user = _$v.user?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Enrollment other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Enrollment;
  }

  @override
  void update(void Function(EnrollmentBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Enrollment build() {
    _$Enrollment _$result;
    try {
      _$result = _$v ??
          new _$Enrollment._(
              role: role,
              type: type,
              id: id,
              courseId: courseId,
              courseSectionId: courseSectionId,
              enrollmentState: enrollmentState,
              userId: userId,
              grades: _grades?.build(),
              computedCurrentScore: computedCurrentScore,
              computedFinalScore: computedFinalScore,
              computedCurrentGrade: computedCurrentGrade,
              computedFinalGrade: computedFinalGrade,
              multipleGradingPeriodsEnabled: multipleGradingPeriodsEnabled,
              totalsForAllGradingPeriodsOption:
                  totalsForAllGradingPeriodsOption,
              currentPeriodComputedCurrentScore:
                  currentPeriodComputedCurrentScore,
              currentPeriodComputedFinalScore: currentPeriodComputedFinalScore,
              currentPeriodComputedCurrentGrade:
                  currentPeriodComputedCurrentGrade,
              currentPeriodComputedFinalGrade: currentPeriodComputedFinalGrade,
              currentGradingPeriodId: currentGradingPeriodId,
              currentGradingPeriodTitle: currentGradingPeriodTitle,
              associatedUserId: associatedUserId,
              lastActivityAt: lastActivityAt,
              limitPrivilegesToCourseSection: limitPrivilegesToCourseSection,
              observedUser: _observedUser?.build(),
              user: _user?.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'grades';
        _grades?.build();

        _$failedField = 'observedUser';
        _observedUser?.build();
        _$failedField = 'user';
        _user?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Enrollment', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
