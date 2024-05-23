// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_enrollment_info.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateEnrollmentInfo> _$createEnrollmentInfoSerializer =
    new _$CreateEnrollmentInfoSerializer();

class _$CreateEnrollmentInfoSerializer
    implements StructuredSerializer<CreateEnrollmentInfo> {
  @override
  final Iterable<Type> types = const [
    CreateEnrollmentInfo,
    _$CreateEnrollmentInfo
  ];
  @override
  final String wireName = 'CreateEnrollmentInfo';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, CreateEnrollmentInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'user_id',
      serializers.serialize(object.userId,
          specifiedType: const FullType(String)),
      'type',
      serializers.serialize(object.type, specifiedType: const FullType(String)),
      'role',
      serializers.serialize(object.role, specifiedType: const FullType(String)),
      'enrollment_state',
      serializers.serialize(object.enrollmentState,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.associatedUserId;

    result
      ..add('associated_user_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  CreateEnrollmentInfo deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateEnrollmentInfoBuilder();

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
        case 'type':
          result.type = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'role':
          result.role = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'enrollment_state':
          result.enrollmentState = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'associated_user_id':
          result.associatedUserId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$CreateEnrollmentInfo extends CreateEnrollmentInfo {
  @override
  final String userId;
  @override
  final String type;
  @override
  final String role;
  @override
  final String enrollmentState;
  @override
  final String? associatedUserId;

  factory _$CreateEnrollmentInfo(
          [void Function(CreateEnrollmentInfoBuilder)? updates]) =>
      (new CreateEnrollmentInfoBuilder()..update(updates))._build();

  _$CreateEnrollmentInfo._(
      {required this.userId,
      required this.type,
      required this.role,
      required this.enrollmentState,
      this.associatedUserId})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        userId, r'CreateEnrollmentInfo', 'userId');
    BuiltValueNullFieldError.checkNotNull(
        type, r'CreateEnrollmentInfo', 'type');
    BuiltValueNullFieldError.checkNotNull(
        role, r'CreateEnrollmentInfo', 'role');
    BuiltValueNullFieldError.checkNotNull(
        enrollmentState, r'CreateEnrollmentInfo', 'enrollmentState');
  }

  @override
  CreateEnrollmentInfo rebuild(
          void Function(CreateEnrollmentInfoBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateEnrollmentInfoBuilder toBuilder() =>
      new CreateEnrollmentInfoBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateEnrollmentInfo &&
        userId == other.userId &&
        type == other.type &&
        role == other.role &&
        enrollmentState == other.enrollmentState &&
        associatedUserId == other.associatedUserId;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, type.hashCode);
    _$hash = $jc(_$hash, role.hashCode);
    _$hash = $jc(_$hash, enrollmentState.hashCode);
    _$hash = $jc(_$hash, associatedUserId.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateEnrollmentInfo')
          ..add('userId', userId)
          ..add('type', type)
          ..add('role', role)
          ..add('enrollmentState', enrollmentState)
          ..add('associatedUserId', associatedUserId))
        .toString();
  }
}

class CreateEnrollmentInfoBuilder
    implements Builder<CreateEnrollmentInfo, CreateEnrollmentInfoBuilder> {
  _$CreateEnrollmentInfo? _$v;

  String? _userId;
  String? get userId => _$this._userId;
  set userId(String? userId) => _$this._userId = userId;

  String? _type;
  String? get type => _$this._type;
  set type(String? type) => _$this._type = type;

  String? _role;
  String? get role => _$this._role;
  set role(String? role) => _$this._role = role;

  String? _enrollmentState;
  String? get enrollmentState => _$this._enrollmentState;
  set enrollmentState(String? enrollmentState) =>
      _$this._enrollmentState = enrollmentState;

  String? _associatedUserId;
  String? get associatedUserId => _$this._associatedUserId;
  set associatedUserId(String? associatedUserId) =>
      _$this._associatedUserId = associatedUserId;

  CreateEnrollmentInfoBuilder() {
    CreateEnrollmentInfo._initializeBuilder(this);
  }

  CreateEnrollmentInfoBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _userId = $v.userId;
      _type = $v.type;
      _role = $v.role;
      _enrollmentState = $v.enrollmentState;
      _associatedUserId = $v.associatedUserId;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateEnrollmentInfo other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateEnrollmentInfo;
  }

  @override
  void update(void Function(CreateEnrollmentInfoBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateEnrollmentInfo build() => _build();

  _$CreateEnrollmentInfo _build() {
    final _$result = _$v ??
        new _$CreateEnrollmentInfo._(
            userId: BuiltValueNullFieldError.checkNotNull(
                userId, r'CreateEnrollmentInfo', 'userId'),
            type: BuiltValueNullFieldError.checkNotNull(
                type, r'CreateEnrollmentInfo', 'type'),
            role: BuiltValueNullFieldError.checkNotNull(
                role, r'CreateEnrollmentInfo', 'role'),
            enrollmentState: BuiltValueNullFieldError.checkNotNull(
                enrollmentState, r'CreateEnrollmentInfo', 'enrollmentState'),
            associatedUserId: associatedUserId);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
