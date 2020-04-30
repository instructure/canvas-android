// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'quiz.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Quiz> _$quizSerializer = new _$QuizSerializer();

class _$QuizSerializer implements StructuredSerializer<Quiz> {
  @override
  final Iterable<Type> types = const [Quiz, _$Quiz];
  @override
  final String wireName = 'Quiz';

  @override
  Iterable<Object> serialize(Serializers serializers, Quiz object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'title',
      serializers.serialize(object.title,
          specifiedType: const FullType(String)),
      'description',
      serializers.serialize(object.description,
          specifiedType: const FullType(String)),
      'due_at',
      serializers.serialize(object.dueAt,
          specifiedType: const FullType(DateTime)),
      'points_possible',
      serializers.serialize(object.pointsPossible,
          specifiedType: const FullType(double)),
    ];

    return result;
  }

  @override
  Quiz deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new QuizBuilder();

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
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'description':
          result.description = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'due_at':
          result.dueAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'points_possible':
          result.pointsPossible = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
      }
    }

    return result.build();
  }
}

class _$Quiz extends Quiz {
  @override
  final String id;
  @override
  final String title;
  @override
  final String description;
  @override
  final DateTime dueAt;
  @override
  final double pointsPossible;

  factory _$Quiz([void Function(QuizBuilder) updates]) =>
      (new QuizBuilder()..update(updates)).build();

  _$Quiz._(
      {this.id, this.title, this.description, this.dueAt, this.pointsPossible})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Quiz', 'id');
    }
    if (title == null) {
      throw new BuiltValueNullFieldError('Quiz', 'title');
    }
    if (description == null) {
      throw new BuiltValueNullFieldError('Quiz', 'description');
    }
    if (dueAt == null) {
      throw new BuiltValueNullFieldError('Quiz', 'dueAt');
    }
    if (pointsPossible == null) {
      throw new BuiltValueNullFieldError('Quiz', 'pointsPossible');
    }
  }

  @override
  Quiz rebuild(void Function(QuizBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  QuizBuilder toBuilder() => new QuizBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Quiz &&
        id == other.id &&
        title == other.title &&
        description == other.description &&
        dueAt == other.dueAt &&
        pointsPossible == other.pointsPossible;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc($jc(0, id.hashCode), title.hashCode), description.hashCode),
            dueAt.hashCode),
        pointsPossible.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Quiz')
          ..add('id', id)
          ..add('title', title)
          ..add('description', description)
          ..add('dueAt', dueAt)
          ..add('pointsPossible', pointsPossible))
        .toString();
  }
}

class QuizBuilder implements Builder<Quiz, QuizBuilder> {
  _$Quiz _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _title;
  String get title => _$this._title;
  set title(String title) => _$this._title = title;

  String _description;
  String get description => _$this._description;
  set description(String description) => _$this._description = description;

  DateTime _dueAt;
  DateTime get dueAt => _$this._dueAt;
  set dueAt(DateTime dueAt) => _$this._dueAt = dueAt;

  double _pointsPossible;
  double get pointsPossible => _$this._pointsPossible;
  set pointsPossible(double pointsPossible) =>
      _$this._pointsPossible = pointsPossible;

  QuizBuilder() {
    Quiz._initializeBuilder(this);
  }

  QuizBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _title = _$v.title;
      _description = _$v.description;
      _dueAt = _$v.dueAt;
      _pointsPossible = _$v.pointsPossible;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Quiz other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Quiz;
  }

  @override
  void update(void Function(QuizBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Quiz build() {
    final _$result = _$v ??
        new _$Quiz._(
            id: id,
            title: title,
            description: description,
            dueAt: dueAt,
            pointsPossible: pointsPossible);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
