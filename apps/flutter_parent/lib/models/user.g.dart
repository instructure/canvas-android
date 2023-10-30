// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<User> _$userSerializer = new _$UserSerializer();
Serializer<UserPermission> _$userPermissionSerializer =
    new _$UserPermissionSerializer();

class _$UserSerializer implements StructuredSerializer<User> {
  @override
  final Iterable<Type> types = const [User, _$User];
  @override
  final String wireName = 'User';

  @override
  Iterable<Object?> serialize(Serializers serializers, User object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.sortableName;

    result
      ..add('sortable_name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.shortName;

    result
      ..add('short_name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.pronouns;

    result
      ..add('pronouns')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.avatarUrl;

    result
      ..add('avatar_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.primaryEmail;

    result
      ..add('primary_email')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.locale;

    result
      ..add('locale')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.effectiveLocale;

    result
      ..add('effective_locale')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.permissions;

    result
      ..add('permissions')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(UserPermission)));
    value = object.loginId;

    result
      ..add('login_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  User deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UserBuilder();

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
        case 'sortable_name':
          result.sortableName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'short_name':
          result.shortName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'pronouns':
          result.pronouns = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'primary_email':
          result.primaryEmail = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'locale':
          result.locale = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'effective_locale':
          result.effectiveLocale = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'permissions':
          result.permissions.replace(serializers.deserialize(value,
                  specifiedType: const FullType(UserPermission))!
              as UserPermission);
          break;
        case 'login_id':
          result.loginId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$UserPermissionSerializer
    implements StructuredSerializer<UserPermission> {
  @override
  final Iterable<Type> types = const [UserPermission, _$UserPermission];
  @override
  final String wireName = 'UserPermission';

  @override
  Iterable<Object?> serialize(Serializers serializers, UserPermission object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'become_user',
      serializers.serialize(object.become_user,
          specifiedType: const FullType(bool)),
      'can_update_name',
      serializers.serialize(object.canUpdateName,
          specifiedType: const FullType(bool)),
      'can_update_avatar',
      serializers.serialize(object.canUpdateAvatar,
          specifiedType: const FullType(bool)),
      'limit_parent_app_web_access',
      serializers.serialize(object.limitParentAppWebAccess,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  UserPermission deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UserPermissionBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'become_user':
          result.become_user = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'can_update_name':
          result.canUpdateName = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'can_update_avatar':
          result.canUpdateAvatar = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'limit_parent_app_web_access':
          result.limitParentAppWebAccess = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$User extends User {
  @override
  final String id;
  @override
  final String name;
  @override
  final String? sortableName;
  @override
  final String? shortName;
  @override
  final String? pronouns;
  @override
  final String? avatarUrl;
  @override
  final String? primaryEmail;
  @override
  final String? locale;
  @override
  final String? effectiveLocale;
  @override
  final UserPermission? permissions;
  @override
  final String? loginId;

  factory _$User([void Function(UserBuilder)? updates]) =>
      (new UserBuilder()..update(updates))._build();

  _$User._(
      {required this.id,
      required this.name,
      this.sortableName,
      this.shortName,
      this.pronouns,
      this.avatarUrl,
      this.primaryEmail,
      this.locale,
      this.effectiveLocale,
      this.permissions,
      this.loginId})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'User', 'id');
    BuiltValueNullFieldError.checkNotNull(name, r'User', 'name');
  }

  @override
  User rebuild(void Function(UserBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  UserBuilder toBuilder() => new UserBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is User &&
        id == other.id &&
        name == other.name &&
        sortableName == other.sortableName &&
        shortName == other.shortName &&
        pronouns == other.pronouns &&
        avatarUrl == other.avatarUrl &&
        primaryEmail == other.primaryEmail &&
        locale == other.locale &&
        effectiveLocale == other.effectiveLocale &&
        permissions == other.permissions &&
        loginId == other.loginId;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, sortableName.hashCode);
    _$hash = $jc(_$hash, shortName.hashCode);
    _$hash = $jc(_$hash, pronouns.hashCode);
    _$hash = $jc(_$hash, avatarUrl.hashCode);
    _$hash = $jc(_$hash, primaryEmail.hashCode);
    _$hash = $jc(_$hash, locale.hashCode);
    _$hash = $jc(_$hash, effectiveLocale.hashCode);
    _$hash = $jc(_$hash, permissions.hashCode);
    _$hash = $jc(_$hash, loginId.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'User')
          ..add('id', id)
          ..add('name', name)
          ..add('sortableName', sortableName)
          ..add('shortName', shortName)
          ..add('pronouns', pronouns)
          ..add('avatarUrl', avatarUrl)
          ..add('primaryEmail', primaryEmail)
          ..add('locale', locale)
          ..add('effectiveLocale', effectiveLocale)
          ..add('permissions', permissions)
          ..add('loginId', loginId))
        .toString();
  }
}

class UserBuilder implements Builder<User, UserBuilder> {
  _$User? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _sortableName;
  String? get sortableName => _$this._sortableName;
  set sortableName(String? sortableName) => _$this._sortableName = sortableName;

  String? _shortName;
  String? get shortName => _$this._shortName;
  set shortName(String? shortName) => _$this._shortName = shortName;

  String? _pronouns;
  String? get pronouns => _$this._pronouns;
  set pronouns(String? pronouns) => _$this._pronouns = pronouns;

  String? _avatarUrl;
  String? get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String? avatarUrl) => _$this._avatarUrl = avatarUrl;

  String? _primaryEmail;
  String? get primaryEmail => _$this._primaryEmail;
  set primaryEmail(String? primaryEmail) => _$this._primaryEmail = primaryEmail;

  String? _locale;
  String? get locale => _$this._locale;
  set locale(String? locale) => _$this._locale = locale;

  String? _effectiveLocale;
  String? get effectiveLocale => _$this._effectiveLocale;
  set effectiveLocale(String? effectiveLocale) =>
      _$this._effectiveLocale = effectiveLocale;

  UserPermissionBuilder? _permissions;
  UserPermissionBuilder get permissions =>
      _$this._permissions ??= new UserPermissionBuilder();
  set permissions(UserPermissionBuilder? permissions) =>
      _$this._permissions = permissions;

  String? _loginId;
  String? get loginId => _$this._loginId;
  set loginId(String? loginId) => _$this._loginId = loginId;

  UserBuilder() {
    User._initializeBuilder(this);
  }

  UserBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _name = $v.name;
      _sortableName = $v.sortableName;
      _shortName = $v.shortName;
      _pronouns = $v.pronouns;
      _avatarUrl = $v.avatarUrl;
      _primaryEmail = $v.primaryEmail;
      _locale = $v.locale;
      _effectiveLocale = $v.effectiveLocale;
      _permissions = $v.permissions?.toBuilder();
      _loginId = $v.loginId;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(User other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$User;
  }

  @override
  void update(void Function(UserBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  User build() => _build();

  _$User _build() {
    _$User _$result;
    try {
      _$result = _$v ??
          new _$User._(
              id: BuiltValueNullFieldError.checkNotNull(id, r'User', 'id'),
              name:
                  BuiltValueNullFieldError.checkNotNull(name, r'User', 'name'),
              sortableName: sortableName,
              shortName: shortName,
              pronouns: pronouns,
              avatarUrl: avatarUrl,
              primaryEmail: primaryEmail,
              locale: locale,
              effectiveLocale: effectiveLocale,
              permissions: _permissions?.build(),
              loginId: loginId);
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'permissions';
        _permissions?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'User', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

class _$UserPermission extends UserPermission {
  @override
  final bool become_user;
  @override
  final bool canUpdateName;
  @override
  final bool canUpdateAvatar;
  @override
  final bool limitParentAppWebAccess;

  factory _$UserPermission([void Function(UserPermissionBuilder)? updates]) =>
      (new UserPermissionBuilder()..update(updates))._build();

  _$UserPermission._(
      {required this.become_user,
      required this.canUpdateName,
      required this.canUpdateAvatar,
      required this.limitParentAppWebAccess})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        become_user, r'UserPermission', 'become_user');
    BuiltValueNullFieldError.checkNotNull(
        canUpdateName, r'UserPermission', 'canUpdateName');
    BuiltValueNullFieldError.checkNotNull(
        canUpdateAvatar, r'UserPermission', 'canUpdateAvatar');
    BuiltValueNullFieldError.checkNotNull(
        limitParentAppWebAccess, r'UserPermission', 'limitParentAppWebAccess');
  }

  @override
  UserPermission rebuild(void Function(UserPermissionBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  UserPermissionBuilder toBuilder() =>
      new UserPermissionBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is UserPermission &&
        become_user == other.become_user &&
        canUpdateName == other.canUpdateName &&
        canUpdateAvatar == other.canUpdateAvatar &&
        limitParentAppWebAccess == other.limitParentAppWebAccess;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, become_user.hashCode);
    _$hash = $jc(_$hash, canUpdateName.hashCode);
    _$hash = $jc(_$hash, canUpdateAvatar.hashCode);
    _$hash = $jc(_$hash, limitParentAppWebAccess.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'UserPermission')
          ..add('become_user', become_user)
          ..add('canUpdateName', canUpdateName)
          ..add('canUpdateAvatar', canUpdateAvatar)
          ..add('limitParentAppWebAccess', limitParentAppWebAccess))
        .toString();
  }
}

class UserPermissionBuilder
    implements Builder<UserPermission, UserPermissionBuilder> {
  _$UserPermission? _$v;

  bool? _become_user;
  bool? get become_user => _$this._become_user;
  set become_user(bool? become_user) => _$this._become_user = become_user;

  bool? _canUpdateName;
  bool? get canUpdateName => _$this._canUpdateName;
  set canUpdateName(bool? canUpdateName) =>
      _$this._canUpdateName = canUpdateName;

  bool? _canUpdateAvatar;
  bool? get canUpdateAvatar => _$this._canUpdateAvatar;
  set canUpdateAvatar(bool? canUpdateAvatar) =>
      _$this._canUpdateAvatar = canUpdateAvatar;

  bool? _limitParentAppWebAccess;
  bool? get limitParentAppWebAccess => _$this._limitParentAppWebAccess;
  set limitParentAppWebAccess(bool? limitParentAppWebAccess) =>
      _$this._limitParentAppWebAccess = limitParentAppWebAccess;

  UserPermissionBuilder() {
    UserPermission._initializeBuilder(this);
  }

  UserPermissionBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _become_user = $v.become_user;
      _canUpdateName = $v.canUpdateName;
      _canUpdateAvatar = $v.canUpdateAvatar;
      _limitParentAppWebAccess = $v.limitParentAppWebAccess;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UserPermission other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$UserPermission;
  }

  @override
  void update(void Function(UserPermissionBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  UserPermission build() => _build();

  _$UserPermission _build() {
    final _$result = _$v ??
        new _$UserPermission._(
            become_user: BuiltValueNullFieldError.checkNotNull(
                become_user, r'UserPermission', 'become_user'),
            canUpdateName: BuiltValueNullFieldError.checkNotNull(
                canUpdateName, r'UserPermission', 'canUpdateName'),
            canUpdateAvatar: BuiltValueNullFieldError.checkNotNull(
                canUpdateAvatar, r'UserPermission', 'canUpdateAvatar'),
            limitParentAppWebAccess: BuiltValueNullFieldError.checkNotNull(
                limitParentAppWebAccess,
                r'UserPermission',
                'limitParentAppWebAccess'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
