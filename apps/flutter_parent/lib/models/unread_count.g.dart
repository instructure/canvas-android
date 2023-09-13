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
  Iterable<Object?> serialize(Serializers serializers, UnreadCount object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'unread_count',
      serializers.serialize(object.count,
          specifiedType: const FullType(JsonObject)),
    ];

    return result;
  }

  @override
  UnreadCount deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UnreadCountBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'unread_count':
          result.count = serializers.deserialize(value,
              specifiedType: const FullType(JsonObject))! as JsonObject;
          break;
      }
    }

    return result.build();
  }
}

class _$UnreadCount extends UnreadCount {
  @override
  final JsonObject count;

  factory _$UnreadCount([void Function(UnreadCountBuilder)? updates]) =>
      (new UnreadCountBuilder()..update(updates))._build();

  _$UnreadCount._({required this.count}) : super._() {
    BuiltValueNullFieldError.checkNotNull(count, r'UnreadCount', 'count');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, count.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'UnreadCount')..add('count', count))
        .toString();
  }
}

class UnreadCountBuilder implements Builder<UnreadCount, UnreadCountBuilder> {
  _$UnreadCount? _$v;

  JsonObject? _count;
  JsonObject? get count => _$this._count;
  set count(JsonObject? count) => _$this._count = count;

  UnreadCountBuilder();

  UnreadCountBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _count = $v.count;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UnreadCount other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$UnreadCount;
  }

  @override
  void update(void Function(UnreadCountBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  UnreadCount build() => _build();

  _$UnreadCount _build() {
    final _$result = _$v ??
        new _$UnreadCount._(
            count: BuiltValueNullFieldError.checkNotNull(
                count, r'UnreadCount', 'count'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
