// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'seeded_user.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<SeededUser> _$seededUserSerializer = new _$SeededUserSerializer();

class _$SeededUserSerializer implements StructuredSerializer<SeededUser> {
  @override
  final Iterable<Type> types = const [SeededUser, _$SeededUser];
  @override
  final String wireName = 'SeededUser';

  @override
  Iterable<Object?> serialize(Serializers serializers, SeededUser object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'short_name',
      serializers.serialize(object.shortName,
          specifiedType: const FullType(String)),
      'sortable_name',
      serializers.serialize(object.sortableName,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.termsOfUse;

    result
      ..add('terms_of_use')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));
    value = object.loginId;

    result
      ..add('login_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.password;

    result
      ..add('password')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.avatarUrl;

    result
      ..add('avatar_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.token;

    result
      ..add('token')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.domain;

    result
      ..add('domain')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  SeededUser deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SeededUserBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'short_name':
          result.shortName = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'sortable_name':
          result.sortableName = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'terms_of_use':
          result.termsOfUse = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
          break;
        case 'login_id':
          result.loginId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'password':
          result.password = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'token':
          result.token = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'domain':
          result.domain = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$SeededUser extends SeededUser {
  @override
  final String id;
  @override
  final String name;
  @override
  final String shortName;
  @override
  final String sortableName;
  @override
  final bool? termsOfUse;
  @override
  final String? loginId;
  @override
  final String? password;
  @override
  final String? avatarUrl;
  @override
  final String? token;
  @override
  final String? domain;

  factory _$SeededUser([void Function(SeededUserBuilder)? updates]) =>
      (new SeededUserBuilder()..update(updates))._build();

  _$SeededUser._(
      {required this.id,
      required this.name,
      required this.shortName,
      required this.sortableName,
      this.termsOfUse,
      this.loginId,
      this.password,
      this.avatarUrl,
      this.token,
      this.domain})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'SeededUser', 'id');
    BuiltValueNullFieldError.checkNotNull(name, r'SeededUser', 'name');
    BuiltValueNullFieldError.checkNotNull(
        shortName, r'SeededUser', 'shortName');
    BuiltValueNullFieldError.checkNotNull(
        sortableName, r'SeededUser', 'sortableName');
  }

  @override
  SeededUser rebuild(void Function(SeededUserBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SeededUserBuilder toBuilder() => new SeededUserBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is SeededUser &&
        id == other.id &&
        name == other.name &&
        shortName == other.shortName &&
        sortableName == other.sortableName &&
        termsOfUse == other.termsOfUse &&
        loginId == other.loginId &&
        password == other.password &&
        avatarUrl == other.avatarUrl &&
        token == other.token &&
        domain == other.domain;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, shortName.hashCode);
    _$hash = $jc(_$hash, sortableName.hashCode);
    _$hash = $jc(_$hash, termsOfUse.hashCode);
    _$hash = $jc(_$hash, loginId.hashCode);
    _$hash = $jc(_$hash, password.hashCode);
    _$hash = $jc(_$hash, avatarUrl.hashCode);
    _$hash = $jc(_$hash, token.hashCode);
    _$hash = $jc(_$hash, domain.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'SeededUser')
          ..add('id', id)
          ..add('name', name)
          ..add('shortName', shortName)
          ..add('sortableName', sortableName)
          ..add('termsOfUse', termsOfUse)
          ..add('loginId', loginId)
          ..add('password', password)
          ..add('avatarUrl', avatarUrl)
          ..add('token', token)
          ..add('domain', domain))
        .toString();
  }
}

class SeededUserBuilder implements Builder<SeededUser, SeededUserBuilder> {
  _$SeededUser? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _shortName;
  String? get shortName => _$this._shortName;
  set shortName(String? shortName) => _$this._shortName = shortName;

  String? _sortableName;
  String? get sortableName => _$this._sortableName;
  set sortableName(String? sortableName) => _$this._sortableName = sortableName;

  bool? _termsOfUse;
  bool? get termsOfUse => _$this._termsOfUse;
  set termsOfUse(bool? termsOfUse) => _$this._termsOfUse = termsOfUse;

  String? _loginId;
  String? get loginId => _$this._loginId;
  set loginId(String? loginId) => _$this._loginId = loginId;

  String? _password;
  String? get password => _$this._password;
  set password(String? password) => _$this._password = password;

  String? _avatarUrl;
  String? get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String? avatarUrl) => _$this._avatarUrl = avatarUrl;

  String? _token;
  String? get token => _$this._token;
  set token(String? token) => _$this._token = token;

  String? _domain;
  String? get domain => _$this._domain;
  set domain(String? domain) => _$this._domain = domain;

  SeededUserBuilder() {
    SeededUser._initializeBuilder(this);
  }

  SeededUserBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _name = $v.name;
      _shortName = $v.shortName;
      _sortableName = $v.sortableName;
      _termsOfUse = $v.termsOfUse;
      _loginId = $v.loginId;
      _password = $v.password;
      _avatarUrl = $v.avatarUrl;
      _token = $v.token;
      _domain = $v.domain;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SeededUser other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$SeededUser;
  }

  @override
  void update(void Function(SeededUserBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  SeededUser build() => _build();

  _$SeededUser _build() {
    final _$result = _$v ??
        new _$SeededUser._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'SeededUser', 'id'),
            name: BuiltValueNullFieldError.checkNotNull(
                name, r'SeededUser', 'name'),
            shortName: BuiltValueNullFieldError.checkNotNull(
                shortName, r'SeededUser', 'shortName'),
            sortableName: BuiltValueNullFieldError.checkNotNull(
                sortableName, r'SeededUser', 'sortableName'),
            termsOfUse: termsOfUse,
            loginId: loginId,
            password: password,
            avatarUrl: avatarUrl,
            token: token,
            domain: domain);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
