// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'post_pairing_code.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<PostPairingCode> _$postPairingCodeSerializer =
    new _$PostPairingCodeSerializer();

class _$PostPairingCodeSerializer
    implements StructuredSerializer<PostPairingCode> {
  @override
  final Iterable<Type> types = const [PostPairingCode, _$PostPairingCode];
  @override
  final String wireName = 'PostPairingCode';

  @override
  Iterable<Object?> serialize(Serializers serializers, PostPairingCode object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'code',
      serializers.serialize(object.code, specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  PostPairingCode deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PostPairingCodeBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'code':
          result.code = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
      }
    }

    return result.build();
  }
}

class _$PostPairingCode extends PostPairingCode {
  @override
  final String code;

  factory _$PostPairingCode([void Function(PostPairingCodeBuilder)? updates]) =>
      (new PostPairingCodeBuilder()..update(updates))._build();

  _$PostPairingCode._({required this.code}) : super._() {
    BuiltValueNullFieldError.checkNotNull(code, r'PostPairingCode', 'code');
  }

  @override
  PostPairingCode rebuild(void Function(PostPairingCodeBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PostPairingCodeBuilder toBuilder() =>
      new PostPairingCodeBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PostPairingCode && code == other.code;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, code.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PostPairingCode')..add('code', code))
        .toString();
  }
}

class PostPairingCodeBuilder
    implements Builder<PostPairingCode, PostPairingCodeBuilder> {
  _$PostPairingCode? _$v;

  String? _code;
  String? get code => _$this._code;
  set code(String? code) => _$this._code = code;

  PostPairingCodeBuilder();

  PostPairingCodeBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _code = $v.code;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PostPairingCode other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$PostPairingCode;
  }

  @override
  void update(void Function(PostPairingCodeBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PostPairingCode build() => _build();

  _$PostPairingCode _build() {
    final _$result = _$v ??
        new _$PostPairingCode._(
            code: BuiltValueNullFieldError.checkNotNull(
                code, r'PostPairingCode', 'code'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
