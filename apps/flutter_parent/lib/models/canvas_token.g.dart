// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'canvas_token.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CanvasToken> _$canvasTokenSerializer = new _$CanvasTokenSerializer();

class _$CanvasTokenSerializer implements StructuredSerializer<CanvasToken> {
  @override
  final Iterable<Type> types = const [CanvasToken, _$CanvasToken];
  @override
  final String wireName = 'CanvasToken';

  @override
  Iterable<Object?> serialize(Serializers serializers, CanvasToken object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'access_token',
      serializers.serialize(object.accessToken,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.refreshToken;

    result
      ..add('refresh_token')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.user;

    result
      ..add('user')
      ..add(serializers.serialize(value, specifiedType: const FullType(User)));
    value = object.realUser;

    result
      ..add('real_user')
      ..add(serializers.serialize(value, specifiedType: const FullType(User)));

    return result;
  }

  @override
  CanvasToken deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CanvasTokenBuilder();

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
        case 'refresh_token':
          result.refreshToken = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User))! as User);
          break;
        case 'real_user':
          result.realUser.replace(serializers.deserialize(value,
              specifiedType: const FullType(User))! as User);
          break;
      }
    }

    return result.build();
  }
}

class _$CanvasToken extends CanvasToken {
  @override
  final String accessToken;
  @override
  final String? refreshToken;
  @override
  final User? user;
  @override
  final User? realUser;

  factory _$CanvasToken([void Function(CanvasTokenBuilder)? updates]) =>
      (new CanvasTokenBuilder()..update(updates))._build();

  _$CanvasToken._(
      {required this.accessToken, this.refreshToken, this.user, this.realUser})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        accessToken, r'CanvasToken', 'accessToken');
  }

  @override
  CanvasToken rebuild(void Function(CanvasTokenBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CanvasTokenBuilder toBuilder() => new CanvasTokenBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CanvasToken &&
        accessToken == other.accessToken &&
        refreshToken == other.refreshToken &&
        user == other.user &&
        realUser == other.realUser;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, accessToken.hashCode);
    _$hash = $jc(_$hash, refreshToken.hashCode);
    _$hash = $jc(_$hash, user.hashCode);
    _$hash = $jc(_$hash, realUser.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CanvasToken')
          ..add('accessToken', accessToken)
          ..add('refreshToken', refreshToken)
          ..add('user', user)
          ..add('realUser', realUser))
        .toString();
  }
}

class CanvasTokenBuilder implements Builder<CanvasToken, CanvasTokenBuilder> {
  _$CanvasToken? _$v;

  String? _accessToken;
  String? get accessToken => _$this._accessToken;
  set accessToken(String? accessToken) => _$this._accessToken = accessToken;

  String? _refreshToken;
  String? get refreshToken => _$this._refreshToken;
  set refreshToken(String? refreshToken) => _$this._refreshToken = refreshToken;

  UserBuilder? _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder? user) => _$this._user = user;

  UserBuilder? _realUser;
  UserBuilder get realUser => _$this._realUser ??= new UserBuilder();
  set realUser(UserBuilder? realUser) => _$this._realUser = realUser;

  CanvasTokenBuilder();

  CanvasTokenBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _accessToken = $v.accessToken;
      _refreshToken = $v.refreshToken;
      _user = $v.user?.toBuilder();
      _realUser = $v.realUser?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CanvasToken other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CanvasToken;
  }

  @override
  void update(void Function(CanvasTokenBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CanvasToken build() => _build();

  _$CanvasToken _build() {
    _$CanvasToken _$result;
    try {
      _$result = _$v ??
          new _$CanvasToken._(
              accessToken: BuiltValueNullFieldError.checkNotNull(
                  accessToken, r'CanvasToken', 'accessToken'),
              refreshToken: refreshToken,
              user: _user?.build(),
              realUser: _realUser?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'user';
        _user?.build();
        _$failedField = 'realUser';
        _realUser?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CanvasToken', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
