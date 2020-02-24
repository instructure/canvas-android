// GENERATED CODE - DO NOT MODIFY BY HAND

part of seeded_user;

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
  Iterable<Object> serialize(Serializers serializers, SeededUser object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
    result.add('terms_of_use');
    if (object.termsOfUse == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.termsOfUse,
          specifiedType: const FullType(bool)));
    }
    result.add('login_id');
    if (object.loginId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.loginId,
          specifiedType: const FullType(String)));
    }
    result.add('password');
    if (object.password == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.password,
          specifiedType: const FullType(String)));
    }
    result.add('avatar_url');
    if (object.avatarUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.avatarUrl,
          specifiedType: const FullType(String)));
    }
    result.add('token');
    if (object.token == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.token,
          specifiedType: const FullType(String)));
    }
    result.add('domain');
    if (object.domain == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.domain,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  SeededUser deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SeededUserBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'short_name':
          result.shortName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'sortable_name':
          result.sortableName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'terms_of_use':
          result.termsOfUse = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'login_id':
          result.loginId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'password':
          result.password = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'token':
          result.token = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'domain':
          result.domain = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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
  final bool termsOfUse;
  @override
  final String loginId;
  @override
  final String password;
  @override
  final String avatarUrl;
  @override
  final String token;
  @override
  final String domain;

  factory _$SeededUser([void Function(SeededUserBuilder) updates]) =>
      (new SeededUserBuilder()..update(updates)).build();

  _$SeededUser._(
      {this.id,
      this.name,
      this.shortName,
      this.sortableName,
      this.termsOfUse,
      this.loginId,
      this.password,
      this.avatarUrl,
      this.token,
      this.domain})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('SeededUser', 'id');
    }
    if (name == null) {
      throw new BuiltValueNullFieldError('SeededUser', 'name');
    }
    if (shortName == null) {
      throw new BuiltValueNullFieldError('SeededUser', 'shortName');
    }
    if (sortableName == null) {
      throw new BuiltValueNullFieldError('SeededUser', 'sortableName');
    }
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
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc(
                                $jc($jc($jc(0, id.hashCode), name.hashCode),
                                    shortName.hashCode),
                                sortableName.hashCode),
                            termsOfUse.hashCode),
                        loginId.hashCode),
                    password.hashCode),
                avatarUrl.hashCode),
            token.hashCode),
        domain.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('SeededUser')
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
  _$SeededUser _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _shortName;
  String get shortName => _$this._shortName;
  set shortName(String shortName) => _$this._shortName = shortName;

  String _sortableName;
  String get sortableName => _$this._sortableName;
  set sortableName(String sortableName) => _$this._sortableName = sortableName;

  bool _termsOfUse;
  bool get termsOfUse => _$this._termsOfUse;
  set termsOfUse(bool termsOfUse) => _$this._termsOfUse = termsOfUse;

  String _loginId;
  String get loginId => _$this._loginId;
  set loginId(String loginId) => _$this._loginId = loginId;

  String _password;
  String get password => _$this._password;
  set password(String password) => _$this._password = password;

  String _avatarUrl;
  String get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String avatarUrl) => _$this._avatarUrl = avatarUrl;

  String _token;
  String get token => _$this._token;
  set token(String token) => _$this._token = token;

  String _domain;
  String get domain => _$this._domain;
  set domain(String domain) => _$this._domain = domain;

  SeededUserBuilder() {
    SeededUser._initializeBuilder(this);
  }

  SeededUserBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _name = _$v.name;
      _shortName = _$v.shortName;
      _sortableName = _$v.sortableName;
      _termsOfUse = _$v.termsOfUse;
      _loginId = _$v.loginId;
      _password = _$v.password;
      _avatarUrl = _$v.avatarUrl;
      _token = _$v.token;
      _domain = _$v.domain;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SeededUser other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$SeededUser;
  }

  @override
  void update(void Function(SeededUserBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$SeededUser build() {
    final _$result = _$v ??
        new _$SeededUser._(
            id: id,
            name: name,
            shortName: shortName,
            sortableName: sortableName,
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

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
