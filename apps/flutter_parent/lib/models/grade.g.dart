// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'grade.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Grade> _$gradeSerializer = new _$GradeSerializer();

class _$GradeSerializer implements StructuredSerializer<Grade> {
  @override
  final Iterable<Type> types = const [Grade, _$Grade];
  @override
  final String wireName = 'Grade';

  @override
  Iterable<Object?> serialize(Serializers serializers, Grade object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'html_url',
      serializers.serialize(object.htmlUrl,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.currentScore;

    result
      ..add('current_score')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));
    value = object.finalScore;

    result
      ..add('final_score')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));
    value = object.currentGrade;

    result
      ..add('current_grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.finalGrade;

    result
      ..add('final_grade')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  Grade deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradeBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'current_score':
          result.currentScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double?;
          break;
        case 'final_score':
          result.finalScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double?;
          break;
        case 'current_grade':
          result.currentGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'final_grade':
          result.finalGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$Grade extends Grade {
  @override
  final String htmlUrl;
  @override
  final double? currentScore;
  @override
  final double? finalScore;
  @override
  final String? currentGrade;
  @override
  final String? finalGrade;

  factory _$Grade([void Function(GradeBuilder)? updates]) =>
      (new GradeBuilder()..update(updates))._build();

  _$Grade._(
      {required this.htmlUrl,
      this.currentScore,
      this.finalScore,
      this.currentGrade,
      this.finalGrade})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(htmlUrl, r'Grade', 'htmlUrl');
  }

  @override
  Grade rebuild(void Function(GradeBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  GradeBuilder toBuilder() => new GradeBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Grade &&
        htmlUrl == other.htmlUrl &&
        currentScore == other.currentScore &&
        finalScore == other.finalScore &&
        currentGrade == other.currentGrade &&
        finalGrade == other.finalGrade;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, htmlUrl.hashCode);
    _$hash = $jc(_$hash, currentScore.hashCode);
    _$hash = $jc(_$hash, finalScore.hashCode);
    _$hash = $jc(_$hash, currentGrade.hashCode);
    _$hash = $jc(_$hash, finalGrade.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Grade')
          ..add('htmlUrl', htmlUrl)
          ..add('currentScore', currentScore)
          ..add('finalScore', finalScore)
          ..add('currentGrade', currentGrade)
          ..add('finalGrade', finalGrade))
        .toString();
  }
}

class GradeBuilder implements Builder<Grade, GradeBuilder> {
  _$Grade? _$v;

  String? _htmlUrl;
  String? get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String? htmlUrl) => _$this._htmlUrl = htmlUrl;

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

  GradeBuilder();

  GradeBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _htmlUrl = $v.htmlUrl;
      _currentScore = $v.currentScore;
      _finalScore = $v.finalScore;
      _currentGrade = $v.currentGrade;
      _finalGrade = $v.finalGrade;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Grade other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Grade;
  }

  @override
  void update(void Function(GradeBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Grade build() => _build();

  _$Grade _build() {
    final _$result = _$v ??
        new _$Grade._(
            htmlUrl: BuiltValueNullFieldError.checkNotNull(
                htmlUrl, r'Grade', 'htmlUrl'),
            currentScore: currentScore,
            finalScore: finalScore,
            currentGrade: currentGrade,
            finalGrade: finalGrade);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
