// GENERATED CODE - DO NOT MODIFY BY HAND

part of oauth_token;

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
  Iterable<Object> serialize(Serializers serializers, OAuthToken object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'access_token',
      serializers.serialize(object.accessToken,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  OAuthToken deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new OAuthTokenBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'access_token':
          result.accessToken = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$OAuthToken extends OAuthToken {
  @override
  final String accessToken;

  factory _$OAuthToken([void Function(OAuthTokenBuilder) updates]) =>
      (new OAuthTokenBuilder()..update(updates)).build();

  _$OAuthToken._({this.accessToken}) : super._() {
    if (accessToken == null) {
      throw new BuiltValueNullFieldError('OAuthToken', 'accessToken');
    }
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
    return $jf($jc(0, accessToken.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('OAuthToken')
          ..add('accessToken', accessToken))
        .toString();
  }
}

class OAuthTokenBuilder implements Builder<OAuthToken, OAuthTokenBuilder> {
  _$OAuthToken _$v;

  String _accessToken;
  String get accessToken => _$this._accessToken;
  set accessToken(String accessToken) => _$this._accessToken = accessToken;

  OAuthTokenBuilder() {
    OAuthToken._initializeBuilder(this);
  }

  OAuthTokenBuilder get _$this {
    if (_$v != null) {
      _accessToken = _$v.accessToken;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(OAuthToken other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$OAuthToken;
  }

  @override
  void update(void Function(OAuthTokenBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$OAuthToken build() {
    final _$result = _$v ?? new _$OAuthToken._(accessToken: accessToken);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
