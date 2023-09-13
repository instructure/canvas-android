// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'oauth_token.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<OAuthToken> _$oAuthTokenSerializer = new _$OAuthTokenSerializer();

class _$OAuthTokenSerializer implements StructuredSerializer<OAuthToken> {
  @override
  final Iterable<Type> types = const [OAuthToken, _$OAuthToken];
  @override
  final String wireName = 'OAuthToken';

  @override
  Iterable<Object?> serialize(Serializers serializers, OAuthToken object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'access_token',
      serializers.serialize(object.accessToken,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  OAuthToken deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new OAuthTokenBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'access_token':
          result.accessToken = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
      }
    }

    return result.build();
  }
}

class _$OAuthToken extends OAuthToken {
  @override
  final String accessToken;

  factory _$OAuthToken([void Function(OAuthTokenBuilder)? updates]) =>
      (new OAuthTokenBuilder()..update(updates))._build();

  _$OAuthToken._({required this.accessToken}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        accessToken, r'OAuthToken', 'accessToken');
  }

  @override
  OAuthToken rebuild(void Function(OAuthTokenBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  OAuthTokenBuilder toBuilder() => new OAuthTokenBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is OAuthToken && accessToken == other.accessToken;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, accessToken.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'OAuthToken')
          ..add('accessToken', accessToken))
        .toString();
  }
}

class OAuthTokenBuilder implements Builder<OAuthToken, OAuthTokenBuilder> {
  _$OAuthToken? _$v;

  String? _accessToken;
  String? get accessToken => _$this._accessToken;
  set accessToken(String? accessToken) => _$this._accessToken = accessToken;

  OAuthTokenBuilder() {
    OAuthToken._initializeBuilder(this);
  }

  OAuthTokenBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _accessToken = $v.accessToken;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(OAuthToken other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$OAuthToken;
  }

  @override
  void update(void Function(OAuthTokenBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  OAuthToken build() => _build();

  _$OAuthToken _build() {
    final _$result = _$v ??
        new _$OAuthToken._(
            accessToken: BuiltValueNullFieldError.checkNotNull(
                accessToken, r'OAuthToken', 'accessToken'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
