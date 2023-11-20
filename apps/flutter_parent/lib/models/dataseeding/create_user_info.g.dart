// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_user_info.dart';

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
  Iterable<Object?> serialize(Serializers serializers, CreateUserInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateUserInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(UserNameData))! as UserNameData);
          break;
        case 'pseudonym':
          result.pseudonym.replace(serializers.deserialize(value,
              specifiedType: const FullType(Pseudonym))! as Pseudonym);
          break;
        case 'communication_channel':
          result.communicationChannel.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CommunicationChannel))!
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

  factory _$CreateUserInfo([void Function(CreateUserInfoBuilder)? updates]) =>
      (new CreateUserInfoBuilder()..update(updates))._build();

  _$CreateUserInfo._(
      {required this.user,
      required this.pseudonym,
      required this.communicationChannel})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(user, r'CreateUserInfo', 'user');
    BuiltValueNullFieldError.checkNotNull(
        pseudonym, r'CreateUserInfo', 'pseudonym');
    BuiltValueNullFieldError.checkNotNull(
        communicationChannel, r'CreateUserInfo', 'communicationChannel');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, user.hashCode);
    _$hash = $jc(_$hash, pseudonym.hashCode);
    _$hash = $jc(_$hash, communicationChannel.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateUserInfo')
          ..add('user', user)
          ..add('pseudonym', pseudonym)
          ..add('communicationChannel', communicationChannel))
        .toString();
  }
}

class CreateUserInfoBuilder
    implements Builder<CreateUserInfo, CreateUserInfoBuilder> {
  _$CreateUserInfo? _$v;

  UserNameDataBuilder? _user;
  UserNameDataBuilder get user => _$this._user ??= new UserNameDataBuilder();
  set user(UserNameDataBuilder? user) => _$this._user = user;

  PseudonymBuilder? _pseudonym;
  PseudonymBuilder get pseudonym =>
      _$this._pseudonym ??= new PseudonymBuilder();
  set pseudonym(PseudonymBuilder? pseudonym) => _$this._pseudonym = pseudonym;

  CommunicationChannelBuilder? _communicationChannel;
  CommunicationChannelBuilder get communicationChannel =>
      _$this._communicationChannel ??= new CommunicationChannelBuilder();
  set communicationChannel(CommunicationChannelBuilder? communicationChannel) =>
      _$this._communicationChannel = communicationChannel;

  CreateUserInfoBuilder() {
    CreateUserInfo._initializeBuilder(this);
  }

  CreateUserInfoBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _user = $v.user.toBuilder();
      _pseudonym = $v.pseudonym.toBuilder();
      _communicationChannel = $v.communicationChannel.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateUserInfo other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateUserInfo;
  }

  @override
  void update(void Function(CreateUserInfoBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateUserInfo build() => _build();

  _$CreateUserInfo _build() {
    _$CreateUserInfo _$result;
    try {
      _$result = _$v ??
          new _$CreateUserInfo._(
              user: user.build(),
              pseudonym: pseudonym.build(),
              communicationChannel: communicationChannel.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'user';
        user.build();
        _$failedField = 'pseudonym';
        pseudonym.build();
        _$failedField = 'communicationChannel';
        communicationChannel.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CreateUserInfo', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
