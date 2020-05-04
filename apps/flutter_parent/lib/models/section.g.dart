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
  Iterable<Object> serialize(Serializers serializers, Section object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
    ];
    result.add('start_at');
    if (object.startAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.startAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('end_at');
    if (object.endAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.endAt,
          specifiedType: const FullType(DateTime)));
    }
    return result;
  }

  @override
  Section deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SectionBuilder();

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
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'start_at':
          result.startAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'end_at':
          result.endAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
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
  final DateTime startAt;
  @override
  final DateTime endAt;

  factory _$Section([void Function(SectionBuilder) updates]) =>
      (new SectionBuilder()..update(updates)).build();

  _$Section._({this.id, this.name, this.startAt, this.endAt}) : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Section', 'id');
    }
    if (name == null) {
      throw new BuiltValueNullFieldError('Section', 'name');
    }
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
    return $jf($jc(
        $jc($jc($jc(0, id.hashCode), name.hashCode), startAt.hashCode),
        endAt.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Section')
          ..add('id', id)
          ..add('name', name)
          ..add('startAt', startAt)
          ..add('endAt', endAt))
        .toString();
  }
}

class SectionBuilder implements Builder<Section, SectionBuilder> {
  _$Section _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  DateTime _startAt;
  DateTime get startAt => _$this._startAt;
  set startAt(DateTime startAt) => _$this._startAt = startAt;

  DateTime _endAt;
  DateTime get endAt => _$this._endAt;
  set endAt(DateTime endAt) => _$this._endAt = endAt;

  SectionBuilder() {
    Section._initializeBuilder(this);
  }

  SectionBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _name = _$v.name;
      _startAt = _$v.startAt;
      _endAt = _$v.endAt;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Section other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Section;
  }

  @override
  void update(void Function(SectionBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Section build() {
    final _$result = _$v ??
        new _$Section._(id: id, name: name, startAt: startAt, endAt: endAt);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
