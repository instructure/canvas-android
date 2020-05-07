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
  Iterable<Object> serialize(Serializers serializers, PlannerSubmission object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PlannerSubmissionBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'submitted':
          result.submitted = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'excused':
          result.excused = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'graded':
          result.graded = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'late':
          result.late = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'missing':
          result.missing = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'needs_grading':
          result.needsGrading = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
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
          [void Function(PlannerSubmissionBuilder) updates]) =>
      (new PlannerSubmissionBuilder()..update(updates)).build();

  _$PlannerSubmission._(
      {this.submitted,
      this.excused,
      this.graded,
      this.late,
      this.missing,
      this.needsGrading})
      : super._() {
    if (submitted == null) {
      throw new BuiltValueNullFieldError('PlannerSubmission', 'submitted');
    }
    if (excused == null) {
      throw new BuiltValueNullFieldError('PlannerSubmission', 'excused');
    }
    if (graded == null) {
      throw new BuiltValueNullFieldError('PlannerSubmission', 'graded');
    }
    if (late == null) {
      throw new BuiltValueNullFieldError('PlannerSubmission', 'late');
    }
    if (missing == null) {
      throw new BuiltValueNullFieldError('PlannerSubmission', 'missing');
    }
    if (needsGrading == null) {
      throw new BuiltValueNullFieldError('PlannerSubmission', 'needsGrading');
    }
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
    return $jf($jc(
        $jc(
            $jc(
                $jc($jc($jc(0, submitted.hashCode), excused.hashCode),
                    graded.hashCode),
                late.hashCode),
            missing.hashCode),
        needsGrading.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('PlannerSubmission')
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
  _$PlannerSubmission _$v;

  bool _submitted;
  bool get submitted => _$this._submitted;
  set submitted(bool submitted) => _$this._submitted = submitted;

  bool _excused;
  bool get excused => _$this._excused;
  set excused(bool excused) => _$this._excused = excused;

  bool _graded;
  bool get graded => _$this._graded;
  set graded(bool graded) => _$this._graded = graded;

  bool _late;
  bool get late => _$this._late;
  set late(bool late) => _$this._late = late;

  bool _missing;
  bool get missing => _$this._missing;
  set missing(bool missing) => _$this._missing = missing;

  bool _needsGrading;
  bool get needsGrading => _$this._needsGrading;
  set needsGrading(bool needsGrading) => _$this._needsGrading = needsGrading;

  PlannerSubmissionBuilder() {
    PlannerSubmission._initializeBuilder(this);
  }

  PlannerSubmissionBuilder get _$this {
    if (_$v != null) {
      _submitted = _$v.submitted;
      _excused = _$v.excused;
      _graded = _$v.graded;
      _late = _$v.late;
      _missing = _$v.missing;
      _needsGrading = _$v.needsGrading;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PlannerSubmission other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$PlannerSubmission;
  }

  @override
  void update(void Function(PlannerSubmissionBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$PlannerSubmission build() {
    final _$result = _$v ??
        new _$PlannerSubmission._(
            submitted: submitted,
            excused: excused,
            graded: graded,
            late: late,
            missing: missing,
            needsGrading: needsGrading);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
