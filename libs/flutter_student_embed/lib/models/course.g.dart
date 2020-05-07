// GENERATED CODE - DO NOT MODIFY BY HAND

part of course;

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const HomePage _$homePageFeed = const HomePage._('feed');
const HomePage _$homePageWiki = const HomePage._('wiki');
const HomePage _$homePageModules = const HomePage._('modules');
const HomePage _$homePageAssignments = const HomePage._('assignments');
const HomePage _$homePageSyllabus = const HomePage._('syllabus');

HomePage _$homePageValueOf(String name) {
  switch (name) {
    case 'feed':
      return _$homePageFeed;
    case 'wiki':
      return _$homePageWiki;
    case 'modules':
      return _$homePageModules;
    case 'assignments':
      return _$homePageAssignments;
    case 'syllabus':
      return _$homePageSyllabus;
    default:
      return _$homePageSyllabus;
  }
}

final BuiltSet<HomePage> _$homePageValues =
    new BuiltSet<HomePage>(const <HomePage>[
  _$homePageFeed,
  _$homePageWiki,
  _$homePageModules,
  _$homePageAssignments,
  _$homePageSyllabus,
]);

Serializer<Course> _$courseSerializer = new _$CourseSerializer();
Serializer<HomePage> _$homePageSerializer = new _$HomePageSerializer();

class _$CourseSerializer implements StructuredSerializer<Course> {
  @override
  final Iterable<Type> types = const [Course, _$Course];
  @override
  final String wireName = 'Course';

  @override
  Iterable<Object> serialize(Serializers serializers, Course object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'hide_final_grades',
      serializers.serialize(object.hideFinalGrades,
          specifiedType: const FullType(bool)),
      'is_public',
      serializers.serialize(object.isPublic,
          specifiedType: const FullType(bool)),
      'enrollments',
      serializers.serialize(object.enrollments,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Enrollment)])),
      'needs_grading_count',
      serializers.serialize(object.needsGradingCount,
          specifiedType: const FullType(int)),
      'apply_assignment_group_weights',
      serializers.serialize(object.applyAssignmentGroupWeights,
          specifiedType: const FullType(bool)),
      'is_favorite',
      serializers.serialize(object.isFavorite,
          specifiedType: const FullType(bool)),
      'access_restricted_by_date',
      serializers.serialize(object.accessRestrictedByDate,
          specifiedType: const FullType(bool)),
      'has_weighted_grading_periods',
      serializers.serialize(object.hasWeightedGradingPeriods,
          specifiedType: const FullType(bool)),
      'has_grading_periods',
      serializers.serialize(object.hasGradingPeriods,
          specifiedType: const FullType(bool)),
      'restrict_enrollments_to_course_dates',
      serializers.serialize(object.restrictEnrollmentsToCourseDates,
          specifiedType: const FullType(bool)),
    ];
    result.add('original_name');
    if (object.originalName == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.originalName,
          specifiedType: const FullType(String)));
    }
    result.add('course_code');
    if (object.courseCode == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.courseCode,
          specifiedType: const FullType(String)));
    }
    result.add('start_at');
    if (object.startAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.startAt,
          specifiedType: const FullType(String)));
    }
    result.add('end_at');
    if (object.endAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.endAt,
          specifiedType: const FullType(String)));
    }
    result.add('syllabus_body');
    if (object.syllabusBody == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.syllabusBody,
          specifiedType: const FullType(String)));
    }
    result.add('image_download_url');
    if (object.imageDownloadUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.imageDownloadUrl,
          specifiedType: const FullType(String)));
    }
    result.add('workflow_state');
    if (object.workflowState == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.workflowState,
          specifiedType: const FullType(String)));
    }
    result.add('default_view');
    if (object.homePage == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.homePage,
          specifiedType: const FullType(HomePage)));
    }
    return result;
  }

  @override
  Course deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CourseBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'original_name':
          result.originalName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'course_code':
          result.courseCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'start_at':
          result.startAt = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'end_at':
          result.endAt = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'syllabus_body':
          result.syllabusBody = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'hide_final_grades':
          result.hideFinalGrades = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'is_public':
          result.isPublic = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'enrollments':
          result.enrollments.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Enrollment)]))
              as BuiltList<Object>);
          break;
        case 'needs_grading_count':
          result.needsGradingCount = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'apply_assignment_group_weights':
          result.applyAssignmentGroupWeights = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'is_favorite':
          result.isFavorite = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'access_restricted_by_date':
          result.accessRestrictedByDate = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'image_download_url':
          result.imageDownloadUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'has_weighted_grading_periods':
          result.hasWeightedGradingPeriods = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'has_grading_periods':
          result.hasGradingPeriods = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'restrict_enrollments_to_course_dates':
          result.restrictEnrollmentsToCourseDates = serializers
              .deserialize(value, specifiedType: const FullType(bool)) as bool;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'default_view':
          result.homePage = serializers.deserialize(value,
              specifiedType: const FullType(HomePage)) as HomePage;
          break;
      }
    }

    return result.build();
  }
}

class _$HomePageSerializer implements PrimitiveSerializer<HomePage> {
  @override
  final Iterable<Type> types = const <Type>[HomePage];
  @override
  final String wireName = 'default_view';

  @override
  Object serialize(Serializers serializers, HomePage object,
          {FullType specifiedType = FullType.unspecified}) =>
      object.name;

  @override
  HomePage deserialize(Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      HomePage.valueOf(serialized as String);
}

class _$Course extends Course {
  @override
  final double currentScore;
  @override
  final double finalScore;
  @override
  final String currentGrade;
  @override
  final String finalGrade;
  @override
  final String id;
  @override
  final String name;
  @override
  final String originalName;
  @override
  final String courseCode;
  @override
  final String startAt;
  @override
  final String endAt;
  @override
  final String syllabusBody;
  @override
  final bool hideFinalGrades;
  @override
  final bool isPublic;
  @override
  final BuiltList<Enrollment> enrollments;
  @override
  final int needsGradingCount;
  @override
  final bool applyAssignmentGroupWeights;
  @override
  final bool isFavorite;
  @override
  final bool accessRestrictedByDate;
  @override
  final String imageDownloadUrl;
  @override
  final bool hasWeightedGradingPeriods;
  @override
  final bool hasGradingPeriods;
  @override
  final bool restrictEnrollmentsToCourseDates;
  @override
  final String workflowState;
  @override
  final HomePage homePage;

  factory _$Course([void Function(CourseBuilder) updates]) =>
      (new CourseBuilder()..update(updates)).build();

  _$Course._(
      {this.currentScore,
      this.finalScore,
      this.currentGrade,
      this.finalGrade,
      this.id,
      this.name,
      this.originalName,
      this.courseCode,
      this.startAt,
      this.endAt,
      this.syllabusBody,
      this.hideFinalGrades,
      this.isPublic,
      this.enrollments,
      this.needsGradingCount,
      this.applyAssignmentGroupWeights,
      this.isFavorite,
      this.accessRestrictedByDate,
      this.imageDownloadUrl,
      this.hasWeightedGradingPeriods,
      this.hasGradingPeriods,
      this.restrictEnrollmentsToCourseDates,
      this.workflowState,
      this.homePage})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Course', 'id');
    }
    if (name == null) {
      throw new BuiltValueNullFieldError('Course', 'name');
    }
    if (hideFinalGrades == null) {
      throw new BuiltValueNullFieldError('Course', 'hideFinalGrades');
    }
    if (isPublic == null) {
      throw new BuiltValueNullFieldError('Course', 'isPublic');
    }
    if (enrollments == null) {
      throw new BuiltValueNullFieldError('Course', 'enrollments');
    }
    if (needsGradingCount == null) {
      throw new BuiltValueNullFieldError('Course', 'needsGradingCount');
    }
    if (applyAssignmentGroupWeights == null) {
      throw new BuiltValueNullFieldError(
          'Course', 'applyAssignmentGroupWeights');
    }
    if (isFavorite == null) {
      throw new BuiltValueNullFieldError('Course', 'isFavorite');
    }
    if (accessRestrictedByDate == null) {
      throw new BuiltValueNullFieldError('Course', 'accessRestrictedByDate');
    }
    if (hasWeightedGradingPeriods == null) {
      throw new BuiltValueNullFieldError('Course', 'hasWeightedGradingPeriods');
    }
    if (hasGradingPeriods == null) {
      throw new BuiltValueNullFieldError('Course', 'hasGradingPeriods');
    }
    if (restrictEnrollmentsToCourseDates == null) {
      throw new BuiltValueNullFieldError(
          'Course', 'restrictEnrollmentsToCourseDates');
    }
  }

  @override
  Course rebuild(void Function(CourseBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CourseBuilder toBuilder() => new CourseBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Course &&
        currentScore == other.currentScore &&
        finalScore == other.finalScore &&
        currentGrade == other.currentGrade &&
        finalGrade == other.finalGrade &&
        id == other.id &&
        name == other.name &&
        originalName == other.originalName &&
        courseCode == other.courseCode &&
        startAt == other.startAt &&
        endAt == other.endAt &&
        syllabusBody == other.syllabusBody &&
        hideFinalGrades == other.hideFinalGrades &&
        isPublic == other.isPublic &&
        enrollments == other.enrollments &&
        needsGradingCount == other.needsGradingCount &&
        applyAssignmentGroupWeights == other.applyAssignmentGroupWeights &&
        isFavorite == other.isFavorite &&
        accessRestrictedByDate == other.accessRestrictedByDate &&
        imageDownloadUrl == other.imageDownloadUrl &&
        hasWeightedGradingPeriods == other.hasWeightedGradingPeriods &&
        hasGradingPeriods == other.hasGradingPeriods &&
        restrictEnrollmentsToCourseDates ==
            other.restrictEnrollmentsToCourseDates &&
        workflowState == other.workflowState &&
        homePage == other.homePage;
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
                                                                            $jc($jc($jc($jc($jc($jc(0, currentScore.hashCode), finalScore.hashCode), currentGrade.hashCode), finalGrade.hashCode), id.hashCode),
                                                                                name.hashCode),
                                                                            originalName.hashCode),
                                                                        courseCode.hashCode),
                                                                    startAt.hashCode),
                                                                endAt.hashCode),
                                                            syllabusBody.hashCode),
                                                        hideFinalGrades.hashCode),
                                                    isPublic.hashCode),
                                                enrollments.hashCode),
                                            needsGradingCount.hashCode),
                                        applyAssignmentGroupWeights.hashCode),
                                    isFavorite.hashCode),
                                accessRestrictedByDate.hashCode),
                            imageDownloadUrl.hashCode),
                        hasWeightedGradingPeriods.hashCode),
                    hasGradingPeriods.hashCode),
                restrictEnrollmentsToCourseDates.hashCode),
            workflowState.hashCode),
        homePage.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Course')
          ..add('currentScore', currentScore)
          ..add('finalScore', finalScore)
          ..add('currentGrade', currentGrade)
          ..add('finalGrade', finalGrade)
          ..add('id', id)
          ..add('name', name)
          ..add('originalName', originalName)
          ..add('courseCode', courseCode)
          ..add('startAt', startAt)
          ..add('endAt', endAt)
          ..add('syllabusBody', syllabusBody)
          ..add('hideFinalGrades', hideFinalGrades)
          ..add('isPublic', isPublic)
          ..add('enrollments', enrollments)
          ..add('needsGradingCount', needsGradingCount)
          ..add('applyAssignmentGroupWeights', applyAssignmentGroupWeights)
          ..add('isFavorite', isFavorite)
          ..add('accessRestrictedByDate', accessRestrictedByDate)
          ..add('imageDownloadUrl', imageDownloadUrl)
          ..add('hasWeightedGradingPeriods', hasWeightedGradingPeriods)
          ..add('hasGradingPeriods', hasGradingPeriods)
          ..add('restrictEnrollmentsToCourseDates',
              restrictEnrollmentsToCourseDates)
          ..add('workflowState', workflowState)
          ..add('homePage', homePage))
        .toString();
  }
}

class CourseBuilder implements Builder<Course, CourseBuilder> {
  _$Course _$v;

  double _currentScore;
  double get currentScore => _$this._currentScore;
  set currentScore(double currentScore) => _$this._currentScore = currentScore;

  double _finalScore;
  double get finalScore => _$this._finalScore;
  set finalScore(double finalScore) => _$this._finalScore = finalScore;

  String _currentGrade;
  String get currentGrade => _$this._currentGrade;
  set currentGrade(String currentGrade) => _$this._currentGrade = currentGrade;

  String _finalGrade;
  String get finalGrade => _$this._finalGrade;
  set finalGrade(String finalGrade) => _$this._finalGrade = finalGrade;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _originalName;
  String get originalName => _$this._originalName;
  set originalName(String originalName) => _$this._originalName = originalName;

  String _courseCode;
  String get courseCode => _$this._courseCode;
  set courseCode(String courseCode) => _$this._courseCode = courseCode;

  String _startAt;
  String get startAt => _$this._startAt;
  set startAt(String startAt) => _$this._startAt = startAt;

  String _endAt;
  String get endAt => _$this._endAt;
  set endAt(String endAt) => _$this._endAt = endAt;

  String _syllabusBody;
  String get syllabusBody => _$this._syllabusBody;
  set syllabusBody(String syllabusBody) => _$this._syllabusBody = syllabusBody;

  bool _hideFinalGrades;
  bool get hideFinalGrades => _$this._hideFinalGrades;
  set hideFinalGrades(bool hideFinalGrades) =>
      _$this._hideFinalGrades = hideFinalGrades;

  bool _isPublic;
  bool get isPublic => _$this._isPublic;
  set isPublic(bool isPublic) => _$this._isPublic = isPublic;

  ListBuilder<Enrollment> _enrollments;
  ListBuilder<Enrollment> get enrollments =>
      _$this._enrollments ??= new ListBuilder<Enrollment>();
  set enrollments(ListBuilder<Enrollment> enrollments) =>
      _$this._enrollments = enrollments;

  int _needsGradingCount;
  int get needsGradingCount => _$this._needsGradingCount;
  set needsGradingCount(int needsGradingCount) =>
      _$this._needsGradingCount = needsGradingCount;

  bool _applyAssignmentGroupWeights;
  bool get applyAssignmentGroupWeights => _$this._applyAssignmentGroupWeights;
  set applyAssignmentGroupWeights(bool applyAssignmentGroupWeights) =>
      _$this._applyAssignmentGroupWeights = applyAssignmentGroupWeights;

  bool _isFavorite;
  bool get isFavorite => _$this._isFavorite;
  set isFavorite(bool isFavorite) => _$this._isFavorite = isFavorite;

  bool _accessRestrictedByDate;
  bool get accessRestrictedByDate => _$this._accessRestrictedByDate;
  set accessRestrictedByDate(bool accessRestrictedByDate) =>
      _$this._accessRestrictedByDate = accessRestrictedByDate;

  String _imageDownloadUrl;
  String get imageDownloadUrl => _$this._imageDownloadUrl;
  set imageDownloadUrl(String imageDownloadUrl) =>
      _$this._imageDownloadUrl = imageDownloadUrl;

  bool _hasWeightedGradingPeriods;
  bool get hasWeightedGradingPeriods => _$this._hasWeightedGradingPeriods;
  set hasWeightedGradingPeriods(bool hasWeightedGradingPeriods) =>
      _$this._hasWeightedGradingPeriods = hasWeightedGradingPeriods;

  bool _hasGradingPeriods;
  bool get hasGradingPeriods => _$this._hasGradingPeriods;
  set hasGradingPeriods(bool hasGradingPeriods) =>
      _$this._hasGradingPeriods = hasGradingPeriods;

  bool _restrictEnrollmentsToCourseDates;
  bool get restrictEnrollmentsToCourseDates =>
      _$this._restrictEnrollmentsToCourseDates;
  set restrictEnrollmentsToCourseDates(bool restrictEnrollmentsToCourseDates) =>
      _$this._restrictEnrollmentsToCourseDates =
          restrictEnrollmentsToCourseDates;

  String _workflowState;
  String get workflowState => _$this._workflowState;
  set workflowState(String workflowState) =>
      _$this._workflowState = workflowState;

  HomePage _homePage;
  HomePage get homePage => _$this._homePage;
  set homePage(HomePage homePage) => _$this._homePage = homePage;

  CourseBuilder() {
    Course._initializeBuilder(this);
  }

  CourseBuilder get _$this {
    if (_$v != null) {
      _currentScore = _$v.currentScore;
      _finalScore = _$v.finalScore;
      _currentGrade = _$v.currentGrade;
      _finalGrade = _$v.finalGrade;
      _id = _$v.id;
      _name = _$v.name;
      _originalName = _$v.originalName;
      _courseCode = _$v.courseCode;
      _startAt = _$v.startAt;
      _endAt = _$v.endAt;
      _syllabusBody = _$v.syllabusBody;
      _hideFinalGrades = _$v.hideFinalGrades;
      _isPublic = _$v.isPublic;
      _enrollments = _$v.enrollments?.toBuilder();
      _needsGradingCount = _$v.needsGradingCount;
      _applyAssignmentGroupWeights = _$v.applyAssignmentGroupWeights;
      _isFavorite = _$v.isFavorite;
      _accessRestrictedByDate = _$v.accessRestrictedByDate;
      _imageDownloadUrl = _$v.imageDownloadUrl;
      _hasWeightedGradingPeriods = _$v.hasWeightedGradingPeriods;
      _hasGradingPeriods = _$v.hasGradingPeriods;
      _restrictEnrollmentsToCourseDates = _$v.restrictEnrollmentsToCourseDates;
      _workflowState = _$v.workflowState;
      _homePage = _$v.homePage;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Course other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Course;
  }

  @override
  void update(void Function(CourseBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Course build() {
    _$Course _$result;
    try {
      _$result = _$v ??
          new _$Course._(
              currentScore: currentScore,
              finalScore: finalScore,
              currentGrade: currentGrade,
              finalGrade: finalGrade,
              id: id,
              name: name,
              originalName: originalName,
              courseCode: courseCode,
              startAt: startAt,
              endAt: endAt,
              syllabusBody: syllabusBody,
              hideFinalGrades: hideFinalGrades,
              isPublic: isPublic,
              enrollments: enrollments.build(),
              needsGradingCount: needsGradingCount,
              applyAssignmentGroupWeights: applyAssignmentGroupWeights,
              isFavorite: isFavorite,
              accessRestrictedByDate: accessRestrictedByDate,
              imageDownloadUrl: imageDownloadUrl,
              hasWeightedGradingPeriods: hasWeightedGradingPeriods,
              hasGradingPeriods: hasGradingPeriods,
              restrictEnrollmentsToCourseDates:
                  restrictEnrollmentsToCourseDates,
              workflowState: workflowState,
              homePage: homePage);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'enrollments';
        enrollments.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Course', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
