// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'course.dart';

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
  Iterable<Object?> serialize(Serializers serializers, Course object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
    Object? value;
    value = object.originalName;

    result
      ..add('original_name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.courseCode;

    result
      ..add('course_code')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.startAt;

    result
      ..add('start_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.endAt;

    result
      ..add('end_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.syllabusBody;

    result
      ..add('syllabus_body')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.enrollments;

    result
      ..add('enrollments')
      ..add(serializers.serialize(value,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Enrollment)])));
    value = object.imageDownloadUrl;

    result
      ..add('image_download_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.workflowState;

    result
      ..add('workflow_state')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.homePage;

    result
      ..add('default_view')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(HomePage)));
    value = object.term;

    result
      ..add('term')
      ..add(serializers.serialize(value, specifiedType: const FullType(Term)));
    value = object.sections;

    result
      ..add('sections')
      ..add(serializers.serialize(value,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Section)])));
    value = object.settings;

    result
      ..add('settings')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(CourseSettings)));
    value = object.gradingScheme;

    result
      ..add('grading_scheme')
      ..add(serializers.serialize(value,
          specifiedType:
              const FullType(BuiltList, const [const FullType(JsonObject)])));

    return result;
  }

  @override
  Course deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CourseBuilder();

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
              specifiedType: const FullType(String))! as String;
          break;
        case 'original_name':
          result.originalName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'course_code':
          result.courseCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'start_at':
          result.startAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'end_at':
          result.endAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'syllabus_body':
          result.syllabusBody = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'hide_final_grades':
          result.hideFinalGrades = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'is_public':
          result.isPublic = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'enrollments':
          result.enrollments.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Enrollment)]))!
              as BuiltList<Object?>);
          break;
        case 'needs_grading_count':
          result.needsGradingCount = serializers.deserialize(value,
              specifiedType: const FullType(int))! as int;
          break;
        case 'apply_assignment_group_weights':
          result.applyAssignmentGroupWeights = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'is_favorite':
          result.isFavorite = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'access_restricted_by_date':
          result.accessRestrictedByDate = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'image_download_url':
          result.imageDownloadUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'has_weighted_grading_periods':
          result.hasWeightedGradingPeriods = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'has_grading_periods':
          result.hasGradingPeriods = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'restrict_enrollments_to_course_dates':
          result.restrictEnrollmentsToCourseDates = serializers
              .deserialize(value, specifiedType: const FullType(bool))! as bool;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'default_view':
          result.homePage = serializers.deserialize(value,
              specifiedType: const FullType(HomePage)) as HomePage?;
          break;
        case 'term':
          result.term.replace(serializers.deserialize(value,
              specifiedType: const FullType(Term))! as Term);
          break;
        case 'sections':
          result.sections.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Section)]))!
              as BuiltList<Object?>);
          break;
        case 'settings':
          result.settings.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CourseSettings))!
              as CourseSettings);
          break;
        case 'grading_scheme':
          result.gradingScheme.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(JsonObject)]))!
              as BuiltList<Object?>);
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
  final double? currentScore;
  @override
  final double? finalScore;
  @override
  final String? currentGrade;
  @override
  final String? finalGrade;
  @override
  final String id;
  @override
  final String name;
  @override
  final String? originalName;
  @override
  final String? courseCode;
  @override
  final DateTime? startAt;
  @override
  final DateTime? endAt;
  @override
  final String? syllabusBody;
  @override
  final bool hideFinalGrades;
  @override
  final bool isPublic;
  @override
  final BuiltList<Enrollment>? enrollments;
  @override
  final int needsGradingCount;
  @override
  final bool applyAssignmentGroupWeights;
  @override
  final bool isFavorite;
  @override
  final bool accessRestrictedByDate;
  @override
  final String? imageDownloadUrl;
  @override
  final bool hasWeightedGradingPeriods;
  @override
  final bool hasGradingPeriods;
  @override
  final bool restrictEnrollmentsToCourseDates;
  @override
  final String? workflowState;
  @override
  final HomePage? homePage;
  @override
  final Term? term;
  @override
  final BuiltList<Section>? sections;
  @override
  final CourseSettings? settings;
  @override
  final BuiltList<JsonObject>? gradingScheme;

  factory _$Course([void Function(CourseBuilder)? updates]) =>
      (new CourseBuilder()..update(updates))._build();

  _$Course._(
      {this.currentScore,
      this.finalScore,
      this.currentGrade,
      this.finalGrade,
      required this.id,
      required this.name,
      this.originalName,
      this.courseCode,
      this.startAt,
      this.endAt,
      this.syllabusBody,
      required this.hideFinalGrades,
      required this.isPublic,
      this.enrollments,
      required this.needsGradingCount,
      required this.applyAssignmentGroupWeights,
      required this.isFavorite,
      required this.accessRestrictedByDate,
      this.imageDownloadUrl,
      required this.hasWeightedGradingPeriods,
      required this.hasGradingPeriods,
      required this.restrictEnrollmentsToCourseDates,
      this.workflowState,
      this.homePage,
      this.term,
      this.sections,
      this.settings,
      this.gradingScheme})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Course', 'id');
    BuiltValueNullFieldError.checkNotNull(name, r'Course', 'name');
    BuiltValueNullFieldError.checkNotNull(
        hideFinalGrades, r'Course', 'hideFinalGrades');
    BuiltValueNullFieldError.checkNotNull(isPublic, r'Course', 'isPublic');
    BuiltValueNullFieldError.checkNotNull(
        needsGradingCount, r'Course', 'needsGradingCount');
    BuiltValueNullFieldError.checkNotNull(
        applyAssignmentGroupWeights, r'Course', 'applyAssignmentGroupWeights');
    BuiltValueNullFieldError.checkNotNull(isFavorite, r'Course', 'isFavorite');
    BuiltValueNullFieldError.checkNotNull(
        accessRestrictedByDate, r'Course', 'accessRestrictedByDate');
    BuiltValueNullFieldError.checkNotNull(
        hasWeightedGradingPeriods, r'Course', 'hasWeightedGradingPeriods');
    BuiltValueNullFieldError.checkNotNull(
        hasGradingPeriods, r'Course', 'hasGradingPeriods');
    BuiltValueNullFieldError.checkNotNull(restrictEnrollmentsToCourseDates,
        r'Course', 'restrictEnrollmentsToCourseDates');
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
        homePage == other.homePage &&
        term == other.term &&
        sections == other.sections &&
        settings == other.settings &&
        gradingScheme == other.gradingScheme;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, currentScore.hashCode);
    _$hash = $jc(_$hash, finalScore.hashCode);
    _$hash = $jc(_$hash, currentGrade.hashCode);
    _$hash = $jc(_$hash, finalGrade.hashCode);
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, originalName.hashCode);
    _$hash = $jc(_$hash, courseCode.hashCode);
    _$hash = $jc(_$hash, startAt.hashCode);
    _$hash = $jc(_$hash, endAt.hashCode);
    _$hash = $jc(_$hash, syllabusBody.hashCode);
    _$hash = $jc(_$hash, hideFinalGrades.hashCode);
    _$hash = $jc(_$hash, isPublic.hashCode);
    _$hash = $jc(_$hash, enrollments.hashCode);
    _$hash = $jc(_$hash, needsGradingCount.hashCode);
    _$hash = $jc(_$hash, applyAssignmentGroupWeights.hashCode);
    _$hash = $jc(_$hash, isFavorite.hashCode);
    _$hash = $jc(_$hash, accessRestrictedByDate.hashCode);
    _$hash = $jc(_$hash, imageDownloadUrl.hashCode);
    _$hash = $jc(_$hash, hasWeightedGradingPeriods.hashCode);
    _$hash = $jc(_$hash, hasGradingPeriods.hashCode);
    _$hash = $jc(_$hash, restrictEnrollmentsToCourseDates.hashCode);
    _$hash = $jc(_$hash, workflowState.hashCode);
    _$hash = $jc(_$hash, homePage.hashCode);
    _$hash = $jc(_$hash, term.hashCode);
    _$hash = $jc(_$hash, sections.hashCode);
    _$hash = $jc(_$hash, settings.hashCode);
    _$hash = $jc(_$hash, gradingScheme.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Course')
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
          ..add('homePage', homePage)
          ..add('term', term)
          ..add('sections', sections)
          ..add('settings', settings)
          ..add('gradingScheme', gradingScheme))
        .toString();
  }
}

class CourseBuilder implements Builder<Course, CourseBuilder> {
  _$Course? _$v;

  double? _currentScore;
  double? get currentScore => _$this._currentScore;
  set currentScore(double? currentScore) => _$this._currentScore = currentScore;

  double? _finalScore;
  double? get finalScore => _$this._finalScore;
  set finalScore(double? finalScore) => _$this._finalScore = finalScore;

  String? _currentGrade;
  String? get currentGrade => _$this._currentGrade;
  set currentGrade(String? currentGrade) => _$this._currentGrade = currentGrade;

  String? _finalGrade;
  String? get finalGrade => _$this._finalGrade;
  set finalGrade(String? finalGrade) => _$this._finalGrade = finalGrade;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _originalName;
  String? get originalName => _$this._originalName;
  set originalName(String? originalName) => _$this._originalName = originalName;

  String? _courseCode;
  String? get courseCode => _$this._courseCode;
  set courseCode(String? courseCode) => _$this._courseCode = courseCode;

  DateTime? _startAt;
  DateTime? get startAt => _$this._startAt;
  set startAt(DateTime? startAt) => _$this._startAt = startAt;

  DateTime? _endAt;
  DateTime? get endAt => _$this._endAt;
  set endAt(DateTime? endAt) => _$this._endAt = endAt;

  String? _syllabusBody;
  String? get syllabusBody => _$this._syllabusBody;
  set syllabusBody(String? syllabusBody) => _$this._syllabusBody = syllabusBody;

  bool? _hideFinalGrades;
  bool? get hideFinalGrades => _$this._hideFinalGrades;
  set hideFinalGrades(bool? hideFinalGrades) =>
      _$this._hideFinalGrades = hideFinalGrades;

  bool? _isPublic;
  bool? get isPublic => _$this._isPublic;
  set isPublic(bool? isPublic) => _$this._isPublic = isPublic;

  ListBuilder<Enrollment>? _enrollments;
  ListBuilder<Enrollment> get enrollments =>
      _$this._enrollments ??= new ListBuilder<Enrollment>();
  set enrollments(ListBuilder<Enrollment>? enrollments) =>
      _$this._enrollments = enrollments;

  int? _needsGradingCount;
  int? get needsGradingCount => _$this._needsGradingCount;
  set needsGradingCount(int? needsGradingCount) =>
      _$this._needsGradingCount = needsGradingCount;

  bool? _applyAssignmentGroupWeights;
  bool? get applyAssignmentGroupWeights => _$this._applyAssignmentGroupWeights;
  set applyAssignmentGroupWeights(bool? applyAssignmentGroupWeights) =>
      _$this._applyAssignmentGroupWeights = applyAssignmentGroupWeights;

  bool? _isFavorite;
  bool? get isFavorite => _$this._isFavorite;
  set isFavorite(bool? isFavorite) => _$this._isFavorite = isFavorite;

  bool? _accessRestrictedByDate;
  bool? get accessRestrictedByDate => _$this._accessRestrictedByDate;
  set accessRestrictedByDate(bool? accessRestrictedByDate) =>
      _$this._accessRestrictedByDate = accessRestrictedByDate;

  String? _imageDownloadUrl;
  String? get imageDownloadUrl => _$this._imageDownloadUrl;
  set imageDownloadUrl(String? imageDownloadUrl) =>
      _$this._imageDownloadUrl = imageDownloadUrl;

  bool? _hasWeightedGradingPeriods;
  bool? get hasWeightedGradingPeriods => _$this._hasWeightedGradingPeriods;
  set hasWeightedGradingPeriods(bool? hasWeightedGradingPeriods) =>
      _$this._hasWeightedGradingPeriods = hasWeightedGradingPeriods;

  bool? _hasGradingPeriods;
  bool? get hasGradingPeriods => _$this._hasGradingPeriods;
  set hasGradingPeriods(bool? hasGradingPeriods) =>
      _$this._hasGradingPeriods = hasGradingPeriods;

  bool? _restrictEnrollmentsToCourseDates;
  bool? get restrictEnrollmentsToCourseDates =>
      _$this._restrictEnrollmentsToCourseDates;
  set restrictEnrollmentsToCourseDates(
          bool? restrictEnrollmentsToCourseDates) =>
      _$this._restrictEnrollmentsToCourseDates =
          restrictEnrollmentsToCourseDates;

  String? _workflowState;
  String? get workflowState => _$this._workflowState;
  set workflowState(String? workflowState) =>
      _$this._workflowState = workflowState;

  HomePage? _homePage;
  HomePage? get homePage => _$this._homePage;
  set homePage(HomePage? homePage) => _$this._homePage = homePage;

  TermBuilder? _term;
  TermBuilder get term => _$this._term ??= new TermBuilder();
  set term(TermBuilder? term) => _$this._term = term;

  ListBuilder<Section>? _sections;
  ListBuilder<Section> get sections =>
      _$this._sections ??= new ListBuilder<Section>();
  set sections(ListBuilder<Section>? sections) => _$this._sections = sections;

  CourseSettingsBuilder? _settings;
  CourseSettingsBuilder get settings =>
      _$this._settings ??= new CourseSettingsBuilder();
  set settings(CourseSettingsBuilder? settings) => _$this._settings = settings;

  ListBuilder<JsonObject>? _gradingScheme;
  ListBuilder<JsonObject> get gradingScheme =>
      _$this._gradingScheme ??= new ListBuilder<JsonObject>();
  set gradingScheme(ListBuilder<JsonObject>? gradingScheme) =>
      _$this._gradingScheme = gradingScheme;

  CourseBuilder() {
    Course._initializeBuilder(this);
  }

  CourseBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _currentScore = $v.currentScore;
      _finalScore = $v.finalScore;
      _currentGrade = $v.currentGrade;
      _finalGrade = $v.finalGrade;
      _id = $v.id;
      _name = $v.name;
      _originalName = $v.originalName;
      _courseCode = $v.courseCode;
      _startAt = $v.startAt;
      _endAt = $v.endAt;
      _syllabusBody = $v.syllabusBody;
      _hideFinalGrades = $v.hideFinalGrades;
      _isPublic = $v.isPublic;
      _enrollments = $v.enrollments?.toBuilder();
      _needsGradingCount = $v.needsGradingCount;
      _applyAssignmentGroupWeights = $v.applyAssignmentGroupWeights;
      _isFavorite = $v.isFavorite;
      _accessRestrictedByDate = $v.accessRestrictedByDate;
      _imageDownloadUrl = $v.imageDownloadUrl;
      _hasWeightedGradingPeriods = $v.hasWeightedGradingPeriods;
      _hasGradingPeriods = $v.hasGradingPeriods;
      _restrictEnrollmentsToCourseDates = $v.restrictEnrollmentsToCourseDates;
      _workflowState = $v.workflowState;
      _homePage = $v.homePage;
      _term = $v.term?.toBuilder();
      _sections = $v.sections?.toBuilder();
      _settings = $v.settings?.toBuilder();
      _gradingScheme = $v.gradingScheme?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Course other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Course;
  }

  @override
  void update(void Function(CourseBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Course build() => _build();

  _$Course _build() {
    _$Course _$result;
    try {
      _$result = _$v ??
          new _$Course._(
              currentScore: currentScore,
              finalScore: finalScore,
              currentGrade: currentGrade,
              finalGrade: finalGrade,
              id: BuiltValueNullFieldError.checkNotNull(id, r'Course', 'id'),
              name: BuiltValueNullFieldError.checkNotNull(
                  name, r'Course', 'name'),
              originalName: originalName,
              courseCode: courseCode,
              startAt: startAt,
              endAt: endAt,
              syllabusBody: syllabusBody,
              hideFinalGrades: BuiltValueNullFieldError.checkNotNull(
                  hideFinalGrades, r'Course', 'hideFinalGrades'),
              isPublic: BuiltValueNullFieldError.checkNotNull(
                  isPublic, r'Course', 'isPublic'),
              enrollments: _enrollments?.build(),
              needsGradingCount: BuiltValueNullFieldError.checkNotNull(
                  needsGradingCount, r'Course', 'needsGradingCount'),
              applyAssignmentGroupWeights: BuiltValueNullFieldError.checkNotNull(
                  applyAssignmentGroupWeights, r'Course', 'applyAssignmentGroupWeights'),
              isFavorite: BuiltValueNullFieldError.checkNotNull(
                  isFavorite, r'Course', 'isFavorite'),
              accessRestrictedByDate: BuiltValueNullFieldError.checkNotNull(
                  accessRestrictedByDate, r'Course', 'accessRestrictedByDate'),
              imageDownloadUrl: imageDownloadUrl,
              hasWeightedGradingPeriods: BuiltValueNullFieldError.checkNotNull(
                  hasWeightedGradingPeriods, r'Course', 'hasWeightedGradingPeriods'),
              hasGradingPeriods:
                  BuiltValueNullFieldError.checkNotNull(hasGradingPeriods, r'Course', 'hasGradingPeriods'),
              restrictEnrollmentsToCourseDates: BuiltValueNullFieldError.checkNotNull(restrictEnrollmentsToCourseDates, r'Course', 'restrictEnrollmentsToCourseDates'),
              workflowState: workflowState,
              homePage: homePage,
              term: _term?.build(),
              sections: _sections?.build(),
              settings: _settings?.build(),
              gradingScheme: _gradingScheme?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'enrollments';
        _enrollments?.build();

        _$failedField = 'term';
        _term?.build();
        _$failedField = 'sections';
        _sections?.build();
        _$failedField = 'settings';
        _settings?.build();
        _$failedField = 'gradingScheme';
        _gradingScheme?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'Course', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
