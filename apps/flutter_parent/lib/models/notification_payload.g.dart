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
  Iterable<Object> serialize(
      Serializers serializers, NotificationPayload object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'type',
      serializers.serialize(object.type,
          specifiedType: const FullType(NotificationPayloadType)),
    ];
    result.add('domain');
    if (object.domain == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.domain,
          specifiedType: const FullType(String)));
    }
    result.add('userId');
    if (object.userId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.userId,
          specifiedType: const FullType(String)));
    }
    result.add('studentId');
    if (object.studentId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.studentId,
          specifiedType: const FullType(String)));
    }
    result.add('routeUrl');
    if (object.routeUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.routeUrl,
          specifiedType: const FullType(String)));
    }
    result.add('data');
    if (object.data == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.data,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  NotificationPayload deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new NotificationPayloadBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'type':
          result.type = serializers.deserialize(value,
                  specifiedType: const FullType(NotificationPayloadType))
              as NotificationPayloadType;
          break;
        case 'domain':
          result.domain = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'userId':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'studentId':
          result.studentId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'routeUrl':
          result.routeUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'data':
          result.data = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$NotificationPayloadTypeSerializer
    implements PrimitiveSerializer<NotificationPayloadType> {
  static const Map<String, String> _toWire = const <String, String>{
    'reminder': 'reminder',
  };
  static const Map<String, String> _fromWire = const <String, String>{
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
          _fromWire[serialized] ?? serialized as String);
}

class _$NotificationPayload extends NotificationPayload {
  @override
  final NotificationPayloadType type;
  @override
  final String domain;
  @override
  final String userId;
  @override
  final String studentId;
  @override
  final String routeUrl;
  @override
  final String data;

  factory _$NotificationPayload(
          [void Function(NotificationPayloadBuilder) updates]) =>
      (new NotificationPayloadBuilder()..update(updates)).build();

  _$NotificationPayload._(
      {this.type,
      this.domain,
      this.userId,
      this.studentId,
      this.routeUrl,
      this.data})
      : super._() {
    if (type == null) {
      throw new BuiltValueNullFieldError('NotificationPayload', 'type');
    }
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
        domain == other.domain &&
        userId == other.userId &&
        studentId == other.studentId &&
        routeUrl == other.routeUrl &&
        data == other.data;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc($jc($jc(0, type.hashCode), domain.hashCode),
                    userId.hashCode),
                studentId.hashCode),
            routeUrl.hashCode),
        data.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('NotificationPayload')
          ..add('type', type)
          ..add('domain', domain)
          ..add('userId', userId)
          ..add('studentId', studentId)
          ..add('routeUrl', routeUrl)
          ..add('data', data))
        .toString();
  }
}

class NotificationPayloadBuilder
    implements Builder<NotificationPayload, NotificationPayloadBuilder> {
  _$NotificationPayload _$v;

  NotificationPayloadType _type;
  NotificationPayloadType get type => _$this._type;
  set type(NotificationPayloadType type) => _$this._type = type;

  String _domain;
  String get domain => _$this._domain;
  set domain(String domain) => _$this._domain = domain;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  String _studentId;
  String get studentId => _$this._studentId;
  set studentId(String studentId) => _$this._studentId = studentId;

  String _routeUrl;
  String get routeUrl => _$this._routeUrl;
  set routeUrl(String routeUrl) => _$this._routeUrl = routeUrl;

  String _data;
  String get data => _$this._data;
  set data(String data) => _$this._data = data;

  NotificationPayloadBuilder();

  NotificationPayloadBuilder get _$this {
    if (_$v != null) {
      _type = _$v.type;
      _domain = _$v.domain;
      _userId = _$v.userId;
      _studentId = _$v.studentId;
      _routeUrl = _$v.routeUrl;
      _data = _$v.data;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(NotificationPayload other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$NotificationPayload;
  }

  @override
  void update(void Function(NotificationPayloadBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$NotificationPayload build() {
    final _$result = _$v ??
        new _$NotificationPayload._(
            type: type,
            domain: domain,
            userId: userId,
            studentId: studentId,
            routeUrl: routeUrl,
            data: data);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
