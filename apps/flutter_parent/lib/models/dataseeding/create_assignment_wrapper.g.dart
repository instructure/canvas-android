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
  Iterable<Object?> serialize(
      Serializers serializers, CreateAssignmentWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'assignment',
      serializers.serialize(object.assignment,
          specifiedType: const FullType(CreateAssignmentInfo)),
    ];

    return result;
  }

  @override
  CreateAssignmentWrapper deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateAssignmentWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'assignment':
          result.assignment.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CreateAssignmentInfo))!
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
          [void Function(CreateAssignmentWrapperBuilder)? updates]) =>
      (new CreateAssignmentWrapperBuilder()..update(updates))._build();

  _$CreateAssignmentWrapper._({required this.assignment}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        assignment, r'CreateAssignmentWrapper', 'assignment');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, assignment.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateAssignmentWrapper')
          ..add('assignment', assignment))
        .toString();
  }
}

class CreateAssignmentWrapperBuilder
    implements
        Builder<CreateAssignmentWrapper, CreateAssignmentWrapperBuilder> {
  _$CreateAssignmentWrapper? _$v;

  CreateAssignmentInfoBuilder? _assignment;
  CreateAssignmentInfoBuilder get assignment =>
      _$this._assignment ??= new CreateAssignmentInfoBuilder();
  set assignment(CreateAssignmentInfoBuilder? assignment) =>
      _$this._assignment = assignment;

  CreateAssignmentWrapperBuilder() {
    CreateAssignmentWrapper._initializeBuilder(this);
  }

  CreateAssignmentWrapperBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _assignment = $v.assignment.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateAssignmentWrapper other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateAssignmentWrapper;
  }

  @override
  void update(void Function(CreateAssignmentWrapperBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateAssignmentWrapper build() => _build();

  _$CreateAssignmentWrapper _build() {
    _$CreateAssignmentWrapper _$result;
    try {
      _$result = _$v ??
          new _$CreateAssignmentWrapper._(assignment: assignment.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'assignment';
        assignment.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CreateAssignmentWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
