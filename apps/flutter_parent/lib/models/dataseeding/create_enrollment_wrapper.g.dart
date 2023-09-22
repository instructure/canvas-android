// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_enrollment_wrapper.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateEnrollmentWrapper> _$createEnrollmentWrapperSerializer =
    new _$CreateEnrollmentWrapperSerializer();

class _$CreateEnrollmentWrapperSerializer
    implements StructuredSerializer<CreateEnrollmentWrapper> {
  @override
  final Iterable<Type> types = const [
    CreateEnrollmentWrapper,
    _$CreateEnrollmentWrapper
  ];
  @override
  final String wireName = 'CreateEnrollmentWrapper';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, CreateEnrollmentWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'enrollment',
      serializers.serialize(object.enrollment,
          specifiedType: const FullType(CreateEnrollmentInfo)),
    ];

    return result;
  }

  @override
  CreateEnrollmentWrapper deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateEnrollmentWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'enrollment':
          result.enrollment.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CreateEnrollmentInfo))!
              as CreateEnrollmentInfo);
          break;
      }
    }

    return result.build();
  }
}

class _$CreateEnrollmentWrapper extends CreateEnrollmentWrapper {
  @override
  final CreateEnrollmentInfo enrollment;

  factory _$CreateEnrollmentWrapper(
          [void Function(CreateEnrollmentWrapperBuilder)? updates]) =>
      (new CreateEnrollmentWrapperBuilder()..update(updates))._build();

  _$CreateEnrollmentWrapper._({required this.enrollment}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        enrollment, r'CreateEnrollmentWrapper', 'enrollment');
  }

  @override
  CreateEnrollmentWrapper rebuild(
          void Function(CreateEnrollmentWrapperBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateEnrollmentWrapperBuilder toBuilder() =>
      new CreateEnrollmentWrapperBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateEnrollmentWrapper && enrollment == other.enrollment;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, enrollment.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateEnrollmentWrapper')
          ..add('enrollment', enrollment))
        .toString();
  }
}

class CreateEnrollmentWrapperBuilder
    implements
        Builder<CreateEnrollmentWrapper, CreateEnrollmentWrapperBuilder> {
  _$CreateEnrollmentWrapper? _$v;

  CreateEnrollmentInfoBuilder? _enrollment;
  CreateEnrollmentInfoBuilder get enrollment =>
      _$this._enrollment ??= new CreateEnrollmentInfoBuilder();
  set enrollment(CreateEnrollmentInfoBuilder? enrollment) =>
      _$this._enrollment = enrollment;

  CreateEnrollmentWrapperBuilder() {
    CreateEnrollmentWrapper._initializeBuilder(this);
  }

  CreateEnrollmentWrapperBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _enrollment = $v.enrollment.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateEnrollmentWrapper other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateEnrollmentWrapper;
  }

  @override
  void update(void Function(CreateEnrollmentWrapperBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateEnrollmentWrapper build() => _build();

  _$CreateEnrollmentWrapper _build() {
    _$CreateEnrollmentWrapper _$result;
    try {
      _$result = _$v ??
          new _$CreateEnrollmentWrapper._(enrollment: enrollment.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'enrollment';
        enrollment.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CreateEnrollmentWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
