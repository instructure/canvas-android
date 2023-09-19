// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'term.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Term> _$termSerializer = new _$TermSerializer();

class _$TermSerializer implements StructuredSerializer<Term> {
  @override
  final Iterable<Type> types = const [Term, _$Term];
  @override
  final String wireName = 'Term';

  @override
  Iterable<Object?> serialize(Serializers serializers, Term object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.name;

    result
      ..add('name')
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

    return result;
  }

  @override
  Term deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new TermBuilder();

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
      }
    }

    return result.build();
  }
}

class _$Term extends Term {
  @override
  final String id;
  @override
  final String? name;
  @override
  final DateTime? startAt;
  @override
  final DateTime? endAt;

  factory _$Term([void Function(TermBuilder)? updates]) =>
      (new TermBuilder()..update(updates))._build();

  _$Term._({required this.id, this.name, this.startAt, this.endAt})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Term', 'id');
  }

  @override
  Term rebuild(void Function(TermBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  TermBuilder toBuilder() => new TermBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Term &&
        id == other.id &&
        name == other.name &&
        startAt == other.startAt &&
        endAt == other.endAt;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, startAt.hashCode);
    _$hash = $jc(_$hash, endAt.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Term')
          ..add('id', id)
          ..add('name', name)
          ..add('startAt', startAt)
          ..add('endAt', endAt))
        .toString();
  }
}

class TermBuilder implements Builder<Term, TermBuilder> {
  _$Term? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  DateTime? _startAt;
  DateTime? get startAt => _$this._startAt;
  set startAt(DateTime? startAt) => _$this._startAt = startAt;

  DateTime? _endAt;
  DateTime? get endAt => _$this._endAt;
  set endAt(DateTime? endAt) => _$this._endAt = endAt;

  TermBuilder() {
    Term._initializeBuilder(this);
  }

  TermBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _name = $v.name;
      _startAt = $v.startAt;
      _endAt = $v.endAt;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Term other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Term;
  }

  @override
  void update(void Function(TermBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Term build() => _build();

  _$Term _build() {
    final _$result = _$v ??
        new _$Term._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'Term', 'id'),
            name: name,
            startAt: startAt,
            endAt: endAt);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
