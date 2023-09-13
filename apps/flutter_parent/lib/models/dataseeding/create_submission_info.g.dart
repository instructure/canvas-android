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
  Iterable<Object?> serialize(
      Serializers serializers, CreateSubmissionInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[];
    Object? value;
    value = object.submissionType;

    result
      ..add('submission_type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.body;

    result
      ..add('body')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.url;

    result
      ..add('url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.userId;

    result
      ..add('user_id')
      ..add(serializers.serialize(value, specifiedType: const FullType(int)));
    value = object.submittedAt;

    result
      ..add('submitted_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));

    return result;
  }

  @override
  CreateSubmissionInfo deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateSubmissionInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'submission_type':
          result.submissionType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'body':
          result.body = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int?;
          break;
        case 'submitted_at':
          result.submittedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
      }
    }

    return result.build();
  }
}

class _$CreateSubmissionInfo extends CreateSubmissionInfo {
  @override
  final String? submissionType;
  @override
  final String? body;
  @override
  final String? url;
  @override
  final int? userId;
  @override
  final DateTime? submittedAt;

  factory _$CreateSubmissionInfo(
          [void Function(CreateSubmissionInfoBuilder)? updates]) =>
      (new CreateSubmissionInfoBuilder()..update(updates))._build();

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
    var _$hash = 0;
    _$hash = $jc(_$hash, submissionType.hashCode);
    _$hash = $jc(_$hash, body.hashCode);
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, submittedAt.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateSubmissionInfo')
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
  _$CreateSubmissionInfo? _$v;

  String? _submissionType;
  String? get submissionType => _$this._submissionType;
  set submissionType(String? submissionType) =>
      _$this._submissionType = submissionType;

  String? _body;
  String? get body => _$this._body;
  set body(String? body) => _$this._body = body;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  int? _userId;
  int? get userId => _$this._userId;
  set userId(int? userId) => _$this._userId = userId;

  DateTime? _submittedAt;
  DateTime? get submittedAt => _$this._submittedAt;
  set submittedAt(DateTime? submittedAt) => _$this._submittedAt = submittedAt;

  CreateSubmissionInfoBuilder() {
    CreateSubmissionInfo._initializeBuilder(this);
  }

  CreateSubmissionInfoBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _submissionType = $v.submissionType;
      _body = $v.body;
      _url = $v.url;
      _userId = $v.userId;
      _submittedAt = $v.submittedAt;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateSubmissionInfo other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateSubmissionInfo;
  }

  @override
  void update(void Function(CreateSubmissionInfoBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateSubmissionInfo build() => _build();

  _$CreateSubmissionInfo _build() {
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

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
