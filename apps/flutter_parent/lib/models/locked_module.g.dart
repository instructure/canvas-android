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
  Iterable<Object> serialize(Serializers serializers, LockedModule object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'context_id',
      serializers.serialize(object.contextId,
          specifiedType: const FullType(String)),
      'require_sequential_progress',
      serializers.serialize(object.isRequireSequentialProgress,
          specifiedType: const FullType(bool)),
    ];
    result.add('context_type');
    if (object.contextType == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.contextType,
          specifiedType: const FullType(String)));
    }
    result.add('name');
    if (object.name == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.name,
          specifiedType: const FullType(String)));
    }
    result.add('unlock_at');
    if (object.unlockAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.unlockAt,
          specifiedType: const FullType(DateTime)));
    }
    return result;
  }

  @override
  LockedModule deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new LockedModuleBuilder();

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
        case 'context_id':
          result.contextId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'context_type':
          result.contextType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'unlock_at':
          result.unlockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'require_sequential_progress':
          result.isRequireSequentialProgress = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
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
  final String contextType;
  @override
  final String name;
  @override
  final DateTime unlockAt;
  @override
  final bool isRequireSequentialProgress;

  factory _$LockedModule([void Function(LockedModuleBuilder) updates]) =>
      (new LockedModuleBuilder()..update(updates)).build();

  _$LockedModule._(
      {this.id,
      this.contextId,
      this.contextType,
      this.name,
      this.unlockAt,
      this.isRequireSequentialProgress})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('LockedModule', 'id');
    }
    if (contextId == null) {
      throw new BuiltValueNullFieldError('LockedModule', 'contextId');
    }
    if (isRequireSequentialProgress == null) {
      throw new BuiltValueNullFieldError(
          'LockedModule', 'isRequireSequentialProgress');
    }
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
    return $jf($jc(
        $jc(
            $jc(
                $jc($jc($jc(0, id.hashCode), contextId.hashCode),
                    contextType.hashCode),
                name.hashCode),
            unlockAt.hashCode),
        isRequireSequentialProgress.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('LockedModule')
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
  _$LockedModule _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _contextId;
  String get contextId => _$this._contextId;
  set contextId(String contextId) => _$this._contextId = contextId;

  String _contextType;
  String get contextType => _$this._contextType;
  set contextType(String contextType) => _$this._contextType = contextType;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  DateTime _unlockAt;
  DateTime get unlockAt => _$this._unlockAt;
  set unlockAt(DateTime unlockAt) => _$this._unlockAt = unlockAt;

  bool _isRequireSequentialProgress;
  bool get isRequireSequentialProgress => _$this._isRequireSequentialProgress;
  set isRequireSequentialProgress(bool isRequireSequentialProgress) =>
      _$this._isRequireSequentialProgress = isRequireSequentialProgress;

  LockedModuleBuilder();

  LockedModuleBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _contextId = _$v.contextId;
      _contextType = _$v.contextType;
      _name = _$v.name;
      _unlockAt = _$v.unlockAt;
      _isRequireSequentialProgress = _$v.isRequireSequentialProgress;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(LockedModule other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$LockedModule;
  }

  @override
  void update(void Function(LockedModuleBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$LockedModule build() {
    final _$result = _$v ??
        new _$LockedModule._(
            id: id,
            contextId: contextId,
            contextType: contextType,
            name: name,
            unlockAt: unlockAt,
            isRequireSequentialProgress: isRequireSequentialProgress);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
