// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'locked_module.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<LockedModule> _$lockedModuleSerializer =
    new _$LockedModuleSerializer();

class _$LockedModuleSerializer implements StructuredSerializer<LockedModule> {
  @override
  final Iterable<Type> types = const [LockedModule, _$LockedModule];
  @override
  final String wireName = 'LockedModule';

  @override
  Iterable<Object?> serialize(Serializers serializers, LockedModule object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'context_id',
      serializers.serialize(object.contextId,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.contextType;

    result
      ..add('context_type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.name;

    result
      ..add('name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.unlockAt;

    result
      ..add('unlock_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.isRequireSequentialProgress;

    result
      ..add('require_sequential_progress')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));

    return result;
  }

  @override
  LockedModule deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new LockedModuleBuilder();

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
        case 'context_id':
          result.contextId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'context_type':
          result.contextType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'unlock_at':
          result.unlockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'require_sequential_progress':
          result.isRequireSequentialProgress = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
          break;
      }
    }

    return result.build();
  }
}

class _$LockedModule extends LockedModule {
  @override
  final String id;
  @override
  final String contextId;
  @override
  final String? contextType;
  @override
  final String? name;
  @override
  final DateTime? unlockAt;
  @override
  final bool? isRequireSequentialProgress;

  factory _$LockedModule([void Function(LockedModuleBuilder)? updates]) =>
      (new LockedModuleBuilder()..update(updates))._build();

  _$LockedModule._(
      {required this.id,
      required this.contextId,
      this.contextType,
      this.name,
      this.unlockAt,
      this.isRequireSequentialProgress})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'LockedModule', 'id');
    BuiltValueNullFieldError.checkNotNull(
        contextId, r'LockedModule', 'contextId');
  }

  @override
  LockedModule rebuild(void Function(LockedModuleBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  LockedModuleBuilder toBuilder() => new LockedModuleBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is LockedModule &&
        id == other.id &&
        contextId == other.contextId &&
        contextType == other.contextType &&
        name == other.name &&
        unlockAt == other.unlockAt &&
        isRequireSequentialProgress == other.isRequireSequentialProgress;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, contextId.hashCode);
    _$hash = $jc(_$hash, contextType.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, unlockAt.hashCode);
    _$hash = $jc(_$hash, isRequireSequentialProgress.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'LockedModule')
          ..add('id', id)
          ..add('contextId', contextId)
          ..add('contextType', contextType)
          ..add('name', name)
          ..add('unlockAt', unlockAt)
          ..add('isRequireSequentialProgress', isRequireSequentialProgress))
        .toString();
  }
}

class LockedModuleBuilder
    implements Builder<LockedModule, LockedModuleBuilder> {
  _$LockedModule? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _contextId;
  String? get contextId => _$this._contextId;
  set contextId(String? contextId) => _$this._contextId = contextId;

  String? _contextType;
  String? get contextType => _$this._contextType;
  set contextType(String? contextType) => _$this._contextType = contextType;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  DateTime? _unlockAt;
  DateTime? get unlockAt => _$this._unlockAt;
  set unlockAt(DateTime? unlockAt) => _$this._unlockAt = unlockAt;

  bool? _isRequireSequentialProgress;
  bool? get isRequireSequentialProgress => _$this._isRequireSequentialProgress;
  set isRequireSequentialProgress(bool? isRequireSequentialProgress) =>
      _$this._isRequireSequentialProgress = isRequireSequentialProgress;

  LockedModuleBuilder();

  LockedModuleBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _contextId = $v.contextId;
      _contextType = $v.contextType;
      _name = $v.name;
      _unlockAt = $v.unlockAt;
      _isRequireSequentialProgress = $v.isRequireSequentialProgress;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(LockedModule other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$LockedModule;
  }

  @override
  void update(void Function(LockedModuleBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  LockedModule build() => _build();

  _$LockedModule _build() {
    final _$result = _$v ??
        new _$LockedModule._(
            id: BuiltValueNullFieldError.checkNotNull(
                id, r'LockedModule', 'id'),
            contextId: BuiltValueNullFieldError.checkNotNull(
                contextId, r'LockedModule', 'contextId'),
            contextType: contextType,
            name: name,
            unlockAt: unlockAt,
            isRequireSequentialProgress: isRequireSequentialProgress);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
