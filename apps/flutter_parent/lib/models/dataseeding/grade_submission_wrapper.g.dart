// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'grade_submission_wrapper.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<GradeSubmissionWrapper> _$gradeSubmissionWrapperSerializer =
    new _$GradeSubmissionWrapperSerializer();

class _$GradeSubmissionWrapperSerializer
    implements StructuredSerializer<GradeSubmissionWrapper> {
  @override
  final Iterable<Type> types = const [
    GradeSubmissionWrapper,
    _$GradeSubmissionWrapper
  ];
  @override
  final String wireName = 'GradeSubmissionWrapper';

  @override
  Iterable<Object> serialize(
      Serializers serializers, GradeSubmissionWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'submission',
      serializers.serialize(object.submission,
          specifiedType: const FullType(GradeSubmissionInfo)),
    ];

    return result;
  }

  @override
  GradeSubmissionWrapper deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradeSubmissionWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'submission':
          result.submission.replace(serializers.deserialize(value,
                  specifiedType: const FullType(GradeSubmissionInfo))
              as GradeSubmissionInfo);
          break;
      }
    }

    return result.build();
  }
}

class _$GradeSubmissionWrapper extends GradeSubmissionWrapper {
  @override
  final GradeSubmissionInfo submission;

  factory _$GradeSubmissionWrapper(
          [void Function(GradeSubmissionWrapperBuilder) updates]) =>
      (new GradeSubmissionWrapperBuilder()..update(updates)).build();

  _$GradeSubmissionWrapper._({this.submission}) : super._() {
    if (submission == null) {
      throw new BuiltValueNullFieldError(
          'GradeSubmissionWrapper', 'submission');
    }
  }

  @override
  GradeSubmissionWrapper rebuild(
          void Function(GradeSubmissionWrapperBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  GradeSubmissionWrapperBuilder toBuilder() =>
      new GradeSubmissionWrapperBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is GradeSubmissionWrapper && submission == other.submission;
  }

  @override
  int get hashCode {
    return $jf($jc(0, submission.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('GradeSubmissionWrapper')
          ..add('submission', submission))
        .toString();
  }
}

class GradeSubmissionWrapperBuilder
    implements Builder<GradeSubmissionWrapper, GradeSubmissionWrapperBuilder> {
  _$GradeSubmissionWrapper _$v;

  GradeSubmissionInfoBuilder _submission;
  GradeSubmissionInfoBuilder get submission =>
      _$this._submission ??= new GradeSubmissionInfoBuilder();
  set submission(GradeSubmissionInfoBuilder submission) =>
      _$this._submission = submission;

  GradeSubmissionWrapperBuilder() {
    GradeSubmissionWrapper._initializeBuilder(this);
  }

  GradeSubmissionWrapperBuilder get _$this {
    if (_$v != null) {
      _submission = _$v.submission?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradeSubmissionWrapper other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$GradeSubmissionWrapper;
  }

  @override
  void update(void Function(GradeSubmissionWrapperBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$GradeSubmissionWrapper build() {
    _$GradeSubmissionWrapper _$result;
    try {
      _$result =
          _$v ?? new _$GradeSubmissionWrapper._(submission: submission.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'submission';
        submission.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'GradeSubmissionWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
