// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'login.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Login> _$loginSerializer = new _$LoginSerializer();

class _$LoginSerializer implements StructuredSerializer<Login> {
  @override
  final Iterable<Type> types = const [Login, _$Login];
  @override
  final String wireName = 'Login';

  @override
  Iterable<Object> serialize(Serializers serializers, Login object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'uuid',
      serializers.serialize(object.uuid, specifiedType: const FullType(String)),
      'domain',
      serializers.serialize(object.domain,
          specifiedType: const FullType(String)),
      'accessToken',
      serializers.serialize(object.accessToken,
          specifiedType: const FullType(String)),
      'user',
      serializers.serialize(object.user, specifiedType: const FullType(User)),
    ];
    result.add('masqueradeId');
    if (object.masqueradeId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.masqueradeId,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  Login deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new LoginBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'uuid':
          result.uuid = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'domain':
          result.domain = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'accessToken':
          result.accessToken = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User)) as User);
          break;
        case 'masqueradeId':
          result.masqueradeId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$Login extends Login {
  @override
  final String uuid;
  @override
  final String domain;
  @override
  final String accessToken;
  @override
  final User user;
  @override
  final String masqueradeId;

  factory _$Login([void Function(LoginBuilder) updates]) =>
      (new LoginBuilder()..update(updates)).build();

  _$Login._(
      {this.uuid, this.domain, this.accessToken, this.user, this.masqueradeId})
      : super._() {
    if (uuid == null) {
      throw new BuiltValueNullFieldError('Login', 'uuid');
    }
    if (domain == null) {
      throw new BuiltValueNullFieldError('Login', 'domain');
    }
    if (accessToken == null) {
      throw new BuiltValueNullFieldError('Login', 'accessToken');
    }
    if (user == null) {
      throw new BuiltValueNullFieldError('Login', 'user');
    }
  }

  @override
  Login rebuild(void Function(LoginBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  LoginBuilder toBuilder() => new LoginBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Login &&
        uuid == other.uuid &&
        domain == other.domain &&
        accessToken == other.accessToken &&
        user == other.user &&
        masqueradeId == other.masqueradeId;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc($jc($jc(0, uuid.hashCode), domain.hashCode),
                accessToken.hashCode),
            user.hashCode),
        masqueradeId.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Login')
          ..add('uuid', uuid)
          ..add('domain', domain)
          ..add('accessToken', accessToken)
          ..add('user', user)
          ..add('masqueradeId', masqueradeId))
        .toString();
  }
}

class LoginBuilder implements Builder<Login, LoginBuilder> {
  _$Login _$v;

  String _uuid;
  String get uuid => _$this._uuid;
  set uuid(String uuid) => _$this._uuid = uuid;

  String _domain;
  String get domain => _$this._domain;
  set domain(String domain) => _$this._domain = domain;

  String _accessToken;
  String get accessToken => _$this._accessToken;
  set accessToken(String accessToken) => _$this._accessToken = accessToken;

  UserBuilder _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder user) => _$this._user = user;

  String _masqueradeId;
  String get masqueradeId => _$this._masqueradeId;
  set masqueradeId(String masqueradeId) => _$this._masqueradeId = masqueradeId;

  LoginBuilder() {
    Login._initializeBuilder(this);
  }

  LoginBuilder get _$this {
    if (_$v != null) {
      _uuid = _$v.uuid;
      _domain = _$v.domain;
      _accessToken = _$v.accessToken;
      _user = _$v.user?.toBuilder();
      _masqueradeId = _$v.masqueradeId;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Login other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Login;
  }

  @override
  void update(void Function(LoginBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Login build() {
    _$Login _$result;
    try {
      _$result = _$v ??
          new _$Login._(
              uuid: uuid,
              domain: domain,
              accessToken: accessToken,
              user: user.build(),
              masqueradeId: masqueradeId);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'user';
        user.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Login', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
