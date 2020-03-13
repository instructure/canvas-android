// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'grade_submission_info.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<GradeSubmissionInfo> _$gradeSubmissionInfoSerializer =
    new _$GradeSubmissionInfoSerializer();

class _$GradeSubmissionInfoSerializer
    implements StructuredSerializer<GradeSubmissionInfo> {
  @override
  final Iterable<Type> types = const [
    GradeSubmissionInfo,
    _$GradeSubmissionInfo
  ];
  @override
  final String wireName = 'GradeSubmissionInfo';

  @override
  Iterable<Object> serialize(
      Serializers serializers, GradeSubmissionInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'posted_grade',
      serializers.serialize(object.postedGrade,
          specifiedType: const FullType(String)),
    ];
    result.add('excuse');
    if (object.excuse == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.excuse,
          specifiedType: const FullType(bool)));
    }
    return result;
  }

  @override
  GradeSubmissionInfo deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradeSubmissionInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'posted_grade':
          result.postedGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'excuse':
          result.excuse = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$GradeSubmissionInfo extends GradeSubmissionInfo {
  @override
  final String postedGrade;
  @override
  final bool excuse;

  factory _$GradeSubmissionInfo(
          [void Function(GradeSubmissionInfoBuilder) updates]) =>
      (new GradeSubmissionInfoBuilder()..update(updates)).build();

  _$GradeSubmissionInfo._({this.postedGrade, this.excuse}) : super._() {
    if (postedGrade == null) {
      throw new BuiltValueNullFieldError('GradeSubmissionInfo', 'postedGrade');
    }
  }

  @override
  GradeSubmissionInfo rebuild(
          void Function(GradeSubmissionInfoBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  GradeSubmissionInfoBuilder toBuilder() =>
      new GradeSubmissionInfoBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is GradeSubmissionInfo &&
        postedGrade == other.postedGrade &&
        excuse == other.excuse;
  }

  @override
  int get hashCode {
    return $jf($jc($jc(0, postedGrade.hashCode), excuse.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('GradeSubmissionInfo')
          ..add('postedGrade', postedGrade)
          ..add('excuse', excuse))
        .toString();
  }
}

class GradeSubmissionInfoBuilder
    implements Builder<GradeSubmissionInfo, GradeSubmissionInfoBuilder> {
  _$GradeSubmissionInfo _$v;

  String _postedGrade;
  String get postedGrade => _$this._postedGrade;
  set postedGrade(String postedGrade) => _$this._postedGrade = postedGrade;

  bool _excuse;
  bool get excuse => _$this._excuse;
  set excuse(bool excuse) => _$this._excuse = excuse;

  GradeSubmissionInfoBuilder() {
    GradeSubmissionInfo._initializeBuilder(this);
  }

  GradeSubmissionInfoBuilder get _$this {
    if (_$v != null) {
      _postedGrade = _$v.postedGrade;
      _excuse = _$v.excuse;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradeSubmissionInfo other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$GradeSubmissionInfo;
  }

  @override
  void update(void Function(GradeSubmissionInfoBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$GradeSubmissionInfo build() {
    final _$result = _$v ??
        new _$GradeSubmissionInfo._(postedGrade: postedGrade, excuse: excuse);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
