// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'post_user.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<PostUser> _$postUserSerializer = new _$PostUserSerializer();

class _$PostUserSerializer implements StructuredSerializer<PostUser> {
  @override
  final Iterable<Type> types = const [PostUser, _$PostUser];
  @override
  final String wireName = 'PostUser';

  @override
  Iterable<Object?> serialize(Serializers serializers, PostUser object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'initial_enrollment_type',
      serializers.serialize(object.initialEnrollmentType,
          specifiedType: const FullType(String)),
      'terms_of_use',
      serializers.serialize(object.termsOfUse,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  PostUser deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PostUserBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'initial_enrollment_type':
          result.initialEnrollmentType = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'terms_of_use':
          result.termsOfUse = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$PostUser extends PostUser {
  @override
  final String name;
  @override
  final String initialEnrollmentType;
  @override
  final bool termsOfUse;

  factory _$PostUser([void Function(PostUserBuilder)? updates]) =>
      (new PostUserBuilder()..update(updates))._build();

  _$PostUser._(
      {required this.name,
      required this.initialEnrollmentType,
      required this.termsOfUse})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(name, r'PostUser', 'name');
    BuiltValueNullFieldError.checkNotNull(
        initialEnrollmentType, r'PostUser', 'initialEnrollmentType');
    BuiltValueNullFieldError.checkNotNull(
        termsOfUse, r'PostUser', 'termsOfUse');
  }

  @override
  PostUser rebuild(void Function(PostUserBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PostUserBuilder toBuilder() => new PostUserBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PostUser &&
        name == other.name &&
        initialEnrollmentType == other.initialEnrollmentType &&
        termsOfUse == other.termsOfUse;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, initialEnrollmentType.hashCode);
    _$hash = $jc(_$hash, termsOfUse.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PostUser')
          ..add('name', name)
          ..add('initialEnrollmentType', initialEnrollmentType)
          ..add('termsOfUse', termsOfUse))
        .toString();
  }
}

class PostUserBuilder implements Builder<PostUser, PostUserBuilder> {
  _$PostUser? _$v;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _initialEnrollmentType;
  String? get initialEnrollmentType => _$this._initialEnrollmentType;
  set initialEnrollmentType(String? initialEnrollmentType) =>
      _$this._initialEnrollmentType = initialEnrollmentType;

  bool? _termsOfUse;
  bool? get termsOfUse => _$this._termsOfUse;
  set termsOfUse(bool? termsOfUse) => _$this._termsOfUse = termsOfUse;

  PostUserBuilder();

  PostUserBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _name = $v.name;
      _initialEnrollmentType = $v.initialEnrollmentType;
      _termsOfUse = $v.termsOfUse;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PostUser other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$PostUser;
  }

  @override
  void update(void Function(PostUserBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PostUser build() => _build();

  _$PostUser _build() {
    final _$result = _$v ??
        new _$PostUser._(
            name: BuiltValueNullFieldError.checkNotNull(
                name, r'PostUser', 'name'),
            initialEnrollmentType: BuiltValueNullFieldError.checkNotNull(
                initialEnrollmentType, r'PostUser', 'initialEnrollmentType'),
            termsOfUse: BuiltValueNullFieldError.checkNotNull(
                termsOfUse, r'PostUser', 'termsOfUse'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
