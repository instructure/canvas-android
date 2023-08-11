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
  Iterable<Object?> serialize(
      Serializers serializers, GradeSubmissionInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'posted_grade',
      serializers.serialize(object.postedGrade,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.excuse;

    result
      ..add('excuse')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));

    return result;
  }

  @override
  GradeSubmissionInfo deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradeSubmissionInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'posted_grade':
          result.postedGrade = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'excuse':
          result.excuse = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
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
  final bool? excuse;

  factory _$GradeSubmissionInfo(
          [void Function(GradeSubmissionInfoBuilder)? updates]) =>
      (new GradeSubmissionInfoBuilder()..update(updates))._build();

  _$GradeSubmissionInfo._({required this.postedGrade, this.excuse})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        postedGrade, r'GradeSubmissionInfo', 'postedGrade');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, postedGrade.hashCode);
    _$hash = $jc(_$hash, excuse.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'GradeSubmissionInfo')
          ..add('postedGrade', postedGrade)
          ..add('excuse', excuse))
        .toString();
  }
}

class GradeSubmissionInfoBuilder
    implements Builder<GradeSubmissionInfo, GradeSubmissionInfoBuilder> {
  _$GradeSubmissionInfo? _$v;

  String? _postedGrade;
  String? get postedGrade => _$this._postedGrade;
  set postedGrade(String? postedGrade) => _$this._postedGrade = postedGrade;

  bool? _excuse;
  bool? get excuse => _$this._excuse;
  set excuse(bool? excuse) => _$this._excuse = excuse;

  GradeSubmissionInfoBuilder() {
    GradeSubmissionInfo._initializeBuilder(this);
  }

  GradeSubmissionInfoBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _postedGrade = $v.postedGrade;
      _excuse = $v.excuse;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradeSubmissionInfo other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$GradeSubmissionInfo;
  }

  @override
  void update(void Function(GradeSubmissionInfoBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  GradeSubmissionInfo build() => _build();

  _$GradeSubmissionInfo _build() {
    final _$result = _$v ??
        new _$GradeSubmissionInfo._(
            postedGrade: BuiltValueNullFieldError.checkNotNull(
                postedGrade, r'GradeSubmissionInfo', 'postedGrade'),
            excuse: excuse);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
