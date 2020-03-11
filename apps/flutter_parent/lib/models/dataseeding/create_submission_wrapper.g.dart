// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_submission_wrapper.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateSubmissionWrapper> _$createSubmissionWrapperSerializer =
    new _$CreateSubmissionWrapperSerializer();

class _$CreateSubmissionWrapperSerializer
    implements StructuredSerializer<CreateSubmissionWrapper> {
  @override
  final Iterable<Type> types = const [
    CreateSubmissionWrapper,
    _$CreateSubmissionWrapper
  ];
  @override
  final String wireName = 'CreateSubmissionWrapper';

  @override
  Iterable<Object> serialize(
      Serializers serializers, CreateSubmissionWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'submission',
      serializers.serialize(object.submission,
          specifiedType: const FullType(CreateSubmissionInfo)),
    ];

    return result;
  }

  @override
  CreateSubmissionWrapper deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateSubmissionWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'submission':
          result.submission.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CreateSubmissionInfo))
              as CreateSubmissionInfo);
          break;
      }
    }

    return result.build();
  }
}

class _$CreateSubmissionWrapper extends CreateSubmissionWrapper {
  @override
  final CreateSubmissionInfo submission;

  factory _$CreateSubmissionWrapper(
          [void Function(CreateSubmissionWrapperBuilder) updates]) =>
      (new CreateSubmissionWrapperBuilder()..update(updates)).build();

  _$CreateSubmissionWrapper._({this.submission}) : super._() {
    if (submission == null) {
      throw new BuiltValueNullFieldError(
          'CreateSubmissionWrapper', 'submission');
    }
  }

  @override
  CreateSubmissionWrapper rebuild(
          void Function(CreateSubmissionWrapperBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateSubmissionWrapperBuilder toBuilder() =>
      new CreateSubmissionWrapperBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateSubmissionWrapper && submission == other.submission;
  }

  @override
  int get hashCode {
    return $jf($jc(0, submission.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateSubmissionWrapper')
          ..add('submission', submission))
        .toString();
  }
}

class CreateSubmissionWrapperBuilder
    implements
        Builder<CreateSubmissionWrapper, CreateSubmissionWrapperBuilder> {
  _$CreateSubmissionWrapper _$v;

  CreateSubmissionInfoBuilder _submission;
  CreateSubmissionInfoBuilder get submission =>
      _$this._submission ??= new CreateSubmissionInfoBuilder();
  set submission(CreateSubmissionInfoBuilder submission) =>
      _$this._submission = submission;

  CreateSubmissionWrapperBuilder() {
    CreateSubmissionWrapper._initializeBuilder(this);
  }

  CreateSubmissionWrapperBuilder get _$this {
    if (_$v != null) {
      _submission = _$v.submission?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateSubmissionWrapper other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateSubmissionWrapper;
  }

  @override
  void update(void Function(CreateSubmissionWrapperBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateSubmissionWrapper build() {
    _$CreateSubmissionWrapper _$result;
    try {
      _$result = _$v ??
          new _$CreateSubmissionWrapper._(submission: submission.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'submission';
        submission.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CreateSubmissionWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
