// GENERATED CODE - DO NOT MODIFY BY HAND

part of user;

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<User> _$userSerializer = new _$UserSerializer();

class _$UserSerializer implements StructuredSerializer<User> {
  @override
  final Iterable<Type> types = const [User, _$User];
  @override
  final String wireName = 'User';

  @override
  Iterable<Object> serialize(Serializers serializers, User object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(int)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'sortable_name',
      serializers.serialize(object.sortableName,
          specifiedType: const FullType(String)),
      'avatar_url',
      serializers.serialize(object.avatarUrl,
          specifiedType: const FullType(String)),
      'primary_email',
      serializers.serialize(object.primaryEmail,
          specifiedType: const FullType(String)),
      'locale',
      serializers.serialize(object.locale,
          specifiedType: const FullType(String)),
      'effective_locale',
      serializers.serialize(object.effectiveLocale,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  User deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UserBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'sortable_name':
          result.sortableName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'primary_email':
          result.primaryEmail = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'locale':
          result.locale = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'effective_locale':
          result.effectiveLocale = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$User extends User {
  @override
  final int id;
  @override
  final String name;
  @override
  final String sortableName;
  @override
  final String avatarUrl;
  @override
  final String primaryEmail;
  @override
  final String locale;
  @override
  final String effectiveLocale;

  factory _$User([void Function(UserBuilder) updates]) =>
      (new UserBuilder()..update(updates)).build();

  _$User._(
      {this.id,
      this.name,
      this.sortableName,
      this.avatarUrl,
      this.primaryEmail,
      this.locale,
      this.effectiveLocale})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('User', 'id');
    }
    if (name == null) {
      throw new BuiltValueNullFieldError('User', 'name');
    }
    if (sortableName == null) {
      throw new BuiltValueNullFieldError('User', 'sortableName');
    }
    if (avatarUrl == null) {
      throw new BuiltValueNullFieldError('User', 'avatarUrl');
    }
    if (primaryEmail == null) {
      throw new BuiltValueNullFieldError('User', 'primaryEmail');
    }
    if (locale == null) {
      throw new BuiltValueNullFieldError('User', 'locale');
    }
    if (effectiveLocale == null) {
      throw new BuiltValueNullFieldError('User', 'effectiveLocale');
    }
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
        avatarUrl == other.avatarUrl &&
        primaryEmail == other.primaryEmail &&
        locale == other.locale &&
        effectiveLocale == other.effectiveLocale;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc($jc($jc(0, id.hashCode), name.hashCode),
                        sortableName.hashCode),
                    avatarUrl.hashCode),
                primaryEmail.hashCode),
            locale.hashCode),
        effectiveLocale.hashCode));
  }
}

class UserBuilder implements Builder<User, UserBuilder> {
  _$User _$v;

  int _id;
  int get id => _$this._id;
  set id(int id) => _$this._id = id;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _sortableName;
  String get sortableName => _$this._sortableName;
  set sortableName(String sortableName) => _$this._sortableName = sortableName;

  String _avatarUrl;
  String get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String avatarUrl) => _$this._avatarUrl = avatarUrl;

  String _primaryEmail;
  String get primaryEmail => _$this._primaryEmail;
  set primaryEmail(String primaryEmail) => _$this._primaryEmail = primaryEmail;

  String _locale;
  String get locale => _$this._locale;
  set locale(String locale) => _$this._locale = locale;

  String _effectiveLocale;
  String get effectiveLocale => _$this._effectiveLocale;
  set effectiveLocale(String effectiveLocale) =>
      _$this._effectiveLocale = effectiveLocale;

  UserBuilder();

  UserBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _name = _$v.name;
      _sortableName = _$v.sortableName;
      _avatarUrl = _$v.avatarUrl;
      _primaryEmail = _$v.primaryEmail;
      _locale = _$v.locale;
      _effectiveLocale = _$v.effectiveLocale;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(User other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$User;
  }

  @override
  void update(void Function(UserBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$User build() {
    final _$result = _$v ??
        new _$User._(
            id: id,
            name: name,
            sortableName: sortableName,
            avatarUrl: avatarUrl,
            primaryEmail: primaryEmail,
            locale: locale,
            effectiveLocale: effectiveLocale);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
