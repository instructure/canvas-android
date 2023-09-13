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
  Iterable<Object?> serialize(
      Serializers serializers, MobileVerifyResult object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'authorized',
      serializers.serialize(object.authorized,
          specifiedType: const FullType(bool)),
      'result',
      serializers.serialize(object.result,
          specifiedType: const FullType(VerifyResultEnum)),
      'client_id',
      serializers.serialize(object.clientId,
          specifiedType: const FullType(String)),
      'client_secret',
      serializers.serialize(object.clientSecret,
          specifiedType: const FullType(String)),
      'api_key',
      serializers.serialize(object.apiKey,
          specifiedType: const FullType(String)),
      'base_url',
      serializers.serialize(object.baseUrl,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  MobileVerifyResult deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new MobileVerifyResultBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'authorized':
          result.authorized = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'result':
          result.result = serializers.deserialize(value,
                  specifiedType: const FullType(VerifyResultEnum))!
              as VerifyResultEnum;
          break;
        case 'client_id':
          result.clientId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'client_secret':
          result.clientSecret = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'api_key':
          result.apiKey = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'base_url':
          result.baseUrl = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
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
  final VerifyResultEnum result;
  @override
  final String clientId;
  @override
  final String clientSecret;
  @override
  final String apiKey;
  @override
  final String baseUrl;

  factory _$MobileVerifyResult(
          [void Function(MobileVerifyResultBuilder)? updates]) =>
      (new MobileVerifyResultBuilder()..update(updates))._build();

  _$MobileVerifyResult._(
      {required this.authorized,
      required this.result,
      required this.clientId,
      required this.clientSecret,
      required this.apiKey,
      required this.baseUrl})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        authorized, r'MobileVerifyResult', 'authorized');
    BuiltValueNullFieldError.checkNotNull(
        result, r'MobileVerifyResult', 'result');
    BuiltValueNullFieldError.checkNotNull(
        clientId, r'MobileVerifyResult', 'clientId');
    BuiltValueNullFieldError.checkNotNull(
        clientSecret, r'MobileVerifyResult', 'clientSecret');
    BuiltValueNullFieldError.checkNotNull(
        apiKey, r'MobileVerifyResult', 'apiKey');
    BuiltValueNullFieldError.checkNotNull(
        baseUrl, r'MobileVerifyResult', 'baseUrl');
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
        clientSecret == other.clientSecret &&
        apiKey == other.apiKey &&
        baseUrl == other.baseUrl;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, authorized.hashCode);
    _$hash = $jc(_$hash, result.hashCode);
    _$hash = $jc(_$hash, clientId.hashCode);
    _$hash = $jc(_$hash, clientSecret.hashCode);
    _$hash = $jc(_$hash, apiKey.hashCode);
    _$hash = $jc(_$hash, baseUrl.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'MobileVerifyResult')
          ..add('authorized', authorized)
          ..add('result', result)
          ..add('clientId', clientId)
          ..add('clientSecret', clientSecret)
          ..add('apiKey', apiKey)
          ..add('baseUrl', baseUrl))
        .toString();
  }
}

class MobileVerifyResultBuilder
    implements Builder<MobileVerifyResult, MobileVerifyResultBuilder> {
  _$MobileVerifyResult? _$v;

  bool? _authorized;
  bool? get authorized => _$this._authorized;
  set authorized(bool? authorized) => _$this._authorized = authorized;

  VerifyResultEnum? _result;
  VerifyResultEnum? get result => _$this._result;
  set result(VerifyResultEnum? result) => _$this._result = result;

  String? _clientId;
  String? get clientId => _$this._clientId;
  set clientId(String? clientId) => _$this._clientId = clientId;

  String? _clientSecret;
  String? get clientSecret => _$this._clientSecret;
  set clientSecret(String? clientSecret) => _$this._clientSecret = clientSecret;

  String? _apiKey;
  String? get apiKey => _$this._apiKey;
  set apiKey(String? apiKey) => _$this._apiKey = apiKey;

  String? _baseUrl;
  String? get baseUrl => _$this._baseUrl;
  set baseUrl(String? baseUrl) => _$this._baseUrl = baseUrl;

  MobileVerifyResultBuilder() {
    MobileVerifyResult._initializeBuilder(this);
  }

  MobileVerifyResultBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _authorized = $v.authorized;
      _result = $v.result;
      _clientId = $v.clientId;
      _clientSecret = $v.clientSecret;
      _apiKey = $v.apiKey;
      _baseUrl = $v.baseUrl;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(MobileVerifyResult other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$MobileVerifyResult;
  }

  @override
  void update(void Function(MobileVerifyResultBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  MobileVerifyResult build() => _build();

  _$MobileVerifyResult _build() {
    final _$result = _$v ??
        new _$MobileVerifyResult._(
            authorized: BuiltValueNullFieldError.checkNotNull(
                authorized, r'MobileVerifyResult', 'authorized'),
            result: BuiltValueNullFieldError.checkNotNull(
                result, r'MobileVerifyResult', 'result'),
            clientId: BuiltValueNullFieldError.checkNotNull(
                clientId, r'MobileVerifyResult', 'clientId'),
            clientSecret: BuiltValueNullFieldError.checkNotNull(
                clientSecret, r'MobileVerifyResult', 'clientSecret'),
            apiKey: BuiltValueNullFieldError.checkNotNull(
                apiKey, r'MobileVerifyResult', 'apiKey'),
            baseUrl: BuiltValueNullFieldError.checkNotNull(
                baseUrl, r'MobileVerifyResult', 'baseUrl'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
