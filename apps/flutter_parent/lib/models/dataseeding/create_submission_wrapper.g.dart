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
  Iterable<Object?> serialize(
      Serializers serializers, CreateSubmissionWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'submission',
      serializers.serialize(object.submission,
          specifiedType: const FullType(CreateSubmissionInfo)),
    ];

    return result;
  }

  @override
  CreateSubmissionWrapper deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateSubmissionWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'submission':
          result.submission.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CreateSubmissionInfo))!
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
          [void Function(CreateSubmissionWrapperBuilder)? updates]) =>
      (new CreateSubmissionWrapperBuilder()..update(updates))._build();

  _$CreateSubmissionWrapper._({required this.submission}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        submission, r'CreateSubmissionWrapper', 'submission');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, submission.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateSubmissionWrapper')
          ..add('submission', submission))
        .toString();
  }
}

class CreateSubmissionWrapperBuilder
    implements
        Builder<CreateSubmissionWrapper, CreateSubmissionWrapperBuilder> {
  _$CreateSubmissionWrapper? _$v;

  CreateSubmissionInfoBuilder? _submission;
  CreateSubmissionInfoBuilder get submission =>
      _$this._submission ??= new CreateSubmissionInfoBuilder();
  set submission(CreateSubmissionInfoBuilder? submission) =>
      _$this._submission = submission;

  CreateSubmissionWrapperBuilder() {
    CreateSubmissionWrapper._initializeBuilder(this);
  }

  CreateSubmissionWrapperBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _submission = $v.submission.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateSubmissionWrapper other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateSubmissionWrapper;
  }

  @override
  void update(void Function(CreateSubmissionWrapperBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateSubmissionWrapper build() => _build();

  _$CreateSubmissionWrapper _build() {
    _$CreateSubmissionWrapper _$result;
    try {
      _$result = _$v ??
          new _$CreateSubmissionWrapper._(submission: submission.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'submission';
        submission.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CreateSubmissionWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
