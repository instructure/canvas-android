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
  Iterable<Object> serialize(
      Serializers serializers, CreateEnrollmentWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'enrollment',
      serializers.serialize(object.enrollment,
          specifiedType: const FullType(CreateEnrollmentInfo)),
    ];

    return result;
  }

  @override
  CreateEnrollmentWrapper deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateEnrollmentWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'enrollment':
          result.enrollment.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CreateEnrollmentInfo))
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
          [void Function(CreateEnrollmentWrapperBuilder) updates]) =>
      (new CreateEnrollmentWrapperBuilder()..update(updates)).build();

  _$CreateEnrollmentWrapper._({this.enrollment}) : super._() {
    if (enrollment == null) {
      throw new BuiltValueNullFieldError(
          'CreateEnrollmentWrapper', 'enrollment');
    }
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
    return $jf($jc(0, enrollment.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateEnrollmentWrapper')
          ..add('enrollment', enrollment))
        .toString();
  }
}

class CreateEnrollmentWrapperBuilder
    implements
        Builder<CreateEnrollmentWrapper, CreateEnrollmentWrapperBuilder> {
  _$CreateEnrollmentWrapper _$v;

  CreateEnrollmentInfoBuilder _enrollment;
  CreateEnrollmentInfoBuilder get enrollment =>
      _$this._enrollment ??= new CreateEnrollmentInfoBuilder();
  set enrollment(CreateEnrollmentInfoBuilder enrollment) =>
      _$this._enrollment = enrollment;

  CreateEnrollmentWrapperBuilder() {
    CreateEnrollmentWrapper._initializeBuilder(this);
  }

  CreateEnrollmentWrapperBuilder get _$this {
    if (_$v != null) {
      _enrollment = _$v.enrollment?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateEnrollmentWrapper other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateEnrollmentWrapper;
  }

  @override
  void update(void Function(CreateEnrollmentWrapperBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateEnrollmentWrapper build() {
    _$CreateEnrollmentWrapper _$result;
    try {
      _$result = _$v ??
          new _$CreateEnrollmentWrapper._(enrollment: enrollment.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'enrollment';
        enrollment.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CreateEnrollmentWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
