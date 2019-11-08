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
  Iterable<Object> serialize(Serializers serializers, CanvasToken object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'access_token',
      serializers.serialize(object.accessToken,
          specifiedType: const FullType(String)),
    ];
    result.add('refresh_token');
    if (object.refreshToken == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.refreshToken,
          specifiedType: const FullType(String)));
    }
    result.add('user');
    if (object.user == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.user,
          specifiedType: const FullType(User)));
    }
    return result;
  }

  @override
  CanvasToken deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CanvasTokenBuilder();

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
        case 'refresh_token':
          result.refreshToken = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User)) as User);
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
  final String refreshToken;
  @override
  final User user;

  factory _$CanvasToken([void Function(CanvasTokenBuilder) updates]) =>
      (new CanvasTokenBuilder()..update(updates)).build();

  _$CanvasToken._({this.accessToken, this.refreshToken, this.user})
      : super._() {
    if (accessToken == null) {
      throw new BuiltValueNullFieldError('CanvasToken', 'accessToken');
    }
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
        user == other.user;
  }

  @override
  int get hashCode {
    return $jf($jc($jc($jc(0, accessToken.hashCode), refreshToken.hashCode),
        user.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CanvasToken')
          ..add('accessToken', accessToken)
          ..add('refreshToken', refreshToken)
          ..add('user', user))
        .toString();
  }
}

class CanvasTokenBuilder implements Builder<CanvasToken, CanvasTokenBuilder> {
  _$CanvasToken _$v;

  String _accessToken;
  String get accessToken => _$this._accessToken;
  set accessToken(String accessToken) => _$this._accessToken = accessToken;

  String _refreshToken;
  String get refreshToken => _$this._refreshToken;
  set refreshToken(String refreshToken) => _$this._refreshToken = refreshToken;

  UserBuilder _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder user) => _$this._user = user;

  CanvasTokenBuilder();

  CanvasTokenBuilder get _$this {
    if (_$v != null) {
      _accessToken = _$v.accessToken;
      _refreshToken = _$v.refreshToken;
      _user = _$v.user?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CanvasToken other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CanvasToken;
  }

  @override
  void update(void Function(CanvasTokenBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CanvasToken build() {
    _$CanvasToken _$result;
    try {
      _$result = _$v ??
          new _$CanvasToken._(
              accessToken: accessToken,
              refreshToken: refreshToken,
              user: _user?.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'user';
        _user?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CanvasToken', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
