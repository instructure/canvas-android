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
  Iterable<Object?> serialize(
      Serializers serializers, GradeSubmissionWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'submission',
      serializers.serialize(object.submission,
          specifiedType: const FullType(GradeSubmissionInfo)),
    ];

    return result;
  }

  @override
  GradeSubmissionWrapper deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradeSubmissionWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'submission':
          result.submission.replace(serializers.deserialize(value,
                  specifiedType: const FullType(GradeSubmissionInfo))!
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
          [void Function(GradeSubmissionWrapperBuilder)? updates]) =>
      (new GradeSubmissionWrapperBuilder()..update(updates))._build();

  _$GradeSubmissionWrapper._({required this.submission}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        submission, r'GradeSubmissionWrapper', 'submission');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, submission.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'GradeSubmissionWrapper')
          ..add('submission', submission))
        .toString();
  }
}

class GradeSubmissionWrapperBuilder
    implements Builder<GradeSubmissionWrapper, GradeSubmissionWrapperBuilder> {
  _$GradeSubmissionWrapper? _$v;

  GradeSubmissionInfoBuilder? _submission;
  GradeSubmissionInfoBuilder get submission =>
      _$this._submission ??= new GradeSubmissionInfoBuilder();
  set submission(GradeSubmissionInfoBuilder? submission) =>
      _$this._submission = submission;

  GradeSubmissionWrapperBuilder() {
    GradeSubmissionWrapper._initializeBuilder(this);
  }

  GradeSubmissionWrapperBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _submission = $v.submission.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradeSubmissionWrapper other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$GradeSubmissionWrapper;
  }

  @override
  void update(void Function(GradeSubmissionWrapperBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  GradeSubmissionWrapper build() => _build();

  _$GradeSubmissionWrapper _build() {
    _$GradeSubmissionWrapper _$result;
    try {
      _$result =
          _$v ?? new _$GradeSubmissionWrapper._(submission: submission.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'submission';
        submission.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'GradeSubmissionWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
