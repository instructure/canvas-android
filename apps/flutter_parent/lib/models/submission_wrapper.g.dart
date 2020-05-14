// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'submission_wrapper.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$SubmissionWrapper extends SubmissionWrapper {
  @override
  final Submission submission;
  @override
  final BuiltList<Submission> submissionList;

  factory _$SubmissionWrapper(
          [void Function(SubmissionWrapperBuilder) updates]) =>
      (new SubmissionWrapperBuilder()..update(updates)).build();

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
    return $jf($jc($jc(0, submission.hashCode), submissionList.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('SubmissionWrapper')
          ..add('submission', submission)
          ..add('submissionList', submissionList))
        .toString();
  }
}

class SubmissionWrapperBuilder
    implements Builder<SubmissionWrapper, SubmissionWrapperBuilder> {
  _$SubmissionWrapper _$v;

  SubmissionBuilder _submission;
  SubmissionBuilder get submission =>
      _$this._submission ??= new SubmissionBuilder();
  set submission(SubmissionBuilder submission) =>
      _$this._submission = submission;

  ListBuilder<Submission> _submissionList;
  ListBuilder<Submission> get submissionList =>
      _$this._submissionList ??= new ListBuilder<Submission>();
  set submissionList(ListBuilder<Submission> submissionList) =>
      _$this._submissionList = submissionList;

  SubmissionWrapperBuilder();

  SubmissionWrapperBuilder get _$this {
    if (_$v != null) {
      _submission = _$v.submission?.toBuilder();
      _submissionList = _$v.submissionList?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SubmissionWrapper other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$SubmissionWrapper;
  }

  @override
  void update(void Function(SubmissionWrapperBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$SubmissionWrapper build() {
    _$SubmissionWrapper _$result;
    try {
      _$result = _$v ??
          new _$SubmissionWrapper._(
              submission: _submission?.build(),
              submissionList: _submissionList?.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'submission';
        _submission?.build();
        _$failedField = 'submissionList';
        _submissionList?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'SubmissionWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
