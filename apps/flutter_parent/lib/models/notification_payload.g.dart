// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_payload.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const NotificationPayloadType _$notificationPayloadTypeReminder =
    const NotificationPayloadType._('reminder');
const NotificationPayloadType _$notificationPayloadTypeOther =
    const NotificationPayloadType._('other');

NotificationPayloadType _$notificationPayloadTypeValueOf(String name) {
  switch (name) {
    case 'reminder':
      return _$notificationPayloadTypeReminder;
    case 'other':
      return _$notificationPayloadTypeOther;
    default:
      return _$notificationPayloadTypeOther;
  }
}

final BuiltSet<NotificationPayloadType> _$notificationPayloadTypeValues =
    new BuiltSet<NotificationPayloadType>(const <NotificationPayloadType>[
  _$notificationPayloadTypeReminder,
  _$notificationPayloadTypeOther,
]);

Serializer<NotificationPayload> _$notificationPayloadSerializer =
    new _$NotificationPayloadSerializer();
Serializer<NotificationPayloadType> _$notificationPayloadTypeSerializer =
    new _$NotificationPayloadTypeSerializer();

class _$NotificationPayloadSerializer
    implements StructuredSerializer<NotificationPayload> {
  @override
  final Iterable<Type> types = const [
    NotificationPayload,
    _$NotificationPayload
  ];
  @override
  final String wireName = 'NotificationPayload';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, NotificationPayload object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'type',
      serializers.serialize(object.type,
          specifiedType: const FullType(NotificationPayloadType)),
    ];
    Object? value;
    value = object.data;

    result
      ..add('data')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  NotificationPayload deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new NotificationPayloadBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'type':
          result.type = serializers.deserialize(value,
                  specifiedType: const FullType(NotificationPayloadType))!
              as NotificationPayloadType;
          break;
        case 'data':
          result.data = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$NotificationPayloadTypeSerializer
    implements PrimitiveSerializer<NotificationPayloadType> {
  static const Map<String, Object> _toWire = const <String, Object>{
    'reminder': 'reminder',
  };
  static const Map<Object, String> _fromWire = const <Object, String>{
    'reminder': 'reminder',
  };

  @override
  final Iterable<Type> types = const <Type>[NotificationPayloadType];
  @override
  final String wireName = 'type';

  @override
  Object serialize(Serializers serializers, NotificationPayloadType object,
          {FullType specifiedType = FullType.unspecified}) =>
      _toWire[object.name] ?? object.name;

  @override
  NotificationPayloadType deserialize(
          Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      NotificationPayloadType.valueOf(
          _fromWire[serialized] ?? (serialized is String ? serialized : ''));
}

class _$NotificationPayload extends NotificationPayload {
  @override
  final NotificationPayloadType type;
  @override
  final String? data;

  factory _$NotificationPayload(
          [void Function(NotificationPayloadBuilder)? updates]) =>
      (new NotificationPayloadBuilder()..update(updates))._build();

  _$NotificationPayload._({required this.type, this.data}) : super._() {
    BuiltValueNullFieldError.checkNotNull(type, r'NotificationPayload', 'type');
  }

  @override
  NotificationPayload rebuild(
          void Function(NotificationPayloadBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  NotificationPayloadBuilder toBuilder() =>
      new NotificationPayloadBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is NotificationPayload &&
        type == other.type &&
        data == other.data;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, type.hashCode);
    _$hash = $jc(_$hash, data.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'NotificationPayload')
          ..add('type', type)
          ..add('data', data))
        .toString();
  }
}

class NotificationPayloadBuilder
    implements Builder<NotificationPayload, NotificationPayloadBuilder> {
  _$NotificationPayload? _$v;

  NotificationPayloadType? _type;
  NotificationPayloadType? get type => _$this._type;
  set type(NotificationPayloadType? type) => _$this._type = type;

  String? _data;
  String? get data => _$this._data;
  set data(String? data) => _$this._data = data;

  NotificationPayloadBuilder();

  NotificationPayloadBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _type = $v.type;
      _data = $v.data;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(NotificationPayload other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$NotificationPayload;
  }

  @override
  void update(void Function(NotificationPayloadBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  NotificationPayload build() => _build();

  _$NotificationPayload _build() {
    final _$result = _$v ??
        new _$NotificationPayload._(
            type: BuiltValueNullFieldError.checkNotNull(
                type, r'NotificationPayload', 'type'),
            data: data);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
