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
  Iterable<Object> serialize(Serializers serializers, PostPairingCode object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'code',
      serializers.serialize(object.code, specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  PostPairingCode deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PostPairingCodeBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      switch (key) {
        case 'code':
          result.code = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$PostPairingCode extends PostPairingCode {
  @override
  final String code;

  factory _$PostPairingCode([void Function(PostPairingCodeBuilder) updates]) =>
      (new PostPairingCodeBuilder()..update(updates)).build();

  _$PostPairingCode._({this.code}) : super._() {
    if (code == null) {
      throw new BuiltValueNullFieldError('PostPairingCode', 'code');
    }
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
    return $jf($jc(0, code.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('PostPairingCode')..add('code', code))
        .toString();
  }
}

class PostPairingCodeBuilder
    implements Builder<PostPairingCode, PostPairingCodeBuilder> {
  _$PostPairingCode _$v;

  String _code;
  String get code => _$this._code;
  set code(String code) => _$this._code = code;

  PostPairingCodeBuilder();

  PostPairingCodeBuilder get _$this {
    if (_$v != null) {
      _code = _$v.code;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PostPairingCode other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$PostPairingCode;
  }

  @override
  void update(void Function(PostPairingCodeBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$PostPairingCode build() {
    final _$result = _$v ?? new _$PostPairingCode._(code: code);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
