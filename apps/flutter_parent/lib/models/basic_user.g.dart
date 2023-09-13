// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'basic_user.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<BasicUser> _$basicUserSerializer = new _$BasicUserSerializer();

class _$BasicUserSerializer implements StructuredSerializer<BasicUser> {
  @override
  final Iterable<Type> types = const [BasicUser, _$BasicUser];
  @override
  final String wireName = 'BasicUser';

  @override
  Iterable<Object?> serialize(Serializers serializers, BasicUser object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.name;

    result
      ..add('name')
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

    return result;
  }

  @override
  BasicUser deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new BasicUserBuilder();

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
      }
    }

    return result.build();
  }
}

class _$BasicUser extends BasicUser {
  @override
  final String id;
  @override
  final String? name;
  @override
  final String? pronouns;
  @override
  final String? avatarUrl;

  factory _$BasicUser([void Function(BasicUserBuilder)? updates]) =>
      (new BasicUserBuilder()..update(updates))._build();

  _$BasicUser._({required this.id, this.name, this.pronouns, this.avatarUrl})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'BasicUser', 'id');
  }

  @override
  BasicUser rebuild(void Function(BasicUserBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  BasicUserBuilder toBuilder() => new BasicUserBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is BasicUser &&
        id == other.id &&
        name == other.name &&
        pronouns == other.pronouns &&
        avatarUrl == other.avatarUrl;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, pronouns.hashCode);
    _$hash = $jc(_$hash, avatarUrl.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'BasicUser')
          ..add('id', id)
          ..add('name', name)
          ..add('pronouns', pronouns)
          ..add('avatarUrl', avatarUrl))
        .toString();
  }
}

class BasicUserBuilder implements Builder<BasicUser, BasicUserBuilder> {
  _$BasicUser? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _pronouns;
  String? get pronouns => _$this._pronouns;
  set pronouns(String? pronouns) => _$this._pronouns = pronouns;

  String? _avatarUrl;
  String? get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String? avatarUrl) => _$this._avatarUrl = avatarUrl;

  BasicUserBuilder() {
    BasicUser._initializeBuilder(this);
  }

  BasicUserBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _name = $v.name;
      _pronouns = $v.pronouns;
      _avatarUrl = $v.avatarUrl;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(BasicUser other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$BasicUser;
  }

  @override
  void update(void Function(BasicUserBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  BasicUser build() => _build();

  _$BasicUser _build() {
    final _$result = _$v ??
        new _$BasicUser._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'BasicUser', 'id'),
            name: name,
            pronouns: pronouns,
            avatarUrl: avatarUrl);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
