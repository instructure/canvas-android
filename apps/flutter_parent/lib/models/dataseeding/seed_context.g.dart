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
  Iterable<Object> serialize(Serializers serializers, SeedContext object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
  SeedContext deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SeedContextBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'seedingComplete':
          result.seedingComplete = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'seedObjects':
          result.seedObjects.replace(serializers.deserialize(value,
              specifiedType: const FullType(BuiltMap,
                  const [const FullType(String), const FullType(String)])));
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

  factory _$SeedContext([void Function(SeedContextBuilder) updates]) =>
      (new SeedContextBuilder()..update(updates)).build();

  _$SeedContext._({this.seedingComplete, this.seedObjects}) : super._() {
    if (seedingComplete == null) {
      throw new BuiltValueNullFieldError('SeedContext', 'seedingComplete');
    }
    if (seedObjects == null) {
      throw new BuiltValueNullFieldError('SeedContext', 'seedObjects');
    }
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
    return $jf($jc($jc(0, seedingComplete.hashCode), seedObjects.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('SeedContext')
          ..add('seedingComplete', seedingComplete)
          ..add('seedObjects', seedObjects))
        .toString();
  }
}

class SeedContextBuilder implements Builder<SeedContext, SeedContextBuilder> {
  _$SeedContext _$v;

  bool _seedingComplete;
  bool get seedingComplete => _$this._seedingComplete;
  set seedingComplete(bool seedingComplete) =>
      _$this._seedingComplete = seedingComplete;

  MapBuilder<String, String> _seedObjects;
  MapBuilder<String, String> get seedObjects =>
      _$this._seedObjects ??= new MapBuilder<String, String>();
  set seedObjects(MapBuilder<String, String> seedObjects) =>
      _$this._seedObjects = seedObjects;

  SeedContextBuilder() {
    SeedContext._initializeBuilder(this);
  }

  SeedContextBuilder get _$this {
    if (_$v != null) {
      _seedingComplete = _$v.seedingComplete;
      _seedObjects = _$v.seedObjects?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SeedContext other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$SeedContext;
  }

  @override
  void update(void Function(SeedContextBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$SeedContext build() {
    _$SeedContext _$result;
    try {
      _$result = _$v ??
          new _$SeedContext._(
              seedingComplete: seedingComplete,
              seedObjects: seedObjects.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'seedObjects';
        seedObjects.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'SeedContext', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
