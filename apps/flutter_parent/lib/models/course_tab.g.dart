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
  Iterable<Object?> serialize(Serializers serializers, CourseTab object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  CourseTab deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CourseTabBuilder();

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
      }
    }

    return result.build();
  }
}

class _$CourseTab extends CourseTab {
  @override
  final String id;

  factory _$CourseTab([void Function(CourseTabBuilder)? updates]) =>
      (new CourseTabBuilder()..update(updates))._build();

  _$CourseTab._({required this.id}) : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'CourseTab', 'id');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CourseTab')..add('id', id))
        .toString();
  }
}

class CourseTabBuilder implements Builder<CourseTab, CourseTabBuilder> {
  _$CourseTab? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  CourseTabBuilder();

  CourseTabBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CourseTab other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CourseTab;
  }

  @override
  void update(void Function(CourseTabBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CourseTab build() => _build();

  _$CourseTab _build() {
    final _$result = _$v ??
        new _$CourseTab._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'CourseTab', 'id'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
