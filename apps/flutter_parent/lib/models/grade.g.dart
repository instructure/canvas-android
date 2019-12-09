// GENERATED CODE - DO NOT MODIFY BY HAND

part of grade;

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
  Iterable<Object> serialize(Serializers serializers, Grade object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'html_url',
      serializers.serialize(object.htmlUrl,
          specifiedType: const FullType(String)),
    ];
    result.add('current_score');
    if (object.currentScore == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.currentScore,
          specifiedType: const FullType(double)));
    }
    result.add('final_score');
    if (object.finalScore == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.finalScore,
          specifiedType: const FullType(double)));
    }
    result.add('current_grade');
    if (object.currentGrade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.currentGrade,
          specifiedType: const FullType(String)));
    }
    result.add('final_grade');
    if (object.finalGrade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.finalGrade,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  Grade deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradeBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'current_score':
          result.currentScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'final_score':
          result.finalScore = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'current_grade':
          result.currentGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'final_grade':
          result.finalGrade = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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
  final double currentScore;
  @override
  final double finalScore;
  @override
  final String currentGrade;
  @override
  final String finalGrade;

  factory _$Grade([void Function(GradeBuilder) updates]) =>
      (new GradeBuilder()..update(updates)).build();

  _$Grade._(
      {this.htmlUrl,
      this.currentScore,
      this.finalScore,
      this.currentGrade,
      this.finalGrade})
      : super._() {
    if (htmlUrl == null) {
      throw new BuiltValueNullFieldError('Grade', 'htmlUrl');
    }
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
    return $jf($jc(
        $jc(
            $jc($jc($jc(0, htmlUrl.hashCode), currentScore.hashCode),
                finalScore.hashCode),
            currentGrade.hashCode),
        finalGrade.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Grade')
          ..add('htmlUrl', htmlUrl)
          ..add('currentScore', currentScore)
          ..add('finalScore', finalScore)
          ..add('currentGrade', currentGrade)
          ..add('finalGrade', finalGrade))
        .toString();
  }
}

class GradeBuilder implements Builder<Grade, GradeBuilder> {
  _$Grade _$v;

  String _htmlUrl;
  String get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String htmlUrl) => _$this._htmlUrl = htmlUrl;

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

  GradeBuilder();

  GradeBuilder get _$this {
    if (_$v != null) {
      _htmlUrl = _$v.htmlUrl;
      _currentScore = _$v.currentScore;
      _finalScore = _$v.finalScore;
      _currentGrade = _$v.currentGrade;
      _finalGrade = _$v.finalGrade;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Grade other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Grade;
  }

  @override
  void update(void Function(GradeBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Grade build() {
    final _$result = _$v ??
        new _$Grade._(
            htmlUrl: htmlUrl,
            currentScore: currentScore,
            finalScore: finalScore,
            currentGrade: currentGrade,
            finalGrade: finalGrade);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
