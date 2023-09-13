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
  Iterable<Object?> serialize(Serializers serializers, PairingCode object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
  PairingCode deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PairingCodeBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'code':
          result.code = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'expires_at':
          result.expiresAt = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
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

  factory _$PairingCode([void Function(PairingCodeBuilder)? updates]) =>
      (new PairingCodeBuilder()..update(updates))._build();

  _$PairingCode._(
      {required this.userId,
      required this.code,
      required this.expiresAt,
      required this.workflowState})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(userId, r'PairingCode', 'userId');
    BuiltValueNullFieldError.checkNotNull(code, r'PairingCode', 'code');
    BuiltValueNullFieldError.checkNotNull(
        expiresAt, r'PairingCode', 'expiresAt');
    BuiltValueNullFieldError.checkNotNull(
        workflowState, r'PairingCode', 'workflowState');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, code.hashCode);
    _$hash = $jc(_$hash, expiresAt.hashCode);
    _$hash = $jc(_$hash, workflowState.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PairingCode')
          ..add('userId', userId)
          ..add('code', code)
          ..add('expiresAt', expiresAt)
          ..add('workflowState', workflowState))
        .toString();
  }
}

class PairingCodeBuilder implements Builder<PairingCode, PairingCodeBuilder> {
  _$PairingCode? _$v;

  String? _userId;
  String? get userId => _$this._userId;
  set userId(String? userId) => _$this._userId = userId;

  String? _code;
  String? get code => _$this._code;
  set code(String? code) => _$this._code = code;

  String? _expiresAt;
  String? get expiresAt => _$this._expiresAt;
  set expiresAt(String? expiresAt) => _$this._expiresAt = expiresAt;

  String? _workflowState;
  String? get workflowState => _$this._workflowState;
  set workflowState(String? workflowState) =>
      _$this._workflowState = workflowState;

  PairingCodeBuilder() {
    PairingCode._initializeBuilder(this);
  }

  PairingCodeBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _userId = $v.userId;
      _code = $v.code;
      _expiresAt = $v.expiresAt;
      _workflowState = $v.workflowState;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PairingCode other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$PairingCode;
  }

  @override
  void update(void Function(PairingCodeBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PairingCode build() => _build();

  _$PairingCode _build() {
    final _$result = _$v ??
        new _$PairingCode._(
            userId: BuiltValueNullFieldError.checkNotNull(
                userId, r'PairingCode', 'userId'),
            code: BuiltValueNullFieldError.checkNotNull(
                code, r'PairingCode', 'code'),
            expiresAt: BuiltValueNullFieldError.checkNotNull(
                expiresAt, r'PairingCode', 'expiresAt'),
            workflowState: BuiltValueNullFieldError.checkNotNull(
                workflowState, r'PairingCode', 'workflowState'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
