// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'submission_wrapper.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$SubmissionWrapper extends SubmissionWrapper {
  @override
  final Submission? submission;
  @override
  final BuiltList<Submission>? submissionList;

  factory _$SubmissionWrapper(
          [void Function(SubmissionWrapperBuilder)? updates]) =>
      (new SubmissionWrapperBuilder()..update(updates))._build();

  _$SubmissionWrapper._({this.submission, this.submissionList}) : super._();

  @override
  SubmissionWrapper rebuild(void Function(SubmissionWrapperBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SubmissionWrapperBuilder toBuilder() =>
      new SubmissionWrapperBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is SubmissionWrapper &&
        submission == other.submission &&
        submissionList == other.submissionList;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, submission.hashCode);
    _$hash = $jc(_$hash, submissionList.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'SubmissionWrapper')
          ..add('submission', submission)
          ..add('submissionList', submissionList))
        .toString();
  }
}

class SubmissionWrapperBuilder
    implements Builder<SubmissionWrapper, SubmissionWrapperBuilder> {
  _$SubmissionWrapper? _$v;

  SubmissionBuilder? _submission;
  SubmissionBuilder get submission =>
      _$this._submission ??= new SubmissionBuilder();
  set submission(SubmissionBuilder? submission) =>
      _$this._submission = submission;

  ListBuilder<Submission>? _submissionList;
  ListBuilder<Submission> get submissionList =>
      _$this._submissionList ??= new ListBuilder<Submission>();
  set submissionList(ListBuilder<Submission>? submissionList) =>
      _$this._submissionList = submissionList;

  SubmissionWrapperBuilder();

  SubmissionWrapperBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _submission = $v.submission?.toBuilder();
      _submissionList = $v.submissionList?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SubmissionWrapper other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$SubmissionWrapper;
  }

  @override
  void update(void Function(SubmissionWrapperBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  SubmissionWrapper build() => _build();

  _$SubmissionWrapper _build() {
    _$SubmissionWrapper _$result;
    try {
      _$result = _$v ??
          new _$SubmissionWrapper._(
              submission: _submission?.build(),
              submissionList: _submissionList?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'submission';
        _submission?.build();
        _$failedField = 'submissionList';
        _submissionList?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'SubmissionWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
