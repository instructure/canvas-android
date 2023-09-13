// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'account_notification.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<AccountNotification> _$accountNotificationSerializer =
    new _$AccountNotificationSerializer();

class _$AccountNotificationSerializer
    implements StructuredSerializer<AccountNotification> {
  @override
  final Iterable<Type> types = const [
    AccountNotification,
    _$AccountNotification
  ];
  @override
  final String wireName = 'AccountNotification';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, AccountNotification object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'message',
      serializers.serialize(object.message,
          specifiedType: const FullType(String)),
      'subject',
      serializers.serialize(object.subject,
          specifiedType: const FullType(String)),
      'start_at',
      serializers.serialize(object.startAt,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  AccountNotification deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AccountNotificationBuilder();

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
        case 'message':
          result.message = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'subject':
          result.subject = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'start_at':
          result.startAt = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
      }
    }

    return result.build();
  }
}

class _$AccountNotification extends AccountNotification {
  @override
  final String id;
  @override
  final String message;
  @override
  final String subject;
  @override
  final String startAt;

  factory _$AccountNotification(
          [void Function(AccountNotificationBuilder)? updates]) =>
      (new AccountNotificationBuilder()..update(updates))._build();

  _$AccountNotification._(
      {required this.id,
      required this.message,
      required this.subject,
      required this.startAt})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'AccountNotification', 'id');
    BuiltValueNullFieldError.checkNotNull(
        message, r'AccountNotification', 'message');
    BuiltValueNullFieldError.checkNotNull(
        subject, r'AccountNotification', 'subject');
    BuiltValueNullFieldError.checkNotNull(
        startAt, r'AccountNotification', 'startAt');
  }

  @override
  AccountNotification rebuild(
          void Function(AccountNotificationBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AccountNotificationBuilder toBuilder() =>
      new AccountNotificationBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is AccountNotification &&
        id == other.id &&
        message == other.message &&
        subject == other.subject &&
        startAt == other.startAt;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, message.hashCode);
    _$hash = $jc(_$hash, subject.hashCode);
    _$hash = $jc(_$hash, startAt.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'AccountNotification')
          ..add('id', id)
          ..add('message', message)
          ..add('subject', subject)
          ..add('startAt', startAt))
        .toString();
  }
}

class AccountNotificationBuilder
    implements Builder<AccountNotification, AccountNotificationBuilder> {
  _$AccountNotification? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _message;
  String? get message => _$this._message;
  set message(String? message) => _$this._message = message;

  String? _subject;
  String? get subject => _$this._subject;
  set subject(String? subject) => _$this._subject = subject;

  String? _startAt;
  String? get startAt => _$this._startAt;
  set startAt(String? startAt) => _$this._startAt = startAt;

  AccountNotificationBuilder() {
    AccountNotification._initializeBuilder(this);
  }

  AccountNotificationBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _message = $v.message;
      _subject = $v.subject;
      _startAt = $v.startAt;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AccountNotification other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$AccountNotification;
  }

  @override
  void update(void Function(AccountNotificationBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  AccountNotification build() => _build();

  _$AccountNotification _build() {
    final _$result = _$v ??
        new _$AccountNotification._(
            id: BuiltValueNullFieldError.checkNotNull(
                id, r'AccountNotification', 'id'),
            message: BuiltValueNullFieldError.checkNotNull(
                message, r'AccountNotification', 'message'),
            subject: BuiltValueNullFieldError.checkNotNull(
                subject, r'AccountNotification', 'subject'),
            startAt: BuiltValueNullFieldError.checkNotNull(
                startAt, r'AccountNotification', 'startAt'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
