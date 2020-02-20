// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'authenticated_url.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<AuthenticatedUrl> _$authenticatedUrlSerializer =
    new _$AuthenticatedUrlSerializer();

class _$AuthenticatedUrlSerializer
    implements StructuredSerializer<AuthenticatedUrl> {
  @override
  final Iterable<Type> types = const [AuthenticatedUrl, _$AuthenticatedUrl];
  @override
  final String wireName = 'AuthenticatedUrl';

  @override
  Iterable<Object> serialize(Serializers serializers, AuthenticatedUrl object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'session_url',
      serializers.serialize(object.sessionUrl,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  AuthenticatedUrl deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AuthenticatedUrlBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'session_url':
          result.sessionUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$AuthenticatedUrl extends AuthenticatedUrl {
  @override
  final String sessionUrl;

  factory _$AuthenticatedUrl(
          [void Function(AuthenticatedUrlBuilder) updates]) =>
      (new AuthenticatedUrlBuilder()..update(updates)).build();

  _$AuthenticatedUrl._({this.sessionUrl}) : super._() {
    if (sessionUrl == null) {
      throw new BuiltValueNullFieldError('AuthenticatedUrl', 'sessionUrl');
    }
  }

  @override
  AuthenticatedUrl rebuild(void Function(AuthenticatedUrlBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AuthenticatedUrlBuilder toBuilder() =>
      new AuthenticatedUrlBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is AuthenticatedUrl && sessionUrl == other.sessionUrl;
  }

  @override
  int get hashCode {
    return $jf($jc(0, sessionUrl.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('AuthenticatedUrl')
          ..add('sessionUrl', sessionUrl))
        .toString();
  }
}

class AuthenticatedUrlBuilder
    implements Builder<AuthenticatedUrl, AuthenticatedUrlBuilder> {
  _$AuthenticatedUrl _$v;

  String _sessionUrl;
  String get sessionUrl => _$this._sessionUrl;
  set sessionUrl(String sessionUrl) => _$this._sessionUrl = sessionUrl;

  AuthenticatedUrlBuilder() {
    AuthenticatedUrl._initializeBuilder(this);
  }

  AuthenticatedUrlBuilder get _$this {
    if (_$v != null) {
      _sessionUrl = _$v.sessionUrl;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AuthenticatedUrl other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$AuthenticatedUrl;
  }

  @override
  void update(void Function(AuthenticatedUrlBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$AuthenticatedUrl build() {
    final _$result = _$v ?? new _$AuthenticatedUrl._(sessionUrl: sessionUrl);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
