// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_assignment_wrapper.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateAssignmentWrapper> _$createAssignmentWrapperSerializer =
    new _$CreateAssignmentWrapperSerializer();

class _$CreateAssignmentWrapperSerializer
    implements StructuredSerializer<CreateAssignmentWrapper> {
  @override
  final Iterable<Type> types = const [
    CreateAssignmentWrapper,
    _$CreateAssignmentWrapper
  ];
  @override
  final String wireName = 'CreateAssignmentWrapper';

  @override
  Iterable<Object> serialize(
      Serializers serializers, CreateAssignmentWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'assignment',
      serializers.serialize(object.assignment,
          specifiedType: const FullType(CreateAssignmentInfo)),
    ];

    return result;
  }

  @override
  CreateAssignmentWrapper deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateAssignmentWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'assignment':
          result.assignment.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CreateAssignmentInfo))
              as CreateAssignmentInfo);
          break;
      }
    }

    return result.build();
  }
}

class _$CreateAssignmentWrapper extends CreateAssignmentWrapper {
  @override
  final CreateAssignmentInfo assignment;

  factory _$CreateAssignmentWrapper(
          [void Function(CreateAssignmentWrapperBuilder) updates]) =>
      (new CreateAssignmentWrapperBuilder()..update(updates)).build();

  _$CreateAssignmentWrapper._({this.assignment}) : super._() {
    if (assignment == null) {
      throw new BuiltValueNullFieldError(
          'CreateAssignmentWrapper', 'assignment');
    }
  }

  @override
  CreateAssignmentWrapper rebuild(
          void Function(CreateAssignmentWrapperBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateAssignmentWrapperBuilder toBuilder() =>
      new CreateAssignmentWrapperBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateAssignmentWrapper && assignment == other.assignment;
  }

  @override
  int get hashCode {
    return $jf($jc(0, assignment.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateAssignmentWrapper')
          ..add('assignment', assignment))
        .toString();
  }
}

class CreateAssignmentWrapperBuilder
    implements
        Builder<CreateAssignmentWrapper, CreateAssignmentWrapperBuilder> {
  _$CreateAssignmentWrapper _$v;

  CreateAssignmentInfoBuilder _assignment;
  CreateAssignmentInfoBuilder get assignment =>
      _$this._assignment ??= new CreateAssignmentInfoBuilder();
  set assignment(CreateAssignmentInfoBuilder assignment) =>
      _$this._assignment = assignment;

  CreateAssignmentWrapperBuilder() {
    CreateAssignmentWrapper._initializeBuilder(this);
  }

  CreateAssignmentWrapperBuilder get _$this {
    if (_$v != null) {
      _assignment = _$v.assignment?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateAssignmentWrapper other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateAssignmentWrapper;
  }

  @override
  void update(void Function(CreateAssignmentWrapperBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateAssignmentWrapper build() {
    _$CreateAssignmentWrapper _$result;
    try {
      _$result = _$v ??
          new _$CreateAssignmentWrapper._(assignment: assignment.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'assignment';
        assignment.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CreateAssignmentWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
