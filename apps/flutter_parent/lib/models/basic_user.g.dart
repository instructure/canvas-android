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
  Iterable<Object> serialize(Serializers serializers, BasicUser object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
    ];
    result.add('name');
    if (object.name == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.name,
          specifiedType: const FullType(String)));
    }
    result.add('pronouns');
    if (object.pronouns == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.pronouns,
          specifiedType: const FullType(String)));
    }
    result.add('avatar_url');
    if (object.avatarUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.avatarUrl,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  BasicUser deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new BasicUserBuilder();

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
        case 'pronouns':
          result.pronouns = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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
  final String name;
  @override
  final String pronouns;
  @override
  final String avatarUrl;

  factory _$BasicUser([void Function(BasicUserBuilder) updates]) =>
      (new BasicUserBuilder()..update(updates)).build();

  _$BasicUser._({this.id, this.name, this.pronouns, this.avatarUrl})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('BasicUser', 'id');
    }
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
    return $jf($jc(
        $jc($jc($jc(0, id.hashCode), name.hashCode), pronouns.hashCode),
        avatarUrl.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('BasicUser')
          ..add('id', id)
          ..add('name', name)
          ..add('pronouns', pronouns)
          ..add('avatarUrl', avatarUrl))
        .toString();
  }
}

class BasicUserBuilder implements Builder<BasicUser, BasicUserBuilder> {
  _$BasicUser _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _pronouns;
  String get pronouns => _$this._pronouns;
  set pronouns(String pronouns) => _$this._pronouns = pronouns;

  String _avatarUrl;
  String get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String avatarUrl) => _$this._avatarUrl = avatarUrl;

  BasicUserBuilder() {
    BasicUser._initializeBuilder(this);
  }

  BasicUserBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _name = _$v.name;
      _pronouns = _$v.pronouns;
      _avatarUrl = _$v.avatarUrl;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(BasicUser other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$BasicUser;
  }

  @override
  void update(void Function(BasicUserBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$BasicUser build() {
    final _$result = _$v ??
        new _$BasicUser._(
            id: id, name: name, pronouns: pronouns, avatarUrl: avatarUrl);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
