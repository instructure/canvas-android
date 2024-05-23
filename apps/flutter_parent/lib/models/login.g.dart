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
  Iterable<Object?> serialize(Serializers serializers, Login object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
    Object? value;
    value = object.clientId;

    result
      ..add('clientId')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.clientSecret;

    result
      ..add('clientSecret')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.selectedStudentId;

    result
      ..add('selectedStudentId')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.canMasquerade;

    result
      ..add('canMasquerade')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));
    value = object.masqueradeUser;

    result
      ..add('masqueradeUser')
      ..add(serializers.serialize(value, specifiedType: const FullType(User)));
    value = object.masqueradeDomain;

    result
      ..add('masqueradeDomain')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.isMasqueradingFromQRCode;

    result
      ..add('isMasqueradingFromQRCode')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));

    return result;
  }

  @override
  Login deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new LoginBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'uuid':
          result.uuid = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'domain':
          result.domain = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'clientId':
          result.clientId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'clientSecret':
          result.clientSecret = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'accessToken':
          result.accessToken = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'refreshToken':
          result.refreshToken = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'user':
          result.user.replace(serializers.deserialize(value,
              specifiedType: const FullType(User))! as User);
          break;
        case 'selectedStudentId':
          result.selectedStudentId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'canMasquerade':
          result.canMasquerade = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
          break;
        case 'masqueradeUser':
          result.masqueradeUser.replace(serializers.deserialize(value,
              specifiedType: const FullType(User))! as User);
          break;
        case 'masqueradeDomain':
          result.masqueradeDomain = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'isMasqueradingFromQRCode':
          result.isMasqueradingFromQRCode = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
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
  final String? clientId;
  @override
  final String? clientSecret;
  @override
  final String accessToken;
  @override
  final String refreshToken;
  @override
  final User user;
  @override
  final String? selectedStudentId;
  @override
  final bool? canMasquerade;
  @override
  final User? masqueradeUser;
  @override
  final String? masqueradeDomain;
  @override
  final bool? isMasqueradingFromQRCode;

  factory _$Login([void Function(LoginBuilder)? updates]) =>
      (new LoginBuilder()..update(updates))._build();

  _$Login._(
      {required this.uuid,
      required this.domain,
      this.clientId,
      this.clientSecret,
      required this.accessToken,
      required this.refreshToken,
      required this.user,
      this.selectedStudentId,
      this.canMasquerade,
      this.masqueradeUser,
      this.masqueradeDomain,
      this.isMasqueradingFromQRCode})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(uuid, r'Login', 'uuid');
    BuiltValueNullFieldError.checkNotNull(domain, r'Login', 'domain');
    BuiltValueNullFieldError.checkNotNull(accessToken, r'Login', 'accessToken');
    BuiltValueNullFieldError.checkNotNull(
        refreshToken, r'Login', 'refreshToken');
    BuiltValueNullFieldError.checkNotNull(user, r'Login', 'user');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, uuid.hashCode);
    _$hash = $jc(_$hash, domain.hashCode);
    _$hash = $jc(_$hash, clientId.hashCode);
    _$hash = $jc(_$hash, clientSecret.hashCode);
    _$hash = $jc(_$hash, accessToken.hashCode);
    _$hash = $jc(_$hash, refreshToken.hashCode);
    _$hash = $jc(_$hash, user.hashCode);
    _$hash = $jc(_$hash, selectedStudentId.hashCode);
    _$hash = $jc(_$hash, canMasquerade.hashCode);
    _$hash = $jc(_$hash, masqueradeUser.hashCode);
    _$hash = $jc(_$hash, masqueradeDomain.hashCode);
    _$hash = $jc(_$hash, isMasqueradingFromQRCode.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Login')
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
  _$Login? _$v;

  String? _uuid;
  String? get uuid => _$this._uuid;
  set uuid(String? uuid) => _$this._uuid = uuid;

  String? _domain;
  String? get domain => _$this._domain;
  set domain(String? domain) => _$this._domain = domain;

  String? _clientId;
  String? get clientId => _$this._clientId;
  set clientId(String? clientId) => _$this._clientId = clientId;

  String? _clientSecret;
  String? get clientSecret => _$this._clientSecret;
  set clientSecret(String? clientSecret) => _$this._clientSecret = clientSecret;

  String? _accessToken;
  String? get accessToken => _$this._accessToken;
  set accessToken(String? accessToken) => _$this._accessToken = accessToken;

  String? _refreshToken;
  String? get refreshToken => _$this._refreshToken;
  set refreshToken(String? refreshToken) => _$this._refreshToken = refreshToken;

  UserBuilder? _user;
  UserBuilder get user => _$this._user ??= new UserBuilder();
  set user(UserBuilder? user) => _$this._user = user;

  String? _selectedStudentId;
  String? get selectedStudentId => _$this._selectedStudentId;
  set selectedStudentId(String? selectedStudentId) =>
      _$this._selectedStudentId = selectedStudentId;

  bool? _canMasquerade;
  bool? get canMasquerade => _$this._canMasquerade;
  set canMasquerade(bool? canMasquerade) =>
      _$this._canMasquerade = canMasquerade;

  UserBuilder? _masqueradeUser;
  UserBuilder get masqueradeUser =>
      _$this._masqueradeUser ??= new UserBuilder();
  set masqueradeUser(UserBuilder? masqueradeUser) =>
      _$this._masqueradeUser = masqueradeUser;

  String? _masqueradeDomain;
  String? get masqueradeDomain => _$this._masqueradeDomain;
  set masqueradeDomain(String? masqueradeDomain) =>
      _$this._masqueradeDomain = masqueradeDomain;

  bool? _isMasqueradingFromQRCode;
  bool? get isMasqueradingFromQRCode => _$this._isMasqueradingFromQRCode;
  set isMasqueradingFromQRCode(bool? isMasqueradingFromQRCode) =>
      _$this._isMasqueradingFromQRCode = isMasqueradingFromQRCode;

  LoginBuilder() {
    Login._initializeBuilder(this);
  }

  LoginBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _uuid = $v.uuid;
      _domain = $v.domain;
      _clientId = $v.clientId;
      _clientSecret = $v.clientSecret;
      _accessToken = $v.accessToken;
      _refreshToken = $v.refreshToken;
      _user = $v.user.toBuilder();
      _selectedStudentId = $v.selectedStudentId;
      _canMasquerade = $v.canMasquerade;
      _masqueradeUser = $v.masqueradeUser?.toBuilder();
      _masqueradeDomain = $v.masqueradeDomain;
      _isMasqueradingFromQRCode = $v.isMasqueradingFromQRCode;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Login other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Login;
  }

  @override
  void update(void Function(LoginBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Login build() => _build();

  _$Login _build() {
    _$Login _$result;
    try {
      _$result = _$v ??
          new _$Login._(
              uuid:
                  BuiltValueNullFieldError.checkNotNull(uuid, r'Login', 'uuid'),
              domain: BuiltValueNullFieldError.checkNotNull(
                  domain, r'Login', 'domain'),
              clientId: clientId,
              clientSecret: clientSecret,
              accessToken: BuiltValueNullFieldError.checkNotNull(
                  accessToken, r'Login', 'accessToken'),
              refreshToken: BuiltValueNullFieldError.checkNotNull(
                  refreshToken, r'Login', 'refreshToken'),
              user: user.build(),
              selectedStudentId: selectedStudentId,
              canMasquerade: canMasquerade,
              masqueradeUser: _masqueradeUser?.build(),
              masqueradeDomain: masqueradeDomain,
              isMasqueradingFromQRCode: isMasqueradingFromQRCode);
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'user';
        user.build();

        _$failedField = 'masqueradeUser';
        _masqueradeUser?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'Login', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
