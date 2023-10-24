// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'post_pseudonym.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<PostPseudonym> _$postPseudonymSerializer =
    new _$PostPseudonymSerializer();

class _$PostPseudonymSerializer implements StructuredSerializer<PostPseudonym> {
  @override
  final Iterable<Type> types = const [PostPseudonym, _$PostPseudonym];
  @override
  final String wireName = 'PostPseudonym';

  @override
  Iterable<Object?> serialize(Serializers serializers, PostPseudonym object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'unique_id',
      serializers.serialize(object.uniqueId,
          specifiedType: const FullType(String)),
      'password',
      serializers.serialize(object.password,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  PostPseudonym deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PostPseudonymBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'unique_id':
          result.uniqueId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'password':
          result.password = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
      }
    }

    return result.build();
  }
}

class _$PostPseudonym extends PostPseudonym {
  @override
  final String uniqueId;
  @override
  final String password;

  factory _$PostPseudonym([void Function(PostPseudonymBuilder)? updates]) =>
      (new PostPseudonymBuilder()..update(updates))._build();

  _$PostPseudonym._({required this.uniqueId, required this.password})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        uniqueId, r'PostPseudonym', 'uniqueId');
    BuiltValueNullFieldError.checkNotNull(
        password, r'PostPseudonym', 'password');
  }

  @override
  PostPseudonym rebuild(void Function(PostPseudonymBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PostPseudonymBuilder toBuilder() => new PostPseudonymBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PostPseudonym &&
        uniqueId == other.uniqueId &&
        password == other.password;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, uniqueId.hashCode);
    _$hash = $jc(_$hash, password.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PostPseudonym')
          ..add('uniqueId', uniqueId)
          ..add('password', password))
        .toString();
  }
}

class PostPseudonymBuilder
    implements Builder<PostPseudonym, PostPseudonymBuilder> {
  _$PostPseudonym? _$v;

  String? _uniqueId;
  String? get uniqueId => _$this._uniqueId;
  set uniqueId(String? uniqueId) => _$this._uniqueId = uniqueId;

  String? _password;
  String? get password => _$this._password;
  set password(String? password) => _$this._password = password;

  PostPseudonymBuilder();

  PostPseudonymBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _uniqueId = $v.uniqueId;
      _password = $v.password;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PostPseudonym other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$PostPseudonym;
  }

  @override
  void update(void Function(PostPseudonymBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PostPseudonym build() => _build();

  _$PostPseudonym _build() {
    final _$result = _$v ??
        new _$PostPseudonym._(
            uniqueId: BuiltValueNullFieldError.checkNotNull(
                uniqueId, r'PostPseudonym', 'uniqueId'),
            password: BuiltValueNullFieldError.checkNotNull(
                password, r'PostPseudonym', 'password'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
