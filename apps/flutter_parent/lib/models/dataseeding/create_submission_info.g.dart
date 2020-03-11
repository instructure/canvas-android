// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_submission_info.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateSubmissionInfo> _$createSubmissionInfoSerializer =
    new _$CreateSubmissionInfoSerializer();

class _$CreateSubmissionInfoSerializer
    implements StructuredSerializer<CreateSubmissionInfo> {
  @override
  final Iterable<Type> types = const [
    CreateSubmissionInfo,
    _$CreateSubmissionInfo
  ];
  @override
  final String wireName = 'CreateSubmissionInfo';

  @override
  Iterable<Object> serialize(
      Serializers serializers, CreateSubmissionInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[];
    result.add('submission_type');
    if (object.submissionType == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.submissionType,
          specifiedType: const FullType(String)));
    }
    result.add('body');
    if (object.body == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.body,
          specifiedType: const FullType(String)));
    }
    result.add('url');
    if (object.url == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.url,
          specifiedType: const FullType(String)));
    }
    result.add('user_id');
    if (object.userId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.userId,
          specifiedType: const FullType(int)));
    }
    result.add('submitted_at');
    if (object.submittedAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.submittedAt,
          specifiedType: const FullType(DateTime)));
    }
    return result;
  }

  @override
  CreateSubmissionInfo deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateSubmissionInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'submission_type':
          result.submissionType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'body':
          result.body = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'submitted_at':
          result.submittedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
      }
    }

    return result.build();
  }
}

class _$CreateSubmissionInfo extends CreateSubmissionInfo {
  @override
  final String submissionType;
  @override
  final String body;
  @override
  final String url;
  @override
  final int userId;
  @override
  final DateTime submittedAt;

  factory _$CreateSubmissionInfo(
          [void Function(CreateSubmissionInfoBuilder) updates]) =>
      (new CreateSubmissionInfoBuilder()..update(updates)).build();

  _$CreateSubmissionInfo._(
      {this.submissionType, this.body, this.url, this.userId, this.submittedAt})
      : super._();

  @override
  CreateSubmissionInfo rebuild(
          void Function(CreateSubmissionInfoBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateSubmissionInfoBuilder toBuilder() =>
      new CreateSubmissionInfoBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateSubmissionInfo &&
        submissionType == other.submissionType &&
        body == other.body &&
        url == other.url &&
        userId == other.userId &&
        submittedAt == other.submittedAt;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc($jc($jc(0, submissionType.hashCode), body.hashCode),
                url.hashCode),
            userId.hashCode),
        submittedAt.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateSubmissionInfo')
          ..add('submissionType', submissionType)
          ..add('body', body)
          ..add('url', url)
          ..add('userId', userId)
          ..add('submittedAt', submittedAt))
        .toString();
  }
}

class CreateSubmissionInfoBuilder
    implements Builder<CreateSubmissionInfo, CreateSubmissionInfoBuilder> {
  _$CreateSubmissionInfo _$v;

  String _submissionType;
  String get submissionType => _$this._submissionType;
  set submissionType(String submissionType) =>
      _$this._submissionType = submissionType;

  String _body;
  String get body => _$this._body;
  set body(String body) => _$this._body = body;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  int _userId;
  int get userId => _$this._userId;
  set userId(int userId) => _$this._userId = userId;

  DateTime _submittedAt;
  DateTime get submittedAt => _$this._submittedAt;
  set submittedAt(DateTime submittedAt) => _$this._submittedAt = submittedAt;

  CreateSubmissionInfoBuilder() {
    CreateSubmissionInfo._initializeBuilder(this);
  }

  CreateSubmissionInfoBuilder get _$this {
    if (_$v != null) {
      _submissionType = _$v.submissionType;
      _body = _$v.body;
      _url = _$v.url;
      _userId = _$v.userId;
      _submittedAt = _$v.submittedAt;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateSubmissionInfo other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateSubmissionInfo;
  }

  @override
  void update(void Function(CreateSubmissionInfoBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateSubmissionInfo build() {
    final _$result = _$v ??
        new _$CreateSubmissionInfo._(
            submissionType: submissionType,
            body: body,
            url: url,
            userId: userId,
            submittedAt: submittedAt);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
