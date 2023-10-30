// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'seed_context.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<SeedContext> _$seedContextSerializer = new _$SeedContextSerializer();

class _$SeedContextSerializer implements StructuredSerializer<SeedContext> {
  @override
  final Iterable<Type> types = const [SeedContext, _$SeedContext];
  @override
  final String wireName = 'SeedContext';

  @override
  Iterable<Object?> serialize(Serializers serializers, SeedContext object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'seedingComplete',
      serializers.serialize(object.seedingComplete,
          specifiedType: const FullType(bool)),
      'seedObjects',
      serializers.serialize(object.seedObjects,
          specifiedType: const FullType(BuiltMap,
              const [const FullType(String), const FullType(String)])),
    ];

    return result;
  }

  @override
  SeedContext deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SeedContextBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'seedingComplete':
          result.seedingComplete = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'seedObjects':
          result.seedObjects.replace(serializers.deserialize(value,
              specifiedType: const FullType(BuiltMap,
                  const [const FullType(String), const FullType(String)]))!);
          break;
      }
    }

    return result.build();
  }
}

class _$SeedContext extends SeedContext {
  @override
  final bool seedingComplete;
  @override
  final BuiltMap<String, String> seedObjects;

  factory _$SeedContext([void Function(SeedContextBuilder)? updates]) =>
      (new SeedContextBuilder()..update(updates))._build();

  _$SeedContext._({required this.seedingComplete, required this.seedObjects})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        seedingComplete, r'SeedContext', 'seedingComplete');
    BuiltValueNullFieldError.checkNotNull(
        seedObjects, r'SeedContext', 'seedObjects');
  }

  @override
  SeedContext rebuild(void Function(SeedContextBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SeedContextBuilder toBuilder() => new SeedContextBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is SeedContext &&
        seedingComplete == other.seedingComplete &&
        seedObjects == other.seedObjects;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, seedingComplete.hashCode);
    _$hash = $jc(_$hash, seedObjects.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'SeedContext')
          ..add('seedingComplete', seedingComplete)
          ..add('seedObjects', seedObjects))
        .toString();
  }
}

class SeedContextBuilder implements Builder<SeedContext, SeedContextBuilder> {
  _$SeedContext? _$v;

  bool? _seedingComplete;
  bool? get seedingComplete => _$this._seedingComplete;
  set seedingComplete(bool? seedingComplete) =>
      _$this._seedingComplete = seedingComplete;

  MapBuilder<String, String>? _seedObjects;
  MapBuilder<String, String> get seedObjects =>
      _$this._seedObjects ??= new MapBuilder<String, String>();
  set seedObjects(MapBuilder<String, String>? seedObjects) =>
      _$this._seedObjects = seedObjects;

  SeedContextBuilder() {
    SeedContext._initializeBuilder(this);
  }

  SeedContextBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _seedingComplete = $v.seedingComplete;
      _seedObjects = $v.seedObjects.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SeedContext other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$SeedContext;
  }

  @override
  void update(void Function(SeedContextBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  SeedContext build() => _build();

  _$SeedContext _build() {
    _$SeedContext _$result;
    try {
      _$result = _$v ??
          new _$SeedContext._(
              seedingComplete: BuiltValueNullFieldError.checkNotNull(
                  seedingComplete, r'SeedContext', 'seedingComplete'),
              seedObjects: seedObjects.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'seedObjects';
        seedObjects.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'SeedContext', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
