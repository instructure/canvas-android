// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'planner_submission.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<PlannerSubmission> _$plannerSubmissionSerializer =
    new _$PlannerSubmissionSerializer();

class _$PlannerSubmissionSerializer
    implements StructuredSerializer<PlannerSubmission> {
  @override
  final Iterable<Type> types = const [PlannerSubmission, _$PlannerSubmission];
  @override
  final String wireName = 'PlannerSubmission';

  @override
  Iterable<Object?> serialize(Serializers serializers, PlannerSubmission object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'submitted',
      serializers.serialize(object.submitted,
          specifiedType: const FullType(bool)),
      'excused',
      serializers.serialize(object.excused,
          specifiedType: const FullType(bool)),
      'graded',
      serializers.serialize(object.graded, specifiedType: const FullType(bool)),
      'late',
      serializers.serialize(object.late, specifiedType: const FullType(bool)),
      'missing',
      serializers.serialize(object.missing,
          specifiedType: const FullType(bool)),
      'needs_grading',
      serializers.serialize(object.needsGrading,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  PlannerSubmission deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PlannerSubmissionBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'submitted':
          result.submitted = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'excused':
          result.excused = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'graded':
          result.graded = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'late':
          result.late = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'missing':
          result.missing = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'needs_grading':
          result.needsGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$PlannerSubmission extends PlannerSubmission {
  @override
  final bool submitted;
  @override
  final bool excused;
  @override
  final bool graded;
  @override
  final bool late;
  @override
  final bool missing;
  @override
  final bool needsGrading;

  factory _$PlannerSubmission(
          [void Function(PlannerSubmissionBuilder)? updates]) =>
      (new PlannerSubmissionBuilder()..update(updates))._build();

  _$PlannerSubmission._(
      {required this.submitted,
      required this.excused,
      required this.graded,
      required this.late,
      required this.missing,
      required this.needsGrading})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        submitted, r'PlannerSubmission', 'submitted');
    BuiltValueNullFieldError.checkNotNull(
        excused, r'PlannerSubmission', 'excused');
    BuiltValueNullFieldError.checkNotNull(
        graded, r'PlannerSubmission', 'graded');
    BuiltValueNullFieldError.checkNotNull(late, r'PlannerSubmission', 'late');
    BuiltValueNullFieldError.checkNotNull(
        missing, r'PlannerSubmission', 'missing');
    BuiltValueNullFieldError.checkNotNull(
        needsGrading, r'PlannerSubmission', 'needsGrading');
  }

  @override
  PlannerSubmission rebuild(void Function(PlannerSubmissionBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PlannerSubmissionBuilder toBuilder() =>
      new PlannerSubmissionBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PlannerSubmission &&
        submitted == other.submitted &&
        excused == other.excused &&
        graded == other.graded &&
        late == other.late &&
        missing == other.missing &&
        needsGrading == other.needsGrading;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, submitted.hashCode);
    _$hash = $jc(_$hash, excused.hashCode);
    _$hash = $jc(_$hash, graded.hashCode);
    _$hash = $jc(_$hash, late.hashCode);
    _$hash = $jc(_$hash, missing.hashCode);
    _$hash = $jc(_$hash, needsGrading.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PlannerSubmission')
          ..add('submitted', submitted)
          ..add('excused', excused)
          ..add('graded', graded)
          ..add('late', late)
          ..add('missing', missing)
          ..add('needsGrading', needsGrading))
        .toString();
  }
}

class PlannerSubmissionBuilder
    implements Builder<PlannerSubmission, PlannerSubmissionBuilder> {
  _$PlannerSubmission? _$v;

  bool? _submitted;
  bool? get submitted => _$this._submitted;
  set submitted(bool? submitted) => _$this._submitted = submitted;

  bool? _excused;
  bool? get excused => _$this._excused;
  set excused(bool? excused) => _$this._excused = excused;

  bool? _graded;
  bool? get graded => _$this._graded;
  set graded(bool? graded) => _$this._graded = graded;

  bool? _late;
  bool? get late => _$this._late;
  set late(bool? late) => _$this._late = late;

  bool? _missing;
  bool? get missing => _$this._missing;
  set missing(bool? missing) => _$this._missing = missing;

  bool? _needsGrading;
  bool? get needsGrading => _$this._needsGrading;
  set needsGrading(bool? needsGrading) => _$this._needsGrading = needsGrading;

  PlannerSubmissionBuilder() {
    PlannerSubmission._initializeBuilder(this);
  }

  PlannerSubmissionBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _submitted = $v.submitted;
      _excused = $v.excused;
      _graded = $v.graded;
      _late = $v.late;
      _missing = $v.missing;
      _needsGrading = $v.needsGrading;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PlannerSubmission other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$PlannerSubmission;
  }

  @override
  void update(void Function(PlannerSubmissionBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PlannerSubmission build() => _build();

  _$PlannerSubmission _build() {
    final _$result = _$v ??
        new _$PlannerSubmission._(
            submitted: BuiltValueNullFieldError.checkNotNull(
                submitted, r'PlannerSubmission', 'submitted'),
            excused: BuiltValueNullFieldError.checkNotNull(
                excused, r'PlannerSubmission', 'excused'),
            graded: BuiltValueNullFieldError.checkNotNull(
                graded, r'PlannerSubmission', 'graded'),
            late: BuiltValueNullFieldError.checkNotNull(
                late, r'PlannerSubmission', 'late'),
            missing: BuiltValueNullFieldError.checkNotNull(
                missing, r'PlannerSubmission', 'missing'),
            needsGrading: BuiltValueNullFieldError.checkNotNull(
                needsGrading, r'PlannerSubmission', 'needsGrading'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
