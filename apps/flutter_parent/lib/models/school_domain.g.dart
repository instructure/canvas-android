// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'school_domain.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<SchoolDomain> _$schoolDomainSerializer =
    new _$SchoolDomainSerializer();

class _$SchoolDomainSerializer implements StructuredSerializer<SchoolDomain> {
  @override
  final Iterable<Type> types = const [SchoolDomain, _$SchoolDomain];
  @override
  final String wireName = 'SchoolDomain';

  @override
  Iterable<Object?> serialize(Serializers serializers, SchoolDomain object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'domain',
      serializers.serialize(object.domain,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.name;

    result
      ..add('name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.authenticationProvider;

    result
      ..add('authentication_provider')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  SchoolDomain deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SchoolDomainBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'domain':
          result.domain = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'authentication_provider':
          result.authenticationProvider = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$SchoolDomain extends SchoolDomain {
  @override
  final String domain;
  @override
  final String? name;
  @override
  final String? authenticationProvider;

  factory _$SchoolDomain([void Function(SchoolDomainBuilder)? updates]) =>
      (new SchoolDomainBuilder()..update(updates))._build();

  _$SchoolDomain._(
      {required this.domain, this.name, this.authenticationProvider})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(domain, r'SchoolDomain', 'domain');
  }

  @override
  SchoolDomain rebuild(void Function(SchoolDomainBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SchoolDomainBuilder toBuilder() => new SchoolDomainBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is SchoolDomain &&
        domain == other.domain &&
        name == other.name &&
        authenticationProvider == other.authenticationProvider;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, domain.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, authenticationProvider.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'SchoolDomain')
          ..add('domain', domain)
          ..add('name', name)
          ..add('authenticationProvider', authenticationProvider))
        .toString();
  }
}

class SchoolDomainBuilder
    implements Builder<SchoolDomain, SchoolDomainBuilder> {
  _$SchoolDomain? _$v;

  String? _domain;
  String? get domain => _$this._domain;
  set domain(String? domain) => _$this._domain = domain;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _authenticationProvider;
  String? get authenticationProvider => _$this._authenticationProvider;
  set authenticationProvider(String? authenticationProvider) =>
      _$this._authenticationProvider = authenticationProvider;

  SchoolDomainBuilder();

  SchoolDomainBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _domain = $v.domain;
      _name = $v.name;
      _authenticationProvider = $v.authenticationProvider;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SchoolDomain other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$SchoolDomain;
  }

  @override
  void update(void Function(SchoolDomainBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  SchoolDomain build() => _build();

  _$SchoolDomain _build() {
    final _$result = _$v ??
        new _$SchoolDomain._(
            domain: BuiltValueNullFieldError.checkNotNull(
                domain, r'SchoolDomain', 'domain'),
            name: name,
            authenticationProvider: authenticationProvider);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
