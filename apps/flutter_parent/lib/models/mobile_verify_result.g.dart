// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mobile_verify_result.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<MobileVerifyResult> _$mobileVerifyResultSerializer =
    new _$MobileVerifyResultSerializer();

class _$MobileVerifyResultSerializer
    implements StructuredSerializer<MobileVerifyResult> {
  @override
  final Iterable<Type> types = const [MobileVerifyResult, _$MobileVerifyResult];
  @override
  final String wireName = 'MobileVerifyResult';

  @override
  Iterable<Object> serialize(Serializers serializers, MobileVerifyResult object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'authorized',
      serializers.serialize(object.authorized,
          specifiedType: const FullType(bool)),
      'result',
      serializers.serialize(object.result, specifiedType: const FullType(int)),
      'client_id',
      serializers.serialize(object.clientId,
          specifiedType: const FullType(String)),
      'api_key',
      serializers.serialize(object.apiKey,
          specifiedType: const FullType(String)),
      'client_secret',
      serializers.serialize(object.clientSecret,
          specifiedType: const FullType(String)),
      'base_url',
      serializers.serialize(object.baseUrl,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  MobileVerifyResult deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new MobileVerifyResultBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'authorized':
          result.authorized = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'result':
          result.result = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'client_id':
          result.clientId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'api_key':
          result.apiKey = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'client_secret':
          result.clientSecret = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'base_url':
          result.baseUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$MobileVerifyResult extends MobileVerifyResult {
  @override
  final bool authorized;
  @override
  final int result;
  @override
  final String clientId;
  @override
  final String apiKey;
  @override
  final String clientSecret;
  @override
  final String baseUrl;

  factory _$MobileVerifyResult(
          [void Function(MobileVerifyResultBuilder) updates]) =>
      (new MobileVerifyResultBuilder()..update(updates)).build();

  _$MobileVerifyResult._(
      {this.authorized,
      this.result,
      this.clientId,
      this.apiKey,
      this.clientSecret,
      this.baseUrl})
      : super._() {
    if (authorized == null) {
      throw new BuiltValueNullFieldError('MobileVerifyResult', 'authorized');
    }
    if (result == null) {
      throw new BuiltValueNullFieldError('MobileVerifyResult', 'result');
    }
    if (clientId == null) {
      throw new BuiltValueNullFieldError('MobileVerifyResult', 'clientId');
    }
    if (apiKey == null) {
      throw new BuiltValueNullFieldError('MobileVerifyResult', 'apiKey');
    }
    if (clientSecret == null) {
      throw new BuiltValueNullFieldError('MobileVerifyResult', 'clientSecret');
    }
    if (baseUrl == null) {
      throw new BuiltValueNullFieldError('MobileVerifyResult', 'baseUrl');
    }
  }

  @override
  MobileVerifyResult rebuild(
          void Function(MobileVerifyResultBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  MobileVerifyResultBuilder toBuilder() =>
      new MobileVerifyResultBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is MobileVerifyResult &&
        authorized == other.authorized &&
        result == other.result &&
        clientId == other.clientId &&
        apiKey == other.apiKey &&
        clientSecret == other.clientSecret &&
        baseUrl == other.baseUrl;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc($jc($jc(0, authorized.hashCode), result.hashCode),
                    clientId.hashCode),
                apiKey.hashCode),
            clientSecret.hashCode),
        baseUrl.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('MobileVerifyResult')
          ..add('authorized', authorized)
          ..add('result', result)
          ..add('clientId', clientId)
          ..add('apiKey', apiKey)
          ..add('clientSecret', clientSecret)
          ..add('baseUrl', baseUrl))
        .toString();
  }
}

class MobileVerifyResultBuilder
    implements Builder<MobileVerifyResult, MobileVerifyResultBuilder> {
  _$MobileVerifyResult _$v;

  bool _authorized;
  bool get authorized => _$this._authorized;
  set authorized(bool authorized) => _$this._authorized = authorized;

  int _result;
  int get result => _$this._result;
  set result(int result) => _$this._result = result;

  String _clientId;
  String get clientId => _$this._clientId;
  set clientId(String clientId) => _$this._clientId = clientId;

  String _apiKey;
  String get apiKey => _$this._apiKey;
  set apiKey(String apiKey) => _$this._apiKey = apiKey;

  String _clientSecret;
  String get clientSecret => _$this._clientSecret;
  set clientSecret(String clientSecret) => _$this._clientSecret = clientSecret;

  String _baseUrl;
  String get baseUrl => _$this._baseUrl;
  set baseUrl(String baseUrl) => _$this._baseUrl = baseUrl;

  MobileVerifyResultBuilder();

  MobileVerifyResultBuilder get _$this {
    if (_$v != null) {
      _authorized = _$v.authorized;
      _result = _$v.result;
      _clientId = _$v.clientId;
      _apiKey = _$v.apiKey;
      _clientSecret = _$v.clientSecret;
      _baseUrl = _$v.baseUrl;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(MobileVerifyResult other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$MobileVerifyResult;
  }

  @override
  void update(void Function(MobileVerifyResultBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$MobileVerifyResult build() {
    final _$result = _$v ??
        new _$MobileVerifyResult._(
            authorized: authorized,
            result: result,
            clientId: clientId,
            apiKey: apiKey,
            clientSecret: clientSecret,
            baseUrl: baseUrl);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
