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
  Iterable<Object> serialize(
      Serializers serializers, CreateEnrollmentInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
    result.add('associated_user_id');
    if (object.associatedUserId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.associatedUserId,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  CreateEnrollmentInfo deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateEnrollmentInfoBuilder();

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
        case 'type':
          result.type = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'role':
          result.role = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'enrollment_state':
          result.enrollmentState = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'associated_user_id':
          result.associatedUserId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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
  final String associatedUserId;

  factory _$CreateEnrollmentInfo(
          [void Function(CreateEnrollmentInfoBuilder) updates]) =>
      (new CreateEnrollmentInfoBuilder()..update(updates)).build();

  _$CreateEnrollmentInfo._(
      {this.userId,
      this.type,
      this.role,
      this.enrollmentState,
      this.associatedUserId})
      : super._() {
    if (userId == null) {
      throw new BuiltValueNullFieldError('CreateEnrollmentInfo', 'userId');
    }
    if (type == null) {
      throw new BuiltValueNullFieldError('CreateEnrollmentInfo', 'type');
    }
    if (role == null) {
      throw new BuiltValueNullFieldError('CreateEnrollmentInfo', 'role');
    }
    if (enrollmentState == null) {
      throw new BuiltValueNullFieldError(
          'CreateEnrollmentInfo', 'enrollmentState');
    }
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
    return $jf($jc(
        $jc($jc($jc($jc(0, userId.hashCode), type.hashCode), role.hashCode),
            enrollmentState.hashCode),
        associatedUserId.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateEnrollmentInfo')
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
  _$CreateEnrollmentInfo _$v;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  String _type;
  String get type => _$this._type;
  set type(String type) => _$this._type = type;

  String _role;
  String get role => _$this._role;
  set role(String role) => _$this._role = role;

  String _enrollmentState;
  String get enrollmentState => _$this._enrollmentState;
  set enrollmentState(String enrollmentState) =>
      _$this._enrollmentState = enrollmentState;

  String _associatedUserId;
  String get associatedUserId => _$this._associatedUserId;
  set associatedUserId(String associatedUserId) =>
      _$this._associatedUserId = associatedUserId;

  CreateEnrollmentInfoBuilder() {
    CreateEnrollmentInfo._initializeBuilder(this);
  }

  CreateEnrollmentInfoBuilder get _$this {
    if (_$v != null) {
      _userId = _$v.userId;
      _type = _$v.type;
      _role = _$v.role;
      _enrollmentState = _$v.enrollmentState;
      _associatedUserId = _$v.associatedUserId;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateEnrollmentInfo other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateEnrollmentInfo;
  }

  @override
  void update(void Function(CreateEnrollmentInfoBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateEnrollmentInfo build() {
    final _$result = _$v ??
        new _$CreateEnrollmentInfo._(
            userId: userId,
            type: type,
            role: role,
            enrollmentState: enrollmentState,
            associatedUserId: associatedUserId);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
