// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'course_tab.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CourseTab> _$courseTabSerializer = new _$CourseTabSerializer();

class _$CourseTabSerializer implements StructuredSerializer<CourseTab> {
  @override
  final Iterable<Type> types = const [CourseTab, _$CourseTab];
  @override
  final String wireName = 'CourseTab';

  @override
  Iterable<Object> serialize(Serializers serializers, CourseTab object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  CourseTab deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CourseTabBuilder();

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
      }
    }

    return result.build();
  }
}

class _$CourseTab extends CourseTab {
  @override
  final String id;

  factory _$CourseTab([void Function(CourseTabBuilder) updates]) =>
      (new CourseTabBuilder()..update(updates)).build();

  _$CourseTab._({this.id}) : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('CourseTab', 'id');
    }
  }

  @override
  CourseTab rebuild(void Function(CourseTabBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CourseTabBuilder toBuilder() => new CourseTabBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CourseTab && id == other.id;
  }

  @override
  int get hashCode {
    return $jf($jc(0, id.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CourseTab')..add('id', id)).toString();
  }
}

class CourseTabBuilder implements Builder<CourseTab, CourseTabBuilder> {
  _$CourseTab _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  CourseTabBuilder();

  CourseTabBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CourseTab other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CourseTab;
  }

  @override
  void update(void Function(CourseTabBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CourseTab build() {
    final _$result = _$v ?? new _$CourseTab._(id: id);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
