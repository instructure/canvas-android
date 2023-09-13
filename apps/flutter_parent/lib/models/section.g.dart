// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'section.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Section> _$sectionSerializer = new _$SectionSerializer();

class _$SectionSerializer implements StructuredSerializer<Section> {
  @override
  final Iterable<Type> types = const [Section, _$Section];
  @override
  final String wireName = 'Section';

  @override
  Iterable<Object?> serialize(Serializers serializers, Section object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
    ];
    Object? value;
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
  Section deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SectionBuilder();

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

class _$Section extends Section {
  @override
  final String id;
  @override
  final String name;
  @override
  final DateTime? startAt;
  @override
  final DateTime? endAt;

  factory _$Section([void Function(SectionBuilder)? updates]) =>
      (new SectionBuilder()..update(updates))._build();

  _$Section._({required this.id, required this.name, this.startAt, this.endAt})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Section', 'id');
    BuiltValueNullFieldError.checkNotNull(name, r'Section', 'name');
  }

  @override
  Section rebuild(void Function(SectionBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SectionBuilder toBuilder() => new SectionBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Section &&
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
    return (newBuiltValueToStringHelper(r'Section')
          ..add('id', id)
          ..add('name', name)
          ..add('startAt', startAt)
          ..add('endAt', endAt))
        .toString();
  }
}

class SectionBuilder implements Builder<Section, SectionBuilder> {
  _$Section? _$v;

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

  SectionBuilder() {
    Section._initializeBuilder(this);
  }

  SectionBuilder get _$this {
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
  void replace(Section other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Section;
  }

  @override
  void update(void Function(SectionBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Section build() => _build();

  _$Section _build() {
    final _$result = _$v ??
        new _$Section._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'Section', 'id'),
            name:
                BuiltValueNullFieldError.checkNotNull(name, r'Section', 'name'),
            startAt: startAt,
            endAt: endAt);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
