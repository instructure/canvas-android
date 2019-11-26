// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'unread_count.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<UnreadCount> _$unreadCountSerializer = new _$UnreadCountSerializer();

class _$UnreadCountSerializer implements StructuredSerializer<UnreadCount> {
  @override
  final Iterable<Type> types = const [UnreadCount, _$UnreadCount];
  @override
  final String wireName = 'UnreadCount';

  @override
  Iterable<Object> serialize(Serializers serializers, UnreadCount object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'unread_count',
      serializers.serialize(object.count, specifiedType: const FullType(int)),
    ];

    return result;
  }

  @override
  UnreadCount deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UnreadCountBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'unread_count':
          result.count = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
      }
    }

    return result.build();
  }
}

class _$UnreadCount extends UnreadCount {
  @override
  final int count;

  factory _$UnreadCount([void Function(UnreadCountBuilder) updates]) =>
      (new UnreadCountBuilder()..update(updates)).build();

  _$UnreadCount._({this.count}) : super._() {
    if (count == null) {
      throw new BuiltValueNullFieldError('UnreadCount', 'count');
    }
  }

  @override
  UnreadCount rebuild(void Function(UnreadCountBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  UnreadCountBuilder toBuilder() => new UnreadCountBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is UnreadCount && count == other.count;
  }

  @override
  int get hashCode {
    return $jf($jc(0, count.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('UnreadCount')..add('count', count))
        .toString();
  }
}

class UnreadCountBuilder implements Builder<UnreadCount, UnreadCountBuilder> {
  _$UnreadCount _$v;

  int _count;
  int get count => _$this._count;
  set count(int count) => _$this._count = count;

  UnreadCountBuilder();

  UnreadCountBuilder get _$this {
    if (_$v != null) {
      _count = _$v.count;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UnreadCount other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$UnreadCount;
  }

  @override
  void update(void Function(UnreadCountBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$UnreadCount build() {
    final _$result = _$v ?? new _$UnreadCount._(count: count);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
