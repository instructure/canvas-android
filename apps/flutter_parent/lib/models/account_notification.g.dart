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
  Iterable<Object> serialize(
      Serializers serializers, AccountNotification object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AccountNotificationBuilder();

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
        case 'message':
          result.message = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'subject':
          result.subject = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'start_at':
          result.startAt = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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
          [void Function(AccountNotificationBuilder) updates]) =>
      (new AccountNotificationBuilder()..update(updates)).build();

  _$AccountNotification._({this.id, this.message, this.subject, this.startAt})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('AccountNotification', 'id');
    }
    if (message == null) {
      throw new BuiltValueNullFieldError('AccountNotification', 'message');
    }
    if (subject == null) {
      throw new BuiltValueNullFieldError('AccountNotification', 'subject');
    }
    if (startAt == null) {
      throw new BuiltValueNullFieldError('AccountNotification', 'startAt');
    }
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
    return $jf($jc(
        $jc($jc($jc(0, id.hashCode), message.hashCode), subject.hashCode),
        startAt.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('AccountNotification')
          ..add('id', id)
          ..add('message', message)
          ..add('subject', subject)
          ..add('startAt', startAt))
        .toString();
  }
}

class AccountNotificationBuilder
    implements Builder<AccountNotification, AccountNotificationBuilder> {
  _$AccountNotification _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _message;
  String get message => _$this._message;
  set message(String message) => _$this._message = message;

  String _subject;
  String get subject => _$this._subject;
  set subject(String subject) => _$this._subject = subject;

  String _startAt;
  String get startAt => _$this._startAt;
  set startAt(String startAt) => _$this._startAt = startAt;

  AccountNotificationBuilder() {
    AccountNotification._initializeBuilder(this);
  }

  AccountNotificationBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _message = _$v.message;
      _subject = _$v.subject;
      _startAt = _$v.startAt;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AccountNotification other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$AccountNotification;
  }

  @override
  void update(void Function(AccountNotificationBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$AccountNotification build() {
    final _$result = _$v ??
        new _$AccountNotification._(
            id: id, message: message, subject: subject, startAt: startAt);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
