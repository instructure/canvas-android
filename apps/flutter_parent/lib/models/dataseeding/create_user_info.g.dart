// GENERATED CODE - DO NOT MODIFY BY HAND

part of create_user_info;

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateUserInfo> _$createUserInfoSerializer =
    new _$CreateUserInfoSerializer();

class _$CreateUserInfoSerializer
    implements StructuredSerializer<CreateUserInfo> {
  @override
  final Iterable<Type> types = const [CreateUserInfo, _$CreateUserInfo];
  @override
  final String wireName = 'CreateUserInfo';

  @override
  Iterable<Object> serialize(Serializers serializers, CreateUserInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'user',
      serializers.serialize(object.user,
          specifiedType: const FullType(UserNameData)),
      'pseudonym',
      serializers.serialize(object.pseudonym,
          specifiedType: const FullType(Pseudonym)),
      'communication_channel',
      serializers.serialize(object.communicationChannel,
          specifiedType: const FullType(CommunicationChannel)),
    ];

    return result;
  }

  @override
  CreateUserInfo deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateUserInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(UserNameData)) as UserNameData);
          break;
        case 'pseudonym':
          result.pseudonym.replace(serializers.deserialize(value,
              specifiedType: const FullType(Pseudonym)) as Pseudonym);
          break;
        case 'communication_channel':
          result.communicationChannel.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CommunicationChannel))
              as CommunicationChannel);
          break;
      }
    }

    return result.build();
  }
}

class _$CreateUserInfo extends CreateUserInfo {
  @override
  final UserNameData user;
  @override
  final Pseudonym pseudonym;
  @override
  final CommunicationChannel communicationChannel;

  factory _$CreateUserInfo([void Function(CreateUserInfoBuilder) updates]) =>
      (new CreateUserInfoBuilder()..update(updates)).build();

  _$CreateUserInfo._({this.user, this.pseudonym, this.communicationChannel})
      : super._() {
    if (user == null) {
      throw new BuiltValueNullFieldError('CreateUserInfo', 'user');
    }
    if (pseudonym == null) {
      throw new BuiltValueNullFieldError('CreateUserInfo', 'pseudonym');
    }
    if (communicationChannel == null) {
      throw new BuiltValueNullFieldError(
          'CreateUserInfo', 'communicationChannel');
    }
  }

  @override
  CreateUserInfo rebuild(void Function(CreateUserInfoBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateUserInfoBuilder toBuilder() =>
      new CreateUserInfoBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateUserInfo &&
        user == other.user &&
        pseudonym == other.pseudonym &&
        communicationChannel == other.communicationChannel;
  }

  @override
  int get hashCode {
    return $jf($jc($jc($jc(0, user.hashCode), pseudonym.hashCode),
        communicationChannel.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateUserInfo')
          ..add('user', user)
          ..add('pseudonym', pseudonym)
          ..add('communicationChannel', communicationChannel))
        .toString();
  }
}

class CreateUserInfoBuilder
    implements Builder<CreateUserInfo, CreateUserInfoBuilder> {
  _$CreateUserInfo _$v;

  UserNameDataBuilder _user;
  UserNameDataBuilder get user => _$this._user ??= new UserNameDataBuilder();
  set user(UserNameDataBuilder user) => _$this._user = user;

  PseudonymBuilder _pseudonym;
  PseudonymBuilder get pseudonym =>
      _$this._pseudonym ??= new PseudonymBuilder();
  set pseudonym(PseudonymBuilder pseudonym) => _$this._pseudonym = pseudonym;

  CommunicationChannelBuilder _communicationChannel;
  CommunicationChannelBuilder get communicationChannel =>
      _$this._communicationChannel ??= new CommunicationChannelBuilder();
  set communicationChannel(CommunicationChannelBuilder communicationChannel) =>
      _$this._communicationChannel = communicationChannel;

  CreateUserInfoBuilder() {
    CreateUserInfo._initializeBuilder(this);
  }

  CreateUserInfoBuilder get _$this {
    if (_$v != null) {
      _user = _$v.user?.toBuilder();
      _pseudonym = _$v.pseudonym?.toBuilder();
      _communicationChannel = _$v.communicationChannel?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateUserInfo other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateUserInfo;
  }

  @override
  void update(void Function(CreateUserInfoBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateUserInfo build() {
    _$CreateUserInfo _$result;
    try {
      _$result = _$v ??
          new _$CreateUserInfo._(
              user: user.build(),
              pseudonym: pseudonym.build(),
              communicationChannel: communicationChannel.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'user';
        user.build();
        _$failedField = 'pseudonym';
        pseudonym.build();
        _$failedField = 'communicationChannel';
        communicationChannel.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CreateUserInfo', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
