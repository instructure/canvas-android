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
      'refreshToken',
      serializers.serialize(object.refreshToken,
          specifiedType: const FullType(String)),
      'user',
      serializers.serialize(object.user, specifiedType: const FullType(User)),
    ];
    result.add('clientId');
    if (object.clientId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.clientId,
          specifiedType: const FullType(String)));
    }
    result.add('clientSecret');
    if (object.clientSecret == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.clientSecret,
          specifiedType: const FullType(String)));
    }
    result.add('selectedStudentId');
    if (object.selectedStudentId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.selectedStudentId,
          specifiedType: const FullType(String)));
    }
    result.add('canMasquerade');
    if (object.canMasquerade == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.canMasquerade,
          specifiedType: const FullType(bool)));
    }
    result.add('masqueradeUser');
    if (object.masqueradeUser == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.masqueradeUser,
          specifiedType: const FullType(User)));
    }
    result.add('masqueradeDomain');
    if (object.masqueradeDomain == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.masqueradeDomain,
          specifiedType: const FullType(String)));
    }
    result.add('isMasqueradingFromQRCode');
    if (object.isMasqueradingFromQRCode == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.isMasqueradingFromQRCode,
          specifiedType: const FullType(bool)));
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
        case 'clientId':
          result.clientId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'clientSecret':
          result.clientSecret = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'accessToken':
          result.accessToken = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'refreshToken':
          result.refreshToken = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User)) as User);
          break;
        case 'selectedStudentId':
          result.selectedStudentId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'canMasquerade':
          result.canMasquerade = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'masqueradeUser':
          result.masqueradeUser.replace(serializers.deserialize(value,
              specifiedType: const FullType(User)) as User);
          break;
        case 'masqueradeDomain':
          result.masqueradeDomain = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'isMasqueradingFromQRCode':
          result.isMasqueradingFromQRCode = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
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
  final String clientId;
  @override
  final String clientSecret;
  @override
  final String accessToken;
  @override
  final String refreshToken;
  @override
  final User user;
  @override
  final String selectedStudentId;
  @override
  final bool canMasquerade;
  @override
  final User masqueradeUser;
  @override
  final String masqueradeDomain;
  @override
  final bool isMasqueradingFromQRCode;

  factory _$Login([void Function(LoginBuilder) updates]) =>
      (new LoginBuilder()..update(updates)).build();

  _$Login._(
      {this.uuid,
      this.domain,
      this.clientId,
      this.clientSecret,
      this.accessToken,
      this.refreshToken,
      this.user,
      this.selectedStudentId,
      this.canMasquerade,
      this.masqueradeUser,
      this.masqueradeDomain,
      this.isMasqueradingFromQRCode})
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
    if (refreshToken == null) {
      throw new BuiltValueNullFieldError('Login', 'refreshToken');
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
        clientId == other.clientId &&
        clientSecret == other.clientSecret &&
        accessToken == other.accessToken &&
        refreshToken == other.refreshToken &&
        user == other.user &&
        selectedStudentId == other.selectedStudentId &&
        canMasquerade == other.canMasquerade &&
        masqueradeUser == other.masqueradeUser &&
        masqueradeDomain == other.masqueradeDomain &&
        isMasqueradingFromQRCode == other.isMasqueradingFromQRCode;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc(
                                $jc(
                                    $jc(
                                        $jc(
                                            $jc($jc(0, uuid.hashCode),
                                                domain.hashCode),
                                            clientId.hashCode),
                                        clientSecret.hashCode),
                                    accessToken.hashCode),
                                refreshToken.hashCode),
                            user.hashCode),
                        selectedStudentId.hashCode),
                    canMasquerade.hashCode),
                masqueradeUser.hashCode),
            masqueradeDomain.hashCode),
        isMasqueradingFromQRCode.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Login')
          ..add('uuid', uuid)
          ..add('domain', domain)
          ..add('clientId', clientId)
          ..add('clientSecret', clientSecret)
          ..add('accessToken', accessToken)
          ..add('refreshToken', refreshToken)
          ..add('user', user)
          ..add('selectedStudentId', selectedStudentId)
          ..add('canMasquerade', canMasquerade)
          ..add('masqueradeUser', masqueradeUser)
          ..add('masqueradeDomain', masqueradeDomain)
          ..add('isMasqueradingFromQRCode', isMasqueradingFromQRCode))
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

  String _clientId;
  String get clientId => _$this._clientId;
  set clientId(String clientId) => _$this._clientId = clientId;

  String _clientSecret;
  String get clientSecret => _$this._clientSecret;
  set clientSecret(String clientSecret) => _$this._clientSecret = clientSecret;

  String _accessToken;
  String get accessToken => _$this._accessToken;
  set accessToken(String accessToken) => _$this._accessToken = accessToken;

  String _refreshToken;
  String get refreshToken => _$this._refreshToken;
  set refreshToken(String refreshToken) => _$this._refreshToken = refreshToken;

  UserBuilder _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder user) => _$this._user = user;

  String _selectedStudentId;
  String get selectedStudentId => _$this._selectedStudentId;
  set selectedStudentId(String selectedStudentId) =>
      _$this._selectedStudentId = selectedStudentId;

  bool _canMasquerade;
  bool get canMasquerade => _$this._canMasquerade;
  set canMasquerade(bool canMasquerade) =>
      _$this._canMasquerade = canMasquerade;

  UserBuilder _masqueradeUser;
  UserBuilder get masqueradeUser =>
      _$this._masqueradeUser ??= new UserBuilder();
  set masqueradeUser(UserBuilder masqueradeUser) =>
      _$this._masqueradeUser = masqueradeUser;

  String _masqueradeDomain;
  String get masqueradeDomain => _$this._masqueradeDomain;
  set masqueradeDomain(String masqueradeDomain) =>
      _$this._masqueradeDomain = masqueradeDomain;

  bool _isMasqueradingFromQRCode;
  bool get isMasqueradingFromQRCode => _$this._isMasqueradingFromQRCode;
  set isMasqueradingFromQRCode(bool isMasqueradingFromQRCode) =>
      _$this._isMasqueradingFromQRCode = isMasqueradingFromQRCode;

  LoginBuilder() {
    Login._initializeBuilder(this);
  }

  LoginBuilder get _$this {
    if (_$v != null) {
      _uuid = _$v.uuid;
      _domain = _$v.domain;
      _clientId = _$v.clientId;
      _clientSecret = _$v.clientSecret;
      _accessToken = _$v.accessToken;
      _refreshToken = _$v.refreshToken;
      _user = _$v.user?.toBuilder();
      _selectedStudentId = _$v.selectedStudentId;
      _canMasquerade = _$v.canMasquerade;
      _masqueradeUser = _$v.masqueradeUser?.toBuilder();
      _masqueradeDomain = _$v.masqueradeDomain;
      _isMasqueradingFromQRCode = _$v.isMasqueradingFromQRCode;
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
              clientId: clientId,
              clientSecret: clientSecret,
              accessToken: accessToken,
              refreshToken: refreshToken,
              user: user.build(),
              selectedStudentId: selectedStudentId,
              canMasquerade: canMasquerade,
              masqueradeUser: _masqueradeUser?.build(),
              masqueradeDomain: masqueradeDomain,
              isMasqueradingFromQRCode: isMasqueradingFromQRCode);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'user';
        user.build();

        _$failedField = 'masqueradeUser';
        _masqueradeUser?.build();
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
