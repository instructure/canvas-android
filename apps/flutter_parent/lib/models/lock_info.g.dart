// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'lock_info.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<LockInfo> _$lockInfoSerializer = new _$LockInfoSerializer();

class _$LockInfoSerializer implements StructuredSerializer<LockInfo> {
  @override
  final Iterable<Type> types = const [LockInfo, _$LockInfo];
  @override
  final String wireName = 'LockInfo';

  @override
  Iterable<Object?> serialize(Serializers serializers, LockInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[];
    Object? value;
    value = object.contextModule;

    result
      ..add('context_module')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(LockedModule)));
    value = object.unlockAt;

    result
      ..add('unlock_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));

    return result;
  }

  @override
  LockInfo deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new LockInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'context_module':
          result.contextModule.replace(serializers.deserialize(value,
              specifiedType: const FullType(LockedModule))! as LockedModule);
          break;
        case 'unlock_at':
          result.unlockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
      }
    }

    return result.build();
  }
}

class _$LockInfo extends LockInfo {
  @override
  final LockedModule? contextModule;
  @override
  final DateTime? unlockAt;
  @override
  final List<String>? modulePrerequisiteNames;

  factory _$LockInfo([void Function(LockInfoBuilder)? updates]) =>
      (new LockInfoBuilder()..update(updates))._build();

  _$LockInfo._(
      {this.contextModule, this.unlockAt, this.modulePrerequisiteNames})
      : super._();

  @override
  LockInfo rebuild(void Function(LockInfoBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  LockInfoBuilder toBuilder() => new LockInfoBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is LockInfo &&
        contextModule == other.contextModule &&
        unlockAt == other.unlockAt &&
        modulePrerequisiteNames == other.modulePrerequisiteNames;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, contextModule.hashCode);
    _$hash = $jc(_$hash, unlockAt.hashCode);
    _$hash = $jc(_$hash, modulePrerequisiteNames.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'LockInfo')
          ..add('contextModule', contextModule)
          ..add('unlockAt', unlockAt)
          ..add('modulePrerequisiteNames', modulePrerequisiteNames))
        .toString();
  }
}

class LockInfoBuilder implements Builder<LockInfo, LockInfoBuilder> {
  _$LockInfo? _$v;

  LockedModuleBuilder? _contextModule;
  LockedModuleBuilder get contextModule =>
      _$this._contextModule ??= new LockedModuleBuilder();
  set contextModule(LockedModuleBuilder? contextModule) =>
      _$this._contextModule = contextModule;

  DateTime? _unlockAt;
  DateTime? get unlockAt => _$this._unlockAt;
  set unlockAt(DateTime? unlockAt) => _$this._unlockAt = unlockAt;

  List<String>? _modulePrerequisiteNames;
  List<String>? get modulePrerequisiteNames => _$this._modulePrerequisiteNames;
  set modulePrerequisiteNames(List<String>? modulePrerequisiteNames) =>
      _$this._modulePrerequisiteNames = modulePrerequisiteNames;

  LockInfoBuilder();

  LockInfoBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _contextModule = $v.contextModule?.toBuilder();
      _unlockAt = $v.unlockAt;
      _modulePrerequisiteNames = $v.modulePrerequisiteNames;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(LockInfo other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$LockInfo;
  }

  @override
  void update(void Function(LockInfoBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  LockInfo build() => _build();

  _$LockInfo _build() {
    _$LockInfo _$result;
    try {
      _$result = _$v ??
          new _$LockInfo._(
              contextModule: _contextModule?.build(),
              unlockAt: unlockAt,
              modulePrerequisiteNames: modulePrerequisiteNames);
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'contextModule';
        _contextModule?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'LockInfo', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
