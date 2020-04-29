// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pairing_code.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<PairingCode> _$pairingCodeSerializer = new _$PairingCodeSerializer();

class _$PairingCodeSerializer implements StructuredSerializer<PairingCode> {
  @override
  final Iterable<Type> types = const [PairingCode, _$PairingCode];
  @override
  final String wireName = 'PairingCode';

  @override
  Iterable<Object> serialize(Serializers serializers, PairingCode object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'user_id',
      serializers.serialize(object.userId,
          specifiedType: const FullType(String)),
      'code',
      serializers.serialize(object.code, specifiedType: const FullType(String)),
      'expires_at',
      serializers.serialize(object.expiresAt,
          specifiedType: const FullType(String)),
      'workflow_state',
      serializers.serialize(object.workflowState,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  PairingCode deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PairingCodeBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'code':
          result.code = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'expires_at':
          result.expiresAt = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$PairingCode extends PairingCode {
  @override
  final String userId;
  @override
  final String code;
  @override
  final String expiresAt;
  @override
  final String workflowState;

  factory _$PairingCode([void Function(PairingCodeBuilder) updates]) =>
      (new PairingCodeBuilder()..update(updates)).build();

  _$PairingCode._({this.userId, this.code, this.expiresAt, this.workflowState})
      : super._() {
    if (userId == null) {
      throw new BuiltValueNullFieldError('PairingCode', 'userId');
    }
    if (code == null) {
      throw new BuiltValueNullFieldError('PairingCode', 'code');
    }
    if (expiresAt == null) {
      throw new BuiltValueNullFieldError('PairingCode', 'expiresAt');
    }
    if (workflowState == null) {
      throw new BuiltValueNullFieldError('PairingCode', 'workflowState');
    }
  }

  @override
  PairingCode rebuild(void Function(PairingCodeBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PairingCodeBuilder toBuilder() => new PairingCodeBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PairingCode &&
        userId == other.userId &&
        code == other.code &&
        expiresAt == other.expiresAt &&
        workflowState == other.workflowState;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc(0, userId.hashCode), code.hashCode), expiresAt.hashCode),
        workflowState.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('PairingCode')
          ..add('userId', userId)
          ..add('code', code)
          ..add('expiresAt', expiresAt)
          ..add('workflowState', workflowState))
        .toString();
  }
}

class PairingCodeBuilder implements Builder<PairingCode, PairingCodeBuilder> {
  _$PairingCode _$v;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  String _code;
  String get code => _$this._code;
  set code(String code) => _$this._code = code;

  String _expiresAt;
  String get expiresAt => _$this._expiresAt;
  set expiresAt(String expiresAt) => _$this._expiresAt = expiresAt;

  String _workflowState;
  String get workflowState => _$this._workflowState;
  set workflowState(String workflowState) =>
      _$this._workflowState = workflowState;

  PairingCodeBuilder() {
    PairingCode._initializeBuilder(this);
  }

  PairingCodeBuilder get _$this {
    if (_$v != null) {
      _userId = _$v.userId;
      _code = _$v.code;
      _expiresAt = _$v.expiresAt;
      _workflowState = _$v.workflowState;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PairingCode other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$PairingCode;
  }

  @override
  void update(void Function(PairingCodeBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$PairingCode build() {
    final _$result = _$v ??
        new _$PairingCode._(
            userId: userId,
            code: code,
            expiresAt: expiresAt,
            workflowState: workflowState);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
