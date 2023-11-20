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
  Iterable<Object?> serialize(Serializers serializers, Quiz object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
  Quiz deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new QuizBuilder();

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
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'description':
          result.description = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'due_at':
          result.dueAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime))! as DateTime;
          break;
        case 'points_possible':
          result.pointsPossible = serializers.deserialize(value,
              specifiedType: const FullType(double))! as double;
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

  factory _$Quiz([void Function(QuizBuilder)? updates]) =>
      (new QuizBuilder()..update(updates))._build();

  _$Quiz._(
      {required this.id,
      required this.title,
      required this.description,
      required this.dueAt,
      required this.pointsPossible})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Quiz', 'id');
    BuiltValueNullFieldError.checkNotNull(title, r'Quiz', 'title');
    BuiltValueNullFieldError.checkNotNull(description, r'Quiz', 'description');
    BuiltValueNullFieldError.checkNotNull(dueAt, r'Quiz', 'dueAt');
    BuiltValueNullFieldError.checkNotNull(
        pointsPossible, r'Quiz', 'pointsPossible');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, title.hashCode);
    _$hash = $jc(_$hash, description.hashCode);
    _$hash = $jc(_$hash, dueAt.hashCode);
    _$hash = $jc(_$hash, pointsPossible.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Quiz')
          ..add('id', id)
          ..add('title', title)
          ..add('description', description)
          ..add('dueAt', dueAt)
          ..add('pointsPossible', pointsPossible))
        .toString();
  }
}

class QuizBuilder implements Builder<Quiz, QuizBuilder> {
  _$Quiz? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _title;
  String? get title => _$this._title;
  set title(String? title) => _$this._title = title;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  DateTime? _dueAt;
  DateTime? get dueAt => _$this._dueAt;
  set dueAt(DateTime? dueAt) => _$this._dueAt = dueAt;

  double? _pointsPossible;
  double? get pointsPossible => _$this._pointsPossible;
  set pointsPossible(double? pointsPossible) =>
      _$this._pointsPossible = pointsPossible;

  QuizBuilder() {
    Quiz._initializeBuilder(this);
  }

  QuizBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _title = $v.title;
      _description = $v.description;
      _dueAt = $v.dueAt;
      _pointsPossible = $v.pointsPossible;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Quiz other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Quiz;
  }

  @override
  void update(void Function(QuizBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Quiz build() => _build();

  _$Quiz _build() {
    final _$result = _$v ??
        new _$Quiz._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'Quiz', 'id'),
            title:
                BuiltValueNullFieldError.checkNotNull(title, r'Quiz', 'title'),
            description: BuiltValueNullFieldError.checkNotNull(
                description, r'Quiz', 'description'),
            dueAt:
                BuiltValueNullFieldError.checkNotNull(dueAt, r'Quiz', 'dueAt'),
            pointsPossible: BuiltValueNullFieldError.checkNotNull(
                pointsPossible, r'Quiz', 'pointsPossible'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
