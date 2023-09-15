// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_account_post_body.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateAccountPostBody> _$createAccountPostBodySerializer =
    new _$CreateAccountPostBodySerializer();

class _$CreateAccountPostBodySerializer
    implements StructuredSerializer<CreateAccountPostBody> {
  @override
  final Iterable<Type> types = const [
    CreateAccountPostBody,
    _$CreateAccountPostBody
  ];
  @override
  final String wireName = 'CreateAccountPostBody';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, CreateAccountPostBody object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'pseudonym',
      serializers.serialize(object.pseudonym,
          specifiedType: const FullType(PostPseudonym)),
      'pairing_code',
      serializers.serialize(object.pairingCode,
          specifiedType: const FullType(PostPairingCode)),
      'user',
      serializers.serialize(object.user,
          specifiedType: const FullType(PostUser)),
    ];

    return result;
  }

  @override
  CreateAccountPostBody deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateAccountPostBodyBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'pseudonym':
          result.pseudonym.replace(serializers.deserialize(value,
              specifiedType: const FullType(PostPseudonym))! as PostPseudonym);
          break;
        case 'pairing_code':
          result.pairingCode.replace(serializers.deserialize(value,
                  specifiedType: const FullType(PostPairingCode))!
              as PostPairingCode);
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(PostUser))! as PostUser);
          break;
      }
    }

    return result.build();
  }
}

class _$CreateAccountPostBody extends CreateAccountPostBody {
  @override
  final PostPseudonym pseudonym;
  @override
  final PostPairingCode pairingCode;
  @override
  final PostUser user;

  factory _$CreateAccountPostBody(
          [void Function(CreateAccountPostBodyBuilder)? updates]) =>
      (new CreateAccountPostBodyBuilder()..update(updates))._build();

  _$CreateAccountPostBody._(
      {required this.pseudonym, required this.pairingCode, required this.user})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        pseudonym, r'CreateAccountPostBody', 'pseudonym');
    BuiltValueNullFieldError.checkNotNull(
        pairingCode, r'CreateAccountPostBody', 'pairingCode');
    BuiltValueNullFieldError.checkNotNull(
        user, r'CreateAccountPostBody', 'user');
  }

  @override
  CreateAccountPostBody rebuild(
          void Function(CreateAccountPostBodyBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateAccountPostBodyBuilder toBuilder() =>
      new CreateAccountPostBodyBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateAccountPostBody &&
        pseudonym == other.pseudonym &&
        pairingCode == other.pairingCode &&
        user == other.user;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, pseudonym.hashCode);
    _$hash = $jc(_$hash, pairingCode.hashCode);
    _$hash = $jc(_$hash, user.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateAccountPostBody')
          ..add('pseudonym', pseudonym)
          ..add('pairingCode', pairingCode)
          ..add('user', user))
        .toString();
  }
}

class CreateAccountPostBodyBuilder
    implements Builder<CreateAccountPostBody, CreateAccountPostBodyBuilder> {
  _$CreateAccountPostBody? _$v;

  PostPseudonymBuilder? _pseudonym;
  PostPseudonymBuilder get pseudonym =>
      _$this._pseudonym ??= new PostPseudonymBuilder();
  set pseudonym(PostPseudonymBuilder? pseudonym) =>
      _$this._pseudonym = pseudonym;

  PostPairingCodeBuilder? _pairingCode;
  PostPairingCodeBuilder get pairingCode =>
      _$this._pairingCode ??= new PostPairingCodeBuilder();
  set pairingCode(PostPairingCodeBuilder? pairingCode) =>
      _$this._pairingCode = pairingCode;

  PostUserBuilder? _user;
  PostUserBuilder get user => _$this._user ??= new PostUserBuilder();
  set user(PostUserBuilder? user) => _$this._user = user;

  CreateAccountPostBodyBuilder();

  CreateAccountPostBodyBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _pseudonym = $v.pseudonym.toBuilder();
      _pairingCode = $v.pairingCode.toBuilder();
      _user = $v.user.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateAccountPostBody other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateAccountPostBody;
  }

  @override
  void update(void Function(CreateAccountPostBodyBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateAccountPostBody build() => _build();

  _$CreateAccountPostBody _build() {
    _$CreateAccountPostBody _$result;
    try {
      _$result = _$v ??
          new _$CreateAccountPostBody._(
              pseudonym: pseudonym.build(),
              pairingCode: pairingCode.build(),
              user: user.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'pseudonym';
        pseudonym.build();
        _$failedField = 'pairingCode';
        pairingCode.build();
        _$failedField = 'user';
        user.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CreateAccountPostBody', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
