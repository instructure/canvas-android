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
  Iterable<Object> serialize(Serializers serializers, PostUser object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
  PostUser deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PostUserBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      switch (key) {
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'initial_enrollment_type':
          result.initialEnrollmentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'terms_of_use':
          result.termsOfUse = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
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

  factory _$PostUser([void Function(PostUserBuilder) updates]) =>
      (new PostUserBuilder()..update(updates)).build();

  _$PostUser._({this.name, this.initialEnrollmentType, this.termsOfUse})
      : super._() {
    if (name == null) {
      throw new BuiltValueNullFieldError('PostUser', 'name');
    }
    if (initialEnrollmentType == null) {
      throw new BuiltValueNullFieldError('PostUser', 'initialEnrollmentType');
    }
    if (termsOfUse == null) {
      throw new BuiltValueNullFieldError('PostUser', 'termsOfUse');
    }
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
    return $jf($jc($jc($jc(0, name.hashCode), initialEnrollmentType.hashCode),
        termsOfUse.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('PostUser')
          ..add('name', name)
          ..add('initialEnrollmentType', initialEnrollmentType)
          ..add('termsOfUse', termsOfUse))
        .toString();
  }
}

class PostUserBuilder implements Builder<PostUser, PostUserBuilder> {
  _$PostUser _$v;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _initialEnrollmentType;
  String get initialEnrollmentType => _$this._initialEnrollmentType;
  set initialEnrollmentType(String initialEnrollmentType) =>
      _$this._initialEnrollmentType = initialEnrollmentType;

  bool _termsOfUse;
  bool get termsOfUse => _$this._termsOfUse;
  set termsOfUse(bool termsOfUse) => _$this._termsOfUse = termsOfUse;

  PostUserBuilder();

  PostUserBuilder get _$this {
    if (_$v != null) {
      _name = _$v.name;
      _initialEnrollmentType = _$v.initialEnrollmentType;
      _termsOfUse = _$v.termsOfUse;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PostUser other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$PostUser;
  }

  @override
  void update(void Function(PostUserBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$PostUser build() {
    final _$result = _$v ??
        new _$PostUser._(
            name: name,
            initialEnrollmentType: initialEnrollmentType,
            termsOfUse: termsOfUse);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
